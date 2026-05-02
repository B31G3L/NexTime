package de.beigel.nextime.ui.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─── Unterstützte Sprachen ────────────────────────────────────────────────────

enum class AppLanguage(
    val tag: String,
    val displayName: String
) {
    SYSTEM("", "Systemsprache"),
    GERMAN("de", "Deutsch"),
    ENGLISH("en", "English"),
    FRENCH("fr", "Français"),
    SPANISH("es", "Español"),
    ITALIAN("it", "Italiano")
}

// ─── LanguageManager ─────────────────────────────────────────────────────────

object LanguageManager {

    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    // Aktuelle Spracheinstellung aus DataStore lesen
    fun getLanguage(context: Context): Flow<AppLanguage> =
        context.dataStore.data.map { prefs ->
            val tag = prefs[LANGUAGE_KEY] ?: ""
            AppLanguage.values().find { it.tag == tag } ?: AppLanguage.SYSTEM
        }

    // Sprache persistieren — applyLanguage MUSS auf dem Main-Thread laufen,
    // daher hier NUR speichern; der Aufrufer ruft applyLanguage() separat auf.
    suspend fun setLanguage(context: Context, language: AppLanguage) {
        // In DataStore speichern
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language.tag
        }
        // Sync-Fallback für App-Start
        persistLanguageSync(context, language)
    }

    // Sprache sofort anwenden — MUSS auf dem Main-Thread aufgerufen werden!
    fun applyLanguage(language: AppLanguage) {
        val localeList = if (language == AppLanguage.SYSTEM || language.tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        // AppCompatDelegate.setApplicationLocales() ist thread-safe und
        // triggert automatisch einen Activity-Recreate auf dem Main-Thread
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    // Beim App-Start gespeicherte Sprache synchron anwenden
    fun applyLanguageFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val tag = prefs.getString("app_language_sync", null)
        // Nur anwenden wenn explizit gesetzt — bei null/leer Systemsprache belassen
        if (tag != null) {
            val language = AppLanguage.values().find { it.tag == tag } ?: AppLanguage.SYSTEM
            applyLanguage(language)
        }
    }

    // Sync-Speicherung für App-Start (SharedPreferences, da DataStore async ist)
    fun persistLanguageSync(context: Context, language: AppLanguage) {
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language_sync", language.tag)
            .apply()
    }
}