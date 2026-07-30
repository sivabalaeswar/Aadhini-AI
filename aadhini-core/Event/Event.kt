package ai.aadhini.core.event

data class Event(
    val id: EventId,
    val type: EventType,
    val properties: EventProperties = emptyMap()
)