package ai.aadhini.core.engine

import ai.aadhini.core.decision.Decision
import ai.aadhini.core.decision.DecisionId
import ai.aadhini.core.decision.DecisionType
import ai.aadhini.core.intent.Intent
import ai.aadhini.core.intent.IntentType

class BasicDecisionEngine : DecisionEngine {

    override fun decide(intent: Intent): Decision {
        val type = when (intent.type) {
            IntentType.COMMAND -> DecisionType.EXECUTE
            IntentType.QUESTION -> DecisionType.ASK
            IntentType.REQUEST -> DecisionType.EXECUTE
            IntentType.CONVERSATION -> DecisionType.WAIT
            IntentType.RESPONSE -> DecisionType.WAIT
            IntentType.UNKNOWN -> DecisionType.UNKNOWN
        }

        return Decision(
            id = DecisionId("decision-${System.currentTimeMillis()}"),
            type = type,
            properties = mapOf(
                "intentId" to intent.id.value,
                "intentType" to intent.type.name
            )
        )
    }
}
