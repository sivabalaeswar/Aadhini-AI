package ai.aadhini.android.ui.component

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.TextView
import ai.aadhini.android.ui.theme.AadhiniPalette

/** Compact status indicator for live, warning, error, and neutral states. */
class AadhiniStatusIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    enum class State { LIVE, WARNING, ERROR, NEUTRAL }

    fun setState(state: State, label: String? = null) {
        val (color, defaultLabel) = when (state) {
            State.LIVE -> AadhiniPalette.Success to "Live"
            State.WARNING -> AadhiniPalette.Warning to "Attention"
            State.ERROR -> AadhiniPalette.Error to "Offline"
            State.NEUTRAL -> AadhiniPalette.TextSecondary to "Ready"
        }

        text = label ?: defaultLabel
        setTextColor(color)
        textSize = 14f
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = resources.displayMetrics.density * 18f
            setColor(withAlpha(color, 0x24))
            setStroke(dp(1), withAlpha(color, 0x55))
        }
        setPadding(dp(12), dp(6), dp(12), dp(6))
    }

    init {
        setState(State.NEUTRAL)
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
