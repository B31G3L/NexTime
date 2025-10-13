package de.beigel.nextime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import de.beigel.nextime.MainActivity
import de.beigel.nextime.R
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

class CountdownWidgetReceiver : AppWidgetProvider() {

    companion object {
        private const val TAG = "CountdownWidget"
        const val ACTION_UPDATE_WIDGET = "de.beigel.nextime.ACTION_UPDATE_WIDGET"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            Log.d(TAG, "=== UPDATE WIDGET START ===")
            Log.d(TAG, "Widget ID: $appWidgetId")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 1. Prüfe SharedPreferences
                    val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
                    val allPrefs = prefs.all
                    Log.d(TAG, "All SharedPreferences: $allPrefs")

                    val countdownId = prefs.getLong("widget_${appWidgetId}_countdown_id", -1L)
                    Log.d(TAG, "Countdown ID from prefs: $countdownId")

                    if (countdownId == -1L) {
                        Log.e(TAG, "No countdown ID found in preferences!")
                        withContext(Dispatchers.Main) {
                            showErrorWidget(context, appWidgetManager, appWidgetId, "Keine Konfiguration gefunden")
                        }
                        return@launch
                    }

                    // 2. Versuche Countdown zu laden
                    Log.d(TAG, "Loading countdown from database...")
                    val database = CountdownDatabase.getDatabase(context)
                    val countdown = database.countdownDao().getCountdownById(countdownId)

                    if (countdown == null) {
                        Log.e(TAG, "Countdown $countdownId not found in database!")
                        withContext(Dispatchers.Main) {
                            showErrorWidget(context, appWidgetManager, appWidgetId, "Countdown nicht gefunden")
                        }
                        return@launch
                    }

                    Log.d(TAG, "Countdown loaded: ${countdown.title}")

                    // 3. Erstelle Widget-View
                    Log.d(TAG, "Creating widget view...")
                    val views = createSimpleWidget(context, countdown, appWidgetId)

                    // 4. Update Widget
                    withContext(Dispatchers.Main) {
                        Log.d(TAG, "Updating widget on main thread...")
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                        Log.d(TAG, "Widget updated successfully!")

                        // Toast zur Bestätigung
                        Toast.makeText(context, "Widget aktualisiert: ${countdown.title}", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "ERROR updating widget", e)
                    Log.e(TAG, "Error message: ${e.message}")
                    Log.e(TAG, "Error stacktrace:", e)

                    withContext(Dispatchers.Main) {
                        showErrorWidget(context, appWidgetManager, appWidgetId, e.message ?: "Unbekannter Fehler")
                    }
                }
            }

            Log.d(TAG, "=== UPDATE WIDGET END ===")
        }

        private fun createSimpleWidget(
            context: Context,
            countdown: Countdown,
            appWidgetId: Int
        ): RemoteViews {
            Log.d(TAG, "Creating simple widget view")

            val views = RemoteViews(context.packageName, R.layout.widget_small)
            val timeInfo = countdown.calculateTimeRemaining()

            try {
                // Farbe setzen
                try {
                    val color = android.graphics.Color.parseColor(countdown.color)
                    views.setInt(R.id.widget_color_bar, "setBackgroundColor", color)
                    Log.d(TAG, "Color set: ${countdown.color}")
                } catch (e: Exception) {
                    Log.w(TAG, "Error setting color: ${e.message}")
                }

                // Titel
                views.setTextViewText(R.id.widget_title, countdown.title)
                Log.d(TAG, "Title set: ${countdown.title}")

                // Countdown-Zahl
                val countdownText = "${timeInfo.days}"
                views.setTextViewText(R.id.widget_countdown, countdownText)
                Log.d(TAG, "Countdown set: $countdownText")

                // Subtitle
                val subtitle = if (timeInfo.days == 1L) "Tag" else "Tage"
                views.setTextViewText(R.id.widget_subtitle, subtitle)
                Log.d(TAG, "Subtitle set: $subtitle")

                // Datum
                val dateText = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                views.setTextViewText(R.id.widget_date, dateText)
                Log.d(TAG, "Date set: $dateText")

                // Click-Handler
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                Log.d(TAG, "Click handler set")

            } catch (e: Exception) {
                Log.e(TAG, "Error creating widget content", e)
                throw e
            }

            return views
        }

        private fun showErrorWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            errorMessage: String
        ) {
            Log.d(TAG, "Showing error widget: $errorMessage")

            val views = RemoteViews(context.packageName, R.layout.widget_small)

            try {
                views.setTextViewText(R.id.widget_title, "❌ Fehler")
                views.setTextViewText(R.id.widget_countdown, "!")
                views.setTextViewText(R.id.widget_subtitle, "Widget-Fehler")
                views.setTextViewText(R.id.widget_date, errorMessage.take(50))

                // Click zum Öffnen der Config
                val configIntent = Intent(context, CountdownWidgetConfigActivity::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    appWidgetId,
                    configIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)

                // Toast mit Fehler
                Toast.makeText(context, "Widget-Fehler: $errorMessage", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                Log.e(TAG, "Error creating error widget", e)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets: ${appWidgetIds.joinToString()}")
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Log.d(TAG, "onReceive: action=${intent.action}, extras=${intent.extras}")

        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, CountdownWidgetReceiver::class.java)
            )
            Log.d(TAG, "Manual update triggered for ${appWidgetIds.size} widgets")
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)

        Log.d(TAG, "onDeleted: ${appWidgetIds.joinToString()}")

        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        appWidgetIds.forEach { appWidgetId ->
            editor.remove("widget_${appWidgetId}_countdown_id")
            Log.d(TAG, "Deleted config for widget $appWidgetId")
        }

        editor.apply()
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d(TAG, "First widget added")

        try {
            WidgetUpdateWorker.scheduleWork(context)
            Log.d(TAG, "WorkManager scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling WorkManager", e)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Log.d(TAG, "Last widget removed")

        try {
            WidgetUpdateWorker.cancelWork(context)
            Log.d(TAG, "WorkManager cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling WorkManager", e)
        }
    }
}