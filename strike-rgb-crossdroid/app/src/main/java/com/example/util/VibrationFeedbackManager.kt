package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Manages tactile vibration and haptic feedback across the application.
 * Provides distinct, tactile sensations for toggling LEDs, selecting
 * color profiles, switching modes, and interactive UI gestures.
 */
class VibrationFeedbackManager(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w("VibrationManager", "Failed to access system Vibrator: ${e.message}")
            null
        }
    }

    var isEnabled: Boolean = true

    /**
     * Tactile feedback when the user toggles LEDs ON or OFF.
     * ON: Crisp double-click / energetic tactile pop.
     * OFF: Gentle single click / release impulse.
     */
    fun vibrateLedToggle(turnedOn: Boolean) {
        if (!isEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (turnedOn) {
                    val effect = try {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                    } catch (_: Exception) {
                        VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                    v.vibrate(effect)
                } else {
                    val effect = try {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    } catch (_: Exception) {
                        VibrationEffect.createOneShot(20, (VibrationEffect.DEFAULT_AMPLITUDE * 0.7f).toInt().coerceIn(1, 255))
                    }
                    v.vibrate(effect)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (turnedOn) {
                    val timings = longArrayOf(0, 30, 40, 35)
                    val amplitudes = intArrayOf(0, 220, 0, 255)
                    v.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    v.vibrate(VibrationEffect.createOneShot(25, 140))
                }
            } else {
                @Suppress("DEPRECATION")
                if (turnedOn) {
                    v.vibrate(longArrayOf(0, 30, 40, 35), -1)
                } else {
                    v.vibrate(25)
                }
            }
        } catch (e: Exception) {
            Log.d("VibrationManager", "Vibration failed: ${e.message}")
        }
    }

    /**
     * Satisfying tactile feedback when selecting a new color profile or preset swatch.
     * Produces a distinct, snappy micro-pulse.
     */
    fun vibrateColorProfileSelected() {
        if (!isEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = try {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                } catch (_: Exception) {
                    VibrationEffect.createOneShot(18, 180)
                }
                v.vibrate(effect)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(18, 180))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(18)
            }
        } catch (e: Exception) {
            Log.d("VibrationManager", "Vibration failed: ${e.message}")
        }
    }

    /**
     * Subtle micro tick for sliders or continuous adjustments
     */
    fun vibrateTick() {
        if (!isEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(10, 80))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(10)
            }
        } catch (_: Exception) { }
    }

    /**
     * Haptic click for major UI button presses
     */
    fun vibrateClick() {
        if (!isEnabled) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(25)
            }
        } catch (_: Exception) { }
    }
}
