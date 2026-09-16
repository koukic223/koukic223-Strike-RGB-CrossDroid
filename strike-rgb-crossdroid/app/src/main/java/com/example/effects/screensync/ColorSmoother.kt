package com.example.effects.screensync

import com.example.model.RgbColor
import kotlin.math.roundToInt

/**
 * Temporal color interpolator for Ambilight / Screen Sync effect.
 * Smoothly transitions the physical LED between colors without harsh stepping.
 */
class ColorSmoother {

    private var currentR = 0.0f
    private var currentG = 0.0f
    private var currentB = 0.0f
    private var isInitialized = false

    /**
     * Resets the smoother to a given initial color or black.
     */
    fun reset(initialColor: RgbColor = RgbColor(0, 0, 0)) {
        currentR = initialColor.r.toFloat()
        currentG = initialColor.g.toFloat()
        currentB = initialColor.b.toFloat()
        isInitialized = true
    }

    /**
     * Smooths the transition from current color towards targetColor.
     * @param targetColor Newly detected target screen color
     * @param smoothingFactor 0f (Low / instant) to 1f (High / very gradual)
     */
    fun step(targetColor: RgbColor, smoothingFactor: Float): RgbColor {
        if (!isInitialized) {
            reset(targetColor)
            return targetColor
        }

        // Map smoothingFactor (0.0 .. 1.0) to interpolation weight alpha (0.75 .. 0.08)
        // High smoothing = smaller alpha (gentler transition)
        // Low smoothing = larger alpha (faster transition)
        val clampedSmoothing = smoothingFactor.coerceIn(0.0f, 1.0f)
        val alpha = (0.75f - clampedSmoothing * 0.65f).coerceIn(0.08f, 0.75f)

        currentR += alpha * (targetColor.r.toFloat() - currentR)
        currentG += alpha * (targetColor.g.toFloat() - currentG)
        currentB += alpha * (targetColor.b.toFloat() - currentB)

        return RgbColor(
            r = currentR.roundToInt().coerceIn(0, 255),
            g = currentG.roundToInt().coerceIn(0, 255),
            b = currentB.roundToInt().coerceIn(0, 255)
        )
    }

    fun getCurrentColor(): RgbColor {
        return RgbColor(
            r = currentR.roundToInt().coerceIn(0, 255),
            g = currentG.roundToInt().coerceIn(0, 255),
            b = currentB.roundToInt().coerceIn(0, 255)
        )
    }
}
