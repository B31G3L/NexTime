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
        LARGE("Groß (4×3)", 3, de.beigel.nextime.R.layout.widget_countdown_large)
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
    private fun WidgetSizeSelectionScreen(
        onSizeSelected: (WidgetSize) -> Unit,
        onCancel: () -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Widget-Größe wählen") },
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Wähle die Widget-Größe:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Kleine Widget
                WidgetSizeCard(
                    size = WidgetSize.SMALL,
                    isSelected = selectedWidgetSize == WidgetSize.SMALL,
                    onClick = { onSizeSelected(WidgetSize.SMALL) }
                )

                // Mittlere Widget (Default)
                WidgetSizeCard(
                    size = WidgetSize.MEDIUM,
                    isSelected = selectedWidgetSize == WidgetSize.MEDIUM,
                    isDefault = true,
                    onClick = { onSizeSelected(WidgetSize.MEDIUM) }
                )

                // Große Widget
                WidgetSizeCard(
                    size = WidgetSize.LARGE,
                    isSelected = selectedWidgetSize == WidgetSize.LARGE,
                    onClick = { onSizeSelected(WidgetSize.LARGE) }
                )

                Spacer(modifier = Modifier.weight(1f))

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
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

                    Button(
                        onClick = { onSizeSelected(selectedWidgetSize) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Weiter")
                    }
                }
            }
        }
    }

    @Composable
    private fun WidgetSizeCard(
        size: WidgetSize,
        isSelected: Boolean,
        isDefault: Boolean = false,
        onClick: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(DesignSystem.CornerRadius.large),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 4.dp else 2.dp
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
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            size.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isDefault) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Standard",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Text(
                        getWidgetSizeDescription(size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Ausgewählt",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    private fun getWidgetSizeDescription(size: WidgetSize): String {
        return when (size) {
            WidgetSize.SMALL -> "Eine Zeile - platzsparend"
            WidgetSize.MEDIUM -> "Balance zwischen Info und Größe"
            WidgetSize.LARGE -> "Detailliert mit Sekunden"
        }
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
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Wähle einen Countdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Widget-Größe: ${selectedWidgetSize.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                                Text("Zurück")
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

                // Countdown-Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tage
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

                    // Stunden
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

                    // Minuten
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

                Spacer(modifier = Modifier.height(8.dp))

                // Datum
                Text(
                    text = "📅 ${countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
        prefs.edit().putInt(PREF_KEY_WIDGET_SIZE + appWidgetId, selectedWidgetSize.ordinal).apply()
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
        const val PREF_KEY_WIDGET_SIZE = "widget_size_"

        fun getCountdownId(context: Context, appWidgetId: Int): Long {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getLong(PREF_KEY_COUNTDOWN_ID + appWidgetId, -1L)
        }

        fun deleteCountdownPref(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(PREF_KEY_COUNTDOWN_ID + appWidgetId).apply()
            prefs.edit().remove(PREF_KEY_WIDGET_SIZE + appWidgetId).apply()
        }
    }
}