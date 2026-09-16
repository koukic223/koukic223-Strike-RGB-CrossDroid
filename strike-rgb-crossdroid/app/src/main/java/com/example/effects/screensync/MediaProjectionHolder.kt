package com.example.effects.screensync

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager

/**
 * Thread-safe singleton holder for MediaProjection permission result.
 * Allows passing capture credentials from UI Activity to Foreground Service.
 */
object MediaProjectionHolder {
    private var resultCode: Int = 0
    private var resultData: Intent? = null

    val hasPermission: Boolean
        get() = resultCode != 0 && resultData != null

    @Synchronized
    fun setPermission(code: Int, data: Intent?) {
        resultCode = code
        resultData = data
    }

    @Synchronized
    fun getMediaProjection(context: Context): MediaProjection? {
        val data = resultData ?: return null
        if (resultCode == 0) return null
        return try {
            val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            projectionManager?.getMediaProjection(resultCode, data.clone() as Intent)
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    fun clear() {
        resultCode = 0
        resultData = null
    }
}
