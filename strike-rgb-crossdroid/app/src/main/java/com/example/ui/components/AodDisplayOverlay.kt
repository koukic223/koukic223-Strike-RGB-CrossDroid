package com.example.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AodClockPosition
import com.example.model.AodClockSize
import com.example.model.AodClockStyle
import com.example.model.AodClockType
import com.example.model.AodClockWeight
import com.example.model.AodConfig
import com.example.model.AodEdgeGlowPosition
import com.example.model.AodNotificationStyle
import com.example.model.AodTheme
import com.example.model.QualityLevel
import com.example.notification.AodDisplayState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Flagship ULTIMATE NEWBIE LIQUID GLASS Always-On Display (AOD) System.
 *
 * Implements:
 * 1. Physical Glass Material: Translucent frosted glass, refraction, Fresnel highlights, specular glints.
 * 2. iOS 26-Inspired Liquid Glass Clock: Monumental typography, translucent glass digits, top gloss glare.
 * 3. Dynamic Glass Clock: Inherits subtle tint from wallpaper/background & notification accents.
 * 4. Fluid Digit Morphing: Smooth animated transitions with spring physics on time changes.
 * 5. Full 9-Layer Glass Architecture: Background -> Dark Tint -> Optical Blur -> Glass Clock ->
 *    Highlights/Refraction -> Date Pill -> Battery Capsule -> Notification Badges -> Edge Glow.
 * 6. Quality Preset Scaling: Automatic adaptation for Low, Balanced, and High hardware tiers.
 */
