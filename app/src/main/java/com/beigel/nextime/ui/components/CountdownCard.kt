package com.beigel.nextime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.CheckCircle
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
import com.beigel.nextime.data.model.buildDisplaySegments
import com.beigel.nextime.data.model.calculateTimeRemaining
import com.beigel.nextime.R
import com.beigel.nextime.data.model.Countdown
import com.beigel.nextime.data.model.CountdownInfo
import com.beigel.nextime.data.model.DisplayFormat
import com.beigel.nextime.data.model.DisplayUnit
import com.beigel.nextime.data.model.RecurrenceType
import com.beigel.nextime.data.model.TIME_UNITS
import com.beigel.nextime.ui.theme.AppPreferences
import com.beigel.nextime.ui.theme.DisplayStyle
import com.beigel.nextime.ui.viewmodel.CountdownViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

// ─── Hilfsfunktion: lokalisiertes Datum ──────────────────────────────────────

private fun formatDate(countdown: Countdown, locale: Locale): String {
    val datePart = countdown.effectiveTarget.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    )
    return if (countdown.hasTime) {
        val timePart = countdown.effectiveTarget.format(
            DateTimeFormatter.ofPattern("HH:mm", locale)
        )
        "$datePart · $timePart"
    } else {
        datePart
    }
}

// ─── CountdownCard ────────────────────────────────────────────────────────────

@Composable
fun CountdownCard(
    countdown    : Countdown,
    viewModel    : CountdownViewModel = viewModel(),
    previewStyle : DisplayStyle? = null
) {
    val context = LocalContext.current
    val locale  = Locale.getDefault()

    val displayStyle by if (previewStyle != null) {
        produceState(initialValue = previewStyle) { value = previewStyle }
    } else {
        AppPreferences.getDisplayStyle(context).collectAsState(initial = DisplayStyle.STANDARD)
    }

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

    val tick by if (needsSecondTick)
        viewModel.tickSeconds.collectAsState()
    else
        viewModel.tickMinutes.collectAsState()

    val timeInfo = remember(countdown.id, activeUnits, needsSecondTick, tick) {
        countdown.calculateTimeRemaining()
    }

    val fallbackAccent = MaterialTheme.colorScheme.primary
    val accentColor = remember(countdown.color, fallbackAccent) {
        runCatching { Color(android.graphics.Color.parseColor(countdown.color)) }
            .getOrElse { fallbackAccent }
    }

    val isToday = remember(countdown.id, countdown.effectiveTarget, tick) {
        countdown.effectiveTarget.toLocalDate() == LocalDate.now()
    }

    // Abgeschlossen = Zieltermin liegt in der Vergangenheit, der Countdown
    // wiederholt sich nicht UND das Zieldatum lag bei Erstellung noch in der
    // Zukunft (echter Ablauf). Bewusst angelegte Count-ups (Zieldatum war schon
    // bei Erstellung in der Vergangenheit) zählen weiterhin normal hoch.
    val isCompleted = remember(countdown.id, countdown.isRecurring, timeInfo.isPast, countdown.targetDateTime, countdown.createdAt) {
        timeInfo.isPast && !countdown.isRecurring &&
                countdown.targetDateTime.isAfter(countdown.createdAt)
    }

    val contentColor = MaterialTheme.colorScheme.onSurface
    val subtleColor  = MaterialTheme.colorScheme.onSurfaceVariant

    // ── Lokalisiertes Datum ───────────────────────────────────────────────────
    val dateText = remember(countdown.effectiveTarget, countdown.hasTime, locale, tick) {
        formatDate(countdown, locale)
    }

    Surface(
        modifier        = Modifier.fillMaxWidth(),
        shape           = RoundedCornerShape(14.dp),
        color           = MaterialTheme.colorScheme.background,
        tonalElevation  = 0.dp,
        shadowElevation = 0.dp,
        border          = androidx.compose.foundation.BorderStroke(0.5.dp, accentColor.copy(alpha = 0.28f)),
    ) {
        when (displayStyle) {
            DisplayStyle.STANDARD   -> StandardLayout(countdown, timeInfo, activeUnits, accentColor, contentColor, subtleColor, isToday, dateText, isCompleted)
            DisplayStyle.KOMPAKT    -> KompaktLayout(countdown, timeInfo, activeUnits, accentColor, contentColor, subtleColor, isToday, dateText, isCompleted)
            DisplayStyle.BANNER     -> BannerLayout(countdown, timeInfo, activeUnits, accentColor, contentColor, subtleColor, isToday, dateText, isCompleted)
            DisplayStyle.INVERTIERT -> InvertiertLayout(countdown, timeInfo, activeUnits, accentColor, contentColor, subtleColor, isToday, dateText, isCompleted)
        }
    }
}

// ─── Layout 1: STANDARD ───────────────────────────────────────────────────────

