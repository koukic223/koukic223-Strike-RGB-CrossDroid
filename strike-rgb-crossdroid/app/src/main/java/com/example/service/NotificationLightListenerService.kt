package com.example.service

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.util.Log
import com.example.model.AppNotificationProfile
import com.example.model.LightFlowColorMode
import com.example.model.NotificationHistoryItem
import com.example.model.RgbColor
import com.example.notification.AppColorExtractor
import com.example.notification.LightFlowRepository
import com.example.notification.NotificationFlowEngine

/**
 * Android NotificationListenerService implementation for Light Flow:
 * - Catches incoming system notifications
 * - Evaluates automation conditions (Screen ON/OFF, Charging, DND)
 * - Resolves per-app LightFlowProfile & auto-extracted app colors
 * - Dispatches through NotificationFlowEngine to real physical RGB LED + Flash + AOD
 * - Records color and notification history
 */
class NotificationLightListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "NotificationLightListener"

        @Volatile
        private var instance: NotificationLightListenerService? = null

        fun isConnected(): Boolean = instance != null

        // Legacy compatibility profile map (delegates to LightFlowRepository)
        val appProfiles = mutableMapOf<String, AppNotificationProfile>()

        fun isPermissionGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":").toTypedArray()
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null && TextUtils.equals(packageName, cn.packageName)) {
                        return true
                    }
                }
            }
            return false
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        Log.d(TAG, "NotificationLightListenerService connected.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        Log.d(TAG, "NotificationLightListenerService disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val pkg = sbn.packageName
        if (pkg == packageName) return // Ignore Strike RGB own notifications

        val repo = LightFlowRepository.getInstance(applicationContext)
        if (!repo.isMasterEnabled()) {
            Log.d(TAG, "Light Flow master disabled, ignoring notification.")
            return
        }

        // Evaluate automation conditions
        val automation = repo.getAutomationConfig()
        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isScreenOn = powerManager?.isInteractive ?: true

        if (isScreenOn && !automation.screenOnEnabled) {
            Log.d(TAG, "Notification ignored: Screen is ON and screenOnEnabled is false.")
            return
        }
        if (!isScreenOn && !automation.screenOffEnabled) {
            Log.d(TAG, "Notification ignored: Screen is OFF and screenOffEnabled is false.")
            return
        }

        if (automation.chargingOnly) {
            val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            if (!isCharging) {
                Log.d(TAG, "Notification ignored: chargingOnly is enabled and device is not charging.")
                return
            }
        }

        if (automation.respectDnd) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val filter = notificationManager?.currentInterruptionFilter ?: NotificationManager.INTERRUPTION_FILTER_ALL
            if (filter == NotificationManager.INTERRUPTION_FILTER_NONE ||
                filter == NotificationManager.INTERRUPTION_FILTER_ALARMS) {
                Log.d(TAG, "Notification ignored: DND filter is active ($filter).")
                return
            }
        }

        val extras = sbn.notification.extras
        val title = extras?.getString("android.title") ?: "Notification"
        val text = extras?.getCharSequence("android.text")?.toString() ?: ""

        val pm = packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            pkg
        }

        // Get or build profile
        val profile = repo.getProfile(pkg) ?: run {
            val generated = AppColorExtractor.generateDefaultProfile(applicationContext, pkg, appName)
            repo.saveProfile(generated)
            generated
        }

        if (!profile.isEnabled) {
            Log.d(TAG, "Profile for $appName is disabled.")
            return
        }

        // Check if notification accent color can augment detected colors
        val notifColor = sbn.notification.color
        val effectiveProfile = if (notifColor != 0 && profile.colorMode == LightFlowColorMode.AUTO_APP_COLOR) {
            val r = android.graphics.Color.red(notifColor)
            val g = android.graphics.Color.green(notifColor)
            val b = android.graphics.Color.blue(notifColor)
            val accentRgb = RgbColor(r, g, b)
            if (profile.colors.none { it.r == r && it.g == g && it.b == b }) {
                profile.copy(colors = listOf(accentRgb) + profile.colors)
            } else profile
        } else {
            profile
        }

        // Add to history
        repo.addHistoryItem(
            NotificationHistoryItem(
                id = "${sbn.id}_${sbn.postTime}",
                packageName = pkg,
                appName = appName,
                title = title,
                message = text,
                detectedColor = effectiveProfile.primaryColor,
                effect = effectiveProfile.effect,
                timestamp = System.currentTimeMillis()
            )
        )

        // Dispatch through unified NotificationFlowEngine
        NotificationFlowEngine.getInstance(applicationContext).triggerProfileNotification(
            profile = effectiveProfile,
            title = title,
            message = text
        )
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        NotificationFlowEngine.getInstance(applicationContext).stopNotification()
    }
}
