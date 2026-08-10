package ai.aadhini.android.core

import ai.aadhini.android.ai.AIProviderManager
import ai.aadhini.android.ai.DemoProvider
import ai.aadhini.android.ai.GeminiProvider
import ai.aadhini.core.context.Context
import ai.aadhini.core.decision.Decision
import ai.aadhini.core.engine.BasicContextEngine
import ai.aadhini.core.engine.BasicDecisionEngine
import ai.aadhini.core.engine.BasicIntentEngine
import ai.aadhini.core.engine.BasicMemoryEngine
import ai.aadhini.core.intent.Intent
import ai.aadhini.core.memory.Memory

class AadhiniCore {

    private val providerManager = AIProviderManager(
        providers = mapOf(
            "demo" to DemoProvider(),
            "gemini" to GeminiProvider()
        ),
        activeProvider = "demo"
    )

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
        return providerManager.process(input)
    }

    fun getLastIntent(): Intent? {
        return lastIntent
    }

    fun getLastDecision(): Decision? {
        return lastDecision
    }

    fun getLastDecisionType(): String {
        return lastDecision?.type?.name ?: "UNKNOWN"
    }

    fun getLastContext(): Context? {
        return lastContext
    }

    fun getLastContextType(): String {
        return lastContext?.type?.name ?: "UNKNOWN"
    }

    fun getMemoryCount(): Int {
        return memoryEngine.recall().size
    }

    fun getMemories(): List<Memory> {
        return memoryEngine.recall()
    }

    fun clearMemory() {
        memoryEngine.clear()
    }

    fun setProvider(name: String): Boolean {
        return providerManager.setProvider(name)
    }

    fun getActiveProvider(): String {
        return providerManager.getActiveProvider()
    }

    fun getAvailableProviders(): List<String> {
        return providerManager.getAvailableProviders()
    }
}
