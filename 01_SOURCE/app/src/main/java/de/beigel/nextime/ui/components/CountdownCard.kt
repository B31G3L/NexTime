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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = baseColor.copy(alpha = 0.08f) // Leicht transparenter Hintergrund in Countdown-Farbe
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
                // Titel mit Count-up/Countdown Indikator
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

                // Hauptanzeige - einheitliches Layout wie Widget
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tage
                    TimeUnit(
                        value = if (timeInfo.isPast) "${timeInfo.days}" else "${timeInfo.days}",
                        label = if (timeInfo.days == 1L) "Tag" else "Tage",
                        textColor = baseColor,
                        isPrimary = true
                    )

                    // Stunden
                    TimeUnit(
                        value = String.format("%02d", timeInfo.hours),
                        label = if (timeInfo.hours == 1L) "Stunde" else "Stunden",
                        textColor = baseColor.copy(alpha = 0.85f),
                        isPrimary = false
                    )

                    // Minuten
                    TimeUnit(
                        value = String.format("%02d", timeInfo.minutes),
                        label = if (timeInfo.minutes == 1L) "Minute" else "Minuten",
                        textColor = baseColor.copy(alpha = 0.7f),
                        isPrimary = false
                    )

                    // Sekunden (nur wenn Zeit enthalten ist)
                    if (countdown.includeTime) {
                        TimeUnit(
                            value = String.format("%02d", timeInfo.seconds),
                            label = "Sek",
                            textColor = baseColor.copy(alpha = 0.55f),
                            isPrimary = false,
                            isSmall = true
                        )
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
private fun TimeUnit(
    value: String,
    label: String,
    textColor: Color,
    isPrimary: Boolean = false,
    isSmall: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = if (isSmall) 4.dp else 8.dp)
    ) {
        Text(
            text = value,
            fontSize = when {
                isSmall -> 24.sp
                isPrimary -> 36.sp
                else -> 32.sp
            },
            fontWeight = FontWeight.Bold,
            color = textColor,
            lineHeight = when {
                isSmall -> 28.sp
                isPrimary -> 40.sp
                else -> 36.sp
            }
        )
        Text(
            text = label,
            fontSize = if (isSmall) 10.sp else 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}