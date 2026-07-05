package todo.beigelwick.de.todolist.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
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
import todo.beigelwick.de.todolist.ui.theme.ThemeMode
import todo.beigelwick.de.todolist.ui.theme.ThemePreferences
import java.time.format.DateTimeFormatter

// ─── Widget-Farben (folgen dem App-Hell/Dunkel-Theme) ─────────────────────────

private data class WidgetColors(
    val background       : Color,
    val onSurface        : Color,
    val onSurfaceVariant : Color,
    val outline          : Color,
)

private fun widgetColors(dark: Boolean): WidgetColors = if (dark) WidgetColors(
    background       = Color(0xFF211D17),
    onSurface        = Color(0xFFEDE8DF),
    onSurfaceVariant = Color(0xFFCDC8BF),
    outline          = Color(0xFF453F35),
) else WidgetColors(
    background       = Color(0xFFFAF8F2),
    onSurface        = Color(0xFF1E1A16),
    onSurfaceVariant = Color(0xFF4A4540),
    outline          = Color(0xFFD8D0C8),
)

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
        val resolvedFormat   = buildWidgetFormat(globalDateUnits)

        // App-Hell/Dunkel ermitteln (folgt App-Theme, bei SYSTEM der Geräteeinstellung)
        val dark   = isWidgetDark(context)
        val colors = widgetColors(dark)

        provideContent {
            val size        = LocalSize.current
            val clickAction = actionStartActivity<MainActivity>()

            if (countdown == null) {
                EmptyWidget(context, size, colors, clickAction)
                return@provideContent
            }

            val timeInfo    = countdown.calculateTimeRemaining()
            val accentColor = parseColor(countdown.color)

            when {
                size.width >= SIZE_XLARGE.width                                      -> XLargeLayout(context, countdown, timeInfo, accentColor, colors, clickAction, resolvedFormat)
                size.width >= SIZE_LARGE.width                                       -> LargeLayout(context, countdown, timeInfo, accentColor, colors, clickAction, resolvedFormat)
                size.width >= SIZE_MEDIUM.width && size.height >= SIZE_MEDIUM.height -> MediumLayout(context, countdown, timeInfo, accentColor, colors, clickAction, resolvedFormat)
                size.width >= SIZE_WIDE.width                                        -> WideLayout(context, countdown, timeInfo, accentColor, colors, clickAction, resolvedFormat)
                size.height >= SIZE_TALL.height                                      -> TallLayout(context, countdown, timeInfo, accentColor, colors, clickAction, resolvedFormat)
                else                                                                 -> SmallLayout(context, countdown, timeInfo, accentColor, colors, clickAction, resolvedFormat)
            }
        }
    }

    // ─── Icon-Kästchen (getöntes Logo, da Glance keine Material-Icons kann) ────

    @Composable
    private fun WidgetIconBox(accentColor: Color, sizeDp: Int) {
        Box(
            modifier         = GlanceModifier
                .size(sizeDp.dp)
                .background(accentColor.copy(alpha = 0.15f))
                .cornerRadius(9.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider           = ImageProvider(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                colorFilter        = ColorFilter.tint(ColorProvider(accentColor)),
                modifier           = GlanceModifier.size((sizeDp * 0.6f).toInt().dp)
            )
        }
    }

    // ─── 1×1 — nur Zahl + Einheit ──────────────────────────────────────────────

    @Composable
    private fun SmallLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, colors: WidgetColors, clickAction: Action, displayFormat: String) {
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitShort(context, timeInfo, displayFormat)

        WidgetCard(accentColor, colors, clickAction) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically, modifier = GlanceModifier.fillMaxSize().padding(8.dp)) {
                Text(text = mainVal, style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurface), textAlign = TextAlign.Center))
                Text(text = mainUnit, style = TextStyle(fontSize = 10.sp, color = ColorProvider(colors.onSurfaceVariant), textAlign = TextAlign.Center))
            }
        }
    }

    // ─── 1×2 — Zahl oben, Titel darunter ───────────────────────────────────────

    @Composable
    private fun TallLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, colors: WidgetColors, clickAction: Action, displayFormat: String) {
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitShort(context, timeInfo, displayFormat)

        WidgetCard(accentColor, colors, clickAction) {
            Column(modifier = GlanceModifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.Start) {
                Text(text = mainVal, style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurface)))
                Text(text = mainUnit, style = TextStyle(fontSize = 11.sp, color = ColorProvider(colors.onSurfaceVariant)))
                Spacer(GlanceModifier.defaultWeight())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WidgetIconBox(accentColor, 24)
                    Spacer(GlanceModifier.width(6.dp))
                    Text(text = countdown.title, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurfaceVariant)), maxLines = 1, modifier = GlanceModifier.defaultWeight())
                }
            }
        }
    }

    // ─── 2×1 — niedrig: Icon links, Titel + Zahl rechts ────────────────────────

    @Composable
    private fun WideLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, colors: WidgetColors, clickAction: Action, displayFormat: String) {
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitShort(context, timeInfo, displayFormat)

        WidgetCard(accentColor, colors, clickAction) {
            Row(modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                WidgetIconBox(accentColor, 36)
                Spacer(GlanceModifier.width(10.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(text = countdown.title, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurfaceVariant)), maxLines = 1)
                    Text(text = "$mainVal $mainUnit", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurface)))
                }
            }
        }
    }

    // ─── 2×2 — Card A: Zahl oben, Icon + Titel darunter ────────────────────────

    @Composable
    private fun MediumLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, colors: WidgetColors, clickAction: Action, displayFormat: String) {
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitFull(context, timeInfo, displayFormat)

        WidgetCard(accentColor, colors, clickAction) {
            Column(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
                Text(text = mainVal, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurface)))
                Text(text = mainUnit, style = TextStyle(fontSize = 12.sp, color = ColorProvider(colors.onSurfaceVariant)))
                Spacer(GlanceModifier.defaultWeight())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WidgetIconBox(accentColor, 28)
                    Spacer(GlanceModifier.width(8.dp))
                    Text(text = countdown.title, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurfaceVariant)), maxLines = 1, modifier = GlanceModifier.defaultWeight())
                }
            }
        }
    }

    // ─── 3×2 — Card A größer ───────────────────────────────────────────────────

    @Composable
    private fun LargeLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, colors: WidgetColors, clickAction: Action, displayFormat: String) {
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitFull(context, timeInfo, displayFormat)

        WidgetCard(accentColor, colors, clickAction) {
            Column(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = mainVal, style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurface)))
                    Spacer(GlanceModifier.width(6.dp))
                    Text(text = mainUnit, style = TextStyle(fontSize = 14.sp, color = ColorProvider(colors.onSurfaceVariant)), modifier = GlanceModifier.padding(bottom = 6.dp))
                }
                Spacer(GlanceModifier.defaultWeight())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WidgetIconBox(accentColor, 30)
                    Spacer(GlanceModifier.width(8.dp))
                    Text(text = countdown.title, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurfaceVariant)), maxLines = 1, modifier = GlanceModifier.defaultWeight())
                    Text(text = countdown.effectiveTarget.format(dateFormatter(countdown.hasTime)), style = TextStyle(fontSize = 10.sp, color = ColorProvider(colors.onSurfaceVariant)))
                }
            }
        }
    }

    // ─── 4×2 — Card A am breitesten ────────────────────────────────────────────

    @Composable
    private fun XLargeLayout(context: Context, countdown: Countdown, timeInfo: CountdownInfo, accentColor: Color, colors: WidgetColors, clickAction: Action, displayFormat: String) {
        val mainVal  = formatMainValue(timeInfo, displayFormat)
        val mainUnit = formatMainUnitFull(context, timeInfo, displayFormat)

        WidgetCard(accentColor, colors, clickAction) {
            Column(modifier = GlanceModifier.fillMaxSize().padding(18.dp)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(text = mainVal, style = TextStyle(fontSize = 46.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurface)))
                    Spacer(GlanceModifier.width(8.dp))
                    Text(text = mainUnit, style = TextStyle(fontSize = 16.sp, color = ColorProvider(colors.onSurfaceVariant)), modifier = GlanceModifier.padding(bottom = 7.dp))
                }
                Spacer(GlanceModifier.defaultWeight())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WidgetIconBox(accentColor, 34)
                    Spacer(GlanceModifier.width(10.dp))
                    Text(text = countdown.title, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ColorProvider(colors.onSurfaceVariant)), maxLines = 1, modifier = GlanceModifier.defaultWeight())
                    Text(text = countdown.effectiveTarget.format(dateFormatter(countdown.hasTime)), style = TextStyle(fontSize = 11.sp, color = ColorProvider(colors.onSurfaceVariant)))
                }
            }
        }
    }

    // ─── Leer ──────────────────────────────────────────────────────────────────

    @Composable
    private fun EmptyWidget(context: Context, size: DpSize, colors: WidgetColors, clickAction: Action) {
        WidgetCard(colors.onSurfaceVariant, colors, clickAction) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider           = ImageProvider(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    colorFilter        = ColorFilter.tint(ColorProvider(colors.onSurfaceVariant)),
                    modifier           = GlanceModifier.size(32.dp)
                )
                if (size.height > 60.dp) {
                    Spacer(GlanceModifier.height(4.dp))
                    Text(text = context.getString(R.string.widget_no_countdowns), style = TextStyle(fontSize = 10.sp, color = ColorProvider(colors.onSurfaceVariant), textAlign = TextAlign.Center))
                }
            }
        }
    }

    // ─── Gemeinsame Karte mit Akzent-Rand ──────────────────────────────────────
    // Glance kennt keinen border-Modifier → Rand über zwei verschachtelte Boxen:
    // äußere Box trägt die (transparente) Akzentfarbe, die innere den Hintergrund.
    // Der 1.dp-Zwischenraum bildet den sichtbaren Ring.

    @Composable
    private fun WidgetCard(
        accentColor : Color,
        colors      : WidgetColors,
        clickAction : Action,
        content     : @Composable () -> Unit
    ) {
        Box(
            modifier         = GlanceModifier
                .fillMaxSize()
                .background(accentColor.copy(alpha = 0.28f))
                .cornerRadius(16.dp)
                .clickable(clickAction),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding(1.dp)
            ) {
                Box(
                    modifier         = GlanceModifier
                        .fillMaxSize()
                        .background(colors.background)
                        .cornerRadius(15.dp),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    // ─── Hilfsfunktionen ───────────────────────────────────────────────────────

    private fun dateFormatter(hasTime: Boolean): DateTimeFormatter =
        DateTimeFormatter.ofPattern(if (hasTime) "dd.MM.yyyy · HH:mm" else "dd.MM.yyyy")

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
}

// ─── Theme-Erkennung fürs Widget ───────────────────────────────────────────────

private suspend fun isWidgetDark(context: Context): Boolean {
    val mode = ThemePreferences.getThemeMode(context).first()
    return when (mode) {
        ThemeMode.DARK   -> true
        ThemeMode.LIGHT  -> false
        ThemeMode.SYSTEM -> {
            val ui = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            ui == Configuration.UI_MODE_NIGHT_YES
        }
    }
}

// ─── Widget Format Builder ─────────────────────────────────────────────────────

private fun buildWidgetFormat(dateUnits: Set<DisplayUnit>): String {
    return DisplayFormat.encode(dateUnits)
}

// ─── Widget Receiver ───────────────────────────────────────────────────────────

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            WidgetConfigActivity.deleteCountdownId(context, appWidgetId)
        }
    }
}