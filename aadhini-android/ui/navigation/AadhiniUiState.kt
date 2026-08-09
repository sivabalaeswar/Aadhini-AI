package ai.aadhini.app.ui.navigation

import ai.aadhini.app.ui.environment.DeviceId
import ai.aadhini.app.ui.environment.DeviceState

data class AadhiniUiState(
    val route: AppRoute = AppRoute.HOME,
    val device: DeviceState = DeviceState(DeviceId.MOTO),
    val notificationCount: Int = 0,
    val muted: Boolean = false,
    val bootComplete: Boolean = false
)
