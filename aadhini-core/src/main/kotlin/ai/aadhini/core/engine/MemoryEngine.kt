package ai.aadhini.core.engine

import ai.aadhini.core.memory.Memory

interface MemoryEngine {
    fun remember(content: String): Memory
    fun recall(): List<Memory>
    fun clear()
}