@Composable
fun AodDisplayOverlay(
    aodState: AodDisplayState,
    aodConfig: AodConfig = AodConfig(),
    qualityLevel: QualityLevel = QualityLevel.BALANCED,
    customBackgroundBitmap: ImageBitmap? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = aodState.isVisible && aodConfig.isEnabled,
        enter = fadeIn(tween(350)),
        exit = fadeOut(tween(250)),
        modifier = modifier.fillMaxSize()
    ) {
        val now = Date()
        val currentTime = SimpleDateFormat(if (aodConfig.is24Hour) "HH:mm" else "hh:mm", Locale.getDefault()).format(now)
        val amPm = if (!aodConfig.is24Hour) SimpleDateFormat("a", Locale.getDefault()).format(now).uppercase() else ""
        val currentDate = SimpleDateFormat(aodConfig.dateFormat, Locale.getDefault()).format(now)

        // Accent color calculation
        val accentColor = if (aodConfig.useAppColorForNotification && aodState.appName.isNotEmpty()) {
            aodState.color.toComposeColor()
        } else if (!aodConfig.isAutoColor) {
            aodConfig.clockColor.toComposeColor()
        } else {
            // Signature liquid glass cerulean teal matching photo
            Color(0xFF00A3C4)
        }

        // Animated breathing & chromatic cycles
        val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_aod_fx")
        val pulseAlpha by infiniteTransition.animateFloat(
            initialValue = 0.40f,
            targetValue = 0.95f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_alpha"
        )
        val rainbowAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rainbow_angle"
        )
        val specularShift by infiniteTransition.animateFloat(
            initialValue = -0.2f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "specular_shift"
        )

        // Clock typography metrics
        val clockFontSize = when (aodConfig.clockSize) {
            AodClockSize.SMALL -> 56.sp
            AodClockSize.MEDIUM -> 76.sp
            AodClockSize.LARGE -> 96.sp
            AodClockSize.EXTRA_LARGE -> 120.sp
        }

        val clockFontWeight = when (aodConfig.clockWeight) {
            AodClockWeight.THIN -> FontWeight.Thin
            AodClockWeight.LIGHT -> FontWeight.Light
            AodClockWeight.REGULAR -> FontWeight.Normal
            AodClockWeight.SEMI_BOLD -> FontWeight.SemiBold
            AodClockWeight.BOLD -> FontWeight.Bold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            // LAYER 1: Background (Custom Wallpaper or Pure OLED Black)
            if (customBackgroundBitmap != null && aodConfig.theme == AodTheme.LIQUID_GLASS) {
                val blurDp = (aodConfig.backgroundBlur * 40f).dp
                val bgModifier = if (qualityLevel != QualityLevel.LOW && blurDp > 0.dp) {
                    Modifier
                        .fillMaxSize()
                        .blur(blurDp)
                } else {
                    Modifier.fillMaxSize()
                }

                Image(
                    bitmap = customBackgroundBitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = bgModifier
                )
            }

            // LAYER 2: Dark Ambient Overlay
            if (customBackgroundBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = aodConfig.backgroundDarkOverlay.coerceIn(0.2f, 0.95f)))
                )
            }

            // LAYER 3: Theme-Specific Optical Atmospheres (Liquid Glass / Aurora / Neon / RGB)
            when (aodConfig.theme) {
                AodTheme.LIQUID_GLASS -> {
                    // Organic wavy liquid silk folds inspired by the reference photo (when no custom image)
                    if (customBackgroundBitmap == null) {
                        LiquidWavySilkBackdrop()
                    }
                    // Subtle chromatic caustic glow reacting to accent
                    if (qualityLevel != QualityLevel.LOW) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.09f * aodConfig.clockGlow),
                                            Color(0x06FFFFFF),
                                            Color.Transparent
                                        ),
                                        center = Offset(Float.POSITIVE_INFINITY, 0f),
                                        radius = 1200f
                                    )
                                )
                        )
                    }
                }
                AodTheme.AURORA -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x2410B981),
                                        Color(0x1C06B6D4),
                                        Color(0x248B5CF6),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                AodTheme.NEON -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x1E00F5FF),
                                        Color(0x12FF007F),
                                        Color.Transparent
                                    ),
                                    radius = 1000f
                                )
                            )
                    )
                }
                AodTheme.RGB -> {
                    val rainbowHsl = Color.hsl(rainbowAngle, 0.70f, 0.50f)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        rainbowHsl.copy(alpha = 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                else -> { /* Pure OLED black */ }
            }

            // LAYER 4 & 5: Central Layout (Clock, Date, Battery, Notifications)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = when (aodConfig.clockPosition) {
                    AodClockPosition.TOP -> Arrangement.Top
                    AodClockPosition.CENTER -> Arrangement.Center
                }
            ) {
                // TOP SPACING if Top aligned
                if (aodConfig.clockPosition == AodClockPosition.TOP) {
                    Spacer(modifier = Modifier.height(36.dp))
                }

                // Date display logic: For Liquid Glass style (matching photo), default to top above clock
                val showDateAbove = aodConfig.showDate && (
                    aodConfig.datePosition == com.example.model.AodDatePosition.ABOVE_CLOCK ||
                    aodConfig.clockStyle == AodClockStyle.LIQUID_GLASS
                )

                // DATE ABOVE CLOCK
                if (showDateAbove) {
                    LiquidGlassDatePill(
                        dateString = currentDate,
                        accentColor = accentColor,
                        isLiquidGlass = aodConfig.theme == AodTheme.LIQUID_GLASS || aodConfig.clockStyle == AodClockStyle.LIQUID_GLASS,
                        qualityLevel = qualityLevel
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // CLOCK DISPLAY
                if (aodConfig.clockType == AodClockType.ANALOG || aodConfig.clockStyle == AodClockStyle.ANALOG) {
                    AodAnalogClockView(
                        now = now,
                        accentColor = accentColor,
                        theme = aodConfig.theme,
                        qualityLevel = qualityLevel,
                        modifier = Modifier.size(200.dp)
                    )
                } else {
                    // DIGITAL / LIQUID GLASS / MINIMAL / PIXEL CLOCK
                    when (aodConfig.clockStyle) {
                        AodClockStyle.LIQUID_GLASS -> {
                            TubularLiquidGlassClock(
                                timeString = currentTime,
                                amPm = amPm,
                                accentColor = accentColor,
                                aodConfig = aodConfig,
                                qualityLevel = qualityLevel
                            )
                        }
                        AodClockStyle.MINIMAL -> {
                            MinimalClock(
                                timeString = currentTime,
                                amPm = amPm,
                                fontSize = clockFontSize,
                                fontWeight = clockFontWeight
                            )
                        }
                        AodClockStyle.PIXEL -> {
                            PixelStyleClock(
                                timeString = currentTime,
                                amPm = amPm,
                                fontSize = clockFontSize,
                                accentColor = accentColor
                            )
                        }
                        AodClockStyle.DIGITAL -> {
                            DigitalHorizonClock(
                                timeString = currentTime,
                                amPm = amPm,
                                fontSize = clockFontSize,
                                accentColor = accentColor,
                                aodConfig = aodConfig
                            )
                        }
                        AodClockStyle.ANALOG -> {
                            AodAnalogClockView(
                                now = now,
                                accentColor = accentColor,
                                theme = aodConfig.theme,
                                qualityLevel = qualityLevel,
                                modifier = Modifier.size(200.dp)
                            )
                        }
                    }
                }

                // DATE BELOW CLOCK (if not already shown above)
                if (aodConfig.showDate && !showDateAbove) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LiquidGlassDatePill(
                        dateString = currentDate,
                        accentColor = accentColor,
                        isLiquidGlass = aodConfig.theme == AodTheme.LIQUID_GLASS,
                        qualityLevel = qualityLevel
                    )
                }

                // BATTERY TELEMETRY (Liquid Glass Capsule)
                if (aodConfig.showBatteryPercentage || aodConfig.showBatteryIcon) {
                    Spacer(modifier = Modifier.height(18.dp))
                    LiquidGlassBatteryIndicator(
                        percent = 86,
                        isCharging = true,
                        showIcon = aodConfig.showBatteryIcon,
                        showPercent = aodConfig.showBatteryPercentage,
                        isLiquidGlass = aodConfig.theme == AodTheme.LIQUID_GLASS,
                        qualityLevel = qualityLevel
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // NOTIFICATION CONTAINER (Translucent Liquid Glass Badge)
                if (aodConfig.showNotificationIcons && aodState.appName.isNotEmpty()) {
                    LiquidGlassNotificationBadge(
                        aodState = aodState,
                        accentColor = accentColor,
                        qualityLevel = qualityLevel
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Subtle minimal dismiss hint
                Text(
                    text = "Tap screen to wake",
                    color = Color.White.copy(alpha = 0.28f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // LAYER 9: Liquid Glass Edge Glow (Perimeter or Directed)
            if (aodConfig.edgeGlow.isEnabled || aodConfig.theme == AodTheme.EDGE_GLOW) {
                val edgeColor = if (aodConfig.edgeGlow.isRgbCycle) {
                    Color.hsl(rainbowAngle, 0.85f, 0.55f)
                } else {
                    accentColor
                }
                LiquidGlassEdgeGlow(
                    position = aodConfig.edgeGlow.position,
                    color = edgeColor,
                    pulseAlpha = pulseAlpha,
                    brightness = aodConfig.edgeGlow.brightness,
                    thicknessDp = aodConfig.edgeGlow.thickness,
                    qualityLevel = qualityLevel
                )
            }
        }
    }
}

/**
 * The iOS 26 Liquid Glass centerpiece clock with optical digit morphing.
 *
 * Digits are rendered with:
 * - Translucent physical glass gradient fill
 * - Dynamic background/accent tint absorption
 * - Specular highlight reflection (3D curved lens glare)
 * - Frosted Fresnel rims and soft inner dispersion
 * - Fluid spring transitions on time change
 */
@Composable
private fun LiquidGlassMorphingClock(
    timeString: String,
    amPm: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    accentColor: Color,
    aodConfig: AodConfig,
    qualityLevel: QualityLevel,
    specularShift: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        timeString.forEach { char ->
            if (char == ':') {
                LiquidGlassColon(
                    fontSize = fontSize,
                    accentColor = accentColor,
                    aodConfig = aodConfig
                )
            } else {
                LiquidGlassDigit(
                    digit = char,
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    accentColor = accentColor,
                    aodConfig = aodConfig,
                    qualityLevel = qualityLevel,
                    specularShift = specularShift
                )
            }
        }

        if (amPm.isNotEmpty()) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = amPm,
                style = TextStyle(
                    fontSize = (fontSize.value * 0.28f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.align(Alignment.Bottom).padding(bottom = (fontSize.value * 0.15f).dp)
            )
        }
    }
}

/**
 * Individual physical Liquid Glass digit with animated fluid morphing transition.
 */
@Composable
private fun LiquidGlassDigit(
    digit: Char,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    accentColor: Color,
    aodConfig: AodConfig,
    qualityLevel: QualityLevel,
    specularShift: Float
) {
    AnimatedContent(
        targetState = digit,
        transitionSpec = {
            (slideInVertically(spring(stiffness = Spring.StiffnessMediumLow)) { it / 3 } +
                    scaleIn(initialScale = 0.88f) +
                    fadeIn(tween(220)))
                .togetherWith(
                    slideOutVertically(tween(180)) { -it / 3 } +
                            scaleOut(targetScale = 1.08f) +
                            fadeOut(tween(180))
                )
        },
        label = "digit_morph_$digit"
    ) { targetDigit ->
        val opacity = aodConfig.clockOpacity.coerceIn(0.2f, 1f)
        val gloss = aodConfig.clockGloss.coerceIn(0.1f, 1f)
        val glow = aodConfig.clockGlow.coerceIn(0.1f, 1f)
        val refraction = aodConfig.clockRefraction.coerceIn(0.1f, 1f)

        // Dynamic glass color tint:
        // Crystalline ice white top, background/accent translucent mid-fill, deep refraction base
        val glassGradient = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f * opacity),
                accentColor.copy(alpha = 0.40f * refraction * opacity),
                Color(0x33FFFFFF).copy(alpha = 0.20f * opacity)
            )
        )

        val shadowAmbient = accentColor.copy(alpha = 0.35f * glow)

        Box(contentAlignment = Alignment.Center) {
            // Ambient Optical Refraction Glow (Behind the glass digit)
            if (qualityLevel != QualityLevel.LOW && aodConfig.glowEffect) {
                Text(
                    text = targetDigit.toString(),
                    style = TextStyle(
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        fontFamily = FontFamily.SansSerif,
                        color = shadowAmbient,
                        shadow = Shadow(
                            color = shadowAmbient,
                            offset = Offset(0f, 4f),
                            blurRadius = (32f * glow)
                        )
                    )
                )
            }

            // The Liquid Glass Digit Body
            Text(
                text = targetDigit.toString(),
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    fontFamily = FontFamily.SansSerif,
                    brush = glassGradient,
                    letterSpacing = 1.sp,
                    shadow = if (qualityLevel == QualityLevel.HIGH) {
                        Shadow(
                            color = Color.Black.copy(alpha = 0.70f),
                            offset = Offset(0f, 6f),
                            blurRadius = 14f
                        )
                    } else null
                )
            )

            // High-Gloss Specular Top Glare (Simulating 3D optical lens curve)
            if (qualityLevel != QualityLevel.LOW) {
                Text(
                    text = targetDigit.toString(),
                    style = TextStyle(
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        fontFamily = FontFamily.SansSerif,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.70f * gloss),
                                Color.White.copy(alpha = 0.15f * gloss),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(0f, fontSize.value * 1.5f)
                        ),
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

/**
 * Liquid Glass animated pulsating colon divider.
 */
@Composable
private fun LiquidGlassColon(
    fontSize: androidx.compose.ui.unit.TextUnit,
    accentColor: Color,
    aodConfig: AodConfig
) {
    val opacity = aodConfig.clockOpacity.coerceIn(0.3f, 1f)
    Text(
        text = ":",
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Light,
            brush = Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.90f * opacity),
                    accentColor.copy(alpha = 0.45f * opacity),
                    Color.White.copy(alpha = 0.25f * opacity)
                )
            ),
            letterSpacing = 0.sp
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

/**
 * Minimalist typographic clock.
 */
@Composable
private fun MinimalClock(
    timeString: String,
    amPm: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight
) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = timeString,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = Color.White.copy(alpha = 0.92f),
                letterSpacing = 2.sp
            )
        )
        if (amPm.isNotEmpty()) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = amPm,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.60f)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

