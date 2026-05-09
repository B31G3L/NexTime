package todo.beigelwick.de.todolist.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.ReminderOption
import todo.beigelwick.de.todolist.data.model.getReminderOptionsList
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationScheduler {

    fun scheduleNotifications(context: Context, countdown: Countdown) {
        if (!countdown.notificationEnabled) {
            cancelAllNotifications(context, countdown)
            return
        }

        // Zieldatum bereits vergangen → keine Notifications einplanen
        if (countdown.targetDateTime.isBefore(LocalDateTime.now())) {
            return
        }

        val reminderOptions = countdown.getReminderOptionsList()

        // Keine Optionen gewählt → nichts tun (nicht crashen)
        if (reminderOptions.isEmpty()) {
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        for (option in reminderOptions) {
            scheduleNotification(context, countdown, option, alarmManager)
        }
    }

    private fun scheduleNotification(
        context: Context,
        countdown: Countdown,
        reminderOption: ReminderOption,
        alarmManager: AlarmManager
    ) {
        val notificationTime = if (reminderOption == ReminderOption.AT_TIME) {
            countdown.targetDateTime
        } else {
            countdown.targetDateTime.minusMinutes(reminderOption.minutes)
        }

        // Nur zukünftige Benachrichtigungen einplanen
        if (!notificationTime.isAfter(LocalDateTime.now())) {
            return
        }

        val triggerTime = notificationTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("countdown_id", countdown.id)
            putExtra("countdown_title", countdown.title)
            putExtra("countdown_color", countdown.color)
            putExtra("is_at_time", reminderOption == ReminderOption.AT_TIME)
        }

        val requestCode = getRequestCode(countdown.id, reminderOption)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // SCHEDULE_EXACT_ALARM nicht erteilt → still ignorieren,
            // der Nutzer wurde in MainActivity bereits darauf hingewiesen
        }
    }

    fun cancelAllNotifications(context: Context, countdown: Countdown) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        ReminderOption.values().forEach { option ->
            val requestCode = getRequestCode(countdown.id, option)
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    private fun getRequestCode(countdownId: Long, reminderOption: ReminderOption): Int {
        return (countdownId * 1000 + reminderOption.ordinal).toInt()
    }
}