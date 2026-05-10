package todo.beigelwick.de.todolist.ui.theme

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
    SYSTEM("",   "Systemsprache"),
    GERMAN("de", "Deutsch"),
    ENGLISH("en", "English"),
    FRENCH("fr",  "Français"),
    SPANISH("es", "Español"),
    ITALIAN("it", "Italiano")
}

// ─── LanguageManager ─────────────────────────────────────────────────────────

object LanguageManager {

    private val LANGUAGE_KEY = stringPreferencesKey("app_language")

    fun getLanguage(context: Context): Flow<AppLanguage> =
        context.dataStore.data.map { prefs ->
            val tag = prefs[LANGUAGE_KEY] ?: ""
            AppLanguage.values().find { it.tag == tag } ?: AppLanguage.SYSTEM
        }

    suspend fun setLanguage(context: Context, language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language.tag
        }
        persistLanguageSync(context, language)
    }

    fun applyLanguage(language: AppLanguage) {
        val localeList = if (language == AppLanguage.SYSTEM || language.tag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun applyLanguageFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val tag   = prefs.getString("app_language_sync", null)
        if (tag != null) {
            val language = AppLanguage.values().find { it.tag == tag } ?: AppLanguage.SYSTEM
            applyLanguage(language)
        }
    }

    fun persistLanguageSync(context: Context, language: AppLanguage) {
        context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("app_language_sync", language.tag)
            .apply()
    }
}