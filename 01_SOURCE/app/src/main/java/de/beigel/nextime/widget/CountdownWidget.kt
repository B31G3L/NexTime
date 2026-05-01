package de.beigel.nextime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import de.beigel.nextime.MainActivity
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.RecurrenceType
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.data.model.formatTime
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter

class CountdownWidget : GlanceAppWidget() {

    companion object {
        // Größen-Breakpoints
        private val SIZE_SMALL  = DpSize(60.dp,  60.dp)   // 1×1
        private val SIZE_TALL   = DpSize(60.dp, 120.dp)   // 1×2
        private val SIZE_WIDE   = DpSize(130.dp, 60.dp)   // 2×1
        private val SIZE_MEDIUM = DpSize(130.dp, 120.dp)  // 2×2
        private val SIZE_LARGE  = DpSize(200.dp, 120.dp)  // 3×2
        private val SIZE_XLARGE = DpSize(270.dp, 120.dp)  // 4×2
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SIZE_SMALL, SIZE_TALL, SIZE_WIDE, SIZE_MEDIUM, SIZE_LARGE, SIZE_XLARGE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val glanceManager = GlanceAppWidgetManager(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val appWidgetId = try {
            glanceManager.getAppWidgetId(id)
        } catch (e: Exception) {
            appWidgetManager
                .getAppWidgetIds(ComponentName(context, CountdownWidgetReceiver::class.java))
                .firstOrNull() ?: AppWidgetManager.INVALID_APPWIDGET_ID
        }

        val database = CountdownDatabase.getDatabase(context)
        val selectedCountdownId = WidgetConfigActivity.loadCountdownId(context, appWidgetId)

        val countdown = if (selectedCountdownId != null) {
            database.countdownDao().getCountdownById(selectedCountdownId)
        } else {
            database.countdownDao().getAllCountdowns().first().firstOrNull()
        }

        provideContent {
            val size = LocalSize.current
            val clickAction = actionStartActivity<MainActivity>()

            // Kein Countdown → leerer Zustand
            if (countdown == null) {
                EmptyWidget(size, clickAction)
                return@provideContent
            }

            val timeInfo = countdown.calculateTimeRemaining()
            val accentColor = parseColor(countdown.color)

            // Responsiv nach Breite/Höhe
            when {
                size.width >= SIZE_XLARGE.width -> XLargeLayout(countdown, timeInfo, accentColor, clickAction)
                size.width >= SIZE_LARGE.width  -> LargeLayout(countdown, timeInfo, accentColor, clickAction)
                size.width >= SIZE_MEDIUM.width && size.height >= SIZE_MEDIUM.height -> MediumLayout(countdown, timeInfo, accentColor, clickAction)
                size.width >= SIZE_WIDE.width   -> WideLayout(countdown, timeInfo, accentColor, clickAction)
                size.height >= SIZE_TALL.height -> TallLayout(countdown, timeInfo, accentColor, clickAction)
                else                            -> SmallLayout(countdown, timeInfo, accentColor, clickAction)
            }
        }
    }

    // ─── 1×1: Icon + Zahl ────────────────────────────────────────────────────
    @Composable
    private fun SmallLayout(
        countdown: Countdown,
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        accentColor: Color,
        clickAction: androidx.glance.action.Action
    ) {
        val darkerColor = darken(accentColor)
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(accentColor)
                .cornerRadius(16.dp)
                .clickable(clickAction),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically,
                modifier = GlanceModifier.fillMaxSize().padding(4.dp)
            ) {
                // Farbbalken oben
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = countdown.icon.ifBlank { "⏰" },
                    style = TextStyle(fontSize = 22.sp, textAlign = TextAlign.Center)
                )
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "${timeInfo.days}",
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White), textAlign = TextAlign.Center)
                )
                Text(
                    text = if (timeInfo.days == 1L) "Tag" else "Tage",
                    style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)), textAlign = TextAlign.Center)
                )
                Spacer(GlanceModifier.defaultWeight())
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
            }
        }
    }

    // ─── 1×2: Icon + Titel + Zahl ────────────────────────────────────────────
    @Composable
    private fun TallLayout(
        countdown: Countdown,
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        accentColor: Color,
        clickAction: androidx.glance.action.Action
    ) {
        val darkerColor = darken(accentColor)
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(accentColor)
                .cornerRadius(16.dp)
                .clickable(clickAction)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    text = countdown.icon.ifBlank { "⏰" },
                    style = TextStyle(fontSize = 26.sp, textAlign = TextAlign.Center)
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = countdown.title,
                    style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f)), textAlign = TextAlign.Center),
                    maxLines = 1
                )
                Spacer(GlanceModifier.height(6.dp))
                Text(
                    text = "${timeInfo.days}",
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White), textAlign = TextAlign.Center)
                )
                Text(
                    text = if (timeInfo.days == 1L) "Tag" else "Tage",
                    style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)), textAlign = TextAlign.Center)
                )
                Spacer(GlanceModifier.defaultWeight())
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
            }
        }
    }

    // ─── 2×1: Icon + Titel + Zahl + Datum ────────────────────────────────────
    @Composable
    private fun WideLayout(
        countdown: Countdown,
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        accentColor: Color,
        clickAction: androidx.glance.action.Action
    ) {
        val darkerColor = darken(accentColor)
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(accentColor)
                .cornerRadius(16.dp)
                .clickable(clickAction)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon-Box
                    Box(
                        modifier = GlanceModifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                            .cornerRadius(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = countdown.icon.ifBlank { "⏰" }, style = TextStyle(fontSize = 22.sp))
                    }
                    Spacer(GlanceModifier.width(10.dp))
                    // Text
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = countdown.title,
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f))),
                            maxLines = 1
                        )
                        Spacer(GlanceModifier.height(2.dp))
                        Text(
                            text = "${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"}",
                            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White))
                        )
                        Text(
                            text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color.White.copy(alpha = 0.65f)))
                        )
                    }
                }
            }
        }
    }

    // ─── 2×2: Vollständige Card ───────────────────────────────────────────────
    @Composable
    private fun MediumLayout(
        countdown: Countdown,
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        accentColor: Color,
        clickAction: androidx.glance.action.Action
    ) {
        val darkerColor = darken(accentColor)
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(accentColor)
                .cornerRadius(16.dp)
                .clickable(clickAction)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f))
                                .cornerRadius(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = countdown.icon.ifBlank { "⏰" }, style = TextStyle(fontSize = 18.sp))
                        }
                        Spacer(GlanceModifier.width(8.dp))
                        Text(
                            text = countdown.title,
                            style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f))),
                            maxLines = 1,
                            modifier = GlanceModifier.defaultWeight()
                        )
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        text = formatMainValue(timeInfo, countdown.displayFormat),
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White))
                    )
                    Text(
                        text = formatMainUnit(timeInfo, countdown.displayFormat),
                        style = TextStyle(fontSize = 13.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)))
                    )
                    if (countdown.includeTime) {
                        Text(
                            text = timeInfo.formatTime(),
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.9f)))
                        )
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    Text(
                        text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                        style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color.White.copy(alpha = 0.6f)))
                    )
                }
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
            }
        }
    }

    // ─── 3×2: Groß mit Subinfo ───────────────────────────────────────────────
    @Composable
    private fun LargeLayout(
        countdown: Countdown,
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        accentColor: Color,
        clickAction: androidx.glance.action.Action
    ) {
        val darkerColor = darken(accentColor)
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(accentColor)
                .cornerRadius(16.dp)
                .clickable(clickAction)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon links
                    Box(
                        modifier = GlanceModifier
                            .size(52.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                            .cornerRadius(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = countdown.icon.ifBlank { "⏰" }, style = TextStyle(fontSize = 26.sp))
                    }
                    Spacer(GlanceModifier.width(12.dp))
                    // Inhalt rechts
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = countdown.title,
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f))),
                            maxLines = 1
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = formatMainValue(timeInfo, countdown.displayFormat),
                            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White))
                        )
                        Text(
                            text = formatMainUnit(timeInfo, countdown.displayFormat),
                            style = TextStyle(fontSize = 14.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)))
                        )
                        if (countdown.includeTime) {
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                text = timeInfo.formatTime(),
                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.9f)))
                            )
                        }
                        Spacer(GlanceModifier.height(4.dp))
                        Row {
                            Text(
                                text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.65f)))
                            )
                            if (countdown.isRecurring) {
                                Spacer(GlanceModifier.width(6.dp))
                                Text(
                                    text = "↻ ${countdown.recurrenceType.displayName}",
                                    style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.65f)))
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ─── 4×2: Extra groß mit allen Details ───────────────────────────────────
    @Composable
    private fun XLargeLayout(
        countdown: Countdown,
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        accentColor: Color,
        clickAction: androidx.glance.action.Action
    ) {
        val darkerColor = darken(accentColor)
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(accentColor)
                .cornerRadius(16.dp)
                .clickable(clickAction)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
                Row(
                    modifier = GlanceModifier.fillMaxSize().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(60.dp)
                            .background(Color.White.copy(alpha = 0.2f))
                            .cornerRadius(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = countdown.icon.ifBlank { "⏰" }, style = TextStyle(fontSize = 30.sp))
                    }
                    Spacer(GlanceModifier.width(14.dp))
                    Column(
                        modifier = GlanceModifier.defaultWeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = countdown.title,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f))),
                            maxLines = 1
                        )
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = formatMainValue(timeInfo, countdown.displayFormat),
                            style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White))
                        )
                        Text(
                            text = formatMainUnit(timeInfo, countdown.displayFormat),
                            style = TextStyle(fontSize = 16.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)))
                        )
                        if (countdown.includeTime) {
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                text = timeInfo.formatTime(),
                                style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.9f)))
                            )
                        }
                    }
                    Spacer(GlanceModifier.width(16.dp))
                    // Rechte Spalte mit Extrainfos
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd. MMM yyyy")),
                            style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.White.copy(alpha = 0.75f)))
                        )
                        if (countdown.includeTime) {
                            Spacer(GlanceModifier.height(2.dp))
                            Text(
                                text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr",
                                style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.White.copy(alpha = 0.75f)))
                            )
                        }
                        if (countdown.isRecurring) {
                            Spacer(GlanceModifier.height(4.dp))
                            Text(
                                text = "↻ ${countdown.recurrenceType.displayName}",
                                style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.65f)))
                            )
                        }
                        Spacer(GlanceModifier.height(4.dp))
                        Text(
                            text = "${timeInfo.days} Tage gesamt",
                            style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.6f)))
                        )
                    }
                }
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darkerColor)) {}
            }
        }
    }

    // ─── Leerer Zustand ───────────────────────────────────────────────────────
    @Composable
    private fun EmptyWidget(
        size: DpSize,
        clickAction: androidx.glance.action.Action
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF2A2A2A))
                .cornerRadius(16.dp)
                .clickable(clickAction),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⏰", style = TextStyle(fontSize = 24.sp, textAlign = TextAlign.Center))
                if (size.height > 60.dp) {
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        text = "Kein Countdown",
                        style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFF888888)), textAlign = TextAlign.Center)
                    )
                }
            }
        }
    }

    // ─── Hilfsfunktionen ─────────────────────────────────────────────────────

    private fun formatMainValue(
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        displayFormat: String
    ): String {
        val format = try { CountdownDisplayFormat.valueOf(displayFormat) }
        catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }
        return when (format) {
            CountdownDisplayFormat.DAYS_ONLY         -> "${timeInfo.days}"
            CountdownDisplayFormat.WEEKS_DAYS        -> "${timeInfo.weeks}"
            CountdownDisplayFormat.MONTHS_DAYS       -> "${timeInfo.months}"
            CountdownDisplayFormat.YEARS_MONTHS_DAYS -> if (timeInfo.years > 0) "${timeInfo.years}" else "${timeInfo.remainingMonthsAfterYears}"
        }
    }

    private fun formatMainUnit(
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        displayFormat: String
    ): String {
        val format = try { CountdownDisplayFormat.valueOf(displayFormat) }
        catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }
        return when (format) {
            CountdownDisplayFormat.DAYS_ONLY         -> if (timeInfo.days == 1L) "Tag" else "Tage"
            CountdownDisplayFormat.WEEKS_DAYS        -> "${if (timeInfo.weeks == 1L) "Woche" else "Wochen"}, ${timeInfo.remainingDaysAfterWeeks} T"
            CountdownDisplayFormat.MONTHS_DAYS       -> "${if (timeInfo.months == 1L) "Monat" else "Monate"}, ${timeInfo.remainingDaysAfterMonths} T"
            CountdownDisplayFormat.YEARS_MONTHS_DAYS -> if (timeInfo.years > 0) "${if (timeInfo.years == 1L) "Jahr" else "Jahre"}, ${timeInfo.remainingMonthsAfterYears} M"
            else "${if (timeInfo.remainingMonthsAfterYears == 1L) "Monat" else "Monate"}, ${timeInfo.remainingDaysAfterYears} T"
        }
    }

    private fun parseColor(colorHex: String): Color {
        return try { Color(android.graphics.Color.parseColor(colorHex)) }
        catch (e: Exception) { Color(0xFFFF7043) }
    }

    private fun darken(color: Color): Color = color.copy(
        red   = (color.red * 0.7f).coerceIn(0f, 1f),
        green = (color.green * 0.7f).coerceIn(0f, 1f),
        blue  = (color.blue * 0.7f).coerceIn(0f, 1f)
    )
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            WidgetConfigActivity.deleteCountdownId(context, appWidgetId)
        }
    }
}