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
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.widget.utils.WidgetHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Large Widget (4×3 / 340×240dp)
 * - Vollständige Ansicht mit allen Details
 * - Titel + Datum
 * - Hauptzähler
 * - Statistiken-Box
 * - Fortschrittsbalken
 */
class LargeCountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val countdown = WidgetHelper.getCountdownForWidget(context, id)
            LargeWidgetContent(countdown, context)
        }
    }

    @Composable
    private fun LargeWidgetContent(countdown: Countdown?, context: Context) {
        if (countdown == null) {
            EmptyLargeWidget()
            return
        }

        val timeInfo = countdown.calculateTimeRemaining()
        val color = WidgetHelper.parseColor(countdown.color)
        val progress = calculateProgress(countdown)

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(androidx.glance.R.color.widget_background_color)
                .clickable(WidgetHelper.getAppOpenAction(context, countdown))
        ) {
            // Farbbalken oben
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ColorProvider(color))
            )

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Header: Titel + Datum
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = countdown.title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(WidgetHelper.getOnSurfaceColor(context))
                    ),
                    maxLines = 1,
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Text(
                    text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorProvider(WidgetHelper.getOnSurfaceVariantColor(context))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Hauptzähler
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${timeInfo.days}",
                    style = TextStyle(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = ColorProvider(color)
                    )
                )

                Text(
                    text = if (timeInfo.days == 1L) "Tag" else "Tage",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = ColorProvider(WidgetHelper.getOnSurfaceVariantColor(context))
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Sublabel
                val weeks = timeInfo.days / 7
                val remainingDays = timeInfo.days % 7
                Text(
                    text = "$weeks Wochen, $remainingDays Tage",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = ColorProvider(WidgetHelper.getOnSurfaceVariantColor(context))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            // Statistiken-Box
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(ColorProvider(WidgetHelper.getSurfaceVariantColor(context)))
                    .padding(10.dp)
            ) {
                StatRow(
                    label = "Erstellt:",
                    value = countdown.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                    context = context
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                StatRow(
                    label = "Verbleibend:",
                    value = "${progress.toInt()}%",
                    context = context
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Fortschrittsbalken
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(horizontal = 16.dp)
                    .background(ColorProvider(WidgetHelper.getSurfaceVariantColor(context)))
            ) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .width((340 * (1 - progress / 100f)).dp)
                        .background(ColorProvider(color))
                )
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

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
    private fun StatRow(label: String, value: String, context: Context) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = ColorProvider(WidgetHelper.getOnSurfaceVariantColor(context))
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            Text(
                text = value,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(WidgetHelper.getOnSurfaceColor(context))
                )
            )
        }
    }

    private fun calculateProgress(countdown: Countdown): Float {
        val now = LocalDateTime.now()
        val total = ChronoUnit.DAYS.between(countdown.createdAt, countdown.targetDateTime).toFloat()
        val elapsed = ChronoUnit.DAYS.between(countdown.createdAt, now).toFloat()

        if (total <= 0) return 100f
        val remaining = ((total - elapsed) / total * 100f).coerceIn(0f, 100f)
        return remaining
    }

    @Composable
    private fun EmptyLargeWidget() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(androidx.glance.R.color.widget_background_color),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏰",
                style = TextStyle(fontSize = 48.sp)
            )
            Spacer(modifier = GlanceModifier.height(12.dp))
            Text(
                text = "Kein Countdown ausgewählt",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Tippe hier, um einen auszuwählen",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

class LargeCountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeCountdownWidget()
}
