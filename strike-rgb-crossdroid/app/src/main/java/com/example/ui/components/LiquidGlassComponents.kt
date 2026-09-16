package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.localization.Strings
import com.example.model.AppLanguage
import com.example.model.AppThemeMode
import com.example.model.QualityLevel
import com.example.model.UiTheme
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Diamond

/**
 * Liquid Glass styling palette & shader emulation inspired by iOS 26 Liquid Glass
 * (GetStream/awesome-liquid-glass: CustomGlassEffect, LiquidGlassHContainer, LiquidGlassJello).
 *
 * Designed to run smoothly on low-power devices (e.g., BQ Aquaris A4.5 / ARM32 / Android Go)
 * using lightweight linear specular gradients, translucent overlays, and spring physics.
 */
object LiquidGlassTheme {

    @Composable
    fun surfaceBrush(isDark: Boolean = isSystemInDarkTheme(), hasBackground: Boolean = false): Brush {
        return if (isDark) {
            // iOS 26 Liquid Glass Authentic Translucent Glass (~16% - 28% opacity)
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x38222E42), // 22% crystalline slate top
                    Color(0x1F141C2B)  // 12% transparent bottom
                )
            )
        } else {
            // iOS 26 Liquid Glass Crystalline Light (~20% - 34% opacity)
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x52FFFFFF), // 32% crystalline white top
                    Color(0x2EEDF2F7)  // 18% frosted cool tint bottom
                )
            )
        }
    }

    @Composable
    fun borderBrush(isDark: Boolean = isSystemInDarkTheme()): Brush {
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x4DFFFFFF), // 30% white specular rim
                    Color(0x1AFFFFFF), // 10% diffuse side
                    Color(0x0AFFFFFF)  // 4% bottom refraction
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xB3FFFFFF), // 70% crisp glint
                    Color(0x33CBD5E1), // 20% specular sheen
                    Color(0x1494A3B8)  // 8% subtle rim
                )
            )
        }
    }

    @Composable
    fun highlightPillBrush(isDark: Boolean = isSystemInDarkTheme()): Brush {
        val primary = MaterialTheme.colorScheme.primary
        return if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    primary.copy(alpha = 0.32f),
                    primary.copy(alpha = 0.16f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    primary.copy(alpha = 0.22f),
                    primary.copy(alpha = 0.10f)
                )
            )
        }
    }
}

// =========================================================================================
// BASE COMPOSE MODIFIERS - PHYSICAL GLASS OPTICS (REFRACTION, FROST, FRESNEL EDGES, SPECULAR)
// =========================================================================================

/**
 * Simulates physical frosted glass diffusion, surface microscopic roughness, and translucent opacity.
 *
 * @param opacity Base translucency opacity (0.1f to 1.0f)
 * @param isDark Whether the surface is rendering against a dark canvas
 * @param tint Optional ambient tint color to wash through the frosted medium
 */
fun Modifier.liquidGlassFrost(
    opacity: Float = 0.25f,
    isDark: Boolean = false,
    tint: Color = Color.Unspecified
): Modifier = this.drawBehind {
    val topTint = if (tint != Color.Unspecified) tint.copy(alpha = 0.18f * opacity) else Color.Transparent
    val frostColors = if (isDark) {
        listOf(
            Color(0x40222E42).copy(alpha = (0.28f * opacity).coerceIn(0.05f, 0.95f)),
            topTint,
            Color(0x1F141C2B).copy(alpha = (0.16f * opacity).coerceIn(0.03f, 0.90f))
        )
    } else {
        listOf(
            Color(0x60FFFFFF).copy(alpha = (0.35f * opacity).coerceIn(0.08f, 0.98f)),
            topTint,
            Color(0x2EEDF2F7).copy(alpha = (0.20f * opacity).coerceIn(0.04f, 0.90f))
        )
    }
    drawRect(
        brush = Brush.verticalGradient(frostColors)
    )
}

