package todo.beigelwick.de.todolist.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import todo.beigelwick.de.todolist.data.model.CountdownDisplayFormat
import java.time.LocalTime

object AppPreferences {

    private val DEFAULT_FORMAT   = stringPreferencesKey("default_format")
    private val DEFAULT_COLOR    = stringPreferencesKey("default_color")
    private val DEFAULT_TIME     = stringPreferencesKey("default_time")

    // ── Standard-Anzeigeformat ────────────────────────────────────────────────

    fun getDefaultFormat(context: Context): Flow<CountdownDisplayFormat> =
        context.dataStore.data.map { prefs ->
            val value = prefs[DEFAULT_FORMAT] ?: CountdownDisplayFormat.DAYS_ONLY.name
            try { CountdownDisplayFormat.valueOf(value) }
            catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }
        }

    suspend fun setDefaultFormat(context: Context, format: CountdownDisplayFormat) {
        context.dataStore.edit { prefs -> prefs[DEFAULT_FORMAT] = format.name }
    }

    // ── Standard-Farbe ────────────────────────────────────────────────────────

    fun getDefaultColor(context: Context): Flow<String> =
        context.dataStore.data.map { prefs -> prefs[DEFAULT_COLOR] ?: "#FF7043" }

    suspend fun setDefaultColor(context: Context, color: String) {
        context.dataStore.edit { prefs -> prefs[DEFAULT_COLOR] = color }
    }

    // ── Standard-Uhrzeit ──────────────────────────────────────────────────────

    fun getDefaultTime(context: Context): Flow<LocalTime> =
        context.dataStore.data.map { prefs ->
            val value = prefs[DEFAULT_TIME] ?: "12:00"
            try { LocalTime.parse(value) } catch (e: Exception) { LocalTime.of(12, 0) }
        }

    suspend fun setDefaultTime(context: Context, time: LocalTime) {
        context.dataStore.edit { prefs -> prefs[DEFAULT_TIME] = time.toString() }
    }
}