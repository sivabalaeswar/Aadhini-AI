package ai.aadhini.core.pattern

data class Pattern(
    val id: PatternId,
    val type: PatternType,
    val description: String,
    val properties: PatternProperties = emptyMap()
)