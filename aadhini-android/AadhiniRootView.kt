package ai.aadhini.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

/**
 * Native, dependency-light Aadhini presentation shell.
 * It deliberately owns only visual state and navigation; Core/Platform remain untouched.
 */
class AadhiniRootView(context: Context) : View(context) {
    private enum class Screen { BOOT, HOME, CONVERSATION }

    private var screen = Screen.BOOT
    private var bootProgress = 0f
    private var bootStep = 0
    private var bootMuted = false
    private var avatarPulse = 0f
    private var device = "MOTO"
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val handler = Handler(Looper.getMainLooper())
    private val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG)

    private val bootItems = arrayOf("Memory", "Context", "Decision", "Platform")

    init {
        isFocusable = true
        paint.typeface = Typeface.create("sans", Typeface.NORMAL)
        stroke.style = Paint.Style.STROKE
        stroke.strokeWidth = 2f
        post(frameLoop)
        post(bootLoop)
    }

    private val frameLoop = object : Runnable {
        override fun run() {
            avatarPulse += 0.035f
            invalidate()
            postDelayed(this, 33L)
        }
    }

    private val bootLoop = object : Runnable {
        override fun run() {
            if (screen != Screen.BOOT) return
            bootProgress += 0.035f
            val targetStep = (bootProgress * bootItems.size).toInt().coerceAtMost(bootItems.size)
            if (targetStep > bootStep) {
                bootStep = targetStep
                if (!bootMuted) tone.startTone(ToneGenerator.TONE_PROP_BEEP, 55)
            }
            if (bootProgress >= 1f) {
                if (!bootMuted) tone.startTone(ToneGenerator.TONE_PROP_ACK, 90)
                screen = Screen.HOME
                invalidate()
                return
            }
            invalidate()
            postDelayed(this, 85L)
        }
    }

    override fun onDetachedFromWindow() {
        tone.release()
        handler.removeCallbacksAndMessages(null)
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(0xFF050608.toInt())
        drawAurora(canvas)
        when (screen) {
            Screen.BOOT -> drawBoot(canvas)
            Screen.HOME -> drawHome(canvas)
            Screen.CONVERSATION -> drawConversation(canvas)
        }
    }

    private fun drawAurora(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w * 0.52f
        val cy = h * 0.48f
        val radius = min(w, h) * 0.42f
        val glow = Paint(Paint.ANTI_ALIAS_FLAG)
        glow.shader = android.graphics.RadialGradient(
            cx, cy, radius,
            intArrayOf(0x332f4cff, 0x181f2a78, 0x00050608),
            floatArrayOf(0f, 0.42f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, radius, glow)
    }

    private fun drawBoot(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val center = w / 2f
        val logoY = h * 0.34f
        drawAadhiniLogo(canvas, center, logoY, min(w, h) * 0.095f, true)

        text(canvas, "A A D H I N I", center, logoY + h * 0.10f, 20f, 0xFFF7F8FF.toInt(), true)
        text(canvas, "Restoring your world...", center, logoY + h * 0.17f, 16f, 0xB8D7DCEF.toInt(), true)

        val left = w * 0.14f
        val right = w * 0.86f
        val top = h * 0.56f
        val row = h * 0.075f
        bootItems.forEachIndexed { index, label ->
            val ready = index < bootStep
            text(canvas, label, left, top + row * index, 16f, 0xFFF5F7FF.toInt(), false)
            val status = if (ready) "✓ Ready" else "Loading"
            val color = if (ready) 0xFF18E39A.toInt() else 0x85AEB7C8.toInt()
            text(canvas, status, right, top + row * index, 16f, color, true, alignRight = true)
        }

        val barLeft = w * 0.16f
        val barRight = w * 0.84f
        val barY = top + row * bootItems.size + h * 0.035f
        rounded(canvas, RectF(barLeft, barY, barRight, barY + 5f), 3f, 0x281F2A44)
        rounded(canvas, RectF(barLeft, barY, barLeft + (barRight - barLeft) * bootProgress, barY + 5f), 3f, 0xFF6678FF.toInt())

        text(canvas, "Continue where we left off.", center, h * 0.88f, 15f, 0x85AEB7C8.toInt(), true)
        drawMute(canvas, w - 28f, 30f)
    }

    private fun drawHome(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val pad = 20f

        drawMute(canvas, w - 28f, 30f)
        drawDevice(canvas, pad, 30f)
        drawAvatarStatus(canvas, w - 82f, 30f)

        drawAadhiniLogo(canvas, pad + 28f, 92f, 27f, false)
        text(canvas, "AADHINI", pad + 66f, 96f, 18f, 0xFFF5F7FF.toInt(), false)
        text(canvas, "Good day, Chief.", pad + 66f, 120f, 14f, 0xB8D7DCEF.toInt(), false)

        glass(canvas, RectF(pad, 148f, w - pad, 220f), 22f)
        text(canvas, "TODAY'S BRIEF", pad + 18f, 175f, 12f, 0xB8D7DCEF.toInt(), false)
        text(canvas, "You're all caught up.", pad + 18f, 202f, 17f, 0xFFF5F7FF.toInt(), false)

        val contentTop = 242f
        if (h > w) {
            drawPortraitHome(canvas, pad, contentTop, w, h)
        } else {
            drawLandscapeHome(canvas, pad, contentTop, w, h)
        }
    }

    private fun drawPortraitHome(canvas: Canvas, pad: Float, top: Float, w: Float, h: Float) {
        glass(canvas, RectF(pad, top, w - pad, top + 78f), 22f)
        text(canvas, "⌕", pad + 22f, top + 48f, 30f, 0xFFF5F7FF.toInt(), false)
        text(canvas, "Search", pad + 62f, top + 47f, 16f, 0x85AEB7C8.toInt(), false)

        val y = top + 98f
        quickCard(canvas, pad, y, w - pad, y + 78f, "◉", "Conversation", "Open Aadhini chat", 2)
        quickCard(canvas, pad, y + 92f, w - pad, y + 170f, "☎", "Calls", "Quick action", 0)
        quickCard(canvas, pad, y + 184f, w - pad, y + 262f, "🚲", "BIK-E", "$device connected", 1)
        quickCard(canvas, pad, y + 276f, w - pad, y + 354f, "▣", "Notifications", "Nothing urgent", 0)

        text(canvas, "Tap a card to open its module", w / 2f, min(h - 26f, y + 390f), 12f, 0x85AEB7C8.toInt(), true)
    }

    private fun drawLandscapeHome(canvas: Canvas, pad: Float, top: Float, w: Float, h: Float) {
        val gap = 14f
        val cardW = (w - pad * 2f - gap * 2f) / 3f
        quickCard(canvas, pad, top, pad + cardW, h - 30f, "◉", "Conversation", "Chat, voice & avatar", 2)
        quickCard(canvas, pad + cardW + gap, top, pad + cardW * 2f + gap, h - 30f, "🚲", "BIK-E", "Live vehicle status", 1)
        quickCard(canvas, pad + cardW * 2f + gap * 2f, top, w - pad, h - 30f, "◌", "Activity", "Notifications & tasks", 0)
    }

    private fun quickCard(canvas: Canvas, l: Float, t: Float, r: Float, b: Float, icon: String, title: String, subtitle: String, badge: Int) {
        glass(canvas, RectF(l, t, r, b), 22f)
        text(canvas, icon, l + 22f, t + 38f, 24f, 0xFF9A72FF.toInt(), false)
        text(canvas, title, l + 22f, t + 68f, 17f, 0xFFF5F7FF.toInt(), false)
        text(canvas, subtitle, l + 22f, t + 91f, 12f, 0xB8D7DCEF.toInt(), false)
        if (badge > 0) {
            val cx = r - 24f
            val cy = t + 24f
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = 0xFF6678FF.toInt()
            canvas.drawCircle(cx, cy, 12f, p)
            text(canvas, badge.toString(), cx, cy + 5f, 11f, 0xFFFFFFFF.toInt(), true)
        }
    }

    private fun drawConversation(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val pad = 18f
        drawBack(canvas, pad, 34f)
        drawAadhiniLogo(canvas, pad + 62f, 34f, 21f, false)
        text(canvas, "AADHINI", pad + 94f, 39f, 16f, 0xFFF5F7FF.toInt(), false)
        text(canvas, "Ready to listen", pad + 94f, 59f, 11f, 0xFF18E39A.toInt(), false)
        drawAvatarStatus(canvas, w - 34f, 34f)

        text(canvas, "How can I help, Chief?", pad, h * 0.28f, 25f, 0xFFF5F7FF.toInt(), false)
        text(canvas, "Text or voice — both work here.", pad, h * 0.28f + 30f, 14f, 0xB8D7DCEF.toInt(), false)

        val box = RectF(pad, h - 78f, w - pad, h - 18f)
        glass(canvas, box, 28f)
        text(canvas, "Message Aadhini...", pad + 20f, h - 42f, 14f, 0x85AEB7C8.toInt(), false)
        text(canvas, "●", w - 52f, h - 40f, 18f, 0xFF61E7FF.toInt(), true)
    }

    private fun drawAadhiniLogo(canvas: Canvas, cx: Float, cy: Float, size: Float, animated: Boolean) {
        val glow = Paint(Paint.ANTI_ALIAS_FLAG)
        val pulse = if (animated) 1f + 0.08f * kotlin.math.sin(avatarPulse * 2.0).toFloat() else 1f
        glow.color = 0x556678FF
        canvas.drawCircle(cx, cy, size * 1.25f * pulse, glow)

        val p = Path()
        p.moveTo(cx - size * 0.72f, cy + size * 0.72f)
        p.lineTo(cx, cy - size * 0.82f)
        p.lineTo(cx + size * 0.72f, cy + size * 0.72f)
        p.lineTo(cx + size * 0.43f, cy + size * 0.72f)
        p.lineTo(cx, cy - size * 0.15f)
        p.lineTo(cx - size * 0.43f, cy + size * 0.72f)
        p.close()
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)
        fill.shader = LinearGradient(cx, cy - size, cx, cy + size, 0xFF6678FF.toInt(), 0xFF9A72FF.toInt(), Shader.TileMode.CLAMP)
        canvas.drawPath(p, fill)
    }

    private fun drawAvatarStatus(canvas: Canvas, cx: Float, cy: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = 0xFF11141C.toInt()
        canvas.drawCircle(cx, cy, 25f, p)
        stroke.color = 0x2AFFFFFF
        canvas.drawCircle(cx, cy, 25f, stroke)
        val pulse = 1f + 0.15f * kotlin.math.sin(avatarPulse).toFloat()
        p.color = 0xFF18E39A.toInt()
        canvas.drawCircle(cx + 17f, cy + 17f, 5f * pulse, p)
        text(canvas, "A", cx, cy + 6f, 18f, 0xFF9A72FF.toInt(), true)
    }

    private fun drawDevice(canvas: Canvas, x: Float, y: Float) {
        glass(canvas, RectF(x, y - 18f, x + 78f, y + 18f), 18f)
        text(canvas, device, x + 39f, y + 5f, 11f, 0xFF61E7FF.toInt(), true)
    }

    private fun drawMute(canvas: Canvas, cx: Float, cy: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = 0xB8F5F7FF.toInt()
        canvas.drawCircle(cx, cy, 14f, p)
        text(canvas, if (bootMuted) "×" else "•", cx, cy + 5f, 13f, 0xFF050608.toInt(), true)
    }

    private fun drawBack(canvas: Canvas, x: Float, y: Float) {
        text(canvas, "‹", x + 12f, y + 9f, 34f, 0xFFF5F7FF.toInt(), false)
    }

    private fun glass(canvas: Canvas, rect: RectF, radius: Float) {
        rounded(canvas, rect, radius, 0x190B0D12)
        stroke.color = 0x2AFFFFFF
        stroke.strokeWidth = 1f
        canvas.drawRoundRect(rect, radius, radius, stroke)
    }

    private fun rounded(canvas: Canvas, rect: RectF, radius: Float, color: Int) {
        paint.shader = null
        paint.color = color
        canvas.drawRoundRect(rect, radius, radius, paint)
    }

    private fun text(canvas: Canvas, value: String, x: Float, baseline: Float, size: Float, color: Int, center: Boolean, alignRight: Boolean = false) {
        paint.shader = null
        paint.color = color
        paint.textSize = size
        paint.typeface = Typeface.create("sans", if (center) Typeface.BOLD else Typeface.NORMAL)
        paint.textAlign = when {
            alignRight -> Paint.Align.RIGHT
            center -> Paint.Align.CENTER
            else -> Paint.Align.LEFT
        }
        canvas.drawText(value, x, baseline, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        lastTouchX = event.x
        lastTouchY = event.y
        val w = width.toFloat()
        val h = height.toFloat()

        when (screen) {
            Screen.BOOT -> {
                if (event.x > w - 60f && event.y < 70f) {
                    bootMuted = !bootMuted
                    invalidate()
                }
            }
            Screen.HOME -> {
                if (event.x > w - 115f && event.y < 80f) {
                    screen = Screen.CONVERSATION
                    invalidate()
                } else if (event.y > 250f && event.y < 390f) {
                    screen = Screen.CONVERSATION
                    invalidate()
                }
            }
            Screen.CONVERSATION -> {
                if (event.x < 70f && event.y < 80f) {
                    screen = Screen.HOME
                    invalidate()
                }
            }
        }
        return true
    }
}
