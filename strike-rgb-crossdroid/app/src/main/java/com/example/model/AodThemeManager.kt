package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Metadata and specification for built-in AOD Themes.
 */
data class AodThemeDefinition(
    val theme: AodTheme,
    val titleKey: String,
    val subtitle: String,
    val description: String,
    val categoryTag: String,
    val signatureColor: Color,
    val accentColors: List<Color>,
    val defaultPreset: AodConfig
)

/**
 * Central AOD Theme Management Engine supporting the 10 built-in presets and customization.
 */
object AodThemeManager {

    val THEME_DEFINITIONS: List<AodThemeDefinition> = listOf(
        // 1. LIQUID GLASS (iOS 26 Signature)
        AodThemeDefinition(
            theme = AodTheme.LIQUID_GLASS,
            titleKey = "aod_theme_liquid_glass",
            subtitle = "Tubular Specular Glass & Silk Caustics",
            description = "Translucent liquid glass stadium typography with chromatic reflections and an ambient silk backdrop.",
            categoryTag = "Signature",
            signatureColor = Color(0xFF00A3C4),
            accentColors = listOf(Color(0xFF00A3C4), Color(0xFF38BDF8), Color(0xFF0F172A)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.LIQUID_GLASS,
                clockType = AodClockType.DIGITAL,
                clockStyle = AodClockStyle.LIQUID_GLASS,
                clockSize = AodClockSize.LARGE,
                clockWeight = AodClockWeight.REGULAR,
                clockPosition = AodClockPosition.TOP,
                clockColor = RgbColor(0, 163, 196),
                isAutoColor = false,
                is24Hour = true,
                showDate = true,
                dateFormat = "EEE, MMM d",
                datePosition = AodDatePosition.ABOVE_CLOCK,
                dateStyle = AodDateStyle.GLOW,
                clockOpacity = 0.95f,
                clockBlur = 0.50f,
                clockRefraction = 0.65f,
                clockGloss = 0.80f,
                clockGlow = 0.70f,
                clockThickness = 0.60f,
                backgroundInteraction = true,
                showBatteryPercentage = true,
                showBatteryIcon = true,
                batteryStyle = AodBatteryStyle.CAPSULE,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = true,
                    position = AodEdgeGlowPosition.FULL,
                    color = RgbColor(0, 163, 196),
                    isRgbCycle = false,
                    brightness = 0.85f,
                    thickness = 6.0f,
                    glowRadius = 18.0f,
                    speed = 1.0f,
                    animation = AodEdgeAnimation.BREATHING
                )
            )
        ),

