package com.example.notification

import android.content.Context
import android.util.Log
import com.example.effects.EffectEngine
import com.example.hardware.CameraFlashController
import com.example.hardware.HardwareController
import com.example.model.ActiveNotificationEvent
import com.example.model.AodNotificationStyle
import com.example.model.EffectType
import com.example.model.FlashPattern
import com.example.model.LightFlowFlashMode
import com.example.model.LightFlowProfile
import com.example.model.NotificationPriority
import com.example.model.RgbColor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Screen Flash Overlay UI State
 */
data class ScreenFlashState(
    val isVisible: Boolean = false,
    val color: RgbColor = RgbColor.White,
    val alpha: Float = 0.95f
)

/**
 * AOD Display UI State for notification popups and ambient glowing overlays
 */
data class AodDisplayState(
    val isVisible: Boolean = false,
    val appName: String = "",
    val title: String = "",
    val message: String = "",
    val color: RgbColor = RgbColor.Blue,
    val colors: List<RgbColor> = emptyList(),
    val style: AodNotificationStyle = AodNotificationStyle.RGB_GLOW,
    val edgeGlowEnabled: Boolean = true
)

/**
 * Unified Notification Flow Engine for Strike RGB CrossDroid.
 *
 * Coordinates Physical RGB LED (Root/Sysfs), Camera Flash, Front Screen Flash, and AOD.
 * Drives real LED hardware using fluid HSV/HSL transitions without harsh jumps.
 */
class NotificationFlowEngine private constructor(private val context: Context) {

    companion object {
        private const val TAG = "NotificationFlowEngine"
        private const val FRAME_DELAY_MS = 38L // ~26 FPS for smooth fluid interpolation

        @Volatile
        private var instance: NotificationFlowEngine? = null

        fun getInstance(context: Context): NotificationFlowEngine {
            return instance ?: synchronized(this) {
                instance ?: NotificationFlowEngine(context.applicationContext).also { instance = it }
            }
        }
    }

    private val engineScope = CoroutineScope(Dispatchers.Default)
    private var activeJob: Job? = null
    private val isExecuting = AtomicBoolean(false)

    private val hardwareController = HardwareController.getInstance()
    private val cameraFlashController = CameraFlashController(context)

    private val _screenFlashState = MutableStateFlow(ScreenFlashState())
    val screenFlashState: StateFlow<ScreenFlashState> = _screenFlashState.asStateFlow()

    private val _aodDisplayState = MutableStateFlow(AodDisplayState())
    val aodDisplayState: StateFlow<AodDisplayState> = _aodDisplayState.asStateFlow()

    private val _activeNotification = MutableStateFlow<ActiveNotificationEvent?>(null)
    val activeNotification: StateFlow<ActiveNotificationEvent?> = _activeNotification.asStateFlow()

    // Real-time RGB LED output state for live UI visualizers
    private val _activeLedOutput = MutableStateFlow(RgbColor(0, 0, 0))
    val activeLedOutput: StateFlow<RgbColor> = _activeLedOutput.asStateFlow()

    private var currentActivePriority = NotificationPriority.NORMAL

