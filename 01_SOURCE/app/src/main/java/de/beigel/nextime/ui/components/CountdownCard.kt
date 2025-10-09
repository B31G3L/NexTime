package de.beigel.nextime.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.utils.HapticFeedback
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownCard(
    countdown: Countdown,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    showPercentage: Boolean = true
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }

    // Live-Update für Countdowns mit Uhrzeit
    var timeInfo by remember { mutableStateOf(countdown.calculateTimeRemaining()) }

    // Fortschritts-Berechnung
    val progress = remember(countdown, timeInfo) {
        calculateProgress(countdown)
    }

    LaunchedEffect(countdown.id) {
        if (countdown.includeTime) {
            while (true) {
                delay(1000)
                timeInfo = countdown.calculateTimeRemaining()
            }
        } else {
            timeInfo = countdown.calculateTimeRemaining()
        }
    }

    val cardColor = try {
        Color(android.graphics.Color.parseColor(countdown.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (countdown.includeTime) 220.dp else 200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient Hintergrund
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

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Kopfzeile mit Titel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = countdown.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Status Chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (timeInfo.isPast)
                                    MaterialTheme.colorScheme.errorContainer
                                else
                                    cardColor.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = if (timeInfo.isPast) "Vergangen" else "Bevorstehend",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (timeInfo.isPast)
                                        MaterialTheme.colorScheme.error
                                    else
                                        cardColor
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                haptic.tick()
                                onEdit()
                            }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Bearbeiten",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = {
                                haptic.tick()
                                onDelete()
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Löschen",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Countdown-Anzeige (zentriert)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hauptzähler - großer Fokus
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (timeInfo.days > 0 || !countdown.includeTime) {
                                    "${timeInfo.days}"
                                } else {
                                    String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds)
                                },
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardColor,
                                letterSpacing = (-1).sp
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = if (timeInfo.days > 0 || !countdown.includeTime) {
                                    if (timeInfo.days == 1L) "Tag" else "Tage"
                                } else {
                                    ""
                                },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Zusätzliche Zeit-Info
                        if (countdown.includeTime && timeInfo.days > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format("%02d:%02d Uhr", timeInfo.hours, timeInfo.minutes),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Nächte-Anzeige
                        if (countdown.showNights && timeInfo.nights > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🌙",
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${timeInfo.nights} ${if (timeInfo.nights == 1L) "Nacht" else "Nächte"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Zieldatum mit Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = countdown.targetDateTime.format(
                                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (countdown.includeTime) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = countdown.targetDateTime.format(
                                        DateTimeFormatter.ofPattern("HH:mm")
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Fortschrittsbalken
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                ) {
                    // Hintergrund
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                    )

                    // Animierter Fortschrittsbalken
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(
                            durationMillis = 1000,
                            easing = FastOutSlowInEasing
                        ),
                        label = "progress"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        cardColor.copy(alpha = 0.7f),
                                        cardColor
                                    )
                                )
                            )
                    )
                }

                // Prozentanzeige (optional)
                if (showPercentage && progress > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}% vergangen",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            }
        }
    }
}

// Hilfsfunktion zur Berechnung des Fortschritts
private fun calculateProgress(countdown: Countdown): Float {
    val now = LocalDateTime.now()
    val created = countdown.createdAt
    val target = countdown.targetDateTime

    return if (target.isBefore(now)) {
        1f // 100% wenn vergangen
    } else {
        val totalDuration = Duration.between(created, target).toMillis()
        val elapsedDuration = Duration.between(created, now).toMillis()

        if (totalDuration > 0) {
            min(1f, elapsedDuration.toFloat() / totalDuration.toFloat())
        } else {
            0f
        }
    }
}