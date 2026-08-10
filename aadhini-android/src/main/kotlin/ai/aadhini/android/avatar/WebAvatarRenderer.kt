package ai.aadhini.android.avatar

import android.webkit.WebView
import ai.aadhini.core.avatar.AvatarState

class WebAvatarRenderer(
    private val webView: WebView
) : AvatarRenderer {

    override fun setState(state: AvatarState) {
        val command = when (state) {
            AvatarState.LISTENING -> "avatarState('listening')"
            AvatarState.THINKING -> "avatarState('thinking')"
            AvatarState.SPEAKING -> "avatarTalk(true)"
            AvatarState.HAPPY -> "avatarState('happy')"
            AvatarState.CONFUSED -> "avatarState('confused')"
            AvatarState.ERROR -> "avatarState('error')"
            AvatarState.IDLE -> "avatarTalk(false); avatarState('idle')"
        }
        webView.evaluateJavascript(command, null)
    }

    override fun release() = Unit
}
