package com.example.ui.background

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.util.FastBlur
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Singleton manager responsible for loading, downscaling, precomputing,
 * and caching discrete blur levels of the custom wallpaper for 60fps real-time responsiveness.
 *
 * Prevents expensive per-frame blur recalculation on ARM32 / Android Go devices (MT6735M).
 */
object CustomBackgroundManager {

    private const val TAG = "CustomBackgroundMgr"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Cached ImageBitmaps for 0%, 10%, 20%, 30%, 40%, 50%, 60%, 70%, 80%, 90%, 100%
    private val blurCache = mutableMapOf<Int, ImageBitmap>()

    private var currentSourcePath: String? = null
    private var originalSourceBitmap: Bitmap? = null

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _cacheVersion = MutableStateFlow(0)
    val cacheVersion: StateFlow<Int> = _cacheVersion.asStateFlow()

    /**
     * Load an image file from the specified path and generate blur cache.
     */
    fun loadFromPath(path: String?) {
        if (path.isNullOrEmpty()) {
            clear()
            return
        }

        if (path == currentSourcePath && _isLoaded.value) {
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                val file = File(path)
                if (!file.exists()) {
                    clear()
                    return@launch
                }

                // Decode with sample size to fit 540x960 device memory constraints
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bitmap = BitmapFactory.decodeFile(path, options)
                if (bitmap != null) {
                    currentSourcePath = path
                    loadAndCache(bitmap)
                } else {
                    clear()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode background image at $path", e)
                clear()
            }
        }
    }

    /**
     * Cache the source bitmap and precompute discrete blur levels.
     * Step 0% is strictly the crisp, unblurred original image.
     */
    fun loadAndCache(source: Bitmap) {
        originalSourceBitmap = source

        synchronized(blurCache) {
            blurCache.clear()
            // 0% is always the completely sharp original bitmap
            blurCache[0] = source.asImageBitmap()
        }
        _isLoaded.value = true
        _cacheVersion.value += 1

        // Precompute blur levels asynchronously
        scope.launch(Dispatchers.Default) {
            try {
                // Downscale moderately for fast stack blur (width ~ 360px)
                val scale = 360f / max(source.width, 1).toFloat()
                val targetW = (source.width * scale).roundToInt().coerceAtLeast(120)
                val targetH = (source.height * scale).roundToInt().coerceAtLeast(120)
                val downscaled = Bitmap.createScaledBitmap(source, targetW, targetH, true)

                // Precise blur radius mapping:
                // 10% -> 4, 20% -> 8, 30% -> 13, 40% -> 19, 50% -> 26, 60% -> 34, 70% -> 43, 80% -> 53, 90% -> 65, 100% -> 78
                val radiusMap = mapOf(
                    10 to 4,
                    20 to 8,
                    30 to 13,
                    40 to 19,
                    50 to 26,
                    60 to 34,
                    70 to 43,
                    80 to 53,
                    90 to 65,
                    100 to 78
                )

                for ((step, radius) in radiusMap) {
                    val blurred = FastBlur.stackBlur(downscaled, radius)
                    if (blurred != null) {
                        synchronized(blurCache) {
                            blurCache[step] = blurred.asImageBitmap()
                        }
                    }
                }
                _cacheVersion.value += 1
                Log.d(TAG, "Blur cache precomputed successfully for ${blurCache.size} levels")
            } catch (e: Exception) {
                Log.e(TAG, "Error generating background blur cache", e)
            }
        }
    }

    /**
     * Retrieve the cached ImageBitmap corresponding to the given blur intensity (0.0f .. 1.0f).
     *
     * 0.0f returns the completely sharp unblurred original image.
     * Higher values return the corresponding cached blur level with O(1) lookup.
     */
    fun getBitmapForIntensity(intensity: Float): ImageBitmap? {
        val clamped = intensity.coerceIn(0f, 1f)
        val percent = (clamped * 100).roundToInt()

        // 0% - 2% returns strictly the sharp original
        if (percent <= 2) {
            return synchronized(blurCache) {
                blurCache[0]
            }
        }

        // Round to nearest 10% step (10, 20, 30, 40, 50, 60, 70, 80, 90, 100)
        val roundedStep = (((percent + 4) / 10) * 10).coerceIn(10, 100)
        return synchronized(blurCache) {
            blurCache[roundedStep] ?: blurCache[0]
        }
    }

    /**
     * Clear all cached bitmaps and reset state.
     */
    fun clear() {
        synchronized(blurCache) {
            blurCache.clear()
        }
        originalSourceBitmap = null
        currentSourcePath = null
        _isLoaded.value = false
        _cacheVersion.value += 1
    }
}
