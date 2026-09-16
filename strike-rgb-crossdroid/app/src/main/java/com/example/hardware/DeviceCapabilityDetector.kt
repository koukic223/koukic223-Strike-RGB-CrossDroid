package com.example.hardware

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.example.model.DeviceCapabilities

/**
 * Robust, non-crashing detector for device hardware capabilities:
 * - RGB sysfs LEDs
 * - Rear camera flash
 * - Front camera hardware flash
 * - Screen flash (always viable)
 * - Always-on Display / Ambient display detection (distinguishing native OLED/AOD from unsupported LCDs)
 */
class DeviceCapabilityDetector(private val context: Context) {

    companion object {
        private const val TAG = "DeviceCapabilityDetect"
    }

    fun detectCapabilities(isCameraPermissionGranted: Boolean, isRgbLedDetected: Boolean): DeviceCapabilities {
        var rearFlashAvailable = false
        var frontFlashAvailable = false
        var rearCameraId: String? = null
        var frontCameraId: String? = null

        val packageManager = context.packageManager
        val hasSystemFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            if (cameraManager != null) {
                val cameraIds = cameraManager.cameraIdList
                for (id in cameraIds) {
                    try {
                        val chars = cameraManager.getCameraCharacteristics(id)
                        val facing = chars.get(CameraCharacteristics.LENS_FACING)
                        val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                        if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                            if (hasFlash) {
                                rearFlashAvailable = true
                                rearCameraId = id
                            }
                        } else if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                            if (hasFlash) {
                                frontFlashAvailable = true
                                frontCameraId = id
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error querying camera $id characteristics: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error accessing CameraManager: ${e.message}")
            rearFlashAvailable = hasSystemFlash
        }

        // If package manager says no flash, double check
        if (!rearFlashAvailable && hasSystemFlash) {
            rearFlashAvailable = true
        }

        // AOD / Ambient Display Detection
        val (aodSupported, aodReason) = detectAodSupport()

        return DeviceCapabilities(
            hasRgbLed = isRgbLedDetected,
            hasRearCameraFlash = rearFlashAvailable,
            hasFrontHardwareFlash = frontFlashAvailable,
            hasScreenFlash = true, // Universal fallback for any device (including BQ Aquaris A4.5)
            hasAodSupport = aodSupported,
            aodReason = aodReason,
            rearCameraId = rearCameraId,
            frontCameraId = frontCameraId,
            isCameraPermissionGranted = isCameraPermissionGranted
        )
    }

    /**
     * Checks whether device natively supports Always On Display or Ambient Display.
     * On devices like BQ Aquaris A4.5 (IPS LCD, Android 5/6/7 legacy or low-tier), AOD is not natively supported.
     */
    private fun detectAodSupport(): Pair<Boolean, String?> {
        val pm = context.packageManager
        // Check for ambient display system feature
        val hasAmbientFeature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            pm.hasSystemFeature("android.hardware.ambient_display") ||
                    pm.hasSystemFeature("com.google.android.feature.AOD") ||
                    pm.hasSystemFeature("android.hardware.biometrics.face")
        } else {
            pm.hasSystemFeature("android.hardware.ambient_display")
        }

        // Check system settings for doze / always on display
        var aodSettingExists = false
        try {
            val aodSetting = Settings.Secure.getInt(context.contentResolver, "doze_always_on", -1)
            if (aodSetting != -1) {
                aodSettingExists = true
            }
        } catch (_: Exception) {}

        try {
            val dozeEnabled = Settings.Secure.getInt(context.contentResolver, "doze_enabled", -1)
            if (dozeEnabled != -1) {
                aodSettingExists = true
            }
        } catch (_: Exception) {}

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isDozeSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isDeviceIdleMode != null
        } else false

        val isSupported = hasAmbientFeature || aodSettingExists

        val reason = if (!isSupported) {
            "AOD / Ambient Display is unavailable on this device hardware (standard LCD display or no ambient driver detected). Standard screen flash and RGB will be used."
        } else null

        return Pair(isSupported, reason)
    }
}
