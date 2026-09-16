package com.example.notification

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.example.model.EffectType
import com.example.model.LightFlowColorMode
import com.example.model.LightFlowProfile
import com.example.model.NotificationPriority
import com.example.model.RgbColor
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

/**
 * App Color Extraction Engine for Strike RGB CrossDroid Light Flow.
 *
 * Pipeline:
 * Application Icon
 *  -> Downscale to 64x64
 *  -> Remove transparent pixels
 *  -> Analyze dominant colors & HSV saturation/value
 *  -> Ignore near-black and near-white background noise
 *  -> Rank colors by prominence & vibrancy
 *  -> Select primary LED colors
 *  -> Cache and generate notification profiles
 */
object AppColorExtractor {

    private const val TAG = "AppColorExtractor"
    private val colorCache = ConcurrentHashMap<String, List<RgbColor>>()

    // Well-known signature palettes for top apps
    private val knownAppPalettes: Map<String, List<RgbColor>> = mapOf(
        "com.instagram.android" to listOf(
            RgbColor(225, 48, 108),  // Instagram Pink/Magenta
            RgbColor(253, 29, 29),   // Instagram Orange-Red
            RgbColor(131, 58, 180),  // Instagram Purple
            RgbColor(252, 175, 69)   // Instagram Warm Yellow
        ),
        "com.google.android.youtube" to listOf(
            RgbColor(255, 0, 0),     // YouTube Red
            RgbColor(255, 255, 255), // Pure White
            RgbColor(40, 40, 40)     // Dark Accent
        ),
        "com.facebook.katana" to listOf(
            RgbColor(24, 119, 242),  // Facebook Blue
            RgbColor(255, 255, 255), // Facebook White
            RgbColor(13, 80, 168)    // Facebook Deep Navy
        ),
        "com.whatsapp" to listOf(
            RgbColor(37, 211, 102),  // WhatsApp Emerald
            RgbColor(18, 140, 126),  // WhatsApp Dark Teal
            RgbColor(255, 255, 255)  // White
        ),
        "org.telegram.messenger" to listOf(
            RgbColor(0, 136, 204),   // Telegram Cyan Blue
            RgbColor(36, 161, 222),  // Telegram Sky Blue
            RgbColor(255, 255, 255)  // White
        ),
        "com.google.android.gm" to listOf(
            RgbColor(234, 67, 53),   // Gmail Red
            RgbColor(251, 188, 5),   // Google Yellow
            RgbColor(66, 133, 244),  // Google Blue
            RgbColor(52, 168, 83)    // Google Green
        ),
        "com.google.android.apps.messaging" to listOf(
            RgbColor(11, 87, 208),   // Messages Blue
            RgbColor(26, 115, 232),  // Google Light Blue
            RgbColor(255, 255, 255)  // White
        ),
        "com.twitter.android" to listOf(
            RgbColor(29, 155, 240),  // Twitter Sky Blue
            RgbColor(255, 255, 255), // White
            RgbColor(0, 0, 0)        // X Black
        ),
        "com.spotify.music" to listOf(
            RgbColor(30, 215, 96),   // Spotify Neon Green
            RgbColor(18, 18, 18),    // Spotify Jet Black
            RgbColor(255, 255, 255)  // White
        ),
        "com.snapchat.android" to listOf(
            RgbColor(255, 252, 0),   // Snapchat Yellow
            RgbColor(0, 0, 0),       // Black
            RgbColor(255, 255, 255)  // White
        ),
        "com.zhiliaoapp.musically" to listOf(
            RgbColor(0, 242, 234),   // TikTok Cyan
            RgbColor(254, 44, 85),   // TikTok Red/Pink
            RgbColor(0, 0, 0)        // Black
        ),
        "com.discord" to listOf(
            RgbColor(88, 101, 242),  // Discord Blurple
            RgbColor(255, 255, 255), // White
            RgbColor(47, 49, 54)     // Charcoal
        ),
        "com.reddit.frontpage" to listOf(
            RgbColor(255, 69, 0),    // Reddit Orange-Red
            RgbColor(255, 255, 255), // White
            RgbColor(0, 121, 211)    // Accent Blue
        ),
        "com.android.phone" to listOf(
            RgbColor(37, 211, 102),  // Phone Call Green
            RgbColor(11, 87, 208)    // Phone Dial Blue
        )
    )

    /**
     * Extracts dominant colors from application icon using the analysis pipeline.
     */
    fun extractColorsForPackage(context: Context, packageName: String): List<RgbColor> {
        // Return cached if available
        colorCache[packageName]?.let { return it }

        // Check if known palette exists
        val known = knownAppPalettes[packageName]
        if (known != null) {
            colorCache[packageName] = known
            return known
        }

        // Fuzzy match for common app package names
        for ((key, palette) in knownAppPalettes) {
            val keyCore = key.substringAfterLast('.').lowercase()
            if (packageName.lowercase().contains(keyCore)) {
                colorCache[packageName] = palette
                return palette
            }
        }

        // Real dynamic extraction from device Package Manager icon
        val extracted = try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val iconDrawable = pm.getApplicationIcon(appInfo)
            analyzeIconDrawable(iconDrawable)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to extract icon colors for $packageName: ${e.message}")
            emptyList()
        }

