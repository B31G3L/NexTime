package de.beigel.nextime.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class CountdownWidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Standard-Ergebnis auf CANCELLED setzen
        setResult(RESULT_CANCELED)

        // Widget ID aus Intent holen
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            NexTimeTheme {
                WidgetConfigScreen(
                    appWidgetId = appWidgetId,
                    onConfigComplete = { countdownId ->
                        saveWidgetConfig(countdownId)
                        finishWithSuccess()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    private fun saveWidgetConfig(countdownId: Long) {
        val prefs = getSharedPreferences("widget_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putLong("widget_${appWidgetId}_countdown_id", countdownId)
            apply()
        }
    }

    private fun finishWithSuccess() {
        // Widget updaten
        val appWidgetManager = AppWidgetManager.getInstance(this)
        CountdownWidgetReceiver.updateWidget(this, appWidgetManager, appWidgetId)

        // WorkManager für Updates starten
        WidgetUpdateWorker.scheduleWork(this)

        // Erfolg zurückmelden
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
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

    // Countdowns laden
    LaunchedEffect(Unit) {
        scope.launch {
            val database = CountdownDatabase.getDatabase(context)
            countdowns = database.countdownDao().getAllCountdowns().first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Countdown für Widget auswählen") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (countdowns.isEmpty()) {
                // Keine Countdowns vorhanden
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(DesignSystem.Spacing.xxLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(DesignSystem.Spacing.large),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 48.sp
                            )
                            Text(
                                text = "Keine Countdowns vorhanden",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Erstelle zuerst einen Countdown in der NexTime App, bevor du ein Widget hinzufügst.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
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
            } else {
                // Countdown-Liste
                Column(modifier = Modifier.weight(1f)) {
                    // Info-Text
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "Wähle einen Countdown für dein Widget aus:",
                            modifier = Modifier.padding(DesignSystem.Spacing.medium),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(DesignSystem.Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
                    ) {
                        items(countdowns) { countdown ->
                            WidgetCountdownCard(
                                countdown = countdown,
                                isSelected = selectedCountdown?.id == countdown.id,
                                onClick = { selectedCountdown = countdown }
                            )
                        }
                    }
                }

                // Buttons
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignSystem.Spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
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
            defaultElevation = if (isSelected) 8.dp else DesignSystem.Card.elevation
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient Hintergrund
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
                    .padding(DesignSystem.Spacing.medium),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Titel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = countdown.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
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

                // Countdown-Anzeige (vereinfacht)
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