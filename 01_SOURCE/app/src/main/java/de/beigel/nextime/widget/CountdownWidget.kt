package de.beigel.nextime.widget

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import de.beigel.nextime.data.model.calculateTimeRemaining
import kotlinx.coroutines.flow.first

/**
 * Small Widget (2×2) - Kompakte Anzeige
 * Design: Fast weißer Hintergrund (#FAFAFA) mit Farbakzenten
 * Zeigt: Titel, Hauptzahl, Label (z.B. "Tage")
 */
class CountdownSmallWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = CountdownDatabase.getDatabase(context)
        val countdowns = database.countdownDao().getAllCountdowns().first()

        val countdown = countdowns.firstOrNull()

        provideContent {
            SmallWidgetContent(countdown = countdown)
        }
    }

    @Composable
    private fun SmallWidgetContent(countdown: Countdown?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))  // Fast weiß
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
                        style = TextStyle(
                            fontSize = 32.sp,
                            color = ColorProvider(Color(0xFFBBBBBB))
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = "Kein Countdown",
                        style = TextStyle(
                            fontSize = 12.sp,
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
                    // Oberer Farbakzent
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Titel in dunkler Farbe
                    Text(
                        text = countdown.title.take(12),
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF333333))
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    // Hauptzahl in Akzentfarbe
                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(accentColor)
                        )
                    )

                    // Label in mittlerer Graufarbe
                    Text(
                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorProvider(Color(0xFF666666))
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Unterer Farbakzent
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
}

/**
 * Widget Receiver für Small Widget
 */
class CountdownSmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownSmallWidget()
}