package com.example.effects

import android.content.Context
import android.graphics.Color
import android.util.Log
import com.example.effects.screensync.ColorSmoother
import com.example.effects.screensync.ScreenColorSampler
import com.example.hardware.HardwareController
import com.example.hardware.LedHardwareController
import com.example.model.EffectEngineDiagnostic
import com.example.model.EffectType
import com.example.model.LedState
import com.example.model.RgbColor
import com.example.model.ScreenSyncConfig
import com.example.model.ScreenSyncDebugInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Unified Effect Engine for Strike RGB CrossDroid.
 *
 * Guaranteed Single Hardware Output Pipeline:
 * [Effect Algorithms] -> [Frame Generation] -> [HardwareController] -> [Root / Sysfs] -> [Physical LED]
 *
 * Features:
 * - Runs continuously on Dispatchers.Default at ~25-30 FPS.
 * - Single active loop guaranteed (strict mutual cancellation).
 * - Comprehensive hardware write logging (Frame, Effect, RGB).
 * - Live diagnostic StateFlow for UI & Test Mode.
 * - Direct Hardware Effect Test execution.
 */
class EffectEngine(
    private val context: Context,
    private val ledHardwareController: LedHardwareController,
    private val onDebugUpdate: ((ScreenSyncDebugInfo) -> Unit)? = null
) {
    companion object {
        private const val TAG = "EffectEngine"
        private const val FRAME_DELAY_MS = 40L // ~25 FPS

        @Volatile
        private var instance: EffectEngine? = null

        fun getInstance(context: Context): EffectEngine {
            return instance ?: synchronized(this) {
                instance ?: EffectEngine(
                    context = context.applicationContext,
                    ledHardwareController = HardwareController.getInstance().controller
                ).also { instance = it }
            }
        }
    }

    private val engineScope = CoroutineScope(Dispatchers.Default)
    private var effectJob: Job? = null

    private val colorSmoother = ColorSmoother()
    private var screenColorSampler: ScreenColorSampler? = null

    @Volatile
    private var isRunning = false

    @Volatile
    private var isPaused = false

    private var currentEffect = EffectType.STATIC
    private var currentColor = RgbColor.Blue
    private var currentBrightness = 0.85f
    private var currentSpeed = 1.0f
    private var currentIntensity = 1.0f
    private var currentSyncConfig = ScreenSyncConfig()

    private var frameCounter = 0L
    private var lastFpsTimestamp = 0L
    private var framesInWindow = 0
    private var calculatedFps = 0

    private val _diagnosticFlow = MutableStateFlow(EffectEngineDiagnostic())
    val diagnosticFlow: StateFlow<EffectEngineDiagnostic> = _diagnosticFlow.asStateFlow()

    init {
        screenColorSampler = ScreenColorSampler(
            context = context,
            onColorSampled = { rawColor, fps ->
                handleScreenColorSample(rawColor, fps)
            },
            onError = { errMsg ->
                Log.w(TAG, "ScreenColorSampler error: $errMsg")
            }
        )
    }

    @Synchronized
    fun start(state: LedState) {
        currentEffect = state.currentEffect
        currentColor = state.currentColor
        currentBrightness = state.brightness
        currentSpeed = state.effectSpeed
        currentIntensity = state.intensity
        currentSyncConfig = state.screenSyncConfig
        isRunning = state.isRunning
        isPaused = false

        if (isRunning) {
            runActiveEffect()
        } else {
            stop(turnOffHardware = true)
        }
    }

    @Synchronized
    fun updateState(state: LedState) {
        currentEffect = state.currentEffect
        currentColor = state.currentColor
        currentBrightness = state.brightness
        currentSpeed = state.effectSpeed
        currentIntensity = state.intensity
        currentSyncConfig = state.screenSyncConfig
        isRunning = state.isRunning

        if (isRunning && !isPaused) {
            runActiveEffect()
        } else {
            stop(turnOffHardware = true)
        }
    }

    @Synchronized
    fun stop(turnOffHardware: Boolean = true) {
        isRunning = false
        isPaused = false
        effectJob?.cancel()
        effectJob = null
        screenColorSampler?.stop()

        if (turnOffHardware) {
            engineScope.launch {
                ledHardwareController.turnOff()
            }
        }

        updateDiagnostic(
            effect = currentEffect,
            running = false,
            fps = 0,
            rgb = RgbColor(0, 0, 0)
        )

        onDebugUpdate?.invoke(
            ScreenSyncDebugInfo(
                isRunning = false,
                permissionGranted = false,
                detectedColor = RgbColor(0, 0, 0),
                smoothedLedColor = RgbColor(0, 0, 0),
                currentFps = 0,
                isRootAvailable = ledHardwareController.isRootAvailable(),
                isLedDetected = ledHardwareController.isHardwareDetected()
            )
        )
    }

    @Synchronized
    fun pause(keepLedOn: Boolean = false) {
        isPaused = true
        if (currentEffect == EffectType.SCREEN_SYNC || currentEffect == EffectType.AMBILIGHT) {
            screenColorSampler?.pause()
        }
        if (!keepLedOn) {
            engineScope.launch {
                ledHardwareController.turnOff()
            }
        }
        updateDiagnostic(
            effect = currentEffect,
            running = false,
            fps = 0,
            rgb = if (keepLedOn) currentColor else RgbColor(0, 0, 0)
        )
    }

    @Synchronized
    fun resume() {
        if (!isRunning) return
        isPaused = false
        if (currentEffect == EffectType.SCREEN_SYNC || currentEffect == EffectType.AMBILIGHT) {
            screenColorSampler?.resume()
        } else {
            runActiveEffect()
        }
    }

    @Synchronized
    fun setEffect(effect: EffectType) {
        if (currentEffect == effect && isRunning && effectJob?.isActive == true) return
        currentEffect = effect
        if (isRunning && !isPaused) {
            runActiveEffect()
        }
    }

    @Synchronized
    fun setColor(color: RgbColor) {
        currentColor = color
        if (isRunning && !isPaused && currentEffect == EffectType.STATIC) {
            runActiveEffect()
        }
    }

    @Synchronized
    fun setBrightness(brightness: Float) {
        currentBrightness = brightness.coerceIn(0.05f, 1.0f)
        currentSyncConfig = currentSyncConfig.copy(brightness = currentBrightness)
        screenColorSampler?.updateConfig(currentSyncConfig)
        if (isRunning && !isPaused && currentEffect == EffectType.STATIC) {
            runActiveEffect()
        }
    }

    @Synchronized
    fun setSpeed(speed: Float) {
        currentSpeed = speed.coerceIn(0.2f, 3.0f)
    }

    @Synchronized
    fun setIntensity(intensity: Float) {
        currentIntensity = intensity.coerceIn(0.0f, 1.0f)
    }

    @Synchronized
    fun updateScreenSyncConfig(config: ScreenSyncConfig) {
        currentSyncConfig = config
        screenColorSampler?.updateConfig(config)
    }

    /**
     * Direct test trigger for developer and test modes.
     * Bypasses UI state and exercises the EffectEngine -> HardwareController pipeline.
     */
    @Synchronized
    fun testEffect(effect: EffectType) {
        currentEffect = effect
        isRunning = true
        isPaused = false
        if (currentColor.r == 0 && currentColor.g == 0 && currentColor.b == 0) {
            currentColor = RgbColor.Blue
        }
        runActiveEffect()
    }

    /**
     * Re-launches the active effect frame loop, ensuring strict mutual exclusivity.
     */
    private fun runActiveEffect() {
        effectJob?.cancel()
        effectJob = null
        screenColorSampler?.stop()

        if (!isRunning || isPaused) return

        resetFpsCounter()

        when (currentEffect) {
            EffectType.STATIC -> {
                startStaticEffect()
            }
            EffectType.BREATHING -> {
                startBreathingLoop()
            }
            EffectType.HEARTBEAT -> {
                startHeartbeatLoop()
            }
            EffectType.PULSE -> {
                startPulseLoop()
            }
            EffectType.WAVE -> {
                startWaveLoop()
            }
            EffectType.RAINBOW -> {
                startRainbowLoop()
            }
            EffectType.STROBE -> {
                startStrobeLoop()
            }
            EffectType.RAINBOW_CYCLE,
            EffectType.FLASH,
            EffectType.DOUBLE_FLASH,
            EffectType.TRIPLE_FLASH,
            EffectType.FADE,
            EffectType.COLOR_CYCLE,
            EffectType.SMOOTH_GRADIENT,
            EffectType.AURORA,
            EffectType.SPARKLE -> {
                startFluidEffectLoop()
            }
            EffectType.SCREEN_SYNC, EffectType.AMBILIGHT -> {
                startScreenSync()
            }
        }
    }

    private fun startFluidEffectLoop() {
        effectJob = engineScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive && isRunning && !isPaused) {
                val elapsed = System.currentTimeMillis() - startTime
                val (r, g, b) = com.example.notification.LightFlowFluidInterpolator.computeFrame(
                    effect = currentEffect,
                    colors = listOf(currentColor),
                    elapsedMs = elapsed,
                    speedFactor = currentSpeed,
                    brightness = currentBrightness,
                    intensity = currentIntensity
                )
                writeFrame(r, g, b)
                delay(FRAME_DELAY_MS)
            }
        }
    }

    // ================= EFFECT FRAME GENERATORS =================

    private fun startStaticEffect() {
        effectJob = engineScope.launch {
            val r = (currentColor.r * currentBrightness).roundToInt().coerceIn(0, 255)
            val g = (currentColor.g * currentBrightness).roundToInt().coerceIn(0, 255)
            val b = (currentColor.b * currentBrightness).roundToInt().coerceIn(0, 255)

            writeFrame(r, g, b)
        }
    }

    private fun startBreathingLoop() {
        effectJob = engineScope.launch {
            var angle = 0.0
            while (isActive && isRunning && !isPaused) {
                val speedFactor = currentSpeed.coerceIn(0.2f, 3.0f)
                // Sine wave from 0.0 to 1.0
                val sineWave = ((sin(angle) + 1.0) / 2.0).toFloat()

                // Modulate by intensity (depth of breathing) and brightness
                val minDepth = 1.0f - currentIntensity.coerceIn(0f, 1f)
                val effectiveFactor = (minDepth + (1f - minDepth) * sineWave) * currentBrightness

                val r = (currentColor.r * effectiveFactor).roundToInt().coerceIn(0, 255)
                val g = (currentColor.g * effectiveFactor).roundToInt().coerceIn(0, 255)
                val b = (currentColor.b * effectiveFactor).roundToInt().coerceIn(0, 255)

                writeFrame(r, g, b)

                angle += 0.08 * speedFactor
                if (angle > 2 * PI) angle -= 2 * PI

                delay(FRAME_DELAY_MS)
            }
        }
    }

    private fun startHeartbeatLoop() {
        effectJob = engineScope.launch {
            val startTime = System.currentTimeMillis()
            while (isActive && isRunning && !isPaused) {
                val speedFactor = currentSpeed.coerceIn(0.3f, 3.0f)
                val cycleDurationMs = (1200 / speedFactor).toLong().coerceAtLeast(300L)
                val elapsed = (System.currentTimeMillis() - startTime) % cycleDurationMs
                val t = elapsed.toFloat() / cycleDurationMs.toFloat()

                val amplitude = when {
                    // First beat (Lub)
                    t < 0.14f -> {
                        val p = t / 0.14f
                        sin(p * PI.toFloat())
                    }
                    // Brief pause between lub and dub
                    t < 0.22f -> 0.05f
                    // Second beat (Dub)
                    t < 0.38f -> {
                        val p = (t - 0.22f) / 0.16f
                        0.85f * sin(p * PI.toFloat())
                    }
                    // Diastolic rest period
                    else -> 0.02f
                }

                val factor = (0.02f + amplitude * currentIntensity.coerceIn(0.1f, 1f) * 0.98f) * currentBrightness

                val r = (currentColor.r * factor).roundToInt().coerceIn(0, 255)
                val g = (currentColor.g * factor).roundToInt().coerceIn(0, 255)
                val b = (currentColor.b * factor).roundToInt().coerceIn(0, 255)

                writeFrame(r, g, b)
                delay(FRAME_DELAY_MS)
            }
        }
    }

    private fun startPulseLoop() {
        effectJob = engineScope.launch {
            var angle = 0.0
            while (isActive && isRunning && !isPaused) {
                val speedFactor = currentSpeed.coerceIn(0.3f, 3.0f)
                // Smooth pulse with sharp attack and soft decay
                val wave = ((sin(angle) + 1.0) / 2.0).toFloat()
                val pulseFactor = (wave * wave)

                val factor = (0.03f + pulseFactor * currentIntensity.coerceIn(0.1f, 1f) * 0.97f) * currentBrightness

                val r = (currentColor.r * factor).roundToInt().coerceIn(0, 255)
                val g = (currentColor.g * factor).roundToInt().coerceIn(0, 255)
                val b = (currentColor.b * factor).roundToInt().coerceIn(0, 255)

                writeFrame(r, g, b)

                angle += 0.09 * speedFactor
                if (angle > 2 * PI) angle -= 2 * PI
                delay(FRAME_DELAY_MS)
            }
        }
    }

    private fun startWaveLoop() {
        effectJob = engineScope.launch {
            var angle = 0.0
            while (isActive && isRunning && !isPaused) {
                val speedFactor = currentSpeed.coerceIn(0.2f, 3.0f)

                val rWave = ((sin(angle) + 1.0) / 2.0).toFloat()
                val gWave = ((sin(angle + (2 * PI / 3)) + 1.0) / 2.0).toFloat()
                val bWave = ((sin(angle + (4 * PI / 3)) + 1.0) / 2.0).toFloat()

                val minD = 1.0f - currentIntensity.coerceIn(0.1f, 1f)
                val rFactor = (minD + (1f - minD) * rWave) * currentBrightness
                val gFactor = (minD + (1f - minD) * gWave) * currentBrightness
                val bFactor = (minD + (1f - minD) * bWave) * currentBrightness

                val r = (currentColor.r * rFactor).roundToInt().coerceIn(0, 255)
                val g = (currentColor.g * gFactor).roundToInt().coerceIn(0, 255)
                val b = (currentColor.b * bFactor).roundToInt().coerceIn(0, 255)

                writeFrame(r, g, b)

                angle += 0.07 * speedFactor
                if (angle > 2 * PI) angle -= 2 * PI
                delay(FRAME_DELAY_MS)
            }
        }
    }

    private fun startRainbowLoop() {
        effectJob = engineScope.launch {
            var hue = 0f
            val hsv = FloatArray(3)
            var lastTime = System.currentTimeMillis()
            while (isActive && isRunning && !isPaused) {
                val now = System.currentTimeMillis()
                val deltaSec = ((now - lastTime).coerceIn(1L, 100L)) / 1000f
                lastTime = now

                val speedFactor = currentSpeed.coerceIn(0.2f, 3.0f)
                // Continuous HSV hue rotation (RED -> ORANGE -> YELLOW -> GREEN -> CYAN -> BLUE -> PURPLE -> MAGENTA -> RED)
                hue = (hue + 50f * speedFactor * deltaSec) % 360f

                hsv[0] = hue
                hsv[1] = currentIntensity.coerceIn(0.3f, 1f)
                hsv[2] = currentBrightness.coerceIn(0.05f, 1f)

                val colorInt = Color.HSVToColor(hsv)
                val r = Color.red(colorInt)
                val g = Color.green(colorInt)
                val b = Color.blue(colorInt)

                writeFrame(r, g, b)
                delay(FRAME_DELAY_MS)
            }
        }
    }

    private fun startStrobeLoop() {
        effectJob = engineScope.launch {
            var isOn = false
            while (isActive && isRunning && !isPaused) {
                val speedFactor = currentSpeed.coerceIn(0.3f, 3.0f)
                val halfPeriod = (100 / speedFactor).toLong().coerceIn(25L, 300L)

                isOn = !isOn
                if (isOn) {
                    val r = (currentColor.r * currentBrightness).roundToInt().coerceIn(0, 255)
                    val g = (currentColor.g * currentBrightness).roundToInt().coerceIn(0, 255)
                    val b = (currentColor.b * currentBrightness).roundToInt().coerceIn(0, 255)
                    writeFrame(r, g, b)
                } else {
                    writeFrame(0, 0, 0)
                }

                delay(halfPeriod)
            }
        }
    }

    private fun startScreenSync() {
        colorSmoother.reset(RgbColor(0, 0, 0))
        val started = screenColorSampler?.start(currentSyncConfig) ?: false
        if (!started) {
            Log.w(TAG, "ScreenSync could not start (missing permission)")
            // Fallback to static color
            startStaticEffect()
        }
    }

    private fun handleScreenColorSample(rawColor: RgbColor, fps: Int) {
        if (!isRunning || isPaused || (currentEffect != EffectType.SCREEN_SYNC && currentEffect != EffectType.AMBILIGHT)) return

        // Step 1: Smooth transition
        val smoothed = colorSmoother.step(rawColor, currentSyncConfig.smoothing)

        // Step 2: Apply Intensity and Brightness scaling
        val scale = (currentSyncConfig.intensity.coerceIn(0f, 1f)) *
                (currentSyncConfig.brightness.coerceIn(0f, 1f))

        val finalR = (smoothed.r * scale).roundToInt().coerceIn(0, 255)
        val finalG = (smoothed.g * scale).roundToInt().coerceIn(0, 255)
        val finalB = (smoothed.b * scale).roundToInt().coerceIn(0, 255)

        engineScope.launch {
            writeFrame(finalR, finalG, finalB)
        }

        onDebugUpdate?.invoke(
            ScreenSyncDebugInfo(
                isRunning = true,
                permissionGranted = true,
                detectedColor = rawColor,
                smoothedLedColor = RgbColor(finalR, finalG, finalB),
                currentFps = fps,
                isRootAvailable = ledHardwareController.isRootAvailable(),
                isLedDetected = ledHardwareController.isHardwareDetected()
            )
        )
    }

    /**
     * Primary hardware output pipeline.
     * Executes the hardware write, records debug logs, and tracks real-time FPS.
     */
    private suspend fun writeFrame(r: Int, g: Int, b: Int) {
        frameCounter++
        trackFps()

        Log.i(TAG, "EffectEngine: ${currentEffect.name} | Frame: $frameCounter | RGB: $r,$g,$b")

        val success = ledHardwareController.setRGB(r, g, b)
        if (!success) {
            Log.w(TAG, "Hardware write skipped or unacknowledged for effect ${currentEffect.name} at RGB: ($r, $g, $b)")
        }

        updateDiagnostic(
            effect = currentEffect,
            running = isRunning && !isPaused,
            fps = calculatedFps,
            rgb = RgbColor(r, g, b),
            lastError = if (success) null else "Write unacknowledged for RGB($r,$g,$b)"
        )
    }

    private fun trackFps() {
        val now = System.currentTimeMillis()
        framesInWindow++
        if (now - lastFpsTimestamp >= 1000L) {
            calculatedFps = framesInWindow
            framesInWindow = 0
            lastFpsTimestamp = now
        }
    }

    private fun resetFpsCounter() {
        framesInWindow = 0
        lastFpsTimestamp = System.currentTimeMillis()
        calculatedFps = 0
    }

    private fun updateDiagnostic(
        effect: EffectType,
        running: Boolean,
        fps: Int,
        rgb: RgbColor,
        lastError: String? = null
    ) {
        _diagnosticFlow.value = EffectEngineDiagnostic(
            effect = effect,
            isRunning = running,
            currentFps = fps,
            currentRgb = rgb,
            frameCount = frameCounter,
            isRootAvailable = ledHardwareController.isRootAvailable(),
            isLedDetected = ledHardwareController.isHardwareDetected(),
            lastError = lastError
        )
    }
}
