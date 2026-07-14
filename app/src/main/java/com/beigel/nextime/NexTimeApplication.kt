package com.beigel.nextime

import android.app.Application
import android.content.res.Configuration
import com.beigel.nextime.ui.theme.LanguageManager
import com.beigel.nextime.widget.WidgetUpdateWorker
import com.beigel.nextime.widget.scheduleMinutelyWidgetUpdate

class NexTimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LanguageManager.applyLanguageFromPrefs(this)
        scheduleMinutelyWidgetUpdate(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val nightModeFlags = newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES,
            Configuration.UI_MODE_NIGHT_NO -> WidgetUpdateWorker.updateNow(this)
        }
    }
}