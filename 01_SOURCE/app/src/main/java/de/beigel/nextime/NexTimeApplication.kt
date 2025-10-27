package de.beigel.nextime

import android.app.Application
import android.content.res.Configuration
import de.beigel.nextime.widget.WidgetUpdateWorker

/**
 * Application-Klasse für globale App-Konfiguration
 * Überwacht Theme-Änderungen und aktualisiert Widgets
 */
class NexTimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Starte periodische Widget-Updates
        WidgetUpdateWorker.enqueue(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Theme wurde geändert - Widget sofort aktualisieren
        val nightModeFlags = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK

        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES,
            Configuration.UI_MODE_NIGHT_NO -> {
                // Theme hat sich geändert (Light <-> Dark)
                WidgetUpdateWorker.updateNow(this)
            }
        }
    }
}