    /**
     * Executes a LightFlowProfile on the physical LED and synchronized outputs.
     */
    @Synchronized
    fun triggerProfileNotification(
        profile: LightFlowProfile,
        title: String = "${profile.appName} Notification",
        message: String = "Notification light active"
    ) {
        // Priority arbitration
        if (isExecuting.get() && profile.priority.level < currentActivePriority.level) {
            Log.d(TAG, "Notification ${profile.appName} discarded: lower priority (${profile.priority}) than active ($currentActivePriority)")
            return
        }

        stopNotification()

        currentActivePriority = profile.priority
        isExecuting.set(true)

        val colors = if (profile.colors.isNotEmpty()) profile.colors else listOf(profile.primaryColor)
        val primaryColor = profile.primaryColor

        val event = ActiveNotificationEvent(
            id = "${profile.packageName}_${System.currentTimeMillis()}",
            packageName = profile.packageName,
            appName = profile.appName,
            title = title,
            message = message,
            resolvedColor = primaryColor,
            effectType = profile.effect,
            isRearFlashActive = profile.flashMode != LightFlowFlashMode.OFF,
            isFrontFlashActive = false,
            isScreenFlashActive = profile.flashMode == LightFlowFlashMode.SYNC_RGB,
            isAodActive = profile.aodGlowEnabled,
            pattern = FlashPattern.SINGLE,
            speed = com.example.model.FlashSpeed.NORMAL,
            durationSeconds = profile.durationSeconds,
            aodStyle = profile.aodStyle
        )
        _activeNotification.value = event

        // Pause normal background effect engine while notification light takes over physical hardware
        try {
            EffectEngine.getInstance(context).pause(keepLedOn = false)
        } catch (_: Exception) {}

        activeJob = engineScope.launch {
            try {
                if (profile.aodGlowEnabled) {
                    _aodDisplayState.value = AodDisplayState(
                        isVisible = true,
                        appName = profile.appName,
                        title = title,
                        message = message,
                        color = primaryColor,
                        colors = colors,
                        style = profile.aodStyle,
                        edgeGlowEnabled = profile.aodEdgeGlowEnabled
                    )
                }

                val startTime = System.currentTimeMillis()
                val totalDurationMs = if (profile.durationSeconds <= 0) 60_000L else profile.durationSeconds * 1000L
                var flashState = false
                var lastFlashToggle = System.currentTimeMillis()
                val flashHalfPeriodMs = (180 / profile.speed).toLong().coerceIn(60L, 400L)

                var elapsedMs = 0L

                while (isActive && isExecuting.get()) {
                    val now = System.currentTimeMillis()
                    elapsedMs = now - startTime

                    if (elapsedMs >= totalDurationMs) {
                        Log.d(TAG, "Notification profile finished duration: ${profile.durationSeconds}s")
                        break
                    }

                    // 1. Calculate physical RGB LED frame with fluid HSV/HSL transitions
                    val (r, g, b) = LightFlowFluidInterpolator.computeFrame(
                        effect = profile.effect,
                        colors = colors,
                        elapsedMs = elapsedMs,
                        speedFactor = profile.speed,
                        brightness = profile.brightness
                    )

                    // Write directly to physical hardware controller (Root/Sysfs)
                    hardwareController.controller.setRGB(r, g, b)
                    _activeLedOutput.value = RgbColor(r, g, b)

                    // 2. Camera Flash Sync
                    if (profile.flashMode != LightFlowFlashMode.OFF) {
                        if (now - lastFlashToggle >= flashHalfPeriodMs) {
                            flashState = !flashState
                            lastFlashToggle = now
                            when (profile.flashMode) {
                                LightFlowFlashMode.OFF -> {}
                                LightFlowFlashMode.SYNC_RGB -> {
                                    val shouldFlash = (r + g + b) > 100
                                    cameraFlashController.setRearTorch(shouldFlash)
                                }
                                else -> {
                                    cameraFlashController.setRearTorch(flashState)
                                }
                            }
                        }
                    }

                    // 3. Screen Flash Sync
                    if (profile.flashMode == LightFlowFlashMode.SYNC_RGB) {
                        _screenFlashState.value = ScreenFlashState(
                            isVisible = (r + g + b) > 80,
                            color = RgbColor(r, g, b),
                            alpha = if ((r + g + b) > 80) 0.85f else 0.0f
                        )
                    }

                    delay(FRAME_DELAY_MS)
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "Profile notification cancelled.")
            } catch (e: Exception) {
                Log.e(TAG, "Error in notification loop: ${e.message}", e)
            } finally {
                withContext(NonCancellable) {
                    failsafeShutdown()
                    // Resume regular effect engine if it was active
                    try {
                        EffectEngine.getInstance(context).resume()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    /**
     * Backward-compatible trigger for ActiveNotificationEvent
     */
    @Synchronized
    fun triggerNotification(event: ActiveNotificationEvent) {
        stopNotification()

        _activeNotification.value = event
        isExecuting.set(true)
        currentActivePriority = NotificationPriority.NORMAL

        try {
            EffectEngine.getInstance(context).pause(keepLedOn = false)
        } catch (_: Exception) {}

        activeJob = engineScope.launch {
            try {
                if (event.isAodActive) {
                    _aodDisplayState.value = AodDisplayState(
                        isVisible = true,
                        appName = event.appName,
                        title = event.title,
                        message = event.message,
                        color = event.resolvedColor,
                        style = event.aodStyle
                    )
                }

                val startTime = System.currentTimeMillis()
                val durationMs = event.durationSeconds * 1000L
                val pulseMs = event.speed.pulseDurationMs

                while (isActive && isExecuting.get()) {
                    if (System.currentTimeMillis() - startTime >= durationMs) {
                        break
                    }

                    when (event.pattern) {
                        FlashPattern.SINGLE -> {
                            executeStep(true, event, pulseMs)
                            executeStep(false, event, pulseMs * 2)
                        }
                        FlashPattern.DOUBLE -> {
                            executeStep(true, event, pulseMs)
                            executeStep(false, event, pulseMs)
                            executeStep(true, event, pulseMs)
                            executeStep(false, event, pulseMs * 3)
                        }
                        FlashPattern.TRIPLE -> {
                            executeStep(true, event, pulseMs)
                            executeStep(false, event, pulseMs)
                            executeStep(true, event, pulseMs)
                            executeStep(false, event, pulseMs)
                            executeStep(true, event, pulseMs)
                            executeStep(false, event, pulseMs * 4)
                        }
                        FlashPattern.HEARTBEAT -> {
                            executeStep(true, event, (pulseMs * 0.7f).toLong())
                            executeStep(false, event, (pulseMs * 0.7f).toLong())
                            executeStep(true, event, pulseMs)
                            executeStep(false, event, pulseMs * 4)
                        }
                        FlashPattern.STROBE -> {
                            val fastPulse = (pulseMs * 0.5f).toLong().coerceAtLeast(35L)
                            executeStep(true, event, fastPulse)
                            executeStep(false, event, fastPulse)
                        }
                        FlashPattern.SOS -> {
                            val dot = pulseMs / 2
                            val dash = pulseMs * 3 / 2
                            repeat(3) { executeStep(true, event, dot); executeStep(false, event, dot) }
                            delay(dot)
                            repeat(3) { executeStep(true, event, dash); executeStep(false, event, dot) }
                            delay(dot)
                            repeat(3) { executeStep(true, event, dot); executeStep(false, event, dot) }
                            delay(pulseMs * 3)
                        }
                    }
                }
            } catch (e: CancellationException) {
                Log.d(TAG, "Notification loop cancelled.")
            } catch (e: Exception) {
                Log.e(TAG, "Error in notification loop: ${e.message}", e)
            } finally {
                withContext(NonCancellable) {
                    failsafeShutdown()
                    try {
                        EffectEngine.getInstance(context).resume()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private suspend fun executeStep(on: Boolean, event: ActiveNotificationEvent, durationMs: Long) {
        if (on) {
            hardwareController.controller.setRGB(event.resolvedColor.r, event.resolvedColor.g, event.resolvedColor.b)
            _activeLedOutput.value = event.resolvedColor
        } else {
            hardwareController.controller.turnOff()
            _activeLedOutput.value = RgbColor(0, 0, 0)
        }

        if (event.isRearFlashActive) {
            cameraFlashController.setRearTorch(on)
        }
        if (event.isFrontFlashActive) {
            cameraFlashController.setFrontTorch(on)
        }
        if (event.isScreenFlashActive) {
            _screenFlashState.value = ScreenFlashState(
                isVisible = on,
                color = event.resolvedColor,
                alpha = if (on) 0.95f else 0.0f
            )
        }
        delay(durationMs)
    }

    @Synchronized
    fun stopNotification() {
        isExecuting.set(false)
        val jobToCancel = activeJob
        activeJob = null
        if (jobToCancel != null) {
            jobToCancel.cancel()
        } else {
            engineScope.launch(NonCancellable) {
                failsafeShutdown()
            }
        }
    }

    private suspend fun failsafeShutdown() {
        withContext(NonCancellable) {
            try {
                cameraFlashController.turnOffAll()
            } catch (_: Exception) {}

            try {
                hardwareController.controller.turnOff()
                _activeLedOutput.value = RgbColor(0, 0, 0)
            } catch (_: Exception) {}

            _screenFlashState.value = ScreenFlashState(isVisible = false)
            _aodDisplayState.value = AodDisplayState(isVisible = false)
            _activeNotification.value = null
            isExecuting.set(false)
            currentActivePriority = NotificationPriority.NORMAL
        }
    }

    fun dismissAod() {
        _aodDisplayState.value = AodDisplayState(isVisible = false)
    }
}
