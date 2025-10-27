package de.beigel.nextime.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Broadcast Receiver der auf Theme-Änderungen reagiert
 * und alle Widgets sofort aktualisiert
 */
class ThemeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_CONFIGURATION_CHANGED -> {
                // Theme-Änderung erkannt - Widget sofort aktualisieren
                CoroutineScope(Dispatchers.IO).launch {
                    WidgetUpdateWorker.updateNow(context)
                }
            }
        }
    }
}