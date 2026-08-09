package ai.aadhini.app.ui.environment

data class DeviceState(
    val device: DeviceId,
    val connected: Boolean = false,
    val lastUpdatedEpochMs: Long = 0L
)
