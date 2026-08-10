package ai.aadhini.android.core

import ai.aadhini.android.BuildConfig

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray

class AadhiniCore {

    private val client = OkHttpClient()
    private val apiKey = BuildConfig.GEMINI_API_KEY

    fun process(input: String): String {
        return try {
            val json = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "You are Radhika, a personal AI companion. You speak in a caring, friendly way. Always respond in Tamil or English based on user input. User says: $input")
                            })
                        })
                    })
                })
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=$apiKey")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return "No response"
            val result = JSONObject(responseBody)

// Check candidates exist
if (!result.has("candidates")) {
    return "API Response: $responseBody"
}

result.getJSONArray("candidates")
    .getJSONObject(0)
    .getJSONObject("content")
    .getJSONArray("parts")
    .getJSONObject(0)
    .getString("text")
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
