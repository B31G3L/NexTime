package de.beigel.nextime.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.calculateTimeRemaining
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter

/**
 * Large Widget (4×3) - Vollständig mit Statistiken
 * Design: Fast weißer Hintergrund (#FAFAFA) mit Farbakzenten
 * Zeigt: Emoji, Titel, Datum, Große Zahl, Format-Text, Erstellt, Verbleibend %
 */
class CountdownLargeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = CountdownDatabase.getDatabase(context)
        val countdowns = database.countdownDao().getAllCountdowns().first()

        val countdown = countdowns.firstOrNull()

        provideContent {
            LargeWidgetContent(countdown = countdown)
        }
    }

    @Composable
    private fun LargeWidgetContent(countdown: Countdown?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),  // Fast weiß
            contentAlignment = Alignment.Center
        ) {
            if (countdown == null) {
                // Kein Countdown
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏰",
                        style = TextStyle(
                            fontSize = 48.sp,
                            color = ColorProvider(Color(0xFFBBBBBB))
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(16.dp))
                    Text(
                        text = "Kein Countdown",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = ColorProvider(Color(0xFF999999))
                        )
                    )
                }
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val accentColor = try {
                    Color(android.graphics.Color.parseColor(countdown.color))
                } catch (e: Exception) {
                    Color(0xFFFF9800)
                }

                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Top
                ) {
                    // Oberer Farbakzent - ganz am Rand über die volle Breite
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    // Header: Emoji + Titel + Datum (mit horizontalem Padding)
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Titel
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = countdown.title,
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color(0xFF333333))
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(10.dp))

                        // Datum
                        Text(
                            text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = ColorProvider(Color(0xFF666666))
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Format bestimmen
                    val format = try {
                        CountdownDisplayFormat.valueOf(countdown.displayFormat)
                    } catch (e: Exception) {
                        CountdownDisplayFormat.DAYS_ONLY
                    }

                    // Hauptanzeige - Format-abhängig
                    when (format) {
                        CountdownDisplayFormat.DAYS_ONLY -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${timeInfo.days}",
                                    style = TextStyle(
                                        fontSize = 80.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(accentColor)
                                    )
                                )
                                Text(
                                    text = if (timeInfo.days == 1L) "Tag" else "Tage",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        color = ColorProvider(Color(0xFF333333))
                                    )
                                )
                            }
                        }

                        CountdownDisplayFormat.WEEKS_DAYS -> {
                            val remainingDays = timeInfo.days % 7
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${timeInfo.weeks}",
                                        style = TextStyle(
                                            fontSize = 64.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorProvider(accentColor)
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = if (timeInfo.weeks == 1L) "Woche" else "Wochen",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = ColorProvider(Color(0xFF333333))
                                        ),
                                        modifier = GlanceModifier.padding(bottom = 8.dp)
                                    )
                                }
                                Spacer(modifier = GlanceModifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$remainingDays",
                                        style = TextStyle(
                                            fontSize = 42.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorProvider(accentColor.copy(alpha = 0.7f))
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = if (remainingDays == 1L) "Tag" else "Tage",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            color = ColorProvider(Color(0xFF333333))
                                        ),
                                        modifier = GlanceModifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }

                        CountdownDisplayFormat.MONTHS_DAYS -> {
                            val remainingDays = timeInfo.days % 30
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${timeInfo.months}",
                                        style = TextStyle(
                                            fontSize = 64.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorProvider(accentColor)
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = if (timeInfo.months == 1L) "Monat" else "Monate",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = ColorProvider(Color(0xFF333333))
                                        ),
                                        modifier = GlanceModifier.padding(bottom = 8.dp)
                                    )
                                }
                                if (remainingDays > 0) {
                                    Spacer(modifier = GlanceModifier.height(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$remainingDays",
                                            style = TextStyle(
                                                fontSize = 42.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorProvider(accentColor.copy(alpha = 0.7f))
                                            )
                                        )
                                        Spacer(modifier = GlanceModifier.width(6.dp))
                                        Text(
                                            text = if (remainingDays == 1L) "Tag" else "Tage",
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                color = ColorProvider(Color(0xFF333333))
                                            ),
                                            modifier = GlanceModifier.padding(bottom = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    if (timeInfo.years > 0) {
                                        Text(
                                            text = "${timeInfo.years}",
                                            style = TextStyle(
                                                fontSize = 48.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorProvider(accentColor)
                                            )
                                        )
                                        Text(
                                            text = "J ",
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                color = ColorProvider(Color(0xFF333333))
                                            ),
                                            modifier = GlanceModifier.padding(bottom = 6.dp)
                                        )
                                    }
                                    if (timeInfo.months > 0 || timeInfo.years > 0) {
                                        val remainingMonths = timeInfo.months % 12
                                        if (remainingMonths > 0) {
                                            Text(
                                                text = "$remainingMonths",
                                                style = TextStyle(
                                                    fontSize = 48.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ColorProvider(accentColor)
                                                )
                                            )
                                            Text(
                                                text = "M ",
                                                style = TextStyle(
                                                    fontSize = 16.sp,
                                                    color = ColorProvider(Color(0xFF333333))
                                                ),
                                                modifier = GlanceModifier.padding(bottom = 6.dp)
                                            )
                                        }
                                    }
                                    val remainingDays = timeInfo.days % 30
                                    Text(
                                        text = "$remainingDays",
                                        style = TextStyle(
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorProvider(accentColor)
                                        )
                                    )
                                    Text(
                                        text = "T",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            color = ColorProvider(Color(0xFF333333))
                                        ),
                                        modifier = GlanceModifier.padding(bottom = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Statistiken - mit leichtem Hintergrund für Abgrenzung
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0))  // Ganz leichter Hintergrund
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Erstellt
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Erstellt:",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF666666))
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Text(
                                text = countdown.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(Color(0xFF333333))
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        // Verbleibend (Prozent)
                        val percentage = calculatePercentage(countdown, timeInfo)
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verbleibend:",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF666666))
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Text(
                                text = percentage,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(accentColor)  // Prozent in Akzentfarbe
                                )
                            )
                        }
                    }


                    // Unterer Farbakzent - ganz am Rand über die volle Breite
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentColor)
                    ) { }
                }
            }
        }
    }

    private fun getEmojiForCountdown(title: String): String {
        return when {
            title.contains("Geburtstag", ignoreCase = true) -> "🎂"
            title.contains("Urlaub", ignoreCase = true) || title.contains("Sommerurlaub", ignoreCase = true) -> "🏖️"
            title.contains("Weihnachten", ignoreCase = true) -> "🎄"
            title.contains("Silvester", ignoreCase = true) -> "🎆"
            title.contains("Hochzeit", ignoreCase = true) -> "💍"
            else -> "⏰"
        }
    }

    private fun calculatePercentage(
        countdown: Countdown,
        timeInfo: de.beigel.nextime.data.model.CountdownInfo
    ): String {
        val now = java.time.LocalDateTime.now()
        val total = java.time.temporal.ChronoUnit.DAYS.between(
            countdown.createdAt.toLocalDate(),
            countdown.targetDateTime.toLocalDate()
        )
        val remaining = timeInfo.days

        return if (total > 0) {
            val percentage = (remaining.toFloat() / total.toFloat() * 100).toInt()
            "$percentage%"
        } else {
            "0%"
        }
    }
}

/**
 * Widget Receiver für Large Widget
 */
class CountdownLargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownLargeWidget()
}