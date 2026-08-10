package ai.aadhini.core.engine

import ai.aadhini.core.memory.Memory
import ai.aadhini.core.memory.MemoryId
import ai.aadhini.core.memory.MemoryType

class BasicMemoryEngine : MemoryEngine {

    private val memories = mutableListOf<Memory>()

    override fun remember(content: String): Memory {
        val memory = Memory(
            id = MemoryId("memory-${System.currentTimeMillis()}"),
            type = MemoryType.SHORT_TERM,
            content = content,
            properties = mapOf("source" to "aadhini-core")
        )
        memories += memory
        return memory
    }

    override fun recall(): List<Memory> {
        return memories.toList()
    }

    override fun clear() {
        memories.clear()
    }
}
