package com.example.hardware

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * High-reliability hardware flashlight controller with strict safety failsafes:
 * - Always turns OFF on effect completion, error, or service stop.
 * - Hardware availability callbacks prevent state drift.
 * - Never leaves flashlight stuck ON.
 * - Supports Rear Flash and Front Hardware Flash (if present).
 */
class CameraFlashController(private val context: Context) {

    companion object {
        private const val TAG = "CameraFlashController"
    }

    private val cameraManager: CameraManager? =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private var rearCameraId: String? = null
    private var frontCameraId: String? = null

    private val mutex = Mutex()
    private var isRearTorchOn = false
    private var isFrontTorchOn = false

    private val torchCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        object : CameraManager.TorchCallback() {
            override fun onTorchModeUnavailable(cameraId: String) {
                Log.w(TAG, "Torch mode unavailable on camera $cameraId")
                if (cameraId == rearCameraId) isRearTorchOn = false
                if (cameraId == frontCameraId) isFrontTorchOn = false
            }

            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                if (cameraId == rearCameraId) isRearTorchOn = enabled
                if (cameraId == frontCameraId) isFrontTorchOn = enabled
            }
        }
    } else null

    init {
        findCameraFlashIds()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && torchCallback != null) {
            try {
                cameraManager?.registerTorchCallback(torchCallback, null)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to register torch callback: ${e.message}")
            }
        }
    }

    fun updateCameraIds(rearId: String?, frontId: String?) {
        rearCameraId = rearId
        frontCameraId = frontId
    }

    private fun findCameraFlashIds() {
        try {
            val cm = cameraManager ?: return
            for (id in cm.cameraIdList) {
                val chars = cm.getCameraCharacteristics(id)
                val facing = chars.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                val flash = chars.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (flash) {
                    if (facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK && rearCameraId == null) {
                        rearCameraId = id
                    } else if (facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_FRONT && frontCameraId == null) {
                        frontCameraId = id
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error discovering camera IDs: ${e.message}")
        }
    }

    suspend fun setRearTorch(enabled: Boolean): Boolean = mutex.withLock {
        val id = rearCameraId ?: return false
        return applyTorch(id, enabled).also { if (it) isRearTorchOn = enabled }
    }

    suspend fun setFrontTorch(enabled: Boolean): Boolean = mutex.withLock {
        val id = frontCameraId ?: return false
        return applyTorch(id, enabled).also { if (it) isFrontTorchOn = enabled }
    }

    private fun applyTorch(cameraId: String, enabled: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager?.setTorchMode(cameraId, enabled)
                return true
            } catch (e: CameraAccessException) {
                Log.e(TAG, "CameraAccessException setting torch on $cameraId ($enabled): ${e.message}")
            } catch (e: Exception) {
                Log.e(TAG, "Exception setting torch on $cameraId: ${e.message}")
            }
        }
        return false
    }

    /**
     * Absolute failsafe shutdown: guarantees both camera torches are turned OFF immediately.
     */
    suspend fun turnOffAll() {
        withContext(NonCancellable) {
            try {
                mutex.withLock {
                    rearCameraId?.let { applyTorch(it, false) }
                    frontCameraId?.let { applyTorch(it, false) }
                }
            } catch (_: CancellationException) {
                // Expected when coroutine is cancelled
            } catch (e: Exception) {
                Log.e(TAG, "Error in turnOffAll failsafe: ${e.message}")
            } finally {
                // Guarantee torch state reset and direct turn off even if mutex failed
                try {
                    rearCameraId?.let { applyTorch(it, false) }
                    frontCameraId?.let { applyTorch(it, false) }
                } catch (_: Exception) {}
                isRearTorchOn = false
                isFrontTorchOn = false
            }
        }
    }

    fun release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && torchCallback != null) {
            try {
                cameraManager?.unregisterTorchCallback(torchCallback)
            } catch (_: Exception) {}
        }
    }
}
