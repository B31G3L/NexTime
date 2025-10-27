package de.beigel.nextime.widget

import android.app.Activity
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.theme.NexTimeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Widget Configuration Activity
 * Ermöglicht dem Benutzer die Auswahl eines Countdowns für das Widget
 */
class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Standardmäßig abbrechen, falls kein Widget-ID
        setResult(Activity.RESULT_CANCELED)

        // Widget ID aus Intent holen
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Wenn keine gültige ID, Activity beenden
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            NexTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigScreen(
                        onCountdownSelected = { countdown ->
                            saveSelectedCountdown(countdown)
                            updateWidgetAndFinish()
                        },
                        onCancel = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    /**
     * Speichert die ausgewählte Countdown-ID für dieses Widget
     */
    private fun saveSelectedCountdown(countdown: Countdown) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(PREF_PREFIX_KEY + appWidgetId, countdown.id).apply()
    }

    /**
     * Widget aktualisieren und Activity beenden
     */
    private fun updateWidgetAndFinish() {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        // Widget sofort aktualisieren
        WidgetUpdateWorker.updateNow(this)

        // Erfolg zurückgeben
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }

    companion object {
        private const val PREFS_NAME = "de.beigel.nextime.widget.WidgetConfig"
        private const val PREF_PREFIX_KEY = "countdown_id_"

        /**
         * Gibt die gespeicherte Countdown-ID für ein Widget zurück
         */
        fun loadCountdownId(context: Context, appWidgetId: Int): Long? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val countdownId = prefs.getLong(PREF_PREFIX_KEY + appWidgetId, -1L)
            return if (countdownId != -1L) countdownId else null
        }

        /**
         * Löscht die gespeicherte Konfiguration für ein Widget
         */
        fun deleteCountdownId(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(PREF_PREFIX_KEY + appWidgetId).apply()
        }
    }
}

/**
 * Composable für die Widget-Konfiguration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(
    onCountdownSelected: (Countdown) -> Unit,
    onCancel: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var countdowns by remember { mutableStateOf<List<Countdown>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Lade Countdowns
    LaunchedEffect(Unit) {
        val database = CountdownDatabase.getDatabase(context)
        countdowns = database.countdownDao().getAllCountdowns().first()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget einrichten") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Abbrechen")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            countdowns.isEmpty() -> {
                // Keine Countdowns vorhanden
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⏰",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "Keine Countdowns",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Erstelle zuerst einen Countdown in der App",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                // Countdown-Liste anzeigen
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Wähle einen Countdown für das Widget:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(countdowns) { countdown ->
                        CountdownSelectionCard(
                            countdown = countdown,
                            onClick = { onCountdownSelected(countdown) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card für die Countdown-Auswahl
 */
@Composable
private fun CountdownSelectionCard(
    countdown: Countdown,
    onClick: () -> Unit
) {
    val cardColor = try {
        Color(android.graphics.Color.parseColor(countdown.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Titel
                Text(
                    text = countdown.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Datum
                Text(
                    text = countdown.targetDateTime.format(
                        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Countdown-Info
                val timeInfo = countdown.calculateTimeRemaining()
                Text(
                    text = if (timeInfo.isPast) {
                        "Vor ${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tagen"}"
                    } else {
                        "Noch ${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = cardColor
                )
            }

            // Farbindikator
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = cardColor
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}