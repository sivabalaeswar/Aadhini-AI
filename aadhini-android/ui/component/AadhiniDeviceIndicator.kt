package ai.aadhini.android.ui.component

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import ai.aadhini.android.ui.theme.AadhiniPalette

/** Compact presentation control showing the active Aadhini device environment. */
class AadhiniDeviceIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    init {
        gravity = Gravity.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 14f
        setTextColor(AadhiniPalette.TextPrimary)
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(18).toFloat()
            setColor(AadhiniPalette.SurfaceElevated)
            setStroke(dp(1), AadhiniPalette.Border)
        }
        contentDescription = "Active Aadhini device"
        setDevice("📱", "Aadhini")
    }

    fun setDevice(symbol: String, name: String) {
        text = "$symbol  $name ⌄"
        contentDescription = "Switch Aadhini device, current device: $name"
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
