package todo.beigelwick.de.todolist

import android.app.Application
import android.content.res.Configuration
import todo.beigelwick.de.todolist.ui.theme.LanguageManager
import todo.beigelwick.de.todolist.widget.WidgetUpdateWorker

class NexTimeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        LanguageManager.applyLanguageFromPrefs(this)
        WidgetUpdateWorker.enqueue(this)
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