package todo.beigelwick.de.todolist.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        CoroutineScope(Dispatchers.IO).launch {
            CountdownWidget().updateAll(context)
        }
        // Nächsten Alarm für die nächste Minute planen
        scheduleMinutelyWidgetUpdate(context)
    }
}

fun scheduleMinutelyWidgetUpdate(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, WidgetAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val nextMinute = System.currentTimeMillis() + 60_000L
    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, nextMinute, pendingIntent)
}