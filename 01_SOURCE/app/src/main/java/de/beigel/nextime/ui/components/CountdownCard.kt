package de.beigel.nextime.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.data.model.getFormattedTime
import de.beigel.nextime.data.model.getFormattedTimeLabel
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.utils.HapticFeedback
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownCard(
    countdown: Countdown,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }

    var timeInfo by remember { mutableStateOf(countdown.calculateTimeRemaining()) }

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

    // Dynamische Card-Höhe basierend auf Format
    val format = try {
        CountdownDisplayFormat.valueOf(countdown.displayFormat)
    } catch (e: Exception) {
        CountdownDisplayFormat.DAYS_ONLY
    }

    val cardHeight = when (format) {
        CountdownDisplayFormat.FULL_TIME -> 200.dp
        CountdownDisplayFormat.DAYS_HOURS,
        CountdownDisplayFormat.HOURS_MINUTES -> 190.dp
        else -> 180.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight),
        shape = RoundedCornerShape(DesignSystem.Card.cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = DesignSystem.Card.elevation)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient Hintergrund
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                cardColor.copy(alpha = DesignSystem.Alpha.cardBackground),
                                cardColor.copy(alpha = DesignSystem.Alpha.verySubtle)
                            )
                        )
                    )
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(DesignSystem.Spacing.large),
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

                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxSmall))

                            // Status Badge - Countdown oder Count-up
                            Surface(
                                shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                                color = if (timeInfo.isPast)
                                    MaterialTheme.colorScheme.errorContainer
                                else
                                    cardColor.copy(alpha = DesignSystem.Alpha.surface)
                            ) {
                                Text(
                                    text = if (timeInfo.isPast) "✨ Count-up" else "✨ Countdown",
                                    modifier = Modifier.padding(
                                        horizontal = DesignSystem.Spacing.small,
                                        vertical = DesignSystem.Spacing.xxSmall
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (timeInfo.isPast)
                                        MaterialTheme.colorScheme.onErrorContainer
                                    else
                                        cardColor
                                )
                            }
                        }
                    }

                    // Countdown-Anzeige (zentriert) - Angepasst an Format
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val format = try {
                            de.beigel.nextime.data.model.CountdownDisplayFormat.valueOf(countdown.displayFormat)
                        } catch (e: Exception) {
                            de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY
                        }

                        when (format) {
                            de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY,
                            de.beigel.nextime.data.model.CountdownDisplayFormat.WEEKS_DAYS,
                            de.beigel.nextime.data.model.CountdownDisplayFormat.MONTHS_DAYS -> {
                                // Große Anzeige für simple Formate
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = countdown.getFormattedTime(timeInfo),
                                        fontSize = DesignSystem.Typography.countdownLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor,
                                        letterSpacing = (-1).sp
                                    )

                                    val label = countdown.getFormattedTimeLabel(timeInfo)
                                    if (label.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(DesignSystem.Spacing.xSmall))
                                        Text(
                                            text = label,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = DesignSystem.Spacing.xxSmall)
                                        )
                                    }
                                }
                            }
                            de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_HOURS,
                            de.beigel.nextime.data.model.CountdownDisplayFormat.HOURS_MINUTES -> {
                                // Kompakte Anzeige für Stunden-Formate
                                Text(
                                    text = countdown.getFormattedTime(timeInfo),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cardColor,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                            de.beigel.nextime.data.model.CountdownDisplayFormat.FULL_TIME -> {
                                // Extra kompakt für vollständige Zeit
                                Text(
                                    text = countdown.getFormattedTime(timeInfo),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cardColor,
                                    letterSpacing = (-0.5).sp
                                )
                            }
                        }

                        // Zusätzliche Zeit-Info
                        if (countdown.includeTime && timeInfo.days > 0) {
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxSmall))
                            Text(
                                text = String.format("%02d:%02d Uhr", timeInfo.hours, timeInfo.minutes),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Nächte-Anzeige
                        if (countdown.showNights && timeInfo.nights > 0) {
                            Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxSmall))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "🌙", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(DesignSystem.Spacing.xxSmall))
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
                                modifier = Modifier.size(DesignSystem.Icon.small)
                            )
                            Spacer(modifier = Modifier.width(DesignSystem.Spacing.xxSmall + DesignSystem.Spacing.xxSmall))
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
                                    modifier = Modifier.size(DesignSystem.Icon.small)
                                )
                                Spacer(modifier = Modifier.width(DesignSystem.Spacing.xxSmall + DesignSystem.Spacing.xxSmall))
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
            }
        }
    }
}