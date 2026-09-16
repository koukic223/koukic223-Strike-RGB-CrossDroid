package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.effects.EffectEngine
import com.example.effects.screensync.MediaProjectionHolder
import com.example.hardware.HardwareController
import com.example.model.EffectType
import com.example.model.LedState
import com.example.model.RgbColor
import com.example.model.ScreenRegion
import com.example.model.ScreenSyncConfig
import com.example.model.ScreenSyncDebugInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LedControlService : Service() {

    private val hardwareController = HardwareController()
    private var effectEngine: EffectEngine? = null

    private var wakeLock: PowerManager.WakeLock? = null
    private var currentLedState = LedState()
    private var keepLedOnScreenOff = true

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    val keepOn = if (currentLedState.currentEffect == EffectType.SCREEN_SYNC) {
                        currentLedState.screenSyncConfig.keepColorOnScreenOff
                    } else {
                        keepLedOnScreenOff
                    }
                    effectEngine?.pause(keepLedOn = keepOn)
                }
                Intent.ACTION_SCREEN_ON -> {
                    effectEngine?.resume()
                }
            }
        }
    }

    companion object {
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val ACTION_UPDATE = "com.example.service.ACTION_UPDATE"

        const val EXTRA_R = "extra_r"
        const val EXTRA_G = "extra_g"
        const val EXTRA_B = "extra_b"
        const val EXTRA_EFFECT = "extra_effect"
        const val EXTRA_BRIGHTNESS = "extra_brightness"
        const val EXTRA_SPEED = "extra_speed"
        const val EXTRA_BREATHING_SPEED = "extra_breathing_speed"
        const val EXTRA_INTENSITY = "extra_intensity"
        const val EXTRA_RUNNING = "extra_running"
        const val EXTRA_KEEP_SCREEN_OFF = "extra_keep_screen_off"

        // Screen Sync extras
        const val EXTRA_SYNC_INTENSITY = "extra_sync_intensity"
        const val EXTRA_SYNC_SMOOTHING = "extra_sync_smoothing"
        const val EXTRA_SYNC_COLOR_BOOST = "extra_sync_color_boost"
        const val EXTRA_SYNC_SAMPLING_RATE = "extra_sync_sampling_rate"
        const val EXTRA_SYNC_REGION = "extra_sync_region"
        const val EXTRA_SYNC_KEEP_OFF = "extra_sync_keep_off"

        const val CHANNEL_ID = "strike_rgb_channel"
        const val NOTIFICATION_ID = 1001

        private val _debugInfoFlow = MutableStateFlow(ScreenSyncDebugInfo())
        val debugInfoFlow: StateFlow<ScreenSyncDebugInfo> = _debugInfoFlow.asStateFlow()

        @Volatile
        var isRunning = false
            private set

        fun start(context: Context, state: LedState, keepScreenOff: Boolean) {
            val intent = createServiceIntent(context, ACTION_START, state, keepScreenOff)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            val intent = Intent(context, LedControlService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }

        fun update(context: Context, state: LedState, keepScreenOff: Boolean) {
            val intent = createServiceIntent(context, ACTION_UPDATE, state, keepScreenOff)
            try {
                context.startService(intent)
            } catch (_: Exception) {}
        }

        private fun createServiceIntent(
            context: Context,
            action: String,
            state: LedState,
            keepScreenOff: Boolean
        ): Intent {
            return Intent(context, LedControlService::class.java).apply {
                this.action = action
                putExtra(EXTRA_R, state.currentColor.r)
                putExtra(EXTRA_G, state.currentColor.g)
                putExtra(EXTRA_B, state.currentColor.b)
                putExtra(EXTRA_EFFECT, state.currentEffect.name)
                putExtra(EXTRA_BRIGHTNESS, state.brightness)
                putExtra(EXTRA_SPEED, state.effectSpeed)
                putExtra(EXTRA_BREATHING_SPEED, state.breathingSpeed)
                putExtra(EXTRA_INTENSITY, state.intensity)
                putExtra(EXTRA_RUNNING, state.isRunning)
                putExtra(EXTRA_KEEP_SCREEN_OFF, keepScreenOff)

                // Screen Sync extras
                val sync = state.screenSyncConfig
                putExtra(EXTRA_SYNC_INTENSITY, sync.intensity)
                putExtra(EXTRA_SYNC_SMOOTHING, sync.smoothing)
                putExtra(EXTRA_SYNC_COLOR_BOOST, sync.colorBoost)
                putExtra(EXTRA_SYNC_SAMPLING_RATE, sync.samplingRate)
                putExtra(EXTRA_SYNC_REGION, sync.region.name)
                putExtra(EXTRA_SYNC_KEEP_OFF, sync.keepColorOnScreenOff)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initWakeLock()

        effectEngine = EffectEngine.getInstance(applicationContext)

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        try {
            registerReceiver(screenStateReceiver, filter)
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_STOP -> {
                effectEngine?.stop(turnOffHardware = true)
                releaseWakeLock()
                isRunning = false
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_UPDATE -> {
                extractState(intent)
                startForegroundNotification()
                manageWakeLock()

                if (currentLedState.isRunning) {
                    effectEngine?.start(currentLedState)
                } else {
                    effectEngine?.stop(turnOffHardware = true)
                }
            }
        }

        isRunning = true
        return START_STICKY
    }

    private fun extractState(intent: Intent) {
        val r = intent.getIntExtra(EXTRA_R, currentLedState.currentColor.r)
        val g = intent.getIntExtra(EXTRA_G, currentLedState.currentColor.g)
        val b = intent.getIntExtra(EXTRA_B, currentLedState.currentColor.b)
        val effectName = intent.getStringExtra(EXTRA_EFFECT) ?: currentLedState.currentEffect.name
        val effect = try { EffectType.valueOf(effectName) } catch (_: Exception) { EffectType.STATIC }
        val brightness = intent.getFloatExtra(EXTRA_BRIGHTNESS, currentLedState.brightness)
        val speed = intent.getFloatExtra(EXTRA_SPEED, currentLedState.effectSpeed)
        val breathingSpeed = intent.getFloatExtra(EXTRA_BREATHING_SPEED, currentLedState.breathingSpeed)
        val intensity = intent.getFloatExtra(EXTRA_INTENSITY, currentLedState.intensity)
        val running = intent.getBooleanExtra(EXTRA_RUNNING, currentLedState.isRunning)
        keepLedOnScreenOff = intent.getBooleanExtra(EXTRA_KEEP_SCREEN_OFF, keepLedOnScreenOff)

        val syncIntensity = intent.getFloatExtra(EXTRA_SYNC_INTENSITY, currentLedState.screenSyncConfig.intensity)
        val syncSmoothing = intent.getFloatExtra(EXTRA_SYNC_SMOOTHING, currentLedState.screenSyncConfig.smoothing)
        val syncColorBoost = intent.getBooleanExtra(EXTRA_SYNC_COLOR_BOOST, currentLedState.screenSyncConfig.colorBoost)
        val syncSamplingRate = intent.getIntExtra(EXTRA_SYNC_SAMPLING_RATE, currentLedState.screenSyncConfig.samplingRate)
        val syncRegionName = intent.getStringExtra(EXTRA_SYNC_REGION) ?: currentLedState.screenSyncConfig.region.name
        val syncRegion = try { ScreenRegion.valueOf(syncRegionName) } catch (_: Exception) { ScreenRegion.FULL_SCREEN }
        val syncKeepOff = intent.getBooleanExtra(EXTRA_SYNC_KEEP_OFF, currentLedState.screenSyncConfig.keepColorOnScreenOff)

        val syncConfig = ScreenSyncConfig(
            isEnabled = effect == EffectType.SCREEN_SYNC,
            intensity = syncIntensity,
            brightness = brightness,
            smoothing = syncSmoothing,
            colorBoost = syncColorBoost,
            samplingRate = syncSamplingRate,
            region = syncRegion,
            keepColorOnScreenOff = syncKeepOff
        )

        currentLedState = LedState(
            isRunning = running,
            currentColor = RgbColor(r, g, b),
            currentEffect = effect,
            brightness = brightness,
            effectSpeed = speed,
            breathingSpeed = breathingSpeed,
            intensity = intensity,
            screenSyncConfig = syncConfig
        )
    }

    private fun startForegroundNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            }

            // In Android 14+, include MEDIA_PROJECTION when Screen Sync is active or permission available
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (currentLedState.currentEffect == EffectType.SCREEN_SYNC || MediaProjectionHolder.hasPermission) {
                    type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
                }
            }

            try {
                startForeground(NOTIFICATION_ID, notification, type)
            } catch (_: Exception) {
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, LedControlService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val effectTitle = currentLedState.currentEffect.name.replace("_", " ")
        val contentText = if (currentLedState.isRunning) {
            "LED Active • $effectTitle • Strike RGB CrossDroid"
        } else {
            "LED Paused"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Strike RGB CrossDroid")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(openPendingIntent)
            .addAction(0, "STOP", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Strike RGB Hardware Controller",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps RGB hardware LED effects running continuously in background and when screen is off"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun initWakeLock() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            // ONLY PARTIAL_WAKE_LOCK: Never keep screen awake or use SCREEN_BRIGHT_WAKE_LOCK
            wakeLock = powerManager?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "StrikeRGB:LedHardwareControllerWakeLock"
            )?.apply {
                setReferenceCounted(false)
            }
        } catch (_: Exception) {}
    }

    private fun manageWakeLock() {
        val shouldWake = if (currentLedState.currentEffect == EffectType.SCREEN_SYNC) {
            currentLedState.screenSyncConfig.keepColorOnScreenOff && currentLedState.isRunning
        } else {
            keepLedOnScreenOff && currentLedState.isRunning
        }

        if (shouldWake) {
            try {
                if (wakeLock?.isHeld == false) {
                    wakeLock?.acquire(24 * 60 * 60 * 1000L) // 24 hours max
                }
            } catch (_: Exception) {}
        } else {
            releaseWakeLock()
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (_: Exception) {}
        effectEngine?.stop(turnOffHardware = true)
        releaseWakeLock()
    }
}
