package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuiltInColorProfiles
import com.example.model.ColorProfile
import com.example.ui.components.PixelStatusDot
import com.example.localization.Strings
import com.example.model.AppLanguage
import com.example.model.LedState
import com.example.model.RgbColor
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.LiquidGlassSegmentedMenu
import com.example.ui.components.LiquidGlassSliderItem
import com.example.ui.components.LiquidGlassTheme
import com.example.ui.components.M3MotionDefaults
import com.example.ui.components.m3PageTransition
import com.example.ui.components.PixelButton
import com.example.ui.components.PixelCard
import com.example.ui.components.PixelSectionTitle
import com.example.ui.components.PixelSliderItem
import com.example.ui.components.PixelTonalButton
import kotlin.math.roundToInt

enum class ColorPickerSubMenu(val titleKey: String, val icon: ImageVector) {
    SWATCHES("preset_colors", Icons.Default.Palette),
    SLIDERS("rgb_values", Icons.Default.Tune)
}

@Composable
fun ColorPickerScreen(
    ledState: LedState,
    language: AppLanguage,
    onColorChanged: (RgbColor) -> Unit,
    onBrightnessChanged: (Float) -> Unit,
    onApply: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleLed: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val isDark = isSystemInDarkTheme()

    var redVal by remember(ledState.currentColor) { mutableIntStateOf(ledState.currentColor.r) }
    var greenVal by remember(ledState.currentColor) { mutableIntStateOf(ledState.currentColor.g) }
    var blueVal by remember(ledState.currentColor) { mutableIntStateOf(ledState.currentColor.b) }

    val currentColor = remember(redVal, greenVal, blueVal) {
        RgbColor(redVal, greenVal, blueVal)
    }

    val subMenus = remember { ColorPickerSubMenu.entries }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { subMenus.size }
    )
    val coroutineScope = rememberCoroutineScope()

    val pixelPalette = listOf(
        RgbColor(11, 87, 208),     // Pixel Blue
        RgbColor(140, 79, 255),    // Pixel Violet
        RgbColor(0, 168, 132),     // Pixel Mint
        RgbColor(52, 168, 83),     // Google Green
        RgbColor(251, 188, 4),     // Google Yellow
        RgbColor(234, 67, 53),     // Google Coral Red
        RgbColor(0, 163, 196),     // Pixel Cyan
        RgbColor(255, 110, 180),   // Hot Pink
        RgbColor(255, 153, 0),     // Deep Amber
        RgbColor(186, 26, 26),     // Crimson
        RgbColor(79, 195, 247),    // Light Sky
        RgbColor(255, 255, 255)    // Pure White
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Title & Tactile LED Power Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Strings.get("choose_led_color", language),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            // Tactile LED Power Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onToggleLed?.invoke() }
                    .background(
                        if (ledState.isRunning) LiquidGlassTheme.highlightPillBrush(isDark)
                        else LiquidGlassTheme.surfaceBrush(isDark)
                    )
                    .border(
                        1.dp,
                        if (ledState.isRunning) SolidColor(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        else LiquidGlassTheme.borderBrush(isDark),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PixelStatusDot(isActive = ledState.isRunning)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (ledState.isRunning) Strings.get("running", language) else Strings.get("stopped", language),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (ledState.isRunning) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Center: Large Circular Color Preview (Section 62 - Tap to toggle LED)
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(140.dp)
                .clickable { onToggleLed?.invoke() },
            contentAlignment = Alignment.Center
        ) {
            // Ambient diffused halo
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                currentColor.toComposeColor().copy(alpha = 0.5f),
                                currentColor.toComposeColor().copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner solid preview disc
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(currentColor.toComposeColor())
                    .border(
                        3.dp,
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        CircleShape
                    )
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .testTag("circular_color_preview")
            )
        }

        // Hex & Luminance info chip with Liquid Glass
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(LiquidGlassTheme.surfaceBrush(isDark))
                .border(1.dp, LiquidGlassTheme.borderBrush(isDark), RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentColor.toHex(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "RGB(${currentColor.r}, ${currentColor.g}, ${currentColor.b})",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Curated Tactile Color Profiles Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.get("color_profiles", language),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "${BuiltInColorProfiles.size} presets",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (profile in BuiltInColorProfiles) {
                    val isSel = currentColor == profile.color
                    val compColor = profile.color.toComposeColor()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                redVal = profile.color.r
                                greenVal = profile.color.g
                                blueVal = profile.color.b
                                onColorChanged(profile.color)
                            }
                            .background(LiquidGlassTheme.surfaceBrush(isDark))
                            .background(
                                if (isSel) compColor.copy(alpha = 0.22f)
                                else Color.Transparent
                            )
                            .border(
                                width = if (isSel) 2.dp else 1.dp,
                                brush = if (isSel) SolidColor(compColor) else LiquidGlassTheme.borderBrush(isDark),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(compColor)
                                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (profile.color.isLight()) Color.Black else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = profile.name,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = profile.subtitle.ifEmpty { "Profile" },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

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
            testTag = "color_picker_segmented_menu"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Swipeable Pager for Color Picker Sub-menus with M3 Motion
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            pageSpacing = 12.dp,
            beyondViewportPageCount = 1
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .m3PageTransition(pagerState, page, scaleDown = 0.05f, alphaFade = 0.40f, tiltAngle = 3f)
            ) {
                when (subMenus[page]) {
                ColorPickerSubMenu.SWATCHES -> {
                    // Preset Swatches Card (Liquid Glass)
                    LiquidGlassCard(modifier = Modifier.fillMaxSize()) {
                        Column {
                            Text(
                                text = Strings.get("preset_colors", language),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (preset in pixelPalette.take(6)) {
                                    val isSel = currentColor == preset
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(preset.toComposeColor())
                                            .border(
                                                if (isSel) 3.dp else 1.dp,
                                                if (isSel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                CircleShape
                                            )
                                            .clickable {
                                                redVal = preset.r
                                                greenVal = preset.g
                                                blueVal = preset.b
                                                onColorChanged(preset)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSel) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (preset.isLight()) Color.Black else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (preset in pixelPalette.drop(6).take(6)) {
                                    val isSel = currentColor == preset
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(preset.toComposeColor())
                                            .border(
                                                if (isSel) 3.dp else 1.dp,
                                                if (isSel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                                CircleShape
                                            )
                                            .clickable {
                                                redVal = preset.r
                                                greenVal = preset.g
                                                blueVal = preset.b
                                                onColorChanged(preset)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSel) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (preset.isLight()) Color.Black else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ColorPickerSubMenu.SLIDERS -> {
                    // RGB Values Sliders (Liquid Glass Card & Sliders)
                    LiquidGlassCard(modifier = Modifier.fillMaxSize()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // R Slider
                            LiquidGlassSliderItem(
                                title = "R ($redVal)",
                                value = redVal / 255f,
                                onValueChange = {
                                    redVal = (it * 255).roundToInt()
                                    onColorChanged(RgbColor(redVal, greenVal, blueVal))
                                },
                                displayFormatter = { "$redVal" },
                                testTag = "slider_r"
                            )

                            // G Slider
                            LiquidGlassSliderItem(
                                title = "G ($greenVal)",
                                value = greenVal / 255f,
                                onValueChange = {
                                    greenVal = (it * 255).roundToInt()
                                    onColorChanged(RgbColor(redVal, greenVal, blueVal))
                                },
                                displayFormatter = { "$greenVal" },
                                testTag = "slider_g"
                            )

                            // B Slider
                            LiquidGlassSliderItem(
                                title = "B ($blueVal)",
                                value = blueVal / 255f,
                                onValueChange = {
                                    blueVal = (it * 255).roundToInt()
                                    onColorChanged(RgbColor(redVal, greenVal, blueVal))
                                },
                                displayFormatter = { "$blueVal" },
                                testTag = "slider_b"
                            )
                        }
                    }
                }
            }
        }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Brightness Slider (Liquid Glass Card)
        LiquidGlassCard {
            LiquidGlassSliderItem(
                title = Strings.get("brightness", language),
                value = ledState.brightness,
                onValueChange = onBrightnessChanged,
                valueRange = 0.05f..1.0f,
                icon = Icons.Default.Lightbulb,
                displayFormatter = { "${(it * 100).roundToInt()}%" },
                testTag = "color_picker_brightness_slider"
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Buttons: APPLY & SAVE (Liquid Glass Buttons)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LiquidGlassButton(
                text = Strings.get("apply", language),
                onClick = onApply,
                modifier = Modifier.weight(1f),
                testTag = "color_picker_apply_button"
            )

            LiquidGlassButton(
                text = Strings.get("save", language),
                onClick = onSave,
                isPrimary = false,
                modifier = Modifier.weight(1f),
                testTag = "color_picker_save_button"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