/**
 * Simulates optical refraction, chromatic dispersion, and subtle caustics inside the physical glass volume.
 *
 * @param refractionFactor Strength of refraction bending and caustic pool intensity (0f to 1f)
 * @param chromaticDispersion Micro-prism wavelength dispersion offset (0f to 1f)
 * @param tintColor Dynamic tint color absorbed by the glass medium (e.g. from background or accent)
 */
fun Modifier.liquidGlassRefraction(
    refractionFactor: Float = 0.45f,
    chromaticDispersion: Float = 0.25f,
    tintColor: Color = Color.Unspecified
): Modifier = this.drawBehind {
    val effectiveTint = if (tintColor != Color.Unspecified) tintColor else Color(0xFF00F5FF)

    if (refractionFactor > 0.02f) {
        // Incident refractive caustic pool (upper quadrant)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    effectiveTint.copy(alpha = 0.14f * refractionFactor),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.25f, size.height * 0.25f),
                radius = size.minDimension * 0.85f
            ),
            radius = size.minDimension * 0.85f,
            center = Offset(size.width * 0.25f, size.height * 0.25f)
        )

        // Opposing chromatic dispersion flare (warm prismatic secondary refraction)
        if (chromaticDispersion > 0.05f) {
            val dispersionColor = Color(0xFFFFB74D)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        dispersionColor.copy(alpha = 0.08f * chromaticDispersion * refractionFactor),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.80f, size.height * 0.80f),
                    radius = size.minDimension * 0.70f
                ),
                radius = size.minDimension * 0.70f,
                center = Offset(size.width * 0.80f, size.height * 0.80f)
            )
        }
    }
}

/**
 * Simulates Augustin-Jean Fresnel reflection optics along glancing boundaries.
 * At glancing angles (the perimeter of the glass), reflectance increases sharply towards 100%,
 * producing a luminous directional rim light with incident specular glint and softer exit refraction.
 *
 * @param edgeWidth Width of the refractive boundary rim (default 1.dp)
 * @param rimAlpha Intensity of the incident specular rim reflection (0f to 1f)
 * @param accentTint Color tint reflected along the exit edge
 * @param shape Shape of the glass element
 * @param isDark Whether surface is in dark mode
 */
fun Modifier.fresnelEdge(
    edgeWidth: Dp = 1.dp,
    rimAlpha: Float = 0.75f,
    accentTint: Color? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    isDark: Boolean = false
): Modifier {
    val specularRimAlpha = (rimAlpha * if (isDark) 0.65f else 0.85f).coerceIn(0.1f, 1f)
    val exitRimAlpha = (rimAlpha * if (isDark) 0.12f else 0.20f).coerceIn(0.04f, 0.5f)
    val accent = accentTint ?: if (isDark) Color(0x33FFFFFF) else Color(0x22CBD5E1)

    val fresnelBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = specularRimAlpha),
            accent.copy(alpha = specularRimAlpha * 0.40f),
            Color.White.copy(alpha = exitRimAlpha)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    return this.border(
        width = edgeWidth,
        brush = fresnelBrush,
        shape = shape
    )
}

/**
 * High-gloss incident specular curved highlight glare, simulating 3D curved lens refraction at the top rim.
 *
 * @param intensity Brightness and opacity of the glare glint (0f to 1f)
 */
fun Modifier.specularHighlight(
    intensity: Float = 0.65f
): Modifier = this.drawWithContent {
    drawContent()
    if (intensity > 0.05f) {
        // High-gloss top specular lens curve (incident reflection)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.38f * intensity),
                    Color.White.copy(alpha = 0.08f * intensity),
                    Color.Transparent
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width * 0.35f, size.height * 0.55f)
            ),
            size = Size(size.width, size.height * 0.65f)
        )
    }
}

