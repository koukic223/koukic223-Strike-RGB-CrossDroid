package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AodClockSize
import com.example.model.AodConfig
import com.example.model.QualityLevel

/**
 * Prime Liquid Glass Clock:
 * Faithfully reproduces the ultra-tall condensed tubular liquid glass aesthetic with:
 * - Crisp, high-gloss Fresnel boundary rim (pure white to ice-cyan linear gradient)
 * - Deep translucent teal/cyan liquid glass fill
 * - Elongated stadium capsule geometry for all digits 0-9
 * - Circular filled colon dots with matching rim
 * - Animated fluid morphing spring transitions on digit changes
 */
@Composable
fun TubularLiquidGlassClock(
    timeString: String,
    amPm: String = "",
    accentColor: Color = Color(0xFF00A3C4),
    aodConfig: AodConfig = AodConfig(),
    qualityLevel: QualityLevel = QualityLevel.BALANCED,
    modifier: Modifier = Modifier
) {
    // Determine digit dimensions based on clockSize
    val (digitWidth, digitHeight) = when (aodConfig.clockSize) {
        AodClockSize.SMALL -> Pair(50.dp, 155.dp)
        AodClockSize.MEDIUM -> Pair(60.dp, 186.dp)
        AodClockSize.LARGE -> Pair(68.dp, 212.dp)
        AodClockSize.EXTRA_LARGE -> Pair(78.dp, 242.dp)
    }

    val tubeThicknessFactor = aodConfig.clockThickness.coerceIn(0.5f, 1.5f)
    val tubeWidth = (digitWidth.value * 0.165f * tubeThicknessFactor).dp.coerceIn(8.dp, 15.dp)
    val rimWidth = 1.6.dp

    val colonWidth = (digitWidth.value * 0.32f).dp.coerceIn(16.dp, 26.dp)
    val digitSpacing = (digitWidth.value * 0.12f).dp.coerceIn(6.dp, 12.dp)

    // Breathing pulse for ambient glow
    val infiniteTransition = rememberInfiniteTransition(label = "tubular_fx")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        timeString.forEachIndexed { index, char ->
            if (char == ':') {
                TubularLiquidGlassColon(
                    width = colonWidth,
                    height = digitHeight,
                    tubeWidth = tubeWidth,
                    rimWidth = rimWidth,
                    accentColor = accentColor,
                    aodConfig = aodConfig,
                    pulseGlow = pulseGlow,
                    qualityLevel = qualityLevel
                )
            } else {
                if (index > 0 && timeString[index - 1] != ':') {
                    Spacer(modifier = Modifier.width(digitSpacing))
                }

                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        (slideInVertically(spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)) { it / 3 } +
                                scaleIn(initialScale = 0.90f) +
                                fadeIn(tween(220)))
                            .togetherWith(
                                slideOutVertically(tween(180)) { -it / 3 } +
                                        scaleOut(targetScale = 1.06f) +
                                        fadeOut(tween(180))
                            )
                    },
                    label = "tubular_digit_anim_$index"
                ) { targetDigit ->
                    TubularLiquidGlassDigit(
                        digit = targetDigit,
                        width = digitWidth,
                        height = digitHeight,
                        tubeWidth = tubeWidth,
                        rimWidth = rimWidth,
                        accentColor = accentColor,
                        aodConfig = aodConfig,
                        pulseGlow = pulseGlow,
                        qualityLevel = qualityLevel
                    )
                }
            }
        }

        if (amPm.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = amPm,
                style = TextStyle(
                    fontSize = (digitHeight.value * 0.12f).sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF38BDF8),
                    letterSpacing = 1.sp
                ),
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(bottom = (digitHeight.value * 0.08f).dp)
            )
        }
    }
}

/**
 * Individual tubular liquid glass digit renderer.
 */
