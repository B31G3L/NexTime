package com.beigel.nextime.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.beigel.nextime.data.model.getReminderOptionsList
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.ReminderOption
import todo.beigelwick.de.todolist.data.model.getReminderOptionsList
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationScheduler {

    fun scheduleNotifications(context: Context, countdown: com.beigel.nextime.data.model.Countdown) {
        if (!countdown.notificationEnabled) {
            cancelAllNotifications(context, countdown)
            return
        }

        // effectiveTarget: bei wiederkehrenden Einträgen das nächste Vorkommen
        val target = countdown.effectiveTarget
        if (target.isBefore(LocalDateTime.now())) return

        val reminderOptions = countdown.getReminderOptionsList()
        if (reminderOptions.isEmpty()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        reminderOptions.forEach { option ->
            scheduleNotification(context, countdown, target, option, alarmManager)
        }
    }

    private fun scheduleNotification(
        context        : Context,
        countdown      : com.beigel.nextime.data.model.Countdown,
        target         : LocalDateTime,
        reminderOption : com.beigel.nextime.data.model.ReminderOption,
        alarmManager   : AlarmManager
    ) {
        val notificationTime = if (reminderOption == _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.AT_TIME) {
            target
        } else {
            target.minusMinutes(reminderOption.minutes)
        }

        if (!notificationTime.isAfter(LocalDateTime.now())) return

        val triggerTime = notificationTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("countdown_id",    countdown.id)
            putExtra("countdown_title", countdown.title)
            putExtra("countdown_color", countdown.color)
            putExtra("is_at_time",      reminderOption == _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.AT_TIME)
        }

        val requestCode   = getRequestCode(countdown.id, reminderOption)
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
            // SCHEDULE_EXACT_ALARM nicht erteilt → still ignorieren
        }
    }

    fun cancelAllNotifications(context: Context, countdown: com.beigel.nextime.data.model.Countdown) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.values().forEach { option ->
            val requestCode   = getRequestCode(countdown.id, option)
            val intent        = Intent(context, NotificationReceiver::class.java)
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

    private fun getRequestCode(countdownId: Long, reminderOption: com.beigel.nextime.data.model.ReminderOption): Int =
        (countdownId * 1000 + reminderOption.ordinal).toInt()
}