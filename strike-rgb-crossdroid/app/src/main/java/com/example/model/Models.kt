package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.roundToInt

data class RgbColor(
    val r: Int = 0,
    val g: Int = 108,
    val b: Int = 232
) {
    fun toComposeColor(): Color = Color(r, g, b)

    fun toArgbInt(): Int = android.graphics.Color.rgb(r, g, b)

    fun toHex(): String = String.format("#%02X%02X%02X", r, g, b)

    fun luminance(): Float = (0.299f * r + 0.587f * g + 0.114f * b) / 255f

    fun isLight(): Boolean = luminance() > 0.5f

    companion object {
        val Blue = RgbColor(11, 87, 208)        // Pixel Blue
        val Purple = RgbColor(140, 79, 255)     // Pixel Violet
        val Green = RgbColor(30, 142, 62)       // Pixel Green
        val Mint = RgbColor(0, 168, 132)        // Pixel Mint
        val Coral = RgbColor(234, 67, 53)       // Pixel Coral Red
        val Amber = RgbColor(249, 171, 0)       // Pixel Amber
        val Cyan = RgbColor(0, 163, 196)        // Pixel Cyan
        val White = RgbColor(255, 255, 255)
        val Red = RgbColor(220, 38, 38)
        val Yellow = RgbColor(234, 179, 8)

        fun fromHsv(h: Float, s: Float, v: Float): RgbColor {
            val hsv = floatArrayOf(h, s, v)
            val colorInt = android.graphics.Color.HSVToColor(hsv)
            return RgbColor(
                r = android.graphics.Color.red(colorInt),
                g = android.graphics.Color.green(colorInt),
                b = android.graphics.Color.blue(colorInt)
            )
        }
    }
}

enum class EffectType(val displayNameKey: String) {
    STATIC("effect_static"),
    BREATHING("effect_breathing"),
    HEARTBEAT("effect_heartbeat"),
    PULSE("effect_pulse"),
    WAVE("effect_wave"),
    RAINBOW("effect_rainbow"),
    RAINBOW_CYCLE("effect_rainbow_cycle"),
    STROBE("effect_strobe"),
    FLASH("effect_flash"),
    DOUBLE_FLASH("effect_double_flash"),
    TRIPLE_FLASH("effect_triple_flash"),
    FADE("effect_fade"),
    COLOR_CYCLE("effect_color_cycle"),
    SMOOTH_GRADIENT("effect_smooth_gradient"),
    AURORA("effect_aurora"),
    SPARKLE("effect_sparkle"),
    SCREEN_SYNC("effect_screen_sync"),
    AMBILIGHT("effect_screen_sync")
}

enum class ScreenRegion(val displayNameKey: String) {
    FULL_SCREEN("region_full_screen"),
    TOP("region_top"),
    CENTER("region_center"),
    BOTTOM("region_bottom")
}

data class ScreenSyncConfig(
    val isEnabled: Boolean = false,
    val intensity: Float = 1.0f,            // 0f .. 1f
    val brightness: Float = 0.85f,          // 0f .. 1f
    val smoothing: Float = 0.50f,           // 0f .. 1f (Low / Med / High)
    val colorBoost: Boolean = true,
    val samplingRate: Int = 15,             // 10 .. 20 Hz
    val region: ScreenRegion = ScreenRegion.FULL_SCREEN,
    val keepColorOnScreenOff: Boolean = false
)

data class ScreenSyncDebugInfo(
    val isRunning: Boolean = false,
    val permissionGranted: Boolean = false,
    val detectedColor: RgbColor = RgbColor(0, 0, 0),
    val smoothedLedColor: RgbColor = RgbColor(0, 0, 0),
    val currentFps: Int = 0,
    val isRootAvailable: Boolean = false,
    val isLedDetected: Boolean = false
)

data class EffectEngineDiagnostic(
    val effect: EffectType = EffectType.STATIC,
    val isRunning: Boolean = false,
    val currentFps: Int = 0,
    val currentRgb: RgbColor = RgbColor(0, 0, 0),
    val frameCount: Long = 0L,
    val isRootAvailable: Boolean = false,
    val isLedDetected: Boolean = false,
    val lastError: String? = null
)

