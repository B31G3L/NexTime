package todo.beigelwick.de.todolist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.CountdownInfo
import todo.beigelwick.de.todolist.data.model.DisplayFormat
import todo.beigelwick.de.todolist.data.model.DisplayUnit
import todo.beigelwick.de.todolist.data.model.RecurrenceType
import todo.beigelwick.de.todolist.data.model.TIME_UNITS
import todo.beigelwick.de.todolist.data.model.buildDisplaySegments
import todo.beigelwick.de.todolist.data.model.calculateTimeRemaining
import todo.beigelwick.de.todolist.ui.components.iconByName
import todo.beigelwick.de.todolist.ui.theme.AppPreferences
import todo.beigelwick.de.todolist.ui.theme.DisplayStyle
import todo.beigelwick.de.todolist.ui.viewmodel.CountdownViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Style-Konfiguration ──────────────────────────────────────────────────────

private data class CardStyleConfig(
    val iconSize      : Dp,
    val cardPaddingH  : Dp,
    val cardPaddingV  : Dp,
    val rowSpacing    : Dp,
    val numberSize    : TextUnit,
    val unitSize      : TextUnit,
    val titleSize     : TextUnit,
    val subInfoSize   : TextUnit,
    val dateSize      : TextUnit,
    val showSubInfo   : Boolean,
    val showDate      : Boolean,
    val columnSpacing : Dp,
    val barHeight     : Dp,
)

private fun styleConfig(style: DisplayStyle): CardStyleConfig = when (style) {
    DisplayStyle.COMPACT -> CardStyleConfig(
        iconSize = 36.dp,
        cardPaddingH = 12.dp, cardPaddingV = 10.dp,
        rowSpacing = 10.dp, numberSize = 20.sp,
        unitSize = 12.sp, titleSize = 11.sp,
        subInfoSize = 0.sp, dateSize = 0.sp,
        showSubInfo = false, showDate = false,
        columnSpacing = 2.dp, barHeight = 2.dp,
    )
    DisplayStyle.NORMAL -> CardStyleConfig(
        iconSize = 48.dp,
        cardPaddingH = 14.dp, cardPaddingV = 12.dp,
        rowSpacing = 12.dp, numberSize = 26.sp,
        unitSize = 14.sp, titleSize = 13.sp,
        subInfoSize = 11.sp, dateSize = 11.sp,
        showSubInfo = true, showDate = true,
        columnSpacing = 3.dp, barHeight = 3.dp,
    )
    DisplayStyle.GENEROUS -> CardStyleConfig(
        iconSize = 60.dp,
        cardPaddingH = 16.dp, cardPaddingV = 16.dp,
        rowSpacing = 14.dp, numberSize = 34.sp,
        unitSize = 17.sp, titleSize = 14.sp,
        subInfoSize = 12.sp, dateSize = 12.sp,
        showSubInfo = true, showDate = true,
        columnSpacing = 4.dp, barHeight = 3.dp,
    )
}

// ─── UnitSplit ────────────────────────────────────────────────────────────────

private data class UnitSplit(
    val mainUnits     : List<DisplayUnit>,
    val timeInSubInfo : Boolean
)

private fun buildUnitSplit(dateUnits: Set<DisplayUnit>, showTime: Boolean) =
    UnitSplit(mainUnits = DisplayFormat.sorted(dateUnits), timeInSubInfo = showTime)

// ─── CountdownCard ────────────────────────────────────────────────────────────

