package ai.aadhini.platform.infrastructure

data class Infrastructure(
    val id: InfrastructureId,
    val type: InfrastructureType,
    val properties: InfrastructureProperties = emptyMap()
)