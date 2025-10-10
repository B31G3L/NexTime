package de.beigel.nextime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import de.beigel.nextime.MainActivity
import de.beigel.nextime.R
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

open class CountdownWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, CountdownWidgetReceiver::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "de.beigel.nextime.ACTION_UPDATE_WIDGET"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                val countdownId = prefs.getLong("widget_${appWidgetId}_countdown_id", -1L)

                if (countdownId == -1L) {
                    // Kein Countdown ausgewählt - zeige Platzhalter
                    updateWidgetWithPlaceholder(context, appWidgetManager, appWidgetId)
                    return@launch
                }

                val database = CountdownDatabase.getDatabase(context)
                val countdown = database.countdownDao().getCountdownById(countdownId)

                if (countdown == null) {
                    updateWidgetWithPlaceholder(context, appWidgetManager, appWidgetId)
                    return@launch
                }

                val widgetSize = prefs.getString("widget_${appWidgetId}_size", "SMALL") ?: "SMALL"

                val views = when (widgetSize) {
                    "SMALL" -> createSmallWidget(context, countdown, appWidgetId)
                    "MEDIUM" -> createMediumWidget(context, countdown, appWidgetId)
                    "LARGE" -> createLargeWidget(context, countdown, appWidgetId)
                    else -> createSmallWidget(context, countdown, appWidgetId)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun updateWidgetWithPlaceholder(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_small)
            views.setTextViewText(R.id.widget_title, "NexTime")
            views.setTextViewText(R.id.widget_countdown, "Kein Countdown")
            views.setTextViewText(R.id.widget_subtitle, "Tippen zum Einrichten")

            // Click zum Öffnen der Config
            val configIntent = Intent(context, CountdownWidgetConfigActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun createSmallWidget(
            context: Context,
            countdown: Countdown,
            appWidgetId: Int
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_small)
            val timeInfo = countdown.calculateTimeRemaining()

            // Farbe setzen
            try {
                val color = android.graphics.Color.parseColor(countdown.color)
                views.setInt(R.id.widget_color_bar, "setBackgroundColor", color)
            } catch (e: Exception) {
                // Fallback auf Orange
            }

            // Titel
            views.setTextViewText(R.id.widget_title, countdown.title)

            // Countdown
            val countdownText = if (timeInfo.days > 0 || !countdown.includeTime) {
                "${timeInfo.days}"
            } else {
                String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds)
            }
            views.setTextViewText(R.id.widget_countdown, countdownText)

            // Subtitle
            val subtitle = if (timeInfo.days > 0 || !countdown.includeTime) {
                if (timeInfo.days == 1L) "Tag" else "Tage"
            } else {
                "verbleibend"
            }
            views.setTextViewText(R.id.widget_subtitle, subtitle)

            // Datum
            views.setTextViewText(
                R.id.widget_date,
                countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            )

            // Click zum Öffnen der App
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            return views
        }

        private fun createMediumWidget(
            context: Context,
            countdown: Countdown,
            appWidgetId: Int
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_medium)
            val timeInfo = countdown.calculateTimeRemaining()

            // Farbe setzen
            try {
                val color = android.graphics.Color.parseColor(countdown.color)
                views.setInt(R.id.widget_color_bar, "setBackgroundColor", color)
                views.setInt(R.id.widget_background, "setBackgroundColor",
                    android.graphics.Color.argb(30,
                        android.graphics.Color.red(color),
                        android.graphics.Color.green(color),
                        android.graphics.Color.blue(color)
                    )
                )
            } catch (e: Exception) {
                // Fallback
            }

            // Titel
            views.setTextViewText(R.id.widget_title, countdown.title)

            // Countdown - aufgeteilt
            views.setTextViewText(R.id.widget_days, "${timeInfo.days}")
            views.setTextViewText(R.id.widget_days_label, if (timeInfo.days == 1L) "Tag" else "Tage")

            if (countdown.includeTime) {
                views.setTextViewText(R.id.widget_hours, "${timeInfo.hours}")
                views.setTextViewText(R.id.widget_minutes, "${timeInfo.minutes}")
                views.setTextViewText(R.id.widget_seconds, "${timeInfo.seconds}")
            } else {
                views.setTextViewText(R.id.widget_hours, "")
                views.setTextViewText(R.id.widget_minutes, "")
                views.setTextViewText(R.id.widget_seconds, "")
            }

            // Datum und Uhrzeit
            views.setTextViewText(
                R.id.widget_date,
                countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy"))
            )

            if (countdown.includeTime) {
                views.setTextViewText(
                    R.id.widget_time,
                    countdown.targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm 'Uhr'"))
                )
            }

            // Click zum Öffnen der App
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            return views
        }

        private fun createLargeWidget(
            context: Context,
            countdown: Countdown,
            appWidgetId: Int
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_large)
            val timeInfo = countdown.calculateTimeRemaining()

            // Farbe setzen
            try {
                val color = android.graphics.Color.parseColor(countdown.color)
                views.setInt(R.id.widget_accent_bar, "setBackgroundColor", color)
            } catch (e: Exception) {
                // Fallback
            }

            // Status
            views.setTextViewText(
                R.id.widget_status,
                if (timeInfo.isPast) "Vergangen" else "Bevorstehend"
            )

            // Titel
            views.setTextViewText(R.id.widget_title, countdown.title)

            // Countdown - detailliert
            views.setTextViewText(R.id.widget_days_value, "${timeInfo.days}")
            views.setTextViewText(R.id.widget_days_label, if (timeInfo.days == 1L) "Tag" else "Tage")

            if (countdown.includeTime) {
                views.setTextViewText(R.id.widget_hours_value, String.format("%02d", timeInfo.hours))
                views.setTextViewText(R.id.widget_minutes_value, String.format("%02d", timeInfo.minutes))
                views.setTextViewText(R.id.widget_seconds_value, String.format("%02d", timeInfo.seconds))
            } else {
                views.setTextViewText(R.id.widget_hours_value, "--")
                views.setTextViewText(R.id.widget_minutes_value, "--")
                views.setTextViewText(R.id.widget_seconds_value, "--")
            }

            // Datum
            views.setTextViewText(
                R.id.widget_date,
                "📅 " + countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy"))
            )

            // Uhrzeit
            if (countdown.includeTime) {
                views.setTextViewText(
                    R.id.widget_time,
                    "🕐 " + countdown.targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm 'Uhr'"))
                )
            }

            // Nächte
            if (countdown.showNights && timeInfo.nights > 0) {
                views.setTextViewText(
                    R.id.widget_nights,
                    "🌙 ${timeInfo.nights} ${if (timeInfo.nights == 1L) "Nacht" else "Nächte"}"
                )
            }

            // Click zum Öffnen der App
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            return views
        }
    }
}