package com.example.ui.screens

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.localization.Strings
import com.example.model.AppLanguage
import com.example.model.HardwareStatus
import com.example.model.LedState
import com.example.model.RgbColor
import com.example.model.UiTheme
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassTheme
import com.example.ui.components.PixelLedOrb
import com.example.ui.components.PixelSectionTitle
import com.example.ui.components.PixelStatusDot

@Composable
fun HomeScreen(
    ledState: LedState,
    hardwareStatus: HardwareStatus,
    language: AppLanguage,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRequestRootDialog: () -> Unit,
    modifier: Modifier = Modifier,
    uiTheme: UiTheme = UiTheme.NEWBIE_UI_26,
    onToggleLed: (() -> Unit)? = null,
    onColorSelect: ((RgbColor) -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    // Live Android Battery Telemetry for Matrix Battery Dr XML feature
    val batteryIntent = remember(context) {
        context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
    val batteryLevel = remember(batteryIntent) {
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        if (level >= 0 && scale > 0) ((level.toFloat() / scale.toFloat()) * 100).toInt() else 85
    }
    val isCharging = remember(batteryIntent) {
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top App Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.final_cut_pro_icon),
                    contentDescription = "Strike RGB CrossDroid Icon",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = Strings.get("app_title", language),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("app_title_text")
                    )
                    Text(
                        text = Strings.get("app_version", language) + " • API 7+",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.testTag("app_version_text")
                    )
                }
            }

            // Power status pill with Liquid Glass & tactile toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        if (onToggleLed != null) onToggleLed()
                        else if (ledState.isRunning) onStop() else onStart()
                    }
                    .background(
                        if (ledState.isRunning) LiquidGlassTheme.highlightPillBrush(isDark)
                        else LiquidGlassTheme.surfaceBrush(isDark)
                    )
                    .border(
                        1.dp,
                        if (ledState.isRunning) SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        else LiquidGlassTheme.borderBrush(isDark),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelStatusDot(isActive = ledState.isRunning)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (ledState.isRunning) Strings.get("running", language) else Strings.get("stopped", language),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (ledState.isRunning) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Center Hardware Glowing LED Orb with live simulation & tap to toggle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    if (onToggleLed != null) onToggleLed()
                    else if (ledState.isRunning) onStop() else onStart()
                },
            contentAlignment = Alignment.Center
        ) {
            PixelLedOrb(
                color = ledState.currentColor,
                isRunning = ledState.isRunning,
                effect = ledState.currentEffect,
                brightness = ledState.brightness,
                speed = ledState.effectSpeed,
                sizeDp = 130
            )
        }

        // Color & Mode Tag with Liquid Glass
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(LiquidGlassTheme.surfaceBrush(isDark))
                .border(1.dp, LiquidGlassTheme.borderBrush(isDark), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(ledState.currentColor.toComposeColor())
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "${Strings.get("active_mode", language)}: ${Strings.get(ledState.currentEffect.displayNameKey, language)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        // Quick Tactile Color Palette Swatches
        Row(
            modifier = Modifier
                .padding(top = 10.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val quickProfiles = remember {
                listOf(
                    RgbColor(0, 240, 255),    // Cyber Cyan
                    RgbColor(140, 79, 255),   // Electric Violet
                    RgbColor(0, 220, 130),    // Matrix Mint
                    RgbColor(255, 87, 51),    // Sunset Coral
                    RgbColor(11, 87, 208),    // Pixel Blue
                    RgbColor(255, 0, 128),    // Hot Magenta
                    RgbColor(255, 170, 0),    // Solar Amber
                    RgbColor(255, 255, 255)   // Pure White
                )
            }
            for (qc in quickProfiles) {
                val isSel = ledState.currentColor == qc
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(qc.toComposeColor())
                        .border(
                            width = if (isSel) 2.5.dp else 1.dp,
                            brush = if (isSel) SolidColor(MaterialTheme.colorScheme.onSurface) else LiquidGlassTheme.borderBrush(isDark),
                            shape = CircleShape
                        )
                        .clickable {
                            onColorSelect?.invoke(qc)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSel) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (qc.isLight()) Color.Black else Color.White)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // RGB LED STATUS Section (Section 56)
        Column(modifier = Modifier.fillMaxWidth()) {
            PixelSectionTitle(title = Strings.get("rgb_led_status", language))

            LiquidGlassCard(
                testTag = "rgb_status_card",
                onClick = { if (!hardwareStatus.isRootConnected) onRequestRootDialog() }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Item 1: Root Connected
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PixelStatusDot(isActive = hardwareStatus.isRootConnected)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (hardwareStatus.isRootConnected)
                                    Strings.get("root_connected", language)
                                else
                                    Strings.get("root_disconnected", language),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (hardwareStatus.isRootConnected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (hardwareStatus.isRootConnected) Strings.get("uid_label", language) else "No Root",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (hardwareStatus.isRootConnected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }

                    // Item 2: RGB LED Detected
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PixelStatusDot(isActive = hardwareStatus.isLedDetected)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (hardwareStatus.isLedDetected)
                                    Strings.get("led_detected", language)
                                else
                                    Strings.get("led_not_detected", language),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Sysfs Node Details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiquidGlassTheme.surfaceBrush(isDark))
                            .border(1.dp, LiquidGlassTheme.borderBrush(isDark), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${Strings.get("node_path", language)}${hardwareStatus.ledPath}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Matrix Battery Dr & Prismal Telemetry Card (New XML Assets Integration)
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            testTag = "matrix_battery_telemetry_card"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_battery_prismal),
                            contentDescription = "Matrix Battery Dr",
                            tint = Color(0xFF00D68F), // Prismal Matrix Green
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = Strings.get("matrix_battery_telemetry", language),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    // Prismal active chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF00D68F).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF00D68F).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_matrix_chip),
                            contentDescription = null,
                            tint = Color(0xFF00D68F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCharging) Strings.get("battery_charging", language) else Strings.get("battery_discharging", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00D68F)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Telemetry metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Battery Level Metric
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiquidGlassTheme.surfaceBrush(isDark))
                            .border(1.dp, LiquidGlassTheme.borderBrush(isDark), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = Strings.get("battery_level", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$batteryLevel%",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00D68F)
                            )
                        )
                    }

                    // Prismal Engine Health
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiquidGlassTheme.surfaceBrush(isDark))
                            .border(1.dp, LiquidGlassTheme.borderBrush(isDark), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = Strings.get("battery_health", language),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Strings.get("prismal_engine", language),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF) // Prismal Cyan
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main controls: START & STOP (Liquid Glass floating buttons with spring motion)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LiquidGlassButton(
                text = Strings.get("start", language),
                onClick = onStart,
                icon = Icons.Default.PlayArrow,
                enabled = !ledState.isRunning,
                modifier = Modifier.weight(1f),
                testTag = "start_button"
            )

            LiquidGlassButton(
                text = Strings.get("stop", language),
                onClick = onStop,
                icon = Icons.Default.Stop,
                enabled = ledState.isRunning,
                isDestructive = true,
                modifier = Modifier.weight(1f),
                testTag = "stop_button"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
