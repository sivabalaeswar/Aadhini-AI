package ai.aadhini.core.intent

data class Intent(
    val id: IntentId,
    val type: IntentType,
    val properties: IntentProperties = emptyMap()
)