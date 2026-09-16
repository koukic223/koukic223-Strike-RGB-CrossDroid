package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Color modes supported by Light Flow for notifications
 */
enum class LightFlowColorMode(val titleKey: String) {
    AUTO_APP_COLOR("light_flow_mode_auto"),
    SINGLE_COLOR("light_flow_mode_single"),
    MULTI_COLOR("light_flow_mode_multi"),
    COLOR_SEQUENCE("light_flow_mode_sequence"),
    RAINBOW("light_flow_mode_rainbow"),
    SCREEN_COLOR("light_flow_mode_screen")
}

/**
 * Multi-color sequence switching behavior
 */
enum class ColorSequenceBehavior(val titleKey: String) {
    CYCLE("sequence_cycle"),
    SEQUENCE("sequence_smooth"),
    RANDOM("sequence_random"),
    NOTIFICATION_TYPE("sequence_notif_type")
}

/**
 * Notification Priority levels:
 * Higher priority notifications override lower ones temporarily.
 */
enum class NotificationPriority(val level: Int, val titleKey: String) {
    LOW(1, "priority_low"),
    NORMAL(2, "priority_normal"),
    HIGH(3, "priority_high"),
    CRITICAL(4, "priority_critical")
}

/**
 * Flash modes for Light Flow profiles
 */
enum class LightFlowFlashMode(val titleKey: String) {
    OFF("flash_mode_off"),
    SINGLE("flash_mode_single"),
    DOUBLE("flash_mode_double"),
    TRIPLE("flash_mode_triple"),
    HEARTBEAT("flash_mode_heartbeat"),
    STROBE("flash_mode_strobe"),
    SYNC_RGB("flash_mode_sync_rgb")
}

/**
 * Comprehensive per-app Light Flow notification profile
 */
data class LightFlowProfile(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean = true,
    val colorMode: LightFlowColorMode = LightFlowColorMode.AUTO_APP_COLOR,
    val colors: List<RgbColor> = listOf(RgbColor(37, 211, 102)),
    val detectedColors: List<RgbColor> = emptyList(),
    val sequenceBehavior: ColorSequenceBehavior = ColorSequenceBehavior.CYCLE,
    val effect: EffectType = EffectType.BREATHING,
    val brightness: Float = 0.85f,
    val speed: Float = 1.0f,
    val durationSeconds: Int = 5,
    val repeatCount: Int = 3,
    val delayMs: Long = 200L,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val flashMode: LightFlowFlashMode = LightFlowFlashMode.OFF,
    val aodStyle: AodNotificationStyle = AodNotificationStyle.RGB_GLOW,
    val aodGlowEnabled: Boolean = true,
    val aodEdgeGlowEnabled: Boolean = true,
    val screenOffEnabled: Boolean = true,
    val isCustomized: Boolean = false
) {
    val primaryColor: RgbColor
        get() = when (colorMode) {
            LightFlowColorMode.AUTO_APP_COLOR -> detectedColors.firstOrNull() ?: colors.firstOrNull() ?: RgbColor.Blue
            LightFlowColorMode.RAINBOW -> RgbColor.Red
            else -> colors.firstOrNull() ?: RgbColor.Blue
        }
}

/**
 * Automation conditions determining when Light Flow operates
 */
data class LightFlowAutomationConfig(
    val screenOnEnabled: Boolean = true,
    val screenOffEnabled: Boolean = true,
    val chargingOnly: Boolean = false,
    val respectDnd: Boolean = true,
    val headphonesOnly: Boolean = false,
    val aodActiveOnly: Boolean = false
)

/**
 * Historical record of detected notifications with colors and timing
 */
data class NotificationHistoryItem(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val message: String,
    val detectedColor: RgbColor,
    val effect: EffectType,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Installed app info item for profile selection picker
 */
data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val hasProfile: Boolean = false
)
