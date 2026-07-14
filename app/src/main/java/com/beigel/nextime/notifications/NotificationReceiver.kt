package com.beigel.nextime.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.beigel.nextime.data.database.CountdownDatabase

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val countdownId = intent.getLongExtra("countdown_id", -1L)
        val isAtTime    = intent.getBooleanExtra("is_at_time", false)

        if (countdownId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database  = CountdownDatabase.getDatabase(context)
                val countdown = database.countdownDao().getCountdownById(countdownId)
                countdown?.let {
                    CountdownNotificationManager.showCountdownNotification(context, it, isAtTime)

                    // Wiederkehrende Einträge: nächstes Vorkommen neu planen.
                    // effectiveTarget zeigt nach dem Auslösen bereits auf den
                    // nächsten Termin, daher genügt ein erneuter Plan-Aufruf.
                    if (isAtTime && it.isRecurring) {
                        NotificationScheduler.scheduleNotifications(context, it)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}