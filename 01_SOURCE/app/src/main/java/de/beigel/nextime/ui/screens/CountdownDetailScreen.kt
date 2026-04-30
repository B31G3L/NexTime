package de.beigel.nextime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.CountdownInfo
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.utils.HapticFeedback
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    var showDeleteDialog by remember { mutableStateOf(false) }
    var swipeOffset by remember { mutableStateOf(0f) }
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

    val stats = remember(countdown, timeInfo) {
        calculateStatistics(countdown, timeInfo)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // HERO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                swipeOffset += dragAmount
                                change.consumePositionChange()
                            },
                            onDragEnd = {
                                if (swipeOffset > 150f) {
                                    haptic.tick()
                                    onBack()
                                }
                                swipeOffset = 0f
                            },
                            onDragCancel = { swipeOffset = 0f }
                        )
                    }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                cardColor.copy(alpha = 0.3f),
                                cardColor.copy(alpha = 0.05f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                IconButton(
                    onClick = { haptic.tick(); onBack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 8.dp, top = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Zurück",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = countdown.title,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val format = try {
                        CountdownDisplayFormat.valueOf(countdown.displayFormat)
                    } catch (e: Exception) {
                        CountdownDisplayFormat.DAYS_ONLY
                    }

                    when (format) {
                        CountdownDisplayFormat.DAYS_ONLY -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${timeInfo.days}",
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cardColor,
                                    letterSpacing = (-2).sp
                                )
                                Text(
                                    text = if (timeInfo.days == 1L) "Tag" else "Tage",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        CountdownDisplayFormat.WEEKS_DAYS -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${timeInfo.weeks}",
                                        fontSize = 56.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor,
                                        letterSpacing = (-1).sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (timeInfo.weeks == 1L) "Woche" else "Wochen",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${timeInfo.remainingDaysAfterWeeks}",
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor.copy(alpha = 0.7f),
                                        letterSpacing = (-0.5).sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (timeInfo.remainingDaysAfterWeeks == 1L) "Tag" else "Tage",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                            }
                        }

                        CountdownDisplayFormat.MONTHS_DAYS -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${timeInfo.months}",
                                        fontSize = 56.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor,
                                        letterSpacing = (-1).sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (timeInfo.months == 1L) "Monat" else "Monate",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                if (timeInfo.remainingDaysAfterMonths > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${timeInfo.remainingDaysAfterMonths}",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor.copy(alpha = 0.7f),
                                            letterSpacing = (-0.5).sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (timeInfo.remainingDaysAfterMonths == 1L) "Tag" else "Tage",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    if (timeInfo.years > 0) {
                                        Text(
                                            text = "${timeInfo.years}",
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor,
                                            letterSpacing = (-1).sp
                                        )
                                        Text(
                                            text = "J ",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                    if (timeInfo.remainingMonthsAfterYears > 0) {
                                        Text(
                                            text = "${timeInfo.remainingMonthsAfterYears}",
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor,
                                            letterSpacing = (-1).sp
                                        )
                                        Text(
                                            text = "M ",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                    }
                                    Text(
                                        text = "${timeInfo.remainingDaysAfterYears}",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor,
                                        letterSpacing = (-1).sp
                                    )
                                    Text(
                                        text = "T",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // BOTTOM
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(title = "Zieldatum", icon = Icons.Outlined.CalendarToday) {
                    Text(
                        text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                InfoCard(title = "Statistiken", icon = Icons.Outlined.Event) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionButtonCompact(
                        icon = Icons.Default.Share,
                        label = "Teilen",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { haptic.click(); onShare() }
                    )
                    ActionButtonCompact(
                        icon = Icons.Default.Edit,
                        label = "Bearbeiten",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { haptic.click(); onEdit() }
                    )
                    ActionButtonCompact(
                        icon = Icons.Default.Delete,
                        label = "Löschen",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        onClick = { haptic.tick(); showDeleteDialog = true }
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { haptic.tick(); showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Countdown löschen?") },
            text = {
                Text(
                    "Möchtest du \"${countdown.title}\" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { haptic.heavy(); showDeleteDialog = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { haptic.tick(); showDeleteDialog = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun ActionButtonCompact(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(56.dp),
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.1f),
        tonalElevation = 0.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold, fontSize = 9.sp)
        }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

private data class Statistics(val duration: String, val status: String, val createdDate: String)

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
        days > 30  -> "${days / 30} Monat${if (days / 30 > 1) "e" else ""}"
        days > 0   -> "$days Tag${if (days > 1) "e" else ""}"
        else       -> "Heute"
    }
}