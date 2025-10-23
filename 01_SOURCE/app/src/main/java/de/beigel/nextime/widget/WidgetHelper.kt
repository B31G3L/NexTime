package de.beigel.nextime.widget.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.getAppWidgetState
import de.beigel.nextime.MainActivity
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

object WidgetHelper {

    // DataStore Keys
    val COUNTDOWN_ID_KEY = longPreferencesKey("countdown_id")

    /**
     * Holt den Countdown für das Widget
     */
    suspend fun getCountdownForWidget(context: Context, glanceId: GlanceId): Countdown? {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = getAppWidgetState(context, glanceId)
                val countdownId = prefs[COUNTDOWN_ID_KEY] ?: return@withContext null

                val database = CountdownDatabase.getDatabase(context)
                database.countdownDao().getCountdownById(countdownId)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Holt alle Countdowns
     */
    suspend fun getAllCountdowns(context: Context): List<Countdown> {
        return withContext(Dispatchers.IO) {
            try {
                val database = CountdownDatabase.getDatabase(context)
                database.countdownDao().getAllCountdowns().first()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    /**
     * Parst einen Farbstring zu Int
     */
    fun parseColor(colorString: String): Int {
        return try {
            Color.parseColor(colorString)
        } catch (e: Exception) {
            Color.parseColor("#FF9800") // Default Orange
        }
    }

    /**
     * Gibt eine Action zurück, die die App öffnet
     */
    fun getAppOpenAction(context: Context, countdown: Countdown): Action {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("countdown_id", countdown.id)
        }
        return actionStartActivity(intent)
    }

    /**
     * Theme-abhängige Farben
     */
    fun getOnSurfaceColor(context: Context): Int {
        return if (isDarkTheme(context)) {
            Color.parseColor("#E0E3E3")
        } else {
            Color.parseColor("#191C1C")
        }
    }

    fun getOnSurfaceVariantColor(context: Context): Int {
        return if (isDarkTheme(context)) {
            Color.parseColor("#BFC9C6")
        } else {
            Color.parseColor("#3F4947")
        }
    }

    fun getSurfaceVariantColor(context: Context): Int {
        return if (isDarkTheme(context)) {
            Color.parseColor("#3F4947")
        } else {
            Color.parseColor("#F8F9FA")
        }
    }

    /**
     * Prüft ob Dark Theme aktiv ist
     */
    private fun isDarkTheme(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}
