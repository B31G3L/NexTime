package de.beigel.nextime.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.data.model.getFormattedTime
import de.beigel.nextime.ui.theme.DesignSystem
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownCard(countdown: Countdown) {
    var timeInfo by remember { mutableStateOf(countdown.calculateTimeRemaining()) }

    LaunchedEffect(countdown.id) {
        while (true) {
            delay(1000)
            timeInfo = countdown.calculateTimeRemaining()
        }
    }

    val baseColor = runCatching { Color(android.graphics.Color.parseColor(countdown.color)) }
        .getOrElse { MaterialTheme.colorScheme.primary }

    // Format aus Countdown holen
    val format = try {
        CountdownDisplayFormat.valueOf(countdown.displayFormat)
    } catch (e: Exception) {
        CountdownDisplayFormat.FULL_DETAILED
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = baseColor.copy(alpha = 0.08f)
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Farbbalken oben
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(baseColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                }

                // Hauptanzeige - Format-abhängig
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (format) {
                        CountdownDisplayFormat.FULL_DETAILED -> {
                            FullDetailedDisplay(timeInfo, baseColor)
                        }
                        CountdownDisplayFormat.DAYS_ONLY -> {
                            DaysOnlyDisplay(timeInfo, baseColor)
                        }
                        CountdownDisplayFormat.DAYS_HOURS -> {
                            DaysHoursDisplay(timeInfo, baseColor)
                        }
                        CountdownDisplayFormat.HOURS_MINUTES -> {
                            HoursMinutesDisplay(timeInfo, baseColor)
                        }
                        CountdownDisplayFormat.FULL_TIME -> {
                            FullTimeDisplay(timeInfo, baseColor)
                        }
                        CountdownDisplayFormat.WEEKS_DAYS -> {
                            WeeksDaysDisplay(timeInfo, baseColor)
                        }
                        CountdownDisplayFormat.MONTHS_DAYS -> {
                            MonthsDaysDisplay(timeInfo, baseColor)
                        }
                    }
                }

                // Datum und Zeit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (countdown.includeTime) {
                        Spacer(Modifier.width(12.dp))
                        Icon(
                            Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Farbbalken unten
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(baseColor)
            )
        }
    }
}

@Composable
private fun FullDetailedDisplay(
    timeInfo: de.beigel.nextime.data.model.CountdownInfo,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Jahre, Monate, Tage
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom
        ) {
            if (timeInfo.years > 0) {
                Text(
                    text = "${timeInfo.years}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "J ",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            if (timeInfo.months > 0 || timeInfo.years > 0) {
                val remainingMonths = timeInfo.months % 12
                if (remainingMonths > 0) {
                    Text(
                        text = "$remainingMonths",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "M ",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            val remainingDays = timeInfo.days % 30
            Text(
                text = "$remainingDays",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "T",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        // HH:MM:SS
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun DaysOnlyDisplay(
    timeInfo: de.beigel.nextime.data.model.CountdownInfo,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${timeInfo.days}",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = if (timeInfo.days == 1L) "Tag" else "Tage",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DaysHoursDisplay(
    timeInfo: de.beigel.nextime.data.model.CountdownInfo,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tage
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "${timeInfo.days}",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = if (timeInfo.days == 1L) "Tag" else "Tage",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Stunden
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = String.format("%02d", timeInfo.hours),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.8f)
            )
            Text(
                text = "Std",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = ":",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Minuten
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = String.format("%02d", timeInfo.minutes),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.6f)
            )
            Text(
                text = "Min",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HoursMinutesDisplay(
    timeInfo: de.beigel.nextime.data.model.CountdownInfo,
    color: Color
) {
    val totalHours = timeInfo.days * 24 + timeInfo.hours
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "$totalHours",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = "h ",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = String.format("%02d", timeInfo.minutes),
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.8f)
        )
        Text(
            text = "m",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
    }
}

@Composable
private fun FullTimeDisplay(
    timeInfo: de.beigel.nextime.data.model.CountdownInfo,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "${timeInfo.days}",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "d ",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun WeeksDaysDisplay(
    timeInfo: de.beigel.nextime.data.model.CountdownInfo,
    color: Color
) {
    val remainingDays = timeInfo.days % 7
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "${timeInfo.weeks}",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (timeInfo.weeks == 1L) "Woche" else "Wochen",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$remainingDays",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (remainingDays == 1L) "Tag" else "Tage",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

@Composable
private fun MonthsDaysDisplay(
    timeInfo: de.beigel.nextime.data.model.CountdownInfo,
    color: Color
) {
    val remainingDays = timeInfo.days - (timeInfo.months * 30)
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "${timeInfo.months}",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (timeInfo.months == 1L) "Monat" else "Monate",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        if (remainingDays > 0) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "$remainingDays",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (remainingDays == 1L) "Tag" else "Tage",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}