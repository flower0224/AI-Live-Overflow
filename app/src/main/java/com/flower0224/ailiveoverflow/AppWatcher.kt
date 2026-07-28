package com.flower0224.ailiveoverflow

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.*

class AppWatcher(
    private val context: Context,
    private val onAppChanged: (packageName: String) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastPkg = ""

    fun start() {
        scope.launch {
            while (isActive) {
                val pkg = getForegroundPackage()
                if (pkg != null && pkg != lastPkg) {
                    lastPkg = pkg
                    withContext(Dispatchers.Main) {
                        onAppChanged(pkg)
                    }
                }
                delay(3_000L)
            }
        }
    }

    private fun getForegroundPackage(): String? {
        return try {
            val m = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val now = System.currentTimeMillis()
            val events = m.queryEvents(now - 5_000, now)
            val event = UsageEvents.Event()
            var foregroundPkg: String? = null
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foregroundPkg = event.packageName
                }
            }
            foregroundPkg
        } catch (_: Exception) {
            null
        }
    }

    fun stop() {
        scope.cancel()
    }
}
