package de.beigel.nextime.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.calculateTimeRemaining
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
        CountdownDisplayFormat.DAYS_ONLY
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
                        CountdownDisplayFormat.DAYS_ONLY -> {
                            DaysOnlyDisplay(timeInfo, baseColor)
                        }
                        CountdownDisplayFormat.WEEKS_DAYS -> {
                            WeeksDaysDisplay(timeInfo, baseColor)
                        }
                        CountdownDisplayFormat.MONTHS_DAYS -> {
                            MonthsDaysDisplay(timeInfo, baseColor)
                        }
                        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                            YearsMonthsDaysDisplay(timeInfo, baseColor)
                        }
                    }
                }

                // Datum
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

@Composable
private fun YearsMonthsDaysDisplay(
    timeInfo: de.beigel.nextime.data.model.CountdownInfo,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
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
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
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
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}