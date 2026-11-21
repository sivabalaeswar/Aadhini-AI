package ai.aadhini.core.engine

import ai.aadhini.core.ai.AIProvider
import ai.aadhini.core.context.Context
import ai.aadhini.core.decision.Decision
import ai.aadhini.core.intent.Intent
import ai.aadhini.core.memory.Memory

/**
 * Core orchestration entry point.
 *
 * The Android app supplies an AIProvider implementation from Platform.
 * Provider/network details remain outside Core and can be replaced without
 * changing Aadhini's models, intent, decision, context or memory engines.
 */
class AadhiniEngine(
    private val aiProvider: AIProvider
) {
    private val intentEngine = BasicIntentEngine()
    private val decisionEngine = BasicDecisionEngine()
    private val memoryEngine = BasicMemoryEngine()
    private val contextEngine = BasicContextEngine()

    private var lastIntent: Intent? = null
    private var lastDecision: Decision? = null
    private var lastContext: Context? = null

    fun process(input: String): String {
        val intent = intentEngine.detect(input)
        lastIntent = intent
        lastDecision = decisionEngine.decide(intent)
        lastContext = contextEngine.update(input, intent)
        memoryEngine.remember(input)

        // AIProvider is an injected port. In the current prototype Platform
        // implements it by calling the separate Termux Core Runtime.
        return aiProvider.process(input)
    }

    fun getLastIntent(): Intent? = lastIntent

    fun getLastDecision(): Decision? = lastDecision

    fun getLastDecisionType(): String = lastDecision?.type?.name ?: "UNKNOWN"

    fun getLastContext(): Context? = lastContext

    fun getLastContextType(): String = lastContext?.type?.name ?: "UNKNOWN"

    fun getMemoryCount(): Int = memoryEngine.recall().size

    fun getMemories(): List<Memory> = memoryEngine.recall()

    fun clearMemory() = memoryEngine.clear()

    fun getProviderName(): String = "TERMUX CORE"
}