@Composable
fun CountdownCard(
    countdown    : Countdown,
    viewModel    : CountdownViewModel = viewModel(),
    previewStyle : DisplayStyle? = null
) {
    val context = LocalContext.current

    val displayStyle by if (previewStyle != null) {
        produceState(initialValue = previewStyle) { value = previewStyle }
    } else {
        AppPreferences.getDisplayStyle(context).collectAsState(initial = DisplayStyle.NORMAL)
    }

    val defaultDateUnits by AppPreferences.getDefaultDateUnits(context)
        .collectAsState(initial = setOf(DisplayUnit.DAYS))
    val showTimeOnCard by AppPreferences.getShowTimeOnCard(context)
        .collectAsState(initial = false)

    val cfg   = remember(displayStyle) { styleConfig(displayStyle) }
    val split = remember(defaultDateUnits, showTimeOnCard) {
        buildUnitSplit(defaultDateUnits, showTimeOnCard)
    }

    val tick by if (showTimeOnCard)
        viewModel.tickSeconds.collectAsState()
    else
        viewModel.tickMinutes.collectAsState()

    val timeInfo = remember(countdown.id, defaultDateUnits, showTimeOnCard, tick) {
        countdown.calculateTimeRemaining()
    }

    // Akzentfarbe des Countdowns (für die 2 Balken)
    // MaterialTheme darf nicht in remember{} aufgerufen werden → Fallback separat
    val fallbackAccent = MaterialTheme.colorScheme.primary
    val accentColor = remember(countdown.color, fallbackAccent) {
        runCatching { Color(android.graphics.Color.parseColor(countdown.color)) }
            .getOrElse { fallbackAccent }
    }

    // BUG FIX: tick als Key, damit isToday nach Mitternacht korrekt neu berechnet wird
    val isToday = remember(countdown.id, countdown.effectiveTarget, tick) {
        countdown.effectiveTarget.toLocalDate() == LocalDate.now()
    }

    // Hintergrund = App-Oberfläche (surfaceVariant für leichte Abhebung)
    val contentColor   = MaterialTheme.colorScheme.onSurface
    val subtleColor    = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
    ) {
        Column {
            // ── Oberer Akzentbalken ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cfg.barHeight)
                    .background(accentColor)
            )

            // ── Inhalt ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = cfg.cardPaddingH, vertical = cfg.cardPaddingV),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(cfg.rowSpacing)
            ) {
                // Material Icon ohne Hintergrund
                Icon(
                    imageVector        = iconByName(countdown.icon),
                    contentDescription = countdown.title,
                    tint               = accentColor,
                    modifier           = Modifier.size(cfg.iconSize)
                )

                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(cfg.columnSpacing)
                ) {
                    // Titel + Badges
                    Row(
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text     = countdown.title,
                            fontSize = cfg.titleSize,
                            color    = subtleColor,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (countdown.isPinned)    PinBadge(accentColor)
                        if (countdown.isRecurring) RecurringBadge(countdown.recurrenceType, accentColor)
                        if (isToday)               TodayBadge(accentColor)
                    }

                    // Hauptanzeige: Zahl + Einheit in Akzentfarbe
                    CountdownMainDisplay(
                        timeInfo    = timeInfo,
                        units       = split.mainUnits,
                        numberSize  = cfg.numberSize,
                        unitSize    = cfg.unitSize,
                        accentColor = accentColor,
                        textColor   = contentColor,
                    )

                    // SubInfo-Zeile
                    if (cfg.showSubInfo || cfg.showDate) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            if (cfg.showSubInfo) {
                                Text(
                                    text       = buildSubInfo(timeInfo, countdown, isToday, split.timeInSubInfo),
                                    fontSize   = cfg.subInfoSize,
                                    color      = if (split.timeInSubInfo) accentColor else subtleColor,
                                    fontWeight = if (split.timeInSubInfo) FontWeight.SemiBold else FontWeight.Normal,
                                    letterSpacing = if (split.timeInSubInfo) 0.5.sp else 0.sp
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                            if (cfg.showDate) {
                                val datePattern = if (countdown.hasTime) "dd.MM.yyyy  HH:mm" else "dd.MM.yyyy"
                                Text(
                                    text     = countdown.effectiveTarget.format(
                                        DateTimeFormatter.ofPattern(datePattern)
                                    ),
                                    fontSize = cfg.dateSize,
                                    color    = subtleColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Unterer Akzentbalken ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cfg.barHeight)
                    .background(accentColor)
            )
        }
    }
}

// ─── Badges ───────────────────────────────────────────────────────────────────

@Composable
private fun PinBadge(accentColor: Color) {
    Box(
        modifier         = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(accentColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Default.PushPin,
            contentDescription = null,
            tint               = accentColor,
            modifier           = Modifier
                .padding(horizontal = 5.dp, vertical = 3.dp)
                .size(11.dp)
                .rotate(45f)
        )
    }
}

