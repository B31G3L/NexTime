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
import androidx.compose.ui.text.style.TextOverflow
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
    val cardPaddingH   : Dp,
    val cardPaddingV   : Dp,
    val numberSize     : TextUnit,
    val unitSize       : TextUnit,
    val titleSize      : TextUnit,
    val dateSize       : TextUnit,
    val iconBoxSize    : Dp,
    val iconGlyphSize  : Dp,
    val rowSpacing     : Dp,
    val showDate       : Boolean,
)

private fun styleConfig(style: DisplayStyle): CardStyleConfig = when (style) {
    DisplayStyle.COMPACT -> CardStyleConfig(
        cardPaddingH = 14.dp, cardPaddingV = 12.dp,
        numberSize = 32.sp, unitSize = 12.sp, titleSize = 12.sp, dateSize = 11.sp,
        iconBoxSize = 28.dp, iconGlyphSize = 16.dp, rowSpacing = 10.dp, showDate = false,
    )
    DisplayStyle.NORMAL -> CardStyleConfig(
        cardPaddingH = 18.dp, cardPaddingV = 16.dp,
        numberSize = 42.sp, unitSize = 15.sp, titleSize = 13.sp, dateSize = 12.sp,
        iconBoxSize = 30.dp, iconGlyphSize = 17.dp, rowSpacing = 14.dp, showDate = true,
    )
    DisplayStyle.GENEROUS -> CardStyleConfig(
        cardPaddingH = 20.dp, cardPaddingV = 20.dp,
        numberSize = 52.sp, unitSize = 17.sp, titleSize = 14.sp, dateSize = 13.sp,
        iconBoxSize = 34.dp, iconGlyphSize = 19.dp, rowSpacing = 16.dp, showDate = true,
    )
}

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

    // Custom-Format pro Countdown hat Vorrang, sonst globale Einstellungen
    val globalDateUnits by AppPreferences.getDefaultDateUnits(context)
        .collectAsState(initial = setOf(DisplayUnit.DAYS))
    val showTimeOnCard by AppPreferences.getShowTimeOnCard(context)
        .collectAsState(initial = false)

    val hasCustomFormat = countdown.displayFormat.isNotBlank()
    val activeUnits = remember(countdown.displayFormat, globalDateUnits) {
        if (hasCustomFormat) countdown.activeDisplayUnitsOrdered
        else DisplayFormat.sorted(globalDateUnits)
    }
    val needsSecondTick = showTimeOnCard ||
            (hasCustomFormat && activeUnits.any { it in TIME_UNITS })

    val cfg = remember(displayStyle) { styleConfig(displayStyle) }

    val tick by if (needsSecondTick)
        viewModel.tickSeconds.collectAsState()
    else
        viewModel.tickMinutes.collectAsState()

    val timeInfo = remember(countdown.id, activeUnits, needsSecondTick, tick) {
        countdown.calculateTimeRemaining()
    }

    // Akzentfarbe des Countdowns — trägt Icon-Kästchen + Rand
    val fallbackAccent = MaterialTheme.colorScheme.primary
    val accentColor = remember(countdown.color, fallbackAccent) {
        runCatching { Color(android.graphics.Color.parseColor(countdown.color)) }
            .getOrElse { fallbackAccent }
    }

    val isToday = remember(countdown.id, countdown.effectiveTarget, tick) {
        countdown.effectiveTarget.toLocalDate() == LocalDate.now()
    }

    val contentColor = MaterialTheme.colorScheme.onSurface
    val subtleColor  = MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(14.dp),
        color           = MaterialTheme.colorScheme.background,
        tonalElevation  = 0.dp,
        shadowElevation = 0.dp,
        border          = androidx.compose.foundation.BorderStroke(0.5.dp, accentColor.copy(alpha = 0.28f)),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {

            // ── Inhalt: Zahl oben, Rest darunter verteilt ──────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = cfg.cardPaddingH, vertical = cfg.cardPaddingV),
                verticalArrangement = Arrangement.spacedBy(cfg.rowSpacing)
            ) {

                // Obere Hälfte: nur die Countdown-Zahl (Hauptelement)
                CountdownMainDisplay(
                    timeInfo    = timeInfo,
                    units       = activeUnits,
                    numberSize  = cfg.numberSize,
                    unitSize    = cfg.unitSize,
                    accentColor = contentColor,  // Zahl monochrom (Textfarbe)
                    textColor   = contentColor,
                )

                // Untere Zeile: Icon + Titel (links), Badges, Datum (rechts)
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Icon im getönten Kästchen (trägt die Akzentfarbe)
                    Box(
                        modifier = Modifier
                            .size(cfg.iconBoxSize)
                            .clip(RoundedCornerShape(9.dp))
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = iconByName(countdown.icon),
                            contentDescription = countdown.title,
                            tint               = accentColor,
                            modifier           = Modifier.size(cfg.iconGlyphSize)
                        )
                    }

                    // Titel füllt den Raum → Datum sitzt immer konsistent rechts.
                    // Ellipsis kürzt lange Titel, statt das Datum zu verschieben.
                    Text(
                        text     = countdown.title,
                        fontSize = cfg.titleSize,
                        color    = subtleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (countdown.isPinned)    PinBadge(accentColor)
                    if (countdown.isRecurring) RecurringBadge(countdown.recurrenceType, accentColor)
                    if (isToday)               TodayBadge(accentColor)

                    if (cfg.showDate) {
                        val datePattern = if (countdown.hasTime) "dd.MM.yyyy · HH:mm" else "dd.MM.yyyy"
                        Text(
                            text     = countdown.effectiveTarget.format(
                                DateTimeFormatter.ofPattern(datePattern)
                            ),
                            fontSize = cfg.dateSize,
                            color    = subtleColor.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                    }
                }
            }
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
    val resolvedNumber = if (accentColor == Color.Unspecified)
        MaterialTheme.colorScheme.onSurface else accentColor
    val resolvedText   = if (textColor   == Color.Unspecified)
        MaterialTheme.colorScheme.onSurface else textColor

    val segments = remember(timeInfo, units) { timeInfo.buildDisplaySegments(units) }

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
                fontWeight = FontWeight.Medium,
                color      = resolvedNumber
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