        // 2. PIXEL 17 (Retro Dot-Matrix)
        AodThemeDefinition(
            theme = AodTheme.PIXEL,
            titleKey = "aod_theme_pixel",
            subtitle = "Retro Dot-Matrix & CRT Scanline",
            description = "8-bit dot-matrix numerical typography with low-power phosphor green illumination and CRT scanlines.",
            categoryTag = "Retro 8-Bit",
            signatureColor = Color(0xFF39FF14),
            accentColors = listOf(Color(0xFF39FF14), Color(0xFF10B981), Color(0xFF022C22)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.PIXEL,
                clockType = AodClockType.DIGITAL,
                clockStyle = AodClockStyle.PIXEL,
                clockSize = AodClockSize.MEDIUM,
                clockWeight = AodClockWeight.BOLD,
                clockPosition = AodClockPosition.CENTER,
                clockColor = RgbColor(57, 255, 20),
                isAutoColor = false,
                is24Hour = true,
                showDate = true,
                dateFormat = "yyyy-MM-dd",
                datePosition = AodDatePosition.BELOW_CLOCK,
                dateStyle = AodDateStyle.MINIMAL,
                clockOpacity = 1.0f,
                showBatteryPercentage = true,
                showBatteryIcon = true,
                batteryStyle = AodBatteryStyle.MINIMAL_BAR,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = false,
                    position = AodEdgeGlowPosition.FULL,
                    color = RgbColor(57, 255, 20),
                    thickness = 4.0f,
                    animation = AodEdgeAnimation.STATIC
                )
            )
        ),

        // 3. NEON CYBERPUNK
        AodThemeDefinition(
            theme = AodTheme.NEON,
            titleKey = "aod_theme_neon",
            subtitle = "Gas-Discharge Magenta & Cyan Plasma",
            description = "High-voltage neon gas illumination with pulsing edge conduits and vivid dual-spectrum saturation.",
            categoryTag = "Cyberpunk",
            signatureColor = Color(0xFFFF007F),
            accentColors = listOf(Color(0xFFFF007F), Color(0xFF00F5FF), Color(0xFF3B0764)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.NEON,
                clockType = AodClockType.DIGITAL,
                clockStyle = AodClockStyle.LIQUID_GLASS,
                clockSize = AodClockSize.LARGE,
                clockWeight = AodClockWeight.BOLD,
                clockPosition = AodClockPosition.TOP,
                clockColor = RgbColor(255, 0, 127),
                isAutoColor = false,
                is24Hour = true,
                showDate = true,
                dateFormat = "EEE, MMM d",
                datePosition = AodDatePosition.BELOW_CLOCK,
                dateStyle = AodDateStyle.PILL,
                clockOpacity = 1.0f,
                clockGlow = 0.90f,
                showBatteryPercentage = true,
                showBatteryIcon = true,
                batteryStyle = AodBatteryStyle.CAPSULE,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = true,
                    position = AodEdgeGlowPosition.FULL,
                    color = RgbColor(0, 245, 255),
                    isRgbCycle = false,
                    brightness = 1.0f,
                    thickness = 8.0f,
                    glowRadius = 24.0f,
                    speed = 1.4f,
                    animation = AodEdgeAnimation.PULSE
                )
            )
        ),

        // 4. AURORA BOREALIS
        AodThemeDefinition(
            theme = AodTheme.AURORA,
            titleKey = "aod_theme_aurora",
            subtitle = "Polar Curtains & Emerald Waves",
            description = "Atmospheric sweeping curtains of emerald, cyan, and deep purple with tranquil wave animations.",
            categoryTag = "Atmospheric",
            signatureColor = Color(0xFF10B981),
            accentColors = listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF8B5CF6)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.AURORA,
                clockType = AodClockType.DIGITAL,
                clockStyle = AodClockStyle.MINIMAL,
                clockSize = AodClockSize.LARGE,
                clockWeight = AodClockWeight.LIGHT,
                clockPosition = AodClockPosition.TOP,
                clockColor = RgbColor(16, 185, 129),
                isAutoColor = false,
                is24Hour = true,
                showDate = true,
                dateFormat = "EEEE, MMMM d",
                datePosition = AodDatePosition.BELOW_CLOCK,
                dateStyle = AodDateStyle.PILL,
                clockOpacity = 0.90f,
                showBatteryPercentage = true,
                showBatteryIcon = true,
                batteryStyle = AodBatteryStyle.CIRCULAR_RING,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = true,
                    position = AodEdgeGlowPosition.TOP,
                    color = RgbColor(6, 182, 212),
                    isRgbCycle = false,
                    brightness = 0.90f,
                    thickness = 10.0f,
                    glowRadius = 22.0f,
                    speed = 0.8f,
                    animation = AodEdgeAnimation.WAVE
                )
            )
        ),

        // 5. DIGITAL HORIZON
        AodThemeDefinition(
            theme = AodTheme.DIGITAL,
            titleKey = "aod_theme_digital",
            subtitle = "Monochrome Studio Precision",
            description = "Ultra-high contrast digital LED numerals engineered for instant legibility in pitch darkness.",
            categoryTag = "Precision",
            signatureColor = Color(0xFFFFFFFF),
            accentColors = listOf(Color(0xFFFFFFFF), Color(0xFF94A3B8), Color(0xFF0F172A)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.DIGITAL,
                clockType = AodClockType.DIGITAL,
                clockStyle = AodClockStyle.DIGITAL,
                clockSize = AodClockSize.EXTRA_LARGE,
                clockWeight = AodClockWeight.BOLD,
                clockPosition = AodClockPosition.TOP,
                clockColor = RgbColor(255, 255, 255),
                isAutoColor = false,
                is24Hour = true,
                showDate = true,
                dateFormat = "EEE, MMM d",
                datePosition = AodDatePosition.BELOW_CLOCK,
                dateStyle = AodDateStyle.MINIMAL,
                showBatteryPercentage = true,
                showBatteryIcon = true,
                batteryStyle = AodBatteryStyle.PERCENT_ONLY,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = false,
                    position = AodEdgeGlowPosition.FULL,
                    color = RgbColor.White,
                    thickness = 4.0f
                )
            )
        ),

        // 6. PRECISION ANALOG
        AodThemeDefinition(
            theme = AodTheme.ANALOG,
            titleKey = "aod_theme_analog",
            subtitle = "Haute Horlogerie Sweep Dial",
            description = "Luxury timepiece dial with sweeping second tick, hour indices, and gold bezel illumination.",
            categoryTag = "Horology",
            signatureColor = Color(0xFFF59E0B),
            accentColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFF78350F)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.ANALOG,
                clockType = AodClockType.ANALOG,
                clockStyle = AodClockStyle.ANALOG,
                clockSize = AodClockSize.LARGE,
                clockWeight = AodClockWeight.REGULAR,
                clockPosition = AodClockPosition.CENTER,
                clockColor = RgbColor(245, 158, 11),
                isAutoColor = false,
                showDate = true,
                dateFormat = "EEE, MMM d",
                datePosition = AodDatePosition.BELOW_CLOCK,
                dateStyle = AodDateStyle.PILL,
                showBatteryPercentage = true,
                showBatteryIcon = true,
                batteryStyle = AodBatteryStyle.CIRCULAR_RING,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = true,
                    position = AodEdgeGlowPosition.FULL,
                    color = RgbColor(245, 158, 11),
                    brightness = 0.70f,
                    thickness = 4.0f,
                    glowRadius = 14.0f,
                    speed = 1.0f,
                    animation = AodEdgeAnimation.STATIC
                )
            )
        ),

        // 7. RGB CHROMA
        AodThemeDefinition(
            theme = AodTheme.RGB,
            titleKey = "aod_theme_rgb",
            subtitle = "Continuous 360° Spectrum Wave",
            description = "Dynamic chromatic hue cycling across clock numerals, battery indicators, and edge perimeter.",
            categoryTag = "Chroma",
            signatureColor = Color(0xFFEC4899),
            accentColors = listOf(Color(0xFFEF4444), Color(0xFF10B981), Color(0xFF3B82F6)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.RGB,
                clockType = AodClockType.DIGITAL,
                clockStyle = AodClockStyle.LIQUID_GLASS,
                clockSize = AodClockSize.LARGE,
                clockWeight = AodClockWeight.BOLD,
                clockPosition = AodClockPosition.TOP,
                clockColor = RgbColor(0, 163, 196),
                isAutoColor = false,
                is24Hour = true,
                showDate = true,
                dateFormat = "EEE, MMM d",
                datePosition = AodDatePosition.BELOW_CLOCK,
                dateStyle = AodDateStyle.PILL,
                showBatteryPercentage = true,
                showBatteryIcon = true,
                batteryStyle = AodBatteryStyle.CAPSULE,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = true,
                    position = AodEdgeGlowPosition.FULL,
                    isRgbCycle = true,
                    brightness = 1.0f,
                    thickness = 8.0f,
                    glowRadius = 22.0f,
                    speed = 1.6f,
                    animation = AodEdgeAnimation.WAVE
                )
            )
        ),

        // 8. NEWBIE MINIMAL
        AodThemeDefinition(
            theme = AodTheme.NEWBIE_MINIMAL,
            titleKey = "aod_theme_newbie_minimal",
            subtitle = "Ultra-Low Power OLED Saver",
            description = "Delicate hairline digits on pure black obsidian, designed for zero extra battery overhead.",
            categoryTag = "Eco Saver",
            signatureColor = Color(0xFF94A3B8),
            accentColors = listOf(Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF000000)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.NEWBIE_MINIMAL,
                clockType = AodClockType.DIGITAL,
                clockStyle = AodClockStyle.MINIMAL,
                clockSize = AodClockSize.MEDIUM,
                clockWeight = AodClockWeight.THIN,
                clockPosition = AodClockPosition.TOP,
                clockColor = RgbColor(148, 163, 184),
                isAutoColor = false,
                is24Hour = true,
                showDate = true,
                dateFormat = "EEE, MMM d",
                datePosition = AodDatePosition.BELOW_CLOCK,
                dateStyle = AodDateStyle.MINIMAL,
                showBatteryPercentage = true,
                showBatteryIcon = false,
                batteryStyle = AodBatteryStyle.PERCENT_ONLY,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = false,
                    position = AodEdgeGlowPosition.FULL
                )
            )
        ),

        // 9. EDGE GLOW MATRIX
        AodThemeDefinition(
            theme = AodTheme.EDGE_GLOW,
            titleKey = "aod_theme_edge_glow",
            subtitle = "Perimeter Lightwave Boundary",
            description = "Intense breathing light boundary wrapping the screen edges, highlighting minimalist floating stats.",
            categoryTag = "Illumination",
            signatureColor = Color(0xFF38BDF8),
            accentColors = listOf(Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0369A1)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.EDGE_GLOW,
                clockType = AodClockType.DIGITAL,
                clockStyle = AodClockStyle.MINIMAL,
                clockSize = AodClockSize.MEDIUM,
                clockWeight = AodClockWeight.REGULAR,
                clockPosition = AodClockPosition.CENTER,
                clockColor = RgbColor(56, 189, 248),
                isAutoColor = false,
                is24Hour = true,
                showDate = true,
                dateFormat = "EEE, MMM d",
                datePosition = AodDatePosition.ABOVE_CLOCK,
                dateStyle = AodDateStyle.UPPERCASE,
                showBatteryPercentage = true,
                showBatteryIcon = true,
                batteryStyle = AodBatteryStyle.MINIMAL_BAR,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = true,
                    position = AodEdgeGlowPosition.FULL,
                    color = RgbColor(56, 189, 248),
                    isRgbCycle = false,
                    brightness = 1.0f,
                    thickness = 10.0f,
                    glowRadius = 26.0f,
                    speed = 1.2f,
                    animation = AodEdgeAnimation.BREATHING
                )
            )
        ),

        // 10. MINIMAL AMBIENT
        AodThemeDefinition(
            theme = AodTheme.AMBIENT,
            titleKey = "aod_theme_ambient",
            subtitle = "Celestial Starlight Nebula",
            description = "Subtle cosmic starlight particles with soothing lavender luminescence and bottom horizon edge glow.",
            categoryTag = "Cosmic",
            signatureColor = Color(0xFFA78BFA),
            accentColors = listOf(Color(0xFFA78BFA), Color(0xFF7C3AED), Color(0xFF2E1065)),
            defaultPreset = AodConfig(
                isEnabled = true,
                theme = AodTheme.AMBIENT,
                clockType = AodClockType.DIGITAL,
                clockStyle = AodClockStyle.LIQUID_GLASS,
                clockSize = AodClockSize.LARGE,
                clockWeight = AodClockWeight.LIGHT,
                clockPosition = AodClockPosition.TOP,
                clockColor = RgbColor(167, 139, 250),
                isAutoColor = false,
                is24Hour = true,
                showDate = true,
                dateFormat = "EEE, MMM d",
                datePosition = AodDatePosition.BELOW_CLOCK,
                dateStyle = AodDateStyle.PILL,
                clockOpacity = 0.88f,
                showBatteryPercentage = true,
                showBatteryIcon = true,
                batteryStyle = AodBatteryStyle.CIRCULAR_RING,
                edgeGlow = AodEdgeGlowConfig(
                    isEnabled = true,
                    position = AodEdgeGlowPosition.BOTTOM,
                    color = RgbColor(167, 139, 250),
                    isRgbCycle = false,
                    brightness = 0.80f,
                    thickness = 6.0f,
                    glowRadius = 18.0f,
                    speed = 0.9f,
                    animation = AodEdgeAnimation.BREATHING
                )
            )
        )
    )

    fun getThemeDefinition(theme: AodTheme): AodThemeDefinition {
        return THEME_DEFINITIONS.find { it.theme == theme } ?: THEME_DEFINITIONS.first()
    }

    /**
     * Applies a built-in theme's preset values onto an existing AodConfig,
     * preserving master enable state.
     */
    fun applyThemePreset(theme: AodTheme, current: AodConfig): AodConfig {
        val def = getThemeDefinition(theme)
        return def.defaultPreset.copy(
            isEnabled = current.isEnabled
        )
    }
}
