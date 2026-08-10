package ai.aadhini.core.engine

import ai.aadhini.core.intent.Intent

interface ConversationEngine {
    fun respond(input: String, intent: Intent): String?
}
