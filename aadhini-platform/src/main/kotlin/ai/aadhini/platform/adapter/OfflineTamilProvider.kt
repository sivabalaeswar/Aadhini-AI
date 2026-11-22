package ai.aadhini.platform.adapter

import ai.aadhini.core.ai.AIProvider
import java.util.Locale

/**
 * Zero-API-cost provider for the first Android milestone.
 *
 * This is an intentionally small offline command/conversation baseline, not
 * a claim of LLM-level reasoning. It keeps the app usable without network
 * access, API keys, subscriptions or a remote runtime.
 */
class OfflineTamilProvider : AIProvider {
    override fun process(input: String): String {
        val text = input.trim()
        if (text.isEmpty()) return "சொல்லுங்க. நான் கேட்கிறேன்."

        val normalized = text.lowercase(Locale.ROOT)
        return when {
            normalized in setOf("hi", "hello", "வணக்கம்", "ஹாய்", "ஹலோ") ||
                normalized.contains("vanakkam") ->
                "வணக்கம்! நான் ஆதினி. இன்று என்ன உதவி வேண்டும்?"
            normalized.contains("உன் பெயர்") || normalized.contains("who are you") ||
                normalized.contains("your name") ->
                "என் பெயர் ஆதினி. தமிழ் முதன்மையாகப் பேசும் உங்களுடைய தனிப்பட்ட உதவியாளர்."
            normalized.contains("நினைவில் வைத்துக்கொள்") ||
                normalized.contains("நினைவில் வைத்துக்கோ") ||
                normalized.contains("remember this") ->
                "சரி. இந்த உரையாடலில் இதை நினைவில் வைத்துக்கொள்கிறேன். நிரந்தர நினைவகத்தில் சேமிக்கப்பட்டதா என்பதை Memory பகுதியில் சரிபார்க்கவும்."
            normalized.contains("நன்றி") || normalized.contains("thanks") ->
                "சந்தோஷம்! இன்னும் ஏதாவது உதவி வேண்டுமா?"
            normalized.contains("நேரம்") || normalized.contains("time") ->
                "தற்போதைய நேரத்தைச் சரிபார்க்க இணைய அல்லது சாதன நேர அணுகல் தேவை. இந்த offline baseline இன்னும் அதை இணைக்கவில்லை."
            normalized.contains("உதவி") || normalized.contains("help") ->
                "நான் இப்போது offline அடிப்படை நிலையில் இயங்குகிறேன். உரையாடல், எளிய கட்டளைகள், குரல் உள்ளீடு மற்றும் சாதனத்தின் Text-to-Speech வசதிகளைப் பயன்படுத்தலாம்."
            else ->
                "நான் கேட்டுக்கொண்டிருக்கிறேன். இப்போது API இல்லாத offline அடிப்படை பதில் முறையில் இயங்குகிறேன்; சிக்கலான கேள்விகளுக்கு local language model இணைத்த பிறகு விரிவாக உதவ முடியும்.\n\nநீங்கள் சொன்னது: $text"
        }
    }
}
