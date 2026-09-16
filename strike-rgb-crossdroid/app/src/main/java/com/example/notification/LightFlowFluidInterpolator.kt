package com.example.notification

import android.graphics.Color
import com.example.model.EffectType
import com.example.model.RgbColor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Fluid Color Interpolation and Physical Frame Generation for Strike RGB Light Flow.
 *
 * Implements smooth HSV/HSL circular shortest-path blending without harsh jumps,
 * supporting multi-color sequences, gradients, and all 16 notification effects.
 */
object LightFlowFluidInterpolator {

    /**
     * Interpolates smoothly between two colors in HSV color space using shortest-path hue wrapping.
     */
    fun interpolateHsv(c1: RgbColor, c2: RgbColor, fraction: Float): RgbColor {
        val t = fraction.coerceIn(0f, 1f)
        val hsv1 = FloatArray(3)
        val hsv2 = FloatArray(3)
        Color.RGBToHSV(c1.r, c1.g, c1.b, hsv1)
        Color.RGBToHSV(c2.r, c2.g, c2.b, hsv2)

        // Shortest-path circular hue interpolation
        var h1 = hsv1[0]
        var h2 = hsv2[0]
        val diff = h2 - h1
        if (abs(diff) > 180f) {
            if (h2 > h1) {
                h1 += 360f
            } else {
                h2 += 360f
            }
        }
        val interpolatedHue = ((h1 + (h2 - h1) * t) % 360f + 360f) % 360f
        val interpolatedSat = hsv1[1] + (hsv2[1] - hsv1[1]) * t
        val interpolatedVal = hsv1[2] + (hsv2[2] - hsv1[2]) * t

        val resultInt = Color.HSVToColor(floatArrayOf(interpolatedHue, interpolatedSat, interpolatedVal))
        return RgbColor(Color.red(resultInt), Color.green(resultInt), Color.blue(resultInt))
    }

    /**
     * Smoothly blends across a sequence of colors over normalized phase [0..1]
     */
    fun sampleColorSequence(colors: List<RgbColor>, phase: Float): RgbColor {
        if (colors.isEmpty()) return RgbColor.Blue
        if (colors.size == 1) return colors[0]

        val p = (phase % 1.0f + 1.0f) % 1.0f
        val segmentCount = colors.size
        val totalPosition = p * segmentCount
        val index1 = totalPosition.toInt() % segmentCount
        val index2 = (index1 + 1) % segmentCount
        val localFraction = totalPosition - index1.toFloat()

        return interpolateHsv(colors[index1], colors[index2], localFraction)
    }

