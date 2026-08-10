package ai.aadhini.core.engine

import ai.aadhini.core.intent.Intent
import ai.aadhini.core.intent.IntentType

class BasicConversationEngine : ConversationEngine {

    override fun respond(input: String, intent: Intent): String? {
        if (intent.type != IntentType.CONVERSATION) {
            return null
        }

        return when (input.trim().lowercase()) {
            "hello", "hi", "hey" -> "Hello, Chief. Aadhini is online."
            "good morning" -> "Good morning, Chief. Aadhini is ready."
            "good afternoon" -> "Good afternoon, Chief. Aadhini is ready."
            "good evening" -> "Good evening, Chief. Aadhini is ready."
            "good night" -> "Good night, Chief. Aadhini is standing by."
            else -> "I'm here, Chief. How can I help?"
        }
    }
}
