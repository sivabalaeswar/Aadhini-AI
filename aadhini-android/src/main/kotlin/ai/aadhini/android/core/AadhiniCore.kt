package ai.aadhini.android.core

import ai.aadhini.android.ai.AIProviderManager
import ai.aadhini.android.ai.DemoProvider
import ai.aadhini.android.ai.GeminiProvider
import ai.aadhini.core.engine.BasicIntentEngine
import ai.aadhini.core.intent.Intent

class AadhiniCore {

    private val providerManager = AIProviderManager(
        providers = mapOf(
            "demo" to DemoProvider(),
            "gemini" to GeminiProvider()
        ),
        activeProvider = "demo"
    )

    private val intentEngine = BasicIntentEngine()

    private var lastIntent: Intent? = null

    fun process(input: String): String {
        lastIntent = intentEngine.detect(input)
        return providerManager.process(input)
    }

    fun getLastIntent(): Intent? {
        return lastIntent
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