    /**
     * Computes the exact (r, g, b) LED output for an active effect at elapsed time tMs.
     */
    fun computeFrame(
        effect: EffectType,
        colors: List<RgbColor>,
        elapsedMs: Long,
        speedFactor: Float,
        brightness: Float,
        intensity: Float = 1.0f
    ): Triple<Int, Int, Int> {
        val safeColors = if (colors.isEmpty()) listOf(RgbColor.Blue) else colors
        val safeBrightness = brightness.coerceIn(0.02f, 1.0f)
        val safeSpeed = speedFactor.coerceIn(0.2f, 3.5f)

        when (effect) {
            EffectType.STATIC -> {
                val primary = safeColors.first()
                val r = (primary.r * safeBrightness).roundToInt().coerceIn(0, 255)
                val g = (primary.g * safeBrightness).roundToInt().coerceIn(0, 255)
                val b = (primary.b * safeBrightness).roundToInt().coerceIn(0, 255)
                return Triple(r, g, b)
            }

            EffectType.BREATHING -> {
                val periodMs = (2400 / safeSpeed).toLong().coerceAtLeast(400L)
                val phase = (elapsedMs % periodMs).toFloat() / periodMs.toFloat()
                val sine = ((sin(phase * 2 * PI - PI / 2) + 1.0) / 2.0).toFloat()
                val minDepth = 1.0f - intensity.coerceIn(0.1f, 1.0f)
                val factor = (minDepth + (1f - minDepth) * sine) * safeBrightness

                val activeColor = if (safeColors.size > 1) {
                    val colorCyclePhase = (elapsedMs.toFloat() / (periodMs * safeColors.size)) % 1.0f
                    sampleColorSequence(safeColors, colorCyclePhase)
                } else safeColors.first()

                return scaleColor(activeColor, factor)
            }

            EffectType.PULSE -> {
                val periodMs = (1000 / safeSpeed).toLong().coerceAtLeast(200L)
                val phase = (elapsedMs % periodMs).toFloat() / periodMs.toFloat()
                val wave = ((sin(phase * 2 * PI - PI / 2) + 1.0) / 2.0).toFloat()
                val pulseCurve = wave * wave
                val factor = (0.02f + pulseCurve * 0.98f) * safeBrightness

                val activeColor = if (safeColors.size > 1) {
                    val cPhase = (elapsedMs.toFloat() / (periodMs * safeColors.size * 2)) % 1.0f
                    sampleColorSequence(safeColors, cPhase)
                } else safeColors.first()

                return scaleColor(activeColor, factor)
            }

            EffectType.HEARTBEAT -> {
                val periodMs = (1200 / safeSpeed).toLong().coerceAtLeast(300L)
                val t = (elapsedMs % periodMs).toFloat() / periodMs.toFloat()
                val amplitude = when {
                    t < 0.14f -> sin((t / 0.14f) * PI.toFloat())
                    t < 0.22f -> 0.04f
                    t < 0.38f -> 0.85f * sin(((t - 0.22f) / 0.16f) * PI.toFloat())
                    else -> 0.02f
                }
                val factor = (amplitude * intensity).coerceIn(0.02f, 1f) * safeBrightness
                return scaleColor(safeColors.first(), factor)
            }

            EffectType.WAVE -> {
                val periodMs = (2000 / safeSpeed).toLong().coerceAtLeast(300L)
                val angle = (elapsedMs % periodMs).toFloat() / periodMs.toFloat() * 2 * PI.toFloat()
                val rWave = ((sin(angle) + 1.0) / 2.0).toFloat()
                val gWave = ((sin(angle + (2 * PI / 3).toFloat()) + 1.0) / 2.0).toFloat()
                val bWave = ((sin(angle + (4 * PI / 3).toFloat()) + 1.0) / 2.0).toFloat()

                val base = safeColors.first()
                val r = (base.r * rWave * safeBrightness).roundToInt().coerceIn(0, 255)
                val g = (base.g * gWave * safeBrightness).roundToInt().coerceIn(0, 255)
                val b = (base.b * bWave * safeBrightness).roundToInt().coerceIn(0, 255)
                return Triple(r, g, b)
            }

            EffectType.RAINBOW, EffectType.RAINBOW_CYCLE -> {
                val periodMs = (3000 / safeSpeed).toLong().coerceAtLeast(500L)
                val hue = ((elapsedMs % periodMs).toFloat() / periodMs.toFloat()) * 360f
                val hsv = floatArrayOf(hue, 1.0f, safeBrightness)
                val c = Color.HSVToColor(hsv)
                return Triple(Color.red(c), Color.green(c), Color.blue(c))
            }

            EffectType.STROBE -> {
                val halfPeriod = (80 / safeSpeed).toLong().coerceIn(25L, 250L)
                val isOn = (elapsedMs / halfPeriod) % 2 == 0L
                if (isOn) {
                    val activeColor = safeColors.first()
                    return scaleColor(activeColor, safeBrightness)
                } else {
                    return Triple(0, 0, 0)
                }
            }

            EffectType.FLASH -> {
                val periodMs = (800 / safeSpeed).toLong().coerceAtLeast(150L)
                val flashDurationMs = (200 / safeSpeed).toLong().coerceAtLeast(50L)
                val t = elapsedMs % periodMs
                if (t < flashDurationMs) {
                    return scaleColor(safeColors.first(), safeBrightness)
                } else {
                    return Triple(0, 0, 0)
                }
            }

            EffectType.DOUBLE_FLASH -> {
                val periodMs = (1000 / safeSpeed).toLong().coerceAtLeast(200L)
                val flashMs = (120 / safeSpeed).toLong().coerceAtLeast(30L)
                val t = elapsedMs % periodMs
                val isOn = (t < flashMs) || (t in (flashMs * 2)..(flashMs * 3))
                if (isOn) {
                    return scaleColor(safeColors.first(), safeBrightness)
                } else {
                    return Triple(0, 0, 0)
                }
            }

            EffectType.TRIPLE_FLASH -> {
                val periodMs = (1200 / safeSpeed).toLong().coerceAtLeast(300L)
                val flashMs = (90 / safeSpeed).toLong().coerceAtLeast(25L)
                val t = elapsedMs % periodMs
                val isOn = (t < flashMs) ||
                        (t in (flashMs * 2)..(flashMs * 3)) ||
                        (t in (flashMs * 4)..(flashMs * 5))
                if (isOn) {
                    return scaleColor(safeColors.first(), safeBrightness)
                } else {
                    return Triple(0, 0, 0)
                }
            }

            EffectType.FADE -> {
                val periodMs = (1800 / safeSpeed).toLong().coerceAtLeast(300L)
                val t = (elapsedMs % periodMs).toFloat() / periodMs.toFloat()
                // Sharp attack, slow exponential fade
                val fadeVal = ((1f - t) * (1f - t)).coerceIn(0.01f, 1f) * safeBrightness
                return scaleColor(safeColors.first(), fadeVal)
            }

            EffectType.COLOR_CYCLE, EffectType.SMOOTH_GRADIENT -> {
                val periodMs = (2500 / safeSpeed).toLong().coerceAtLeast(400L)
                val phase = (elapsedMs % periodMs).toFloat() / periodMs.toFloat()
                val blendedColor = sampleColorSequence(safeColors, phase)
                return scaleColor(blendedColor, safeBrightness)
            }

            EffectType.AURORA -> {
                val periodMs = (3200 / safeSpeed).toLong().coerceAtLeast(500L)
                val t = (elapsedMs % periodMs).toFloat() / periodMs.toFloat()
                val angle = t * 2 * PI.toFloat()
                // Aurora shifts between emerald green, cyan, indigo, and violet
                val auroraColors = listOf(
                    RgbColor(0, 255, 140),   // Aurora Emerald
                    RgbColor(0, 210, 255),   // Aurora Cyan
                    RgbColor(75, 0, 230),    // Aurora Indigo
                    RgbColor(180, 0, 255)    // Aurora Violet
                )
                val waveFactor = ((sin(angle) + 1.0) / 2.0).toFloat()
                val activeAurora = sampleColorSequence(auroraColors, t)
                val factor = (0.35f + 0.65f * waveFactor) * safeBrightness
                return scaleColor(activeAurora, factor)
            }

            EffectType.SPARKLE -> {
                // Pseudo-random sparkling flashes with soft baseline glow
                val step = (elapsedMs / (70 / safeSpeed).toLong().coerceAtLeast(20L))
                val seed = (step * 2654435761L)
                val isFlash = (seed % 5L) == 0L
                val factor = if (isFlash) safeBrightness else (0.15f * safeBrightness)
                val colorIdx = (seed % safeColors.size.toLong()).toInt()
                val activeColor = safeColors[colorIdx]
                return scaleColor(activeColor, factor)
            }

            EffectType.SCREEN_SYNC, EffectType.AMBILIGHT -> {
                val primary = safeColors.first()
                return scaleColor(primary, safeBrightness)
            }
        }
    }

    private fun scaleColor(color: RgbColor, factor: Float): Triple<Int, Int, Int> {
        val f = factor.coerceIn(0f, 1f)
        val r = (color.r * f).roundToInt().coerceIn(0, 255)
        val g = (color.g * f).roundToInt().coerceIn(0, 255)
        val b = (color.b * f).roundToInt().coerceIn(0, 255)
        return Triple(r, g, b)
    }
}
