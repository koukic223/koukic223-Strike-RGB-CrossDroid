package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Strings
import com.example.model.AppLanguage
import com.example.ui.screens.ScreenTab

/**
 * iOS Liquid Glass Floating Pill Navigation Bar.
 *
 * Faithfully reproduces the fluid sliding pill navigation experience from iOS:
 * - Full stadium / pill capsule container with frosted glass finish and ambient shadow
 * - Sliding pill slider indicator with spring physics that glides across tabs
 * - Selected active tab with vibrant coral-red tone and soft ambient glowing halo
 * - Crisp, high-contrast inactive tabs with icon and title
 * - Full gesture navigation edge-to-edge support with safe area padding
 */
@Composable
fun LiquidGlassNavigationBar(
    tabs: List<ScreenTab>,
    selectedTab: ScreenTab,
    onTabSelected: (ScreenTab) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    tabBadges: Map<ScreenTab, String?> = emptyMap(),
    pagerState: PagerState? = null
) {
    val configuration = LocalConfiguration.current
    val isCompactScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360
    val isDark = isSystemInDarkTheme()

    // Stadium / full capsule shape
    val containerShape = CircleShape

    val tabCount = tabs.size.coerceAtLeast(1)
    val selectedIndex = tabs.indexOf(selectedTab).coerceIn(0, tabCount - 1)

    // Gesture detection state for swiping on the navigation bar itself
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    // Semi-transparent backdrop glass: background: rgba(255, 255, 255, 0.1) as requested
    // with backdrop-filter: blur(20px) to prevent solid black-box artifacts.
    val glassBackgroundColor = Color(1f, 1f, 1f, 0.10f) // rgba(255, 255, 255, 0.1)

    // Specular border
    val borderBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0x40FFFFFF),
                Color(0x14FFFFFF),
                Color(0x0A000000)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xC0FFFFFF),
                Color(0x40CBD5E1),
                Color(0x2094A3B8)
            )
        )
    }

    // Active accent color (iOS Coral-Red as shown in user GIF/image)
    val activeAccentColor = Color(0xFFFF3B30)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(
                start = if (isCompactScreen) 16.dp else 24.dp,
                end = if (isCompactScreen) 16.dp else 24.dp,
                bottom = if (isCompactScreen) 8.dp else 16.dp,
                top = 4.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        // Floating pill capsule bar with horizontal swipe/flick navigation
        Box(
            modifier = Modifier
                .testTag("bottom-navigation-bar")
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .height(if (isCompactScreen) 58.dp else 66.dp)
                .clip(containerShape)
                .border(1.dp, borderBrush, containerShape)
                .pointerInput(tabs, selectedIndex) {
                    detectHorizontalDragGestures(
                        onDragStart = { accumulatedDrag = 0f },
                        onHorizontalDrag = { _, dragAmount ->
                            accumulatedDrag += dragAmount
                        },
                        onDragEnd = {
                            if (accumulatedDrag < -28f) {
                                // Swipe left -> advance to next tab
                                val nextIdx = (selectedIndex + 1).coerceAtMost(tabCount - 1)
                                onTabSelected(tabs[nextIdx])
                            } else if (accumulatedDrag > 28f) {
                                // Swipe right -> return to previous tab
                                val prevIdx = (selectedIndex - 1).coerceAtLeast(0)
                                onTabSelected(tabs[prevIdx])
                            }
                            accumulatedDrag = 0f
                        }
                    )
                }
        ) {
            // Hardware-accelerated semi-transparent backdrop-filter: blur(20px) layer
            // with background: rgba(255, 255, 255, 0.1) to eliminate solid black-box artifacts
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(20.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                    .background(glassBackgroundColor)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val tabWidth = maxWidth / tabCount

                val isScrolling = pagerState?.isScrollInProgress == true
                // The Slider Indicator: smoothly glides horizontally synchronized with swipe gestures or spring snap
                val targetIndicatorOffset = if (pagerState != null) {
                    tabWidth * (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, (tabCount - 1).toFloat())
                } else {
                    tabWidth * selectedIndex
                }

                val animatedOffset by animateDpAsState(
                    targetValue = targetIndicatorOffset,
                    animationSpec = spring(
                        dampingRatio = 0.74f, // Apple spring with subtle overshoot bounce
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "pill_slider_indicator_offset"
                )

                val indicatorOffset = if (isScrolling) targetIndicatorOffset else animatedOffset

                val sliderPillShape = CircleShape

                // Sliding capsule indicator background
                Box(
                    modifier = Modifier
                        .offset(x = indicatorOffset)
                        .width(tabWidth)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .clip(sliderPillShape)
                        .background(
                            if (isDark) {
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x38FFFFFF),
                                        Color(0x22FFFFFF)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x1F000000),
                                        Color(0x14000000)
                                    )
                                )
                            }
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (isDark) Color(0x2EFFFFFF) else Color(0x12000000),
                            shape = sliderPillShape
                        )
                )

                // Interactive Tab items layered on top
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEach { tab ->
                        val isSelected = tab == selectedTab
                        val badgeText = tabBadges[tab]

                        LiquidGlassTabItem(
                            tab = tab,
                            isSelected = isSelected,
                            activeColor = activeAccentColor,
                            badgeText = badgeText,
                            isDark = isDark,
                            isCompact = isCompactScreen,
                            label = Strings.get(tab.titleKey, language),
                            onClick = { onTabSelected(tab) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun LiquidGlassTabItem(
    tab: ScreenTab,
    isSelected: Boolean,
    activeColor: Color,
    badgeText: String?,
    isDark: Boolean,
    isCompact: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Subtle touch bounce on selection
    val bounceScale by animateFloatAsState(
        targetValue = if (isSelected) 1.0f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tab_scale_${tab.name}"
    )

    val inactiveColor = if (isDark) Color(0xFFF2F2F7) else Color(0xFF111113)

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else inactiveColor,
        animationSpec = tween(durationMillis = 200),
        label = "tab_color_${tab.name}"
    )

    Box(
        modifier = modifier
            .scale(bounceScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .testTag("nav_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon container with soft radial glowing halo when selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(if (isCompact) 28.dp else 32.dp)
            ) {
                // Glowing red halo behind the active icon (matching the screenshot/GIF)
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 28.dp else 32.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        activeColor.copy(alpha = 0.65f),
                                        activeColor.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                }

                BadgedBox(
                    badge = {
                        if (!badgeText.isNullOrEmpty()) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.offset(x = 4.dp, y = (-2).dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = label,
                        tint = contentColor,
                        modifier = Modifier.size(if (isCompact) 19.dp else 22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(1.dp))

            // Label
            Text(
                text = label,
                color = contentColor,
                fontSize = if (isCompact) 10.sp else 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}
