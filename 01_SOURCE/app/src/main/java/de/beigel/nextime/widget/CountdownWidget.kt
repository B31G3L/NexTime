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

// ================== MITTLERE WIDGET (4x2) - DEFAULT ==================
class CountdownWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("CountdownWidget", "onUpdate called for ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, R.layout.widget_countdown_medium)
        }
    }

    companion object {
        private const val TAG = "CountdownWidget"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            layoutResId: Int
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val views = RemoteViews(context.packageName, layoutResId)

                    // Standard-Werte setzen
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
                        updateWidgetViews(views, countdown, layoutResId)
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
            countdown: Countdown,
            layoutResId: Int
        ) {
            try {
                val timeInfo = countdown.calculateTimeRemaining()

                // Titel
                views.setTextViewText(R.id.widget_title, countdown.title)

                // Anzeigeformat des Countdowns auslesen
                val displayFormat = try {
                    de.beigel.nextime.data.model.CountdownDisplayFormat.valueOf(countdown.displayFormat)
                } catch (e: Exception) {
                    de.beigel.nextime.data.model.CountdownDisplayFormat.FULL_DETAILED
                }

                // === SETZE SICHTBARKEIT UND INHALTE BASIEREND AUF COUNTDOWN-FORMAT ===

                // Standardmäßig zeige immer Tage, Stunden, Minuten
                views.setTextViewText(R.id.widget_days, "${timeInfo.days}")
                views.setTextViewText(R.id.widget_hours, String.format("%02d", timeInfo.hours))
                views.setTextViewText(R.id.widget_minutes, String.format("%02d", timeInfo.minutes))

                // Labels
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

                // Sekunden nur bei LARGE Widget und wenn Uhrzeit einbezogen
                if (layoutResId == R.layout.widget_countdown_large && countdown.includeTime) {
                    views.setViewVisibility(R.id.widget_seconds_container, View.VISIBLE)
                    views.setTextViewText(R.id.widget_seconds, String.format("%02d", timeInfo.seconds))
                } else if (layoutResId == R.layout.widget_countdown_large) {
                    views.setViewVisibility(R.id.widget_seconds_container, View.GONE)
                }

                // === DATUM ANZEIGEN ===
                val dateText = countdown.targetDateTime.format(
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                )
                views.setTextViewText(R.id.widget_date, " $dateText")

                // === UHRZEIT ANZEIGEN - NUR WENN COUNTDOWN includeTime=true UND Format nicht nur Tage ===
                if (countdown.includeTime) {
                    val timeText = countdown.targetDateTime.format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                    )
                    views.setTextViewText(R.id.widget_time_with_icon, " 🕐 $timeText Uhr")
                    views.setViewVisibility(R.id.widget_time_with_icon, View.VISIBLE)
                } else {
                    // Verstecke die Uhrzeit, wenn Countdown keine Zeit hat
                    views.setViewVisibility(R.id.widget_time_with_icon, View.GONE)
                }

                // === FARBEN SETZEN ===
                try {
                    val color = android.graphics.Color.parseColor(countdown.color)

                    // Beide Farbbalken (oben und unten)
                    views.setInt(R.id.widget_color_bar_top, "setBackgroundColor", color)
                    views.setInt(R.id.widget_color_bar_bottom, "setBackgroundColor", color)

                    // Tage-Zahl in der Countdown-Farbe
                    views.setTextColor(R.id.widget_days, color)

                    // Stunden mit Transparenz
                    views.setTextColor(R.id.widget_hours,
                        android.graphics.Color.argb(217, // 85% alpha
                            android.graphics.Color.red(color),
                            android.graphics.Color.green(color),
                            android.graphics.Color.blue(color)
                        )
                    )

                    // Minuten mit Transparenz
                    views.setTextColor(R.id.widget_minutes,
                        android.graphics.Color.argb(178, // 70% alpha
                            android.graphics.Color.red(color),
                            android.graphics.Color.green(color),
                            android.graphics.Color.blue(color)
                        )
                    )

                    // Sekunden mit Transparenz (wenn sichtbar)
                    if (layoutResId == R.layout.widget_countdown_large && countdown.includeTime) {
                        views.setTextColor(R.id.widget_seconds,
                            android.graphics.Color.argb(140, // 55% alpha
                                android.graphics.Color.red(color),
                                android.graphics.Color.green(color),
                                android.graphics.Color.blue(color)
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing color", e)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget views", e)
            }
        }

        // In der CountdownWidget.kt - updateAllWidgets() Methode ERSETZEN:

        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)

                // Aktualisiere alle drei Widget-Varianten
                updateWidgetsByProvider(context, appWidgetManager, "de.beigel.nextime.widget.CountdownWidgetSmall")
                updateWidgetsByProvider(context, appWidgetManager, "de.beigel.nextime.widget.CountdownWidget")
                updateWidgetsByProvider(context, appWidgetManager, "de.beigel.nextime.widget.CountdownWidgetLarge")

            } catch (e: Exception) {
                Log.e(TAG, "Error updating all widgets", e)
            }
        }

        private fun updateWidgetsByProvider(
            context: Context,
            appWidgetManager: AppWidgetManager,
            providerName: String
        ) {
            try {
                val componentName = ComponentName(context, providerName)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                Log.d(TAG, "Updating ${appWidgetIds.size} widgets from $providerName")

                for (appWidgetId in appWidgetIds) {
                    // Lade die gespeicherte Größe für dieses Widget
                    val layoutResId = getLayoutForWidget(context, appWidgetId)
                    updateAppWidget(context, appWidgetManager, appWidgetId, layoutResId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets from provider $providerName", e)
            }
        }

        private fun getLayoutForWidget(context: Context, appWidgetId: Int): Int {
            val prefs = context.getSharedPreferences(
                "de.beigel.nextime.widget.CountdownWidget",
                Context.MODE_PRIVATE
            )
            val sizeOrdinal = prefs.getInt("widget_size_$appWidgetId", 1)  // ❌ DEFAULT: 1 (MEDIUM)

            return when (sizeOrdinal) {
                0 -> R.layout.widget_countdown_small      // 4×1
                1 -> R.layout.widget_countdown_medium     // 4×2  ← DEFAULT
                2 -> R.layout.widget_countdown_large      // 4×3
                else -> R.layout.widget_countdown_medium  // ← Fallback auf MEDIUM
            }
        }
    }
}

// ================== KLEINE WIDGET (4x1) ==================
class CountdownWidgetSmall : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("CountdownWidgetSmall", "onUpdate called for ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            CountdownWidget.updateAppWidget(context, appWidgetManager, appWidgetId, R.layout.widget_countdown_small)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("CountdownWidgetSmall", "Small widget enabled")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            CountdownWidgetConfigActivity.deleteCountdownPref(context, appWidgetId)
        }
    }
}

// ================== GROSSE WIDGET (4x3) ==================
class CountdownWidgetLarge : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("CountdownWidgetLarge", "onUpdate called for ${appWidgetIds.size} widgets")
        for (appWidgetId in appWidgetIds) {
            CountdownWidget.updateAppWidget(context, appWidgetManager, appWidgetId, R.layout.widget_countdown_large)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("CountdownWidgetLarge", "Large widget enabled")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            CountdownWidgetConfigActivity.deleteCountdownPref(context, appWidgetId)
        }
    }
}