package ai.aadhini.app.ui.navigation

import ai.aadhini.app.ui.environment.DeviceId
import ai.aadhini.app.ui.environment.DeviceState

/** Presentation controller only. Business decisions remain outside the Android UI layer. */
class AadhiniUiController(initial: AadhiniUiState = AadhiniUiState()) {
    var state: AadhiniUiState = initial
        private set

    fun completeBoot() { state = state.copy(bootComplete = true, route = AppRoute.HOME) }
    fun openConversation() = navigate(AppRoute.CONVERSATION)
    fun openSettings() = navigate(AppRoute.SETTINGS)
    fun openDevice() = navigate(AppRoute.DEVICE)
    fun goHome() = navigate(AppRoute.HOME)
    fun navigate(route: AppRoute) { state = state.copy(route = route) }
    fun back() { if (state.route != AppRoute.HOME) state = state.copy(route = AppRoute.HOME) }
    fun selectDevice(device: DeviceId, connected: Boolean = false) {
        state = state.copy(route = AppRoute.HOME, device = DeviceState(device, connected))
    }
    fun setNotificationCount(count: Int) { state = state.copy(notificationCount = count.coerceAtLeast(0)) }
    fun setMuted(muted: Boolean) { state = state.copy(muted = muted) }
}
