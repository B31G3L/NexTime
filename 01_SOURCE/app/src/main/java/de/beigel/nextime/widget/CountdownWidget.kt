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

// ================== MITTLERE WIDGET (4x2) - DEFAULT ==================
class CountdownWidget : AppWidgetProvider() {

    private val TAG = "CountdownWidget_MEDIUM"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "=== onUpdate called for ${appWidgetIds.size} MEDIUM widgets ===")
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "onUpdate: Processing widget ID $appWidgetId")
            saveWidgetSizeMedium(context, appWidgetId)
            updateAppWidget(context, appWidgetManager, appWidgetId, R.layout.widget_countdown_medium)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "=== Medium widget enabled ===")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "=== Medium widget deleted ===")
        for (appWidgetId in appWidgetIds) {
            deleteCountdownIdForWidget(context, appWidgetId)
        }
    }

    private fun saveWidgetSizeMedium(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(
            "de.beigel.nextime.widget.CountdownWidget",
            Context.MODE_PRIVATE
        )
        prefs.edit().putInt("widget_size_$appWidgetId", 1).apply()
        Log.d(TAG, "saveWidgetSizeMedium: Widget $appWidgetId saved with size ordinal 1 (MEDIUM)")
    }

    private fun deleteCountdownIdForWidget(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(
            "de.beigel.nextime.widget.CountdownWidget",
            Context.MODE_PRIVATE
        )
        prefs.edit().remove("countdown_id_$appWidgetId").apply()
        Log.d(TAG, "deleteCountdownIdForWidget: Deleted countdown ID for widget $appWidgetId")
    }

    companion object {
        private const val TAG = "CountdownWidget"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            layoutResId: Int
        ) {
            Log.d(TAG, "updateAppWidget START - widgetId=$appWidgetId, layoutResId=$layoutResId")

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val views = RemoteViews(context.packageName, layoutResId)

                    views.setTextViewText(R.id.widget_title, "NexTime")
                    views.setTextViewText(R.id.widget_days, "--")
                    views.setTextViewText(R.id.widget_hours, "--")
                    views.setTextViewText(R.id.widget_minutes, "--")
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

                views.setTextViewText(R.id.widget_title, countdown.title)
                views.setTextViewText(R.id.widget_days, "${timeInfo.days}")
                views.setTextViewText(R.id.widget_hours, String.format("%02d", timeInfo.hours))
                views.setTextViewText(R.id.widget_minutes, String.format("%02d", timeInfo.minutes))

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

                if (layoutResId == R.layout.widget_countdown_large && countdown.includeTime) {
                    views.setViewVisibility(R.id.widget_seconds_container, View.VISIBLE)
                    views.setTextViewText(R.id.widget_seconds, String.format("%02d", timeInfo.seconds))
                } else if (layoutResId == R.layout.widget_countdown_large) {
                    views.setViewVisibility(R.id.widget_seconds_container, View.GONE)
                }

                val dateText = countdown.targetDateTime.format(
                    java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                )
                views.setTextViewText(R.id.widget_date, " $dateText")

                if (countdown.includeTime) {
                    val timeText = countdown.targetDateTime.format(
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                    )
                    views.setTextViewText(R.id.widget_time_with_icon, " 🕐 $timeText Uhr")
                    views.setViewVisibility(R.id.widget_time_with_icon, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_time_with_icon, View.GONE)
                }

                try {
                    val color = android.graphics.Color.parseColor(countdown.color)

                    views.setInt(R.id.widget_color_bar_top, "setBackgroundColor", color)
                    views.setInt(R.id.widget_color_bar_bottom, "setBackgroundColor", color)

                    views.setTextColor(R.id.widget_days, color)

                    views.setTextColor(R.id.widget_hours,
                        android.graphics.Color.argb(217,
                            android.graphics.Color.red(color),
                            android.graphics.Color.green(color),
                            android.graphics.Color.blue(color)
                        )
                    )

                    views.setTextColor(R.id.widget_minutes,
                        android.graphics.Color.argb(178,
                            android.graphics.Color.red(color),
                            android.graphics.Color.green(color),
                            android.graphics.Color.blue(color)
                        )
                    )

                    if (layoutResId == R.layout.widget_countdown_large && countdown.includeTime) {
                        views.setTextColor(R.id.widget_seconds,
                            android.graphics.Color.argb(140,
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
                e.printStackTrace()
            }
        }

        fun updateAllWidgets(context: Context) {
            Log.d(TAG, "=== updateAllWidgets called ===")
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)

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
                Log.d(TAG, "updateWidgetsByProvider: $providerName - Found ${appWidgetIds.size} widgets")

                for (appWidgetId in appWidgetIds) {
                    val layoutResId = getLayoutForWidget(context, appWidgetId)
                    Log.d(TAG, "updateWidgetsByProvider: Widget $appWidgetId will use layout $layoutResId")
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
            val sizeOrdinal = prefs.getInt("widget_size_$appWidgetId", 1)

            Log.d(TAG, "getLayoutForWidget: Widget $appWidgetId has size ordinal $sizeOrdinal")

            val layout = when (sizeOrdinal) {
                0 -> {
                    Log.d(TAG, "getLayoutForWidget: Using SMALL layout")
                    R.layout.widget_countdown_small
                }
                1 -> {
                    Log.d(TAG, "getLayoutForWidget: Using MEDIUM layout")
                    R.layout.widget_countdown_medium
                }
                2 -> {
                    Log.d(TAG, "getLayoutForWidget: Using LARGE layout")
                    R.layout.widget_countdown_large
                }
                else -> {
                    Log.w(TAG, "getLayoutForWidget: Unknown size ordinal $sizeOrdinal, using MEDIUM")
                    R.layout.widget_countdown_medium
                }
            }

            return layout
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

// ================== KLEINE WIDGET (4x1) ==================
class CountdownWidgetSmall : AppWidgetProvider() {

    private val TAG = "CountdownWidget_SMALL"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "=== onUpdate called for ${appWidgetIds.size} SMALL widgets ===")
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "onUpdate: Processing widget ID $appWidgetId")
            saveWidgetSizeSmall(context, appWidgetId)
            CountdownWidget.updateAppWidget(context, appWidgetManager, appWidgetId, R.layout.widget_countdown_small)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "=== Small widget enabled ===")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "=== Small widget deleted ===")
        for (appWidgetId in appWidgetIds) {
            CountdownWidget.deleteCountdownIdForWidget(context, appWidgetId)
        }
    }

    private fun saveWidgetSizeSmall(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(
            "de.beigel.nextime.widget.CountdownWidget",
            Context.MODE_PRIVATE
        )
        prefs.edit().putInt("widget_size_$appWidgetId", 0).apply()
        Log.d(TAG, "saveWidgetSizeSmall: Widget $appWidgetId saved with size ordinal 0 (SMALL)")
    }
}

// ================== GROSSE WIDGET (4x3) ==================
class CountdownWidgetLarge : AppWidgetProvider() {

    private val TAG = "CountdownWidget_LARGE"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "=== onUpdate called for ${appWidgetIds.size} LARGE widgets ===")
        for (appWidgetId in appWidgetIds) {
            Log.d(TAG, "onUpdate: Processing widget ID $appWidgetId")
            saveWidgetSizeLarge(context, appWidgetId)
            CountdownWidget.updateAppWidget(context, appWidgetManager, appWidgetId, R.layout.widget_countdown_large)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "=== Large widget enabled ===")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Log.d(TAG, "=== Large widget deleted ===")
        for (appWidgetId in appWidgetIds) {
            CountdownWidget.deleteCountdownIdForWidget(context, appWidgetId)
        }
    }

    private fun saveWidgetSizeLarge(context: Context, appWidgetId: Int) {
        val prefs = context.getSharedPreferences(
            "de.beigel.nextime.widget.CountdownWidget",
            Context.MODE_PRIVATE
        )
        prefs.edit().putInt("widget_size_$appWidgetId", 2).apply()
        Log.d(TAG, "saveWidgetSizeLarge: Widget $appWidgetId saved with size ordinal 2 (LARGE)")
    }
}