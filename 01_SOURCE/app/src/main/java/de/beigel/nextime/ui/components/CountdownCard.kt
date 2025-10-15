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
fun CountdownCard(countdown: Countdown) {
    var timeInfo by remember { mutableStateOf(countdown.calculateTimeRemaining()) }

    LaunchedEffect(countdown.id) {
        if (countdown.includeTime || timeInfo.isPast) {
            while (true) {
                delay(1000)
                timeInfo = countdown.calculateTimeRemaining()
            }
        } else timeInfo = countdown.calculateTimeRemaining()
    }

    val baseColor = runCatching { Color(android.graphics.Color.parseColor(countdown.color)) }
        .getOrElse { MaterialTheme.colorScheme.primary }
    val backgroundColor = baseColor.copy(alpha = 0.12f)
    val format = runCatching { CountdownDisplayFormat.valueOf(countdown.displayFormat) }
        .getOrDefault(CountdownDisplayFormat.DAYS_ONLY)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                when (format) {
                    CountdownDisplayFormat.FULL_DETAILED, CountdownDisplayFormat.FULL_TIME -> 180.dp
                    CountdownDisplayFormat.DAYS_HOURS, CountdownDisplayFormat.WEEKS_DAYS, CountdownDisplayFormat.MONTHS_DAYS -> 170.dp
                    else -> 150.dp
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar
                Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(baseColor))

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Titel
                    Text(
                        text = countdown.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Main display
                    when (format) {
                        CountdownDisplayFormat.DAYS_ONLY -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${timeInfo.days}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = baseColor)
                                Text(if (timeInfo.days == 1L) "Tag" else "Tage", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        CountdownDisplayFormat.DAYS_HOURS -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${timeInfo.days}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = baseColor)
                                Text(if (timeInfo.days == 1L) "Tag" else "Tage", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(6.dp))
                                Text(String.format("%02d:%02d", timeInfo.hours, timeInfo.minutes), fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        CountdownDisplayFormat.HOURS_MINUTES -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(String.format("%d:%02d", timeInfo.hours, timeInfo.minutes), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = baseColor)
                                Text("Stunden", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        CountdownDisplayFormat.WEEKS_DAYS -> {
                            val remDays = (timeInfo.days % 7)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${timeInfo.weeks}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = baseColor)
                                Text(if (timeInfo.weeks == 1L) "Woche" else "Wochen", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (remDays > 0) { Spacer(Modifier.height(6.dp)); Text("$remDays ${if (remDays==1L) "Tag" else "Tage"}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                        CountdownDisplayFormat.MONTHS_DAYS -> {
                            val remDays = (timeInfo.days - timeInfo.months * 30)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${timeInfo.months}", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = baseColor)
                                Text(if (timeInfo.months == 1L) "Monat" else "Monate", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (remDays > 0) { Spacer(Modifier.height(6.dp)); Text("$remDays ${if (remDays==1L) "Tag" else "Tage"}", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            }
                        }
                        CountdownDisplayFormat.FULL_TIME -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${timeInfo.days}d", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = baseColor)
                                Spacer(Modifier.height(6.dp))
                                Text(String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = baseColor)
                            }
                        }
                        CountdownDisplayFormat.FULL_DETAILED -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                                    if (timeInfo.years > 0) { Text("${timeInfo.years}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = baseColor); Text("J ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    if (timeInfo.months > 0) { Text("${timeInfo.months}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = baseColor); Text("M ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    Text("${timeInfo.days}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = baseColor); Text("T", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = baseColor)
                            }
                        }
                    }

                    // Datum/Zeit zentriert
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(6.dp))
                        Text(countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (countdown.includeTime) {
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Outlined.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(6.dp))
                            Text(countdown.targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // bottom bar
                Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(baseColor))
            }
        }
    }
}
