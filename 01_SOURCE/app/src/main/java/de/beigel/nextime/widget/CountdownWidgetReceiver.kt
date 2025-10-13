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
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.calculateTimeRemaining
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class CountdownWidgetReceiver : AppWidgetProvider() {

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
                    updateWidgetWithPlaceholder(context, appWidgetManager, appWidgetId)
                    return@launch
                }

                val database = CountdownDatabase.getDatabase(context)
                val countdown = database.countdownDao().getCountdownById(countdownId)

                if (countdown == null) {
                    updateWidgetWithPlaceholder(context, appWidgetManager, appWidgetId)
                    return@launch
                }

                // Widget-Größe aus den Optionen ermitteln
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

                val views = when {
                    minWidth >= 250 && minHeight >= 180 -> createLargeWidget(context, countdown, appWidgetId)
                    minWidth >= 250 -> createMediumWidget(context, countdown, appWidgetId)
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
            views.setTextViewText(R.id.widget_title, "⏰ NexTime")
            views.setTextViewText(R.id.widget_countdown, "--")
            views.setTextViewText(R.id.widget_subtitle, "Nicht konfiguriert")
            views.setTextViewText(R.id.widget_date, "Tippen zum Einrichten")

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

            // Countdown - wie in der App formatiert
            val format = try {
                CountdownDisplayFormat.valueOf(countdown.displayFormat)
            } catch (e: Exception) {
                CountdownDisplayFormat.FULL_DETAILED
            }

            val countdownText = when (format) {
                CountdownDisplayFormat.FULL_DETAILED -> {
                    if (timeInfo.years > 0 || timeInfo.months > 0) {
                        "${timeInfo.days}"
                    } else {
                        String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds)
                    }
                }
                CountdownDisplayFormat.DAYS_ONLY -> "${timeInfo.days}"
                CountdownDisplayFormat.HOURS_MINUTES -> String.format("%d:%02d", timeInfo.hours, timeInfo.minutes)
                else -> "${timeInfo.days}"
            }
            views.setTextViewText(R.id.widget_countdown, countdownText)

            // Subtitle
            val subtitle = when (format) {
                CountdownDisplayFormat.FULL_DETAILED -> {
                    if (timeInfo.years > 0 || timeInfo.months > 0) {
                        if (timeInfo.days == 1L) "Tag" else "Tage"
                    } else {
                        if (countdown.includeTime) "verbleibend" else if (timeInfo.days == 1L) "Tag" else "Tage"
                    }
                }
                CountdownDisplayFormat.DAYS_ONLY -> if (timeInfo.days == 1L) "Tag" else "Tage"
                CountdownDisplayFormat.HOURS_MINUTES -> "Stunden"
                else -> if (timeInfo.days == 1L) "Tag" else "Tage"
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

            // Format ermitteln
            val format = try {
                CountdownDisplayFormat.valueOf(countdown.displayFormat)
            } catch (e: Exception) {
                CountdownDisplayFormat.FULL_DETAILED
            }

            // Countdown anzeigen - abhängig vom Format
            when (format) {
                CountdownDisplayFormat.FULL_DETAILED -> {
                    // Zeige Jahre/Monate/Tage und Zeit
                    views.setTextViewText(R.id.widget_days, "${timeInfo.days % 30}")
                    views.setTextViewText(R.id.widget_days_label, if (timeInfo.days % 30 == 1L) "Tag" else "Tage")

                    if (countdown.includeTime) {
                        views.setTextViewText(R.id.widget_hours, "${timeInfo.hours}")
                        views.setTextViewText(R.id.widget_minutes, String.format("%02d", timeInfo.minutes))
                        views.setTextViewText(R.id.widget_seconds, String.format("%02d", timeInfo.seconds))
                    }
                }
                CountdownDisplayFormat.DAYS_ONLY -> {
                    views.setTextViewText(R.id.widget_days, "${timeInfo.days}")
                    views.setTextViewText(R.id.widget_days_label, if (timeInfo.days == 1L) "Tag" else "Tage")
                    views.setTextViewText(R.id.widget_hours, "")
                    views.setTextViewText(R.id.widget_minutes, "")
                    views.setTextViewText(R.id.widget_seconds, "")
                }
                else -> {
                    views.setTextViewText(R.id.widget_days, "${timeInfo.days}")
                    views.setTextViewText(R.id.widget_days_label, if (timeInfo.days == 1L) "Tag" else "Tage")
                    if (countdown.includeTime) {
                        views.setTextViewText(R.id.widget_hours, "${timeInfo.hours}")
                        views.setTextViewText(R.id.widget_minutes, String.format("%02d", timeInfo.minutes))
                        views.setTextViewText(R.id.widget_seconds, String.format("%02d", timeInfo.seconds))
                    }
                }
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
            } else {
                views.setTextViewText(R.id.widget_time, "")
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
                if (timeInfo.isPast) "⏱️ Count-up" else "⏰ Countdown"
            )

            // Titel
            views.setTextViewText(R.id.widget_title, countdown.title)

            // Format ermitteln
            val format = try {
                CountdownDisplayFormat.valueOf(countdown.displayFormat)
            } catch (e: Exception) {
                CountdownDisplayFormat.FULL_DETAILED
            }

            // Countdown - detailliert wie in der App
            when (format) {
                CountdownDisplayFormat.FULL_DETAILED -> {
                    val remainingDays = timeInfo.days % 30
                    views.setTextViewText(R.id.widget_days_value, "$remainingDays")
                    views.setTextViewText(R.id.widget_days_label, if (remainingDays == 1L) "Tag" else "Tage")

                    if (countdown.includeTime) {
                        views.setTextViewText(R.id.widget_hours_value, String.format("%02d", timeInfo.hours))
                        views.setTextViewText(R.id.widget_minutes_value, String.format("%02d", timeInfo.minutes))
                        views.setTextViewText(R.id.widget_seconds_value, String.format("%02d", timeInfo.seconds))
                    }
                }
                CountdownDisplayFormat.DAYS_ONLY -> {
                    views.setTextViewText(R.id.widget_days_value, "${timeInfo.days}")
                    views.setTextViewText(R.id.widget_days_label, if (timeInfo.days == 1L) "Tag" else "Tage")
                    views.setTextViewText(R.id.widget_hours_value, "--")
                    views.setTextViewText(R.id.widget_minutes_value, "--")
                    views.setTextViewText(R.id.widget_seconds_value, "--")
                }
                else -> {
                    views.setTextViewText(R.id.widget_days_value, "${timeInfo.days}")
                    views.setTextViewText(R.id.widget_days_label, if (timeInfo.days == 1L) "Tag" else "Tage")

                    if (countdown.includeTime) {
                        views.setTextViewText(R.id.widget_hours_value, String.format("%02d", timeInfo.hours))
                        views.setTextViewText(R.id.widget_minutes_value, String.format("%02d", timeInfo.minutes))
                        views.setTextViewText(R.id.widget_seconds_value, String.format("%02d", timeInfo.seconds))
                    }
                }
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
            } else {
                views.setTextViewText(R.id.widget_time, "")
            }

            // Nächte
            if (countdown.showNights && timeInfo.nights > 0) {
                views.setTextViewText(
                    R.id.widget_nights,
                    "🌙 ${timeInfo.nights} ${if (timeInfo.nights == 1L) "Nacht" else "Nächte"}"
                )
            } else {
                views.setTextViewText(R.id.widget_nights, "")
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