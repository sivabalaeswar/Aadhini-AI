package ai.aadhini.core.decision

data class Decision(
    val id: DecisionId,
    val type: DecisionType,
    val properties: DecisionProperties = emptyMap()
)