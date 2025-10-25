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
import de.beigel.nextime.data.model.calculateTimeRemaining
import kotlinx.coroutines.flow.first

/**
 * Mini Widget (1×1) - Ultra kompakt
 * Design: Fast weißer Hintergrund (#FAFAFA) mit Farbakzenten
 * Zeigt nur: Zahl + Farbbalken oben/unten + Label
 */
class CountdownMiniWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = CountdownDatabase.getDatabase(context)
        val countdowns = database.countdownDao().getAllCountdowns().first()

        val countdown = countdowns.firstOrNull()

        provideContent {
            MiniWidgetContent(countdown = countdown)
        }
    }

    @Composable
    private fun MiniWidgetContent(countdown: Countdown?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),  // Fast weiß
            contentAlignment = Alignment.Center
        ) {
            if (countdown == null) {
                // Kein Countdown - dezentes Icon
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏰",
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = ColorProvider(Color(0xFFBBBBBB))
                        )
                    )
                }
            } else {
                // Countdown vorhanden
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
                            .height(3.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Hauptzahl in Akzentfarbe
                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(accentColor)
                        )
                    )

                    // Label in dunkler Textfarbe
                    Text(
                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                        style = TextStyle(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorProvider(Color(0xFF666666))
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Unterer Farbakzent - volle Breite
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }
                }
            }
        }
    }
}

/**
 * Widget Receiver für Mini Widget
 */
class CountdownMiniWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownMiniWidget()
}