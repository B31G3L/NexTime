package de.beigel.nextime.widget.utils

import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.util.Log
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import de.beigel.nextime.MainActivity
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import kotlinx.coroutines.flow.first

object WidgetHelper {

    private const val TAG = "WidgetHelper"
    val COUNTDOWN_ID_KEY = longPreferencesKey("countdown_id")

    suspend fun getCountdownForWidget(context: Context, glanceId: GlanceId): Countdown? {
        return try {
            Log.d(TAG, "Loading countdown for widget: $glanceId")

            // State aus Glance Preferences laden
            val prefs = getAppWidgetState(
                context = context,
                definition = PreferencesGlanceStateDefinition,
                glanceId = glanceId
            )
            val countdownId = prefs[COUNTDOWN_ID_KEY]

            if (countdownId == null) {
                Log.w(TAG, "No countdown ID found in widget state for $glanceId")
                return null
            }

            Log.d(TAG, "Found countdown ID: $countdownId")

            // Countdown aus Datenbank laden
            val database = CountdownDatabase.getDatabase(context)
            val countdown = database.countdownDao().getCountdownById(countdownId)

            if (countdown == null) {
                Log.w(TAG, "Countdown with ID $countdownId not found in database")
            } else {
                Log.d(TAG, "Successfully loaded countdown: ${countdown.title}")
            }

            countdown
        } catch (e: Exception) {
            Log.e(TAG, "Error loading countdown for widget", e)
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllCountdowns(context: Context): List<Countdown> {
        return try {
            val database = CountdownDatabase.getDatabase(context)
            database.countdownDao().getAllCountdowns().first()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading all countdowns", e)
            e.printStackTrace()
            emptyList()
        }
    }

    fun parseColor(colorString: String): Int {
        return try {
            Color.parseColor(colorString)
        } catch (e: Exception) {
            Log.w(TAG, "Invalid color string: $colorString, using default", e)
            Color.parseColor("#FF9800")
        }
    }

    fun getAppOpenAction(context: Context, countdown: Countdown): Action {
        val componentName = ComponentName(context, MainActivity::class.java)
        return actionStartActivity(componentName)
    }

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

    private fun isDarkTheme(context: Context): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}