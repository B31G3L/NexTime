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
 * Medium Widget (4×2) - Standard mit Datum & Format
 * Design: Fast weißer Hintergrund (#FAFAFA) mit Farbakzenten
 * Zeigt: Emoji, Titel, Datum, Große Zahl (formatabhängig), Format-Text
 */
class CountdownMediumWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = CountdownDatabase.getDatabase(context)
        val countdowns = database.countdownDao().getAllCountdowns().first()

        val countdown = countdowns.firstOrNull()

        provideContent {
            MediumWidgetContent(countdown = countdown)
        }
    }

    @Composable
    private fun MediumWidgetContent(countdown: Countdown?) {
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
                            fontSize = 40.sp,
                            color = ColorProvider(Color(0xFFBBBBBB))
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    Text(
                        text = "Kein Countdown",
                        style = TextStyle(
                            fontSize = 14.sp,
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
                    // Oberer Farbakzent - volle Breite
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
                        // Emoji
                        Text(
                            text = getEmojiForCountdown(countdown.title),
                            style = TextStyle(fontSize = 22.sp)
                        )
                        Spacer(modifier = GlanceModifier.width(10.dp))

                        // Titel
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = countdown.title,
                                style = TextStyle(
                                    fontSize = 16.sp,
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
                                fontSize = 12.sp,
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
                                        fontSize = 64.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(accentColor)
                                    )
                                )
                                Text(
                                    text = if (timeInfo.days == 1L) "Tag" else "Tage",
                                    style = TextStyle(
                                        fontSize = 16.sp,
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
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorProvider(accentColor)
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = if (timeInfo.weeks == 1L) "Woche" else "Wochen",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            color = ColorProvider(Color(0xFF333333))
                                        ),
                                        modifier = GlanceModifier.padding(bottom = 6.dp)
                                    )
                                }
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$remainingDays",
                                        style = TextStyle(
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorProvider(accentColor.copy(alpha = 0.7f))
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = if (remainingDays == 1L) "Tag" else "Tage",
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = ColorProvider(Color(0xFF333333))
                                        ),
                                        modifier = GlanceModifier.padding(bottom = 2.dp)
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
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorProvider(accentColor)
                                        )
                                    )
                                    Spacer(modifier = GlanceModifier.width(6.dp))
                                    Text(
                                        text = if (timeInfo.months == 1L) "Monat" else "Monate",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            color = ColorProvider(Color(0xFF333333))
                                        ),
                                        modifier = GlanceModifier.padding(bottom = 6.dp)
                                    )
                                }
                                if (remainingDays > 0) {
                                    Spacer(modifier = GlanceModifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.Bottom,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "$remainingDays",
                                            style = TextStyle(
                                                fontSize = 32.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorProvider(accentColor.copy(alpha = 0.7f))
                                            )
                                        )
                                        Spacer(modifier = GlanceModifier.width(6.dp))
                                        Text(
                                            text = if (remainingDays == 1L) "Tag" else "Tage",
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = ColorProvider(Color(0xFF333333))
                                            ),
                                            modifier = GlanceModifier.padding(bottom = 2.dp)
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
                                                fontSize = 38.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorProvider(accentColor)
                                            )
                                        )
                                        Text(
                                            text = "J ",
                                            style = TextStyle(
                                                fontSize = 14.sp,
                                                color = ColorProvider(Color(0xFF333333))
                                            ),
                                            modifier = GlanceModifier.padding(bottom = 4.dp)
                                        )
                                    }
                                    if (timeInfo.months > 0 || timeInfo.years > 0) {
                                        val remainingMonths = timeInfo.months % 12
                                        if (remainingMonths > 0) {
                                            Text(
                                                text = "$remainingMonths",
                                                style = TextStyle(
                                                    fontSize = 38.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ColorProvider(accentColor)
                                                )
                                            )
                                            Text(
                                                text = "M ",
                                                style = TextStyle(
                                                    fontSize = 14.sp,
                                                    color = ColorProvider(Color(0xFF333333))
                                                ),
                                                modifier = GlanceModifier.padding(bottom = 4.dp)
                                            )
                                        }
                                    }
                                    val remainingDays = timeInfo.days % 30
                                    Text(
                                        text = "$remainingDays",
                                        style = TextStyle(
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ColorProvider(accentColor)
                                        )
                                    )
                                    Text(
                                        text = "T",
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

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Unterer Farbakzent - volle Breite
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
}

/**
 * Widget Receiver für Medium Widget
 */
class CountdownMediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownMediumWidget()
}