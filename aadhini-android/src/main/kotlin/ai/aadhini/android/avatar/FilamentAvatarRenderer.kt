package ai.aadhini.android.avatar

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.SurfaceView
import ai.aadhini.core.avatar.AvatarState
import com.google.android.filament.View
import com.google.android.filament.android.UiHelper
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer

/**
 * Realtime Filament renderer for Aadhini's future 3D avatar.
 *
 * The renderer deliberately falls back to an empty scene when the model asset
 * is not present. This keeps the current WebView avatar as the safe UI fallback
 * until a real Aadhini .glb model is supplied.
 */
class FilamentAvatarRenderer(private val context: Context) : AvatarRenderer {

    companion object {
        private const val MODEL_ASSET = "models/aadhini_avatar.glb"

        init {
            Utils.init()
        }
    }

    val surfaceView: SurfaceView = SurfaceView(context)
    private val uiHelper = UiHelper(UiHelper.ContextErrorPolicy.DONT_CHECK).apply {
        isOpaque = false
    }
    private val modelViewer = ModelViewer(surfaceView, uiHelper = uiHelper)
    private val choreographer = Choreographer.getInstance()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var attached = false
    private var modelLoaded = false
    private var currentState = AvatarState.IDLE

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!attached) return
            modelViewer.animator?.let { animator ->
                if (animator.animationCount > 0) {
                    // Animation selection/state mapping will be added once the
                    // production avatar model's clips are known.
                    animator.applyAnimation(0, frameTimeNanos / 1_000_000_000.0f)
                    animator.updateBoneMatrices()
                }
            }
            modelViewer.render(frameTimeNanos)
            choreographer.postFrameCallback(this)
        }
    }

    init {
        modelViewer.view.blendMode = View.BlendMode.TRANSLUCENT
        modelViewer.renderer.clearOptions = modelViewer.renderer.clearOptions.apply {
            clear = true
        }
        surfaceView.setOnTouchListener { _, event ->
            modelViewer.onTouchEvent(event)
            true
        }
    }

    /** Attach the renderer to its SurfaceView and begin the render loop. */
    fun attach() {
        if (attached) return
        attached = true
        if (!modelLoaded) loadModelIfPresent()
        choreographer.postFrameCallback(frameCallback)
    }

    private fun loadModelIfPresent() {
        try {
            val bytes = context.assets.open(MODEL_ASSET).use { input ->
                input.readBytes()
            }
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes)
            buffer.flip()
            modelViewer.loadModelGlb(buffer)
            modelViewer.transformToUnitCube()
            modelLoaded = true
        } catch (_: Exception) {
            // Model is optional until the production .glb is supplied.
            modelLoaded = false
        }
    }

    override fun setState(state: AvatarState) {
        currentState = state
        // The production model's animation clips / morph targets will map here.
        // Keeping the state now makes the renderer ready for that model contract.
        mainHandler.post { currentState = state }
    }

    override fun release() {
        if (!attached) return
        attached = false
        choreographer.removeFrameCallback(frameCallback)
        modelViewer.destroyModel()
        uiHelper.detach()
    }
}
