package todo.beigelwick.de.todolist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import todo.beigelwick.de.todolist.ui.theme.AppPreferences
import todo.beigelwick.de.todolist.ui.theme.DisplayStyle
import todo.beigelwick.de.todolist.ui.viewmodel.CountdownViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ─── Style-Konfiguration ──────────────────────────────────────────────────────

private data class CardStyleConfig(
    val iconSize      : Dp,
    val iconCorner    : Dp,
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
)

private fun styleConfig(style: DisplayStyle): CardStyleConfig = when (style) {
    DisplayStyle.COMPACT -> CardStyleConfig(
        iconSize      = 40.dp,
        iconCorner    = 10.dp,
        cardPaddingH  = 12.dp,
        cardPaddingV  = 10.dp,
        rowSpacing    = 10.dp,
        numberSize    = 22.sp,
        unitSize      = 13.sp,
        titleSize     = 12.sp,
        subInfoSize   = 0.sp,
        dateSize      = 0.sp,
        showSubInfo   = false,
        showDate      = false,
        columnSpacing = 2.dp,
    )
    DisplayStyle.NORMAL -> CardStyleConfig(
        iconSize      = 56.dp,
        iconCorner    = 12.dp,
        cardPaddingH  = 16.dp,
        cardPaddingV  = 14.dp,
        rowSpacing    = 14.dp,
        numberSize    = 28.sp,
        unitSize      = 16.sp,
        titleSize     = 13.sp,
        subInfoSize   = 12.sp,
        dateSize      = 12.sp,
        showSubInfo   = true,
        showDate      = true,
        columnSpacing = 3.dp,
    )
    DisplayStyle.GENEROUS -> CardStyleConfig(
        iconSize      = 68.dp,
        iconCorner    = 16.dp,
        cardPaddingH  = 18.dp,
        cardPaddingV  = 18.dp,
        rowSpacing    = 16.dp,
        numberSize    = 36.sp,
        unitSize      = 18.sp,
        titleSize     = 14.sp,
        subInfoSize   = 13.sp,
        dateSize      = 13.sp,
        showSubInfo   = true,
        showDate      = true,
        columnSpacing = 5.dp,
    )
}

// ─── Einheiten-Klassifikation ─────────────────────────────────────────────────

private data class UnitSplit(
    val mainUnits     : List<DisplayUnit>,
    val timeInSubInfo : Boolean
)

private fun splitUnits(orderedUnits: List<DisplayUnit>): UnitSplit {
    val dateUnits = orderedUnits.filter { it !in TIME_UNITS }
    val timeUnits = orderedUnits.filter { it in TIME_UNITS }
    return when {
        dateUnits.isEmpty()      -> UnitSplit(orderedUnits, false)
        timeUnits.isNotEmpty()   -> UnitSplit(dateUnits, true)
        else                     -> UnitSplit(orderedUnits, false)
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

    val displayStyle by if (previewStyle != null) {
        produceState(initialValue = previewStyle) { value = previewStyle }
    } else {
        AppPreferences.getDisplayStyle(context).collectAsState(initial = DisplayStyle.NORMAL)
    }

    val cfg          = remember(displayStyle) { styleConfig(displayStyle) }
    val orderedUnits = remember(countdown.displayFormat) { DisplayFormat.decodeOrdered(countdown.displayFormat) }
    val split        = remember(orderedUnits) { splitUnits(orderedUnits) }
    val hasAnyTime   = remember(orderedUnits) { orderedUnits.any { it in TIME_UNITS } }

    val tick by if (hasAnyTime)
        viewModel.tickSeconds.collectAsState()
    else
        viewModel.tickMinutes.collectAsState()

    val timeInfo = remember(countdown.id, countdown.displayFormat, tick) {
        countdown.calculateTimeRemaining()
    }

    val baseColor = remember(countdown.color) {
        runCatching { Color(android.graphics.Color.parseColor(countdown.color)) }
            .getOrElse { Color(0xFFFF7043) }
    }
    val darkerBar = remember(baseColor) {
        baseColor.copy(
            red   = (baseColor.red   * 0.7f).coerceIn(0f, 1f),
            green = (baseColor.green * 0.7f).coerceIn(0f, 1f),
            blue  = (baseColor.blue  * 0.7f).coerceIn(0f, 1f)
        )
    }
    val isToday = remember(countdown.id, countdown.effectiveTarget) {
        countdown.effectiveTarget.toLocalDate() == LocalDate.now()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(baseColor)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(darkerBar))

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = cfg.cardPaddingH, vertical = cfg.cardPaddingV),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(cfg.rowSpacing)
            ) {
                // ── Icon ──────────────────────────────────────────────────────
                Box(
                    modifier         = Modifier
                        .size(cfg.iconSize)
                        .clip(RoundedCornerShape(cfg.iconCorner))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = countdown.icon.ifBlank { "⏰" },
                        fontSize = (cfg.iconSize.value * 0.43f).sp
                    )
                }

                // ── Inhalt ────────────────────────────────────────────────────
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(cfg.columnSpacing)
                ) {
                    // Titelzeile + Badges
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text     = countdown.title,
                            fontSize = cfg.titleSize,
                            color    = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (countdown.isPinned) {
                            PinBadge()
                        }
                        if (countdown.isRecurring) {
                            RecurringBadge(countdown.recurrenceType)
                        }
                        if (isToday && split.mainUnits.none { it in TIME_UNITS }) {
                            TodayBadge(baseColor)
                        }
                    }

                    // Hauptanzeige
                    CountdownMainDisplay(
                        timeInfo   = timeInfo,
                        units      = split.mainUnits,
                        numberSize = cfg.numberSize,
                        unitSize   = cfg.unitSize
                    )

                    // Subinfo + Datum
                    if (cfg.showSubInfo || cfg.showDate) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            if (cfg.showSubInfo) {
                                val subInfo = buildSubInfo(timeInfo, countdown, isToday, split.timeInSubInfo)
                                Text(
                                    text          = subInfo,
                                    fontSize      = cfg.subInfoSize,
                                    color         = Color.White.copy(alpha = 0.75f),
                                    fontWeight    = if (split.timeInSubInfo) FontWeight.SemiBold else FontWeight.Normal,
                                    letterSpacing = if (split.timeInSubInfo) 0.5.sp else 0.sp
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                            if (cfg.showDate) {
                                Text(
                                    text     = countdown.effectiveTarget
                                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                    fontSize = cfg.dateSize,
                                    color    = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(darkerBar))
        }
    }
}

