package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.notification.ScreenFlashState

/**
 * Screen Flash Overlay:
 * Temporarily flashes the entire screen with the notification's resolved accent color or pure white.
 * Automatically synchronizes with RGB LED and camera flash.
 */
@Composable
fun ScreenFlashOverlay(
    flashState: ScreenFlashState,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = flashState.isVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        val flashColor = flashState.color.toComposeColor()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(flashColor.copy(alpha = flashState.alpha))
                .clickable { onDismiss() }
        )
    }
}
