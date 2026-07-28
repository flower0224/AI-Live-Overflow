package com.flower0224.ailiveoverflow

import android.app.AppOpsManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !Settings.canDrawOverlays(this)
        ) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "请开启「使用情况访问」权限，否则无法感知前台App", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            usageStatsLauncher.launch(intent)
        } else {
            startOverlayService()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (_: Exception) {
            false
        }
    }

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                Settings.canDrawOverlays(this)
            ) {
                if (!hasUsageStatsPermission()) {
                    Toast.makeText(this, "请开启「使用情况访问」权限", Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    usageStatsLauncher.launch(intent)
                } else {
                    startOverlayService()
                }
            } else {
                Toast.makeText(this, "需要悬浮窗权限才能显示宠物哦", Toast.LENGTH_LONG).show()
            }
            finish()
        }

    private val usageStatsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (hasUsageStatsPermission()) {
                startOverlayService()
            } else {
                Toast.makeText(this, "未开启使用情况访问，宠物将无法感知前台App", Toast.LENGTH_LONG).show()
                startOverlayService()
            }
            finish()
        }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        startForegroundService(intent)
        finish()
    }
}