// ─── Badges ───────────────────────────────────────────────────────────────────

@Composable
private fun PinBadge() {
    Box(
        modifier         = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Default.PushPin,
            contentDescription = null,
            tint               = Color.White,
            modifier           = Modifier
                .padding(horizontal = 5.dp, vertical = 3.dp)
                .size(11.dp)
                .rotate(45f)
        )
    }
}

@Composable
private fun TodayBadge(cardColor: Color) {
    Box(
        modifier         = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = stringResource(R.string.badge_today),
            fontSize   = 9.sp,
            fontWeight = FontWeight.Bold,
            color      = cardColor,
            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun RecurringBadge(recurrenceType: RecurrenceType) {
    Box(
        modifier         = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.25f)),
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
                tint               = Color.White,
                modifier           = Modifier.size(10.dp)
            )
            Text(
                text       = when (recurrenceType) {
                    RecurrenceType.DAILY   -> stringResource(R.string.badge_daily)
                    RecurrenceType.WEEKLY  -> stringResource(R.string.badge_weekly)
                    RecurrenceType.MONTHLY -> stringResource(R.string.badge_monthly)
                    RecurrenceType.YEARLY  -> stringResource(R.string.badge_yearly)
                    RecurrenceType.NONE    -> ""
                },
                fontSize   = 9.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White
            )
        }
    }
}

// ─── Hauptanzeige ─────────────────────────────────────────────────────────────

@Composable
fun CountdownMainDisplay(
    timeInfo   : CountdownInfo,
    units      : List<DisplayUnit>,
    numberSize : TextUnit = 28.sp,
    unitSize   : TextUnit = 16.sp
) {
    val segments = remember(timeInfo, units) { timeInfo.buildDisplaySegments(units) }

    fun pluralRes(value: Long, singular: Int, plural: Int) =
        if (value == 1L) singular else plural

    val labelMap: Map<DisplayUnit, @Composable (Long) -> String> = mapOf(
        DisplayUnit.YEARS   to { v -> stringResource(pluralRes(v, R.string.year,   R.string.years)) },
        DisplayUnit.MONTHS  to { v -> stringResource(pluralRes(v, R.string.month,  R.string.months)) },
        DisplayUnit.WEEKS   to { v -> stringResource(pluralRes(v, R.string.week,   R.string.weeks)) },
        DisplayUnit.DAYS    to { v -> stringResource(pluralRes(v, R.string.day,    R.string.days)) },
        DisplayUnit.HOURS   to { v -> stringResource(pluralRes(v, R.string.hour,   R.string.hours)) },
        DisplayUnit.MINUTES to { v -> stringResource(pluralRes(v, R.string.minute, R.string.minutes)) },
        DisplayUnit.SECONDS to { v -> stringResource(pluralRes(v, R.string.second, R.string.seconds)) },
    )

    val text = buildAnnotatedString {
        segments.forEachIndexed { index, seg ->
            if (index > 0) {
                withStyle(SpanStyle(fontSize = unitSize, color = Color.White.copy(alpha = 0.75f))) {
                    append("  ")
                }
            }
            withStyle(SpanStyle(fontSize = numberSize, fontWeight = FontWeight.Bold, color = Color.White)) {
                val formatted = if (index > 0 && seg.unit in TIME_UNITS)
                    "%02d".format(seg.value)
                else
                    "${seg.value}"
                append(formatted)
            }
            val label = labelMap[seg.unit]?.invoke(seg.value) ?: ""
            withStyle(SpanStyle(fontSize = unitSize, color = Color.White.copy(alpha = 0.85f))) {
                append(" $label")
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
    if (timeInSubInfo) {
        return "%02d:%02d:%02d".format(timeInfo.hours, timeInfo.minutes, timeInfo.seconds)
    }

    val orderedUnits = DisplayFormat.decodeOrdered(countdown.displayFormat)
    val hasTimeUnits = orderedUnits.any { it in TIME_UNITS }

    if (isToday && !hasTimeUnits) return stringResource(R.string.card_today_message)

    if (countdown.isRecurring) return stringResource(
        R.string.card_next_occurrence,
        countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    )

    if (timeInfo.isPast) return ""

    return if (DisplayUnit.DAYS !in orderedUnits || orderedUnits.size > 1) {
        val dayLabel   = stringResource(if (timeInfo.days == 1L) R.string.day else R.string.days)
        val totalLabel = stringResource(R.string.label_total)
        "${timeInfo.days} $dayLabel $totalLabel"
    } else {
        val weekLabel = stringResource(if (timeInfo.weeks == 1L) R.string.week else R.string.weeks)
        "${timeInfo.weeks} $weekLabel"
    }
}