@Composable
fun TubularLiquidGlassDigit(
    digit: Char,
    width: Dp,
    height: Dp,
    tubeWidth: Dp,
    rimWidth: Dp,
    accentColor: Color,
    aodConfig: AodConfig,
    pulseGlow: Float,
    qualityLevel: QualityLevel,
    modifier: Modifier = Modifier
) {
    val opacity = aodConfig.clockOpacity.coerceIn(0.3f, 1f)
    val glow = aodConfig.clockGlow.coerceIn(0.1f, 1.2f)

    // Deep translucent petrol teal / cyan liquid glass fill
    val glassFillBrush = remember(accentColor, opacity) {
        val topTint = if (accentColor != Color.Unspecified) accentColor else Color(0xFF00A3C4)
        Brush.verticalGradient(
            colors = listOf(
                topTint.copy(alpha = 0.85f * opacity),
                Color(0xFF0284C7).copy(alpha = 0.80f * opacity),
                Color(0xFF065A6F).copy(alpha = 0.88f * opacity)
            )
        )
    }

    // High-gloss Fresnel perimeter rim (crisp white to ice-cyan linear gradient)
    val rimBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                Color(0xFFE0F7FA).copy(alpha = 0.92f),
                Color(0xFF7DD3FC).copy(alpha = 0.85f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    // Ambient backlight glow
    val ambientGlowColor = remember(accentColor, glow, pulseGlow) {
        accentColor.copy(alpha = 0.28f * glow * pulseGlow)
    }

    Canvas(
        modifier = modifier.size(width, height)
    ) {
        val w = size.width
        val h = size.height
        val strokeWidthPx = tubeWidth.toPx()
        val rimWidthPx = rimWidth.toPx()
        val innerStrokeWidthPx = (strokeWidthPx - 2 * rimWidthPx).coerceAtLeast(1f)

        val padX = strokeWidthPx / 2f
        val padY = strokeWidthPx / 2f
        val left = padX
        val right = w - padX
        val top = padY
        val bottom = h - padY
        val r = (right - left) / 2f
        val midX = (left + right) / 2f
        val midY = (top + bottom) / 2f

        val path = buildDigitCenterlinePath(
            digit = digit,
            left = left,
            right = right,
            top = top,
            bottom = bottom,
            midX = midX,
            midY = midY,
            r = r
        )

        // 1. Ambient Refractive Backlight Glow
        if (qualityLevel != QualityLevel.LOW && aodConfig.glowEffect) {
            drawPath(
                path = path,
                color = ambientGlowColor,
                style = Stroke(
                    width = strokeWidthPx + 10.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // 2. Outer Fresnel Rim (Crisp white / ice-cyan boundary line)
        drawPath(
            path = path,
            brush = rimBrush,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Inner Translucent Liquid Glass Core
        drawPath(
            path = path,
            brush = glassFillBrush,
            style = Stroke(
                width = innerStrokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 4. Specular Curved Top Glint (High-gloss lens reflection)
        if (qualityLevel == QualityLevel.HIGH) {
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.50f),
                        Color.White.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.40f
                ),
                style = Stroke(
                    width = innerStrokeWidthPx * 0.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

/**
 * Tubular liquid glass colon divider with two circular dots.
 */
@Composable
fun TubularLiquidGlassColon(
    width: Dp,
    height: Dp,
    tubeWidth: Dp,
    rimWidth: Dp,
    accentColor: Color,
    aodConfig: AodConfig,
    pulseGlow: Float,
    qualityLevel: QualityLevel,
    modifier: Modifier = Modifier
) {
    val opacity = aodConfig.clockOpacity.coerceIn(0.3f, 1f)
    val glow = aodConfig.clockGlow.coerceIn(0.1f, 1.2f)

    val glassFillBrush = remember(accentColor, opacity) {
        val topTint = if (accentColor != Color.Unspecified) accentColor else Color(0xFF00A3C4)
        Brush.verticalGradient(
            colors = listOf(
                topTint.copy(alpha = 0.85f * opacity),
                Color(0xFF0284C7).copy(alpha = 0.80f * opacity),
                Color(0xFF065A6F).copy(alpha = 0.88f * opacity)
            )
        )
    }

    val rimBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.98f),
                Color(0xFFE0F7FA).copy(alpha = 0.92f),
                Color(0xFF7DD3FC).copy(alpha = 0.85f)
            )
        )
    }

    val ambientGlowColor = remember(accentColor, glow, pulseGlow) {
        accentColor.copy(alpha = 0.32f * glow * pulseGlow)
    }

    Canvas(
        modifier = modifier.size(width, height)
    ) {
        val w = size.width
        val h = size.height
        val midX = w / 2f
        val midY = h / 2f

        val dotRadius = tubeWidth.toPx() * 0.58f
        val rimWidthPx = rimWidth.toPx()
        val innerDotRadius = (dotRadius - rimWidthPx).coerceAtLeast(1f)

        // Vertical positions matching the photo (~33% and ~67% height)
        val topCenterY = midY - h * 0.17f
        val bottomCenterY = midY + h * 0.17f

        val dotCenters = listOf(
            Offset(midX, topCenterY),
            Offset(midX, bottomCenterY)
        )

        for (center in dotCenters) {
            // Ambient glow
            if (qualityLevel != QualityLevel.LOW && aodConfig.glowEffect) {
                drawCircle(
                    color = ambientGlowColor,
                    radius = dotRadius + 6.dp.toPx(),
                    center = center
                )
            }

            // Outer crisp white rim
            drawCircle(
                brush = rimBrush,
                radius = dotRadius,
                center = center
            )

            // Inner teal liquid glass fill
            drawCircle(
                brush = glassFillBrush,
                radius = innerDotRadius,
                center = center
            )

            // Specular glint
            if (qualityLevel == QualityLevel.HIGH) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.65f),
                    radius = innerDotRadius * 0.35f,
                    center = Offset(center.x - innerDotRadius * 0.25f, center.y - innerDotRadius * 0.25f)
                )
            }
        }
    }
}

/**
 * Builds the mathematical centerline path for any digit '0'..'9' matching the photo.
 */
private fun buildDigitCenterlinePath(
    digit: Char,
    left: Float,
    right: Float,
    top: Float,
    bottom: Float,
    midX: Float,
    midY: Float,
    r: Float
): Path {
    val path = Path()

    when (digit) {
        '0' -> {
            // Closed elongated stadium (capsule)
            path.moveTo(left, top + r)
            path.arcTo(
                rect = Rect(left, top, right, top + 2 * r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            path.lineTo(right, bottom - r)
            path.arcTo(
                rect = Rect(left, bottom - 2 * r, right, bottom),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            path.lineTo(left, top + r)
            path.close()
        }

        '1' -> {
            // Center stem with subtle curved top beak
            path.moveTo(left + r * 0.25f, top + r * 1.25f)
            path.quadraticTo(left + r * 0.45f, top, midX, top)
            path.lineTo(midX, bottom)
        }

        '2' -> {
            // Top hook, graceful center diagonal, flat base
            path.moveTo(left, top + r * 1.4f)
            path.lineTo(left, top + r)
            path.arcTo(
                rect = Rect(left, top, right, top + 2 * r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            path.lineTo(right, midY - r * 0.2f)
            path.cubicTo(
                right, midY + (bottom - midY) * 0.35f,
                left, midY + (bottom - midY) * 0.65f,
                left, bottom
            )
            path.lineTo(right, bottom)
        }

        '3' -> {
            // EXACT PHOTO DESIGN:
            // Symmetrical top hook, top arc, inward spur at middle, bottom arc, and bottom hook
            path.moveTo(left, top + r * 1.5f)
            path.lineTo(left, top + r)
            path.arcTo(
                rect = Rect(left, top, right, top + 2 * r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            path.lineTo(right, midY - r * 0.45f)
            path.lineTo(left + r * 0.85f, midY)
            path.lineTo(right, midY + r * 0.45f)
            path.lineTo(right, bottom - r)
            path.arcTo(
                rect = Rect(left, bottom - 2 * r, right, bottom),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            path.lineTo(left, bottom - r * 1.5f)
        }

        '4' -> {
            // Right stem, upper-left arm, and horizontal crossbar
            path.moveTo(right, top)
            path.lineTo(right, bottom)

            path.moveTo(left, top + r * 0.4f)
            path.lineTo(left, midY + r * 0.4f)
            path.lineTo(right, midY + r * 0.4f)
        }

        '5' -> {
            // Top horizontal bar, left stalk, bottom stadium loop with hook
            path.moveTo(right, top)
            path.lineTo(left, top)
            path.lineTo(left, midY)
            path.lineTo(right - r, midY)
            path.arcTo(
                rect = Rect(left, midY, right, midY + 2 * r),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            path.lineTo(right, bottom - r)
            path.arcTo(
                rect = Rect(left, bottom - 2 * r, right, bottom),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            path.lineTo(left, bottom - r * 1.5f)
        }

        '6' -> {
            // Top right hook, full left stem, closed bottom stadium loop
            path.moveTo(right, top + r * 1.5f)
            path.lineTo(right, top + r)
            path.arcTo(
                rect = Rect(left, top, right, top + 2 * r),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            path.lineTo(left, bottom - r)
            path.arcTo(
                rect = Rect(left, bottom - 2 * r, right, bottom),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            path.lineTo(right, midY)
            path.lineTo(left, midY)
        }

        '7' -> {
            // Subtle top-left spur, top bar, descending right stalk
            path.moveTo(left, top + r * 0.9f)
            path.lineTo(left, top + r * 0.4f)
            path.quadraticTo(left, top, left + r * 0.5f, top)
            path.lineTo(right, top)
            path.lineTo(right, bottom)
        }

        '8' -> {
            // Two closed stadium loops connected at center
            path.moveTo(left, top + r)
            path.arcTo(Rect(left, top, right, top + 2 * r), 180f, 180f, false)
            path.lineTo(right, midY)
            path.lineTo(left, midY)
            path.lineTo(left, top + r)
            path.close()

            path.moveTo(left, midY)
            path.lineTo(right, midY)
            path.lineTo(right, bottom - r)
            path.arcTo(Rect(left, bottom - 2 * r, right, bottom), 0f, 180f, false)
            path.lineTo(left, midY)
            path.close()
        }

        '9' -> {
            // EXACT PHOTO DESIGN:
            // 1. Closed top stadium loop
            path.moveTo(left, top + r)
            path.arcTo(
                rect = Rect(left, top, right, top + 2 * r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            path.lineTo(right, midY)
            path.lineTo(left, midY)
            path.lineTo(left, top + r)
            path.close()

            // 2. Right stem descending to bottom and curving into U-hook
            path.moveTo(right, midY)
            path.lineTo(right, bottom - r)
            path.arcTo(
                rect = Rect(left, bottom - 2 * r, right, bottom),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 180f,
                forceMoveTo = false
            )
            path.lineTo(left, bottom - r * 1.5f)
        }

        else -> {
            // Fallback stalk
            path.moveTo(midX, top)
            path.lineTo(midX, bottom)
        }
    }

    return path
}

/**
 * Organic wavy dark silk backdrop simulating the deep ripples from the user's photo.
 */
@Composable
fun LiquidWavySilkBackdrop(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base obsidian black
        drawRect(color = Color(0xFF030508))

        // Wave Ribbon 1 (Top-Left sweep)
        val wave1 = Path().apply {
            moveTo(0f, h * 0.08f)
            cubicTo(
                w * 0.35f, h * 0.02f,
                w * 0.65f, h * 0.22f,
                w, h * 0.12f
            )
            lineTo(w, h * 0.35f)
            cubicTo(
                w * 0.60f, h * 0.42f,
                w * 0.25f, h * 0.20f,
                0f, h * 0.28f
            )
            close()
        }
        drawPath(
            path = wave1,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1E2836).copy(alpha = 0.55f),
                    Color(0xFF0E131C).copy(alpha = 0.35f),
                    Color.Transparent
                )
            )
        )

        // Wave Ribbon 2 (Main central diagonal fold)
        val wave2 = Path().apply {
            moveTo(0f, h * 0.38f)
            cubicTo(
                w * 0.40f, h * 0.30f,
                w * 0.70f, h * 0.55f,
                w, h * 0.42f
            )
            lineTo(w, h * 0.72f)
            cubicTo(
                w * 0.55f, h * 0.85f,
                w * 0.20f, h * 0.52f,
                0f, h * 0.60f
            )
            close()
        }
        drawPath(
            path = wave2,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF161E2A).copy(alpha = 0.65f),
                    Color(0xFF0A0F17).copy(alpha = 0.40f),
                    Color(0xFF030508)
                ),
                start = Offset(0f, h * 0.35f),
                end = Offset(w, h * 0.65f)
            )
        )

        // Wave Ribbon 3 (Lower sweeping fold)
        val wave3 = Path().apply {
            moveTo(0f, h * 0.68f)
            cubicTo(
                w * 0.45f, h * 0.62f,
                w * 0.75f, h * 0.88f,
                w, h * 0.75f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = wave3,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF131922).copy(alpha = 0.50f),
                    Color(0xFF080B10).copy(alpha = 0.80f),
                    Color(0xFF030508)
                )
            )
        )

        // Delicate specular curve sheen highlights along the crests
        val crest1 = Path().apply {
            moveTo(0f, h * 0.08f)
            cubicTo(w * 0.35f, h * 0.02f, w * 0.65f, h * 0.22f, w, h * 0.12f)
        }
        drawPath(
            path = crest1,
            color = Color.White.copy(alpha = 0.08f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )

        val crest2 = Path().apply {
            moveTo(0f, h * 0.38f)
            cubicTo(w * 0.40f, h * 0.30f, w * 0.70f, h * 0.55f, w, h * 0.42f)
        }
        drawPath(
            path = crest2,
            color = Color.White.copy(alpha = 0.12f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        val crest3 = Path().apply {
            moveTo(0f, h * 0.68f)
            cubicTo(w * 0.45f, h * 0.62f, w * 0.75f, h * 0.88f, w, h * 0.75f)
        }
        drawPath(
            path = crest3,
            color = Color.White.copy(alpha = 0.07f),
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
