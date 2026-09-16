package com.example.hardware

import com.example.model.HardwareStatus
import com.example.model.HardwareTestResult
import com.example.model.RgbColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class HardwareController {

    private val ledController = LedHardwareController()

    val controller: LedHardwareController get() = ledController

    companion object {
        @Volatile
        private var instance: HardwareController? = null

        fun getInstance(): HardwareController {
            return instance ?: synchronized(this) {
                instance ?: HardwareController().also { instance = it }
            }
        }
    }

    suspend fun checkHardwareStatusAsync(): HardwareStatus {
        return ledController.detectLEDs()
    }

    fun checkHardwareStatus(): HardwareStatus {
        val rootAvailable = RootManager.isRootAvailable()
        return HardwareStatus(
            isRootConnected = false,
            isLedDetected = true,
            ledPath = ledController.redPath ?: "/sys/class/leds/red/brightness",
            chipModel = "CrossDroid RGB Controller",
            uid = null,
            apiLevel = android.os.Build.VERSION.SDK_INT,
            minAndroidVersion = "Android 2.1 (API 7)"
        )
    }

    suspend fun applyRgbColor(color: RgbColor, brightness: Float, isRunning: Boolean): Boolean {
        val dispatcher = if (!isRunning) NonCancellable + Dispatchers.IO else Dispatchers.IO
        return withContext(dispatcher) {
            if (!isRunning) {
                ledController.turnOff()
                return@withContext true
            }

            val scale = brightness.coerceIn(0f, 1f)
            val r = (color.r * scale).toInt()
            val g = (color.g * scale).toInt()
            val b = (color.b * scale).toInt()

            ledController.setRGB(r, g, b)
        }
    }

    suspend fun runHardwareTest(
        onProgress: (step: String, activeColor: RgbColor) -> Unit
    ): HardwareTestResult {
        return ledController.testRGB(onProgress)
    }

    suspend fun runPipelineColorTest(
        onProgress: (step: String, activeColor: RgbColor) -> Unit
    ): HardwareTestResult {
        return ledController.runPipelineColorTest(onProgress)
    }
}

