package ai.aadhini.core.memory

data class Memory(
    val id: MemoryId,
    val type: MemoryType,
    val content: String,
    val properties: MemoryProperties = emptyMap()
)