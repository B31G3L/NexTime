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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.ui.theme.NexTimeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class CountdownWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var database: CountdownDatabase
    private var selectedWidgetSize: WidgetSize = WidgetSize.MEDIUM  // Default: Mittel

    enum class WidgetSize(val displayName: String, val cellHeight: Int, val layoutResId: Int) {
        SMALL("Kompakt (4×1)", 1, de.beigel.nextime.R.layout.widget_countdown_small),
        MEDIUM("Mittel (4×2)", 2, de.beigel.nextime.R.layout.widget_countdown_medium),
        LARGE("Groß (4×3)", 3, de.beigel.nextime.R.layout.widget_countdown_large);

        companion object {
            fun fromOrdinal(ordinal: Int): WidgetSize = values().getOrNull(ordinal) ?: MEDIUM
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ergebnis auf CANCELED setzen
        setResult(RESULT_CANCELED)

        // Widget-ID aus Intent holen
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Bei ungültiger ID beenden
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        database = CountdownDatabase.getDatabase(this)

        setContent {
            NexTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigScreen(
                        onCountdownSelected = { countdown ->
                            saveCountdownPref(countdown.id)
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun WidgetConfigScreen(
        onCountdownSelected: (Countdown) -> Unit,
        onCancel: () -> Unit
    ) {
        var countdowns by remember { mutableStateOf<List<Countdown>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            lifecycleScope.launch {
                countdowns = database.countdownDao().getAllCountdowns().first()
                isLoading = false
            }
        }

        // Direkt zum Countdown Selection (nur noch ein Schritt)
        CountdownSelectionScreen(
            countdowns = countdowns,
            isLoading = isLoading,
            onCountdownSelected = onCountdownSelected,
            onCancel = onCancel
        )
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CountdownSelectionScreen(
        countdowns: List<Countdown>,
        isLoading: Boolean,
        onCountdownSelected: (Countdown) -> Unit,
        onCancel: () -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Countdown wählen") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (countdowns.isEmpty()) {
                    // Keine Countdowns vorhanden
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "⏰",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Noch keine Countdowns",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Erstelle zuerst einen Countdown in der App",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(onClick = onCancel) {
                                Text("Zurück")
                            }
                            Button(onClick = onCancel) {
                                Text("Abbrechen")
                            }
                        }
                    }
                } else {
                    // Countdowns anzeigen
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Wähle einen Countdown",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Liste der Countdowns
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(countdowns) { countdown ->
                                CountdownSelectionCard(
                                    countdown = countdown,
                                    onClick = { onCountdownSelected(countdown) }
                                )
                            }
                        }

                        // Navigation Buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TextButton(
                                onClick = onCancel,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Abbrechen")
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CountdownSelectionCard(
        countdown: Countdown,
        onClick: () -> Unit
    ) {
        val timeInfo = countdown.calculateTimeRemaining()
        val cardColor = try {
            Color(android.graphics.Color.parseColor(countdown.color))
        } catch (e: Exception) {
            MaterialTheme.colorScheme.primary
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(DesignSystem.CornerRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Farbbalken
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = cardColor,
                    shape = RoundedCornerShape(2.dp)
                ) {}

                Spacer(modifier = Modifier.height(12.dp))

                // Titel
                Text(
                    text = countdown.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Countdown-Info - BEACHTE DAS COUNTDOWN-FORMAT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tage - IMMER zeigen
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${timeInfo.days}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = cardColor
                        )
                        Text(
                            text = "Tage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stunden - NUR wenn countdown.includeTime=true
                    if (countdown.includeTime) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d", timeInfo.hours),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = cardColor.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Std",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Minuten - NUR wenn countdown.includeTime=true
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d", timeInfo.minutes),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = cardColor.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "Min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Datum - IMMER zeigen
                Text(
                    text = "📅 ${countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Uhrzeit - NUR wenn countdown.includeTime=true
                if (countdown.includeTime) {
                    Text(
                        text = "🕐 ${countdown.targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))} Uhr",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    private fun saveCountdownPref(countdownId: Long) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(PREF_KEY_COUNTDOWN_ID + appWidgetId, countdownId).apply()
    }

    private fun updateWidgetAndFinish() {
        val appWidgetManager = AppWidgetManager.getInstance(this)

        // Aktualisiere das Widget mit der korrekten Größe
        CountdownWidget.updateAppWidget(
            this,
            appWidgetManager,
            appWidgetId,
            selectedWidgetSize.layoutResId
        )

        // Ergebnis auf OK setzen
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }

    companion object {
        const val PREFS_NAME = "de.beigel.nextime.widget.CountdownWidget"
        const val PREF_KEY_COUNTDOWN_ID = "countdown_id_"

        fun getCountdownId(context: Context, appWidgetId: Int): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getLong(PREF_KEY_COUNTDOWN_ID + appWidgetId, -1L)
        }

        fun deleteCountdownPref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(PREF_KEY_COUNTDOWN_ID + appWidgetId).apply()
        }
    }
}