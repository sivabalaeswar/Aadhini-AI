package ai.aadhini.app.ui.notification

enum class NotificationPriority {
    NORMAL,
    IMPORTANT,
    SENSITIVE
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val read: Boolean = false
)
