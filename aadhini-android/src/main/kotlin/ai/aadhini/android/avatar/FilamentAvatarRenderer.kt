package ai.aadhini.android.avatar

import android.content.Context
import android.view.Surface
import android.view.SurfaceView
import ai.aadhini.core.avatar.AvatarState
import com.google.android.filament.Engine
import com.google.android.filament.android.UiHelper

/** First real-time 3D renderer boundary for Aadhini. */
class FilamentAvatarRenderer(context: Context) : AvatarRenderer {

    private val engine = Engine.create()
    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    val surfaceView: SurfaceView = SurfaceView(context)

    init {
        uiHelper.renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: Surface) {
                // Swap-chain and scene setup will be added with the first glTF model.
            }

            override fun onDetachedFromSurface() {
                // Resources remain owned by this renderer until release().
            }

            override fun onResized(width: Int, height: Int) {
                // Camera viewport will be configured with the model.
            }
        }
    }

    fun attach() {
        uiHelper.attachTo(surfaceView)
    }

    override fun setState(state: AvatarState) {
        // State-to-animation mapping comes with the first 3D model integration.
    }

    override fun release() {
        uiHelper.detach()
        engine.destroy()
    }
}
