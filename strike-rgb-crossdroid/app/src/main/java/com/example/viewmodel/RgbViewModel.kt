package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hardware.HardwareController
import com.example.hardware.LedHardwareController
import com.example.hardware.RootManager
import com.example.model.AppLanguage
import com.example.model.AppSettings
import com.example.model.AppThemeMode
import com.example.model.QualityLevel
import com.example.model.UiTheme
import com.example.model.AodConfig
import com.example.model.AodTheme
import com.example.model.AodClockSize
import com.example.model.AodClockStyle
import com.example.model.AodClockWeight
import com.example.model.AodClockPosition
import com.example.model.AodDatePosition
import com.example.model.AodDateStyle
import com.example.model.AodBatteryStyle
import com.example.model.AodEdgeAnimation
import com.example.model.AodThemeManager
import com.example.model.AodEdgeGlowPosition
import com.example.model.AodEdgeGlowConfig
import com.example.model.EffectType
import com.example.model.HardwareStatus
import com.example.model.HardwareTestResult
import com.example.model.LedState
import com.example.model.RgbColor
import com.example.model.RootStatus
import com.example.effects.EffectEngine
import com.example.effects.screensync.MediaProjectionHolder
import com.example.model.EffectEngineDiagnostic
import com.example.model.ScreenRegion
import com.example.model.ScreenSyncConfig
import com.example.model.ScreenSyncDebugInfo
import com.example.model.DeviceCapabilities
import com.example.model.NotificationOutputMode
import com.example.model.FlashPattern
import com.example.model.FlashSpeed
import com.example.model.FlashDuration
import com.example.model.FrontFlashMode
import com.example.model.LightFlowProfile
import com.example.model.LightFlowAutomationConfig
import com.example.model.NotificationHistoryItem
import com.example.model.InstalledAppItem
import com.example.notification.AppColorExtractor
import com.example.notification.LightFlowRepository
import com.example.model.AodNotificationStyle
import com.example.model.AodColorSource
import com.example.model.ScreenOffMasterControl
import com.example.model.ActiveNotificationEvent
import com.example.model.AppNotificationProfile
import com.example.model.ColorProfile
import com.example.model.BuiltInColorProfiles
import com.example.hardware.DeviceCapabilityDetector
import com.example.notification.NotificationFlowEngine
import com.example.service.NotificationLightListenerService
import com.example.util.VibrationFeedbackManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.example.service.LedControlService
import com.example.ui.background.CustomBackgroundManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

class RgbViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("koukou_rgb_prefs", Context.MODE_PRIVATE)
    private val hardwareController = HardwareController()
    private val ledHardwareController = hardwareController.controller

    private val _ledState = MutableStateFlow(loadInitialLedState())
    val ledState: StateFlow<LedState> = _ledState.asStateFlow()

    private val _hardwareStatus = MutableStateFlow(hardwareController.checkHardwareStatus())
    val hardwareStatus: StateFlow<HardwareStatus> = _hardwareStatus.asStateFlow()

    private val _appSettings = MutableStateFlow(loadInitialSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val capabilityDetector = DeviceCapabilityDetector(application)
    private val _deviceCapabilities = MutableStateFlow(
        capabilityDetector.detectCapabilities(
            isCameraPermissionGranted = false,
            isRgbLedDetected = _hardwareStatus.value.isLedDetected
        )
    )
    val deviceCapabilities: StateFlow<DeviceCapabilities> = _deviceCapabilities.asStateFlow()

    val notificationFlowEngine = NotificationFlowEngine.getInstance(application)
    val screenFlashState = notificationFlowEngine.screenFlashState
    val aodDisplayState = notificationFlowEngine.aodDisplayState
    val activeNotificationEvent = notificationFlowEngine.activeNotification

    private val _appProfiles = MutableStateFlow<List<AppNotificationProfile>>(
        NotificationLightListenerService.appProfiles.values.toList()
    )
    val appProfiles: StateFlow<List<AppNotificationProfile>> = _appProfiles.asStateFlow()

    // Light Flow Complete Engine Repository & StateFlows
    val lightFlowRepo = LightFlowRepository.getInstance(application)
    private val _lightFlowMasterEnabled = MutableStateFlow(lightFlowRepo.isMasterEnabled())
    val lightFlowMasterEnabled: StateFlow<Boolean> = _lightFlowMasterEnabled.asStateFlow()

    private val _lightFlowProfiles = MutableStateFlow<List<LightFlowProfile>>(
        lightFlowRepo.getAllProfiles().values.toList()
    )
    val lightFlowProfiles: StateFlow<List<LightFlowProfile>> = _lightFlowProfiles.asStateFlow()

    private val _defaultLightFlowProfile = MutableStateFlow(lightFlowRepo.getDefaultProfile())
    val defaultLightFlowProfile: StateFlow<LightFlowProfile> = _defaultLightFlowProfile.asStateFlow()

    private val _lightFlowAutomation = MutableStateFlow(lightFlowRepo.getAutomationConfig())
    val lightFlowAutomation: StateFlow<LightFlowAutomationConfig> = _lightFlowAutomation.asStateFlow()

    private val _lightFlowHistory = MutableStateFlow(lightFlowRepo.getHistory())
    val lightFlowHistory: StateFlow<List<NotificationHistoryItem>> = _lightFlowHistory.asStateFlow()

    private val _isNotificationListenerGranted = MutableStateFlow(
        NotificationLightListenerService.isPermissionGranted(application)
    )
    val isNotificationListenerGranted: StateFlow<Boolean> = _isNotificationListenerGranted.asStateFlow()

    val activeLedOutput: StateFlow<RgbColor> = notificationFlowEngine.activeLedOutput

    val vibrationManager = VibrationFeedbackManager(application)

    val effectEngine: EffectEngine = EffectEngine.getInstance(application)
    val effectDiagnostic: StateFlow<EffectEngineDiagnostic> = effectEngine.diagnosticFlow
    val screenSyncDebugInfo: StateFlow<ScreenSyncDebugInfo> = LedControlService.debugInfoFlow

    init {
        vibrationManager.isEnabled = _appSettings.value.vibrationFeedback
        refreshHardwareStatus()
        applyHardwareChanges()
        val bgPath = _appSettings.value.customBackgroundPath
        if (!bgPath.isNullOrEmpty()) {
            CustomBackgroundManager.loadFromPath(bgPath)
        }
    }

    fun refreshHardwareStatus() {
        viewModelScope.launch {
            val status = ledHardwareController.detectLEDs()
            _hardwareStatus.value = status
        }
    }

    private fun loadInitialLedState(): LedState {
        val r = prefs.getInt("led_r", RgbColor.Blue.r)
        val g = prefs.getInt("led_g", RgbColor.Blue.g)
        val b = prefs.getInt("led_b", RgbColor.Blue.b)
        val effectName = prefs.getString("led_effect", EffectType.STATIC.name) ?: EffectType.STATIC.name
        val effect = try { EffectType.valueOf(effectName) } catch (_: Exception) { EffectType.STATIC }
        val brightness = prefs.getFloat("led_brightness", 0.85f)
        val speed = prefs.getFloat("led_speed", 1.0f)
        val breathingSpeed = prefs.getFloat("led_breathing_speed", 2.0f)
        val isRunning = prefs.getBoolean("led_running", true)

        val syncIntensity = prefs.getFloat("setting_sync_intensity", 1.0f)
        val syncSmoothing = prefs.getFloat("setting_sync_smoothing", 0.50f)
        val syncColorBoost = prefs.getBoolean("setting_sync_color_boost", true)
        val syncSamplingRate = prefs.getInt("setting_sync_sampling_rate", 15)
        val syncRegionName = prefs.getString("setting_sync_region", ScreenRegion.FULL_SCREEN.name) ?: ScreenRegion.FULL_SCREEN.name
        val syncRegion = try { ScreenRegion.valueOf(syncRegionName) } catch (_: Exception) { ScreenRegion.FULL_SCREEN }
        val syncKeepOff = prefs.getBoolean("setting_sync_keep_off", false)

        val syncConfig = ScreenSyncConfig(
            isEnabled = effect == EffectType.SCREEN_SYNC,
            intensity = syncIntensity,
            brightness = brightness,
            smoothing = syncSmoothing,
            colorBoost = syncColorBoost,
            samplingRate = syncSamplingRate,
            region = syncRegion,
            keepColorOnScreenOff = syncKeepOff
        )

        return LedState(
            isRunning = isRunning,
            currentColor = RgbColor(r, g, b),
            currentEffect = effect,
            brightness = brightness,
            effectSpeed = speed,
            breathingSpeed = breathingSpeed,
            screenSyncConfig = syncConfig
        )
    }

    private fun loadInitialSettings(): AppSettings {
        val langName = prefs.getString("setting_lang", AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
        val lang = try { AppLanguage.valueOf(langName) } catch (_: Exception) { AppLanguage.ENGLISH }

        val themeName = prefs.getString("setting_theme", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val theme = try { AppThemeMode.valueOf(themeName) } catch (_: Exception) { AppThemeMode.SYSTEM }

        val uiThemeName = prefs.getString("setting_ui_theme", UiTheme.NEWBIE_UI_26.name) ?: UiTheme.NEWBIE_UI_26.name
        val uiTheme = when (uiThemeName) {
            "ULTIMATE_LIQUID_GLASS", "XML_THEME" -> UiTheme.XML_THEME
            else -> try { UiTheme.valueOf(uiThemeName) } catch (_: Exception) { UiTheme.NEWBIE_UI_26 }
        }

        val dynamicColor = prefs.getBoolean("setting_dynamic_color", true)
        val vibrationFeedback = prefs.getBoolean("setting_vibration_feedback", true)
        val startOnBoot = prefs.getBoolean("setting_start_boot", false)
        val screenColor = prefs.getBoolean("setting_screen_color", false)
        val smoothTransition = prefs.getBoolean("setting_smooth_transition", true)
        val saveLastEffect = prefs.getBoolean("setting_save_last_effect", true)
        val keepScreenOff = prefs.getBoolean("setting_keep_screen_off", true)
        val samplingRate = prefs.getInt("setting_sampling_rate", 30)
        val sensitivity = prefs.getFloat("setting_sensitivity", 0.75f)
        val smoothing = prefs.getFloat("setting_smoothing", 0.50f)

        val syncIntensity = prefs.getFloat("setting_sync_intensity", 1.0f)
        val syncSmoothing = prefs.getFloat("setting_sync_smoothing", 0.50f)
        val syncColorBoost = prefs.getBoolean("setting_sync_color_boost", true)
        val syncSamplingRate = prefs.getInt("setting_sync_sampling_rate", 15)
        val syncRegionName = prefs.getString("setting_sync_region", ScreenRegion.FULL_SCREEN.name) ?: ScreenRegion.FULL_SCREEN.name
        val syncRegion = try { ScreenRegion.valueOf(syncRegionName) } catch (_: Exception) { ScreenRegion.FULL_SCREEN }
        val syncKeepOff = prefs.getBoolean("setting_sync_keep_off", false)
        val customBgPath = prefs.getString("setting_custom_bg_path", null)
        val bgBlur = prefs.getFloat("setting_bg_blur", 0.0f)
        val bgOverlay = prefs.getFloat("setting_bg_overlay", 0.30f)

        val outputModeName = prefs.getString("notif_output_mode", NotificationOutputMode.RGB_FLASH_AOD.name)
            ?: NotificationOutputMode.RGB_FLASH_AOD.name
        val outputMode = try { NotificationOutputMode.valueOf(outputModeName) } catch (_: Exception) { NotificationOutputMode.RGB_FLASH_AOD }

        val rearFlash = prefs.getBoolean("notif_rear_flash", true)
        val frontModeName = prefs.getString("notif_front_flash_mode", FrontFlashMode.AUTO.name) ?: FrontFlashMode.AUTO.name
        val frontMode = try { FrontFlashMode.valueOf(frontModeName) } catch (_: Exception) { FrontFlashMode.AUTO }

        val patternName = prefs.getString("notif_pattern", FlashPattern.DOUBLE.name) ?: FlashPattern.DOUBLE.name
        val pattern = try { FlashPattern.valueOf(patternName) } catch (_: Exception) { FlashPattern.DOUBLE }

        val speedName = prefs.getString("notif_speed", FlashSpeed.NORMAL.name) ?: FlashSpeed.NORMAL.name
        val speed = try { FlashSpeed.valueOf(speedName) } catch (_: Exception) { FlashSpeed.NORMAL }

        val durationName = prefs.getString("notif_duration", FlashDuration.SEC_5.name) ?: FlashDuration.SEC_5.name
        val duration = try { FlashDuration.valueOf(durationName) } catch (_: Exception) { FlashDuration.SEC_5 }

        val screenOffName = prefs.getString("notif_screen_off", ScreenOffMasterControl.EVERYTHING.name) ?: ScreenOffMasterControl.EVERYTHING.name
        val screenOff = try { ScreenOffMasterControl.valueOf(screenOffName) } catch (_: Exception) { ScreenOffMasterControl.EVERYTHING }

        val aodStyleName = prefs.getString("notif_aod_style", AodNotificationStyle.RGB_GLOW.name) ?: AodNotificationStyle.RGB_GLOW.name
        val aodStyle = try { AodNotificationStyle.valueOf(aodStyleName) } catch (_: Exception) { AodNotificationStyle.RGB_GLOW }

        val aodColorName = prefs.getString("notif_aod_color", AodColorSource.AUTO_APP_COLOR.name) ?: AodColorSource.AUTO_APP_COLOR.name
        val aodColor = try { AodColorSource.valueOf(aodColorName) } catch (_: Exception) { AodColorSource.AUTO_APP_COLOR }

        val syncConfig = ScreenSyncConfig(
            isEnabled = false,
            intensity = syncIntensity,
            brightness = 0.85f,
            smoothing = syncSmoothing,
            colorBoost = syncColorBoost,
            samplingRate = syncSamplingRate,
            region = syncRegion,
            keepColorOnScreenOff = syncKeepOff
        )

        val qualityLevelName = prefs.getString("setting_quality_level", QualityLevel.BALANCED.name) ?: QualityLevel.BALANCED.name
        val qualityLevel = try { QualityLevel.valueOf(qualityLevelName) } catch (_: Exception) { QualityLevel.BALANCED }

        val aodEnabled = prefs.getBoolean("setting_aod_enabled", true)
        val aodThemeName = prefs.getString("setting_aod_theme", AodTheme.LIQUID_GLASS.name) ?: AodTheme.LIQUID_GLASS.name
        val aodTheme = try { AodTheme.valueOf(aodThemeName) } catch (_: Exception) { AodTheme.LIQUID_GLASS }

        val aodClockStyleName = prefs.getString("setting_aod_clock_style", AodClockStyle.LIQUID_GLASS.name) ?: AodClockStyle.LIQUID_GLASS.name
        val aodClockStyle = try { AodClockStyle.valueOf(aodClockStyleName) } catch (_: Exception) { AodClockStyle.LIQUID_GLASS }

        val aodClockSizeName = prefs.getString("setting_aod_clock_size", AodClockSize.LARGE.name) ?: AodClockSize.LARGE.name
        val aodClockSize = try { AodClockSize.valueOf(aodClockSizeName) } catch (_: Exception) { AodClockSize.LARGE }

        val aodClockWeightName = prefs.getString("setting_aod_clock_weight", AodClockWeight.REGULAR.name) ?: AodClockWeight.REGULAR.name
        val aodClockWeight = try { AodClockWeight.valueOf(aodClockWeightName) } catch (_: Exception) { AodClockWeight.REGULAR }

        val aodClockPosName = prefs.getString("setting_aod_clock_pos", AodClockPosition.TOP.name) ?: AodClockPosition.TOP.name
        val aodClockPos = try { AodClockPosition.valueOf(aodClockPosName) } catch (_: Exception) { AodClockPosition.TOP }

        val aodDatePosName = prefs.getString("setting_aod_date_pos", AodDatePosition.BELOW_CLOCK.name) ?: AodDatePosition.BELOW_CLOCK.name
        val aodDatePos = try { AodDatePosition.valueOf(aodDatePosName) } catch (_: Exception) { AodDatePosition.BELOW_CLOCK }

        val aodDateStyleName = prefs.getString("setting_aod_date_style", AodDateStyle.PILL.name) ?: AodDateStyle.PILL.name
        val aodDateStyle = try { AodDateStyle.valueOf(aodDateStyleName) } catch (_: Exception) { AodDateStyle.PILL }

        val aodBatteryStyleName = prefs.getString("setting_aod_battery_style", AodBatteryStyle.CAPSULE.name) ?: AodBatteryStyle.CAPSULE.name
        val aodBatteryStyle = try { AodBatteryStyle.valueOf(aodBatteryStyleName) } catch (_: Exception) { AodBatteryStyle.CAPSULE }

        val aodEdgePosName = prefs.getString("setting_aod_edge_pos", AodEdgeGlowPosition.FULL.name) ?: AodEdgeGlowPosition.FULL.name
        val aodEdgePos = try { AodEdgeGlowPosition.valueOf(aodEdgePosName) } catch (_: Exception) { AodEdgeGlowPosition.FULL }

        val aodEdgeAnimName = prefs.getString("setting_aod_edge_anim", AodEdgeAnimation.BREATHING.name) ?: AodEdgeAnimation.BREATHING.name
        val aodEdgeAnim = try { AodEdgeAnimation.valueOf(aodEdgeAnimName) } catch (_: Exception) { AodEdgeAnimation.BREATHING }

        val loadedAodConfig = AodConfig(
            isEnabled = aodEnabled,
            theme = aodTheme,
            clockStyle = aodClockStyle,
            clockSize = aodClockSize,
            clockWeight = aodClockWeight,
            clockPosition = aodClockPos,
            showDate = prefs.getBoolean("setting_aod_show_date", true),
            dateFormat = prefs.getString("setting_aod_date_format", "EEE, MMM d") ?: "EEE, MMM d",
            datePosition = aodDatePos,
            dateStyle = aodDateStyle,
            showBatteryPercentage = prefs.getBoolean("setting_aod_show_battery_pct", true),
            showBatteryIcon = prefs.getBoolean("setting_aod_show_battery_icon", true),
            batteryStyle = aodBatteryStyle,
            clockOpacity = prefs.getFloat("setting_aod_clock_opacity", 0.95f),
            clockBlur = prefs.getFloat("setting_aod_clock_blur", 0.50f),
            clockRefraction = prefs.getFloat("setting_aod_clock_refraction", 0.65f),
            clockGloss = prefs.getFloat("setting_aod_clock_gloss", 0.80f),
            clockGlow = prefs.getFloat("setting_aod_clock_glow", 0.70f),
            clockThickness = prefs.getFloat("setting_aod_clock_thickness", 0.60f),
            backgroundInteraction = prefs.getBoolean("setting_aod_bg_interaction", true),
            backgroundDarkOverlay = prefs.getFloat("setting_aod_bg_dark_overlay", 0.40f),
            backgroundBlur = prefs.getFloat("setting_aod_bg_blur", 0.35f),
            backgroundBrightness = prefs.getFloat("setting_aod_bg_brightness", 0.75f),
            backgroundRefraction = prefs.getFloat("setting_aod_bg_refraction", 0.50f),
            edgeGlow = AodEdgeGlowConfig(
                isEnabled = prefs.getBoolean("setting_aod_edge_enabled", true),
                position = aodEdgePos,
                thickness = prefs.getFloat("setting_aod_edge_thickness", 6.0f),
                glowRadius = prefs.getFloat("setting_aod_edge_glow_radius", 18.0f),
                speed = prefs.getFloat("setting_aod_edge_speed", 1.0f),
                isRgbCycle = prefs.getBoolean("setting_aod_edge_rgb", false),
                animation = aodEdgeAnim
            )
        )

        return AppSettings(
            language = lang,
            themeMode = theme,
            uiTheme = uiTheme,
            qualityLevel = qualityLevel,
            dynamicColor = dynamicColor,
            vibrationFeedback = vibrationFeedback,
            startOnBoot = startOnBoot,
            screenColor = screenColor,
            smoothTransition = smoothTransition,
            saveLastEffect = saveLastEffect,
            keepLedOnScreenOff = keepScreenOff,
            samplingRate = samplingRate,
            sensitivity = sensitivity,
            smoothing = smoothing,
            screenSyncConfig = syncConfig,
            customBackgroundPath = customBgPath,
            backgroundBlurIntensity = bgBlur,
            backgroundOverlay = bgOverlay,
            notificationOutputMode = outputMode,
            globalRearFlash = rearFlash,
            globalFrontFlashMode = frontMode,
            globalFlashPattern = pattern,
            globalFlashSpeed = speed,
            globalFlashDuration = duration,
            screenOffMasterControl = screenOff,
            aodStyle = aodStyle,
            aodColorSource = aodColor,
            aodConfig = loadedAodConfig
        )
    }

    private fun persistSettings() {
        val s = _appSettings.value
        val l = _ledState.value
        val sync = l.screenSyncConfig
        prefs.edit()
            .putInt("led_r", l.currentColor.r)
            .putInt("led_g", l.currentColor.g)
            .putInt("led_b", l.currentColor.b)
            .putString("led_effect", l.currentEffect.name)
            .putFloat("led_brightness", l.brightness)
            .putFloat("led_speed", l.effectSpeed)
            .putFloat("led_breathing_speed", l.breathingSpeed)
            .putBoolean("led_running", l.isRunning)
            .putString("setting_lang", s.language.name)
            .putString("setting_theme", s.themeMode.name)
            .putString("setting_ui_theme", s.uiTheme.name)
            .putString("setting_quality_level", s.qualityLevel.name)
            .putBoolean("setting_aod_enabled", s.aodConfig.isEnabled)
            .putString("setting_aod_theme", s.aodConfig.theme.name)
            .putString("setting_aod_clock_style", s.aodConfig.clockStyle.name)
            .putString("setting_aod_clock_size", s.aodConfig.clockSize.name)
            .putString("setting_aod_clock_weight", s.aodConfig.clockWeight.name)
            .putString("setting_aod_clock_pos", s.aodConfig.clockPosition.name)
            .putBoolean("setting_aod_show_date", s.aodConfig.showDate)
            .putString("setting_aod_date_format", s.aodConfig.dateFormat)
            .putString("setting_aod_date_pos", s.aodConfig.datePosition.name)
            .putString("setting_aod_date_style", s.aodConfig.dateStyle.name)
            .putBoolean("setting_aod_show_battery_pct", s.aodConfig.showBatteryPercentage)
            .putBoolean("setting_aod_show_battery_icon", s.aodConfig.showBatteryIcon)
            .putString("setting_aod_battery_style", s.aodConfig.batteryStyle.name)
            .putFloat("setting_aod_clock_opacity", s.aodConfig.clockOpacity)
            .putFloat("setting_aod_clock_blur", s.aodConfig.clockBlur)
            .putFloat("setting_aod_clock_refraction", s.aodConfig.clockRefraction)
            .putFloat("setting_aod_clock_gloss", s.aodConfig.clockGloss)
            .putFloat("setting_aod_clock_glow", s.aodConfig.clockGlow)
            .putFloat("setting_aod_clock_thickness", s.aodConfig.clockThickness)
            .putBoolean("setting_aod_bg_interaction", s.aodConfig.backgroundInteraction)
            .putBoolean("setting_aod_edge_enabled", s.aodConfig.edgeGlow.isEnabled)
            .putString("setting_aod_edge_pos", s.aodConfig.edgeGlow.position.name)
            .putFloat("setting_aod_edge_thickness", s.aodConfig.edgeGlow.thickness)
            .putFloat("setting_aod_edge_glow_radius", s.aodConfig.edgeGlow.glowRadius)
            .putFloat("setting_aod_edge_speed", s.aodConfig.edgeGlow.speed)
            .putString("setting_aod_edge_anim", s.aodConfig.edgeGlow.animation.name)
            .putBoolean("setting_aod_edge_rgb", s.aodConfig.edgeGlow.isRgbCycle)
            .putFloat("setting_aod_bg_dark_overlay", s.aodConfig.backgroundDarkOverlay)
            .putFloat("setting_aod_bg_blur", s.aodConfig.backgroundBlur)
            .putFloat("setting_aod_bg_brightness", s.aodConfig.backgroundBrightness)
            .putFloat("setting_aod_bg_refraction", s.aodConfig.backgroundRefraction)
            .putBoolean("setting_dynamic_color", s.dynamicColor)
            .putBoolean("setting_vibration_feedback", s.vibrationFeedback)
            .putBoolean("setting_start_boot", s.startOnBoot)
            .putBoolean("setting_screen_color", s.screenColor)
            .putBoolean("setting_smooth_transition", s.smoothTransition)
            .putBoolean("setting_save_last_effect", s.saveLastEffect)
            .putBoolean("setting_keep_screen_off", s.keepLedOnScreenOff)
            .putInt("setting_sampling_rate", s.samplingRate)
            .putFloat("setting_sensitivity", s.sensitivity)
            .putFloat("setting_smoothing", s.smoothing)
            .putFloat("setting_sync_intensity", sync.intensity)
            .putFloat("setting_sync_smoothing", sync.smoothing)
            .putBoolean("setting_sync_color_boost", sync.colorBoost)
            .putInt("setting_sync_sampling_rate", sync.samplingRate)
            .putString("setting_sync_region", sync.region.name)
            .putBoolean("setting_sync_keep_off", sync.keepColorOnScreenOff)
            .putString("setting_custom_bg_path", s.customBackgroundPath)
            .putFloat("setting_bg_blur", s.backgroundBlurIntensity)
            .putFloat("setting_bg_overlay", s.backgroundOverlay)
            .putString("notif_output_mode", s.notificationOutputMode.name)
            .putBoolean("notif_rear_flash", s.globalRearFlash)
            .putString("notif_front_flash_mode", s.globalFrontFlashMode.name)
            .putString("notif_pattern", s.globalFlashPattern.name)
            .putString("notif_speed", s.globalFlashSpeed.name)
            .putString("notif_duration", s.globalFlashDuration.name)
            .putString("notif_screen_off", s.screenOffMasterControl.name)
            .putString("notif_aod_style", s.aodStyle.name)
            .putString("notif_aod_color", s.aodColorSource.name)
            .apply()
    }

    private fun syncService() {
        val state = _ledState.value
        val keepOff = _appSettings.value.keepLedOnScreenOff
        val app = getApplication<Application>()
        if (state.isRunning) {
            LedControlService.update(app, state, keepOff)
        } else {
            LedControlService.stop(app)
        }
    }

    private fun applyHardwareChanges() {
        viewModelScope.launch {
            val state = _ledState.value
            effectEngine.updateState(state)
            syncService()
        }
    }

    fun startLed() {
        _ledState.update { it.copy(isRunning = true) }
        applyHardwareChanges()
        persistSettings()
        vibrationManager.vibrateLedToggle(true)
    }

    fun stopLed() {
        _ledState.update { it.copy(isRunning = false) }
        applyHardwareChanges()
        persistSettings()
        vibrationManager.vibrateLedToggle(false)
    }

    fun toggleLed(): Boolean {
        val newState = !_ledState.value.isRunning
        _ledState.update { it.copy(isRunning = newState) }
        applyHardwareChanges()
        persistSettings()
        vibrationManager.vibrateLedToggle(newState)
        return newState
    }

    fun setColor(color: RgbColor, tactileFeedback: Boolean = true) {
        _ledState.update { it.copy(currentColor = color) }
        applyHardwareChanges()
        if (_appSettings.value.saveLastEffect) {
            persistSettings()
        }
        if (tactileFeedback) {
            vibrationManager.vibrateColorProfileSelected()
        }
    }

    fun selectColorProfile(profile: ColorProfile) {
        setColor(profile.color, tactileFeedback = true)
    }

    fun triggerVibrationForLedToggle(turnedOn: Boolean) {
        vibrationManager.vibrateLedToggle(turnedOn)
    }

    fun triggerVibrationForColorProfile() {
        vibrationManager.vibrateColorProfileSelected()
    }

    fun setEffect(effect: EffectType) {
        _ledState.update { it.copy(currentEffect = effect, isRunning = true) }
        applyHardwareChanges()
        if (_appSettings.value.saveLastEffect) {
            persistSettings()
        }
    }

    fun setBrightness(brightness: Float) {
        _ledState.update { it.copy(brightness = brightness.coerceIn(0f, 1f)) }
        applyHardwareChanges()
    }

    fun setIntensity(intensity: Float) {
        _ledState.update { it.copy(intensity = intensity.coerceIn(0f, 1f)) }
        applyHardwareChanges()
    }

    fun setEffectSpeed(speed: Float) {
        _ledState.update { it.copy(effectSpeed = speed.coerceIn(0.2f, 3.0f)) }
        applyHardwareChanges()
    }

    fun setBreathingSpeed(speed: Float) {
        _ledState.update { it.copy(breathingSpeed = speed.coerceIn(0.5f, 5.0f)) }
        applyHardwareChanges()
    }

    fun setKeepLedOnScreenOff(enabled: Boolean) {
        _appSettings.update { it.copy(keepLedOnScreenOff = enabled) }
        persistSettings()
        syncService()
    }

    fun setDynamicColor(enabled: Boolean) {
        _appSettings.update { it.copy(dynamicColor = enabled) }
        persistSettings()
    }

    fun setVibrationFeedback(enabled: Boolean) {
        _appSettings.update { it.copy(vibrationFeedback = enabled) }
        vibrationManager.isEnabled = enabled
        persistSettings()
        if (enabled) {
            vibrationManager.vibrateClick()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _appSettings.update { it.copy(language = lang) }
        persistSettings()
    }

    fun setThemeMode(mode: AppThemeMode) {
        _appSettings.update { it.copy(themeMode = mode) }
        persistSettings()
    }

    fun setUiTheme(theme: UiTheme) {
        _appSettings.update { it.copy(uiTheme = theme) }
        persistSettings()
    }

    fun setQualityLevel(quality: QualityLevel) {
        _appSettings.update { it.copy(qualityLevel = quality) }
        persistSettings()
    }

    fun updateAodConfig(transform: (AodConfig) -> AodConfig) {
        _appSettings.update { it.copy(aodConfig = transform(it.aodConfig)) }
        persistSettings()
    }

    fun applyAodTheme(theme: AodTheme) {
        _appSettings.update { s ->
            s.copy(aodConfig = AodThemeManager.applyThemePreset(theme, s.aodConfig))
        }
        persistSettings()
    }

    fun resetAodThemeToDefault() {
        _appSettings.update { s ->
            s.copy(aodConfig = AodThemeManager.applyThemePreset(s.aodConfig.theme, s.aodConfig))
        }
        persistSettings()
    }

    fun setStartOnBoot(enabled: Boolean) {
        _appSettings.update { it.copy(startOnBoot = enabled) }
        persistSettings()
    }

    fun setScreenColor(enabled: Boolean) {
        _appSettings.update { it.copy(screenColor = enabled) }
        persistSettings()
    }

    fun setSmoothTransition(enabled: Boolean) {
        _appSettings.update { it.copy(smoothTransition = enabled) }
        persistSettings()
    }

    fun setSaveLastEffect(enabled: Boolean) {
        _appSettings.update { it.copy(saveLastEffect = enabled) }
        persistSettings()
    }

    fun setSamplingRate(rate: Int) {
        _appSettings.update { it.copy(samplingRate = rate.coerceIn(10, 60)) }
        persistSettings()
    }

    fun setSensitivity(sens: Float) {
        _appSettings.update { it.copy(sensitivity = sens.coerceIn(0f, 1f)) }
        persistSettings()
    }

    fun setSmoothing(smooth: Float) {
        _appSettings.update { it.copy(smoothing = smooth.coerceIn(0f, 1f)) }
        persistSettings()
    }

    fun updateScreenSyncConfig(transform: (ScreenSyncConfig) -> ScreenSyncConfig) {
        _ledState.update { current ->
            val updated = transform(current.screenSyncConfig)
            current.copy(screenSyncConfig = updated)
        }
        _appSettings.update { current ->
            val updated = transform(current.screenSyncConfig)
            current.copy(screenSyncConfig = updated)
        }
        syncService()
        persistSettings()
    }

    fun setScreenSyncIntensity(intensity: Float) {
        updateScreenSyncConfig { it.copy(intensity = intensity.coerceIn(0f, 1f)) }
    }

    fun setScreenSyncSmoothing(smoothing: Float) {
        updateScreenSyncConfig { it.copy(smoothing = smoothing.coerceIn(0f, 1f)) }
    }

    fun setScreenSyncColorBoost(enabled: Boolean) {
        updateScreenSyncConfig { it.copy(colorBoost = enabled) }
    }

    fun setScreenSyncSamplingRate(rate: Int) {
        updateScreenSyncConfig { it.copy(samplingRate = rate.coerceIn(10, 20)) }
    }

    fun setScreenSyncRegion(region: ScreenRegion) {
        updateScreenSyncConfig { it.copy(region = region) }
    }

    fun setScreenSyncKeepColorOnScreenOff(enabled: Boolean) {
        updateScreenSyncConfig { it.copy(keepColorOnScreenOff = enabled) }
    }

    fun onMediaProjectionGranted(resultCode: Int, data: Intent?) {
        MediaProjectionHolder.setPermission(resultCode, data)
        _ledState.update { it.copy(currentEffect = EffectType.SCREEN_SYNC, isRunning = true) }
        applyHardwareChanges()
        persistSettings()
    }

    fun setDefaultEffect(effect: EffectType) {
        _appSettings.update { it.copy(defaultEffect = effect) }
        _ledState.update { it.copy(currentEffect = effect) }
        persistSettings()
    }

    fun runHardwareTest(onComplete: (HardwareTestResult) -> Unit) {
        viewModelScope.launch {
            _hardwareStatus.update { it.copy(isHardwareTestRunning = true) }
            val result = ledHardwareController.testRGB { stepMsg, stepColor ->
                _statusMessage.value = stepMsg
                _ledState.update { it.copy(currentColor = stepColor, isRunning = true) }
            }
            _hardwareStatus.update { it.copy(isHardwareTestRunning = false) }
            _statusMessage.value = null
            applyHardwareChanges()
            refreshHardwareStatus()
            onComplete(result)
        }
    }

    /**
     * Section 25 Hardware Verification: Tests 255,0,0, 0,255,0, 0,0,255 and alternates 6 colors.
     */
    fun runPipelineColorTest(onComplete: (HardwareTestResult) -> Unit) {
        viewModelScope.launch {
            _hardwareStatus.update { it.copy(isHardwareTestRunning = true) }
            val result = hardwareController.runPipelineColorTest { stepMsg, stepColor ->
                _statusMessage.value = stepMsg
                _ledState.update { it.copy(currentColor = stepColor, isRunning = true) }
            }
            _hardwareStatus.update { it.copy(isHardwareTestRunning = false) }
            _statusMessage.value = null
            applyHardwareChanges()
            refreshHardwareStatus()
            onComplete(result)
        }
    }

    /**
     * Direct test mode: forces real-time animation of selected effect on physical LED.
     */
    fun testEffectDirect(effect: EffectType) {
        _ledState.update { it.copy(currentEffect = effect, isRunning = true) }
        effectEngine.testEffect(effect)
        syncService()
    }

    /**
     * Checks current root authorization status via real SU execution.
     */
    fun checkRoot(onResult: ((RootStatus) -> Unit)? = null) {
        viewModelScope.launch {
            _statusMessage.value = "Checking Root Authorization..."
            val (status, uid) = RootManager.determineRootStatus()
            val hwStatus = ledHardwareController.detectLEDs()
            _hardwareStatus.value = hwStatus
            _statusMessage.value = null
            onResult?.invoke(status)
        }
    }

    /**
     * Requests root access via `su -c id`.
     * This triggers the Magisk / Superuser dialog on real rooted devices.
     */
    fun requestRoot(onResult: ((RootStatus) -> Unit)? = null) {
        viewModelScope.launch {
            _statusMessage.value = "Requesting Magisk Superuser permission..."
            val result = RootManager.requestRoot()
            val uid = RootManager.parseUid(result.stdout)
            val status = when {
                result.exitCode == 0 && uid == 0 -> RootStatus.ROOT_GRANTED
                result.stdout.contains("uid=0") -> RootStatus.ROOT_GRANTED
                result.stderr.contains("denied", ignoreCase = true) -> RootStatus.ROOT_DENIED
                !RootManager.isRootAvailable() -> RootStatus.ROOT_NOT_FOUND
                else -> RootStatus.ROOT_ERROR
            }
            val hwStatus = ledHardwareController.detectLEDs()
            _hardwareStatus.value = hwStatus
            _statusMessage.value = null
            onResult?.invoke(status)
        }
    }

    fun setCustomBackgroundUri(uri: Uri, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val inputStream = app.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    onResult(false)
                    return@launch
                }
                val destFile = File(app.filesDir, "custom_background.jpg")
                val tempBytes = inputStream.use { it.readBytes() }

                // Decode with sample size to fit 540x960 device memory safely
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(tempBytes, 0, tempBytes.size, boundsOptions)

                var inSampleSize = 1
                val maxDim = max(boundsOptions.outWidth, boundsOptions.outHeight)
                while (maxDim / inSampleSize > 1280) {
                    inSampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val bitmap = BitmapFactory.decodeByteArray(tempBytes, 0, tempBytes.size, decodeOptions)
                if (bitmap != null) {
                    FileOutputStream(destFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
                    }
                    CustomBackgroundManager.loadAndCache(bitmap)
                    _appSettings.update {
                        it.copy(customBackgroundPath = destFile.absolutePath)
                    }
                    persistSettings()
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("RgbViewModel", "Failed to save custom background", e)
                onResult(false)
            }
        }
    }

    fun setBackgroundBlurIntensity(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        _appSettings.update { it.copy(backgroundBlurIntensity = clamped) }
        persistSettings()
    }

    fun setBackgroundOverlay(value: Float) {
        val clamped = value.coerceIn(0f, 1f)
        _appSettings.update { it.copy(backgroundOverlay = clamped) }
        persistSettings()
    }

    fun resetCustomBackground() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val file = File(app.filesDir, "custom_background.jpg")
                if (file.exists()) {
                    file.delete()
                }
            } catch (_: Exception) {}
            CustomBackgroundManager.clear()
            _appSettings.update {
                it.copy(customBackgroundPath = null)
            }
            persistSettings()
        }
    }

    fun setNotificationOutputMode(mode: NotificationOutputMode) {
        _appSettings.update { it.copy(notificationOutputMode = mode) }
        persistSettings()
    }

    fun setGlobalRearFlash(enabled: Boolean) {
        _appSettings.update { it.copy(globalRearFlash = enabled) }
        persistSettings()
    }

    fun setGlobalFrontFlashMode(mode: FrontFlashMode) {
        _appSettings.update { it.copy(globalFrontFlashMode = mode) }
        persistSettings()
    }

    fun setGlobalFlashPattern(pattern: FlashPattern) {
        _appSettings.update { it.copy(globalFlashPattern = pattern) }
        persistSettings()
    }

    fun setGlobalFlashSpeed(speed: FlashSpeed) {
        _appSettings.update { it.copy(globalFlashSpeed = speed) }
        persistSettings()
    }

    fun setGlobalFlashDuration(duration: FlashDuration) {
        _appSettings.update { it.copy(globalFlashDuration = duration) }
        persistSettings()
    }

    fun setScreenOffMasterControl(mode: ScreenOffMasterControl) {
        _appSettings.update { it.copy(screenOffMasterControl = mode) }
        persistSettings()
    }

    fun setAodStyle(style: AodNotificationStyle) {
        _appSettings.update { it.copy(aodStyle = style) }
        persistSettings()
    }

    fun setAodColorSource(source: AodColorSource) {
        _appSettings.update { it.copy(aodColorSource = source) }
        persistSettings()
    }

    fun updateCameraPermission(granted: Boolean) {
        _deviceCapabilities.value = capabilityDetector.detectCapabilities(
            isCameraPermissionGranted = granted,
            isRgbLedDetected = _hardwareStatus.value.isLedDetected
        )
    }

    fun updateAppProfile(profile: AppNotificationProfile) {
        NotificationLightListenerService.appProfiles[profile.packageName] = profile
        _appProfiles.value = NotificationLightListenerService.appProfiles.values.toList()
    }

    /**
     * Test trigger to immediately simulate a notification for testing outputs
     */
    fun testNotificationFlow(
        appName: String = "Test App",
        color: RgbColor = RgbColor.Mint,
        isRearFlash: Boolean = true,
        frontMode: FrontFlashMode = FrontFlashMode.AUTO,
        pattern: FlashPattern = FlashPattern.DOUBLE,
        speed: FlashSpeed = FlashSpeed.NORMAL,
        duration: FlashDuration = FlashDuration.SEC_5
    ) {
        val caps = _deviceCapabilities.value
        val event = ActiveNotificationEvent(
            id = "test_${System.currentTimeMillis()}",
            packageName = "com.test.notification",
            appName = appName,
            title = "$appName Notification",
            message = "New message arrived! Testing RGB + Flash + AOD synchronization.",
            resolvedColor = color,
            effectType = EffectType.STATIC,
            isRearFlashActive = isRearFlash && caps.hasRearCameraFlash && caps.isCameraPermissionGranted,
            isFrontFlashActive = (frontMode == FrontFlashMode.HARDWARE_FRONT) && caps.hasFrontHardwareFlash,
            isScreenFlashActive = (frontMode == FrontFlashMode.SCREEN_FLASH) ||
                    (frontMode == FrontFlashMode.AUTO && !caps.hasFrontHardwareFlash),
            isAodActive = true,
            pattern = pattern,
            speed = speed,
            durationSeconds = duration.durationSeconds,
            aodStyle = _appSettings.value.aodStyle
        )
        notificationFlowEngine.triggerNotification(event)
    }

    fun stopNotificationFlow() {
        notificationFlowEngine.stopNotification()
    }

    // ================= LIGHT FLOW NOTIFICATION SYSTEM METHODS =================

    fun setLightFlowMasterEnabled(enabled: Boolean) {
        lightFlowRepo.setMasterEnabled(enabled)
        _lightFlowMasterEnabled.value = enabled
        vibrationManager.vibrateLedToggle(enabled)
    }

    fun saveLightFlowProfile(profile: LightFlowProfile) {
        val oldProfile = lightFlowRepo.getProfile(profile.packageName)
        val toggleChanged = oldProfile != null && oldProfile.isEnabled != profile.isEnabled
        lightFlowRepo.saveProfile(profile)
        _lightFlowProfiles.value = lightFlowRepo.getAllProfiles().values.toList()
        if (toggleChanged) {
            vibrationManager.vibrateLedToggle(profile.isEnabled)
        } else {
            vibrationManager.vibrateColorProfileSelected()
        }
    }

    fun deleteLightFlowProfile(packageName: String) {
        lightFlowRepo.deleteProfile(packageName)
        _lightFlowProfiles.value = lightFlowRepo.getAllProfiles().values.toList()
    }

    fun saveDefaultLightFlowProfile(profile: LightFlowProfile) {
        lightFlowRepo.saveDefaultProfile(profile)
        _defaultLightFlowProfile.value = profile
    }

    fun updateLightFlowAutomation(config: LightFlowAutomationConfig) {
        lightFlowRepo.saveAutomationConfig(config)
        _lightFlowAutomation.value = config
    }

    fun testLightFlowProfile(profile: LightFlowProfile) {
        notificationFlowEngine.triggerProfileNotification(
            profile = profile,
            title = "${profile.appName} LED Test",
            message = "Physical RGB LED flow verified"
        )
    }

    fun stopLightFlowTest() {
        notificationFlowEngine.stopNotification()
    }

    fun refreshNotificationListenerPermission() {
        _isNotificationListenerGranted.value = NotificationLightListenerService.isPermissionGranted(getApplication())
    }

    fun clearLightFlowHistory() {
        lightFlowRepo.clearHistory()
        _lightFlowHistory.value = emptyList()
    }

    fun resetLightFlowDefaults() {
        lightFlowRepo.resetToDefaults()
        _lightFlowProfiles.value = lightFlowRepo.getAllProfiles().values.toList()
        _defaultLightFlowProfile.value = lightFlowRepo.getDefaultProfile()
        _lightFlowAutomation.value = lightFlowRepo.getAutomationConfig()
    }

    fun extractColorsForApp(packageName: String): List<RgbColor> {
        return AppColorExtractor.extractColorsForPackage(getApplication(), packageName)
    }

    fun getInstalledApps(): List<InstalledAppItem> {
        val pm = getApplication<Application>().packageManager
        val installed = pm.getInstalledApplications(0)
        val profilePkgs = lightFlowRepo.getAllProfiles().keys

        return installed.mapNotNull { appInfo ->
            val pkg = appInfo.packageName
            val name = try {
                pm.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                pkg
            }
            InstalledAppItem(
                packageName = pkg,
                appName = name,
                hasProfile = profilePkgs.contains(pkg)
            )
        }.sortedBy { it.appName.lowercase() }
    }

    fun dismissAod() {
        notificationFlowEngine.dismissAod()
    }

    fun saveAll() {
        persistSettings()
    }

    fun resetSettings() {
        _ledState.value = LedState()
        _appSettings.value = AppSettings()
        persistSettings()
        applyHardwareChanges()
    }
}