/**
 * Master Base Compose Modifier for ULTIMATE Liquid Glass theme.
 * Composes physical frost diffusion, chromatic refraction caustics, directional Fresnel rim edges,
 * and optional specular lens glare in a single call.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color? = null,
    isDark: Boolean = false,
    refraction: Float = 0.45f,
    frostOpacity: Float = 0.25f,
    fresnelRimAlpha: Float = 0.75f,
    specularGlare: Boolean = true,
    elevation: Dp = 6.dp
): Modifier {
    val shadowAmbient = if (isDark) Color(0x52000000) else Color(0x180F172A)
    val shadowSpot = if (isDark) Color(0x73000000) else Color(0x220F172A)
    val tint = tintColor ?: Color.Unspecified

    return this
        .then(
            if (elevation > 0.dp) {
                Modifier.shadow(
                    elevation = elevation,
                    shape = shape,
                    ambientColor = shadowAmbient,
                    spotColor = shadowSpot
                )
            } else Modifier
        )
        .clip(shape)
        .liquidGlassFrost(
            opacity = frostOpacity,
            isDark = isDark,
            tint = tint
        )
        .liquidGlassRefraction(
            refractionFactor = refraction,
            chromaticDispersion = 0.25f,
            tintColor = tint
        )
        .then(
            if (specularGlare) {
                Modifier.specularHighlight(intensity = 0.55f)
            } else Modifier
        )
        .fresnelEdge(
            edgeWidth = 1.dp,
            rimAlpha = fresnelRimAlpha,
            accentTint = tintColor,
            shape = shape,
            isDark = isDark
        )
}

// =========================================================================================
// BASE COMPOSE COMPONENTS - SURFACE, BOX, PILL, BADGE, CONTAINER
// =========================================================================================

/**
 * LiquidGlassSurface: Base container composable simulating physical glass optics.
 * Provides physical frosted translucency, dynamic chromatic refraction, and Fresnel glancing edge rim.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    refraction: Float = 0.45f,
    frostOpacity: Float = 0.25f,
    fresnelRimAlpha: Float = 0.75f,
    specularGlare: Boolean = true,
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glass_surface_press"
    )

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .scale(scale)
            .liquidGlass(
                shape = shape,
                tintColor = tintColor,
                isDark = isDark,
                refraction = refraction,
                frostOpacity = frostOpacity,
                fresnelRimAlpha = fresnelRimAlpha,
                specularGlare = specularGlare,
                elevation = elevation
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        content()
    }
}

/**
 * LiquidGlassBox: Versatile, lightweight Box with liquid glass optics.
 */
@Composable
fun LiquidGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    tintColor: Color? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    refraction: Float = 0.45f,
    frostOpacity: Float = 0.25f,
    fresnelRimAlpha: Float = 0.75f,
    specularGlare: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.liquidGlass(
            shape = shape,
            tintColor = tintColor,
            isDark = isDark,
            refraction = refraction,
            frostOpacity = frostOpacity,
            fresnelRimAlpha = fresnelRimAlpha,
            specularGlare = specularGlare,
            elevation = 0.dp
        ),
        contentAlignment = contentAlignment
    ) {
        content()
    }
}

/**
 * LiquidGlassPill: Floating rounded capsule/pill with physical glass refraction and Fresnel edges.
 * Ideal for telemetry pills, status tags, chip indicators, and interactive filters.
 */
@Composable
fun LiquidGlassPill(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color? = null,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    testTag: String? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val effectiveAccent = accentColor ?: primaryColor
    val pillShape = CircleShape

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .heightIn(min = 36.dp)
            .liquidGlass(
                shape = pillShape,
                tintColor = if (isSelected) effectiveAccent else null,
                isDark = isDark,
                refraction = if (isSelected) 0.60f else 0.35f,
                frostOpacity = if (isSelected) 0.35f else 0.18f,
                fresnelRimAlpha = if (isSelected) 0.90f else 0.60f,
                specularGlare = true,
                elevation = if (isSelected) 4.dp else 1.dp
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) effectiveAccent else if (isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF1E293B),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) effectiveAccent else if (isDark) Color.White.copy(alpha = 0.90f) else Color(0xFF0F172A),
                    letterSpacing = 0.3.sp
                )
            )
        }
    }
}

/**
 * LiquidGlassBadge: Compact indicator badge with frosted glass body, chromatic core, and luminous Fresnel border.
 */
