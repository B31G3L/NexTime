package todo.beigelwick.de.todolist.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import todo.beigelwick.de.todolist.data.model.DisplayFormat
import todo.beigelwick.de.todolist.data.model.DisplayUnit
import java.time.LocalTime

// ─── Display Style ────────────────────────────────────────────────────────────

enum class DisplayStyle { COMPACT, NORMAL, GENEROUS }

// ─── AppPreferences ───────────────────────────────────────────────────────────

object AppPreferences {

    private val DEFAULT_COLOR     = stringPreferencesKey("default_color")
    private val DEFAULT_TIME      = stringPreferencesKey("default_time")
    private val DISPLAY_STYLE     = stringPreferencesKey("display_style")
    private val DEFAULT_DATE_UNITS = stringPreferencesKey("default_date_units")  // nur Datumseinheiten
    private val SHOW_TIME_ON_CARD  = booleanPreferencesKey("show_time_on_card")  // Uhrzeit-Toggle

    // ── Standard-Farbe ────────────────────────────────────────────────────────

    fun getDefaultColor(context: Context): Flow<String> =
        context.dataStore.data.map { prefs -> prefs[DEFAULT_COLOR] ?: "#FF7043" }

    suspend fun setDefaultColor(context: Context, color: String) {
        context.dataStore.edit { it[DEFAULT_COLOR] = color }
    }

    // ── Standard-Uhrzeit ──────────────────────────────────────────────────────

    fun getDefaultTime(context: Context): Flow<LocalTime> =
        context.dataStore.data.map { prefs ->
            try { LocalTime.parse(prefs[DEFAULT_TIME] ?: "12:00") }
            catch (e: Exception) { LocalTime.of(12, 0) }
        }

    suspend fun setDefaultTime(context: Context, time: LocalTime) {
        context.dataStore.edit { it[DEFAULT_TIME] = time.toString() }
    }

    // ── Display Style ─────────────────────────────────────────────────────────

    fun getDisplayStyle(context: Context): Flow<DisplayStyle> =
        context.dataStore.data.map { prefs ->
            try { DisplayStyle.valueOf(prefs[DISPLAY_STYLE] ?: DisplayStyle.NORMAL.name) }
            catch (e: Exception) { DisplayStyle.NORMAL }
        }

    suspend fun setDisplayStyle(context: Context, style: DisplayStyle) {
        context.dataStore.edit { it[DISPLAY_STYLE] = style.name }
    }

    // ── Datumseinheiten (Jahre, Monate, Wochen, Tage) ─────────────────────────

    fun getDefaultDateUnits(context: Context): Flow<Set<DisplayUnit>> =
        context.dataStore.data.map { prefs ->
            DisplayFormat.decode(prefs[DEFAULT_DATE_UNITS] ?: DisplayUnit.DAYS.name)
                .filter { it !in setOf(DisplayUnit.HOURS, DisplayUnit.MINUTES, DisplayUnit.SECONDS) }
                .toSet()
                .ifEmpty { setOf(DisplayUnit.DAYS) }
        }

    suspend fun setDefaultDateUnits(context: Context, units: Set<DisplayUnit>) {
        // Sicherheitshalber Zeiteinheiten rausfiltern
        val dateOnly = units.filter { it !in setOf(DisplayUnit.HOURS, DisplayUnit.MINUTES, DisplayUnit.SECONDS) }.toSet()
        context.dataStore.edit { it[DEFAULT_DATE_UNITS] = DisplayFormat.encode(dateOnly.ifEmpty { setOf(DisplayUnit.DAYS) }) }
    }

    // ── Uhrzeit-Anzeige (HH:mm:ss) ───────────────────────────────────────────

    fun getShowTimeOnCard(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[SHOW_TIME_ON_CARD] ?: false }

    suspend fun setShowTimeOnCard(context: Context, show: Boolean) {
        context.dataStore.edit { it[SHOW_TIME_ON_CARD] = show }
    }
}