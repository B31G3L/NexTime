package de.beigel.nextime.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
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
 * Small Widget (2×2) - Kompakte Anzeige
 * Zeigt: Icon, Titel, Hauptzahl, Label (z.B. "Tage")
 */
class CountdownSmallWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = CountdownDatabase.getDatabase(context)
        val countdowns = database.countdownDao().getAllCountdowns().first()

        // Nimm den ersten Countdown oder null
        val countdown = countdowns.firstOrNull()

        provideContent {
            SmallWidgetContent(countdown = countdown)
        }
    }

    @Composable
    private fun SmallWidgetContent(countdown: Countdown?) {
        // Container mit abgerundeten Ecken und Hintergrund
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (countdown == null) {
                // Kein Countdown vorhanden
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏰",
                        style = TextStyle(
                            fontSize = 32.sp
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "Kein Countdown",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = ColorProvider(Color(0xFF666666))
                        )
                    )
                }
            } else {
                // Countdown vorhanden
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
                    // Oberer Balken (Farbe)
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(baseColor),
                        contentAlignment = Alignment.Center
                    ) { }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Titel (gekürzt für Small Widget)
                    Text(
                        text = countdown.title.take(12),
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF333333))
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Hauptzahl - Format abhängig
                    val format = try {
                        CountdownDisplayFormat.valueOf(countdown.displayFormat)
                    } catch (e: Exception) {
                        CountdownDisplayFormat.DAYS_ONLY
                    }

                    when (format) {
                        CountdownDisplayFormat.DAYS_ONLY -> {
                            // Große Zahl
                            Text(
                                text = "${timeInfo.days}",
                                style = TextStyle(
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(baseColor)
                                )
                            )
                            // Label
                            Text(
                                text = if (timeInfo.days == 1L) "Tag" else "Tage",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF666666))
                                )
                            )
                        }
                        else -> {
                            // Für andere Formate: vereinfachte Darstellung
                            Text(
                                text = "${timeInfo.days}",
                                style = TextStyle(
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(baseColor)
                                )
                            )
                            Text(
                                text = "Tage",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF666666))
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Unterer Balken
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(baseColor),
                        contentAlignment = Alignment.Center
                    ) { }
                }
            }
        }
    }
}

/**
 * Widget Receiver für Small Widget
 */
class CountdownSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownSmallWidget()
}