@Composable
fun LiquidGlassBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    dotColor: Color? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    content: (@Composable () -> Unit)? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val effectiveColor = dotColor ?: primaryColor
    val badgeShape = CircleShape

    Box(
        modifier = modifier
            .liquidGlass(
                shape = badgeShape,
                tintColor = effectiveColor,
                isDark = isDark,
                refraction = 0.50f,
                frostOpacity = 0.30f,
                fresnelRimAlpha = 0.85f,
                specularGlare = true,
                elevation = 2.dp
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else if (count != null) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = effectiveColor,
                    fontSize = 10.sp
                )
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(effectiveColor)
            )
        }
    }
}

/**
 * LiquidGlassContainer: High-level container wrapper for grouping liquid glass elements with unified optical atmosphere.
 */
@Composable
fun LiquidGlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    accentColor: Color? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = shape,
                tintColor = accentColor,
                isDark = isDark,
                refraction = 0.40f,
                frostOpacity = 0.22f,
                fresnelRimAlpha = 0.70f,
                specularGlare = true,
                elevation = 4.dp
            )
            .padding(20.dp)
    ) {
        content()
    }
}

/**
 * LiquidGlassCard: Replaces flat PixelCard with a modern iOS 26 translucent glass card.
 *
 * Implements:
 * - Liquid glass refractive gradient surface
 * - Specular highlight rim border
 * - Soft ambient & spot shadow
 * - Reactive to background wallpaper
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    hasBackground: Boolean = false,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 6.dp,
    testTag: String? = null,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surfaceBrush = LiquidGlassTheme.surfaceBrush(isDark = isDark, hasBackground = hasBackground)
    val borderBrush = LiquidGlassTheme.borderBrush(isDark = isDark)

    val shadowAmbient = if (isDark) Color(0x66000000) else Color(0x1F0F172A)
    val shadowSpot = if (isDark) Color(0x80000000) else Color(0x2E0F172A)

    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glass_card_press"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = shadowAmbient,
                spotColor = shadowSpot
            )
            .clip(shape)
            .background(surfaceBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

/**
 * LiquidGlassButton: Floating liquid glass button with squash & stretch spring motion.
 * Inspired by SwiftUI LiquidGlassPlayButton and LiquidGlassRoundedFloating.
 */
@Composable
fun LiquidGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    isPrimary: Boolean = true,
    testTag: String = "liquid_glass_button"
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    val activeColor = if (isDestructive) errorColor else primaryColor

    val containerBrush = if (isPrimary) {
        Brush.verticalGradient(
            colors = listOf(
                activeColor.copy(alpha = if (isDark) 0.88f else 0.82f),
                activeColor.copy(alpha = if (isDark) 0.72f else 0.68f)
            )
        )
    } else {
        LiquidGlassTheme.surfaceBrush(isDark = isDark, hasBackground = true)
    }

    val rimBrush = if (isPrimary) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.65f),
                activeColor.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.15f)
            )
        )
    } else {
        LiquidGlassTheme.borderBrush(isDark = isDark)
    }

    val contentColor = if (isPrimary) {
        Color.White
    } else {
        if (isDestructive) errorColor else primaryColor
    }

    val buttonShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .testTag(testTag)
            .shadow(
                elevation = if (isPrimary) 8.dp else 4.dp,
                shape = buttonShape,
                ambientColor = if (isPrimary) activeColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.15f),
                spotColor = if (isPrimary) activeColor.copy(alpha = 0.40f) else Color.Black.copy(alpha = 0.25f)
            )
            .clip(buttonShape)
            .background(if (enabled) containerBrush else Brush.linearGradient(listOf(Color.Gray.copy(0.2f), Color.Gray.copy(0.2f))))
            .border(width = 1.dp, brush = rimBrush, shape = buttonShape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) contentColor else contentColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) contentColor else contentColor.copy(alpha = 0.4f),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
            )
        }
    }
}

/**
 * LiquidGlassSliderItem: Liquid Glass Slider with jello squash-and-stretch thumb & glass track
 * Inspired by SwiftUI LiquidGlassJello.swift.
 */
