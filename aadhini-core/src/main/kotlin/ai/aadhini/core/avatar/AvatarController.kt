package ai.aadhini.core.avatar

class AvatarController {
    var state: AvatarState = AvatarState.IDLE
        private set

    fun setState(next: AvatarState) {
        state = next
    }

    fun onListening() = setState(AvatarState.LISTENING)
    fun onThinking() = setState(AvatarState.THINKING)
    fun onSpeaking() = setState(AvatarState.SPEAKING)
    fun onIdle() = setState(AvatarState.IDLE)
    fun onHappy() = setState(AvatarState.HAPPY)
    fun onConfused() = setState(AvatarState.CONFUSED)
    fun onError() = setState(AvatarState.ERROR)
}
