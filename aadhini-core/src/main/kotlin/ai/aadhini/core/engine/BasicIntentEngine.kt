package ai.aadhini.core.engine

import ai.aadhini.core.intent.Intent
import ai.aadhini.core.intent.IntentId
import ai.aadhini.core.intent.IntentType

class BasicIntentEngine : IntentEngine {

    override fun detect(input: String): Intent {
        val text = input.trim()

        val type = when {
            text.isEmpty() ->
                IntentType.UNKNOWN

            isConversation(text) ->
                IntentType.CONVERSATION

            isQuestion(text) ->
                IntentType.QUESTION

            isCommand(text) ->
                IntentType.COMMAND

            isRequest(text) ->
                IntentType.REQUEST

            else ->
                IntentType.UNKNOWN
        }

        return Intent(
            id = IntentId("intent-${System.currentTimeMillis()}"),
            type = type,
            properties = mapOf(
                "input" to text
            )
        )
    }

    private fun isConversation(text: String): Boolean {
        val lower = text.lowercase()

        return lower.startsWith("hello") ||
               lower.startsWith("hi") ||
               lower.startsWith("hey") ||
               text.startsWith("வணக்கம்")
    }

    private fun isQuestion(text: String): Boolean {
        val lower = text.lowercase()

        return text.endsWith("?") ||
               lower.startsWith("what ") ||
               lower.startsWith("why ") ||
               lower.startsWith("how ") ||
               lower.startsWith("when ") ||
               lower.startsWith("where ") ||
               lower.startsWith("who ") ||
               lower.startsWith("என்ன") ||
               lower.startsWith("எப்படி") ||
               lower.startsWith("ஏன்")
    }

    private fun isCommand(text: String): Boolean {
        val lower = text.lowercase()

        return lower.startsWith("open ") ||
               lower.startsWith("run ") ||
               lower.startsWith("start ") ||
               lower.startsWith("stop ") ||
               lower.startsWith("show ") ||
               lower.startsWith("check ") ||
               lower.startsWith("list ")
    }

    private fun isRequest(text: String): Boolean {
        val lower = text.lowercase()

        return lower.contains("remind me") ||
               lower.startsWith("please ") ||
               lower.startsWith("can you ") ||
               lower.startsWith("could you ") ||
               lower.startsWith("எனக்கு") ||
               lower.startsWith("நினைவூட்டு")
    }
}
