package de.beigel.nextime.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    private val TAG = "WidgetConfig"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        Log.d(TAG, "Widget Config Activity started with appWidgetId: $appWidgetId")

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "Invalid widget ID, finishing")
            finish()
            return
        }

        database = CountdownDatabase.getDatabase(this)

        // WICHTIG: Bestimme die Widget-Größe basierend auf gespeicherten Daten oder Standardwert
        val widgetSize = getWidgetSizeFromPrefs()
        Log.d(TAG, "Determined widget size: $widgetSize")

        setContent {
            NexTimeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigScreen(
                        onCountdownSelected = { countdown ->
                            saveAndFinish(countdown.id, widgetSize)
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
     * Hole die Widget-Größe aus den Preferences
     * Falls noch nicht gespeichert, nutze MEDIUM als Standard
     */
    private fun getWidgetSizeFromPrefs(): WidgetSize {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sizeOrdinal = prefs.getInt("widget_size_$appWidgetId", -1)

        Log.d(TAG, "Widget size from prefs: ordinal=$sizeOrdinal")

        return if (sizeOrdinal != -1) {
            WidgetSize.values().getOrNull(sizeOrdinal) ?: WidgetSize.MEDIUM
        } else {
            // Neu hinzugefügtes Widget - nutze Standard
            WidgetSize.MEDIUM
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
                    title = { Text("Countdown auswählen") },
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
                        Button(onClick = onCancel) {
                            Text("Abbrechen")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
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
        val baseColor = runCatching { Color(android.graphics.Color.parseColor(countdown.color)) }
            .getOrElse { MaterialTheme.colorScheme.primary }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(DesignSystem.CornerRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = baseColor.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = baseColor,
                    shape = RoundedCornerShape(2.dp)
                ) {}

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = countdown.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${timeInfo.days}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = baseColor
                        )
                        Text(
                            text = "Tage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (countdown.includeTime) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d", timeInfo.hours),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = baseColor.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "Std",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%02d", timeInfo.minutes),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = baseColor.copy(alpha = 0.6f)
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
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = baseColor,
                    shape = RoundedCornerShape(2.dp)
                ) {}
            }
        }
    }

    /**
     * Speichere die Countdown-ID und Widget-Größe, dann schließe die Activity
     */
    private fun saveAndFinish(countdownId: Long, widgetSize: WidgetSize) {
        Log.d(TAG, "Saving widget config: widgetId=$appWidgetId, countdownId=$countdownId, size=${widgetSize.name} (ordinal=${widgetSize.ordinal})")

        // Speichere BEIDE Werte
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putLong("countdown_id_$appWidgetId", countdownId)
            putInt("widget_size_$appWidgetId", widgetSize.ordinal)
            apply()
        }

        Log.d(TAG, "Preferences saved successfully")

        // Aktualisiere das Widget sofort
        val appWidgetManager = AppWidgetManager.getInstance(this)
        CountdownWidget.updateAppWidget(this, appWidgetManager, appWidgetId, widgetSize.layoutResId)

        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }

    enum class WidgetSize(val displayName: String, val cellHeight: Int, val layoutResId: Int) {
        SMALL("Kompakt (4×1)", 1, de.beigel.nextime.R.layout.widget_countdown_small),
        MEDIUM("Mittel (4×2)", 2, de.beigel.nextime.R.layout.widget_countdown_medium),
        LARGE("Groß (4×3)", 3, de.beigel.nextime.R.layout.widget_countdown_large);

        companion object {
            fun fromOrdinal(ordinal: Int): WidgetSize = values().getOrNull(ordinal) ?: MEDIUM
        }
    }

    companion object {
        const val PREFS_NAME = "de.beigel.nextime.widget.CountdownWidget"
    }
}