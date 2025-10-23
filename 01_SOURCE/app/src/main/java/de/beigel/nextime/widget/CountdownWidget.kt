package de.beigel.nextime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import java.time.LocalDateTime

// ================== UNIVERSAL COUNTDOWN WIDGET ==================
// Ein Widget das sich automatisch an alle Größen anpasst
// Ersetzt die 3 separaten Widget-Klassen
class CountdownWidget : AppWidgetProvider() {

    private val TAG = "CountdownWidget"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "=== onUpdate called for ${appWidgetIds.size} widgets ===")
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "onUpdate: Processing widget ID $appWidgetId")
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        // Plane nächsten Midnight-Update
        WidgetUpdateWorker.scheduleMidnightUpdate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "=== Widget enabled ===")
        WidgetUpdateWorker.scheduleMidnightUpdate(context)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "=== Widget deleted ===")
        for (appWidgetId in appWidgetIds) {
            deleteCountdownIdForWidget(context, appWidgetId)
        }
    }

    companion object {
        private const val TAG = "CountdownWidget"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            Log.d(TAG, "updateAppWidget START - widgetId=$appWidgetId")

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Stelle fest welches Layout basierend auf Größe
                    val layoutResId = getLayoutForWidgetSize(appWidgetManager, appWidgetId)
                    Log.d(TAG, "updateAppWidget: Using layout $layoutResId for widget size")

                    val views = RemoteViews(context.packageName, layoutResId)

                    views.setTextViewText(R.id.widget_title, "NexTime")
                    views.setTextViewText(R.id.widget_days, "--")
                    views.setTextViewText(R.id.widget_date, "Lade...")

                    val countdown = withContext(Dispatchers.IO) {
                        loadCountdownForWidget(context, appWidgetId)
                    }

                    if (countdown != null) {
                        Log.d(TAG, "updateAppWidget: Found countdown: ${countdown.title}")
                        updateWidgetViews(views, countdown, layoutResId)
                    } else {
                        Log.w(TAG, "updateAppWidget: No countdown found for widget $appWidgetId")
                        views.setTextViewText(R.id.widget_title, "Kein Countdown")
                        views.setTextViewText(R.id.widget_date, "Bitte einen auswählen")
                        // Setze Fallback-Farbe (dunkles Grau)
                        views.setInt(R.id.widget_container, "setBackgroundColor", 0xFF1E1E1E.toInt())
                    }

                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        appWidgetId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Log.d(TAG, "updateAppWidget DONE - Widget $appWidgetId updated")

                } catch (e: Exception) {
                    Log.e(TAG, "Error updating widget $appWidgetId", e)
                    e.printStackTrace()
                }
            }
        }

        /**
         * Bestimme das richtige Layout basierend auf der Widget-Größe
         * Das Widget passt sich automatisch an verfügbaren Platz an
         */
        private fun getLayoutForWidgetSize(appWidgetManager: AppWidgetManager, appWidgetId: Int): Int {
            return try {
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

                Log.d(TAG, "getLayoutForWidgetSize: Widget $appWidgetId min height = ${minHeight}dp")

                // Adaptive Layout-Auswahl basierend auf Höhe
                // Je nach verfügbarem Platz wird automatisch das beste Layout gewählt
                when {
                    minHeight < 100 -> {
                        Log.d(TAG, "getLayoutForWidgetSize: Ultra-kompakt - Using SMALL layout")
                        R.layout.widget_countdown_small
                    }
                    minHeight < 150 -> {
                        Log.d(TAG, "getLayoutForWidgetSize: Kompakt - Using SMALL layout")
                        R.layout.widget_countdown_small
                    }
                    minHeight < 200 -> {
                        Log.d(TAG, "getLayoutForWidgetSize: Mittel - Using MEDIUM layout")
                        R.layout.widget_countdown_medium
                    }
                    else -> {
                        Log.d(TAG, "getLayoutForWidgetSize: Groß - Using LARGE layout")
                        R.layout.widget_countdown_large
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "getLayoutForWidgetSize: Error, defaulting to MEDIUM", e)
                R.layout.widget_countdown_medium
            }
        }

        private suspend fun loadCountdownForWidget(context: Context, appWidgetId: Int): Countdown? {
            return try {
                Log.d(TAG, "loadCountdownForWidget: Looking for countdown for widget $appWidgetId")

                val database = CountdownDatabase.getDatabase(context)
                val allCountdowns = database.countdownDao().getAllCountdowns().first()
                Log.d(TAG, "loadCountdownForWidget: Total countdowns in DB: ${allCountdowns.size}")

                val savedCountdownId = getCountdownIdForWidget(context, appWidgetId)
                Log.d(TAG, "loadCountdownForWidget: Saved countdown ID for widget: $savedCountdownId")

                if (savedCountdownId != -1L) {
                    val selectedCountdown = allCountdowns.firstOrNull { it.id == savedCountdownId }
                    if (selectedCountdown != null) {
                        Log.d(TAG, "loadCountdownForWidget: Found saved countdown: ${selectedCountdown.title}")
                        return selectedCountdown
                    } else {
                        Log.w(TAG, "loadCountdownForWidget: Saved ID $savedCountdownId not found in DB")
                    }
                } else {
                    Log.d(TAG, "loadCountdownForWidget: No saved countdown ID, using fallback")
                }

                val fallback = allCountdowns.firstOrNull { !it.calculateTimeRemaining().isPast }
                    ?: allCountdowns.firstOrNull()

                if (fallback != null) {
                    Log.d(TAG, "loadCountdownForWidget: Using fallback: ${fallback.title}")
                }

                fallback

            } catch (e: Exception) {
                Log.e(TAG, "Error loading countdown for widget $appWidgetId", e)
                e.printStackTrace()
                null
            }
        }

        private fun updateWidgetViews(
            views: RemoteViews,
            countdown: Countdown,
            layoutResId: Int
        ) {
            try {
                Log.d(TAG, "updateWidgetViews: Updating with countdown: ${countdown.title}, format: ${countdown.displayFormat}")

                val timeInfo = countdown.calculateTimeRemaining()

                val format = try {
                    de.beigel.nextime.data.model.CountdownDisplayFormat.valueOf(countdown.displayFormat)
                } catch (e: Exception) {
                    de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY
                }

                views.setTextViewText(R.id.widget_title, countdown.title)

                // ======= PHASE 1: HINTERGRUNDFARBE SETZEN =======
                try {
                    val color = Color.parseColor(countdown.color)

                    // 20% Opacity für halbdurchsichtig - damit die Farbe nicht zu dominant wirkt
                    // Alternative zu ColorUtils.setAlphaComponent():
                    val alpha = (255 * 0.2).toInt()  // 20% = 51
                    val backgroundColor = (alpha shl 24) or (color and 0x00FFFFFF)

                    views.setInt(R.id.widget_container, "setBackgroundColor", backgroundColor)
                    Log.d(TAG, "updateWidgetViews: Set background color to ${countdown.color} with 20% opacity")

                    // Farbbalken oben/unten bleiben voll sichtbar
                    views.setInt(R.id.widget_color_bar_top, "setBackgroundColor", color)
                    views.setInt(R.id.widget_color_bar_bottom, "setBackgroundColor", color)
                    views.setTextColor(R.id.widget_days, color)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing color", e)
                }

                // Setze immer die Tage
                views.setTextViewText(R.id.widget_days, "${timeInfo.days}")
                views.setTextViewText(
                    R.id.widget_days_label,
                    if (timeInfo.days == 1L) "Tag" else "Tage"
                )

                // Verstecke alle optional Container zuerst
                if (layoutResId != R.layout.widget_countdown_small) {
                    views.setViewVisibility(R.id.widget_weeks_container, View.GONE)
                    views.setViewVisibility(R.id.widget_months_container, View.GONE)
                    views.setViewVisibility(R.id.widget_years_container, View.GONE)

                    // Zeige basierend auf Format
                    when (format) {
                        de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY -> {
                            // Nur Tage - Standard
                        }

                        de.beigel.nextime.data.model.CountdownDisplayFormat.WEEKS_DAYS -> {
                            views.setViewVisibility(R.id.widget_weeks_container, View.VISIBLE)
                            views.setTextViewText(R.id.widget_weeks, "${timeInfo.weeks}")
                            views.setTextViewText(
                                R.id.widget_weeks_label,
                                if (timeInfo.weeks == 1L) "Woche" else "Wochen"
                            )
                        }

                        de.beigel.nextime.data.model.CountdownDisplayFormat.MONTHS_DAYS -> {
                            views.setViewVisibility(R.id.widget_months_container, View.VISIBLE)
                            views.setTextViewText(R.id.widget_months, "${timeInfo.months}")
                            views.setTextViewText(
                                R.id.widget_months_label,
                                if (timeInfo.months == 1L) "Monat" else "Monate"
                            )
                        }

                        de.beigel.nextime.data.model.CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                            if (layoutResId == R.layout.widget_countdown_large) {
                                // Für großes Widget: zeige Jahre, Monate und Tage
                                if (timeInfo.years > 0) {
                                    views.setViewVisibility(R.id.widget_years_container, View.VISIBLE)
                                    views.setTextViewText(R.id.widget_years, "${timeInfo.years}")
                                    views.setTextViewText(
                                        R.id.widget_years_label,
                                        if (timeInfo.years == 1L) "Jahr" else "Jahre"
                                    )
                                }

                                val remainingMonths = timeInfo.months % 12
                                if (remainingMonths > 0) {
                                    views.setViewVisibility(R.id.widget_months_container, View.VISIBLE)
                                    views.setTextViewText(R.id.widget_months, "$remainingMonths")
                                    views.setTextViewText(
                                        R.id.widget_months_label,
                                        if (remainingMonths == 1L) "Monat" else "Monate"
                                    )
                                }
                            } else {
                                // Für kleinere Widgets: nur Monate anzeigen
                                views.setViewVisibility(R.id.widget_months_container, View.VISIBLE)
                                views.setTextViewText(R.id.widget_months, "${timeInfo.months}")
                                views.setTextViewText(
                                    R.id.widget_months_label,
                                    if (timeInfo.months == 1L) "Monat" else "Monate"
                                )
                            }
                        }
                    }

                    val dateText = countdown.targetDateTime.format(
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    )
                    views.setTextViewText(R.id.widget_date, " $dateText")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget views", e)
                e.printStackTrace()
            }
        }

        fun updateAllWidgets(context: Context) {
            Log.d(TAG, "=== updateAllWidgets called ===")
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, CountdownWidget::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

                Log.d(TAG, "updateAllWidgets: Found ${appWidgetIds.size} widgets")

                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error updating all widgets", e)
            }
        }

        fun getCountdownIdForWidget(context: Context, appWidgetId: Int): Long {
            val prefs = context.getSharedPreferences(
                "de.beigel.nextime.widget.CountdownWidget",
                Context.MODE_PRIVATE
            )
            val id = prefs.getLong("countdown_id_$appWidgetId", -1L)
            Log.d(TAG, "getCountdownIdForWidget: Widget $appWidgetId has countdown ID $id")
            return id
        }

        fun saveCountdownIdForWidget(context: Context, appWidgetId: Int, countdownId: Long) {
            val prefs = context.getSharedPreferences(
                "de.beigel.nextime.widget.CountdownWidget",
                Context.MODE_PRIVATE
            )
            prefs.edit().putLong("countdown_id_$appWidgetId", countdownId).apply()
            Log.d(TAG, "saveCountdownIdForWidget: Saved ID $countdownId for widget $appWidgetId")
        }

        fun deleteCountdownIdForWidget(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(
                "de.beigel.nextime.widget.CountdownWidget",
                Context.MODE_PRIVATE
            )
            prefs.edit().remove("countdown_id_$appWidgetId").apply()
            Log.d(TAG, "deleteCountdownIdForWidget: Deleted countdown ID for widget $appWidgetId")
        }
    }
}