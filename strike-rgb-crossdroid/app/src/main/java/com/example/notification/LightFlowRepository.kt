package com.example.notification

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.model.AodNotificationStyle
import com.example.model.ColorSequenceBehavior
import com.example.model.EffectType
import com.example.model.LightFlowAutomationConfig
import com.example.model.LightFlowColorMode
import com.example.model.LightFlowFlashMode
import com.example.model.LightFlowProfile
import com.example.model.NotificationHistoryItem
import com.example.model.NotificationPriority
import com.example.model.RgbColor
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Persistence Repository for Light Flow Notification System.
 * Manages per-app profiles, default fallback profiles, automation rules, and history.
 */
class LightFlowRepository(private val context: Context) {

    companion object {
        private const val TAG = "LightFlowRepository"
        private const val PREFS_NAME = "light_flow_preferences"
        private const val KEY_MASTER_ENABLED = "master_enabled"
        private const val KEY_PROFILES_JSON = "profiles_json"
        private const val KEY_DEFAULT_PROFILE = "default_profile_json"
        private const val KEY_AUTOMATION = "automation_json"
        private const val KEY_HISTORY = "history_json"

        @Volatile
        private var instance: LightFlowRepository? = null

        fun getInstance(context: Context): LightFlowRepository {
            return instance ?: synchronized(this) {
                instance ?: LightFlowRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val profilesCache = ConcurrentHashMap<String, LightFlowProfile>()

    init {
        loadProfilesFromDisk()
        if (profilesCache.isEmpty()) {
            setupInitialDefaultProfiles()
            persistProfilesToDisk()
        }
    }

    fun isMasterEnabled(): Boolean {
        return prefs.getBoolean(KEY_MASTER_ENABLED, true)
    }

    fun setMasterEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MASTER_ENABLED, enabled).apply()
    }

    fun getAllProfiles(): Map<String, LightFlowProfile> {
        return profilesCache.toMap()
    }

    fun getProfile(packageName: String): LightFlowProfile? {
        return profilesCache[packageName]
    }

    fun saveProfile(profile: LightFlowProfile) {
        profilesCache[profile.packageName] = profile
        persistProfilesToDisk()
    }

    fun deleteProfile(packageName: String) {
        profilesCache.remove(packageName)
        persistProfilesToDisk()
    }

    fun getDefaultProfile(): LightFlowProfile {
        val json = prefs.getString(KEY_DEFAULT_PROFILE, null)
        if (json != null) {
            try {
                return parseProfile(JSONObject(json))
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing default profile: ${e.message}")
            }
        }
        return LightFlowProfile(
            packageName = "default.notification",
            appName = "Default Notifications",
            isEnabled = true,
            colorMode = LightFlowColorMode.AUTO_APP_COLOR,
            colors = listOf(RgbColor(11, 87, 208), RgbColor(255, 255, 255)),
            effect = EffectType.BREATHING,
            brightness = 0.85f,
            speed = 1.0f,
            durationSeconds = 5,
            repeatCount = 3,
            delayMs = 200L,
            priority = NotificationPriority.NORMAL
        )
    }

    fun saveDefaultProfile(profile: LightFlowProfile) {
        try {
            val json = serializeProfile(profile).toString()
            prefs.edit().putString(KEY_DEFAULT_PROFILE, json).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save default profile: ${e.message}")
        }
    }

    fun getAutomationConfig(): LightFlowAutomationConfig {
        val json = prefs.getString(KEY_AUTOMATION, null)
        if (json != null) {
            try {
                val obj = JSONObject(json)
                return LightFlowAutomationConfig(
                    screenOnEnabled = obj.optBoolean("screenOnEnabled", true),
                    screenOffEnabled = obj.optBoolean("screenOffEnabled", true),
                    chargingOnly = obj.optBoolean("chargingOnly", false),
                    respectDnd = obj.optBoolean("respectDnd", true),
                    headphonesOnly = obj.optBoolean("headphonesOnly", false),
                    aodActiveOnly = obj.optBoolean("aodActiveOnly", false)
                )
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing automation config: ${e.message}")
            }
        }
        return LightFlowAutomationConfig()
    }

    fun saveAutomationConfig(config: LightFlowAutomationConfig) {
        try {
            val obj = JSONObject().apply {
                put("screenOnEnabled", config.screenOnEnabled)
                put("screenOffEnabled", config.screenOffEnabled)
                put("chargingOnly", config.chargingOnly)
                put("respectDnd", config.respectDnd)
                put("headphonesOnly", config.headphonesOnly)
                put("aodActiveOnly", config.aodActiveOnly)
            }
            prefs.edit().putString(KEY_AUTOMATION, obj.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save automation config: ${e.message}")
        }
    }

    fun getHistory(): List<NotificationHistoryItem> {
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        val list = mutableListOf<NotificationHistoryItem>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val cObj = obj.optJSONObject("color")
                val color = if (cObj != null) {
                    RgbColor(cObj.optInt("r"), cObj.optInt("g"), cObj.optInt("b"))
                } else RgbColor.Blue
                val effect = try {
                    EffectType.valueOf(obj.optString("effect", "STATIC"))
                } catch (_: Exception) {
                    EffectType.STATIC
                }
                list.add(
                    NotificationHistoryItem(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        packageName = obj.optString("packageName"),
                        appName = obj.optString("appName"),
                        title = obj.optString("title"),
                        message = obj.optString("message"),
                        detectedColor = color,
                        effect = effect,
                        timestamp = obj.optLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error parsing history: ${e.message}")
        }
        return list
    }

    fun addHistoryItem(item: NotificationHistoryItem) {
        val current = getHistory().toMutableList()
        current.add(0, item)
        // Keep last 30 items
        val trimmed = if (current.size > 30) current.subList(0, 30) else current

        try {
            val arr = JSONArray()
            for (hist in trimmed) {
                val obj = JSONObject().apply {
                    put("id", hist.id)
                    put("packageName", hist.packageName)
                    put("appName", hist.appName)
                    put("title", hist.title)
                    put("message", hist.message)
                    put("timestamp", hist.timestamp)
                    put("effect", hist.effect.name)
                    put("color", JSONObject().apply {
                        put("r", hist.detectedColor.r)
                        put("g", hist.detectedColor.g)
                        put("b", hist.detectedColor.b)
                    })
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save history: ${e.message}")
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    fun resetToDefaults() {
        profilesCache.clear()
        setupInitialDefaultProfiles()
        persistProfilesToDisk()
        saveDefaultProfile(
            LightFlowProfile(
                packageName = "default.notification",
                appName = "Default Notifications",
                isEnabled = true,
                colorMode = LightFlowColorMode.AUTO_APP_COLOR,
                colors = listOf(RgbColor(11, 87, 208), RgbColor(255, 255, 255)),
                effect = EffectType.BREATHING,
                brightness = 0.85f,
                speed = 1.0f,
                durationSeconds = 5,
                repeatCount = 3,
                delayMs = 200L,
                priority = NotificationPriority.NORMAL
            )
        )
        saveAutomationConfig(LightFlowAutomationConfig())
    }

    private fun setupInitialDefaultProfiles() {
        // Instagram: Pink -> Orange -> Purple -> Red (Breathing / Sequence)
        profilesCache["com.instagram.android"] = LightFlowProfile(
            packageName = "com.instagram.android",
            appName = "Instagram",
            isEnabled = true,
            colorMode = LightFlowColorMode.MULTI_COLOR,
            colors = listOf(
                RgbColor(225, 48, 108), // Pink
                RgbColor(253, 29, 29),  // Orange-Red
                RgbColor(131, 58, 180), // Purple
                RgbColor(252, 175, 69)  // Warm Yellow
            ),
            detectedColors = listOf(
                RgbColor(225, 48, 108),
                RgbColor(253, 29, 29),
                RgbColor(131, 58, 180)
            ),
            sequenceBehavior = ColorSequenceBehavior.CYCLE,
            effect = EffectType.BREATHING,
            brightness = 0.90f,
            speed = 1.1f,
            durationSeconds = 6,
            repeatCount = 4,
            priority = NotificationPriority.HIGH,
            flashMode = LightFlowFlashMode.DOUBLE
        )

        // YouTube: Red & White
        profilesCache["com.google.android.youtube"] = LightFlowProfile(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            isEnabled = true,
            colorMode = LightFlowColorMode.MULTI_COLOR,
            colors = listOf(
                RgbColor(255, 0, 0),     // Red
                RgbColor(255, 255, 255)  // White
            ),
            detectedColors = listOf(RgbColor(255, 0, 0), RgbColor(255, 255, 255)),
            effect = EffectType.PULSE,
            brightness = 0.95f,
            speed = 1.2f,
            durationSeconds = 5,
            repeatCount = 3,
            priority = NotificationPriority.NORMAL,
            flashMode = LightFlowFlashMode.SINGLE
        )

        // Facebook: Blue & White
        profilesCache["com.facebook.katana"] = LightFlowProfile(
            packageName = "com.facebook.katana",
            appName = "Facebook",
            isEnabled = true,
            colorMode = LightFlowColorMode.MULTI_COLOR,
            colors = listOf(
                RgbColor(24, 119, 242),  // Blue
                RgbColor(255, 255, 255)  // White
            ),
            detectedColors = listOf(RgbColor(24, 119, 242)),
            effect = EffectType.BREATHING,
            brightness = 0.85f,
            speed = 1.0f,
            durationSeconds = 5,
            repeatCount = 3,
            priority = NotificationPriority.NORMAL,
            flashMode = LightFlowFlashMode.OFF
        )

        // WhatsApp: Emerald Green & Teal
        profilesCache["com.whatsapp"] = LightFlowProfile(
            packageName = "com.whatsapp",
            appName = "WhatsApp",
            isEnabled = true,
            colorMode = LightFlowColorMode.AUTO_APP_COLOR,
            colors = listOf(
                RgbColor(37, 211, 102), // Green
                RgbColor(18, 140, 126)  // Teal
            ),
            detectedColors = listOf(RgbColor(37, 211, 102)),
            effect = EffectType.HEARTBEAT,
            brightness = 0.95f,
            speed = 1.0f,
            durationSeconds = 7,
            repeatCount = 5,
            priority = NotificationPriority.HIGH,
            flashMode = LightFlowFlashMode.DOUBLE
        )

        // Telegram: Cyan & Blue
        profilesCache["org.telegram.messenger"] = LightFlowProfile(
            packageName = "org.telegram.messenger",
            appName = "Telegram",
            isEnabled = true,
            colorMode = LightFlowColorMode.AUTO_APP_COLOR,
            colors = listOf(
                RgbColor(0, 136, 204), // Cyan Blue
                RgbColor(36, 161, 222)
            ),
            detectedColors = listOf(RgbColor(0, 136, 204)),
            effect = EffectType.BREATHING,
            brightness = 0.85f,
            speed = 1.0f,
            durationSeconds = 5,
            repeatCount = 3,
            priority = NotificationPriority.NORMAL,
            flashMode = LightFlowFlashMode.OFF
        )

        // Gmail: Google Red, Yellow, Blue, Green (Smooth Gradient)
        profilesCache["com.google.android.gm"] = LightFlowProfile(
            packageName = "com.google.android.gm",
            appName = "Gmail",
            isEnabled = true,
            colorMode = LightFlowColorMode.COLOR_SEQUENCE,
            colors = listOf(
                RgbColor(234, 67, 53),   // Red
                RgbColor(251, 188, 5),   // Yellow
                RgbColor(52, 168, 83),   // Green
                RgbColor(66, 133, 244)   // Blue
            ),
            detectedColors = listOf(RgbColor(234, 67, 53), RgbColor(66, 133, 244)),
            effect = EffectType.SMOOTH_GRADIENT,
            brightness = 0.90f,
            speed = 1.0f,
            durationSeconds = 6,
            repeatCount = 3,
            priority = NotificationPriority.NORMAL,
            flashMode = LightFlowFlashMode.OFF
        )

        // Messages: Pixel Blue
        profilesCache["com.google.android.apps.messaging"] = LightFlowProfile(
            packageName = "com.google.android.apps.messaging",
            appName = "Messages",
            isEnabled = true,
            colorMode = LightFlowColorMode.AUTO_APP_COLOR,
            colors = listOf(RgbColor(11, 87, 208)),
            detectedColors = listOf(RgbColor(11, 87, 208)),
            effect = EffectType.PULSE,
            brightness = 0.90f,
            speed = 1.0f,
            durationSeconds = 5,
            repeatCount = 3,
            priority = NotificationPriority.HIGH,
            flashMode = LightFlowFlashMode.SINGLE
        )

        // Phone: Emergency/Call Green (Critical Priority)
        profilesCache["com.android.phone"] = LightFlowProfile(
            packageName = "com.android.phone",
            appName = "Phone",
            isEnabled = true,
            colorMode = LightFlowColorMode.SINGLE_COLOR,
            colors = listOf(RgbColor(0, 255, 100)),
            detectedColors = listOf(RgbColor(37, 211, 102)),
            effect = EffectType.STROBE,
            brightness = 1.0f,
            speed = 1.5f,
            durationSeconds = 15,
            repeatCount = 8,
            priority = NotificationPriority.CRITICAL,
            flashMode = LightFlowFlashMode.STROBE
        )
    }

    private fun loadProfilesFromDisk() {
        val json = prefs.getString(KEY_PROFILES_JSON, null) ?: return
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val profile = parseProfile(obj)
                profilesCache[profile.packageName] = profile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load profiles from disk: ${e.message}")
        }
    }

    private fun persistProfilesToDisk() {
        try {
            val arr = JSONArray()
            for ((_, profile) in profilesCache) {
                arr.put(serializeProfile(profile))
            }
            prefs.edit().putString(KEY_PROFILES_JSON, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist profiles: ${e.message}")
        }
    }

    private fun serializeProfile(p: LightFlowProfile): JSONObject {
        val obj = JSONObject()
        obj.put("packageName", p.packageName)
        obj.put("appName", p.appName)
        obj.put("isEnabled", p.isEnabled)
        obj.put("colorMode", p.colorMode.name)

        val colorsArr = JSONArray()
        for (c in p.colors) {
            colorsArr.put(JSONObject().apply {
                put("r", c.r)
                put("g", c.g)
                put("b", c.b)
            })
        }
        obj.put("colors", colorsArr)

        val detArr = JSONArray()
        for (c in p.detectedColors) {
            detArr.put(JSONObject().apply {
                put("r", c.r)
                put("g", c.g)
                put("b", c.b)
            })
        }
        obj.put("detectedColors", detArr)

        obj.put("sequenceBehavior", p.sequenceBehavior.name)
        obj.put("effect", p.effect.name)
        obj.put("brightness", p.brightness.toDouble())
        obj.put("speed", p.speed.toDouble())
        obj.put("durationSeconds", p.durationSeconds)
        obj.put("repeatCount", p.repeatCount)
        obj.put("delayMs", p.delayMs)
        obj.put("priority", p.priority.name)
        obj.put("flashMode", p.flashMode.name)
        obj.put("aodStyle", p.aodStyle.name)
        obj.put("aodGlowEnabled", p.aodGlowEnabled)
        obj.put("aodEdgeGlowEnabled", p.aodEdgeGlowEnabled)
        obj.put("screenOffEnabled", p.screenOffEnabled)
        obj.put("isCustomized", p.isCustomized)
        return obj
    }

    private fun parseProfile(obj: JSONObject): LightFlowProfile {
        val pkg = obj.optString("packageName", "unknown")
        val appName = obj.optString("appName", pkg)
        val isEnabled = obj.optBoolean("isEnabled", true)
        val colorMode = try {
            LightFlowColorMode.valueOf(obj.optString("colorMode", "AUTO_APP_COLOR"))
        } catch (_: Exception) {
            LightFlowColorMode.AUTO_APP_COLOR
        }

        val colors = mutableListOf<RgbColor>()
        val cArr = obj.optJSONArray("colors")
        if (cArr != null) {
            for (i in 0 until cArr.length()) {
                val cObj = cArr.getJSONObject(i)
                colors.add(RgbColor(cObj.optInt("r"), cObj.optInt("g"), cObj.optInt("b")))
            }
        }
        if (colors.isEmpty()) colors.add(RgbColor.Blue)

        val detectedColors = mutableListOf<RgbColor>()
        val dArr = obj.optJSONArray("detectedColors")
        if (dArr != null) {
            for (i in 0 until dArr.length()) {
                val cObj = dArr.getJSONObject(i)
                detectedColors.add(RgbColor(cObj.optInt("r"), cObj.optInt("g"), cObj.optInt("b")))
            }
        }

        val seq = try {
            ColorSequenceBehavior.valueOf(obj.optString("sequenceBehavior", "CYCLE"))
        } catch (_: Exception) {
            ColorSequenceBehavior.CYCLE
        }

        val effect = try {
            EffectType.valueOf(obj.optString("effect", "BREATHING"))
        } catch (_: Exception) {
            EffectType.BREATHING
        }

        val brightness = obj.optDouble("brightness", 0.85).toFloat()
        val speed = obj.optDouble("speed", 1.0).toFloat()
        val durationSeconds = obj.optInt("durationSeconds", 5)
        val repeatCount = obj.optInt("repeatCount", 3)
        val delayMs = obj.optLong("delayMs", 200L)

        val priority = try {
            NotificationPriority.valueOf(obj.optString("priority", "NORMAL"))
        } catch (_: Exception) {
            NotificationPriority.NORMAL
        }

        val flashMode = try {
            LightFlowFlashMode.valueOf(obj.optString("flashMode", "OFF"))
        } catch (_: Exception) {
            LightFlowFlashMode.OFF
        }

        val aodStyle = try {
            AodNotificationStyle.valueOf(obj.optString("aodStyle", "RGB_GLOW"))
        } catch (_: Exception) {
            AodNotificationStyle.RGB_GLOW
        }

        val aodGlowEnabled = obj.optBoolean("aodGlowEnabled", true)
        val aodEdgeGlowEnabled = obj.optBoolean("aodEdgeGlowEnabled", true)
        val screenOffEnabled = obj.optBoolean("screenOffEnabled", true)
        val isCustomized = obj.optBoolean("isCustomized", false)

        return LightFlowProfile(
            packageName = pkg,
            appName = appName,
            isEnabled = isEnabled,
            colorMode = colorMode,
            colors = colors,
            detectedColors = detectedColors,
            sequenceBehavior = seq,
            effect = effect,
            brightness = brightness,
            speed = speed,
            durationSeconds = durationSeconds,
            repeatCount = repeatCount,
            delayMs = delayMs,
            priority = priority,
            flashMode = flashMode,
            aodStyle = aodStyle,
            aodGlowEnabled = aodGlowEnabled,
            aodEdgeGlowEnabled = aodEdgeGlowEnabled,
            screenOffEnabled = screenOffEnabled,
            isCustomized = isCustomized
        )
    }
}