data class LedState(
    val isRunning: Boolean = true,
    val currentColor: RgbColor = RgbColor.Blue,
    val currentEffect: EffectType = EffectType.STATIC,
    val brightness: Float = 0.85f,          // 0f .. 1f
    val effectSpeed: Float = 1.0f,          // 0.2f .. 3.0f
    val breathingSpeed: Float = 2.0f,       // in seconds
    val intensity: Float = 1.0f,            // 0f .. 1f (Effect amplitude)
    val smoothTransition: Boolean = true,
    val saveLastEffect: Boolean = true,
    val screenSyncConfig: ScreenSyncConfig = ScreenSyncConfig(),
    val screenSyncDebugInfo: ScreenSyncDebugInfo = ScreenSyncDebugInfo()
)

enum class RootStatus {
    ROOT_AVAILABLE,
    ROOT_GRANTED,
    ROOT_DENIED,
    ROOT_NOT_FOUND,
    ROOT_ERROR,
    ROOT_REVOKED
}

enum class HardwareMode {
    ROOT,
    NON_ROOT,
    UNAVAILABLE
}

data class CommandResult(
    val success: Boolean,
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

data class HardwareTestResult(
    val success: Boolean,
    val message: String,
    val errorDetails: String? = null
)

data class HardwareStatus(
    val isRootConnected: Boolean = false,
    val isLedDetected: Boolean = false,
    val ledPath: String = "/sys/class/leds/red",
    val chipModel: String = "Standard sysfs LED",
    val uid: Int? = null,
    val apiLevel: Int = android.os.Build.VERSION.SDK_INT,
    val minAndroidVersion: String = "Android 2.1 (API 7)",
    val isHardwareTestRunning: Boolean = false,
    val rootStatus: RootStatus = RootStatus.ROOT_NOT_FOUND,
    val rootProvider: String = "Magisk / SU",
    val hardwareMode: HardwareMode = HardwareMode.UNAVAILABLE,
    val driver: String = "generic-leds",
    val redPath: String? = null,
    val greenPath: String? = null,
    val bluePath: String? = null,
    val isRedAvailable: Boolean = false,
    val isGreenAvailable: Boolean = false,
    val isBlueAvailable: Boolean = false,
    val maxBrightness: Int = 255,
    val statusMessage: String? = null
)

enum class AppLanguage(val code: String, val title: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    FRENCH("fr", "French", "Français"),
    ARABIC("ar", "Arabic", "العربية"),
    DARIJA("ary", "Darija", "الدارجة")
}

enum class AppThemeMode(val titleKey: String) {
    SYSTEM("theme_system"),
    LIGHT("theme_light"),
    DARK("theme_dark")
}

enum class UiTheme(val titleKey: String) {
    NEWBIE_UI_26("theme_newbieui26"),
    ULTIMATE_LIQUID_GLASS("theme_ultimate_liquid_glass"),
    XML_THEME("theme_xml")
}

enum class QualityLevel(val titleKey: String) {
    LOW("quality_low"),
    BALANCED("quality_balanced"),
    HIGH("quality_high")
}

enum class AodTheme(val titleKey: String) {
    NEWBIE_MINIMAL("aod_theme_newbie_minimal"),
    PIXEL("aod_theme_pixel"),
    DIGITAL("aod_theme_digital"),
    ANALOG("aod_theme_analog"),
    NEON("aod_theme_neon"),
    RGB("aod_theme_rgb"),
    AURORA("aod_theme_aurora"),
    LIQUID_GLASS("aod_theme_liquid_glass"),
    EDGE_GLOW("aod_theme_edge_glow"),
    AMBIENT("aod_theme_ambient")
}

enum class AodClockType(val titleKey: String) {
    DIGITAL("clock_digital"),
    ANALOG("clock_analog")
}

enum class AodClockStyle(val titleKey: String) {
    LIQUID_GLASS("aod_style_liquid_glass"),
    MINIMAL("aod_style_minimal"),
    PIXEL("aod_style_pixel"),
    DIGITAL("aod_style_digital"),
    ANALOG("aod_style_analog")
}

enum class AodClockWeight(val titleKey: String) {
    THIN("weight_thin"),
    LIGHT("weight_light"),
    REGULAR("weight_regular"),
    SEMI_BOLD("weight_semibold"),
    BOLD("weight_bold")
}

enum class AodClockSize(val titleKey: String) {
    SMALL("clock_size_small"),
    MEDIUM("clock_size_medium"),
    LARGE("clock_size_large"),
    EXTRA_LARGE("clock_size_xlarge")
}

enum class AodClockPosition(val titleKey: String) {
    TOP("clock_pos_top"),
    CENTER("clock_pos_center")
}

enum class AodDatePosition(val titleKey: String) {
    BELOW_CLOCK("date_pos_below"),
    ABOVE_CLOCK("date_pos_above")
}

enum class AodDateStyle(val titleKey: String) {
    PILL("date_style_pill"),
    MINIMAL("date_style_minimal"),
    GLOW("date_style_glow"),
    UPPERCASE("date_style_uppercase")
}

enum class AodBatteryStyle(val titleKey: String) {
    CAPSULE("battery_style_capsule"),
    CIRCULAR_RING("battery_style_ring"),
    MINIMAL_BAR("battery_style_bar"),
    PERCENT_ONLY("battery_style_percent_only"),
    ICON_ONLY("battery_style_icon_only")
}

enum class AodEdgeAnimation(val titleKey: String) {
    BREATHING("edge_anim_breathing"),
    PULSE("edge_anim_pulse"),
    WAVE("edge_anim_wave"),
    STATIC("edge_anim_static")
}

enum class AodEdgeGlowPosition(val titleKey: String) {
    FULL("edge_pos_full"),
    TOP("edge_pos_top"),
    BOTTOM("edge_pos_bottom"),
    LEFT("edge_pos_left"),
    RIGHT("edge_pos_right")
}

enum class AodBackgroundType(val titleKey: String) {
    DEFAULT("bg_default_oled"),
    SOLID("bg_solid"),
    GRADIENT("bg_gradient"),
    CUSTOM_IMAGE("bg_custom_image")
}

data class AodEdgeGlowConfig(
    val isEnabled: Boolean = true,
    val position: AodEdgeGlowPosition = AodEdgeGlowPosition.FULL,
    val color: RgbColor = RgbColor.Blue,
    val isRgbCycle: Boolean = false,
    val brightness: Float = 0.85f,          // 0f .. 1f
    val thickness: Float = 6.0f,             // in dp (2dp .. 16dp)
    val glowRadius: Float = 18.0f,          // in dp (4dp .. 32dp)
    val speed: Float = 1.0f,                // 0.2f .. 3.0f
    val durationSeconds: Int = 10,
    val animation: AodEdgeAnimation = AodEdgeAnimation.BREATHING
)

data class AodConfig(
    val isEnabled: Boolean = true,
    val theme: AodTheme = AodTheme.LIQUID_GLASS,
    val clockType: AodClockType = AodClockType.DIGITAL,
    val clockStyle: AodClockStyle = AodClockStyle.LIQUID_GLASS,
    val clockSize: AodClockSize = AodClockSize.LARGE,
    val clockWeight: AodClockWeight = AodClockWeight.REGULAR,
    val clockPosition: AodClockPosition = AodClockPosition.TOP,
    val clockColor: RgbColor = RgbColor.White,
    val isAutoColor: Boolean = true,
    val is24Hour: Boolean = true,
    val showDate: Boolean = true,
    val dateFormat: String = "EEE, MMM d",
    val datePosition: AodDatePosition = AodDatePosition.BELOW_CLOCK,
    val dateStyle: AodDateStyle = AodDateStyle.PILL,
    val clockOpacity: Float = 0.95f,
    val clockBlur: Float = 0.50f,
    val clockRefraction: Float = 0.65f,
    val clockGloss: Float = 0.80f,
    val clockGlow: Float = 0.70f,
    val clockThickness: Float = 0.60f,
    val backgroundInteraction: Boolean = true,
    val showBatteryPercentage: Boolean = true,
    val showBatteryIcon: Boolean = true,
    val showBatteryRing: Boolean = false,
    val batteryStyle: AodBatteryStyle = AodBatteryStyle.CAPSULE,
    val showNotificationIcons: Boolean = true,
    val notificationAnimation: String = "glow",
    val useAppColorForNotification: Boolean = true,
    val backgroundType: AodBackgroundType = AodBackgroundType.DEFAULT,
    val backgroundSolidColor: RgbColor = RgbColor(11, 13, 16),
    val backgroundBlur: Float = 0.35f,
    val backgroundDarkOverlay: Float = 0.40f,
    val backgroundBrightness: Float = 0.75f,
    val backgroundRefraction: Float = 0.50f,
    val brightness: Float = 0.80f,
    val edgeGlow: AodEdgeGlowConfig = AodEdgeGlowConfig(),
    val glowEffect: Boolean = true,
    val pulseEffect: Boolean = false
)

/**
 * Curated Color Profile with high-contrast tactile vibrancy
 */
data class ColorProfile(
    val id: String,
    val name: String,
    val color: RgbColor,
    val subtitle: String = ""
)

val BuiltInColorProfiles = listOf(
    ColorProfile("cyber_cyan", "Cyber Cyan", RgbColor(0, 240, 255), "Hyper Neon"),
    ColorProfile("electric_violet", "Electric Violet", RgbColor(140, 79, 255), "Ultra Pixel"),
    ColorProfile("matrix_mint", "Matrix Mint", RgbColor(0, 220, 130), "Vivid Green"),
    ColorProfile("sunset_coral", "Sunset Coral", RgbColor(255, 87, 51), "Warm Glow"),
    ColorProfile("deep_space", "Deep Space", RgbColor(11, 87, 208), "Pixel Royal"),
    ColorProfile("hot_magenta", "Hot Magenta", RgbColor(255, 0, 128), "Laser Pink"),
    ColorProfile("solar_amber", "Solar Amber", RgbColor(255, 170, 0), "Golden Heat"),
    ColorProfile("arctic_ice", "Arctic Ice", RgbColor(160, 230, 255), "Frost Glow"),
    ColorProfile("pure_moonlight", "Pure Moonlight", RgbColor(255, 255, 255), "Crisp White"),
    ColorProfile("crimson_rage", "Crimson Rage", RgbColor(230, 20, 40), "Deep Red")
)

data class AppSettings(
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val uiTheme: UiTheme = UiTheme.ULTIMATE_LIQUID_GLASS,
    val qualityLevel: QualityLevel = QualityLevel.BALANCED,
    val dynamicColor: Boolean = true,
    val vibrationFeedback: Boolean = true,
    val startOnBoot: Boolean = false,
    val screenColor: Boolean = false,
    val smoothTransition: Boolean = true,
    val saveLastEffect: Boolean = true,
    val keepLedOnScreenOff: Boolean = true,
    val samplingRate: Int = 30,             // Hz (10 .. 60)
    val sensitivity: Float = 0.75f,         // 0f .. 1f
    val smoothing: Float = 0.50f,           // 0f .. 1f
    val defaultEffect: EffectType = EffectType.STATIC,
    val screenSyncConfig: ScreenSyncConfig = ScreenSyncConfig(),
    val customBackgroundPath: String? = null,
    val backgroundBlurIntensity: Float = 0.0f,     // 0f .. 1f (0% sharp, 100% heavy blur)
    val backgroundOverlay: Float = 0.30f,          // 0f .. 1f (overlay tint darkness/brightness for legibility)
    val notificationOutputMode: NotificationOutputMode = NotificationOutputMode.RGB_FLASH_AOD,
    val globalRearFlash: Boolean = true,
    val globalFrontFlashMode: FrontFlashMode = FrontFlashMode.AUTO,
    val globalFlashPattern: FlashPattern = FlashPattern.DOUBLE,
    val globalFlashSpeed: FlashSpeed = FlashSpeed.NORMAL,
    val globalFlashDuration: FlashDuration = FlashDuration.SEC_5,
    val screenOffMasterControl: ScreenOffMasterControl = ScreenOffMasterControl.EVERYTHING,
    val aodStyle: AodNotificationStyle = AodNotificationStyle.RGB_GLOW,
    val aodColorSource: AodColorSource = AodColorSource.AUTO_APP_COLOR,
    val aodConfig: AodConfig = AodConfig()
)