/**
 * Google Pixel styled stacked/horizontal clock.
 */
@Composable
private fun PixelStyleClock(
    timeString: String,
    amPm: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    accentColor: Color
) {
    Text(
        text = timeString,
        style = TextStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = accentColor,
            letterSpacing = 2.sp,
            shadow = Shadow(
                color = accentColor.copy(alpha = 0.5f),
                offset = Offset(0f, 2f),
                blurRadius = 16f
            )
        )
    )
}

/**
 * Digital Horizon futuristic segmented clock layout.
 */
@Composable
private fun DigitalHorizonClock(
    timeString: String,
    amPm: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    accentColor: Color,
    aodConfig: AodConfig
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x22111827))
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(horizontal = 28.dp, vertical = 14.dp)
    ) {
        Text(
            text = timeString,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF00F5FF),
                letterSpacing = 4.sp,
                shadow = Shadow(
                    color = Color(0xFF00F5FF).copy(alpha = 0.6f),
                    offset = Offset(0f, 0f),
                    blurRadius = 20f
                )
            )
        )
    }
}

/**
 * Translucent Liquid Glass floating header for the Date (matching photo aesthetic).
 */
@Composable
private fun LiquidGlassDatePill(
    dateString: String,
    accentColor: Color,
    isLiquidGlass: Boolean,
    qualityLevel: QualityLevel
) {
    if (isLiquidGlass) {
        val cyanColor = Color(0xFF38BDF8)
        Text(
            text = dateString,
            style = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = cyanColor,
                letterSpacing = 0.8.sp,
                shadow = if (qualityLevel != QualityLevel.LOW) {
                    Shadow(
                        color = cyanColor.copy(alpha = 0.40f),
                        offset = Offset(0f, 2f),
                        blurRadius = 12f
                    )
                } else null
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    } else {
        Text(
            text = dateString,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.70f),
                letterSpacing = 0.5.sp
            )
        )
    }
}

