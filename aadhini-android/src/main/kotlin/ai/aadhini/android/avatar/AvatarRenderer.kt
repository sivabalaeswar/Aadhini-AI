package ai.aadhini.android.avatar

import ai.aadhini.core.avatar.AvatarState

/**
 * Rendering boundary for Aadhini's realtime avatar.
 * The current WebView renderer is a fallback implementation.
 * A future 3D renderer can implement this contract without changing AadhiniCore.
 */
interface AvatarRenderer {
    fun setState(state: AvatarState)
    fun release()
}
