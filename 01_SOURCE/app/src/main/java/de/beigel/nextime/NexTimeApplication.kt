package de.beigel.nextime

import android.app.Application
import android.content.res.Configuration
import de.beigel.nextime.ui.theme.LanguageManager
import de.beigel.nextime.widget.WidgetUpdateWorker

/**
 * Application-Klasse für globale App-Konfiguration.
 * Initialisiert Sprache und Widget-Updates.
 */
class NexTimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Gespeicherte Sprache sofort anwenden (vor dem ersten Activity-Start)
        LanguageManager.applyLanguageFromPrefs(this)

        // Periodische Widget-Updates starten
        WidgetUpdateWorker.enqueue(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val nightModeFlags = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES,
            Configuration.UI_MODE_NIGHT_NO -> {
                WidgetUpdateWorker.updateNow(this)
            }
        }
    }
}