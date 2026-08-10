package ai.aadhini.core.engine

import ai.aadhini.core.decision.Decision
import ai.aadhini.core.intent.Intent

interface DecisionEngine {

    fun decide(intent: Intent): Decision
}
