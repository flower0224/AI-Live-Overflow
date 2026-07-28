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
import android.view.MotionEvent
import android.view.WindowManager

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var petView: PetView
    private lateinit var supabaseSync: SupabaseSync
    private lateinit var appWatcher: AppWatcher
    private lateinit var layoutParams: WindowManager.LayoutParams

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification())

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        supabaseSync = SupabaseSync()
        createOverlayView()

        supabaseSync.onStateChanged = { mood ->
            petView.mood = mood
        }

        appWatcher = AppWatcher(this) { pkg ->
            supabaseSync.reportEvent("app_foreground", pkg)
        }
        appWatcher.start()
    }

    private fun createOverlayView() {
        petView = PetView(this)

        layoutParams = WindowManager.LayoutParams(
            150,
            200,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 60
            y = 120
        }

        windowManager.addView(petView, layoutParams)

        petView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    layoutParams.flags =
                        layoutParams.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                    windowManager.updateViewLayout(petView, layoutParams)
                    petView.lastTouchX = event.rawX
                    petView.lastTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - petView.lastTouchX
                    val dy = event.rawY - petView.lastTouchY
                    petView.lastTouchX = event.rawX
                    petView.lastTouchY = event.rawY
                    layoutParams.x += dx.toInt()
                    layoutParams.y += dy.toInt()
                    windowManager.updateViewLayout(petView, layoutParams)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    layoutParams.flags =
                        layoutParams.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    windowManager.updateViewLayout(petView, layoutParams)
                    true
                }
                else -> false
            }
        }
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
        appWatcher.stop()
        supabaseSync.stopPolling()
        windowManager.removeView(petView)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
