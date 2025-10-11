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
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.utils.HapticFeedback
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownCard(
    countdown: Countdown
) {
    var timeInfo by remember { mutableStateOf(countdown.calculateTimeRemaining()) }

    LaunchedEffect(countdown.id) {
        if (countdown.includeTime || timeInfo.isPast) {
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

    // In CountdownCard.kt, Zeile ~49-56
    val cardHeight = when (format) {
        CountdownDisplayFormat.FULL_DETAILED -> 240.dp  // Erhöht von 220dp
        CountdownDisplayFormat.FULL_TIME -> 240.dp      // Erhöht von 220dp
        CountdownDisplayFormat.DAYS_HOURS,
        CountdownDisplayFormat.WEEKS_DAYS,
        CountdownDisplayFormat.MONTHS_DAYS -> 220.dp    // Erhöht von 200dp
        else -> 200.dp                                   // Erhöht von 180dp
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

                        }
                    }

                    // Countdown-Anzeige (zentriert)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (format) {
                            CountdownDisplayFormat.FULL_DETAILED -> {
                                // Jahre:Monate:Tage HH:MM:SS Format
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Erste Zeile: Jahre, Monate, Tage
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        if (timeInfo.years > 0) {
                                            Text(
                                                text = "${timeInfo.years}",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = cardColor
                                            )
                                            Text(
                                                text = "J ",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                        if (timeInfo.months > 0 || timeInfo.years > 0) {
                                            val remainingMonths = timeInfo.months % 12
                                            if (remainingMonths > 0) {
                                                Text(
                                                    text = "$remainingMonths",
                                                    fontSize = 28.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = cardColor
                                                )
                                                Text(
                                                    text = "M ",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(bottom = 2.dp)
                                                )
                                            }
                                        }
                                        val remainingDays = timeInfo.days % 30
                                        Text(
                                            text = "$remainingDays",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor
                                        )
                                        Text(
                                            text = "T",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                    // Zweite Zeile: HH:MM:SS
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor.copy(alpha = 0.9f),
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                            CountdownDisplayFormat.DAYS_ONLY -> {
                                // Nur Tage - große Anzeige
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${timeInfo.days}",
                                        fontSize = DesignSystem.Typography.countdownLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor,
                                        letterSpacing = (-1).sp
                                    )
                                    Spacer(modifier = Modifier.width(DesignSystem.Spacing.xSmall))
                                    Text(
                                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(bottom = DesignSystem.Spacing.xxSmall)
                                    )
                                }
                            }
                            CountdownDisplayFormat.WEEKS_DAYS -> {
                                // Wochen + Tage
                                val remainingDays = timeInfo.days % 7
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${timeInfo.weeks}",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor,
                                            letterSpacing = (-0.5).sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (timeInfo.weeks == 1L) "Woche" else "Wochen",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$remainingDays",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor.copy(alpha = 0.7f)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (remainingDays == 1L) "Tag" else "Tage",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                }
                            }
                            CountdownDisplayFormat.MONTHS_DAYS -> {
                                // Monate + Tage
                                val remainingDays = timeInfo.days - (timeInfo.months * 30)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${timeInfo.months}",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor,
                                            letterSpacing = (-0.5).sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (timeInfo.months == 1L) "Monat" else "Monate",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    if (remainingDays > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "$remainingDays",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = cardColor.copy(alpha = 0.7f)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (remainingDays == 1L) "Tag" else "Tage",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            CountdownDisplayFormat.DAYS_HOURS -> {
                                // Tage + Stunden
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${timeInfo.days}",
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor,
                                            letterSpacing = (-0.5).sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (timeInfo.days == 1L) "Tag" else "Tage",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format("%02d:%02d", timeInfo.hours, timeInfo.minutes),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            CountdownDisplayFormat.HOURS_MINUTES -> {
                                // Nur Stunden
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = String.format("%d:%02d", timeInfo.hours, timeInfo.minutes),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        text = "Stunden",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            CountdownDisplayFormat.FULL_TIME -> {
                                // Vollständig
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "${timeInfo.days}",
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cardColor,
                                            letterSpacing = (-0.5).sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "d",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = cardColor.copy(alpha = 0.8f),
                                        letterSpacing = (-0.5).sp
                                    )
                                }
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