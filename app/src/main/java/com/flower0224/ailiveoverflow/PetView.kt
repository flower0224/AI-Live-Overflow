package com.flower0224.ailiveoverflow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.view.View

class PetView(context: Context) : View(context) {

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pupilPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blushPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mouthPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var blinkAlpha = 255
    private val handler = Handler(Looper.getMainLooper())
    private var isBlinking = false

    init {
        blushPaint.color = Color.argb(80, 255, 160, 160)
        mouthPaint.color = Color.argb(200, 120, 80, 80)
        mouthPaint.style = Paint.Style.STROKE
        mouthPaint.strokeWidth = 3f
        startBlinkTimer()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val cx = w / 2f
        val cy = h / 2f
        val radius = w.coerceAtMost(h) / 2f

        bodyPaint.shader = RadialGradient(
            cx, cy - radius * 0.15f, radius,
            intArrayOf(
                Color.rgb(255, 250, 245),
                Color.rgb(240, 220, 210)
            ),
            null,
            Shader.TileMode.CLAMP
        )
    }

    private fun startBlinkTimer() {
        handler.postDelayed({
            blink()
            startBlinkTimer()
        }, 3000L + (Math.random() * 2000).toLong())
    }

    private fun blink() {
        isBlinking = true
        handler.postDelayed({ isBlinking = false; invalidate() }, 150)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val r = width.coerceAtMost(height) / 2f - 10f

        // body
        canvas.drawCircle(cx, cy, r, bodyPaint)

        // blush
        canvas.drawCircle(cx - r * 0.4f, cy + r * 0.15f, r * 0.15f, blushPaint)
        canvas.drawCircle(cx + r * 0.4f, cy + r * 0.15f, r * 0.18f, blushPaint)

        // eyes
        val eyeY = cy - r * 0.1f
        drawEye(canvas, cx - r * 0.3f, eyeY, r * 0.18f)
        drawEye(canvas, cx + r * 0.3f, eyeY, r * 0.18f)

        // mouth
        canvas.drawArc(
            cx - r * 0.2f, cy + r * 0.05f,
            cx + r * 0.2f, cy + r * 0.35f,
            0f, -180f, false, mouthPaint
        )
    }

    private fun drawEye(canvas: Canvas, x: Float, y: Float, radius: Float) {
        if (isBlinking) {
            // closed eye
            eyePaint.color = Color.rgb(100, 70, 60)
            eyePaint.style = Paint.Style.STROKE
            eyePaint.strokeWidth = 3f
            canvas.drawLine(x - radius, y, x + radius, y, eyePaint)
            eyePaint.style = Paint.Style.FILL
        } else {
            // eye white
            eyePaint.color = Color.WHITE
            eyePaint.style = Paint.Style.FILL
            canvas.drawCircle(x, y, radius, eyePaint)

            // pupil
            pupilPaint.color = Color.rgb(60, 40, 30)
            canvas.drawCircle(x, y, radius * 0.6f, pupilPaint)

            // highlight
            highlightPaint.color = Color.WHITE
            canvas.drawCircle(x - radius * 0.25f, y - radius * 0.3f, radius * 0.25f, highlightPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
    }
}
