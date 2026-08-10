package ai.aadhini.platform.task

data class Task(
    val id: TaskId,
    val type: TaskType,
    val properties: TaskProperties = emptyMap()
)