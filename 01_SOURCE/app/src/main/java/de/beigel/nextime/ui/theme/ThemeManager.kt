package de.beigel.nextime.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.text.get

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object ThemePreferences {
    val DARK_MODE = booleanPreferencesKey("dark_mode")

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
}