package de.beigel.nextime.widget

import android.appwidget.AppWidgetManager
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.ui.theme.NexTimeTheme
import de.beigel.nextime.widget.utils.WidgetHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Configuration Activity für Widgets
 * Wird angezeigt, wenn User ein Widget zum Homescreen hinzufügt
 */
class WidgetConfigurationActivity : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Widget-ID aus Intent holen
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // Wenn keine gültige ID, Activity beenden
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Standardmäßig RESULT_CANCELED setzen
        setResult(RESULT_CANCELED)

        setContent {
            NexTimeTheme {
                WidgetConfigurationScreen(
                    onCountdownSelected = { countdown ->
                        configureWidget(countdown)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    @Composable
    private fun WidgetConfigurationScreen(
        onCountdownSelected: (Countdown) -> Unit,
        onCancel: () -> Unit
    ) {
        var countdowns by remember { mutableStateOf<List<Countdown>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            val database = CountdownDatabase.getDatabase(this@WidgetConfigurationActivity)
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (countdowns.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Erstelle zuerst einen Countdown in der App",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Wähle einen Countdown für dein Widget",
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
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(
                containerColor = cardColor.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Farbindikator
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .align(Alignment.TopCenter)
                        )
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            val timeInfo = countdown.calculateTimeRemaining()
                            Text(
                                text = "${timeInfo.days}",
                                style = MaterialTheme.typography.titleLarge,
                                color = cardColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Countdown Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = countdown.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val timeInfo = countdown.calculateTimeRemaining()
                    Text(
                        text = "${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    private fun configureWidget(countdown: Countdown) {
        lifecycleScope.launch {
            try {
                // GlanceId für das Widget holen
                val glanceId = GlanceAppWidgetManager(this@WidgetConfigurationActivity)
                    .getGlanceIdBy(appWidgetId)

                // Countdown-ID im Widget-State speichern
                updateAppWidgetState(this@WidgetConfigurationActivity, glanceId) { prefs ->
                    prefs[WidgetHelper.COUNTDOWN_ID_KEY] = countdown.id
                }

                // Widget aktualisieren
                when {
                    intent?.component?.className?.contains("Small") == true -> {
                        SmallCountdownWidget().update(this@WidgetConfigurationActivity, glanceId)
                    }
                    intent?.component?.className?.contains("Medium") == true -> {
                        MediumCountdownWidget().update(this@WidgetConfigurationActivity, glanceId)
                    }
                    intent?.component?.className?.contains("Large") == true -> {
                        LargeCountdownWidget().update(this@WidgetConfigurationActivity, glanceId)
                    }
                }

                // Erfolg zurückgeben
                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(RESULT_OK, resultValue)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                finish()
            }
        }
    }
}
