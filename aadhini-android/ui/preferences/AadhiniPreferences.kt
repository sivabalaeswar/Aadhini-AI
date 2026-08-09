package ai.aadhini.app.ui.preferences

/** UI-level preferences. Persistent storage and policy belong to the platform/core layers. */
data class AadhiniPreferences(
    val avatarEnabled: Boolean = true,
    val voiceEnabled: Boolean = true,
    val showNotificationBadges: Boolean = true,
    val compactHome: Boolean = false
)