@Composable
private fun TodayBadge(accentColor: Color) {
    Box(
        modifier         = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(accentColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = stringResource(R.string.badge_today),
            fontSize   = 9.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.White,
            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun RecurringBadge(recurrenceType: RecurrenceType, accentColor: Color) {
    Box(
        modifier         = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(accentColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector        = Icons.Outlined.Repeat,
                contentDescription = null,
                tint               = accentColor,
                modifier           = Modifier.size(10.dp)
            )
            Text(
                text = when (recurrenceType) {
                    RecurrenceType.DAILY   -> stringResource(R.string.badge_daily)
                    RecurrenceType.WEEKLY  -> stringResource(R.string.badge_weekly)
                    RecurrenceType.MONTHLY -> stringResource(R.string.badge_monthly)
                    RecurrenceType.YEARLY  -> stringResource(R.string.badge_yearly)
                    RecurrenceType.NONE    -> ""
                },
                fontSize   = 9.sp,
                fontWeight = FontWeight.Bold,
                color      = accentColor
            )
        }
    }
}

// ─── Hauptanzeige ─────────────────────────────────────────────────────────────

@Composable
fun CountdownMainDisplay(
    timeInfo    : CountdownInfo,
    units       : List<DisplayUnit>,
    numberSize  : TextUnit = 26.sp,
    unitSize    : TextUnit = 14.sp,
    accentColor : Color    = Color.Unspecified,
    textColor   : Color    = Color.Unspecified,
) {
    val resolvedAccent = if (accentColor == Color.Unspecified)
        MaterialTheme.colorScheme.primary else accentColor
    val resolvedText   = if (textColor   == Color.Unspecified)
        MaterialTheme.colorScheme.onSurface else textColor

    val segments = remember(timeInfo, units) { timeInfo.buildDisplaySegments(units) }

    // Strings vorab auflösen – stringResource() darf nicht in buildAnnotatedString aufgerufen werden
    val strYear   = stringResource(R.string.year);   val strYears   = stringResource(R.string.years)
    val strMonth  = stringResource(R.string.month);  val strMonths  = stringResource(R.string.months)
    val strWeek   = stringResource(R.string.week);   val strWeeks   = stringResource(R.string.weeks)
    val strDay    = stringResource(R.string.day);    val strDays    = stringResource(R.string.days)
    val strHour   = stringResource(R.string.hour);   val strHours   = stringResource(R.string.hours)
    val strMinute = stringResource(R.string.minute); val strMinutes = stringResource(R.string.minutes)
    val strSecond = stringResource(R.string.second); val strSeconds = stringResource(R.string.seconds)

    fun unitLabel(unit: DisplayUnit, value: Long): String = when (unit) {
        DisplayUnit.YEARS   -> if (value == 1L) strYear   else strYears
        DisplayUnit.MONTHS  -> if (value == 1L) strMonth  else strMonths
        DisplayUnit.WEEKS   -> if (value == 1L) strWeek   else strWeeks
        DisplayUnit.DAYS    -> if (value == 1L) strDay    else strDays
        DisplayUnit.HOURS   -> if (value == 1L) strHour   else strHours
        DisplayUnit.MINUTES -> if (value == 1L) strMinute else strMinutes
        DisplayUnit.SECONDS -> if (value == 1L) strSecond else strSeconds
    }

    val visibleSegments = segments.filter { seg ->
        if (seg.unit in TIME_UNITS) true else seg.value != 0L
    }
    val displaySegments = visibleSegments.ifEmpty { segments.takeLast(1) }

    val text = buildAnnotatedString {
        displaySegments.forEachIndexed { index, seg ->
            if (index > 0) {
                withStyle(SpanStyle(fontSize = unitSize, color = resolvedText.copy(alpha = 0.3f))) {
                    append("  ")
                }
            }
            withStyle(SpanStyle(
                fontSize   = numberSize,
                fontWeight = FontWeight.Bold,
                color      = resolvedAccent
            )) {
                append(
                    if (index > 0 && seg.unit in TIME_UNITS)
                        "%02d".format(seg.value)
                    else
                        "${seg.value}"
                )
            }
            withStyle(SpanStyle(
                fontSize = unitSize,
                color    = resolvedText.copy(alpha = 0.55f)
            )) {
                append(" ${unitLabel(seg.unit, seg.value)}")
            }
        }
    }
    Text(text = text, lineHeight = (numberSize.value * 1.2f).sp)
}

// ─── Subinfo ──────────────────────────────────────────────────────────────────

@Composable
private fun buildSubInfo(
    timeInfo      : CountdownInfo,
    countdown     : Countdown,
    isToday       : Boolean,
    timeInSubInfo : Boolean
): String {
    if (timeInSubInfo) return "%02d:%02d:%02d".format(
        timeInfo.hours, timeInfo.minutes, timeInfo.seconds
    )
    if (isToday) return stringResource(R.string.card_today_message)
    if (countdown.isRecurring) return stringResource(
        R.string.card_next_occurrence,
        countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    )
    return ""
}