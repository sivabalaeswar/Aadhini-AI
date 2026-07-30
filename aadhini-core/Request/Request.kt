package ai.aadhini.core.request

data class Request(
    val id: RequestId,
    val type: RequestType,
    val properties: RequestProperties = emptyMap()
)