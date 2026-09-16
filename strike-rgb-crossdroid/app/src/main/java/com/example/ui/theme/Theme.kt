package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.RgbColor

fun buildPixelColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    ledColor: RgbColor
): ColorScheme {
    return if (darkTheme) {
        if (dynamicColor) {
            val primary = dynamicDarkColorForLed(ledColor)
            val container = primary.copy(alpha = 0.25f)
            darkColorScheme(
                primary = primary,
                onPrimary = Color(0xFF0F141C),
                primaryContainer = container,
                onPrimaryContainer = primary,
                secondary = primary.copy(alpha = 0.75f),
                onSecondary = Color(0xFF1B1D22),
                secondaryContainer = Color(0xFF262830),
                onSecondaryContainer = Color(0xFFD8DAE0),
                surface = PixelDefaultDarkSurface,
                surfaceContainer = PixelDefaultDarkSurfaceContainer,
                surfaceContainerHigh = PixelDefaultDarkSurfaceContainerHigh,
                onSurface = PixelDefaultDarkOnSurface,
                onSurfaceVariant = Color(0xFFC3C6D0),
                outline = PixelDefaultDarkOutline,
                outlineVariant = Color(0xFF383A42),
                error = PixelDefaultDarkError,
                onError = Color(0xFF690005)
            )
        } else {
            darkColorScheme(
                primary = PixelDefaultDarkPrimary,
                onPrimary = PixelDefaultDarkOnPrimary,
                primaryContainer = PixelDefaultDarkPrimaryContainer,
                onPrimaryContainer = PixelDefaultDarkOnPrimaryContainer,
                secondary = PixelDefaultDarkSecondary,
                onSecondary = Color(0xFF1B1D22),
                secondaryContainer = Color(0xFF262830),
                onSecondaryContainer = Color(0xFFD8DAE0),
                surface = PixelDefaultDarkSurface,
                surfaceContainer = PixelDefaultDarkSurfaceContainer,
                surfaceContainerHigh = PixelDefaultDarkSurfaceContainerHigh,
                onSurface = PixelDefaultDarkOnSurface,
                onSurfaceVariant = Color(0xFFC3C6D0),
                outline = PixelDefaultDarkOutline,
                outlineVariant = Color(0xFF383A42),
                error = PixelDefaultDarkError,
                onError = Color(0xFF690005)
            )
        }
    } else {
        if (dynamicColor) {
            val primary = dynamicLightColorForLed(ledColor)
            val container = primary.copy(alpha = 0.16f)
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = container,
                onPrimaryContainer = primary,
                secondary = primary.copy(alpha = 0.85f),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE4E7EE),
                onSecondaryContainer = Color(0xFF191C20),
                surface = PixelDefaultLightSurface,
                surfaceContainer = PixelDefaultLightSurfaceContainer,
                surfaceContainerHigh = PixelDefaultLightSurfaceContainerHigh,
                onSurface = PixelDefaultLightOnSurface,
                onSurfaceVariant = Color(0xFF43474E),
                outline = PixelDefaultLightOutline,
                outlineVariant = Color(0xFFC4C7D0),
                error = PixelDefaultLightError,
                onError = Color.White
            )
        } else {
            lightColorScheme(
                primary = PixelDefaultLightPrimary,
                onPrimary = PixelDefaultLightOnPrimary,
                primaryContainer = PixelDefaultLightPrimaryContainer,
                onPrimaryContainer = PixelDefaultLightOnPrimaryContainer,
                secondary = PixelDefaultLightSecondary,
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFE4E7EE),
                onSecondaryContainer = Color(0xFF191C20),
                surface = PixelDefaultLightSurface,
                surfaceContainer = PixelDefaultLightSurfaceContainer,
                surfaceContainerHigh = PixelDefaultLightSurfaceContainerHigh,
                onSurface = PixelDefaultLightOnSurface,
                onSurfaceVariant = Color(0xFF43474E),
                outline = PixelDefaultLightOutline,
                outlineVariant = Color(0xFFC4C7D0),
                error = PixelDefaultLightError,
                onError = Color.White
            )
        }
    }
}

@Composable
fun KoukouRgbTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    ledColor: RgbColor = RgbColor.Blue,
    content: @Composable () -> Unit,
) {
    val colorScheme = buildPixelColorScheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        ledColor = ledColor
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun StrikeRgbTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    ledColor: RgbColor = RgbColor.Blue,
    content: @Composable () -> Unit,
) {
    KoukouRgbTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        ledColor = ledColor,
        content = content
    )
}

// Keep MyApplicationTheme alias so tests/legacy references continue to work seamlessly
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    KoukouRgbTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        ledColor = RgbColor.Blue,
        content = content
    )
}