        val result = if (extracted.isNotEmpty()) {
            extracted
        } else {
            // Default vibrant fallback based on package hash
            listOf(generateFallbackColor(packageName))
        }

        colorCache[packageName] = result
        return result
    }

    /**
     * Analyzes a Drawable icon: downscales, removes transparency, filters extremes, quantizes HSV
     */
    fun analyzeIconDrawable(drawable: Drawable): List<RgbColor> {
        val bitmap = drawableToBitmap(drawable, 64, 64) ?: return emptyList()

        val width = bitmap.width
        val height = bitmap.height
        val totalPixels = width * height
        val pixels = IntArray(totalPixels)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Hue bucket histogram (12 buckets of 30 degrees each)
        data class ColorSample(val r: Int, val g: Int, val b: Int, val saturation: Float, val value: Float)
        val hueBuckets = Array(12) { mutableListOf<ColorSample>() }
        var validPixelCount = 0

        for (pixel in pixels) {
            val alpha = Color.alpha(pixel)
            if (alpha < 60) continue // Skip transparent or semi-transparent rim pixels

            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            val hsv = FloatArray(3)
            Color.RGBToHSV(r, g, b, hsv)
            val hue = hsv[0]
            val sat = hsv[1]
            val value = hsv[2]

            // Ignore extreme near-black noise or washed-out near-white/pure gray
            if (value < 0.15f) continue
            if (sat < 0.12f && value > 0.88f) continue

            val bucketIdx = ((hue % 360f) / 30f).toInt().coerceIn(0, 11)
            hueBuckets[bucketIdx].add(ColorSample(r, g, b, sat, value))
            validPixelCount++
        }

        if (validPixelCount == 0) {
            // All pixels were near-black or white: return primary neutral
            return listOf(RgbColor(255, 255, 255), RgbColor(11, 87, 208))
        }

        // Rank hue buckets by: frequency * average vibrancy (saturation * value)
        val rankedBuckets = hueBuckets.mapIndexed { idx, samples ->
            if (samples.isEmpty()) null
            else {
                val avgSat = samples.map { it.saturation }.average().toFloat()
                val avgVal = samples.map { it.value }.average().toFloat()
                val score = samples.size.toFloat() * (0.4f + 0.6f * avgSat * avgVal)
                val avgR = samples.map { it.r }.average().toInt()
                val avgG = samples.map { it.g }.average().toInt()
                val avgB = samples.map { it.b }.average().toInt()
                Triple(idx, score, RgbColor(avgR, avgG, avgB))
            }
        }.filterNotNull().sortedByDescending { it.second }

        val dominantColors = mutableListOf<RgbColor>()
        for (ranked in rankedBuckets) {
            val color = ranked.third
            // Ensure colors are visually distinct (Euclidean color distance > 60)
            val isDistinct = dominantColors.none { existing ->
                val dr = existing.r - color.r
                val dg = existing.g - color.g
                val db = existing.b - color.b
                kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble()) < 65.0
            }
            if (isDistinct) {
                dominantColors.add(color)
                if (dominantColors.size >= 4) break
            }
        }

        return if (dominantColors.isEmpty()) {
            listOf(RgbColor(11, 87, 208))
        } else dominantColors
    }

    /**
     * Converts a drawable to a hardware-scaled bitmap
     */
    private fun drawableToBitmap(drawable: Drawable, reqWidth: Int, reqHeight: Int): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return Bitmap.createScaledBitmap(drawable.bitmap, reqWidth, reqHeight, true)
        }

        val bitmap = Bitmap.createBitmap(
            reqWidth,
            reqHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Deterministic, beautiful saturated color from package name hash
     */
    private fun generateFallbackColor(pkg: String): RgbColor {
        val hash = kotlin.math.abs(pkg.hashCode())
        val hue = (hash % 360).toFloat()
        val hsv = floatArrayOf(hue, 0.90f, 0.95f)
        val colorInt = Color.HSVToColor(hsv)
        return RgbColor(Color.red(colorInt), Color.green(colorInt), Color.blue(colorInt))
    }

    /**
     * Generates a complete default LightFlowProfile for a given app
     */
    fun generateDefaultProfile(
        context: Context,
        packageName: String,
        appName: String
    ): LightFlowProfile {
        val detected = extractColorsForPackage(context, packageName)
        val primaryColor = detected.firstOrNull() ?: RgbColor.Blue

        return LightFlowProfile(
            packageName = packageName,
            appName = appName,
            isEnabled = true,
            colorMode = LightFlowColorMode.AUTO_APP_COLOR,
            colors = detected.ifEmpty { listOf(primaryColor) },
            detectedColors = detected,
            effect = EffectType.BREATHING,
            brightness = 0.85f,
            speed = 1.0f,
            durationSeconds = 5,
            repeatCount = 3,
            delayMs = 200L,
            priority = NotificationPriority.NORMAL
        )
    }
}