@Composable
fun LiquidGlassSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    icon: ImageVector? = null,
    displayFormatter: (Float) -> String = { "${(it * 100).toInt()}%" },
    testTag: String = "liquid_glass_slider"
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Glass value pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LiquidGlassTheme.highlightPillBrush(isDark = isDark))
                    .border(
                        1.dp,
                        LiquidGlassTheme.borderBrush(isDark = isDark),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = displayFormatter(value),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                inactiveTrackColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}

/**
 * LiquidGlassHContainer: Translucent glass pill container for horizontal action bars / toolbars.
 * Inspired by SwiftUI LiquidGlassHContainer.swift.
 */
@Composable
fun LiquidGlassHContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(32.dp),
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val surfaceBrush = LiquidGlassTheme.surfaceBrush(isDark = isDark, hasBackground = true)
    val borderBrush = LiquidGlassTheme.borderBrush(isDark = isDark)

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = if (isDark) Color(0x66000000) else Color(0x2B0F172A),
                spotColor = if (isDark) Color(0x80000000) else Color(0x3D0F172A)
            )
            .clip(shape)
            .background(surfaceBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        content()
    }
}

/**
 * Three-state Liquid Glass appearance slider.
 * Replaces separate Dark/Light/System controls with ONE rounded Liquid Glass capsule:
 * [ Dark ] [ Light ] [ System ]
 * Features spring movement, soft glow, glass thumb, and subtle specular highlight.
 */
@Composable
fun LiquidGlassAppearanceSlider(
    selectedMode: AppThemeMode,
    onModeSelected: (AppThemeMode) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    testTag: String = "liquid_glass_appearance_slider"
) {
    val isDark = isSystemInDarkTheme()
    val modes = listOf(AppThemeMode.DARK, AppThemeMode.LIGHT, AppThemeMode.SYSTEM)

    val sliderShape = RoundedCornerShape(26.dp)
    val thumbShape = RoundedCornerShape(20.dp)

    val surfaceBrush = LiquidGlassTheme.surfaceBrush(isDark = isDark, hasBackground = true)
    val borderBrush = LiquidGlassTheme.borderBrush(isDark = isDark)
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag(testTag)
            .shadow(
                elevation = 6.dp,
                shape = sliderShape,
                ambientColor = if (isDark) Color(0x66000000) else Color(0x1F0F172A),
                spotColor = if (isDark) Color(0x80000000) else Color(0x2E0F172A)
            )
            .clip(sliderShape)
            .background(surfaceBrush)
            .border(1.dp, borderBrush, sliderShape)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            modes.forEach { mode ->
                val isSelected = mode == selectedMode
                val targetScale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.94f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "theme_thumb_scale_${mode.name}"
                )

                val icon = when (mode) {
                    AppThemeMode.DARK -> Icons.Default.DarkMode
                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                    AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                }

                val title = Strings.get(mode.titleKey, language)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(thumbShape)
                        .then(
                            if (isSelected) {
                                Modifier
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = thumbShape,
                                        spotColor = primaryColor.copy(alpha = 0.40f),
                                        ambientColor = primaryColor.copy(alpha = 0.20f)
                                    )
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                primaryColor.copy(alpha = if (isDark) 0.38f else 0.26f),
                                                primaryColor.copy(alpha = if (isDark) 0.20f else 0.12f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.50f),
                                                primaryColor.copy(alpha = 0.30f),
                                                Color.White.copy(alpha = 0.15f)
                                            )
                                        ),
                                        thumbShape
                                    )
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onModeSelected(mode)
                        }
                        .scale(targetScale)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (isDark) Color.White else primaryColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * UI Theme Selector:
 * Sliding segmented option allowing user to toggle smoothly between:
 * 1. NEWBIEUI26
 * 2. ULTIMATE NEWBIE LIQUID GLASS
 */
