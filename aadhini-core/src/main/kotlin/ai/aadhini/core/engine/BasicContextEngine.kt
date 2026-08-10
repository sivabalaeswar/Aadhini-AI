package ai.aadhini.core.engine

import ai.aadhini.core.context.Context
import ai.aadhini.core.context.ContextId
import ai.aadhini.core.context.ContextType
import ai.aadhini.core.intent.Intent

class BasicContextEngine : ContextEngine {

    private var currentContext: Context? = null

    fun update(input: String, intent: Intent): Context {
        val contextType = when (intent.type.name) {
            "CONVERSATION" -> ContextType.CONVERSATION
            "COMMAND", "REQUEST" -> ContextType.TASK
            else -> ContextType.USER
        }

        val context = Context(
            id = ContextId("context-${System.currentTimeMillis()}"),
            type = contextType,
            properties = mapOf(
                "input" to input,
                "intent" to intent.type.name
            )
        )

        currentContext = context
        return context
    }

    fun current(): Context? = currentContext

    fun clear() {
        currentContext = null
    }
}
