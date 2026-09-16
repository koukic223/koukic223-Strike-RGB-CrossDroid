package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.localization.Strings
import com.example.model.AppLanguage
import com.example.model.AppSettings
import com.example.model.AppThemeMode
import com.example.model.HardwareStatus
import com.example.model.LedState
import com.example.model.RgbColor
import com.example.model.RootStatus
import com.example.model.UiTheme
import com.example.ui.background.CustomBackgroundManager
import com.example.ui.components.AodDisplayOverlay
import com.example.ui.components.LiquidGlassNavigationBar
import com.example.ui.components.M3MotionDefaults
import com.example.ui.components.PixelDialog
import com.example.ui.components.m3PageTransition
import com.example.viewmodel.RgbViewModel
import kotlinx.coroutines.launch

enum class ScreenTab(
    val titleKey: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", Icons.Filled.Home, Icons.Outlined.Home),
    COLOR("rgb", Icons.Filled.Palette, Icons.Outlined.Palette),
    EFFECTS("effects", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    LIGHT_FLOW("light_flow", Icons.Filled.NotificationsActive, Icons.Outlined.Notifications),
    AOD("aod", Icons.Filled.DisplaySettings, Icons.Outlined.DisplaySettings),
    SETTINGS("settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    viewModel: RgbViewModel,
    ledState: LedState,
    hardwareStatus: HardwareStatus,
    appSettings: AppSettings,
    statusMessage: String?,
    modifier: Modifier = Modifier
) {
    val tabs = remember { ScreenTab.entries }
    var selectedTab by remember { mutableStateOf(ScreenTab.HOME) }
    val pagerState = rememberPagerState(
        initialPage = tabs.indexOf(selectedTab).coerceIn(0, tabs.size - 1),
        pageCount = { tabs.size }
    )

    // Synchronize horizontal swipe gestures with selectedTab
    LaunchedEffect(pagerState.currentPage) {
        if (tabs[pagerState.currentPage] != selectedTab) {
            selectedTab = tabs[pagerState.currentPage]
        }
    }

    // Synchronize selectedTab changes with pager animation
    LaunchedEffect(selectedTab) {
        val targetIndex = tabs.indexOf(selectedTab)
        if (pagerState.currentPage != targetIndex) {
            pagerState.animateScrollToPage(targetIndex)
        }
    }

    var showRootDialog by remember { mutableStateOf(false) }
    var showUnsupportedDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val effectDiagnostic by viewModel.effectDiagnostic.collectAsStateWithLifecycle()
    val aodDisplayState by viewModel.aodDisplayState.collectAsStateWithLifecycle()
    val language = appSettings.language
    val layoutDirection = Strings.getLayoutDirection(language)
    val context = LocalContext.current

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            viewModel.onMediaProjectionGranted(result.resultCode, result.data)
            scope.launch {
                snackbarHostState.showSnackbar(Strings.get("screen_sync_active", language))
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(Strings.get("screen_sync_permission_denied", language))
            }
        }
    }

    val requestMediaProjection = {
        val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
        if (mediaProjectionManager != null) {
            val intent = mediaProjectionManager.createScreenCaptureIntent()
            mediaProjectionLauncher.launch(intent)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.setCustomBackgroundUri(uri) { success ->
                scope.launch {
                    val msg = if (success) {
                        Strings.get("applied_toast", language)
                    } else {
                        "Failed to load image"
                    }
                    snackbarHostState.showSnackbar(msg)
                }
            }
        }
    }

    val cacheVersion by CustomBackgroundManager.cacheVersion.collectAsStateWithLifecycle()
    val customBackgroundBitmap = remember(cacheVersion, appSettings.backgroundBlurIntensity, appSettings.customBackgroundPath) {
        if (appSettings.customBackgroundPath != null) {
            CustomBackgroundManager.getBitmapForIntensity(appSettings.backgroundBlurIntensity)
        } else {
            null
        }
    }

    // Check screen height / width for compact screens like 540x960 (BQ Aquaris A4.5)
    val configuration = LocalConfiguration.current
    val isCompactScreen = configuration.screenHeightDp < 700 || configuration.screenWidthDp <= 360

    val isDarkTheme = when (appSettings.themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val appBackgroundModifier = if (customBackgroundBitmap != null) {
        Modifier.background(if (isDarkTheme) Color(0xFF0F1117) else Color(0xFFF6F8FA))
    } else if (appSettings.uiTheme == UiTheme.XML_THEME) {
        Modifier.background(if (isDarkTheme) Color(0xFF0E121A) else Color(0xFFF3F5F9))
    } else {
        Modifier.background(
            Brush.verticalGradient(
                colors = if (isDarkTheme) {
                    listOf(Color(0xFF141824), Color(0xFF0D1017), Color(0xFF0A0C11))
                } else {
                    listOf(Color(0xFFF8FAFD), Color(0xFFEFF3F9), Color(0xFFE5ECF4))
                }
            )
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(appBackgroundModifier)
        ) {
            // Global Custom Background Wallpaper layer if active — spans entire screen under floating nav bar
            if (customBackgroundBitmap != null) {
                val overlayAlpha = if (isDarkTheme) {
                    (appSettings.backgroundOverlay * 0.85f).coerceIn(0f, 0.95f)
                } else {
                    (appSettings.backgroundOverlay * 0.70f).coerceIn(0f, 0.90f)
                }
                val overlayColor = if (isDarkTheme) Color.Black else Color.White

                Image(
                    bitmap = customBackgroundBitmap!!,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(overlayColor.copy(alpha = overlayAlpha))
                )
            }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    // iOS 26 Liquid Glass floating bottom navigation bar with swipe navigation
                    LiquidGlassNavigationBar(
                        tabs = tabs,
                        selectedTab = selectedTab,
                        onTabSelected = { tab ->
                            selectedTab = tab
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = tabs.indexOf(tab),
                                    animationSpec = M3MotionDefaults.ScreenTransitionSpring
                                )
                            }
                        },
                        language = language,
                        pagerState = pagerState
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .statusBarsPadding()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        pageSpacing = 16.dp,
                        beyondViewportPageCount = 1
                    ) { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .m3PageTransition(pagerState, page)
                        ) {
                            when (tabs[page]) {
                        ScreenTab.HOME -> {
                            HomeScreen(
                                ledState = ledState,
                                hardwareStatus = hardwareStatus,
                                language = language,
                                onStart = {
                                    viewModel.startLed()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(Strings.get("applied_toast", language))
                                    }
                                },
                                onStop = { viewModel.stopLed() },
                                onRequestRootDialog = { showRootDialog = true },
                                uiTheme = appSettings.uiTheme,
                                onToggleLed = {
                                    val isRunning = viewModel.toggleLed()
                                    if (isRunning) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(Strings.get("applied_toast", language))
                                        }
                                    }
                                },
                                onColorSelect = { viewModel.setColor(it) }
                            )
                        }
                        ScreenTab.EFFECTS -> {
                            EffectsScreen(
                                ledState = ledState,
                                language = language,
                                onEffectSelect = { viewModel.setEffect(it) },
                                onBrightnessChange = { viewModel.setBrightness(it) },
                                onIntensityChange = { viewModel.setIntensity(it) },
                                onSpeedChange = { viewModel.setEffectSpeed(it) },
                                onBreathingSpeedChange = { viewModel.setBreathingSpeed(it) },
                                onSmoothTransitionChange = { viewModel.setSmoothTransition(it) },
                                onSaveLastEffectChange = { viewModel.setSaveLastEffect(it) },
                                onRequestMediaProjection = requestMediaProjection,
                                onApply = {
                                    viewModel.saveAll()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(Strings.get("applied_toast", language))
                                    }
                                },
                                onReset = {
                                    viewModel.resetSettings()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Effects reset to default")
                                    }
                                }
                            )
                        }
                        ScreenTab.COLOR -> {
                            ColorPickerScreen(
                                ledState = ledState,
                                language = language,
                                onColorChanged = { viewModel.setColor(it) },
                                onBrightnessChanged = { viewModel.setBrightness(it) },
                                onApply = {
                                    viewModel.saveAll()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(Strings.get("applied_toast", language))
                                    }
                                },
                                onSave = {
                                    viewModel.saveAll()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(Strings.get("saved_toast", language))
                                    }
                                },
                                onToggleLed = {
                                    val isRunning = viewModel.toggleLed()
                                    if (isRunning) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(Strings.get("applied_toast", language))
                                        }
                                    }
                                }
                            )
                        }
                        ScreenTab.LIGHT_FLOW -> {
                            LightFlowScreen(
                                viewModel = viewModel
                            )
                        }
                        ScreenTab.AOD -> {
                            AodScreen(
                                viewModel = viewModel
                            )
                        }
                        ScreenTab.SETTINGS -> {
                            SettingsScreen(
                                appSettings = appSettings,
                                ledState = ledState,
                                hardwareStatus = hardwareStatus,
                                customBackgroundBitmap = customBackgroundBitmap,
                                onPickCustomBackground = {
                                    imagePickerLauncher.launch("image/*")
                                },
                                onBlurIntensityChange = { viewModel.setBackgroundBlurIntensity(it) },
                                onBackgroundOverlayChange = { viewModel.setBackgroundOverlay(it) },
                                onResetBackground = {
                                    viewModel.resetCustomBackground()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(Strings.get("reset", language))
                                    }
                                },
                                onDynamicColorChange = { viewModel.setDynamicColor(it) },
                                onVibrationFeedbackChange = { viewModel.setVibrationFeedback(it) },
                                onStartOnBootChange = { viewModel.setStartOnBoot(it) },
                                onScreenColorChange = { viewModel.setScreenColor(it) },
                                onSamplingRateChange = { viewModel.setSamplingRate(it) },
                                onSensitivityChange = { viewModel.setSensitivity(it) },
                                onSmoothingChange = { viewModel.setSmoothing(it) },
                                onBrightnessChange = { viewModel.setBrightness(it) },
                                onSpeedChange = { viewModel.setEffectSpeed(it) },
                                onDefaultEffectChange = { viewModel.setDefaultEffect(it) },
                                onOpenLanguageDialog = { showLanguageDialog = true },
                                onOpenThemeDialog = { showThemeDialog = true },
                                onThemeModeChange = { viewModel.setThemeMode(it) },
                                onUiThemeChange = { viewModel.setUiTheme(it) },
                                onQualityLevelChange = { viewModel.setQualityLevel(it) },
                                onUpdateAodConfig = { viewModel.updateAodConfig(it) },
                                onTestAodNotification = { viewModel.testNotificationFlow() },
                                onRequestRootDialog = { showRootDialog = true },
                                onRunHardwareTest = {
                                    viewModel.runHardwareTest { result ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(result.message)
                                        }
                                    }
                                },
                                onCheckRoot = {
                                    viewModel.checkRoot { status ->
                                        scope.launch {
                                            val msg = when (status) {
                                                RootStatus.ROOT_GRANTED -> "✓ Root verified: UID 0"
                                                RootStatus.ROOT_DENIED -> "✕ Root permission denied"
                                                RootStatus.ROOT_NOT_FOUND -> "✕ su binary not found"
                                                RootStatus.ROOT_ERROR -> "✕ Root check failed"
                                                RootStatus.ROOT_REVOKED -> "✕ Root permission revoked"
                                                else -> "✕ Root Not Granted"
                                            }
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                },
                                onRequestRoot = {
                                    viewModel.requestRoot { status ->
                                        scope.launch {
                                            val msg = when (status) {
                                                RootStatus.ROOT_GRANTED -> "✓ Root granted (UID: 0)"
                                                RootStatus.ROOT_DENIED -> "✕ Root denied by Superuser"
                                                RootStatus.ROOT_NOT_FOUND -> "✕ su binary not found"
                                                RootStatus.ROOT_ERROR -> "✕ Root request failed"
                                                RootStatus.ROOT_REVOKED -> "✕ Root permission revoked"
                                                else -> "✕ Root Not Granted"
                                            }
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                },
                                onTestLedAccess = {
                                    viewModel.runHardwareTest { result ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(result.message)
                                        }
                                    }
                                },
                                onKeepLedOnScreenOffChange = { viewModel.setKeepLedOnScreenOff(it) },
                                onScreenSyncIntensityChange = { viewModel.setScreenSyncIntensity(it) },
                                onScreenSyncSmoothingChange = { viewModel.setScreenSyncSmoothing(it) },
                                onScreenSyncColorBoostChange = { viewModel.setScreenSyncColorBoost(it) },
                                onScreenSyncSamplingRateChange = { viewModel.setScreenSyncSamplingRate(it) },
                                onScreenSyncRegionChange = { viewModel.setScreenSyncRegion(it) },
                                onScreenSyncKeepColorOnScreenOffChange = { viewModel.setScreenSyncKeepColorOnScreenOff(it) },
                                onRequestMediaProjection = requestMediaProjection,
                                screenSyncDebugInfo = ledState.screenSyncDebugInfo,
                                effectDiagnostic = effectDiagnostic,
                                onTestEffect = { viewModel.testEffectDirect(it) },
                                onRunPipelineColorTest = {
                                    viewModel.runPipelineColorTest { result ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(result.message)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

                // Banner indicator when hardware test is actively cycling channels
                if (statusMessage != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = statusMessage,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // ================= DIALOGS (Section 65) =================

        // 1. Root Permission Dialog (Section 65)
        if (showRootDialog) {
            PixelDialog(
                title = Strings.get("root_dialog_title", language),
                message = Strings.get("root_dialog_msg", language),
                confirmText = Strings.get("request_root", language),
                onConfirm = {
                    showRootDialog = false
                    viewModel.requestRoot { status ->
                        scope.launch {
                            val msg = when (status) {
                                RootStatus.ROOT_GRANTED -> "✓ Root granted (UID: 0)"
                                RootStatus.ROOT_DENIED -> "✕ Root denied by Superuser"
                                RootStatus.ROOT_NOT_FOUND -> "✕ su binary not found"
                                RootStatus.ROOT_ERROR -> "✕ Root request failed"
                                RootStatus.ROOT_REVOKED -> "✕ Root permission revoked"
                                else -> "✕ Root Not Granted"
                            }
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                },
                dismissText = Strings.get("cancel", language),
                onDismiss = { showRootDialog = false }
            )
        }

        // 2. Unsupported Feature Dialog (Section 65)
        if (showUnsupportedDialog) {
            PixelDialog(
                title = Strings.get("unsupported_dialog_title", language),
                message = Strings.get("unsupported_dialog_msg", language),
                confirmText = Strings.get("ok", language),
                onConfirm = { showUnsupportedDialog = false }
            )
        }

        // 3. Language Selection Dialog
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                title = {
                    Text(
                        text = Strings.get("language", language),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = appSettings.language == lang
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.setLanguage(lang)
                                        showLanguageDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        showLanguageDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = lang.title,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(Strings.get("cancel", language))
                    }
                }
            )
        }

        // 4. Appearance Selection Dialog
        if (showThemeDialog) {
            AlertDialog(
                onDismissRequest = { showThemeDialog = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                title = {
                    Text(
                        text = Strings.get("appearance", language),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AppThemeMode.entries.forEach { mode ->
                            val isSelected = appSettings.themeMode == mode
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setThemeMode(mode)
                                        showThemeDialog = false
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = Strings.get(mode.titleKey, language),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showThemeDialog = false }) {
                        Text(Strings.get("cancel", language))
                    }
                }
            )
        }

        // Always On Display (AOD) System Overlay
        AodDisplayOverlay(
            aodState = aodDisplayState,
            aodConfig = appSettings.aodConfig,
            qualityLevel = appSettings.qualityLevel,
            customBackgroundBitmap = customBackgroundBitmap,
            onDismiss = { viewModel.dismissAod() }
        )
        }
    }
}
