package de.beigel.nextime.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object ThemePreferences {
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val DEFAULT_TIME = stringPreferencesKey("default_time")

    fun getDarkMode(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE] ?: false
        }
    }

    suspend fun setDarkMode(context: Context, isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = isDark
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