package com.flower0224.ailiveoverflow

import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SupabaseSync {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val url = "https://hpijefltncgrczzwxtez.supabase.co"
    private val anonKey = "sb_publishable_wncOZIf8gANJZJQagikb3w_XriyMc52"

    var onStateChanged: ((mood: String) -> Unit)? = null

    fun startPolling() {
        scope.launch {
            while (isActive) {
                fetchPetState()
                delay(30_000L)
            }
        }
    }

    private suspend fun fetchPetState() {
        withContext(Dispatchers.IO) {
            try {
                val conn = URL("$url/rest/v1/pet_state?select=*&order=updated_at.desc&limit=1")
                    .openConnection() as HttpURLConnection

                conn.setRequestProperty("apikey", anonKey)
                conn.setRequestProperty("Authorization", "Bearer $anonKey")
                conn.connectTimeout = 10_000

                val response = conn.inputStream.bufferedReader().readText()

                if (response.isNotBlank() && response != "[]") {
                    val json = JSONObject(
                        response.trim().removePrefix("[").removeSuffix("]")
                    )
                    val mood = json.optString("state_value", "idle")
                    withContext(Dispatchers.Main) {
                        onStateChanged?.invoke(mood)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun stopPolling() {
        scope.cancel()
    }
}
