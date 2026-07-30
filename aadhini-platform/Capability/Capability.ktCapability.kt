package ai.aadhini.platform.capability

data class Capability(
    val id: CapabilityId,
    val type: CapabilityType,
    val name: String,
    val properties: CapabilityProperties = emptyMap()
)