@Composable
private fun StandardLayout(
    countdown: Countdown, timeInfo: CountdownInfo, units: List<DisplayUnit>,
    accentColor: Color, contentColor: Color, subtleColor: Color,
    isToday: Boolean, dateText: String, isCompleted: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (isCompleted) {
            CompletedDisplay(42.sp, 15.sp, accentColor, contentColor)
        } else {
            CountdownMainDisplay(timeInfo, units, 42.sp, 15.sp, contentColor, contentColor)
        }
        InfoRow(countdown, accentColor, contentColor, subtleColor, isToday, dateText)
    }
}

// ─── Layout 4: INVERTIERT ─────────────────────────────────────────────────────

@Composable
private fun InvertiertLayout(
    countdown: Countdown, timeInfo: CountdownInfo, units: List<DisplayUnit>,
    accentColor: Color, contentColor: Color, subtleColor: Color,
    isToday: Boolean, dateText: String, isCompleted: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InfoRow(countdown, accentColor, contentColor, subtleColor, isToday, dateText)
        if (isCompleted) {
            CompletedDisplay(42.sp, 15.sp, accentColor, contentColor)
        } else {
            CountdownMainDisplay(timeInfo, units, 42.sp, 15.sp, contentColor, contentColor)
        }
    }
}

@Composable
private fun InfoRow(
    countdown: Countdown, accentColor: Color, contentColor: Color, subtleColor: Color,
    isToday: Boolean, dateText: String
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconBox(countdown, accentColor, 30.dp, 17.dp)
        Text(
            text       = countdown.title,
            fontSize   = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color      = contentColor,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.weight(1f)
        )
        Badges(countdown, accentColor, isToday)
        Text(
            text     = dateText,
            fontSize = 12.sp,
            color    = subtleColor.copy(alpha = 0.7f),
            maxLines = 1
        )
    }
}

// ─── Layout 2: KOMPAKT ────────────────────────────────────────────────────────

@Composable
private fun KompaktLayout(
    countdown: Countdown, timeInfo: CountdownInfo, units: List<DisplayUnit>,
    accentColor: Color, contentColor: Color, subtleColor: Color,
    isToday: Boolean, dateText: String, isCompleted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconBox(countdown, accentColor, 36.dp, 20.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text       = countdown.title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = contentColor,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f, fill = false)
                )
                Badges(countdown, accentColor, isToday)
            }
            Text(text = dateText, fontSize = 11.sp, color = subtleColor.copy(alpha = 0.7f), maxLines = 1)
        }
        if (isCompleted) {
            CompletedDisplay(26.sp, 11.sp, accentColor, contentColor)
        } else {
            CountdownMainDisplay(timeInfo, units, 26.sp, 11.sp, contentColor, contentColor)
        }
    }
}

// ─── Layout 3: BANNER ─────────────────────────────────────────────────────────

@Composable
private fun BannerLayout(
    countdown: Countdown, timeInfo: CountdownInfo, units: List<DisplayUnit>,
    accentColor: Color, contentColor: Color, subtleColor: Color,
    isToday: Boolean, dateText: String, isCompleted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(60.dp)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = iconByName(countdown.icon),
                contentDescription = countdown.title,
                tint               = accentColor,
                modifier           = Modifier.size(28.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text       = countdown.title,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = contentColor,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.weight(1f, fill = false)
                )
                Badges(countdown, accentColor, isToday)
            }
            if (isCompleted) {
                CompletedDisplay(32.sp, 13.sp, accentColor, contentColor)
            } else {
                CountdownMainDisplay(timeInfo, units, 32.sp, 13.sp, contentColor, contentColor)
            }
            Text(text = dateText, fontSize = 11.sp, color = subtleColor.copy(alpha = 0.7f), maxLines = 1)
        }
    }
}

// ─── Gemeinsame Bausteine ─────────────────────────────────────────────────────

@Composable
private fun IconBox(countdown: Countdown, accentColor: Color, boxSize: Dp, glyphSize: Dp) {
    Box(
        modifier = Modifier
            .size(boxSize)
            .clip(RoundedCornerShape(9.dp))
            .background(accentColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = iconByName(countdown.icon),
            contentDescription = countdown.title,
            tint               = accentColor,
            modifier           = Modifier.size(glyphSize)
        )
    }
}

@Composable
private fun Badges(countdown: Countdown, accentColor: Color, isToday: Boolean) {
    if (countdown.isPinned)    PinBadge(accentColor)
    if (countdown.isRecurring) RecurringBadge(countdown.recurrenceType, accentColor)
    if (isToday)               TodayBadge(accentColor)
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

// ─── Abschluss-Anzeige (statt Count-up) ───────────────────────────────────────

@Composable
private fun CompletedDisplay(
    numberSize  : TextUnit,
    unitSize    : TextUnit,
    accentColor : Color,
    textColor   : Color,
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector        = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint               = accentColor,
            modifier           = Modifier.size((numberSize.value * 0.75f).dp)
        )
        Text(
            text       = stringResource(R.string.countdown_completed_label),
            fontSize   = unitSize,
            fontWeight = FontWeight.Medium,
            color      = textColor.copy(alpha = 0.7f)
        )
    }
}

// ─── Hauptanzeige (Zahl + Einheit) ────────────────────────────────────────────

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