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

/**
 * Mini Widget (1×1) - Ultra kompakt
 * Zeigt nur: Zahl + Farbbalken
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
                .background(Color(0xFFF5F5F5))
                .padding(4.dp),
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
                        style = TextStyle(fontSize = 24.sp)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Oberer Balken
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(baseColor),
                        contentAlignment = Alignment.Center
                    ) { }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Die große Zahl
                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(baseColor)
                        )
                    )

                    // Titel (sehr kurz und klein)
                    Text(
                        text = countdown.title.take(8),
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Normal,
                            color = ColorProvider(Color(0xFF666666))
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Unterer Balken
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(baseColor),
                        contentAlignment = Alignment.Center
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