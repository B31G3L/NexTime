package de.beigel.nextime.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.ui.theme.NexTimeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class CountdownWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    companion object {
        private const val TAG = "WidgetConfig"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")

        // Standard-Ergebnis auf CANCELLED setzen
        setResult(RESULT_CANCELED)

        // Widget ID aus Intent holen
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        Log.d(TAG, "Widget ID: $appWidgetId")

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "Invalid widget ID, finishing")
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
                        appWidgetId = appWidgetId,
                        onConfigComplete = { countdownId ->
                            Log.d(TAG, "Config complete for countdown $countdownId")
                            saveWidgetConfig(countdownId)
                            finishWithSuccess()
                        },
                        onCancel = {
                            Log.d(TAG, "Config cancelled")
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun saveWidgetConfig(countdownId: Long) {
        Log.d(TAG, "Saving config: widget=$appWidgetId, countdown=$countdownId")

        val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putLong("widget_${appWidgetId}_countdown_id", countdownId)
            apply()
        }

        // Verifizieren
        val savedId = prefs.getLong("widget_${appWidgetId}_countdown_id", -1L)
        Log.d(TAG, "Verified saved ID: $savedId")
    }

    private fun finishWithSuccess() {
        Log.d(TAG, "Finishing with success")

        try {
            // Widget updaten
            val appWidgetManager = AppWidgetManager.getInstance(this)

            // Kurze Verzögerung, damit die Datenbank-Transaktion sicher abgeschlossen ist
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                delay(100)

                Log.d(TAG, "Triggering widget update")
                CountdownWidgetReceiver.updateWidget(this@CountdownWidgetConfigActivity, appWidgetManager, appWidgetId)

                // WorkManager für regelmäßige Updates starten
                WidgetUpdateWorker.scheduleWork(this@CountdownWidgetConfigActivity)

                // Erfolg zurückmelden
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(RESULT_OK, resultValue)

                Log.d(TAG, "Success result set, finishing activity")
                finish()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in finishWithSuccess", e)
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    appWidgetId: Int,
    onConfigComplete: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var countdowns by remember { mutableStateOf<List<Countdown>>(emptyList()) }
    var selectedCountdown by remember { mutableStateOf<Countdown?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Countdowns laden
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                Log.d("WidgetConfig", "Loading countdowns...")
                val database = CountdownDatabase.getDatabase(context)
                val loadedCountdowns = database.countdownDao().getAllCountdowns().first()
                countdowns = loadedCountdowns
                isLoading = false
                Log.d("WidgetConfig", "Loaded ${countdowns.size} countdowns")
            } catch (e: Exception) {
                Log.e("WidgetConfig", "Error loading countdowns", e)
                errorMessage = e.message
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Widget einrichten") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    // Loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Lade Countdowns...")
                        }
                    }
                }
                errorMessage != null -> {
                    // Error
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("❌", fontSize = 48.sp)
                                Text(
                                    "Fehler beim Laden",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    errorMessage ?: "Unbekannter Fehler",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Button(
                                    onClick = onCancel,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Schließen")
                                }
                            }
                        }
                    }
                }
                countdowns.isEmpty() -> {
                    // No countdowns
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("⚠️", fontSize = 48.sp)
                                Text(
                                    "Keine Countdowns vorhanden",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Erstelle zuerst einen Countdown in der NexTime App, bevor du ein Widget hinzufügst.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(onClick = onCancel) {
                                    Text("Schließen")
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Countdown list
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Info-Text
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "Wähle einen Countdown für dein Widget aus:",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(countdowns) { countdown ->
                                WidgetCountdownCard(
                                    countdown = countdown,
                                    isSelected = selectedCountdown?.id == countdown.id,
                                    onClick = { selectedCountdown = countdown }
                                )
                            }
                        }

                        // Buttons
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 8.dp,
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onCancel,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Abbrechen")
                                }

                                Button(
                                    onClick = {
                                        selectedCountdown?.let { countdown ->
                                            onConfigComplete(countdown.id)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = selectedCountdown != null
                                ) {
                                    Text("Widget hinzufügen")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetCountdownCard(
    countdown: Countdown,
    isSelected: Boolean,
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
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(DesignSystem.Card.cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient Hintergrund (nur wenn nicht ausgewählt)
            if (!isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    cardColor.copy(alpha = 0.15f),
                                    cardColor.copy(alpha = 0.05f)
                                )
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Titel und Check
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = countdown.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSelected) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Ausgewählt",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }
                    }
                }

                // Countdown-Anzeige
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${timeInfo.days}",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                cardColor,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (timeInfo.days == 1L) "Tag" else "Tage",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }

                // Datum
                Text(
                    text = countdown.targetDateTime.format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    ) + if (countdown.includeTime) {
                        " • " + countdown.targetDateTime.format(
                            DateTimeFormatter.ofPattern("HH:mm")
                        )
                    } else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}