/**
 * Liquid Glass Battery Indicator:
 * - Translucent physical glass capsule outline
 * - Proportional translucent fill
 * - Dynamic color: normal = cool slate/white, low (<20%) = warm amber red, charging = breathing emerald
 */
@Composable
private fun LiquidGlassBatteryIndicator(
    percent: Int,
    isCharging: Boolean,
    showIcon: Boolean,
    showPercent: Boolean,
    isLiquidGlass: Boolean,
    qualityLevel: QualityLevel
) {
    val infiniteTransition = rememberInfiniteTransition(label = "battery_fx")
    val chargePulse by infiniteTransition.animateFloat(
        initialValue = 0.70f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "charge_pulse"
    )

    // Color logic
    val batteryColor = when {
        isCharging -> Color(0xFF34D399).copy(alpha = if (qualityLevel != QualityLevel.LOW) chargePulse else 0.90f)
        percent <= 20 -> Color(0xFFF87171) // Warm amber red for low battery
        else -> Color(0xFFE2E8F0)          // Cool neutral physical glass
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (isLiquidGlass) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0x2E1E293B),
                            Color(0x160F172A)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.08f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.40f),
                        Color.White.copy(alpha = 0.10f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        if (showIcon) {
            // Glass Capsule Battery Drawing
            Canvas(modifier = Modifier.size(width = 24.dp, height = 12.dp)) {
                val corner = 3.dp.toPx()
                val capWidth = 2.dp.toPx()
                val bodyWidth = size.width - capWidth - 2.dp.toPx()
                val bodyHeight = size.height

                // Capsule Body Outline (Frosted glass rim)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.45f),
                    size = Size(bodyWidth, bodyHeight),
                    cornerRadius = CornerRadius(corner, corner),
                    style = Stroke(width = 1.2.dp.toPx())
                )

                // Battery Terminal Cap (+)
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.45f),
                    topLeft = Offset(bodyWidth + 1.dp.toPx(), bodyHeight * 0.28f),
                    size = Size(capWidth, bodyHeight * 0.44f),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )

                // Translucent Inner Fill
                val fillWidth = ((bodyWidth - 4.dp.toPx()) * (percent / 100f)).coerceAtLeast(0f)
                if (fillWidth > 0) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                batteryColor.copy(alpha = 0.85f),
                                batteryColor.copy(alpha = 0.50f)
                            )
                        ),
                        topLeft = Offset(2.dp.toPx(), 2.dp.toPx()),
                        size = Size(fillWidth, bodyHeight - 4.dp.toPx()),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            if (isCharging) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Charging",
                    tint = batteryColor,
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }

        if (showPercent) {
            Text(
                text = "$percent%",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.88f),
                    letterSpacing = 0.3.sp
                )
            )
        }
    }
}

