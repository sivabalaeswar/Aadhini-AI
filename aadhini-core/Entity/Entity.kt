package ai.aadhini.core.entity

data class Entity(
    val id: EntityId,
    val type: EntityType,
    val name: String,
    val relationships: List<EntityRelationship> = emptyList(),
    val properties: EntityProperties = emptyMap()
)