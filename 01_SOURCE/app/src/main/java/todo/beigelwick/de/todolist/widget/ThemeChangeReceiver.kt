package todo.beigelwick.de.todolist.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ThemeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            CoroutineScope(Dispatchers.IO).launch {
                WidgetUpdateWorker.updateNow(context)
            }
        }
    }
}