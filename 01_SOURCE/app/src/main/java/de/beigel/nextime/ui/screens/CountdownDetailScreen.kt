package de.beigel.nextime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownInfo
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.utils.HapticFeedback
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownDetailScreen(
    countdown: Countdown,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Live-Update
    var timeInfo by remember { mutableStateOf(countdown.calculateTimeRemaining()) }

    LaunchedEffect(countdown.id) {
        while (true) {
            delay(1000)
            timeInfo = countdown.calculateTimeRemaining()
        }
    }

    val cardColor = try {
        Color(android.graphics.Color.parseColor(countdown.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    // Statistiken
    val stats = remember(countdown, timeInfo) {
        calculateStatistics(countdown, timeInfo)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.tick()
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        haptic.tick()
                        onShare()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Teilen")
                    }
                    IconButton(onClick = {
                        haptic.tick()
                        onEdit()
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Bearbeiten")
                    }
                    IconButton(onClick = {
                        haptic.tick()
                        showDeleteDialog = true
                    }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Löschen",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
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
                .verticalScroll(scrollState)
        ) {
            // Hero-Bereich mit Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                cardColor.copy(alpha = 0.3f),
                                cardColor.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Titel
                    Text(
                        text = countdown.title,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Hauptzähler - format-abhängig
                    val format = try {
                        de.beigel.nextime.data.model.CountdownDisplayFormat.valueOf(countdown.displayFormat)
                    } catch (e: Exception) {
                        de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY
                    }

                    when (format) {
                        de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${timeInfo.days}",
                                    fontSize = 96.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cardColor,
                                    letterSpacing = (-2).sp
                                )
                                Text(
                                    text = if (timeInfo.days == 1L) "Tag" else "Tage",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        de.beigel.nextime.data.model.CountdownDisplayFormat.WEEKS_DAYS -> {
                            val remainingDays = timeInfo.days % 7
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                                    Text(
                                        text = "${timeInfo.weeks}",
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor,
                                        letterSpacing = (-1.5).sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (timeInfo.weeks == 1L) "Woche" else "Wochen",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                                    Text(
                                        text = "$remainingDays",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor.copy(alpha = 0.7f),
                                        letterSpacing = (-1).sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (remainingDays == 1L) "Tag" else "Tage",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }

                        de.beigel.nextime.data.model.CountdownDisplayFormat.MONTHS_DAYS -> {
                            val remainingDays = timeInfo.days % 30
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                                    Text(
                                        text = "${timeInfo.months}",
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor,
                                        letterSpacing = (-1.5).sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (timeInfo.months == 1L) "Monat" else "Monate",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                                if (remainingDays > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                                        Text(
                                            text = "$remainingDays",
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor.copy(alpha = 0.7f),
                                            letterSpacing = (-1).sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (remainingDays == 1L) "Tag" else "Tage",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        de.beigel.nextime.data.model.CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    if (timeInfo.years > 0) {
                                        Text(
                                            text = "${timeInfo.years}",
                                            fontSize = 64.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor,
                                            letterSpacing = (-1).sp
                                        )
                                        Text(
                                            text = "J ",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    }
                                    if (timeInfo.months > 0 || timeInfo.years > 0) {
                                        val remainingMonths = timeInfo.months % 12
                                        if (remainingMonths > 0) {
                                            Text(
                                                text = "$remainingMonths",
                                                fontSize = 64.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = cardColor,
                                                letterSpacing = (-1).sp
                                            )
                                            Text(
                                                text = "M ",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                        }
                                    }
                                    val remainingDays = timeInfo.days % 30
                                    Text(
                                        text = "$remainingDays",
                                        fontSize = 64.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor,
                                        letterSpacing = (-1).sp
                                    )
                                    Text(
                                        text = "T",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Info-Karten
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Datum Card
                InfoCard(
                    title = "Zieldatum",
                    icon = Icons.Outlined.CalendarToday
                ) {
                    Text(
                        text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Statistiken Card
                InfoCard(
                    title = "Statistiken",
                    icon = Icons.Outlined.Event
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (timeInfo.isPast) {
                            StatRow("Vergangene Zeit", stats.status)
                            StatRow("Erstellt am", stats.createdDate)
                        } else {
                            StatRow("Gesamtdauer", stats.duration)
                            StatRow("Verbleibend", stats.status)
                            StatRow("Erstellt am", stats.createdDate)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Lösch-Bestätigungsdialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                haptic.tick()
                showDeleteDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Countdown löschen?")
            },
            text = {
                Text(
                    "Möchtest du \"${countdown.title}\" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.heavy()
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.tick()
                    showDeleteDialog = false
                }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            content()
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class Statistics(
    val duration: String,
    val status: String,
    val createdDate: String
)

private fun calculateStatistics(countdown: Countdown, timeInfo: CountdownInfo): Statistics {
    val now = LocalDateTime.now().toLocalDate().atStartOfDay()

    return if (timeInfo.isPast) {
        val duration = Duration.between(countdown.targetDateTime, now)

        Statistics(
            duration = formatDuration(duration),
            status = "Seit ${formatDuration(duration)}",
            createdDate = countdown.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        )
    } else {
        val totalDuration = Duration.between(countdown.createdAt, countdown.targetDateTime)
        val remaining = Duration.between(now, countdown.targetDateTime.toLocalDate().atStartOfDay())

        Statistics(
            duration = formatDuration(totalDuration),
            status = formatDuration(remaining),
            createdDate = countdown.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        )
    }
}

private fun formatDuration(duration: Duration): String {
    val days = duration.toDays()

    return when {
        days > 365 -> "${days / 365} Jahr${if (days / 365 > 1) "e" else ""}"
        days > 30 -> "${days / 30} Monat${if (days / 30 > 1) "e" else ""}"
        days > 0 -> "$days Tag${if (days > 1) "e" else ""}"
        else -> "Heute"
    }
}