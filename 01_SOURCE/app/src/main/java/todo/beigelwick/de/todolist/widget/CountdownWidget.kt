package todo.beigelwick.de.todolist.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.Action
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
import kotlinx.coroutines.flow.first
import todo.beigelwick.de.todolist.MainActivity
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.database.CountdownDatabase
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.CountdownInfo
import todo.beigelwick.de.todolist.data.model.DisplayFormat
import todo.beigelwick.de.todolist.data.model.DisplayUnit
import todo.beigelwick.de.todolist.data.model.buildDisplaySegments
import todo.beigelwick.de.todolist.data.model.calculateTimeRemaining
import todo.beigelwick.de.todolist.ui.theme.AppPreferences
import java.time.format.DateTimeFormatter

class CountdownWidget : GlanceAppWidget() {

    companion object {
        private val SIZE_SMALL  = DpSize(60.dp,  60.dp)
        private val SIZE_TALL   = DpSize(60.dp,  120.dp)
        private val SIZE_WIDE   = DpSize(130.dp, 60.dp)
        private val SIZE_MEDIUM = DpSize(130.dp, 120.dp)
        private val SIZE_LARGE  = DpSize(200.dp, 120.dp)
        private val SIZE_XLARGE = DpSize(270.dp, 120.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SIZE_SMALL, SIZE_TALL, SIZE_WIDE, SIZE_MEDIUM, SIZE_LARGE, SIZE_XLARGE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val glanceManager    = GlanceAppWidgetManager(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)

        val appWidgetId = try {
            glanceManager.getAppWidgetId(id)
        } catch (e: Exception) {
            AppWidgetManager.INVALID_APPWIDGET_ID
        }

        val selectedCountdownId = if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            WidgetConfigActivity.loadCountdownId(context, appWidgetId)
        } else null

        val database  = CountdownDatabase.getDatabase(context)
        val countdown = if (selectedCountdownId != null) {
            database.countdownDao().getCountdownById(selectedCountdownId)
        } else {
            database.countdownDao().getAllCountdowns().first().firstOrNull()
        }

        val globalDateUnits  = AppPreferences.getDefaultDateUnits(context).first()
        val showTimeOnWidget = AppPreferences.getShowTimeOnCard(context).first()
        val resolvedFormat   = buildWidgetFormat(globalDateUnits, showTimeOnWidget)

        provideContent {
            val size        = LocalSize.current
            val clickAction = actionStartActivity<MainActivity>()

            if (countdown == null) {
                EmptyWidget(context, size, clickAction)
                return@provideContent
            }

            val timeInfo    = countdown.calculateTimeRemaining()
            val accentColor = parseColor(countdown.color)

            when {
                size.width >= SIZE_XLARGE.width                                      -> XLargeLayout(context, countdown, timeInfo, accentColor, clickAction, resolvedFormat)
                size.width >= SIZE_LARGE.width                                       -> LargeLayout(context, countdown, timeInfo, accentColor, clickAction, resolvedFormat)
                size.width >= SIZE_MEDIUM.width && size.height >= SIZE_MEDIUM.height -> MediumLayout(context, countdown, timeInfo, accentColor, clickAction, resolvedFormat)
                size.width >= SIZE_WIDE.width                                        -> WideLayout(context, countdown, timeInfo, accentColor, clickAction, resolvedFormat)
                size.height >= SIZE_TALL.height                                      -> TallLayout(context, countdown, timeInfo, accentColor, clickAction, resolvedFormat)
                else                                                                 -> SmallLayout(context, countdown, timeInfo, accentColor, clickAction, resolvedFormat)
            }
        }
    }

    // ─── Icon-Box (ersetzt Emoji-Text) ────────────────────────────────────────
    // Glance unterstützt keine Material ImageVector-Icons; wir nutzen daher
    // das App-Drawable als generisches Widget-Icon.