@Composable
fun LiquidGlassThemeSelector(
    selectedTheme: UiTheme,
    onThemeSelected: (UiTheme) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    testTag: String = "liquid_glass_theme_selector"
) {
    val isDark = isSystemInDarkTheme()
    val themes = listOf(UiTheme.NEWBIE_UI_26, UiTheme.ULTIMATE_LIQUID_GLASS)

    val sliderShape = RoundedCornerShape(26.dp)
    val thumbShape = RoundedCornerShape(20.dp)

    val surfaceBrush = LiquidGlassTheme.surfaceBrush(isDark = isDark, hasBackground = true)
    val borderBrush = LiquidGlassTheme.borderBrush(isDark = isDark)
    val primaryColor = MaterialTheme.colorScheme.primary

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag(testTag)
            .shadow(
                elevation = 6.dp,
                shape = sliderShape,
                ambientColor = if (isDark) Color(0x66000000) else Color(0x1F0F172A),
                spotColor = if (isDark) Color(0x80000000) else Color(0x2E0F172A)
            )
            .clip(sliderShape)
            .background(surfaceBrush)
            .border(1.dp, borderBrush, sliderShape)
            .padding(4.dp)
    ) {
        val totalWidth = maxWidth
        val tabWidth = totalWidth / themes.size
        val selectedIndex = themes.indexOf(selectedTheme).coerceIn(0, themes.size - 1)

        val slidingOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = 0.78f,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "theme_slide_offset"
        )

        Box(
            modifier = Modifier
                .offset(x = slidingOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .padding(2.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = thumbShape,
                    spotColor = primaryColor.copy(alpha = 0.40f),
                    ambientColor = primaryColor.copy(alpha = 0.20f)
                )
                .clip(thumbShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.38f else 0.26f),
                            primaryColor.copy(alpha = if (isDark) 0.20f else 0.12f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.50f),
                            primaryColor.copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    ),
                    thumbShape
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            themes.forEach { theme ->
                val isSelected = theme == selectedTheme
                val targetScale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.94f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "theme_style_scale_${theme.name}"
                )

                val title = Strings.get(theme.titleKey, language)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(thumbShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onThemeSelected(theme)
                        }
                        .scale(targetScale)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        when (theme) {
                            UiTheme.NEWBIE_UI_26 -> {
                                Icon(
                                    imageVector = Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            UiTheme.ULTIMATE_LIQUID_GLASS -> {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                            else -> {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_matrix_chip),
                                    contentDescription = null,
                                    tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (isDark) Color.White else primaryColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Three-state Liquid Glass Quality Selector:
 * [ Low ] [ Balanced ] [ High ]
 * Allows switching between lightweight blur and full Prismal refraction/Fresnel fidelity.
 */
@Composable
fun LiquidGlassQualitySelector(
    selectedQuality: QualityLevel,
    onQualitySelected: (QualityLevel) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    testTag: String = "liquid_glass_quality_selector"
) {
    val isDark = isSystemInDarkTheme()
    val levels = listOf(QualityLevel.LOW, QualityLevel.BALANCED, QualityLevel.HIGH)

    val sliderShape = RoundedCornerShape(26.dp)
    val thumbShape = RoundedCornerShape(20.dp)

    val surfaceBrush = LiquidGlassTheme.surfaceBrush(isDark = isDark, hasBackground = true)
    val borderBrush = LiquidGlassTheme.borderBrush(isDark = isDark)
    val primaryColor = MaterialTheme.colorScheme.primary

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .testTag(testTag)
            .shadow(
                elevation = 6.dp,
                shape = sliderShape,
                ambientColor = if (isDark) Color(0x66000000) else Color(0x1F0F172A),
                spotColor = if (isDark) Color(0x80000000) else Color(0x2E0F172A)
            )
            .clip(sliderShape)
            .background(surfaceBrush)
            .border(1.dp, borderBrush, sliderShape)
            .padding(4.dp)
    ) {
        val totalWidth = maxWidth
        val tabWidth = totalWidth / levels.size
        val selectedIndex = levels.indexOf(selectedQuality).coerceIn(0, levels.size - 1)

        val slidingOffset by animateDpAsState(
            targetValue = tabWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = 0.78f,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "quality_slide_offset"
        )

        Box(
            modifier = Modifier
                .offset(x = slidingOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .padding(2.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = thumbShape,
                    spotColor = primaryColor.copy(alpha = 0.40f),
                    ambientColor = primaryColor.copy(alpha = 0.20f)
                )
                .clip(thumbShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isDark) 0.38f else 0.26f),
                            primaryColor.copy(alpha = if (isDark) 0.20f else 0.12f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.50f),
                            primaryColor.copy(alpha = 0.30f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    ),
                    thumbShape
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            levels.forEach { level ->
                val isSelected = level == selectedQuality
                val targetScale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.94f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "quality_scale_${level.name}"
                )

                val icon = when (level) {
                    QualityLevel.LOW -> Icons.Default.Speed
                    QualityLevel.BALANCED -> Icons.Default.Tune
                    QualityLevel.HIGH -> Icons.Default.Diamond
                }

                val title = Strings.get(level.titleKey, language)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(thumbShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onQualitySelected(level)
                        }
                        .scale(targetScale)
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) {
                                    if (isDark) Color.White else primaryColor
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Prismal-inspired Liquid Glass Switch:
 * Replaces flat switches with a physical translucent glass thumb and optical track.
 * OFF: dark/transparent glass.
 * ON: subtle accent-tinted glass (never garish neon).
 * Spring movement and press deformation response.
 */
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "liquid_glass_switch"
) {
    val isDark = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary
    var isPressed by remember { mutableStateOf(false) }

    // Physical sizes
    val trackWidth = 52.dp
    val trackHeight = 30.dp
    val thumbBaseSize = 24.dp
    val thumbWidth = if (isPressed) 27.dp else thumbBaseSize
    val thumbHeight = thumbBaseSize

    val trackShape = RoundedCornerShape(15.dp)
    val thumbShape = RoundedCornerShape(12.dp)

    // Animated thumb travel offset: 3dp (left) to 25dp (right)
    val targetOffset = if (checked) 25.dp else 3.dp
    val animatedOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "switch_thumb_offset"
    )

    val trackBrush = if (checked) {
        Brush.verticalGradient(
            listOf(
                primaryColor.copy(alpha = if (isDark) 0.38f else 0.30f),
                primaryColor.copy(alpha = if (isDark) 0.22f else 0.16f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                if (isDark) Color(0x381E293B) else Color(0x1F64748B),
                if (isDark) Color(0x1F0F172A) else Color(0x0F475569)
            )
        )
    }

    val trackBorderBrush = if (checked) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.50f),
                primaryColor.copy(alpha = 0.40f),
                Color.White.copy(alpha = 0.20f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (isDark) 0.25f else 0.40f),
                Color.White.copy(alpha = if (isDark) 0.08f else 0.15f)
            )
        )
    }

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .testTag(testTag)
            .shadow(
                elevation = if (checked) 4.dp else 1.dp,
                shape = trackShape,
                ambientColor = if (checked) primaryColor.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.10f),
                spotColor = if (checked) primaryColor.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.15f)
            )
            .clip(trackShape)
            .background(trackBrush)
            .border(1.dp, trackBorderBrush, trackShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Frosted glass thumb with specular lens highlight
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .size(width = thumbWidth, height = thumbHeight)
                .shadow(
                    elevation = 4.dp,
                    shape = thumbShape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
                .clip(thumbShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFE2E8F0)
                        )
                    )
                )
                .border(
                    width = 0.75.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.White,
                            Color(0x99CBD5E1)
                        )
                    ),
                    shape = thumbShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Internal specular refraction dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (checked) primaryColor.copy(alpha = 0.60f) else Color(0x33000000)
                    )
            )
        }
    }
}

