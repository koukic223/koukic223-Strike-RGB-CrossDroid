package com.example.effects.screensync

import android.graphics.Color
import com.example.model.RgbColor
import com.example.model.ScreenRegion
import java.nio.ByteBuffer

/**
 * High-efficiency, zero-allocation color analyzer for Screen Sync / Ambilight.
 * Optimized for low-power hardware (ARM32 / MT6735M, Android Go).
 * Directly processes the raw ByteBuffer without allocating temporary Bitmaps.
 */
object ColorAnalyzer {

    fun analyze(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        pixelStride: Int,
        rowStride: Int,
        region: ScreenRegion,
        colorBoost: Boolean
    ): RgbColor {
        if (width <= 0 || height <= 0) return RgbColor(0, 0, 0)

        // Calculate region vertical bounds
        val startY: Int
        val endY: Int
        when (region) {
            ScreenRegion.FULL_SCREEN -> {
                startY = 0
                endY = height
            }
            ScreenRegion.TOP -> {
                startY = 0
                endY = (height / 3).coerceAtLeast(1)
            }
            ScreenRegion.CENTER -> {
                startY = (height / 3).coerceAtLeast(0)
                endY = ((2 * height) / 3).coerceAtMost(height)
            }
            ScreenRegion.BOTTOM -> {
                startY = ((2 * height) / 3).coerceAtMost(height - 1)
                endY = height
            }
        }

        var sumWeightedR = 0.0
        var sumWeightedG = 0.0
        var sumWeightedB = 0.0
        var totalWeight = 0.0

        var nonDarkCount = 0
        var totalSampled = 0

        val hsvTemp = FloatArray(3)
        val bufferCapacity = buffer.limit()

        for (y in startY until endY) {
            val rowOffset = y * rowStride
            for (x in 0 until width) {
                val pixelOffset = rowOffset + x * pixelStride
                if (pixelOffset + 2 >= bufferCapacity) continue

                totalSampled++

                val r = buffer.get(pixelOffset).toInt() and 0xFF
                val g = buffer.get(pixelOffset + 1).toInt() and 0xFF
                val b = buffer.get(pixelOffset + 2).toInt() and 0xFF

                // Fast perceptual luminance (Rec. 601)
                val lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255.0f

                val max = maxOf(r, maxOf(g, b))
                val min = minOf(r, minOf(g, b))
                val delta = max - min
                val sat = if (max == 0) 0f else delta.toFloat() / max.toFloat()

                // Ignore near-black pixels from diluting the vivid color (e.g. 80% black letterboxes / dark themes)
                if (lum < 0.06f) {
                    continue
                }

                nonDarkCount++

                // Saturation and luminance weighting:
                // Saturated and bright pixels are weighted heavily so key content dominates over neutral dark background
                val weight = (sat * sat * 3.2f + 0.2f) * (lum * 1.5f + 0.15f)

                sumWeightedR += r * weight
                sumWeightedG += g * weight
                sumWeightedB += b * weight
                totalWeight += weight
            }
        }

        // If the entire sampled screen area is dark/black, return near-black
        if (totalWeight <= 0.0 || nonDarkCount == 0) {
            return RgbColor(0, 0, 0)
        }

        var rawR = (sumWeightedR / totalWeight).toInt().coerceIn(0, 255)
        var rawG = (sumWeightedG / totalWeight).toInt().coerceIn(0, 255)
        var rawB = (sumWeightedB / totalWeight).toInt().coerceIn(0, 255)

        // Scale by overall screen brightness ratio so a dark screen dims naturally
        // but 20% bright color with 80% black still gives solid ~40-50% visibility
        val contentRatio = (nonDarkCount.toFloat() / totalSampled.coerceAtLeast(1).toFloat())
        val visibilityScale = (contentRatio * 1.6f).coerceIn(0.38f, 1.0f)

        rawR = (rawR * visibilityScale).toInt().coerceIn(0, 255)
        rawG = (rawG * visibilityScale).toInt().coerceIn(0, 255)
        rawB = (rawB * visibilityScale).toInt().coerceIn(0, 255)

        // Apply Color Boost if enabled: safely increase saturation without blowing out
        if (colorBoost) {
            Color.RGBToHSV(rawR, rawG, rawB, hsvTemp)
            // Moderately boost saturation (by 35%)
            hsvTemp[1] = (hsvTemp[1] * 1.35f).coerceIn(0f, 1.0f)
            // Slightly lift value to ensure vividness
            hsvTemp[2] = (hsvTemp[2] * 1.10f).coerceIn(0f, 1.0f)
            val boosted = Color.HSVToColor(hsvTemp)
            rawR = Color.red(boosted)
            rawG = Color.green(boosted)
            rawB = Color.blue(boosted)
        }

        return RgbColor(rawR, rawG, rawB)
    }
}
