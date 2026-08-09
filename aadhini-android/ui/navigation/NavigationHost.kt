package ai.aadhini.app.ui.navigation

/** Small UI navigation boundary used by the native presentation host. */
interface NavigationHost {
    fun navigate(route: AppRoute)
    fun back()
}
