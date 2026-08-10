package ai.aadhini.android.avatar

import android.content.Context
import android.view.SurfaceView
import com.google.android.filament.Engine
import com.google.android.filament.android.UiHelper

/**
 * First real-time 3D renderer boundary for Aadhini.
 *
 * Model loading and animation are intentionally kept out of this first step.
 * The renderer can be plugged into the existing AvatarRenderer lifecycle
 * without changing AadhiniCore or the conversation pipeline.
 */
class FilamentAvatarRenderer(context: Context) : AvatarRenderer {

    private val engine = Engine.create()
    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK)
    private val surfaceView = SurfaceView(context)

    override fun attach() {
        uiHelper.renderCallback = object : UiHelper.RendererCallback {
            override fun onNativeWindowChanged(surface: android.view.Surface) {
                // Swap-chain creation and scene setup will be added with the model loader.
            }

            override fun onDetachedFromSurface() {
                // Rendering resources are released when the renderer is detached.
            }

            override fun onResized(width: Int, height: Int) {
                // Camera viewport will be updated when the first model is loaded.
            }
        }
        uiHelper.attachTo(surfaceView)
    }

    override fun detach() {
        uiHelper.detach()
        engine.destroy()
    }

    override fun setState(state: AvatarState) {
        // State-to-animation mapping will drive the 3D model in the next phase.
    }
}
