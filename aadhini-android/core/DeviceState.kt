package ai.aadhini.app.core

/** Presentation-safe snapshot of a connected Aadhini environment. */
data class DeviceState(
    val device: DeviceId,
    val connected: Boolean = false,
    val lastUpdatedEpochMs: Long = 0L
)
