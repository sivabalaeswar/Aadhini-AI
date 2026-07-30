package ai.aadhini.core.learning

data class Learning(
    val id: LearningId,
    val type: LearningType,
    val properties: LearningProperties = emptyMap()
)