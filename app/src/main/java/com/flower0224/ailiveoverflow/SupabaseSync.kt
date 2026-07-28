package com.flower0224.ailiveoverflow

import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.OutputStream

class SupabaseSync {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val url = "https://hpijefltncgrczzwxtez.supabase.co"
    private val anonKey = "sb_publishable_wncOZIf8gANJZJQagikb3w_XriyMc52"

    var onStateChanged: ((mood: String) -> Unit)? = null
    private var lastSeenEventId = 0L

    // 已知的AI/聊天类App包名
    private val aiApps = setOf(
        "com.alibaba.tongyi",
        "com.baidu.wenxin",
        "com.openai.chatgpt",
        "com.google.android.apps.bard",
        "com.doubao.app",
        "com.moonshot.kimichat",
        "com.stepai.step",
        "com.zhipu.glm",
        "com.anthropic.claude"
    )

    private val videoApps = setOf(
        "com.ss.android.ugc.aweme",
        "com.kuaishou.nebula",
        "com.bilibili.app.in"
    )

    fun startPolling() {
        scope.launch {
            while (isActive) {
                processLatestEvent()
                delay(5_000L)
            }
        }
    }

    private suspend fun processLatestEvent() {
        withContext(Dispatchers.IO) {
            try {
                val conn = URL("$url/rest/v1/events?select=*&order=id.desc&limit=1")
                    .openConnection() as HttpURLConnection
                conn.setRequestProperty("apikey", anonKey)
                conn.setRequestProperty("Authorization", "Bearer $anonKey")
                conn.connectTimeout = 8_000

                val response = conn.inputStream.bufferedReader().readText()

                if (response.isNotBlank() && response != "[]") {
                    val json = JSONObject(
                        response.trim().removePrefix("[").removeSuffix("]")
                    )
                    val eventId = json.optLong("id", 0)
                    if (eventId > lastSeenEventId) {
                        lastSeenEventId = eventId
                        val pkg = json.optString("package_name", "")
                        val mood = decideMood(pkg)
                        updateMood(mood)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun decideMood(pkg: String): String {
        return when {
            pkg in aiApps -> "jealous"
            pkg in videoApps -> "idle"
            pkg.isEmpty() -> "idle"
            else -> "idle"
        }
    }

    private suspend fun updateMood(mood: String) {
        try {
            val conn = URL("$url/rest/v1/pet_state?id=eq.1")
                .openConnection() as HttpURLConnection
            conn.requestMethod = "PATCH"
            conn.setRequestProperty("apikey", anonKey)
            conn.setRequestProperty("Authorization", "Bearer $anonKey")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Prefer", "return=minimal")
            conn.doOutput = true
            conn.connectTimeout = 8_000

            val body = """{"state_value":"$mood"}"""
            conn.outputStream.use { it.write(body.toByteArray()) }
            conn.responseCode

            withContext(Dispatchers.Main) {
                onStateChanged?.invoke(mood)
            }
        } catch (_: Exception) {
        }
    }

    fun reportEvent(eventType: String, packageName: String) {
        scope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val conn = URL("$url/rest/v1/events")
                        .openConnection() as HttpURLConnection
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("apikey", anonKey)
                    conn.setRequestProperty("Authorization", "Bearer $anonKey")
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.setRequestProperty("Prefer", "return=minimal")
                    conn.doOutput = true
                    conn.connectTimeout = 5_000

                    val body = """{"event_type":"$eventType","package_name":"$packageName"}"""
                    conn.outputStream.use { it.write(body.toByteArray()) }
                    conn.responseCode
                } catch (_: Exception) {
                }
            }
        }
    }

    fun stopPolling() {
        scope.cancel()
    }
}
