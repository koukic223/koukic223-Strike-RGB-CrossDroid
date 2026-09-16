package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.ui.components.LiquidGlassSegmentedMenu
import com.example.ui.components.LiquidGlassTheme
import com.example.ui.components.M3MotionDefaults
import com.example.ui.components.m3PageTransition
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import com.example.effects.screensync.MediaProjectionHolder
import com.example.model.ScreenRegion
import com.example.model.ScreenSyncConfig
import com.example.model.ScreenSyncDebugInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.ui.components.PixelOutlinedButton
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.Strings
import com.example.model.AppLanguage
import com.example.model.AppSettings
import com.example.model.AppThemeMode
import com.example.model.QualityLevel
import com.example.model.UiTheme
import com.example.model.AodConfig
import com.example.model.AodTheme
import com.example.model.AodClockType
import com.example.model.AodClockSize
import com.example.model.AodClockPosition
import com.example.model.AodClockStyle
import com.example.model.AodClockWeight
import com.example.model.AodEdgeGlowPosition
import com.example.model.EffectEngineDiagnostic
import com.example.model.EffectType
import com.example.model.HardwareMode
import com.example.model.HardwareStatus
import com.example.model.LedState
import com.example.model.RootStatus
import com.example.ui.components.LiquidGlassAppearanceSlider
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassQualitySelector
import com.example.ui.components.LiquidGlassSliderItem
import com.example.ui.components.LiquidGlassThemeSelector
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelCard
import com.example.ui.components.PixelSectionTitle
import com.example.ui.components.PixelSliderItem
import com.example.ui.components.PixelStatusDot
import com.example.ui.components.PixelSwitchItem
import com.example.ui.components.PixelTonalButton
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import kotlin.math.roundToInt

enum class SettingsSubMenu(val titleKey: String, val icon: ImageVector) {
    GENERAL("general", Icons.Default.Tune),
    AOD("aod_title", Icons.Default.AutoAwesome),
    ROOT("root_access", Icons.Default.Memory),
    SYNC("screen_sync", Icons.Default.Sync),
    ABOUT("about", Icons.Default.Info)
}

