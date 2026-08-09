package ai.aadhini.android.ui.component

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import ai.aadhini.android.ui.theme.AadhiniPalette
import ai.aadhini.android.ui.theme.DesignTokens

/** A reusable dark glass-like presentation surface. */
class AadhiniSurface @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        val radius = dp(DesignTokens.CardCornerRadiusDp)
        background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(AadhiniPalette.Surface)
            setStroke(dp(1), AadhiniPalette.Border)
        }
        setPadding(
            dp(DesignTokens.ScreenPaddingDp),
            dp(DesignTokens.ScreenPaddingDp),
            dp(DesignTokens.ScreenPaddingDp),
            dp(DesignTokens.ScreenPaddingDp)
        )
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
