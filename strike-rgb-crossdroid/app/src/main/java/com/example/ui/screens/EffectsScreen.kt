package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Waves
import com.example.effects.screensync.MediaProjectionHolder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Strings
import com.example.model.AppLanguage
import com.example.model.EffectType
import com.example.model.LedState
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassSegmentedMenu
import com.example.ui.components.LiquidGlassSliderItem
import com.example.ui.components.LiquidGlassTheme
import com.example.ui.components.M3MotionDefaults
import com.example.ui.components.m3PageTransition
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelCard
import com.example.ui.components.PixelLedOrb
import com.example.ui.components.PixelOutlinedButton
import com.example.ui.components.PixelSectionTitle
import com.example.ui.components.PixelSliderItem
import com.example.ui.components.PixelSwitchItem
import com.example.ui.components.PixelTonalButton
import kotlin.math.roundToInt

enum class EffectsSubMenu(val titleKey: String, val icon: ImageVector) {
    MODES("default_effect", Icons.Default.AutoAwesome),
    DYNAMICS("speed", Icons.Default.Tune),
    TRANSITIONS("smooth_transition", Icons.Default.Animation)
}

@Composable
fun EffectsScreen(
    ledState: LedState,
    language: AppLanguage,
    onEffectSelect: (EffectType) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onIntensityChange: (Float) -> Unit = {},
    onSpeedChange: (Float) -> Unit,
    onBreathingSpeedChange: (Float) -> Unit,
    onSmoothTransitionChange: (Boolean) -> Unit,
    onSaveLastEffectChange: (Boolean) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onRequestMediaProjection: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val subMenus = remember { EffectsSubMenu.entries }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { subMenus.size }
    )
    val coroutineScope = rememberCoroutineScope()

    val effectsList = listOf(
        Triple(EffectType.STATIC, "effect_static", Icons.Default.Lightbulb),
        Triple(EffectType.BREATHING, "effect_breathing", Icons.Default.Grain),
        Triple(EffectType.RAINBOW, "effect_rainbow", Icons.Default.InvertColors),
        Triple(EffectType.STROBE, "effect_strobe", Icons.Default.FlashOn),
        Triple(EffectType.WAVE, "effect_wave", Icons.Default.Waves),
        Triple(EffectType.PULSE, "effect_pulse", Icons.Default.GraphicEq),
        Triple(EffectType.SCREEN_SYNC, "effect_screen_sync", Icons.Default.Tv)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Section Title
        Text(
            text = Strings.get("effects", language),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        // Live preview mini card (Liquid Glass)
        LiquidGlassCard(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.get("active_mode", language),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = Strings.get(ledState.currentEffect.displayNameKey, language),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                PixelLedOrb(
                    color = ledState.currentColor,
                    isRunning = ledState.isRunning,
                    effect = ledState.currentEffect,
                    brightness = ledState.brightness,
                    speed = ledState.effectSpeed,
                    sizeDp = 72
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // iOS 26 Liquid Glass Segmented Menu with Swipe Navigation
        LiquidGlassSegmentedMenu(
            items = subMenus,
            selectedItem = subMenus[pagerState.currentPage],
            onItemSelected = { menu ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        page = subMenus.indexOf(menu),
                        animationSpec = M3MotionDefaults.SubMenuSpring
                    )
                }
            },
            labelProvider = { Strings.get(it.titleKey, language) },
            iconProvider = { it.icon },
            pagerState = pagerState,
            testTag = "effects_segmented_menu"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Swipeable Pager for Effects Sub-menus with M3 Motion
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            pageSpacing = 12.dp,
            beyondViewportPageCount = 1
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .m3PageTransition(pagerState, page, scaleDown = 0.05f, alphaFade = 0.40f, tiltAngle = 3f)
            ) {
                when (subMenus[page]) {
                EffectsSubMenu.MODES -> {
                    val modesScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(modesScrollState),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (rowItems in effectsList.chunked(2)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for ((effect, nameKey, icon) in rowItems) {
                                    val isSelected = ledState.currentEffect == effect
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(
                                                if (isSelected) LiquidGlassTheme.highlightPillBrush(isDark)
                                                else LiquidGlassTheme.surfaceBrush(isDark)
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                brush = if (isSelected) {
                                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                                    )
                                                } else LiquidGlassTheme.borderBrush(isDark),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .clickable {
                                                if (effect == EffectType.SCREEN_SYNC && !MediaProjectionHolder.hasPermission) {
                                                    onRequestMediaProjection()
                                                } else {
                                                    onEffectSelect(effect)
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 14.dp)
                                            .testTag("effect_item_${effect.name}"),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = Strings.get(nameKey, language),
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }

                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                EffectsSubMenu.DYNAMICS -> {
                    val dynamicsScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(dynamicsScrollState),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        LiquidGlassCard {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                LiquidGlassSliderItem(
                                    title = Strings.get("brightness", language),
                                    value = ledState.brightness,
                                    onValueChange = onBrightnessChange,
                                    valueRange = 0.05f..1.0f,
                                    icon = Icons.Default.Lightbulb,
                                    displayFormatter = { "${(it * 100).roundToInt()}%" },
                                    testTag = "brightness_slider"
                                )

                                LiquidGlassSliderItem(
                                    title = "Intensity",
                                    value = ledState.intensity,
                                    onValueChange = onIntensityChange,
                                    valueRange = 0.05f..1.0f,
                                    icon = Icons.Default.Tune,
                                    displayFormatter = { "${(it * 100).roundToInt()}%" },
                                    testTag = "intensity_slider"
                                )

                                LiquidGlassSliderItem(
                                    title = Strings.get("effect_speed", language),
                                    value = ledState.effectSpeed,
                                    onValueChange = onSpeedChange,
                                    valueRange = 0.2f..3.0f,
                                    icon = Icons.Default.Speed,
                                    displayFormatter = { String.format("%.1fx", it) },
                                    testTag = "effect_speed_slider"
                                )

                                LiquidGlassSliderItem(
                                    title = Strings.get("breathing_speed", language),
                                    value = ledState.breathingSpeed,
                                    onValueChange = onBreathingSpeedChange,
                                    valueRange = 0.5f..5.0f,
                                    icon = Icons.Default.Timer,
                                    displayFormatter = { String.format("%.1fs", it) },
                                    testTag = "breathing_speed_slider"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                EffectsSubMenu.TRANSITIONS -> {
                    val transitionsScrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(transitionsScrollState),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        LiquidGlassCard {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                PixelSwitchItem(
                                    title = Strings.get("smooth_transition", language),
                                    checked = ledState.smoothTransition,
                                    onCheckedChange = onSmoothTransitionChange,
                                    icon = Icons.Default.Animation,
                                    testTag = "smooth_transition_switch"
                                )

                                PixelSwitchItem(
                                    title = Strings.get("save_last_effect", language),
                                    checked = ledState.saveLastEffect,
                                    onCheckedChange = onSaveLastEffectChange,
                                    icon = Icons.Default.Save,
                                    testTag = "save_last_effect_switch"
                                )
                            }
                        }

                        // Action Buttons: APPLY & RESET (Liquid Glass)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            LiquidGlassButton(
                                text = Strings.get("apply", language),
                                onClick = onApply,
                                modifier = Modifier.weight(1f),
                                testTag = "apply_effects_button"
                            )

                            LiquidGlassButton(
                                text = Strings.get("reset", language),
                                onClick = onReset,
                                icon = Icons.Default.Refresh,
                                isPrimary = false,
                                modifier = Modifier.weight(1f),
                                testTag = "reset_effects_button"
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
    }
}
