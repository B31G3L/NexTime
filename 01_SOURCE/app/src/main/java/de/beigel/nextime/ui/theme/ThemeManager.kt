package de.beigel.nextime.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM,  // Folgt der Systemeinstellung
    LIGHT,   // Immer hell
    DARK     // Immer dunkel
}

object ThemePreferences {
    private val THEME_MODE = stringPreferencesKey("theme_mode")
    private val DEFAULT_TIME = stringPreferencesKey("default_time")

    fun getThemeMode(context: Context): Flow<ThemeMode> {
        return context.dataStore.data.map { preferences ->
            val modeString = preferences[THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(modeString)
            } catch (e: Exception) {
                ThemeMode.SYSTEM
            }
        }
    }

    suspend fun setThemeMode(context: Context, mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode.name
        }
    }

    fun getDefaultTime(context: Context): Flow<LocalTime> {
        return context.dataStore.data.map { preferences ->
            val timeString = preferences[DEFAULT_TIME] ?: "00:00"
            try {
                LocalTime.parse(timeString)
            } catch (e: Exception) {
                LocalTime.of(0, 0)
            }
        }
    }

    suspend fun setDefaultTime(context: Context, time: LocalTime) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_TIME] = time.toString()
        }
    }
}