package ai.aadhini.core.context

data class Context(
    val id: ContextId,
    val type: ContextType,
    val properties: ContextProperties = emptyMap()
)