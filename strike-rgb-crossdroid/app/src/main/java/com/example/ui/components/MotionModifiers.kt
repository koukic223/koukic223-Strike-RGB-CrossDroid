package com.example.ui.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.absoluteValue

/**
 * Material Design 3 and iOS 26 Liquid Glass Motion Standards.
 * Provides fluid spatial physics, spring damping, and parallax depth for screen transitions.
 */
object M3MotionDefaults {
    /**
     * M3 Expressive Spring for primary screen and tab transitions.
     * Damped for a premium, non-jittery iOS-style glide.
     */
    val ScreenTransitionSpring = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = 380f
    )

    /**
     * Crisp spring for segmented menus and sub-page sliders.
     */
    val SubMenuSpring = spring<Float>(
        dampingRatio = 0.84f,
        stiffness = 450f
    )

    /**
     * M3 Emphasized Decelerate easing for fluid entrances.
     */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /**
     * M3 Emphasized Accelerate-Decelerate standard easing curve.
     */
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
}

/**
 * Applies Material Design 3 spatial motion and iOS 26 Liquid Glass 3D perspective to Pager pages.
 *
 * - Subtle scale reduction (depth layer effect)
 * - Smooth progressive opacity fade
 * - Horizontal parallax translation compensation
 * - Subtle 3D perspective tilt
 */
fun Modifier.m3PageTransition(
    pagerState: PagerState,
    page: Int,
    scaleDown: Float = 0.07f,
    alphaFade: Float = 0.50f,
    tiltAngle: Float = 4.5f
): Modifier = this.graphicsLayer {
    val rawOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
    val pageOffset = rawOffset.absoluteValue.coerceIn(0f, 1f)

    // Spatial depth scaling
    val scale = 1f - (scaleDown * pageOffset)
    scaleX = scale
    scaleY = scale

    // Progressive alpha fade
    alpha = 1f - (alphaFade * pageOffset)

    // Parallax glide: content layers appear deeper than the viewport
    translationX = rawOffset * size.width * 0.15f

    // Subtle 3D spatial card tilt
    if (tiltAngle > 0f) {
        cameraDistance = 16f * density
        rotationY = rawOffset.coerceIn(-1f, 1f) * -tiltAngle
    }
}
