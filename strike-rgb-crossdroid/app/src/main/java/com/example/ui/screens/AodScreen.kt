package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AodBatteryStyle
import com.example.model.AodClockPosition
import com.example.model.AodClockSize
import com.example.model.AodClockStyle
import com.example.model.AodClockWeight
import com.example.model.AodDatePosition
import com.example.model.AodDateStyle
import com.example.model.AodEdgeAnimation
import com.example.model.AodEdgeGlowPosition
import com.example.model.AodTheme
import com.example.model.AodThemeManager
import com.example.model.FlashDuration
import com.example.model.FlashPattern
import com.example.model.FlashSpeed
import com.example.model.RgbColor
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassSliderItem
import com.example.ui.components.LiquidGlassSwitch
import com.example.ui.components.PixelSectionTitle
import com.example.localization.Strings
import com.example.viewmodel.RgbViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * First-Class Dedicated AOD (Always-On Display) Screen.
 * Full customization for 10 built-in themes, clocks, battery, edge glow, and live interactive preview.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AodScreen(
    viewModel: RgbViewModel,
    modifier: Modifier = Modifier
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val language = appSettings.language
    val aodConfig = appSettings.aodConfig

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("aod_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 1. MASTER AOD CARD
        item {
            LiquidGlassCard(
                modifier = Modifier.testTag("aod_master_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f)
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DisplaySettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = Strings.get("aod_enable", language),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (aodConfig.isEnabled) "AOD Active • Displaying Themes & Ambient Glow" else "AOD Disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (aodConfig.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        LiquidGlassSwitch(
                            checked = aodConfig.isEnabled,
                            onCheckedChange = { isEnabled ->
                                viewModel.updateAodConfig { it.copy(isEnabled = isEnabled) }
                            },
                            testTag = "aod_master_switch"
                        )
                    }

                    LiquidGlassButton(
                        text = Strings.get("aod_preview_fullscreen", language),
                        onClick = {
                            viewModel.testNotificationFlow(
                                pattern = FlashPattern.SINGLE,
                                speed = FlashSpeed.NORMAL,
                                duration = FlashDuration.SEC_5,
                                color = RgbColor.Blue
                            )
                        },
                        icon = Icons.Filled.PlayArrow,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_test_aod_fullscreen"
                    )
                }
            }
        }

        // 2. LIVE INTERACTIVE AOD MINI PREVIEW
        item {
            LiquidGlassCard(
                modifier = Modifier.testTag("aod_preview_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LIVE AOD PREVIEW",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
                                .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = Strings.get(aodConfig.theme.titleKey, language),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }

                    // Scaled phone display mockup
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.Black)
                            .border(
                                2.dp,
                                if (aodConfig.edgeGlow.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                else Color.DarkGray,
                                RoundedCornerShape(22.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = when (aodConfig.clockPosition) {
                            AodClockPosition.TOP -> Alignment.TopCenter
                            AodClockPosition.CENTER -> Alignment.Center
                        }
                    ) {
                        val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                        val dateFormat = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }
                        val now = Date()

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = timeFormat.format(now),
                                fontSize = 38.sp,
                                fontWeight = when (aodConfig.clockWeight) {
                                    AodClockWeight.THIN -> FontWeight.Thin
                                    AodClockWeight.LIGHT -> FontWeight.Light
                                    AodClockWeight.REGULAR -> FontWeight.Normal
                                    AodClockWeight.SEMI_BOLD -> FontWeight.SemiBold
                                    AodClockWeight.BOLD -> FontWeight.Bold
                                },
                                color = Color.White
                            )

                            Text(
                                text = dateFormat.format(now),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.75f)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "85%",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. 10 BUILT-IN AOD THEMES
        item {
            PixelSectionTitle(title = Strings.get("aod_themes", language))
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val themes = remember { AodTheme.entries }
                    themes.chunked(2).forEach { rowThemes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowThemes.forEach { theme ->
                                val isSelected = aodConfig.theme == theme
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
                                            viewModel.updateAodConfig { it.copy(theme = theme) }
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
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = theme.name.replace("_", " "),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. CLOCK STYLE & CUSTOMIZATION
        item {
            PixelSectionTitle(title = Strings.get("aod_clock_customization", language))
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = Strings.get("aod_clock_style", language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AodClockStyle.entries.forEach { style ->
                            val isSel = aodConfig.clockStyle == style
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        viewModel.updateAodConfig { it.copy(clockStyle = style) }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = Strings.get(style.titleKey, language),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Clock Position
                    Text(
                        text = Strings.get("aod_clock_position", language),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AodClockPosition.entries.forEach { pos ->
                            val isSel = aodConfig.clockPosition == pos
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        viewModel.updateAodConfig { it.copy(clockPosition = pos) }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = Strings.get(pos.titleKey, language),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. EDGE GLOW EFFECTS
        item {
            PixelSectionTitle(title = Strings.get("aod_edge_glow_effects", language))
            LiquidGlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.get("aod_edge_glow_enable", language),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        LiquidGlassSwitch(
                            checked = aodConfig.edgeGlow.isEnabled,
                            onCheckedChange = { isEnabled ->
                                viewModel.updateAodConfig { it.copy(edgeGlow = it.edgeGlow.copy(isEnabled = isEnabled)) }
                            }
                        )
                    }

                    if (aodConfig.edgeGlow.isEnabled) {
                        LiquidGlassSliderItem(
                            title = Strings.get("aod_edge_glow_thickness", language),
                            value = aodConfig.edgeGlow.thickness,
                            onValueChange = { thick ->
                                viewModel.updateAodConfig { it.copy(edgeGlow = it.edgeGlow.copy(thickness = thick)) }
                            },
                            valueRange = 2f..24f,
                            displayFormatter = { "${it.toInt()} dp" }
                        )

                        LiquidGlassSliderItem(
                            title = Strings.get("aod_edge_glow_speed", language),
                            value = aodConfig.edgeGlow.speed,
                            onValueChange = { spd ->
                                viewModel.updateAodConfig { it.copy(edgeGlow = it.edgeGlow.copy(speed = spd)) }
                            },
                            valueRange = 0.5f..3.0f,
                            displayFormatter = { String.format(Locale.US, "%.1fx", it) }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
