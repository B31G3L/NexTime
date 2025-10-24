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
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
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
                        style = TextStyle(fontSize = 40.sp)
                    )
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    Text(
                        text = "Kein Countdown",
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = ColorProvider(Color(0xFF666666))
                        )
                    )
                }
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val baseColor = try {
                    Color(android.graphics.Color.parseColor(countdown.color))
                } catch (e: Exception) {
                    Color(0xFFFF9800)
                }

                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Top
                ) {
                    // Oberer Balken
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(baseColor),
                        contentAlignment = Alignment.Center
                    ) { }

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    // Emoji + Titel + Datum
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getEmojiForCountdown(countdown.title),
                            style = TextStyle(fontSize = 24.sp)
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
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
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(
                            text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = ColorProvider(Color(0xFF666666))
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(16.dp))

                    // Hauptzahl
                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(baseColor)
                        )
                    )

                    // Label
                    Text(
                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = ColorProvider(Color(0xFF333333))
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    // Format-Anzeige
                    val format = try {
                        CountdownDisplayFormat.valueOf(countdown.displayFormat)
                    } catch (e: Exception) {
                        CountdownDisplayFormat.DAYS_ONLY
                    }

                    if (format != CountdownDisplayFormat.DAYS_ONLY) {
                        Text(
                            text = getFormattedDisplay(timeInfo, format),
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ColorProvider(Color(0xFF666666))
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(12.dp))
                    }

                    // Statistiken
                    Column(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
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
                                )
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = countdown.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(Color(0xFF333333))
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(4.dp))

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
                                )
                            )
                            Spacer(modifier = GlanceModifier.defaultWeight())
                            Text(
                                text = percentage,
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(Color(0xFF333333))
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Unterer Balken
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(baseColor),
                        contentAlignment = Alignment.Center
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

    private fun getFormattedDisplay(
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        format: CountdownDisplayFormat
    ): String {
        return when (format) {
            CountdownDisplayFormat.WEEKS_DAYS -> {
                val remainingDays = timeInfo.days % 7
                "${timeInfo.weeks} Wochen, $remainingDays Tage"
            }
            CountdownDisplayFormat.MONTHS_DAYS -> {
                val remainingDays = timeInfo.days % 30
                "${timeInfo.months} Monate, $remainingDays Tage"
            }
            CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                val remainingMonths = timeInfo.months % 12
                val remainingDays = timeInfo.days % 30
                "${timeInfo.years} Jahre, $remainingMonths Monate, $remainingDays Tage"
            }
            else -> ""
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