    @Composable
    private fun WidgetIconBox(accentColor: Color, size: Int = 44) {
        Box(
            modifier         = GlanceModifier
                .size(size.dp)
                .background(Color.White.copy(alpha = 0.2f))
                .cornerRadius(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider           = ImageProvider(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier           = GlanceModifier.size((size * 0.6f).toInt().dp)
            )
        }
    }

    // ─── 1×1 ─────────────────────────────────────────────────────────────────

    @Composable
    private fun SmallLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, clickAction: Action, displayFormat: String) {
        val darker   = darken(accentColor)
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitShort(context, timeInfo, displayFormat)

        Box(modifier = GlanceModifier.fillMaxSize().background(accentColor).cornerRadius(16.dp).clickable(clickAction), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxSize().padding(4.dp)) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
                Spacer(GlanceModifier.defaultWeight())
                WidgetIconBox(accentColor, 28)
                Spacer(GlanceModifier.height(2.dp))
                Text(text = mainVal, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White), textAlign = TextAlign.Center))
                Text(text = mainUnit, style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)), textAlign = TextAlign.Center))
                Spacer(GlanceModifier.defaultWeight())
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
            }
        }
    }

    // ─── 1×2 ─────────────────────────────────────────────────────────────────

    @Composable
    private fun TallLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, clickAction: Action, displayFormat: String) {
        val darker   = darken(accentColor)
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitShort(context, timeInfo, displayFormat)

        Box(modifier = GlanceModifier.fillMaxSize().background(accentColor).cornerRadius(16.dp).clickable(clickAction)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = GlanceModifier.fillMaxSize()) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
                Spacer(GlanceModifier.defaultWeight())
                WidgetIconBox(accentColor, 32)
                Spacer(GlanceModifier.height(4.dp))
                Text(text = countdown.title, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f)), textAlign = TextAlign.Center), maxLines = 1)
                Spacer(GlanceModifier.height(6.dp))
                Text(text = mainVal, style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White), textAlign = TextAlign.Center))
                Text(text = mainUnit, style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)), textAlign = TextAlign.Center))
                Spacer(GlanceModifier.defaultWeight())
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
            }
        }
    }

    // ─── 2×1 ─────────────────────────────────────────────────────────────────

    @Composable
    private fun WideLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, clickAction: Action, displayFormat: String) {
        val darker   = darken(accentColor)
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitShort(context, timeInfo, displayFormat)

        Box(modifier = GlanceModifier.fillMaxSize().background(accentColor).cornerRadius(16.dp).clickable(clickAction)) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
                Row(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    WidgetIconBox(accentColor, 44)
                    Spacer(GlanceModifier.width(10.dp))
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(text = countdown.title, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f))), maxLines = 1)
                        Spacer(GlanceModifier.height(2.dp))
                        Text(text = "$mainVal $mainUnit", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
                        Text(text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color.White.copy(alpha = 0.65f))))
                    }
                }
            }
        }
    }

    // ─── 2×2 ─────────────────────────────────────────────────────────────────

    @Composable
    private fun MediumLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, clickAction: Action, displayFormat: String) {
        val darker   = darken(accentColor)
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitFull(context, timeInfo, displayFormat)

        Box(modifier = GlanceModifier.fillMaxSize().background(accentColor).cornerRadius(16.dp).clickable(clickAction)) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
                Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WidgetIconBox(accentColor, 36)
                        Spacer(GlanceModifier.width(8.dp))
                        Text(text = countdown.title, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f))), maxLines = 1, modifier = GlanceModifier.defaultWeight())
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    Text(text = mainVal, style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
                    Text(text = mainUnit, style = TextStyle(fontSize = 13.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f))))
                    if (countdown.hasTime) {
                        Text(text = "%02d:%02d:%02d".format(timeInfo.hours, timeInfo.minutes, timeInfo.seconds), style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.9f))))
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    Text(text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), style = TextStyle(fontSize = 9.sp, color = ColorProvider(Color.White.copy(alpha = 0.6f))))
                }
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
            }
        }
    }

    // ─── 3×2 ─────────────────────────────────────────────────────────────────

    @Composable
    private fun LargeLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, clickAction: Action, displayFormat: String) {
        val darker   = darken(accentColor)
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitFull(context, timeInfo, displayFormat)

        Box(modifier = GlanceModifier.fillMaxSize().background(accentColor).cornerRadius(16.dp).clickable(clickAction)) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
                Row(modifier = GlanceModifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    WidgetIconBox(accentColor, 52)
                    Spacer(GlanceModifier.width(12.dp))
                    Column(modifier = GlanceModifier.defaultWeight(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = countdown.title, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f))), maxLines = 1)
                        Spacer(GlanceModifier.height(4.dp))
                        Text(text = mainVal, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
                        Text(text = mainUnit, style = TextStyle(fontSize = 14.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f))))
                        if (countdown.hasTime) {
                            Spacer(GlanceModifier.height(2.dp))
                            Text(text = "%02d:%02d:%02d".format(timeInfo.hours, timeInfo.minutes, timeInfo.seconds), style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.9f))))
                        }
                        Spacer(GlanceModifier.height(4.dp))
                        Row {
                            Text(text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")), style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.65f))))
                            if (countdown.isRecurring) {
                                Spacer(GlanceModifier.width(6.dp))
                                Text(text = "${countdown.recurrenceType.name.lowercase()}", style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.65f))))
                            }
                        }
                    }
                }
            }
        }
    }

    // ─── 4×2 ─────────────────────────────────────────────────────────────────

    @Composable
    private fun XLargeLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, clickAction: Action, displayFormat: String) {
        val darker   = darken(accentColor)
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitFull(context, timeInfo, displayFormat)

        Box(modifier = GlanceModifier.fillMaxSize().background(accentColor).cornerRadius(16.dp).clickable(clickAction)) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
                Row(modifier = GlanceModifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    WidgetIconBox(accentColor, 60)
                    Spacer(GlanceModifier.width(14.dp))
                    Column(modifier = GlanceModifier.defaultWeight(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = countdown.title, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.85f))), maxLines = 1)
                        Spacer(GlanceModifier.height(4.dp))
                        Text(text = mainVal, style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White)))
                        Text(text = mainUnit, style = TextStyle(fontSize = 16.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f))))
                        if (countdown.hasTime) {
                            Spacer(GlanceModifier.height(2.dp))
                            Text(text = "%02d:%02d:%02d".format(timeInfo.hours, timeInfo.minutes, timeInfo.seconds), style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White.copy(alpha = 0.9f))))
                        }
                    }
                    Spacer(GlanceModifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd. MMM yyyy")), style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.White.copy(alpha = 0.75f))))
                        if (countdown.hasTime) {
                            Spacer(GlanceModifier.height(2.dp))
                            Text(text = countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr", style = TextStyle(fontSize = 11.sp, color = ColorProvider(Color.White.copy(alpha = 0.75f))))
                        }
                        if (countdown.isRecurring) {
                            Spacer(GlanceModifier.height(4.dp))
                            Text(text = countdown.recurrenceType.name.lowercase(), style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.65f))))
                        }
                        Spacer(GlanceModifier.height(4.dp))
                        val totalDaysLabel = if (timeInfo.days == 1L) context.getString(R.string.day) else context.getString(R.string.days)
                        Text(text = "${timeInfo.days} $totalDaysLabel ${context.getString(R.string.label_total)}", style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.6f))))
                    }
                }
                Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(darker)) {}
            }
        }
    }

    // ─── Leer ─────────────────────────────────────────────────────────────────

    @Composable
    private fun EmptyWidget(context: Context, size: DpSize, clickAction: Action) {
        Box(
            modifier         = GlanceModifier.fillMaxSize().background(Color(0xFF2A2A2A)).cornerRadius(16.dp).clickable(clickAction),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider           = ImageProvider(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier           = GlanceModifier.size(32.dp)
                )
                if (size.height > 60.dp) {
                    Spacer(GlanceModifier.height(4.dp))
                    Text(text = context.getString(R.string.widget_no_countdowns), style = TextStyle(fontSize = 10.sp, color = ColorProvider(Color(0xFF888888)), textAlign = TextAlign.Center))
                }
            }
        }
    }

    // ─── Hilfsfunktionen ──────────────────────────────────────────────────────

    private fun formatMainValue(timeInfo: CountdownInfo, displayFormat: String): String {
        val units = DisplayFormat.decodeOrdered(displayFormat)
        return when (units.firstOrNull() ?: DisplayUnit.DAYS) {
            DisplayUnit.YEARS   -> "${timeInfo.years}"
            DisplayUnit.MONTHS  -> "${timeInfo.months}"
            DisplayUnit.WEEKS   -> "${timeInfo.weeks}"
            DisplayUnit.DAYS    -> "${timeInfo.days}"
            DisplayUnit.HOURS   -> "${timeInfo.totalHours}"
            DisplayUnit.MINUTES -> "${timeInfo.totalMinutes}"
            DisplayUnit.SECONDS -> "${timeInfo.totalSeconds}"
        }
    }

    private fun formatMainUnitShort(context: Context, timeInfo: CountdownInfo, displayFormat: String): String {
        val units = DisplayFormat.decodeOrdered(displayFormat)
        val first = units.firstOrNull() ?: DisplayUnit.DAYS
        return getUnitLabel(context, first, getFirstValue(timeInfo, first))
    }

    private fun formatMainUnitFull(context: Context, timeInfo: CountdownInfo, displayFormat: String): String {
        val units    = DisplayFormat.decodeOrdered(displayFormat)
        val segments = timeInfo.buildDisplaySegments(units)
        if (segments.size == 1) {
            return getUnitLabel(context, segments[0].unit, segments[0].value)
        }
        val firstLabel = getUnitLabel(context, segments[0].unit, segments[0].value)
        val rest = segments.drop(1).joinToString(", ") { seg ->
            "${seg.value} ${getUnitLabel(context, seg.unit, seg.value)}"
        }
        return "$firstLabel, $rest"
    }

    private fun getFirstValue(timeInfo: CountdownInfo, unit: DisplayUnit): Long = when (unit) {
        DisplayUnit.YEARS   -> timeInfo.years
        DisplayUnit.MONTHS  -> timeInfo.months
        DisplayUnit.WEEKS   -> timeInfo.weeks
        DisplayUnit.DAYS    -> timeInfo.days
        DisplayUnit.HOURS   -> timeInfo.totalHours
        DisplayUnit.MINUTES -> timeInfo.totalMinutes
        DisplayUnit.SECONDS -> timeInfo.totalSeconds
    }

    private fun getUnitLabel(context: Context, unit: DisplayUnit, value: Long): String = when (unit) {
        DisplayUnit.YEARS   -> if (value == 1L) context.getString(R.string.year)   else context.getString(R.string.years)
        DisplayUnit.MONTHS  -> if (value == 1L) context.getString(R.string.month)  else context.getString(R.string.months)
        DisplayUnit.WEEKS   -> if (value == 1L) context.getString(R.string.week)   else context.getString(R.string.weeks)
        DisplayUnit.DAYS    -> if (value == 1L) context.getString(R.string.day)    else context.getString(R.string.days)
        DisplayUnit.HOURS   -> if (value == 1L) context.getString(R.string.hour)   else context.getString(R.string.hours)
        DisplayUnit.MINUTES -> if (value == 1L) context.getString(R.string.minute) else context.getString(R.string.minutes)
        DisplayUnit.SECONDS -> if (value == 1L) context.getString(R.string.second) else context.getString(R.string.seconds)
    }

    private fun parseColor(colorHex: String): Color =
        try { Color(android.graphics.Color.parseColor(colorHex)) }
        catch (e: Exception) { Color(0xFFFF7043) }

    private fun darken(color: Color): Color = color.copy(
        red   = (color.red   * 0.7f).coerceIn(0f, 1f),
        green = (color.green * 0.7f).coerceIn(0f, 1f),
        blue  = (color.blue  * 0.7f).coerceIn(0f, 1f)
    )
}

// ─── Widget Format Builder ────────────────────────────────────────────────────

private fun buildWidgetFormat(dateUnits: Set<DisplayUnit>, showTime: Boolean): String {
    return DisplayFormat.encode(dateUnits)
}

// ─── Widget Receiver ──────────────────────────────────────────────────────────

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            WidgetConfigActivity.deleteCountdownId(context, appWidgetId)
        }
    }
}