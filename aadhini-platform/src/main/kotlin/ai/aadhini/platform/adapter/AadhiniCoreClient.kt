package ai.aadhini.platform.adapter

import ai.aadhini.core.ai.AIProvider
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Android-to-Termux bridge for the standalone Aadhini Core Runtime.
 *
 * This class belongs to Platform: Android UI must not call Gemini directly.
 * The Core Runtime owns prompts, conversation history, memory and model routing.
 */
class AadhiniCoreClient(
    private val baseUrl: String = "http://127.0.0.1:8766",
    private val connectTimeoutMs: Int = 4_000,
    private val readTimeoutMs: Int = 30_000
) : AIProvider {

    override fun process(input: String): String {
        val response = post("/chat", JSONObject().put("message", input))
        val reply = response.optString("reply").trim()
        if (reply.isEmpty()) {
            throw IOException("Aadhini Core returned an empty reply.")
        }
        return reply
    }

    fun health(): JSONObject = get("/health")

    fun switchMode(mode: String): JSONObject =
        post("/mode/switch", JSONObject().put("mode", mode))

    fun remember(content: String, category: String = "Important", pinned: Boolean = false): JSONObject =
        post(
            "/memory/add",
            JSONObject()
                .put("content", content)
                .put("category", category)
                .put("pinned", pinned)
        )

    fun memories(): JSONObject = get("/memory/list")

    fun forget(memoryId: Long): JSONObject =
        post("/memory/forget", JSONObject().put("id", memoryId))

    fun pin(memoryId: Long, pinned: Boolean): JSONObject =
        post(
            "/memory/pin",
            JSONObject().put("id", memoryId).put("pinned", pinned)
        )

    private fun get(path: String): JSONObject {
        val connection = open(path, "GET")
        return readResponse(connection)
    }

    private fun post(path: String, payload: JSONObject): JSONObject {
        val connection = open(path, "POST")
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.outputStream.use { output ->
            output.write(payload.toString().toByteArray(Charsets.UTF_8))
        }
        return readResponse(connection)
    }

    private fun open(path: String, method: String): HttpURLConnection {
        val connection = (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection)
        connection.requestMethod = method
        connection.connectTimeout = connectTimeoutMs
        connection.readTimeout = readTimeoutMs
        connection.useCaches = false
        connection.setRequestProperty("Accept", "application/json")
        return connection
    }

    private fun readResponse(connection: HttpURLConnection): JSONObject {
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = if (body.isBlank()) JSONObject() else JSONObject(body)
            if (status !in 200..299) {
                throw IOException(json.optString("error", "Aadhini Core HTTP error: $status"))
            }
            return json
        } finally {
            connection.disconnect()
        }
    }
}
