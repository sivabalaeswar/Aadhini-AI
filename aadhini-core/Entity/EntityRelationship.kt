package ai.aadhini.core.entity

data class EntityRelationship(
    val target: EntityId,
    val type: EntityRelationshipType
)