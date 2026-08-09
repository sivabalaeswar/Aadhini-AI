package ai.aadhini.app.ui.navigation

import ai.aadhini.app.ui.environment.DeviceId
import ai.aadhini.app.ui.environment.DeviceState

/** Presentation controller only. Business decisions remain outside the Android UI layer. */
class AadhiniUiController(initial: AadhiniUiState = AadhiniUiState()) {
    var state: AadhiniUiState = initial
        private set

    fun completeBoot() {
        state = state.copy(bootComplete = true, route = AppRoute.HOME)
    }

    fun openConversation() {
        state = state.copy(route = AppRoute.CONVERSATION)
    }

    fun openSettings() {
        state = state.copy(route = AppRoute.SETTINGS)
    }

    fun openDevice() {
        state = state.copy(route = AppRoute.DEVICE)
    }

    fun goHome() {
        state = state.copy(route = AppRoute.HOME)
    }

    fun selectDevice(device: DeviceId, connected: Boolean = false) {
        state = state.copy(
            route = AppRoute.HOME,
            device = DeviceState(device, connected)
        )
    }

    fun setNotificationCount(count: Int) {
        state = state.copy(notificationCount = count.coerceAtLeast(0))
    }

    fun setMuted(muted: Boolean) {
        state = state.copy(muted = muted)
    }
}
