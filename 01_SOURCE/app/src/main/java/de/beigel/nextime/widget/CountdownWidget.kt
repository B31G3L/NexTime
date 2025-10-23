package de.beigel.nextime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
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
// Vollständig dynamisch - unterstützt alle Größen von 1×1 bis 4×4
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

        WidgetUpdateWorker.scheduleMidnightUpdate(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle
    ) {
        Log.d(TAG, "=== onAppWidgetOptionsChanged for widget $appWidgetId ===")
        updateAppWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
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

        /**
         * Widget-Größen für dynamische Anpassung
         */
        enum class WidgetSize(
            val minWidth: Int,
            val minHeight: Int,
            val name: String
        ) {
            ULTRA_COMPACT(70, 70, "1×1"),           // Nur Nummer
            MINI(150, 70, "2×1"),                    // Nummer + Label
            SMALL(150, 150, "2×2"),                  // Titel + Nummer + Datum
            MEDIUM_H(220, 150, "3×2"),              // Erweitert
            MEDIUM(290, 150, "4×2"),                // Groß
            LARGE(290, 220, "4×3"),                 // Sehr groß
            EXTRA_LARGE(290, 290, "4×4")            // Maximal
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            Log.d(TAG, "updateAppWidget START - widgetId=$appWidgetId")

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Bestimme Größe basierend auf Optionen
                    val size = getWidgetSize(appWidgetManager, appWidgetId)
                    Log.d(TAG, "updateAppWidget: Widget size = ${size.name} (${size.minWidth}×${size.minHeight}dp)")

                    val views = RemoteViews(context.packageName, R.layout.widget_countdown_universal)

                    // Alle Container zunächst verstecken
                    views.setViewVisibility(R.id.widget_ultracompact_container, View.GONE)
                    views.setViewVisibility(R.id.widget_compact_container, View.GONE)
                    views.setViewVisibility(R.id.widget_small_container, View.GONE)
                    views.setViewVisibility(R.id.widget_large_container, View.GONE)

                    // Lade Countdown
                    val countdown = withContext(Dispatchers.IO) {
                        loadCountdownForWidget(context, appWidgetId)
                    }

                    if (countdown != null) {
                        Log.d(TAG, "updateAppWidget: Found countdown: ${countdown.title}")
                        updateWidgetViews(context, views, countdown, size)
                    } else {
                        Log.w(TAG, "updateAppWidget: No countdown found for widget $appWidgetId")
                        showPlaceholder(views, size)
                    }

                    // Click-Listener
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
         * Bestimme die Widget-Größe basierend auf den App Widget Manager Optionen
         */
        private fun getWidgetSize(appWidgetManager: AppWidgetManager, appWidgetId: Int): WidgetSize {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

                    Log.d(TAG, "getWidgetSize: Widget size = ${minWidth}×${minHeight}dp")

                    // Bestimme beste passende Größe
                    when {
                        minWidth < 100 && minHeight < 100 -> {
                            Log.d(TAG, "getWidgetSize: ULTRA_COMPACT (1×1)")
                            WidgetSize.ULTRA_COMPACT
                        }
                        minWidth < 200 && minHeight < 100 -> {
                            Log.d(TAG, "getWidgetSize: MINI (2×1)")
                            WidgetSize.MINI
                        }
                        minWidth < 200 && minHeight < 200 -> {
                            Log.d(TAG, "getWidgetSize: SMALL (2×2)")
                            WidgetSize.SMALL
                        }
                        minWidth < 250 && minHeight < 200 -> {
                            Log.d(TAG, "getWidgetSize: MEDIUM_H (3×2)")
                            WidgetSize.MEDIUM_H
                        }
                        minWidth < 300 && minHeight < 200 -> {
                            Log.d(TAG, "getWidgetSize: MEDIUM (4×2)")
                            WidgetSize.MEDIUM
                        }
                        minWidth < 300 && minHeight < 250 -> {
                            Log.d(TAG, "getWidgetSize: LARGE (4×3)")
                            WidgetSize.LARGE
                        }
                        else -> {
                            Log.d(TAG, "getWidgetSize: EXTRA_LARGE (4×4)")
                            WidgetSize.EXTRA_LARGE
                        }
                    }
                } else {
                    // Fallback für ältere Android-Versionen
                    Log.d(TAG, "getWidgetSize: Fallback to MEDIUM (older Android version)")
                    WidgetSize.MEDIUM
                }
            } catch (e: Exception) {
                Log.w(TAG, "getWidgetSize: Error, defaulting to MEDIUM", e)
                WidgetSize.MEDIUM
            }
        }

        private fun showPlaceholder(views: RemoteViews, size: WidgetSize) {
            when (size) {
                WidgetSize.ULTRA_COMPACT -> {
                    views.setViewVisibility(R.id.widget_ultracompact_container, View.VISIBLE)
                    views.setTextViewText(R.id.widget_days_ultracompact, "?")
                }
                WidgetSize.MINI -> {
                    views.setViewVisibility(R.id.widget_compact_container, View.VISIBLE)
                    views.setTextViewText(R.id.widget_days_compact, "?")
                }
                else -> {
                    views.setViewVisibility(R.id.widget_small_container, View.VISIBLE)
                    views.setTextViewText(R.id.widget_title_small, "Kein Countdown")
                    views.setTextViewText(R.id.widget_days_small, "?")
                }
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
                    }
                }

                val fallback = allCountdowns.firstOrNull { !it.calculateTimeRemaining().isPast }
                    ?: allCountdowns.firstOrNull()

                if (fallback != null) {
                    Log.d(TAG, "loadCountdownForWidget: Using fallback: ${fallback.title}")
                }

                fallback

            } catch (e: Exception) {
                Log.e(TAG, "Error loading countdown for widget $appWidgetId", e)
                null
            }
        }

        private fun updateWidgetViews(
            context: Context,
            views: RemoteViews,
            countdown: Countdown,
            size: WidgetSize
        ) {
            try {
                val timeInfo = countdown.calculateTimeRemaining()
                val color = try {
                    Color.parseColor(countdown.color)
                } catch (e: Exception) {
                    0xFFFF7043.toInt()
                }

                // Farbbalken setzen
                views.setInt(R.id.widget_color_bar_top, "setBackgroundColor", color)
                views.setInt(R.id.widget_color_bar_bottom, "setBackgroundColor", color)

                // Hintergrundfarbe mit 20% Opacity
                val alpha = (255 * 0.2).toInt()
                val backgroundColor = (alpha shl 24) or (color and 0x00FFFFFF)
                views.setInt(R.id.widget_container, "setBackgroundColor", backgroundColor)

                // Bestimme welcher Container sichtbar sein soll
                when (size) {
                    WidgetSize.ULTRA_COMPACT -> {
                        views.setViewVisibility(R.id.widget_ultracompact_container, View.VISIBLE)
                        views.setTextViewText(R.id.widget_days_ultracompact, "${timeInfo.days}")
                        views.setTextColor(R.id.widget_days_ultracompact, color)
                    }

                    WidgetSize.MINI -> {
                        views.setViewVisibility(R.id.widget_compact_container, View.VISIBLE)
                        views.setTextViewText(R.id.widget_days_compact, "${timeInfo.days}")
                        views.setTextViewText(R.id.widget_days_label_compact, 
                            if (timeInfo.days == 1L) "Tag" else "Tage")
                        views.setTextColor(R.id.widget_days_compact, color)
                    }

                    WidgetSize.SMALL -> {
                        views.setViewVisibility(R.id.widget_small_container, View.VISIBLE)
                        views.setTextViewText(R.id.widget_title_small, countdown.title)
                        views.setTextViewText(R.id.widget_days_small, "${timeInfo.days}")
                        views.setTextViewText(R.id.widget_days_label_small, 
                            if (timeInfo.days == 1L) "Tag" else "Tage")
                        views.setTextViewText(R.id.widget_date_small, 
                            "📅 ${countdown.targetDateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                        views.setTextColor(R.id.widget_days_small, color)
                    }

                    else -> {
                        // MEDIUM, MEDIUM_H, LARGE, EXTRA_LARGE
                        views.setViewVisibility(R.id.widget_large_container, View.VISIBLE)
                        views.setTextViewText(R.id.widget_title_large, countdown.title)
                        views.setTextViewText(R.id.widget_days_large, "${timeInfo.days}")
                        views.setTextViewText(R.id.widget_days_label_large, 
                            if (timeInfo.days == 1L) "Tag" else "Tage")
                        views.setTextViewText(R.id.widget_date_large, 
                            "📅 ${countdown.targetDateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                        views.setTextColor(R.id.widget_days_large, color)

                        // Zusätzliche Formate für größere Widgets
                        val format = try {
                            de.beigel.nextime.data.model.CountdownDisplayFormat.valueOf(countdown.displayFormat)
                        } catch (e: Exception) {
                            de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY
                        }

                        // Verstecke zunächst alle optionalen Container
                        views.setViewVisibility(R.id.widget_weeks_container, View.GONE)
                        views.setViewVisibility(R.id.widget_months_container, View.GONE)
                        views.setViewVisibility(R.id.widget_years_container, View.GONE)

                        when (format) {
                            de.beigel.nextime.data.model.CountdownDisplayFormat.WEEKS_DAYS -> {
                                views.setViewVisibility(R.id.widget_weeks_container, View.VISIBLE)
                                views.setTextViewText(R.id.widget_weeks_large, "${timeInfo.weeks}")
                                views.setTextViewText(R.id.widget_weeks_label_large, 
                                    if (timeInfo.weeks == 1L) "Woche" else "Wochen")
                                views.setTextColor(R.id.widget_weeks_large, color)
                            }

                            de.beigel.nextime.data.model.CountdownDisplayFormat.MONTHS_DAYS -> {
                                views.setViewVisibility(R.id.widget_months_container, View.VISIBLE)
                                views.setTextViewText(R.id.widget_months_large, "${timeInfo.months}")
                                views.setTextViewText(R.id.widget_months_label_large, 
                                    if (timeInfo.months == 1L) "Monat" else "Monate")
                                views.setTextColor(R.id.widget_months_large, color)
                            }

                            de.beigel.nextime.data.model.CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                                if (timeInfo.years > 0) {
                                    views.setViewVisibility(R.id.widget_years_container, View.VISIBLE)
                                    views.setTextViewText(R.id.widget_years_large, "${timeInfo.years}")
                                    views.setTextViewText(R.id.widget_years_label_large, 
                                        if (timeInfo.years == 1L) "Jahr" else "Jahre")
                                    views.setTextColor(R.id.widget_years_large, color)
                                }

                                val remainingMonths = timeInfo.months % 12
                                if (remainingMonths > 0) {
                                    views.setViewVisibility(R.id.widget_months_container, View.VISIBLE)
                                    views.setTextViewText(R.id.widget_months_large, "$remainingMonths")
                                    views.setTextViewText(R.id.widget_months_label_large, 
                                        if (remainingMonths == 1L) "Monat" else "Monate")
                                    views.setTextColor(R.id.widget_months_large, color)
                                }
                            }

                            else -> {} // DAYS_ONLY - alles beim Standard
                        }
                    }
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
