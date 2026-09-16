package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.RgbColor

// Default Pixel Accent Palette (when Dynamic Color is OFF)
val PixelDefaultLightPrimary = Color(0xFF006876)
val PixelDefaultLightOnPrimary = Color(0xFFFFFFFF)
val PixelDefaultLightPrimaryContainer = Color(0xFFA1EFFF)
val PixelDefaultLightOnPrimaryContainer = Color(0xFF001F25)
val PixelDefaultLightSecondary = Color(0xFF4A6267)
val PixelDefaultLightSurface = Color(0xFFF8F9FA)
val PixelDefaultLightSurfaceContainer = Color(0xFFEDEEF2)
val PixelDefaultLightSurfaceContainerHigh = Color(0xFFE2E4E9)
val PixelDefaultLightOnSurface = Color(0xFF191C1D)
val PixelDefaultLightOutline = Color(0xFF70787A)
val PixelDefaultLightError = Color(0xFFBA1A1A)

val PixelDefaultDarkPrimary = Color(0xFF82D3E4)
val PixelDefaultDarkOnPrimary = Color(0xFF00363E)
val PixelDefaultDarkPrimaryContainer = Color(0xFF004E59)
val PixelDefaultDarkOnPrimaryContainer = Color(0xFFA1EFFF)
val PixelDefaultDarkSecondary = Color(0xFFB1CBD0)
val PixelDefaultDarkSurface = Color(0xFF131417)
val PixelDefaultDarkSurfaceContainer = Color(0xFF1C1D21)
val PixelDefaultDarkSurfaceContainerHigh = Color(0xFF26282E)
val PixelDefaultDarkOnSurface = Color(0xFFE1E3E5)
val PixelDefaultDarkOutline = Color(0xFF8A9294)
val PixelDefaultDarkError = Color(0xFFFFB4AB)

// Helpers to dynamically generate harmonic colors from chosen RGB LED
fun dynamicLightColorForLed(led: RgbColor): Color {
    // Normalize and avoid overly bright/washed out colors for primary on light bg
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(led.r, led.g, led.b, hsv)
    // Constrain saturation and value for accessible Material 3 light primary
    val sat = hsv[1].coerceIn(0.6f, 0.95f)
    val value = hsv[2].coerceIn(0.35f, 0.65f)
    val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], sat, value))
    return Color(colorInt)
}

fun dynamicDarkColorForLed(led: RgbColor): Color {
    // For dark surfaces, primary should be pastel/vibrant with higher lightness (80-90% tone)
    val hsv = FloatArray(3)
    android.graphics.Color.RGBToHSV(led.r, led.g, led.b, hsv)
    val sat = (hsv[1] * 0.7f).coerceIn(0.25f, 0.65f)
    val value = 0.92f
    val colorInt = android.graphics.Color.HSVToColor(floatArrayOf(hsv[0], sat, value))
    return Color(colorInt)
}
