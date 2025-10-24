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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

/**
 * Medium Widget (4×2 / 340×160dp)
 * - Standard-Ansicht mit allen wichtigen Infos
 * - Titel + Datum in Header
 * - Hauptzahl + Label
 * - Sublabel mit alternativer Darstellung
 */
class MediumCountdownWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val countdown = withContext(Dispatchers.IO) {
            WidgetHelper.getCountdownForWidget(context, id)
        }

        provideContent {
            MediumWidgetContent(countdown, context)
        }
    }

    @Composable
    private fun MediumWidgetContent(countdown: Countdown?, context: Context) {
        if (countdown == null) {
            EmptyMediumWidget(context)
            return
        }

        val timeInfo = countdown.calculateTimeRemaining()
        val color = WidgetHelper.parseColor(countdown.color)
        val format = try {
            CountdownDisplayFormat.valueOf(countdown.displayFormat)
        } catch (e: Exception) {
            CountdownDisplayFormat.DAYS_ONLY
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(WidgetHelper.getSurfaceVariantColor(context)))
                .clickable(WidgetHelper.getAppOpenAction(context, countdown)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Farbbalken oben
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ColorProvider(color))
            ) {}

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
                        fontSize = 16.sp,
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
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorProvider(WidgetHelper.getOnSurfaceVariantColor(context))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Hauptzähler
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${timeInfo.days}",
                    style = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = ColorProvider(color)
                    )
                )

                Text(
                    text = if (timeInfo.days == 1L) "Tag" else "Tage",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = ColorProvider(WidgetHelper.getOnSurfaceVariantColor(context))
                    )
                )

                Spacer(modifier = GlanceModifier.height(4.dp))

                // Sublabel (alternative Darstellung)
                Text(
                    text = getSubLabel(timeInfo, format),
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = ColorProvider(WidgetHelper.getOnSurfaceVariantColor(context))
                    )
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())
            Spacer(modifier = GlanceModifier.height(12.dp))

            // Farbbalken unten
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(ColorProvider(color))
            ) {}
        }
    }

    private fun getSubLabel(
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        format: CountdownDisplayFormat
    ): String {
        return when (format) {
            CountdownDisplayFormat.WEEKS_DAYS -> {
                val weeks = timeInfo.days / 7
                val remainingDays = timeInfo.days % 7
                "$weeks Wochen, $remainingDays Tage"
            }
            CountdownDisplayFormat.MONTHS_DAYS -> {
                val months = timeInfo.days / 30
                val remainingDays = timeInfo.days % 30
                "$months Monate, $remainingDays Tage"
            }
            CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                val years = timeInfo.days / 365
                val remainingMonths = (timeInfo.days % 365) / 30
                val remainingDays = (timeInfo.days % 365) % 30
                "$years Jahre, $remainingMonths Monate, $remainingDays Tage"
            }
            else -> {
                val weeks = timeInfo.days / 7
                val remainingDays = timeInfo.days % 7
                "$weeks Wochen, $remainingDays Tage"
            }
        }
    }

    @Composable
    private fun EmptyMediumWidget(context: Context) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(WidgetHelper.getSurfaceVariantColor(context))),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏰",
                style = TextStyle(fontSize = 40.sp)
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Kein Countdown ausgewählt",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(WidgetHelper.getOnSurfaceColor(context))
                )
            )
        }
    }
}

class MediumCountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumCountdownWidget()
}