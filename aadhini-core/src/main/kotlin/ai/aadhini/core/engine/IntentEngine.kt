package ai.aadhini.core.engine

import ai.aadhini.core.intent.Intent

interface IntentEngine {

    fun detect(input: String): Intent
}
