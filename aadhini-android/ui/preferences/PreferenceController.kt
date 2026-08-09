package ai.aadhini.app.ui.preferences

/** In-memory UI preference controller. Persistence will be supplied by Platform later. */
class PreferenceController(initial: AadhiniPreferences = AadhiniPreferences()) {
    var preferences: AadhiniPreferences = initial
        private set

    fun setAvatarEnabled(enabled: Boolean) {
        preferences = preferences.copy(avatarEnabled = enabled)
    }

    fun setVoiceEnabled(enabled: Boolean) {
        preferences = preferences.copy(voiceEnabled = enabled)
    }

    fun setNotificationBadgesEnabled(enabled: Boolean) {
        preferences = preferences.copy(showNotificationBadges = enabled)
    }

    fun setCompactHome(enabled: Boolean) {
        preferences = preferences.copy(compactHome = enabled)
    }
}
