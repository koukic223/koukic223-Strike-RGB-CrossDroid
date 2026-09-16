package com.example.effects.screensync

import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import com.example.model.RgbColor
import com.example.model.ScreenSyncConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Captures the screen at low resolution using MediaProjection,
 * analyzes dominant colors, and computes FPS.
 * Fully optimized for MT6735M (ARM32 / Android Go).
 */
class ScreenColorSampler(
    private val context: Context,
    private val onColorSampled: (rawColor: RgbColor, fps: Int) -> Unit,
    private val onError: (String) -> Unit
) {
    companion object {
        private const val TAG = "ScreenColorSampler"
        // 24x40 is ~960 pixels total, optimal for 9:15 / 9:16 aspect ratio screen capture
        const val SAMPLE_WIDTH = 24
        const val SAMPLE_HEIGHT = 40
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var samplingJob: Job? = null
    private val samplerScope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var isSampling = false

    @Volatile
    private var isPaused = false

    private var currentConfig = ScreenSyncConfig()

    // FPS calculation tracking
    private var frameCount = 0
    private var lastFpsTimestamp = 0L
    private var currentCalculatedFps = 0

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            Log.d(TAG, "MediaProjection session stopped by system")
            stop()
        }
    }

    /**
     * Updates configuration in real-time without restarting the projection session.
     */
    fun updateConfig(config: ScreenSyncConfig) {
        currentConfig = config
    }

    /**
     * Starts screen sampling using the token stored in MediaProjectionHolder.
     */
    fun start(config: ScreenSyncConfig): Boolean {
        currentConfig = config
        stop()

        val projection = MediaProjectionHolder.getMediaProjection(context)
        if (projection == null) {
            onError("MediaProjection permission not available")
            return false
        }
        mediaProjection = projection

        try {
            projection.registerCallback(projectionCallback, null)

            // Setup background handler for ImageReader
            val thread = HandlerThread("ScreenCaptureThread").apply { start() }
            handlerThread = thread
            val handler = Handler(thread.looper)
            backgroundHandler = handler

            // Create low-resolution ImageReader (maxImages = 2 prevents buffer starvation)
            val reader = ImageReader.newInstance(
                SAMPLE_WIDTH,
                SAMPLE_HEIGHT,
                PixelFormat.RGBA_8888,
                2
            )
            imageReader = reader

            // Create low-overhead VirtualDisplay
            val displayMetrics = context.resources.displayMetrics
            val dpi = displayMetrics.densityDpi.coerceIn(120, 240)

            virtualDisplay = projection.createVirtualDisplay(
                "AmbilightDisplay",
                SAMPLE_WIDTH,
                SAMPLE_HEIGHT,
                dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                reader.surface,
                null,
                handler
            )

            isSampling = true
            isPaused = false
            startSamplingLoop()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize VirtualDisplay", e)
            onError("Initialization error: ${e.message}")
            stop()
            return false
        }
    }

    private fun startSamplingLoop() {
        samplingJob?.cancel()
        lastFpsTimestamp = System.currentTimeMillis()
        frameCount = 0

        samplingJob = samplerScope.launch {
            while (isActive && isSampling) {
                if (!isPaused) {
                    val reader = imageReader
                    if (reader != null) {
                        try {
                            val image = reader.acquireLatestImage()
                            if (image != null) {
                                try {
                                    val planes = image.planes
                                    if (planes.isNotEmpty()) {
                                        val plane = planes[0]
                                        val buffer = plane.buffer
                                        val color = ColorAnalyzer.analyze(
                                            buffer = buffer,
                                            width = SAMPLE_WIDTH,
                                            height = SAMPLE_HEIGHT,
                                            pixelStride = plane.pixelStride,
                                            rowStride = plane.rowStride,
                                            region = currentConfig.region,
                                            colorBoost = currentConfig.colorBoost
                                        )

                                        // Update FPS stats
                                        frameCount++
                                        val now = System.currentTimeMillis()
                                        val elapsed = now - lastFpsTimestamp
                                        if (elapsed >= 1000L) {
                                            currentCalculatedFps = (frameCount * 1000L / elapsed).toInt()
                                            frameCount = 0
                                            lastFpsTimestamp = now
                                        }

                                        onColorSampled(color, currentCalculatedFps)
                                    }
                                } finally {
                                    image.close()
                                }
                            }
                        } catch (e: Exception) {
                            // Ignored: transient buffer read errors during display resize
                        }
                    }
                }

                // Throttle to requested sampling rate (10 .. 20 Hz, default 15 Hz ~ 66ms)
                val targetFps = currentConfig.samplingRate.coerceIn(10, 20)
                val delayMs = (1000L / targetFps).coerceIn(40L, 100L)
                delay(delayMs)
            }
        }
    }

    /**
     * Pauses capture when screen is turned off to save battery.
     */
    fun pause() {
        isPaused = true
    }

    /**
     * Resumes capture when screen is turned back on.
     */
    fun resume() {
        isPaused = false
    }

    /**
     * Stops sampling and releases all hardware resources.
     */
    fun stop() {
        isSampling = false
        isPaused = false
        samplingJob?.cancel()
        samplingJob = null

        try {
            virtualDisplay?.release()
        } catch (e: Exception) {
            // Safe cleanup
        }
        virtualDisplay = null

        try {
            imageReader?.close()
        } catch (e: Exception) {
            // Safe cleanup
        }
        imageReader = null

        handlerThread?.quitSafely()
        handlerThread = null
        backgroundHandler = null

        try {
            mediaProjection?.unregisterCallback(projectionCallback)
            mediaProjection?.stop()
        } catch (e: Exception) {
            // Safe cleanup
        }
        mediaProjection = null
        currentCalculatedFps = 0
    }
}
