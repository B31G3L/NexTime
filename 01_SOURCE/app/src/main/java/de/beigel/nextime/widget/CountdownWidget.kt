package de.beigel.nextime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import de.beigel.nextime.MainActivity
import de.beigel.nextime.R
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class CountdownWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("CountdownWidget", "onUpdate called for ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("CountdownWidget", "Widget enabled")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            CountdownWidgetConfigActivity.deleteCountdownPref(context, appWidgetId)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d("CountdownWidget", "Widget disabled")
    }

    companion object {
        private const val TAG = "CountdownWidget"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val views = RemoteViews(context.packageName, R.layout.widget_countdown)

                    // Standard-Werte setzen (falls keine Daten)
                    views.setTextViewText(R.id.widget_title, "NexTime")
                    views.setTextViewText(R.id.widget_days, "--")
                    views.setTextViewText(R.id.widget_hours, "--")
                    views.setTextViewText(R.id.widget_minutes, "--")
                    views.setTextViewText(R.id.widget_date, "Lade...")

                    // Countdown laden
                    val countdown = withContext(Dispatchers.IO) {
                        loadCountdown(context, appWidgetId)
                    }

                    if (countdown != null) {
                        updateWidgetViews(views, countdown)
                    }

                    // Click-Intent zum Öffnen der App
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Log.d(TAG, "Widget updated successfully")

                } catch (e: Exception) {
                    Log.e(TAG, "Error updating widget", e)
                }
            }
        }

        private suspend fun loadCountdown(context: Context, appWidgetId: Int): Countdown? {
            return try {
                val database = CountdownDatabase.getDatabase(context)
                val countdowns = database.countdownDao().getAllCountdowns().first()

                val savedCountdownId = CountdownWidgetConfigActivity.getCountdownId(context, appWidgetId)

                if (savedCountdownId != -1L) {
                    val selectedCountdown = countdowns.firstOrNull { it.id == savedCountdownId }
                    if (selectedCountdown != null) {
                        return selectedCountdown
                    }
                }

                // Fallback: Hole den ersten aktiven Countdown
                countdowns.firstOrNull {
                    !it.calculateTimeRemaining().isPast
                } ?: countdowns.firstOrNull()

            } catch (e: Exception) {
                Log.e(TAG, "Error loading countdown", e)
                null
            }
        }

        private fun updateWidgetViews(
            views: RemoteViews,
            countdown: Countdown
        ) {
            try {
                val timeInfo = countdown.calculateTimeRemaining()

                // Titel
                views.setTextViewText(R.id.widget_title, countdown.title)

                // Count-up Badge anzeigen wenn in der Vergangenheit
                if (timeInfo.isPast) {
                    views.setViewVisibility(R.id.widget_count_up_badge, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_count_up_badge, View.GONE)
                }

                // Countdown-Werte
                views.setTextViewText(R.id.widget_days, "${timeInfo.days}")
                views.setTextViewText(R.id.widget_hours, String.format("%02d", timeInfo.hours))
                views.setTextViewText(R.id.widget_minutes, String.format("%02d", timeInfo.minutes))

                // Labels anpassen (Singular/Plural)
                views.setTextViewText(
                    R.id.widget_days_label,
                    if (timeInfo.days == 1L) "Tag" else "Tage"
                )
                views.setTextViewText(
                    R.id.widget_hours_label,
                    if (timeInfo.hours == 1L) "Stunde" else "Stunden"
                )
                views.setTextViewText(
                    R.id.widget_minutes_label,
                    if (timeInfo.minutes == 1L) "Minute" else "Minuten"
                )

                // Datum
                val dateText = countdown.targetDateTime.format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                )
                views.setTextViewText(R.id.widget_date, dateText)

                // Zeit anzeigen wenn includeTime gesetzt ist
                if (countdown.includeTime) {
                    val timeText = countdown.targetDateTime.format(
                        DateTimeFormatter.ofPattern("HH:mm")
                    )
                    views.setTextViewText(R.id.widget_time, timeText)
                    views.setViewVisibility(R.id.widget_time_icon, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_time, View.VISIBLE)

                    // Bei großen Widgets könnten wir auch Sekunden anzeigen
                    // views.setViewVisibility(R.id.widget_seconds_container, View.VISIBLE)
                    // views.setTextViewText(R.id.widget_seconds, String.format("%02d", timeInfo.seconds))
                } else {
                    views.setViewVisibility(R.id.widget_time_icon, View.GONE)
                    views.setViewVisibility(R.id.widget_time, View.GONE)
                    views.setViewVisibility(R.id.widget_seconds_container, View.GONE)
                }

                // Farbe setzen
                try {
                    val color = android.graphics.Color.parseColor(countdown.color)
                    views.setInt(R.id.widget_color_bar, "setBackgroundColor", color)

                    // Hauptzahl (Tage) in der Countdown-Farbe
                    views.setTextColor(R.id.widget_days, color)

                    // Stunden etwas transparenter
                    views.setTextColor(R.id.widget_hours,
                        android.graphics.Color.argb(217, // 85% alpha
                            android.graphics.Color.red(color),
                            android.graphics.Color.green(color),
                            android.graphics.Color.blue(color)
                        )
                    )

                    // Minuten noch transparenter
                    views.setTextColor(R.id.widget_minutes,
                        android.graphics.Color.argb(178, // 70% alpha
                            android.graphics.Color.red(color),
                            android.graphics.Color.green(color),
                            android.graphics.Color.blue(color)
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing color", e)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget views", e)
            }
        }

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, CountdownWidget::class.java)
                )
                Log.d(TAG, "Updating ${appWidgetIds.size} widgets")
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating all widgets", e)
            }
        }
    }
}