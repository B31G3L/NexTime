package de.beigel.nextime.widget.utils

import android.content.ComponentName
import android.content.Context
import android.graphics.Color
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

    val COUNTDOWN_ID_KEY = longPreferencesKey("countdown_id")

    suspend fun getCountdownForWidget(context: Context, glanceId: GlanceId): Countdown? {
        return try {
            // Korrekte Signatur für Glance 1.0.0: benötigt PreferencesGlanceStateDefinition
            val prefs = getAppWidgetState(
                context = context,
                definition = PreferencesGlanceStateDefinition,
                glanceId = glanceId
            )
            val countdownId = prefs[COUNTDOWN_ID_KEY] ?: return null

            val database = CountdownDatabase.getDatabase(context)
            database.countdownDao().getCountdownById(countdownId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllCountdowns(context: Context): List<Countdown> {
        return try {
            val database = CountdownDatabase.getDatabase(context)
            database.countdownDao().getAllCountdowns().first()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun parseColor(colorString: String): Int {
        return try {
            Color.parseColor(colorString)
        } catch (e: Exception) {
            Color.parseColor("#FF9800")
        }
    }

    fun getAppOpenAction(context: Context, countdown: Countdown): Action {
        // ComponentName für MainActivity erstellen
        val componentName = ComponentName(context, MainActivity::class.java)

        // actionStartActivity mit ComponentName (ohne Parameter)
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