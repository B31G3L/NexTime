package de.beigel.nextime.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.widget.utils.WidgetHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Small Widget (2×2 / 160×160dp)
 * - Kompakte Ansicht
 * - Nur Titel und Hauptzahl
 * - Farbbalken oben und unten
 */
class SmallCountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val countdown = withContext(Dispatchers.IO) {
                WidgetHelper.getCountdownForWidget(context, id)
            }
            SmallWidgetContent(countdown, context)
        }
    }

    @Composable
    private fun SmallWidgetContent(countdown: Countdown?, context: Context) {
        if (countdown == null) {
            EmptySmallWidget(context)
            return
        }

        val timeInfo = countdown.calculateTimeRemaining()
        val color = WidgetHelper.parseColor(countdown.color)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(WidgetHelper.getSurfaceVariantColor(context)))
                .clickable(WidgetHelper.getAppOpenAction(context, countdown)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Farbbalken oben
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ColorProvider(color))
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Titel
            Text(
                text = countdown.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = ColorProvider(WidgetHelper.getOnSurfaceColor(context))
                ),
                maxLines = 1,
                modifier = GlanceModifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Hauptzahl
            Text(
                text = "${timeInfo.days}",
                style = TextStyle(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = ColorProvider(color)
                )
            )

            // Label
            Text(
                text = if (timeInfo.days == 1L) "Tag" else "Tage",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = ColorProvider(WidgetHelper.getOnSurfaceVariantColor(context))
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Farbbalken unten
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ColorProvider(color))
            )
        }
    }

    @Composable
    private fun EmptySmallWidget(context: Context) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(WidgetHelper.getSurfaceVariantColor(context))),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏰",
                style = TextStyle(fontSize = 32.sp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Kein Countdown",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(WidgetHelper.getOnSurfaceColor(context))
                )
            )
        }
    }
}

class SmallCountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallCountdownWidget()
}