/**
 * Prismal-inspired Glass Icon Button:
 * Floating circular/rounded frosted glass button with spring press & Fresnel border.
 */
@Composable
fun PrismalIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.primary,
    testTag: String = "prismal_icon_button"
) {
    val isDark = isSystemInDarkTheme()
    val shape = CircleShape

    Box(
        modifier = modifier
            .size(46.dp)
            .testTag(testTag)
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = if (isDark) Color(0x40000000) else Color(0x140F172A),
                spotColor = if (isDark) Color(0x60000000) else Color(0x1E0F172A)
            )
            .clip(shape)
            .background(LiquidGlassTheme.surfaceBrush(isDark = isDark, hasBackground = true))
            .border(1.dp, LiquidGlassTheme.borderBrush(isDark = isDark), shape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.4f),
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * iOS 26 Liquid Glass Segmented Tab Menu with Swipe Navigation Support.
 *
 * Features:
 * - Real-time continuous indicator tracking synchronized with a [PagerState] during horizontal swiping
 * - Drag/swipe gesture detection directly on the menu to flick between tabs
 * - Apple-style spring physics on tab tap or snap release
 * - Frosted translucent glass container with specular rim highlight
 */
@Composable
fun <T> LiquidGlassSegmentedMenu(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    iconProvider: (@Composable (T) -> ImageVector?)? = null,
    pagerState: PagerState? = null,
    testTag: String = "liquid_glass_segmented_menu"
) {
    val isDark = isSystemInDarkTheme()
    val itemCount = items.size.coerceAtLeast(1)
    val selectedIndex = items.indexOf(selectedItem).coerceIn(0, itemCount - 1)
    val primaryColor = MaterialTheme.colorScheme.primary

    val containerShape = RoundedCornerShape(20.dp)
    val thumbShape = RoundedCornerShape(16.dp)

    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(46.dp)
            .shadow(
                elevation = 4.dp,
                shape = containerShape,
                ambientColor = if (isDark) Color(0x40000000) else Color(0x140F172A),
                spotColor = if (isDark) Color(0x60000000) else Color(0x1E0F172A)
            )
            .clip(containerShape)
            .background(
                if (isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0x33222A3A),
                            Color(0x1F141A26)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0x4DFFFFFF),
                            Color(0x28F1F5F9)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                brush = LiquidGlassTheme.borderBrush(isDark),
                shape = containerShape
            )
            .pointerInput(items, selectedIndex) {
                detectHorizontalDragGestures(
                    onDragStart = { accumulatedDrag = 0f },
                    onHorizontalDrag = { _, dragAmount -> accumulatedDrag += dragAmount },
                    onDragEnd = {
                        if (accumulatedDrag < -30f) {
                            val nextIdx = (selectedIndex + 1).coerceAtMost(itemCount - 1)
                            onItemSelected(items[nextIdx])
                        } else if (accumulatedDrag > 30f) {
                            val prevIdx = (selectedIndex - 1).coerceAtLeast(0)
                            onItemSelected(items[prevIdx])
                        }
                        accumulatedDrag = 0f
                    }
                )
            }
            .padding(4.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val itemWidth = maxWidth / itemCount

            val isScrolling = pagerState?.isScrollInProgress == true
            val targetIndicatorOffset = if (pagerState != null) {
                itemWidth * (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, (itemCount - 1).toFloat())
            } else {
                itemWidth * selectedIndex
            }

            val animatedOffset by animateDpAsState(
                targetValue = targetIndicatorOffset,
                animationSpec = spring(
                    dampingRatio = 0.78f,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "menu_slider_indicator_offset"
            )

            val indicatorOffset = if (isScrolling) targetIndicatorOffset else animatedOffset

            // Sliding capsule indicator
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp, vertical = 1.dp)
                    .clip(thumbShape)
                    .background(
                        if (isDark) {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x4DFFFFFF),
                                    Color(0x28FFFFFF)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xE6FFFFFF),
                                    Color(0xCCF1F5F9)
                                )
                            )
                        }
                    )
                    .border(
                        width = 0.5.dp,
                        color = if (isDark) Color(0x3DFFFFFF) else Color(0x26000000),
                        shape = thumbShape
                    )
            )

            // Menu Items Row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = item == selectedItem
                    val label = labelProvider(item)
                    val icon = iconProvider?.invoke(item)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(thumbShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onItemSelected(item)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            if (icon != null) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) {
                                        if (isDark) Color.White else primaryColor
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