/**
 * Liquid Glass Notification Badge:
 * Frosted translucent rounded container with app-colored subtle glow and high-contrast typography.
 */
@Composable
private fun LiquidGlassNotificationBadge(
    aodState: AodDisplayState,
    accentColor: Color,
    qualityLevel: QualityLevel
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = accentColor.copy(alpha = 0.35f),
                    spotColor = accentColor.copy(alpha = 0.55f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0x40223046),
                            Color(0x20141C2B)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.65f),
                            accentColor.copy(alpha = 0.40f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                .clip(RoundedCornerShape(18.dp))
        ) {
            // Subtle internal app glow
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = when (aodState.style) {
                            AodNotificationStyle.COLOR -> accentColor.copy(alpha = 0.85f)
                            else -> accentColor.copy(alpha = 0.20f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification",
                    tint = if (aodState.style == AodNotificationStyle.COLOR) Color.White else accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = aodState.appName,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
                letterSpacing = 0.5.sp
            )
        )

        if (aodState.title.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = aodState.title,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.90f)
                )
            )
            if (aodState.message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = aodState.message,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.65f)
                    ),
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

/**
 * Liquid Glass Edge Glow supporting: Full screen perimeter, Top, Bottom, Left, or Right.
 */
@Composable
private fun LiquidGlassEdgeGlow(
    position: AodEdgeGlowPosition,
    color: Color,
    pulseAlpha: Float,
    brightness: Float,
    thicknessDp: Float,
    qualityLevel: QualityLevel
) {
    val glowColor = color.copy(alpha = (pulseAlpha * brightness).coerceIn(0.1f, 1f))

    when (position) {
        AodEdgeGlowPosition.FULL -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .border(
                        width = thicknessDp.dp,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor,
                                glowColor.copy(alpha = 0.20f),
                                Color.Transparent
                            ),
                            radius = 950f
                        ),
                        shape = RoundedCornerShape(32.dp)
                    )
            )
        }
        AodEdgeGlowPosition.TOP -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((thicknessDp * 3).dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(glowColor, Color.Transparent)
                        )
                    )
            )
        }
        AodEdgeGlowPosition.BOTTOM -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((thicknessDp * 3).dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, glowColor)
                        )
                    )
            )
        }
        AodEdgeGlowPosition.LEFT -> {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((thicknessDp * 3).dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(glowColor, Color.Transparent)
                        )
                    )
            )
        }
        AodEdgeGlowPosition.RIGHT -> {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((thicknessDp * 3).dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, glowColor)
                        )
                    )
            )
        }
    }
}

