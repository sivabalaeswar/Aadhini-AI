package ai.aadhini.android.ai

import ai.aadhini.core.ai.AIProvider

class DemoProvider : AIProvider {

    override fun process(input: String): String {
        return when {
            input.contains("status", ignoreCase = true) ->
                "Aadhini Demo Mode\nSystem status: ONLINE\nProvider: Demo"

            input.contains("decision", ignoreCase = true) ->
                "Decision Engine Demo\nStatus: READY\nNo decision executed."

            input.contains("memory", ignoreCase = true) ->
                "Memory Engine Demo\nStatus: READY\nMemory store is available for integration."

            input.contains("agent", ignoreCase = true) ->
                "Agent System Demo\nActive agents: 0\nAgent framework is ready for integration."

            input.contains("hello", ignoreCase = true) ||
            input.contains("hi", ignoreCase = true) ->
                "Hello! 😊 Naan Aadhini. Demo mode-la running."

            input.contains("வணக்கம்") ->
                "வணக்கம் 😊 நான் ஆதினி. Demo mode-ல் இயங்குகிறேன்."

            input.isBlank() ->
                "Naan Aadhini. Enna help venum?"

            else ->
                "Demo Mode\nReceived: $input"
        }
    }
}
