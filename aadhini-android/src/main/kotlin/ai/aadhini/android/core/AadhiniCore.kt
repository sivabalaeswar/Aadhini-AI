package ai.aadhini.android.core

import ai.aadhini.android.ai.AIProviderManager
import ai.aadhini.android.ai.DemoProvider
import ai.aadhini.core.context.Context
import ai.aadhini.core.decision.Decision
import ai.aadhini.core.engine.BasicContextEngine
import ai.aadhini.core.engine.BasicDecisionEngine
import ai.aadhini.core.engine.BasicIntentEngine
import ai.aadhini.core.engine.BasicMemoryEngine
import ai.aadhini.core.intent.Intent
import ai.aadhini.core.memory.Memory
import ai.aadhini.platform.adapter.AadhiniCoreClient

class AadhiniCore {

    private val providerManager = AIProviderManager(
        providers = mapOf(
            "demo" to DemoProvider(),
            "core" to AadhiniCoreClient()
        ),
        activeProvider = "core"
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

        // Every conversation turn goes through the separate Core Runtime.
        // No hard-coded greeting response should bypass Aadhini's identity,
        // history, memory retrieval or selected AI model.
        return providerManager.process(input)
    }

    fun getLastIntent(): Intent? = lastIntent

    fun getLastDecision(): Decision? = lastDecision

    fun getLastDecisionType(): String = lastDecision?.type?.name ?: "UNKNOWN"

    fun getLastContext(): Context? = lastContext

    fun getLastContextType(): String = lastContext?.type?.name ?: "UNKNOWN"

    fun getMemoryCount(): Int = memoryEngine.recall().size

    fun getMemories(): List<Memory> = memoryEngine.recall()

    fun clearMemory() = memoryEngine.clear()

    fun setProvider(name: String): Boolean = providerManager.setProvider(name)

    fun getActiveProvider(): String = providerManager.getActiveProvider()

    fun getAvailableProviders(): List<String> = providerManager.getAvailableProviders()
}
