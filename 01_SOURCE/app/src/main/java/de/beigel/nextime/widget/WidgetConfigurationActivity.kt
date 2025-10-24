package de.beigel.nextime.widget

import android.appwidget.AppWidgetManager
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
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.theme.NexTimeTheme
import de.beigel.nextime.widget.utils.WidgetHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WidgetConfigurationActivity : ComponentActivity() {

    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private val TAG = "WidgetConfig"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "WidgetConfigurationActivity started")

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        Log.d(TAG, "Widget ID: $appWidgetId")

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "Invalid widget ID, finishing activity")
            finish()
            return
        }

        setResult(RESULT_CANCELED)

        setContent {
            NexTimeTheme {
                WidgetConfigurationScreen(
                    onCountdownSelected = { countdown ->
                        configureWidget(countdown)
                    },
                    onCancel = {
                        Log.d(TAG, "Configuration cancelled by user")
                        finish()
                    }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun WidgetConfigurationScreen(
        onCountdownSelected: (Countdown) -> Unit,
        onCancel: () -> Unit
    ) {
        var countdowns by remember { mutableStateOf<List<Countdown>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            try {
                val database = CountdownDatabase.getDatabase(this@WidgetConfigurationActivity)
                countdowns = database.countdownDao().getAllCountdowns().first()
                Log.d(TAG, "Loaded ${countdowns.size} countdowns")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading countdowns", e)
            } finally {
                isLoading = false
            }
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
                Log.d(TAG, "Configuring widget $appWidgetId with countdown ${countdown.id}: ${countdown.title}")

                val glanceManager = GlanceAppWidgetManager(this@WidgetConfigurationActivity)
                val glanceId = glanceManager.getGlanceIdBy(appWidgetId)

                Log.d(TAG, "Got GlanceId: $glanceId")

                updateAppWidgetState(this@WidgetConfigurationActivity, glanceId) { prefs ->
                    prefs[WidgetHelper.COUNTDOWN_ID_KEY] = countdown.id
                    Log.d(TAG, "Saved countdown ID ${countdown.id} to widget state")
                }

                // ⭐ KORREKTUR: Widget-Typ über AppWidgetManager ermitteln
                val appWidgetManager = AppWidgetManager.getInstance(this@WidgetConfigurationActivity)
                val widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                val providerClassName = widgetInfo?.provider?.className

                Log.d(TAG, "Widget provider class name: $providerClassName")

                // Widget basierend auf Provider-Klasse aktualisieren
                when {
                    providerClassName?.contains("SmallCountdownWidgetReceiver") == true -> {
                        Log.d(TAG, "Updating SmallCountdownWidget")
                        SmallCountdownWidget().update(this@WidgetConfigurationActivity, glanceId)
                    }
                    providerClassName?.contains("MediumCountdownWidgetReceiver") == true -> {
                        Log.d(TAG, "Updating MediumCountdownWidget")
                        MediumCountdownWidget().update(this@WidgetConfigurationActivity, glanceId)
                    }
                    providerClassName?.contains("LargeCountdownWidgetReceiver") == true -> {
                        Log.d(TAG, "Updating LargeCountdownWidget")
                        LargeCountdownWidget().update(this@WidgetConfigurationActivity, glanceId)
                    }
                    else -> {
                        Log.w(TAG, "Unknown widget provider: $providerClassName, defaulting to MediumCountdownWidget")
                        MediumCountdownWidget().update(this@WidgetConfigurationActivity, glanceId)
                    }
                }

                val resultValue = Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                }
                setResult(RESULT_OK, resultValue)

                Log.d(TAG, "Widget configuration successful")
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error configuring widget", e)
                e.printStackTrace()
                finish()
            }
        }
    }
}