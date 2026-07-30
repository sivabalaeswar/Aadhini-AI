package ai.aadhini.platform.scheduler

data class Scheduler(
    val id: SchedulerId,
    val type: SchedulerType,
    val properties: SchedulerProperties = emptyMap()
)