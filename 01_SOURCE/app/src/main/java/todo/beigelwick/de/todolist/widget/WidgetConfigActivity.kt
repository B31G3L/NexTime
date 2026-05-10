package todo.beigelwick.de.todolist.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.database.CountdownDatabase
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.calculateTimeRemaining
import todo.beigelwick.de.todolist.ui.theme.CustomTheme
import todo.beigelwick.de.todolist.ui.theme.CustomThemePreferences
import todo.beigelwick.de.todolist.ui.theme.NexTimeTheme
import todo.beigelwick.de.todolist.ui.theme.ThemeMode
import todo.beigelwick.de.todolist.ui.theme.ThemePreferences
import java.time.format.DateTimeFormatter

class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        setContent {
            val themeMode   by ThemePreferences.getThemeMode(this).collectAsState(initial = ThemeMode.SYSTEM)
            val customTheme by CustomThemePreferences.getCustomTheme(this).collectAsState(initial = CustomTheme.NEXTIME)
            val isDark = when (themeMode) {
                ThemeMode.DARK   -> true
                ThemeMode.LIGHT  -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            NexTimeTheme(darkTheme = isDark, customTheme = customTheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WidgetConfigScreen(
                        onCountdownSelected = { countdown ->
                            saveSelectedCountdown(countdown)
                            updateWidgetAndFinish()
                        },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    private fun isSystemInDarkTheme(): Boolean {
        val uiMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return uiMode == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun saveSelectedCountdown(countdown: Countdown) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putLong(PREF_PREFIX_KEY + appWidgetId, countdown.id)
            .apply()
    }

    private fun updateWidgetAndFinish() {
        WidgetUpdateWorker.updateNow(this)
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }

    companion object {
        private const val PREFS_NAME      = "todo.beigelwick.de.todolist.widget.WidgetConfig"
        private const val PREF_PREFIX_KEY = "countdown_id_"

        fun loadCountdownId(context: Context, appWidgetId: Int): Long? {
            val id = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(PREF_PREFIX_KEY + appWidgetId, -1L)
            return if (id != -1L) id else null
        }

        fun deleteCountdownId(context: Context, appWidgetId: Int) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(PREF_PREFIX_KEY + appWidgetId)
                .apply()
        }
    }
}

// ─── Widget Config Screen ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(
    onCountdownSelected : (Countdown) -> Unit,
    onCancel            : () -> Unit
) {
    val context    = LocalContext.current
    var countdowns by remember { mutableStateOf<List<Countdown>>(emptyList()) }
    var isLoading  by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val database = CountdownDatabase.getDatabase(context)
        countdowns   = database.countdownDao().getAllCountdowns().first()
        isLoading    = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.widget_config_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            countdowns.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("⏰", style = MaterialTheme.typography.displayLarge)
                        Text(stringResource(R.string.widget_no_countdowns), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.widget_no_countdowns_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(stringResource(R.string.widget_select_hint), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(countdowns) { countdown ->
                        WidgetCountdownCard(countdown = countdown, onClick = { onCountdownSelected(countdown) })
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetCountdownCard(countdown: Countdown, onClick: () -> Unit) {
    val cardColor = try { Color(android.graphics.Color.parseColor(countdown.color)) }
    catch (e: Exception) { MaterialTheme.colorScheme.primary }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.08f))
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = countdown.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text  = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val timeInfo = countdown.calculateTimeRemaining()
                Text(
                    text  = if (timeInfo.isPast)
                        "${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"} vergangen"
                    else
                        "Noch ${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = cardColor
                )
            }
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = cardColor) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(text = countdown.icon.ifBlank { "⏰" }, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}