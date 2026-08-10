package ai.aadhini.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Aadhini Boot Receiver
 * Initializes Aadhini OS on device boot.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Future: Start Aadhini background service
        }
    }
}
