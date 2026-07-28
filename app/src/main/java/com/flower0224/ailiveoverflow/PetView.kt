package com.flower0224.ailiveoverflow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.View

class PetView(context: Context) : View(context) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val handler = Handler(Looper.getMainLooper())
    private var isBlinking = false

    // pixel robot colors
    private val bodyColor = Color.rgb(180, 210, 230)       // soft blue-gray
    private val darkColor = Color.rgb(100, 130, 160)       // shadow / outline
    private val accentColor = Color.rgb(255, 200, 120)     // warm orange accent
    private val eyeColor = Color.rgb(50, 50, 60)           // dark eyes
    private val blushColor = Color.argb(100, 255, 160, 150)

    private var gridSize = 8f

    init {
        startBlinkTimer()
    }

    private fun startBlinkTimer() {
        handler.postDelayed({
            blink()
            startBlinkTimer()
        }, 3500L + (Math.random() * 2000).toLong())
    }

    private fun blink() {
        isBlinking = true
        handler.postDelayed({ isBlinking = false; invalidate() }, 120)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gridSize = w.coerceAtMost(h) / 18f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val top = height / 2f - gridSize * 7f
        val g = gridSize

        // --- antenna ---
        fillPaint.color = darkColor
        canvas.drawRect(cx - g * 0.3f, top - g * 2.5f, cx + g * 0.3f, top - g * 0.3f, fillPaint)
        fillPaint.color = accentColor
        canvas.drawRect(cx - g * 0.8f, top - g * 3f, cx + g * 0.8f, top - g * 2.5f, fillPaint)

        // --- head ---
        drawPixelRect(canvas, cx - g * 4f, top, cx + g * 4f, top + g * 6f, bodyColor)

        // --- face plate ---
        drawPixelRect(canvas, cx - g * 3f, top + g * 0.8f, cx + g * 3f, top + g * 5.2f, Color.rgb(220, 235, 245))

        // --- eyes ---
        val eyeY = top + g * 2.2f
        fillPaint.color = eyeColor
        if (isBlinking) {
            // blink: horizontal lines
            canvas.drawRect(cx - g * 1.8f, eyeY + g * 0.6f, cx - g * 0.3f, eyeY + g * 1f, fillPaint)
            canvas.drawRect(cx + g * 0.3f, eyeY + g * 0.6f, cx + g * 1.8f, eyeY + g * 1f, fillPaint)
        } else {
            canvas.drawRect(cx - g * 1.8f, eyeY, cx - g * 0.3f, eyeY + g * 1.5f, fillPaint)
            canvas.drawRect(cx + g * 0.3f, eyeY, cx + g * 1.8f, eyeY + g * 1.5f, fillPaint)
            // eye highlights
            fillPaint.color = Color.WHITE
            canvas.drawRect(cx - g * 1.2f, eyeY + g * 0.1f, cx - g * 0.6f, eyeY + g * 0.5f, fillPaint)
            canvas.drawRect(cx + g * 0.9f, eyeY + g * 0.1f, cx + g * 1.5f, eyeY + g * 0.5f, fillPaint)
        }

        // --- blush ---
        fillPaint.color = blushColor
        canvas.drawRect(cx - g * 3.2f, top + g * 3.5f, cx - g * 1.8f, top + g * 4.2f, fillPaint)
        canvas.drawRect(cx + g * 1.8f, top + g * 3.5f, cx + g * 3.2f, top + g * 4.2f, fillPaint)

        // --- mouth ---
        fillPaint.color = darkColor
        canvas.drawRect(cx - g * 0.6f, top + g * 4f, cx + g * 0.6f, top + g * 4.4f, fillPaint)

        // --- neck ---
        fillPaint.color = darkColor
        canvas.drawRect(cx - g * 1f, top + g * 6f, cx + g * 1f, top + g * 7f, fillPaint)

        // --- body ---
        drawPixelRect(canvas, cx - g * 3.5f, top + g * 7f, cx + g * 3.5f, top + g * 11f, bodyColor)

        // --- chest indicator ---
        fillPaint.color = accentColor
        canvas.drawRect(cx - g * 0.6f, top + g * 8.2f, cx + g * 0.6f, top + g * 9f, fillPaint)

        // --- arms ---
        drawPixelRect(canvas, cx - g * 5f, top + g * 7.5f, cx - g * 3.5f, top + g * 10f, bodyColor)
        drawPixelRect(canvas, cx + g * 3.5f, top + g * 7.5f, cx + g * 5f, top + g * 10f, bodyColor)

        // --- hands ---
        fillPaint.color = accentColor
        canvas.drawRect(cx - g * 5.5f, top + g * 9f, cx - g * 5f, top + g * 10f, fillPaint)
        canvas.drawRect(cx + g * 5f, top + g * 9f, cx + g * 5.5f, top + g * 10f, fillPaint)

        // --- feet ---
        fillPaint.color = darkColor
        canvas.drawRect(cx - g * 2.5f, top + g * 11f, cx - g * 0.5f, top + g * 12f, fillPaint)
        canvas.drawRect(cx + g * 0.5f, top + g * 11f, cx + g * 2.5f, top + g * 12f, fillPaint)
    }

    private fun drawPixelRect(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        fillPaint.color = color
        canvas.drawRect(left, top, right, bottom, fillPaint)
        // subtle outline
        strokePaint.color = darkColor
        canvas.drawRect(left, top, right, bottom, strokePaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
    }
}
