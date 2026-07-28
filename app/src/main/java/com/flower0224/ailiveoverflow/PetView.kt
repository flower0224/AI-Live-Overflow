package com.flower0224.ailiveoverflow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class PetView(context: Context) : View(context) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    var mood = "idle"
        set(value) {
            field = value
            postInvalidate()
        }

    // 供 OverlayService 拖动使用
    var lastTouchX = 0f
    var lastTouchY = 0f

    // TV colors
    private val tvBodyColor = Color.rgb(24, 24, 24)
    private val tvFrameColor = Color.rgb(55, 55, 60)
    private val screenBgColor = Color.rgb(0, 47, 167)
    private val textColor = Color.rgb(230, 240, 255)

    private val moodEmojis = mapOf(
        "idle" to "ᗜ𖥦ᗜ",
        "happy" to "⋉(● ∸ ●)⋊",
        "jealous" to "^ ^",
        "surprised" to "O_o",
        "sad" to "╯﹏╰",
        "angry" to "（▼へ▼メ）"
    )

    private val indicatorColors = mapOf(
        "idle" to Color.rgb(255, 200, 100),
        "happy" to Color.rgb(120, 255, 160),
        "jealous" to Color.rgb(255, 140, 140),
        "surprised" to Color.rgb(255, 220, 80),
        "sad" to Color.rgb(140, 160, 220),
        "angry" to Color.rgb(255, 90, 90)
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f

        // --- 天线 ---
        fillPaint.color = tvBodyColor
        canvas.drawRect(cx - 7f, 2f, cx - 2f, 20f, fillPaint)
        canvas.drawRect(cx + 2f, 2f, cx + 7f, 20f, fillPaint)
        fillPaint.color = Color.rgb(70, 70, 75)
        canvas.drawCircle(cx - 4.5f, 2f, 4.5f, fillPaint)
        canvas.drawCircle(cx + 4.5f, 2f, 4.5f, fillPaint)

        // --- 电视机身 ---
        val bodyTop = 18f
        val bodyBottom = h - 14f
        val bodyLeft = 4f
        val bodyRight = w - 4f
        val bodyRect = RectF(bodyLeft, bodyTop, bodyRight, bodyBottom)

        fillPaint.color = tvBodyColor
        canvas.drawRoundRect(bodyRect, 12f, 12f, fillPaint)

        strokePaint.color = tvFrameColor
        canvas.drawRoundRect(bodyRect, 12f, 12f, strokePaint)

        // --- 屏幕 ---
        val sm = 10f
        val screenLeft = bodyLeft + sm
        val screenTop = bodyTop + sm
        val screenRight = bodyRight - sm
        val screenBottom = bodyBottom - sm - 8f
        val screenRect = RectF(screenLeft, screenTop, screenRight, screenBottom)

        fillPaint.color = screenBgColor
        canvas.drawRoundRect(screenRect, 5f, 5f, fillPaint)

        // 扫描线
        val scanlineAlpha = 20
        fillPaint.color = Color.argb(scanlineAlpha, 0, 0, 0)
        var sy = screenTop + 3f
        while (sy < screenBottom) {
            canvas.drawRect(screenLeft + 2f, sy, screenRight - 2f, sy + 1.5f, fillPaint)
            sy += 5f
        }

        // --- 颜文字 ---
        val emoji = moodEmojis[mood] ?: moodEmojis["idle"]!!
        textPaint.color = textColor
        val sw = screenRight - screenLeft
        val sh = screenBottom - screenTop
        val emojiWidth = textPaint.measureText(emoji)
        textPaint.textSize = textPaint.textSize * (sw * 0.78f / emojiWidth.coerceAtLeast(1f))
        val ty = screenTop + sh / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(emoji, cx, ty, textPaint)

        // --- 底部指示灯 ---
        val indicatorColor = indicatorColors[mood] ?: indicatorColors["idle"]!!
        fillPaint.color = indicatorColor
        canvas.drawCircle(cx, bodyBottom + 2f, 3.5f, fillPaint)

        // --- 底座脚 ---
        fillPaint.color = tvBodyColor
        val fw = 9f
        val fh = 7f
        canvas.drawRoundRect(RectF(cx - 20f, bodyBottom, cx - 20f + fw, bodyBottom + fh), 2.5f, 2.5f, fillPaint)
        canvas.drawRoundRect(RectF(cx + 11f, bodyBottom, cx + 11f + fw, bodyBottom + fh), 2.5f, 2.5f, fillPaint)
    }
}
