package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppLanguage
import com.example.model.ColorSequenceBehavior
import com.example.model.EffectType
import com.example.model.InstalledAppItem
import com.example.model.LightFlowAutomationConfig
import com.example.model.LightFlowColorMode
import com.example.model.LightFlowFlashMode
import com.example.model.LightFlowProfile
import com.example.model.NotificationPriority
import com.example.model.RgbColor
import com.example.ui.components.LiquidGlassBadge
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassSliderItem
import com.example.ui.components.LiquidGlassSwitch
import com.example.viewmodel.RgbViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * LIGHT FLOW NOTIFICATION SYSTEM SCREEN
 * Complete configurable RGB notification engine with:
 * - Real hardware RGB LED output (Root / Magisk / Sysfs)
 * - Fluid HSV/HSL color transitions (no harsh jumps)
 * - Per-app notification profiles (WhatsApp, Instagram, YouTube, Facebook, etc.)
 * - Automatic app color and icon color extraction
 * - Multi-color sequences, cycles, gradients, and all 16 physical LED effects
 * - Camera flash & AOD edge glow synchronization
 * - ULTIMATE NEWBIE LIQUID GLASS styling
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LightFlowScreen(
    viewModel: RgbViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val masterEnabled by viewModel.lightFlowMasterEnabled.collectAsState()
    val profiles by viewModel.lightFlowProfiles.collectAsState()
    val defaultProfile by viewModel.defaultLightFlowProfile.collectAsState()
    val automation by viewModel.lightFlowAutomation.collectAsState()
    val history by viewModel.lightFlowHistory.collectAsState()
    val isPermissionGranted by viewModel.isNotificationListenerGranted.collectAsState()
    val activeLedOutput by viewModel.activeLedOutput.collectAsState()
    val activeNotification by viewModel.activeNotificationEvent.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var editingProfile by remember { mutableStateOf<LightFlowProfile?>(null) }
    var showAppPicker by remember { mutableStateOf(false) }
    var isTestingActive by remember { mutableStateOf(false) }

    val filteredProfiles = remember(profiles, searchQuery) {
        if (searchQuery.isBlank()) profiles
        else profiles.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("light_flow_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // 1. MASTER HEADER & TOGGLE
        item {
            LiquidGlassCard(
                modifier = Modifier.testTag("light_flow_master_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
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
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Light Flow Notification Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (masterEnabled) "Active • Controlling Physical RGB LED" else "Suspended • Notifications disabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (masterEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LiquidGlassSwitch(
                        checked = masterEnabled,
                        onCheckedChange = { viewModel.setLightFlowMasterEnabled(it) },
                        testTag = "light_flow_master_switch"
                    )
                }
            }
        }

        // 2. PERMISSION ALERT BANNER (If notification listener is not enabled)
        if (!isPermissionGranted) {
            item {
                LiquidGlassCard(
                    modifier = Modifier.testTag("permission_banner")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Notification Listener Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "Strike RGB CrossDroid needs notification access to detect app notifications, extract colors, and trigger the physical RGB LED.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LiquidGlassButton(
                                text = "Grant Access",
                                onClick = {
                                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    context.startActivity(intent)
                                },
                                icon = Icons.Filled.Security,
                                modifier = Modifier.weight(1f),
                                testTag = "grant_notification_permission_button"
                            )
                            LiquidGlassButton(
                                text = "Refresh",
                                onClick = { viewModel.refreshNotificationListenerPermission() },
                                icon = Icons.Filled.Refresh,
                                isPrimary = false,
                                modifier = Modifier.weight(0.6f),
                                testTag = "refresh_permission_button"
                            )
                        }
                    }
                }
            }
        }

        // 3. REAL HARDWARE LED VISUALIZER & QUICK TEST CARD
        item {
            LiquidGlassCard(
                modifier = Modifier.testTag("led_hardware_visualizer_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REAL HARDWARE RGB OUTPUT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    (if (activeNotification != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary).copy(alpha = 0.2f)
                                )
                                .border(
                                    1.dp,
                                    (if (activeNotification != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary).copy(alpha = 0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (activeNotification != null) "LED LIVE" else "READY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (activeNotification != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Physical LED Simulated Orb (Mirrors exact Sysfs / Root writes)
                    val displayLedColor = remember(activeLedOutput) {
                        Color(activeLedOutput.r, activeLedOutput.g, activeLedOutput.b)
                    }
                    val isLightActive = activeLedOutput.r > 10 || activeLedOutput.g > 10 || activeLedOutput.b > 10

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        displayLedColor.copy(alpha = if (isLightActive) 0.95f else 0.15f),
                                        displayLedColor.copy(alpha = if (isLightActive) 0.50f else 0.05f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = if (isLightActive) displayLedColor else Color.Gray.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(displayLedColor)
                                .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        )
                    }

                    // Active Output Info
                    Text(
                        text = if (activeNotification != null) {
                            "${activeNotification?.appName}: ${activeNotification?.effectType?.name} (RGB: ${activeLedOutput.r}, ${activeLedOutput.g}, ${activeLedOutput.b})"
                        } else {
                            "Hardware Idle • Waiting for notification or manual test"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Quick Test Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassButton(
                            text = if (isTestingActive) "STOP TEST" else "TEST INSTAGRAM LED",
                            onClick = {
                                if (isTestingActive) {
                                    viewModel.stopLightFlowTest()
                                    isTestingActive = false
                                } else {
                                    val insta = profiles.find { it.packageName.contains("instagram") } ?: defaultProfile
                                    viewModel.testLightFlowProfile(insta)
                                    isTestingActive = true
                                }
                            },
                            icon = if (isTestingActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            modifier = Modifier.weight(1f),
                            testTag = "quick_test_led_button"
                        )
                        LiquidGlassButton(
                            text = "YOUTUBE",
                            onClick = {
                                val yt = profiles.find { it.packageName.contains("youtube") } ?: defaultProfile
                                viewModel.testLightFlowProfile(yt)
                                isTestingActive = true
                            },
                            icon = Icons.Filled.Bolt,
                            isPrimary = false,
                            modifier = Modifier.weight(0.7f),
                            testTag = "test_youtube_button"
                        )
                    }
                }
            }
        }

        // 4. APPLICATION PROFILES HEADER & SEARCH
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Application Profiles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${profiles.size} profiles configured",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LiquidGlassButton(
                        text = "Add App",
                        onClick = { showAppPicker = true },
                        icon = Icons.Filled.Add,
                        modifier = Modifier.width(120.dp),
                        testTag = "add_app_profile_button"
                    )
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search application or package...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_profiles_input"),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
            }
        }

        // 5. LIST OF PER-APP PROFILES
        items(filteredProfiles, key = { it.packageName }) { profile ->
            AppProfileItemCard(
                profile = profile,
                onToggle = { enabled ->
                    viewModel.saveLightFlowProfile(profile.copy(isEnabled = enabled))
                },
                onEdit = {
                    editingProfile = profile
                },
                onQuickTest = {
                    viewModel.testLightFlowProfile(profile)
                    isTestingActive = true
                }
            )
        }

        // 6. AUTOMATION CONDITIONS CARD
        item {
            LiquidGlassCard(
                modifier = Modifier.testTag("automation_rules_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Automation Conditions",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    AutomationToggleRow(
                        title = "Screen ON Enabled",
                        desc = "Trigger RGB light even when screen is interactive",
                        checked = automation.screenOnEnabled,
                        onCheckedChange = { viewModel.updateLightFlowAutomation(automation.copy(screenOnEnabled = it)) }
                    )
                    AutomationToggleRow(
                        title = "Screen OFF Enabled",
                        desc = "Illuminate RGB light when device is locked/sleeping",
                        checked = automation.screenOffEnabled,
                        onCheckedChange = { viewModel.updateLightFlowAutomation(automation.copy(screenOffEnabled = it)) }
                    )
                    AutomationToggleRow(
                        title = "Charging Only",
                        desc = "Activate only while connected to power",
                        checked = automation.chargingOnly,
                        onCheckedChange = { viewModel.updateLightFlowAutomation(automation.copy(chargingOnly = it)) }
                    )
                    AutomationToggleRow(
                        title = "Respect Do Not Disturb (DND)",
                        desc = "Suppress LED output when system DND is engaged",
                        checked = automation.respectDnd,
                        onCheckedChange = { viewModel.updateLightFlowAutomation(automation.copy(respectDnd = it)) }
                    )
                }
            }
        }

        // 7. NOTIFICATION COLOR HISTORY CARD
        item {
            LiquidGlassCard(
                modifier = Modifier.testTag("notification_history_card")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Notification Color History",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (history.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearLightFlowHistory() }) {
                                Text("Clear", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    if (history.isEmpty()) {
                        Text(
                            text = "No notification history captured yet. As incoming notifications arrive, detected colors and timings will appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            history.take(6).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(Color(item.detectedColor.r, item.detectedColor.g, item.detectedColor.b))
                                                .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                        )
                                        Column {
                                            Text(
                                                text = item.appName,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${item.title} • ${item.effect.name}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Text(
                                        text = dateFormat.format(Date(item.timestamp)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. RESET DEFAULTS CARD
        item {
            LiquidGlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reset All Profiles",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Restore built-in palettes for Instagram, YouTube, WhatsApp, and Telegram.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LiquidGlassButton(
                        text = "Reset",
                        onClick = { viewModel.resetLightFlowDefaults() },
                        isDestructive = true,
                        isPrimary = false,
                        modifier = Modifier.width(90.dp),
                        testTag = "reset_light_flow_defaults_button"
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // PROFILE EDITOR DIALOG
    editingProfile?.let { prof ->
        LightFlowProfileEditorDialog(
            profile = prof,
            onDismiss = { editingProfile = null },
            onSave = { updated ->
                viewModel.saveLightFlowProfile(updated)
                editingProfile = null
            },
            onTest = { testProf ->
                viewModel.testLightFlowProfile(testProf)
                isTestingActive = true
            },
            onDelete = { pkg ->
                viewModel.deleteLightFlowProfile(pkg)
                editingProfile = null
            },
            onTactileColor = { viewModel.triggerVibrationForColorProfile() }
        )
    }

    // ADD APPLICATION PICKER DIALOG
    if (showAppPicker) {
        AppSelectionDialog(
            installedApps = viewModel.getInstalledApps(),
            onDismiss = { showAppPicker = false },
            onSelectApp = { appItem ->
                val detected = viewModel.extractColorsForApp(appItem.packageName)
                val newProfile = LightFlowProfile(
                    packageName = appItem.packageName,
                    appName = appItem.appName,
                    isEnabled = true,
                    colorMode = LightFlowColorMode.AUTO_APP_COLOR,
                    colors = detected.ifEmpty { listOf(RgbColor.Blue) },
                    detectedColors = detected,
                    effect = EffectType.BREATHING,
                    brightness = 0.85f,
                    speed = 1.0f,
                    durationSeconds = 5,
                    repeatCount = 3,
                    priority = NotificationPriority.NORMAL
                )
                viewModel.saveLightFlowProfile(newProfile)
                editingProfile = newProfile
                showAppPicker = false
            }
        )
    }
}

/**
 * Individual App Profile Card in the List
 */
@Composable
private fun AppProfileItemCard(
    profile: LightFlowProfile,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onQuickTest: () -> Unit
) {
    LiquidGlassCard(
        onClick = onEdit,
        modifier = Modifier.testTag("profile_item_${profile.packageName}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Color Circle Badge Cluster
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val colors = profile.colors.ifEmpty { listOf(profile.primaryColor) }
                    if (colors.size == 1) {
                        val c = colors[0]
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(c.r, c.g, c.b))
                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                        )
                    } else {
                        // Multi-color sequence preview mini-arc
                        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                            colors.take(3).forEach { c ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(c.r, c.g, c.b))
                                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = profile.appName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = profile.effect.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(profile.brightness * 100).toInt()}% LED",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (profile.flashMode != LightFlowFlashMode.OFF) {
                            Text(
                                text = "• Flash",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onQuickTest,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Test",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                LiquidGlassSwitch(
                    checked = profile.isEnabled,
                    onCheckedChange = onToggle,
                    testTag = "toggle_${profile.packageName}"
                )
            }
        }
    }
}

/**
 * Compact Automation row with switch
 */
@Composable
private fun AutomationToggleRow(
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LiquidGlassSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * Full Detailed Profile Editor Dialog
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LightFlowProfileEditorDialog(
    profile: LightFlowProfile,
    onDismiss: () -> Unit,
    onSave: (LightFlowProfile) -> Unit,
    onTest: (LightFlowProfile) -> Unit,
    onDelete: (String) -> Unit,
    onTactileColor: () -> Unit = {}
) {
    var isEnabled by remember { mutableStateOf(profile.isEnabled) }
    var colorMode by remember { mutableStateOf(profile.colorMode) }
    var colors by remember { mutableStateOf(profile.colors) }
    var effect by remember { mutableStateOf(profile.effect) }
    var brightness by remember { mutableStateOf(profile.brightness) }
    var speed by remember { mutableStateOf(profile.speed) }
    var durationSeconds by remember { mutableStateOf(profile.durationSeconds) }
    var repeatCount by remember { mutableStateOf(profile.repeatCount) }
    var priority by remember { mutableStateOf(profile.priority) }
    var flashMode by remember { mutableStateOf(profile.flashMode) }
    var aodGlow by remember { mutableStateOf(profile.aodGlowEnabled) }
    var edgeGlow by remember { mutableStateOf(profile.aodEdgeGlowEnabled) }

    val currentDraft = remember(
        isEnabled, colorMode, colors, effect, brightness, speed,
        durationSeconds, repeatCount, priority, flashMode, aodGlow, edgeGlow
    ) {
        profile.copy(
            isEnabled = isEnabled,
            colorMode = colorMode,
            colors = colors,
            effect = effect,
            brightness = brightness,
            speed = speed,
            durationSeconds = durationSeconds,
            repeatCount = repeatCount,
            priority = priority,
            flashMode = flashMode,
            aodGlowEnabled = aodGlow,
            aodEdgeGlowEnabled = edgeGlow,
            isCustomized = true
        )
    }

    // Standard preset colors for quick addition
    val presetPalette = listOf(
        RgbColor(255, 0, 0),       // Red
        RgbColor(255, 128, 0),     // Orange
        RgbColor(255, 220, 0),     // Yellow
        RgbColor(0, 230, 80),      // Green
        RgbColor(0, 210, 255),     // Cyan
        RgbColor(11, 87, 208),     // Blue
        RgbColor(140, 0, 255),     // Purple
        RgbColor(225, 48, 108),    // Pink
        RgbColor(255, 255, 255)    // White
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("profile_editor_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = profile.appName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = profile.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }
                }

                // 1. Color Mode Selector
                item {
                    Text(
                        text = "COLOR MODE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LightFlowColorMode.entries.forEach { mode ->
                            val isSel = mode == colorMode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable {
                                        colorMode = mode
                                        onTactileColor()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = mode.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 2. Active Sequence Colors
                item {
                    Text(
                        text = "CONFIGURED COLORS (MULTI-COLOR / SEQUENCE)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        colors.forEachIndexed { idx, c ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(c.r, c.g, c.b))
                                    .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                                    .clickable {
                                        if (colors.size > 1) {
                                            colors = colors.filterIndexed { i, _ -> i != idx }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (colors.size > 1) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add color to sequence:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        presetPalette.forEach { pCol ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(pCol.r, pCol.g, pCol.b))
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                    .clickable {
                                        if (colors.size < 6) {
                                            colors = colors + pCol
                                            onTactileColor()
                                        }
                                    }
                            )
                        }
                    }

                    if (profile.detectedColors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { colors = profile.detectedColors }
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore Auto-Detected App Colors")
                        }
                    }
                }

                // 3. Effect Selector (All 16 effects supported)
                item {
                    Text(
                        text = "PHYSICAL LED EFFECT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val allEffects = listOf(
                        EffectType.STATIC,
                        EffectType.BREATHING,
                        EffectType.PULSE,
                        EffectType.HEARTBEAT,
                        EffectType.WAVE,
                        EffectType.RAINBOW,
                        EffectType.RAINBOW_CYCLE,
                        EffectType.STROBE,
                        EffectType.FLASH,
                        EffectType.DOUBLE_FLASH,
                        EffectType.TRIPLE_FLASH,
                        EffectType.FADE,
                        EffectType.COLOR_CYCLE,
                        EffectType.SMOOTH_GRADIENT,
                        EffectType.AURORA,
                        EffectType.SPARKLE
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allEffects.forEach { eff ->
                            val isSel = eff == effect
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { effect = eff }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = eff.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 4. Sliders: Brightness & Speed
                item {
                    LiquidGlassSliderItem(
                        title = "LED Brightness",
                        value = brightness,
                        onValueChange = { brightness = it },
                        valueRange = 0.05f..1.0f,
                        displayFormatter = { "${(it * 100).toInt()}%" }
                    )
                    LiquidGlassSliderItem(
                        title = "Effect Speed",
                        value = speed,
                        onValueChange = { speed = it },
                        valueRange = 0.2f..3.0f,
                        displayFormatter = { String.format(Locale.US, "%.1fx", it) }
                    )
                }

                // 5. Duration & Priority
                item {
                    Text(
                        text = "DURATION (SECONDS)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val durations = listOf(3, 5, 10, 15, 30, 60)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durations.forEach { sec ->
                            val isSel = durationSeconds == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { durationSeconds = sec }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${sec}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "PRIORITY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NotificationPriority.entries.forEach { prio ->
                            val isSel = priority == prio
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSel) {
                                            when (prio) {
                                                NotificationPriority.CRITICAL -> MaterialTheme.colorScheme.error
                                                NotificationPriority.HIGH -> MaterialTheme.colorScheme.tertiary
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { priority = prio }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prio.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 6. Camera Flash Mode
                item {
                    Text(
                        text = "CAMERA FLASH INTEGRATION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LightFlowFlashMode.entries.forEach { fMode ->
                            val isSel = flashMode == fMode
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isSel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                    .clickable { flashMode = fMode }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = fMode.name.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 7. AOD & Edge Glow Toggles
                item {
                    Text(
                        text = "AOD & EDGE GLOW",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AOD Notification Banner Glow", style = MaterialTheme.typography.bodyMedium)
                        LiquidGlassSwitch(checked = aodGlow, onCheckedChange = { aodGlow = it })
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Screen Edge Glow Sync", style = MaterialTheme.typography.bodyMedium)
                        LiquidGlassSwitch(checked = edgeGlow, onCheckedChange = { edgeGlow = it })
                    }
                }

                // Action Buttons
                item {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidGlassButton(
                            text = "TEST ON LED",
                            onClick = { onTest(currentDraft) },
                            icon = Icons.Filled.Bolt,
                            modifier = Modifier.weight(1f),
                            testTag = "test_profile_in_dialog_button"
                        )
                        LiquidGlassButton(
                            text = "SAVE",
                            onClick = { onSave(currentDraft) },
                            icon = Icons.Filled.Check,
                            modifier = Modifier.weight(1f),
                            testTag = "save_profile_button"
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(
                        onClick = { onDelete(profile.packageName) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Remove This Profile", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

/**
 * Installed App Picker Dialog
 */
@Composable
private fun AppSelectionDialog(
    installedApps: List<InstalledAppItem>,
    onDismiss: () -> Unit,
    onSelectApp: (InstalledAppItem) -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(installedApps, search) {
        if (search.isBlank()) installedApps
        else installedApps.filter {
            it.appName.contains(search, ignoreCase = true) ||
                    it.packageName.contains(search, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Application",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("Filter apps...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.packageName }) { appItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectApp(appItem) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = appItem.appName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = appItem.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (appItem.hasProfile) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "CONFIGURED",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
