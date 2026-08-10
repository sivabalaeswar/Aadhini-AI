package ai.aadhini.platform.conversation

data class Conversation(
    val id: ConversationId,
    val type: ConversationType,
    val properties: ConversationProperties = emptyMap()
)