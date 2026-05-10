package todo.beigelwick.de.todolist.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import todo.beigelwick.de.todolist.MainActivity
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.calculateTimeRemaining

object CountdownNotificationManager {

    private const val CHANNEL_ID   = "countdown_notifications"
    private const val CHANNEL_NAME = "Countdown Erinnerungen"

    // ─── Notification Channel erstellen ──────────────────────────────────────

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                enableLights(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // ─── Notification anzeigen ────────────────────────────────────────────────

    fun showCountdownNotification(
        context    : Context,
        countdown  : Countdown,
        isAtTime   : Boolean = false
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            countdown.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isAtTime) "🎉 ${countdown.title}" else "⏰ ${context.getString(R.string.topbar_nextime)}: ${countdown.title}"
        val message = if (isAtTime) {
            context.getString(R.string.dialog_expired_banner)
        } else {
            val timeInfo = countdown.calculateTimeRemaining()
            when {
                timeInfo.days > 0 -> context.getString(R.string.share_days_remaining, timeInfo.days)
                else              -> context.getString(R.string.card_today_message)
            }
        }

        val color = try { Color.parseColor(countdown.color) }
        catch (e: Exception) { Color.parseColor("#FF7043") }

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

        with(NotificationManagerCompat.from(context)) {
            notify(countdown.id.toInt(), notification)
        }
    }

    // ─── Notification abbrechen ───────────────────────────────────────────────

    fun cancelNotification(context: Context, countdownId: Long) {
        NotificationManagerCompat.from(context).cancel(countdownId.toInt())
    }
}