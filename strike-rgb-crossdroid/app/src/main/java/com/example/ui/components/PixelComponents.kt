package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EffectType
import com.example.model.RgbColor
import kotlin.math.roundToInt

/**
 * Pixel-style large rounded primary button with comfortable touch target and elevation.
 */
@Composable
fun PixelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    testTag: String = "pixel_button"
) {
    LiquidGlassButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        isDestructive = isDestructive,
        isPrimary = true,
        testTag = testTag
    )
}

/**
 * Pixel-style secondary / tonal container button with rounded shape.
 */
@Composable
fun PixelTonalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "pixel_tonal_button"
) {
    LiquidGlassButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        isDestructive = false,
        isPrimary = false,
        testTag = testTag
    )
}

/**
 * Pixel-style Outlined Button with rounded pill shape.
 */
@Composable
fun PixelOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "pixel_outlined_button"
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.linearGradient(
                listOf(
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
            )
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

/**
 * Liquid Glass transparent card replacing legacy PixelCard with iOS 26 frosted glass styling.
 */
@Composable
fun PixelCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(26.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    LiquidGlassCard(
        modifier = modifier,
        shape = shape,
        onClick = onClick,
        content = content
    )
}

/**
 * Pixel-style Switch Item with title, optional description, and rounded pill switch.
 */
@Composable
fun PixelSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    testTag: String = "pixel_switch"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            testTag = testTag
        )
    }
}

/**
 * Pixel-style Slider with value badge, title, and dynamic track color.
 */
@Composable
fun PixelSliderItem(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    displayFormatter: (Float) -> String = { "${(it * 100).roundToInt()}%" },
    icon: ImageVector? = null,
    testTag: String = "pixel_slider"
) {
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

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = displayFormatter(value),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
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
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}

/**
 * Pixel-style Section Title with modern typography and optional icon.
 */
@Composable
fun PixelSectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.2.sp
        ),
        modifier = modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

/**
 * Pixel-style Status Dot and Badge
 */
@Composable
fun PixelStatusDot(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF34A853), // Google Green
    inactiveColor: Color = Color(0xFFEA4335) // Google Red
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(
                if (isActive) activeColor.copy(alpha = alpha) else inactiveColor
            )
            .border(
                1.5.dp,
                if (isActive) activeColor else inactiveColor.copy(alpha = 0.7f),
                CircleShape
            )
    )
}

/**
 * Interactive Glowing LED Orb representing active hardware LED
 */
@Composable
fun PixelLedOrb(
    color: RgbColor,
    isRunning: Boolean,
    effect: EffectType,
    brightness: Float,
    speed: Float,
    modifier: Modifier = Modifier,
    sizeDp: Int = 110
) {
    val infiniteTransition = rememberInfiniteTransition(label = "led_orb")

    // Breathing pulse
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1400 / speed.coerceAtLeast(0.3f)).toInt(),
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Strobe flash
    val strobeOn by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe"
    )

    // Rainbow hue
    val rainbowHue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (4000 / speed.coerceAtLeast(0.2f)).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainbow"
    )

    val activeDisplayColor = remember(color, effect, isRunning, rainbowHue) {
        if (!isRunning) {
            Color(0xFF2B2D33)
        } else {
            when (effect) {
                EffectType.RAINBOW -> {
                    val hsv = floatArrayOf(rainbowHue, 0.95f, 1.0f)
                    Color(android.graphics.Color.HSVToColor(hsv))
                }
                else -> color.toComposeColor()
            }
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = activeDisplayColor,
        animationSpec = tween(300),
        label = "orb_color"
    )

    val currentAlpha: Float = remember(isRunning, effect, breathScale, strobeOn, brightness) {
        if (!isRunning) 0.15f
        else when (effect) {
            EffectType.STATIC -> brightness.coerceIn(0.2f, 1f)
            EffectType.BREATHING -> breathScale * brightness.coerceIn(0.2f, 1f)
            EffectType.HEARTBEAT -> breathScale * brightness.coerceIn(0.2f, 1f)
            EffectType.STROBE -> if (strobeOn > 0.5f) brightness else 0.05f
            EffectType.PULSE -> ((breathScale * breathScale) * brightness).coerceIn(0.1f, 1f)
            EffectType.WAVE -> breathScale * brightness
            EffectType.RAINBOW -> brightness.coerceIn(0.2f, 1f)
            EffectType.SCREEN_SYNC -> brightness.coerceIn(0.2f, 1f)
            EffectType.AMBILIGHT -> brightness.coerceIn(0.2f, 1f)
            else -> breathScale * brightness.coerceIn(0.2f, 1f)
        }
    }

    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer diffused aura glow
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val center = this.center
            val radius = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        animatedColor.copy(alpha = (0.55f * currentAlpha).coerceIn(0f, 1f)),
                        animatedColor.copy(alpha = (0.18f * currentAlpha).coerceIn(0f, 1f)),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // Inner solid hardware diode
        Box(
            modifier = Modifier
                .size((sizeDp * 0.52f).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isRunning) 0.85f * currentAlpha else 0.2f),
                            animatedColor.copy(alpha = if (isRunning) 0.95f * currentAlpha else 0.3f),
                            animatedColor.copy(alpha = if (isRunning) 0.8f * currentAlpha else 0.2f)
                        )
                    )
                )
                .border(
                    2.dp,
                    if (isRunning) animatedColor.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.3f),
                    CircleShape
                )
        )
    }
}

/**
 * Pixel-style Dialog
 */
@Composable
fun PixelDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    confirmText: String,
    onDismiss: (() -> Unit)? = null,
    dismissText: String? = null
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val borderBrush = LiquidGlassTheme.borderBrush(isDark)
    val dialogContainerColor = if (isDark) Color(0xD9181E29) else Color(0xEEFFFFFF)

    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() },
        shape = RoundedCornerShape(28.dp),
        containerColor = dialogContainerColor,
        modifier = Modifier.border(1.dp, borderBrush, RoundedCornerShape(28.dp)),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = if (onDismiss != null && dismissText != null) {
            {
                TextButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = dismissText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        } else null
    )
}
