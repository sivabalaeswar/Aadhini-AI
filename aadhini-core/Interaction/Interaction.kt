package ai.aadhini.core.interaction

data class Interaction(
    val id: InteractionId,
    val type: InteractionType,
    val source: InteractionSource,
    val content: InteractionContent,
    val properties: InteractionProperties = emptyMap()
)