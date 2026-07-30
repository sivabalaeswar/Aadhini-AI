package ai.aadhini.platform.voice

data class Voice(
    val id: VoiceId,
    val type: VoiceType,
    val properties: VoiceProperties = emptyMap()
)