@Composable
fun SettingsScreen(
    appSettings: AppSettings,
    ledState: LedState,
    hardwareStatus: HardwareStatus,
    customBackgroundBitmap: ImageBitmap? = null,
    onPickCustomBackground: () -> Unit = {},
    onBlurIntensityChange: (Float) -> Unit = {},
    onBackgroundOverlayChange: (Float) -> Unit = {},
    onResetBackground: () -> Unit = {},
    onDynamicColorChange: (Boolean) -> Unit,
    onVibrationFeedbackChange: (Boolean) -> Unit = {},
    onStartOnBootChange: (Boolean) -> Unit,
    onScreenColorChange: (Boolean) -> Unit,
    onSamplingRateChange: (Int) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onSmoothingChange: (Float) -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onDefaultEffectChange: (EffectType) -> Unit,
    onOpenLanguageDialog: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onThemeModeChange: (AppThemeMode) -> Unit = {},
    onUiThemeChange: (UiTheme) -> Unit = {},
    onQualityLevelChange: (QualityLevel) -> Unit = {},
    onUpdateAodConfig: ((AodConfig) -> AodConfig) -> Unit = {},
    onTestAodNotification: () -> Unit = {},
    onRequestRootDialog: () -> Unit,
    onRunHardwareTest: () -> Unit,
    onCheckRoot: () -> Unit = {},
    onRequestRoot: () -> Unit = {},
    onTestLedAccess: () -> Unit = onRunHardwareTest,
    onKeepLedOnScreenOffChange: (Boolean) -> Unit = {},
    onScreenSyncIntensityChange: (Float) -> Unit = {},
    onScreenSyncSmoothingChange: (Float) -> Unit = {},
    onScreenSyncColorBoostChange: (Boolean) -> Unit = {},
    onScreenSyncSamplingRateChange: (Int) -> Unit = {},
    onScreenSyncRegionChange: (ScreenRegion) -> Unit = {},
    onScreenSyncKeepColorOnScreenOffChange: (Boolean) -> Unit = {},
    onRequestMediaProjection: () -> Unit = {},
    screenSyncDebugInfo: ScreenSyncDebugInfo? = null,
    effectDiagnostic: EffectEngineDiagnostic = EffectEngineDiagnostic(),
    onTestEffect: (EffectType) -> Unit = {},
    onRunPipelineColorTest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val language = appSettings.language
    val isDark = isSystemInDarkTheme()
    val subMenus = remember { SettingsSubMenu.entries }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { subMenus.size }
    )
    val coroutineScope = rememberCoroutineScope()
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        Text(
            text = Strings.get("settings", language),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // iOS 26 Liquid Glass Segmented Menu with Swipe Navigation
        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
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
                testTag = "settings_segmented_menu"
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            pageSpacing = 12.dp,
            beyondViewportPageCount = 1
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .m3PageTransition(pagerState, page, scaleDown = 0.05f, alphaFade = 0.40f, tiltAngle = 3f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                when (subMenus[page]) {
                    SettingsSubMenu.GENERAL -> {
                        // ================= GENERAL =================
                        PixelSectionTitle(title = Strings.get("general", language))
                        LiquidGlassCard {
            Column {
                // Language
                SettingClickableRow(
                    icon = Icons.Default.Language,
                    title = Strings.get("language", language),
                    subtitle = language.nativeName,
                    onClick = onOpenLanguageDialog,
                    testTag = "setting_language_row"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Appearance: Requirement 5 (Three-State Liquid Glass Appearance Slider)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = Strings.get("appearance", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LiquidGlassAppearanceSlider(
                        selectedMode = appSettings.themeMode,
                        onModeSelected = onThemeModeChange,
                        language = language
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // UI Theme: Requirement 6, 7, 8 (NEWBIEUI26 / ULTIMATE NEWBIE LIQUID GLASS)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = Strings.get("ui_theme", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = Strings.get("ui_theme_desc", language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LiquidGlassThemeSelector(
                        selectedTheme = appSettings.uiTheme,
                        onThemeSelected = onUiThemeChange,
                        language = language
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Liquid Glass Quality / Performance Presets
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = Strings.get("quality_level", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = Strings.get("quality_level_desc", language),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LiquidGlassQualitySelector(
                        selectedQuality = appSettings.qualityLevel,
                        onQualitySelected = onQualityLevelChange,
                        language = language
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Dynamic Color (Section 57, 60)
                PixelSwitchItem(
                    title = Strings.get("dynamic_color", language),
                    description = Strings.get("dynamic_color_desc", language),
                    checked = appSettings.dynamicColor,
                    onCheckedChange = onDynamicColorChange,
                    icon = Icons.Default.Palette,
                    testTag = "setting_dynamic_color_switch"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                // Tactile Vibration Feedback
                PixelSwitchItem(
                    title = Strings.get("vibration_feedback", language),
                    description = Strings.get("vibration_feedback_desc", language),
                    checked = appSettings.vibrationFeedback,
                    onCheckedChange = onVibrationFeedbackChange,
                    icon = Icons.Default.Vibration,
                    testTag = "setting_vibration_feedback_switch"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= CUSTOM BACKGROUND =================
        PixelSectionTitle(title = Strings.get("custom_background", language))
        LiquidGlassCard(
            hasBackground = appSettings.customBackgroundPath != null,
            testTag = "custom_background_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Top row with title & upload button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Strings.get("custom_background", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = if (appSettings.customBackgroundPath != null) {
                                "Custom wallpaper active"
                            } else {
                                Strings.get("no_custom_bg", language)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    LiquidGlassButton(
                        text = Strings.get("upload_image", language),
                        onClick = onPickCustomBackground,
                        icon = Icons.Default.AddPhotoAlternate,
                        testTag = "btn_upload_background"
                    )
                }

                // Preview Box (Actual uploaded image with real-time blur & overlay)
                Text(
                    text = Strings.get("preview", language),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                val isDarkTheme = when (appSettings.themeMode) {
                    AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                    AppThemeMode.LIGHT -> false
                    AppThemeMode.DARK -> true
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            RoundedCornerShape(18.dp)
                        )
                ) {
                    if (customBackgroundBitmap != null) {
                        // 1. Background image with real-time blur
                        Image(
                            bitmap = customBackgroundBitmap,
                            contentDescription = Strings.get("preview", language),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // 2. Dark / Light overlay simulation
                        val overlayAlpha = if (isDarkTheme) {
                            (appSettings.backgroundOverlay * 0.85f).coerceIn(0f, 0.95f)
                        } else {
                            (appSettings.backgroundOverlay * 0.70f).coerceIn(0f, 0.90f)
                        }
                        val overlayColor = if (isDarkTheme) Color.Black else Color.White

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(overlayColor.copy(alpha = overlayAlpha))
                        )

                        // 3. UI sharpness sample overlay inside preview
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "540×960 Display Area",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Blur: ${(appSettings.backgroundBlurIntensity * 100).roundToInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            // Simulated card to verify UI text stays sharp
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.90f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Sample UI Card — Text Remains Sharp",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    } else {
                        // Empty placeholder state
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallpaper,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = Strings.get("no_custom_bg", language),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }

                if (appSettings.customBackgroundPath != null) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Blur Intensity Slider: 0% ─────────●── 100%
                    LiquidGlassSliderItem(
                        title = Strings.get("blur_intensity", language),
                        value = appSettings.backgroundBlurIntensity,
                        onValueChange = onBlurIntensityChange,
                        valueRange = 0f..1f,
                        icon = Icons.Default.BlurOn,
                        displayFormatter = { "${(it * 100).roundToInt()}%" },
                        testTag = "setting_blur_intensity_slider"
                    )

                    // Background Overlay Slider: 0% ─────────●── 100%
                    LiquidGlassSliderItem(
                        title = Strings.get("background_overlay", language),
                        value = appSettings.backgroundOverlay,
                        onValueChange = onBackgroundOverlayChange,
                        valueRange = 0f..1f,
                        icon = Icons.Default.Opacity,
                        displayFormatter = { "${(it * 100).roundToInt()}%" },
                        testTag = "setting_background_overlay_slider"
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Reset Background
                    PixelOutlinedButton(
                        text = Strings.get("reset_background", language),
                        onClick = { showResetConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.DeleteOutline,
                        testTag = "btn_reset_background"
                    )
                }
            }
        }

            Spacer(modifier = Modifier.height(20.dp))

            // ================= STARTUP =================
            PixelSectionTitle(title = Strings.get("startup", language))
            LiquidGlassCard {
                PixelSwitchItem(
                    title = Strings.get("start_last_effect_boot", language),
                    checked = appSettings.startOnBoot,
                    onCheckedChange = onStartOnBootChange,
                    icon = Icons.Default.PowerSettingsNew,
                    testTag = "setting_start_boot_switch"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        SettingsSubMenu.AOD -> {
            // ================= ALWAYS ON DISPLAY =================
            PixelSectionTitle(title = Strings.get("aod_title", language))
            Text(
                text = Strings.get("aod_enable_desc", language),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Master Switch & Quick Preview
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PixelSwitchItem(
                        title = Strings.get("aod_enable", language),
                        description = Strings.get("aod_enable_desc", language),
                        checked = appSettings.aodConfig.isEnabled,
                        onCheckedChange = { isEnabled ->
                            onUpdateAodConfig { it.copy(isEnabled = isEnabled) }
                        },
                        icon = Icons.Default.AutoAwesome,
                        testTag = "setting_aod_master_switch"
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    LiquidGlassButton(
                        text = Strings.get("aod_preview_fullscreen", language),
                        onClick = onTestAodNotification,
                        icon = Icons.Default.PlayArrow,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_test_aod_preview"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // AOD Themes (10 Themes)
            PixelSectionTitle(title = Strings.get("aod_themes", language))
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val themes = remember { AodTheme.entries }
                    themes.chunked(2).forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowThemes.forEach { theme ->
                                val isSelected = appSettings.aodConfig.theme == theme
                                val primaryColor = MaterialTheme.colorScheme.primary
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (isSelected) primaryColor.copy(alpha = 0.22f)
                                            else Color.White.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable {
                                            onUpdateAodConfig { it.copy(theme = theme) }
                                        }
                                        .padding(vertical = 12.dp, horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = Strings.get(theme.titleKey, language),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            if (rowThemes.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Clock Customization
            PixelSectionTitle(title = Strings.get("aod_clock_customization", language))
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Clock Type: Digital vs Analog
                    Text(
                        text = Strings.get("aod_clock_type", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AodClockType.entries.forEach { cType ->
                            val isSelected = appSettings.aodConfig.clockType == cType
                            val primaryColor = MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.22f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        onUpdateAodConfig { it.copy(clockType = cType) }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get(cType.titleKey, language),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Clock Render Style
                    Text(
                        text = Strings.get("aod_clock_style", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AodClockStyle.entries.forEach { cStyle ->
                            val isSelected = appSettings.aodConfig.clockStyle == cStyle
                            val primaryColor = MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.22f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onUpdateAodConfig { it.copy(clockStyle = cStyle) }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get(cStyle.titleKey, language).split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Clock Typographic Weight
                    Text(
                        text = Strings.get("aod_clock_weight", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AodClockWeight.entries.forEach { weight ->
                            val isSelected = appSettings.aodConfig.clockWeight == weight
                            val primaryColor = MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.22f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onUpdateAodConfig { it.copy(clockWeight = weight) }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get(weight.titleKey, language),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Clock Size
                    Text(
                        text = Strings.get("aod_clock_size", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AodClockSize.entries.forEach { size ->
                            val isSelected = appSettings.aodConfig.clockSize == size
                            val primaryColor = MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.22f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onUpdateAodConfig { it.copy(clockSize = size) }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get(size.titleKey, language).split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    // Liquid Glass Optical Customization sliders
                    if (appSettings.aodConfig.clockStyle == AodClockStyle.LIQUID_GLASS || appSettings.aodConfig.theme == AodTheme.LIQUID_GLASS) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        LiquidGlassSliderItem(
                            title = Strings.get("aod_clock_opacity", language),
                            value = appSettings.aodConfig.clockOpacity,
                            onValueChange = { op -> onUpdateAodConfig { it.copy(clockOpacity = op) } },
                            valueRange = 0.2f..1.0f,
                            icon = Icons.Default.Opacity,
                            displayFormatter = { "${(it * 100).roundToInt()}%" }
                        )
                        LiquidGlassSliderItem(
                            title = Strings.get("aod_clock_blur", language),
                            value = appSettings.aodConfig.clockBlur,
                            onValueChange = { bl -> onUpdateAodConfig { it.copy(clockBlur = bl) } },
                            valueRange = 0.0f..1.0f,
                            icon = Icons.Default.BlurOn,
                            displayFormatter = { "${(it * 100).roundToInt()}%" }
                        )
                        LiquidGlassSliderItem(
                            title = Strings.get("aod_clock_refraction", language),
                            value = appSettings.aodConfig.clockRefraction,
                            onValueChange = { rf -> onUpdateAodConfig { it.copy(clockRefraction = rf) } },
                            valueRange = 0.0f..1.0f,
                            icon = Icons.Default.AutoAwesome,
                            displayFormatter = { "${(it * 100).roundToInt()}%" }
                        )
                        LiquidGlassSliderItem(
                            title = Strings.get("aod_clock_gloss", language),
                            value = appSettings.aodConfig.clockGloss,
                            onValueChange = { gl -> onUpdateAodConfig { it.copy(clockGloss = gl) } },
                            valueRange = 0.0f..1.0f,
                            icon = Icons.Default.Lightbulb,
                            displayFormatter = { "${(it * 100).roundToInt()}%" }
                        )
                        LiquidGlassSliderItem(
                            title = Strings.get("aod_clock_glow", language),
                            value = appSettings.aodConfig.clockGlow,
                            onValueChange = { gw -> onUpdateAodConfig { it.copy(clockGlow = gw) } },
                            valueRange = 0.0f..1.0f,
                            icon = Icons.Default.FormatPaint,
                            displayFormatter = { "${(it * 100).roundToInt()}%" }
                        )
                        LiquidGlassSliderItem(
                            title = Strings.get("aod_clock_thickness", language),
                            value = appSettings.aodConfig.clockThickness,
                            onValueChange = { tk -> onUpdateAodConfig { it.copy(clockThickness = tk) } },
                            valueRange = 0.0f..1.0f,
                            icon = Icons.Default.Tune,
                            displayFormatter = { "${(it * 100).roundToInt()}%" }
                        )
                        PixelSwitchItem(
                            title = Strings.get("aod_bg_interaction", language),
                            checked = appSettings.aodConfig.backgroundInteraction,
                            onCheckedChange = { inter -> onUpdateAodConfig { it.copy(backgroundInteraction = inter) } },
                            icon = Icons.Default.Wallpaper
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Clock Position (Top vs Center)
                    Text(
                        text = Strings.get("aod_clock_position", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AodClockPosition.entries.forEach { pos ->
                            val isSelected = appSettings.aodConfig.clockPosition == pos
                            val primaryColor = MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.22f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        onUpdateAodConfig { it.copy(clockPosition = pos) }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get(pos.titleKey, language),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // 24-Hour Format
                    PixelSwitchItem(
                        title = Strings.get("aod_24h", language),
                        checked = appSettings.aodConfig.is24Hour,
                        onCheckedChange = { is24 ->
                            onUpdateAodConfig { it.copy(is24Hour = is24) }
                        },
                        icon = Icons.Default.Tune
                    )

                    // Auto Color Sync
                    PixelSwitchItem(
                        title = Strings.get("aod_auto_color", language),
                        checked = appSettings.aodConfig.isAutoColor,
                        onCheckedChange = { autoColor ->
                            onUpdateAodConfig { it.copy(isAutoColor = autoColor) }
                        },
                        icon = Icons.Default.Palette
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date & Battery Telemetry
            PixelSectionTitle(title = Strings.get("aod_battery", language))
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelSwitchItem(
                        title = Strings.get("aod_show_date", language),
                        checked = appSettings.aodConfig.showDate,
                        onCheckedChange = { showDate ->
                            onUpdateAodConfig { it.copy(showDate = showDate) }
                        },
                        icon = Icons.Default.Info
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    PixelSwitchItem(
                        title = Strings.get("aod_battery_percent", language),
                        checked = appSettings.aodConfig.showBatteryPercentage,
                        onCheckedChange = { showPct ->
                            onUpdateAodConfig { it.copy(showBatteryPercentage = showPct) }
                        },
                        icon = Icons.Default.Speed
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    PixelSwitchItem(
                        title = Strings.get("aod_battery_icon", language),
                        checked = appSettings.aodConfig.showBatteryIcon,
                        onCheckedChange = { showIcon ->
                            onUpdateAodConfig { it.copy(showBatteryIcon = showIcon) }
                        },
                        icon = Icons.Default.Animation
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notification Indicators
            PixelSectionTitle(title = Strings.get("aod_notifications", language))
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PixelSwitchItem(
                        title = Strings.get("aod_notifications", language),
                        description = Strings.get("aod_notifications_desc", language),
                        checked = appSettings.aodConfig.showNotificationIcons,
                        onCheckedChange = { showIcons ->
                            onUpdateAodConfig { it.copy(showNotificationIcons = showIcons) }
                        },
                        icon = Icons.Default.Notifications
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    PixelSwitchItem(
                        title = Strings.get("aod_color_auto", language),
                        description = Strings.get("aod_color_notification", language),
                        checked = appSettings.aodConfig.useAppColorForNotification,
                        onCheckedChange = { useAppColor ->
                            onUpdateAodConfig { it.copy(useAppColorForNotification = useAppColor) }
                        },
                        icon = Icons.Default.Palette
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Edge Glow Illumination
            PixelSectionTitle(title = Strings.get("aod_edge_glow", language))
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PixelSwitchItem(
                        title = Strings.get("aod_edge_glow", language),
                        description = Strings.get("aod_edge_glow_desc", language),
                        checked = appSettings.aodConfig.edgeGlow.isEnabled,
                        onCheckedChange = { isGlow ->
                            onUpdateAodConfig { it.copy(edgeGlow = it.edgeGlow.copy(isEnabled = isGlow)) }
                        },
                        icon = Icons.Default.AutoAwesome
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    PixelSwitchItem(
                        title = Strings.get("aod_edge_rgb", language),
                        checked = appSettings.aodConfig.edgeGlow.isRgbCycle,
                        onCheckedChange = { isCycle ->
                            onUpdateAodConfig { it.copy(edgeGlow = it.edgeGlow.copy(isRgbCycle = isCycle)) }
                        },
                        icon = Icons.Default.Palette
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Edge Position
                    Text(
                        text = Strings.get("aod_edge_position", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AodEdgeGlowPosition.entries.forEach { pos ->
                            val isSelected = appSettings.aodConfig.edgeGlow.position == pos
                            val primaryColor = MaterialTheme.colorScheme.primary
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) primaryColor.copy(alpha = 0.22f)
                                        else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onUpdateAodConfig { it.copy(edgeGlow = it.edgeGlow.copy(position = pos)) }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get(pos.titleKey, language).split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    LiquidGlassSliderItem(
                        title = Strings.get("aod_edge_thickness", language),
                        value = appSettings.aodConfig.edgeGlow.thickness,
                        onValueChange = { thickness ->
                            onUpdateAodConfig { it.copy(edgeGlow = it.edgeGlow.copy(thickness = thickness)) }
                        },
                        valueRange = 2f..16f,
                        icon = Icons.Default.Tune,
                        displayFormatter = { "${it.roundToInt()} dp" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
        SettingsSubMenu.ROOT -> {
            // ================= ROOT ACCESS =================
            PixelSectionTitle(title = Strings.get("root_access", language))
            Text(
                text = Strings.get("root_access_desc", language),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            LiquidGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Card header: 🔑 Root Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🔑",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.get("root_status_title", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    // Root Granted vs Root Not Granted status badge
                    val isGranted = hardwareStatus.rootStatus == RootStatus.ROOT_GRANTED
                    val statusText = when (hardwareStatus.rootStatus) {
                        RootStatus.ROOT_GRANTED -> Strings.get("root_granted", language)
                        RootStatus.ROOT_DENIED -> Strings.get("root_denied", language)
                        RootStatus.ROOT_NOT_FOUND -> Strings.get("root_not_found", language)
                        RootStatus.ROOT_ERROR -> Strings.get("root_error", language)
                        RootStatus.ROOT_REVOKED -> Strings.get("root_revoked", language)
                        else -> Strings.get("root_not_granted", language)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isGranted)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isGranted)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }

                // UID & Provider info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UID",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = if (hardwareStatus.uid != null) "${hardwareStatus.uid} (Root)" else "None",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hardwareStatus.uid == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Strings.get("provider_label", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = hardwareStatus.rootProvider,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                // Mode Badge: ROOT MODE / NON-ROOT MODE / HARDWARE UNAVAILABLE
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hardware Mode",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    val modeKey = when (hardwareStatus.hardwareMode) {
                        HardwareMode.ROOT -> "root_mode"
                        HardwareMode.NON_ROOT -> "non_root_mode"
                        HardwareMode.UNAVAILABLE -> "hardware_unavailable"
                    }
                    Text(
                        text = Strings.get(modeKey, language),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = when (hardwareStatus.hardwareMode) {
                                HardwareMode.ROOT -> MaterialTheme.colorScheme.primary
                                HardwareMode.NON_ROOT -> MaterialTheme.colorScheme.tertiary
                                HardwareMode.UNAVAILABLE -> MaterialTheme.colorScheme.outline
                            }
                        )
                    )
                }

                // Driver & Channels detail
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Strings.get("driver_chip", language),
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = hardwareStatus.chipModel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Strings.get("channel_red", language),
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = if (hardwareStatus.isRedAvailable) Strings.get("channel_available", language) else Strings.get("channel_unavailable", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hardwareStatus.isRedAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Strings.get("channel_green", language),
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = if (hardwareStatus.isGreenAvailable) Strings.get("channel_available", language) else Strings.get("channel_unavailable", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hardwareStatus.isGreenAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Strings.get("channel_blue", language),
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = if (hardwareStatus.isBlueAvailable) Strings.get("channel_available", language) else Strings.get("channel_unavailable", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (hardwareStatus.isBlueAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Buttons: [ REQUEST ROOT ] [ CHECK ROOT ] [ TEST ROOT LED ]
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PixelTonalButton(
                            text = Strings.get("request_root", language),
                            onClick = onRequestRoot,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Security,
                            testTag = "btn_request_root"
                        )

                        PixelTonalButton(
                            text = Strings.get("check_root", language),
                            onClick = onCheckRoot,
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Refresh,
                            testTag = "btn_check_root"
                        )
                    }

                    PixelButton(
                        text = Strings.get("test_led_access", language),
                        onClick = onTestLedAccess,
                        enabled = !hardwareStatus.isHardwareTestRunning,
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.PlayArrow,
                        testTag = "btn_test_root_led"
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Screen Off Partial WakeLock toggle
                PixelSwitchItem(
                    title = Strings.get("keep_led_screen_off", language),
                    description = Strings.get("keep_led_screen_off_desc", language),
                    checked = appSettings.keepLedOnScreenOff,
                    onCheckedChange = onKeepLedOnScreenOffChange,
                    icon = Icons.Default.PowerSettingsNew,
                    testTag = "setting_screen_off_wakelock_switch"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= HARDWARE EFFECT TEST MODE =================
        PixelSectionTitle(title = "Hardware Effect Test Mode")
        LiquidGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Directly exercises continuous hardware animation and sysfs writes to physical LEDs.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Diagnostic Status Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (effectDiagnostic.isRunning) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (effectDiagnostic.isRunning) "RUNNING" else "STOPPED",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (effectDiagnostic.isRunning) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Effect: ${effectDiagnostic.effect.name}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Frame #${effectDiagnostic.frameCount} • ${effectDiagnostic.currentFps} FPS",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    // Hardware RGB Color Swatch
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(effectDiagnostic.currentRgb.toComposeColor())
                                .border(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = effectDiagnostic.currentRgb.toHex(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Section 25: 6-Color Pipeline Test Button
                PixelButton(
                    text = "6-Color Pipeline Test (Test 25)",
                    onClick = onRunPipelineColorTest,
                    enabled = !hardwareStatus.isHardwareTestRunning,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.PlayArrow,
                    testTag = "btn_run_pipeline_test"
                )

                Text(
                    text = "Direct Effect Hardware Triggers:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                val testEffects = listOf(
                    EffectType.STATIC,
                    EffectType.BREATHING,
                    EffectType.HEARTBEAT,
                    EffectType.WAVE,
                    EffectType.PULSE,
                    EffectType.RAINBOW,
                    EffectType.STROBE,
                    EffectType.SCREEN_SYNC
                )

                for (row in testEffects.chunked(2)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (eff in row) {
                            val isCurrent = effectDiagnostic.effect == eff && effectDiagnostic.isRunning
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isCurrent) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onTestEffect(eff) }
                                    .padding(vertical = 10.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = eff.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

            Spacer(modifier = Modifier.height(24.dp))
        }
        SettingsSubMenu.SYNC -> {
            // ================= EFFECTS =================
            PixelSectionTitle(title = Strings.get("effects", language))
            LiquidGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Default Effect Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("default_effect", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = Strings.get(appSettings.defaultEffect.displayNameKey, language),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                // Brightness
                PixelSliderItem(
                    title = Strings.get("brightness", language),
                    value = ledState.brightness,
                    onValueChange = onBrightnessChange,
                    valueRange = 0.05f..1.0f,
                    icon = Icons.Default.Lightbulb,
                    displayFormatter = { "${(it * 100).roundToInt()}%" },
                    testTag = "setting_brightness_slider"
                )

                // Speed
                PixelSliderItem(
                    title = Strings.get("speed", language),
                    value = ledState.effectSpeed,
                    onValueChange = onSpeedChange,
                    valueRange = 0.2f..3.0f,
                    icon = Icons.Default.Speed,
                    displayFormatter = { String.format("%.1fx", it) },
                    testTag = "setting_speed_slider"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ================= SCREEN SYNC =================
        val isScreenSyncActive = ledState.currentEffect == EffectType.SCREEN_SYNC && ledState.isRunning
        PixelSectionTitle(title = Strings.get("screen_sync", language) + " / Ambilight")
        LiquidGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelSwitchItem(
                    title = Strings.get("screen_sync_active", language),
                    description = "Analyze screen colors in real time and mirror to RGB LED",
                    checked = isScreenSyncActive,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            if (!MediaProjectionHolder.hasPermission) {
                                onRequestMediaProjection()
                            } else {
                                onDefaultEffectChange(EffectType.SCREEN_SYNC)
                            }
                        } else {
                            onDefaultEffectChange(EffectType.STATIC)
                        }
                    },
                    icon = Icons.Default.Tv,
                    testTag = "setting_screen_sync_switch"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Sync Intensity slider (0.05f .. 1.0f)
                PixelSliderItem(
                    title = Strings.get("screen_sync_intensity", language),
                    value = appSettings.screenSyncConfig.intensity,
                    onValueChange = onScreenSyncIntensityChange,
                    valueRange = 0.05f..1.0f,
                    displayFormatter = { "${(it * 100).roundToInt()}%" },
                    testTag = "setting_screen_sync_intensity_slider"
                )

                // Smoothing slider (0f .. 1f)
                PixelSliderItem(
                    title = Strings.get("smoothing", language),
                    value = appSettings.screenSyncConfig.smoothing,
                    onValueChange = onScreenSyncSmoothingChange,
                    valueRange = 0f..1f,
                    displayFormatter = { "${(it * 100).roundToInt()}%" },
                    testTag = "setting_screen_sync_smoothing_slider"
                )

                // Sampling Rate slider (10 .. 20 Hz, optimized for MT6735M)
                PixelSliderItem(
                    title = Strings.get("screen_sync_rate", language),
                    value = appSettings.screenSyncConfig.samplingRate.toFloat(),
                    onValueChange = { onScreenSyncSamplingRateChange(it.roundToInt()) },
                    valueRange = 10f..20f,
                    steps = 9,
                    displayFormatter = { "${it.roundToInt()} FPS" },
                    testTag = "setting_screen_sync_rate_slider"
                )

                // Color Boost switch
                PixelSwitchItem(
                    title = Strings.get("screen_sync_boost", language),
                    description = Strings.get("screen_sync_boost_desc", language),
                    checked = appSettings.screenSyncConfig.colorBoost,
                    onCheckedChange = onScreenSyncColorBoostChange,
                    icon = Icons.Default.AutoAwesome,
                    testTag = "setting_screen_sync_color_boost_switch"
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Sampling Region selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = Strings.get("screen_sync_region", language),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ScreenRegion.entries.forEach { region ->
                            val isSelected = appSettings.screenSyncConfig.region == region
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainerHigh
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onScreenSyncRegionChange(region) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get(region.displayNameKey, language),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )

                // Keep color when screen is off
                PixelSwitchItem(
                    title = Strings.get("screen_sync_keep_off", language),
                    description = Strings.get("screen_sync_keep_off_desc", language),
                    checked = appSettings.screenSyncConfig.keepColorOnScreenOff,
                    onCheckedChange = onScreenSyncKeepColorOnScreenOffChange,
                    icon = Icons.Default.PowerSettingsNew,
                    testTag = "setting_screen_sync_keep_off_switch"
                )
            }
        }

        // Live Ambilight Diagnostics Card
        val debug = screenSyncDebugInfo ?: ledState.screenSyncDebugInfo
        if (debug.isRunning || isScreenSyncActive) {
            Spacer(modifier = Modifier.height(14.dp))
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.get("screen_sync_debug", language),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (debug.isRunning) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (debug.isRunning) "CAPTURING" else "IDLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (debug.isRunning) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Detected screen color swatch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(debug.detectedColor.toComposeColor())
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = Strings.get("detected_color", language),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = debug.detectedColor.toHex(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Physical LED color swatch
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(debug.smoothedLedColor.toComposeColor())
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = Strings.get("smoothed_led_color", language),
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                                Text(
                                    text = debug.smoothedLedColor.toHex(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // FPS counter
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Strings.get("fps_counter", language),
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                            Text(
                                text = "${debug.currentFps} FPS",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }

            Spacer(modifier = Modifier.height(24.dp))
        }
        SettingsSubMenu.ABOUT -> {
            // ================= ABOUT =================
            PixelSectionTitle(title = Strings.get("about", language))
            LiquidGlassCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("app_title", language),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = Strings.get("app_version", language),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.get("author_label", language),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = Strings.get("author", language),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Package",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "com.koukou.strikergbcrossdroid",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minimum Android",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "2.1 / API 7 (CrossDroid)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LiquidGlassTheme.surfaceBrush(isDark))
                        .border(1.dp, LiquidGlassTheme.borderBrush(isDark), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = Strings.get("design_identity", language),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }
}

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = Strings.get("reset_background_confirm", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(
                    text = Strings.get("reset_background_desc", language),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirmDialog = false
                        onResetBackground()
                    }
                ) {
                    Text(
                        text = Strings.get("reset_background", language),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text(text = Strings.get("cancel", language))
                }
            }
        )
    }
}

@Composable
private fun SettingClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    testTag: String = "setting_row"
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
