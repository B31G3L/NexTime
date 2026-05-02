package de.beigel.nextime.ui.theme

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─── Unterstützte Sprachen ────────────────────────────────────────────────────

enum class AppLanguage(
    val tag: String,        // BCP-47 Sprach-Tag
    val displayName: String // Anzeigename in der App
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

    // Sprache persistieren und sofort anwenden
    suspend fun setLanguage(context: Context, language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language.tag
        }
        applyLanguage(language)
    }

    // Sprache beim App-Start anwenden (in Application.onCreate aufrufen)
    fun applyLanguage(language: AppLanguage) {
        if (language == AppLanguage.SYSTEM) {
            // Systemsprache wiederherstellen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+: Per-App Language Preferences API
                // Wird über AppCompatDelegate gehandhabt
            }
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            val localeList = LocaleListCompat.forLanguageTags(language.tag)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    // Beim App-Start gespeicherte Sprache aus SharedPreferences lesen und anwenden
    // (DataStore ist async — für den Start brauchen wir einen sync-Fallback)
    fun applyLanguageFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val tag = prefs.getString("app_language_sync", "") ?: ""
        val language = AppLanguage.values().find { it.tag == tag } ?: AppLanguage.SYSTEM
        applyLanguage(language)
    }

    // Sprache auch in SharedPreferences speichern (für sync-Zugriff beim Start)
    fun persistLanguageSync(context: Context, language: AppLanguage) {
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language_sync", language.tag)
            .apply()
    }
}