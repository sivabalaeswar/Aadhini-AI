package ai.aadhini.android.ai

import ai.aadhini.core.ai.AIProvider

class AIProviderManager(
    private val providers: Map<String, AIProvider>,
    private var activeProvider: String
) {

    fun process(input: String): String {
        val provider = providers[activeProvider]
            ?: return "Provider unavailable: $activeProvider"

        return provider.process(input)
    }

    fun setProvider(name: String): Boolean {
        if (!providers.containsKey(name)) {
            return false
        }

        activeProvider = name
        return true
    }

    fun getActiveProvider(): String {
        return activeProvider
    }

    fun getAvailableProviders(): List<String> {
        return providers.keys.toList()
    }
}