/**
 * Precision Analog Clock Face with luminous hands and smooth continuous sweep.
 */
@Composable
private fun AodAnalogClockView(
    now: Date,
    accentColor: Color,
    theme: AodTheme,
    qualityLevel: QualityLevel,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance().apply { time = now }
    val hours = cal.get(Calendar.HOUR)
    val minutes = cal.get(Calendar.MINUTE)
    val seconds = cal.get(Calendar.SECOND)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 10f

        // Frosted Glass Rim
        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Glass Dial Face Sheen
        if (qualityLevel != QualityLevel.LOW) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.12f),
                        Color(0x08FFFFFF),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius - 2f,
                center = center
            )
        }

        // 12 Hour Ticks
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val isMajor = i % 3 == 0
            val tickLen = if (isMajor) 14f else 8f
            val startDist = radius - tickLen
            val start = Offset(
                center.x + (startDist * cos(angle)).toFloat(),
                center.y + (startDist * sin(angle)).toFloat()
            )
            val end = Offset(
                center.x + (radius * cos(angle)).toFloat(),
                center.y + (radius * sin(angle)).toFloat()
            )
            drawLine(
                color = if (isMajor) accentColor.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.35f),
                start = start,
                end = end,
                strokeWidth = if (isMajor) 2.5f else 1.5f,
                cap = StrokeCap.Round
            )
        }

        // Hour Hand
        val hourAngle = Math.toRadians(((hours + minutes / 60f) * 30 - 90).toDouble())
        val hourLen = radius * 0.52f
        drawLine(
            color = Color.White.copy(alpha = 0.90f),
            start = center,
            end = Offset(
                center.x + (hourLen * cos(hourAngle)).toFloat(),
                center.y + (hourLen * sin(hourAngle)).toFloat()
            ),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Minute Hand
        val minAngle = Math.toRadians(((minutes + seconds / 60f) * 6 - 90).toDouble())
        val minLen = radius * 0.76f
        drawLine(
            color = Color.White.copy(alpha = 0.90f),
            start = center,
            end = Offset(
                center.x + (minLen * cos(minAngle)).toFloat(),
                center.y + (minLen * sin(minAngle)).toFloat()
            ),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Second Hand (Sweeping accent)
        val secAngle = Math.toRadians((seconds * 6 - 90).toDouble())
        val secLen = radius * 0.86f
        drawLine(
            color = accentColor,
            start = center,
            end = Offset(
                center.x + (secLen * cos(secAngle)).toFloat(),
                center.y + (secLen * sin(secAngle)).toFloat()
            ),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center Glass Pivot
        drawCircle(
            color = Color.White,
            radius = 4.dp.toPx(),
            center = center
        )
        drawCircle(
            color = accentColor,
            radius = 2.dp.toPx(),
            center = center
        )
    }
}
