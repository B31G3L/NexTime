package todo.beigelwick.de.todolist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import todo.beigelwick.de.todolist.data.database.CountdownDatabase

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val countdownId = intent.getLongExtra("countdown_id", -1L)
        val isAtTime = intent.getBooleanExtra("is_at_time", false)

        if (countdownId == -1L) return

        // Countdown aus Datenbank laden
        CoroutineScope(Dispatchers.IO).launch {
            val database = CountdownDatabase.Companion.getDatabase(context)
            val countdown = database.countdownDao().getCountdownById(countdownId)

            countdown?.let {
                // Notification anzeigen
                CountdownNotificationManager.showCountdownNotification(
                    context,
                    it,
                    isAtTime
                )
            }
        }
    }
}