package ai.aadhini.core.entity

data class EntityProperties(
    val name: String = "",
    val description: String = "",
    val tags: List<String> = emptyList()
)
