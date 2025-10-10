package de.beigel.nextime.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import de.beigel.nextime.MainActivity
import de.beigel.nextime.R
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining

object CountdownNotificationManager {

    private const val CHANNEL_ID = "countdown_notifications"
    private const val CHANNEL_NAME = "Countdown Erinnerungen"
    private const val CHANNEL_DESCRIPTION = "Benachrichtigungen für deine Countdowns"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showCountdownNotification(
        context: Context,
        countdown: Countdown,
        isAtTime: Boolean = false
    ) {
        // Intent zum Öffnen der App
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            countdown.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Nachrichtentext
        val title = if (isAtTime) {
            "🎉 ${countdown.title}"
        } else {
            "⏰ Erinnerung: ${countdown.title}"
        }

        val message = if (isAtTime) {
            "Der Countdown ist abgelaufen! 🎊"
        } else {
            val timeInfo = countdown.calculateTimeRemaining()
            when {
                timeInfo.days > 0 -> "Noch ${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"}"
                timeInfo.hours > 0 -> "Noch ${timeInfo.hours} ${if (timeInfo.hours == 1L) "Stunde" else "Stunden"}"
                else -> "Noch ${timeInfo.minutes} ${if (timeInfo.minutes == 1L) "Minute" else "Minuten"}"
            }
        }

        // Farbe aus Countdown
        val color = try {
            android.graphics.Color.parseColor(countdown.color)
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#FF7043")
        }

        // Notification erstellen
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setColor(color)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        // Notification anzeigen
        with(NotificationManagerCompat.from(context)) {
            notify(countdown.id.toInt(), notification)
        }
    }

    fun cancelNotification(context: Context, countdownId: Long) {
        with(NotificationManagerCompat.from(context)) {
            cancel(countdownId.toInt())
        }
    }
}