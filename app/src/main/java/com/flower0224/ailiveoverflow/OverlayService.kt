package com.flower0224.ailiveoverflow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var petView: ImageView
    private lateinit var supabaseSync: SupabaseSync

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification())

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        supabaseSync = SupabaseSync()
        createOverlayView()
    }

    private fun createOverlayView() {
        petView = ImageView(this).apply {
            setBackgroundColor(0x80FFCCCC.toInt())
        }

        val params = WindowManager.LayoutParams(
            200,
            200,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(petView, params)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "pet_overlay",
                "宠物悬浮窗",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "pet_overlay")
                .setContentTitle("AI Live Overflow")
                .setContentText("小宠物正在陪着你")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("AI Live Overflow")
                .setContentText("小宠物正在陪着你")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        supabaseSync.startPolling()
        return START_STICKY
    }

    override fun onDestroy() {
        supabaseSync.stopPolling()
        windowManager.removeView(petView)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
