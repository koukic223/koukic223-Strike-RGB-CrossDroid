package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Output channel selections for the Notification Light Flow engine.
 */
enum class NotificationOutputMode(val titleKey: String) {
    RGB_ONLY("output_rgb_only"),
    REAR_FLASH_ONLY("output_rear_flash_only"),
    FRONT_SCREEN_FLASH_ONLY("output_front_screen_flash_only"),
    AOD_ONLY("output_aod_only"),
    RGB_AND_REAR_FLASH("output_rgb_rear_flash"),
    RGB_AND_FRONT_FLASH("output_rgb_front_flash"),
    RGB_AND_AOD("output_rgb_aod"),
    RGB_FLASH_AOD("output_rgb_flash_aod"),
    ALL("output_all")
}

/**
 * Flash Pattern modes requested by user:
 * - Single (ON -> OFF)
 * - Double (ON -> OFF -> ON -> OFF)
 * - Triple (ON -> OFF -> ON -> OFF -> ON -> OFF)
 * - Heartbeat (ON -> OFF -> ON -> OFF -> Long Pause)
 * - Strobe (Rapid repeated ON -> OFF)
 * - SOS (... --- ...)
 */
enum class FlashPattern(val titleKey: String) {
    SINGLE("pattern_single"),
    DOUBLE("pattern_double"),
    TRIPLE("pattern_triple"),
    HEARTBEAT("pattern_heartbeat"),
    STROBE("pattern_strobe"),
    SOS("pattern_sos")
}

/**
 * Flash Speed settings:
 * - Slow (400ms pulse)
 * - Normal (200ms pulse)
 * - Fast (100ms pulse)
 * - Very Fast (60ms pulse)
 */
enum class FlashSpeed(val titleKey: String, val pulseDurationMs: Long) {
    SLOW("speed_slow", 400L),
    NORMAL("speed_normal", 200L),
    FAST("speed_fast", 100L),
    VERY_FAST("speed_very_fast", 60L);

    companion object {
        fun fromProgress(progress: Float): FlashSpeed {
            return when {
                progress < 0.25f -> SLOW
                progress < 0.60f -> NORMAL
                progress < 0.85f -> FAST
                else -> VERY_FAST
            }
        }
    }
}

/**
 * Flash Duration limits (failsafe timeout):
 * 1s, 3s, 5s, 10s, 30s, or until dismissed (with safety maximum failsafe of 60s).
 */
enum class FlashDuration(val titleKey: String, val durationSeconds: Int) {
    SEC_1("duration_1s", 1),
    SEC_3("duration_3s", 3),
    SEC_5("duration_5s", 5),
    SEC_10("duration_10s", 10),
    SEC_30("duration_30s", 30),
    UNTIL_DISMISSED("duration_until_dismissed", 60)
}

/**
 * Front flash mode:
 * Auto (Hardware front flash if available, else Screen Flash),
 * Hardware Front Flash only,
 * Screen Flash only,
 * Disabled.
 */
enum class FrontFlashMode(val titleKey: String) {
    AUTO("front_flash_auto"),
    HARDWARE_FRONT("front_flash_hardware"),
    SCREEN_FLASH("front_flash_screen"),
    DISABLED("front_flash_disabled")
}

/**
 * AOD Notification style:
 * Minimal (Clock + Icon)
 * Icon (App icon only)
 * Color (Notification color badge)
 * RGB Glow (Colored glow around icon)
 * Full Notification (App name + message preview)
 */
enum class AodNotificationStyle(val titleKey: String) {
    MINIMAL("aod_style_minimal"),
    ICON("aod_style_icon"),
    COLOR("aod_style_color"),
    RGB_GLOW("aod_style_glow"),
    FULL("aod_style_full")
}

/**
 * AOD Color source:
 * Auto App Color, Custom Color, or Notification Color
 */
enum class AodColorSource(val titleKey: String) {
    AUTO_APP_COLOR("aod_color_auto"),
    NOTIFICATION_COLOR("aod_color_notification"),
    CUSTOM("aod_color_custom")
}

/**
 * Screen Off Master Control options:
 */
enum class ScreenOffMasterControl(val titleKey: String) {
    RGB_ONLY("screen_off_rgb_only"),
    RGB_REAR_FLASH("screen_off_rgb_rear_flash"),
    RGB_FRONT_SCREEN("screen_off_rgb_front_screen"),
    RGB_AOD("screen_off_rgb_aod"),
    RGB_REAR_FLASH_AOD("screen_off_rgb_rear_flash_aod"),
    EVERYTHING("screen_off_everything")
}

/**
 * Full device hardware capability detection report
 */
data class DeviceCapabilities(
    val hasRgbLed: Boolean = false,
    val hasRearCameraFlash: Boolean = false,
    val hasFrontHardwareFlash: Boolean = false,
    val hasScreenFlash: Boolean = true, // Universal fallback
    val hasAodSupport: Boolean = false,
    val aodReason: String? = null,
    val rearCameraId: String? = null,
    val frontCameraId: String? = null,
    val isCameraPermissionGranted: Boolean = false
)

/**
 * App-specific notification profile
 */
data class AppNotificationProfile(
    val packageName: String,
    val appName: String,
    val defaultRgbColor: RgbColor,
    val rgbEffect: EffectType = EffectType.STATIC,
    val isRearFlashEnabled: Boolean = false,
    val frontFlashMode: FrontFlashMode = FrontFlashMode.AUTO,
    val flashPattern: FlashPattern = FlashPattern.DOUBLE,
    val flashSpeed: FlashSpeed = FlashSpeed.NORMAL,
    val flashDuration: FlashDuration = FlashDuration.SEC_5,
    val aodStyle: AodNotificationStyle = AodNotificationStyle.RGB_GLOW,
    val aodColorSource: AodColorSource = AodColorSource.AUTO_APP_COLOR,
    val customAodColor: RgbColor = defaultRgbColor,
    val isEnabled: Boolean = true
)

/**
 * Active Notification Event dispatched to the unified engine
 */
data class ActiveNotificationEvent(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val message: String,
    val resolvedColor: RgbColor,
    val effectType: EffectType,
    val isRearFlashActive: Boolean,
    val isFrontFlashActive: Boolean,
    val isScreenFlashActive: Boolean,
    val isAodActive: Boolean,
    val pattern: FlashPattern,
    val speed: FlashSpeed,
    val durationSeconds: Int,
    val aodStyle: AodNotificationStyle,
    val timestamp: Long = System.currentTimeMillis()
)
