package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DeviceCapabilities

/**
 * Device Capability Matrix Component:
 * Accurately displays hardware availability without faking unsupported features:
 * - RGB LED
 * - Rear Camera Flash
 * - Front Hardware Flash
 * - Screen Flash
 * - AOD
 */
@Composable
fun DeviceCapabilityMatrixCard(
    capabilities: DeviceCapabilities,
    modifier: Modifier = Modifier
) {
    LiquidGlassCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Device Hardware Capabilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            CapabilityRow(
                title = "RGB LED",
                isAvailable = capabilities.hasRgbLed,
                description = if (capabilities.hasRgbLed) "Sysfs hardware node detected" else "Sysfs RGB not accessible"
            )

            CapabilityRow(
                title = "Rear Camera Flash",
                isAvailable = capabilities.hasRearCameraFlash,
                description = if (capabilities.hasRearCameraFlash) {
                    if (capabilities.isCameraPermissionGranted) "Available and ready" else "Permission required"
                } else "No camera flash hardware"
            )

            CapabilityRow(
                title = "Front Hardware Flash",
                isAvailable = capabilities.hasFrontHardwareFlash,
                description = if (capabilities.hasFrontHardwareFlash) "Hardware front LED present" else "Not available (auto Screen Flash active)"
            )

            CapabilityRow(
                title = "Screen Flash",
                isAvailable = capabilities.hasScreenFlash,
                description = "Universal front illumination active"
            )

            CapabilityRow(
                title = "Always-On Display (AOD)",
                isAvailable = capabilities.hasAodSupport,
                description = if (capabilities.hasAodSupport) "Supported display panel" else "Not supported on this device display"
            )

            if (!capabilities.hasAodSupport && capabilities.aodReason != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "AOD unavailable on this device — RGB and Screen Flash will be used.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CapabilityRow(
    title: String,
    isAvailable: Boolean,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isAvailable) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = if (isAvailable) "Available" else "Unavailable",
                tint = if (isAvailable) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isAvailable) "Available" else "Not available",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isAvailable) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
            )
        }
    }
}
