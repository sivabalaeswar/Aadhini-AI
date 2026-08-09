package ai.aadhini.app.ui.notification

/** Presentation-only notification store. External notification ingestion belongs to Platform. */
class NotificationCenter {
    private val items = mutableListOf<NotificationItem>()

    fun add(item: NotificationItem) {
        items.removeAll { it.id == item.id }
        items.add(0, item)
    }

    fun markRead(id: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index >= 0) items[index] = items[index].copy(read = true)
    }

    fun unreadCount(): Int = items.count { !it.read }

    fun snapshot(): List<NotificationItem> = items.toList()
}
