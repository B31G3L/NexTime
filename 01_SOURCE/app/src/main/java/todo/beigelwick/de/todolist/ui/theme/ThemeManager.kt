package todo.beigelwick.de.todolist.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─── DataStore Instanz (einmalig pro App) ─────────────────────────────────────

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// ─── Theme Mode ───────────────────────────────────────────────────────────────

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

// ─── Theme Preferences ────────────────────────────────────────────────────────

object ThemePreferences {
    private val THEME_MODE = stringPreferencesKey("theme_mode")

    fun getThemeMode(context: Context): Flow<ThemeMode> =
        context.dataStore.data.map { prefs ->
            try {
                ThemeMode.valueOf(prefs[THEME_MODE] ?: ThemeMode.SYSTEM.name)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode.name
        }
    }
}