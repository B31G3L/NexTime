package todo.beigelwick.de.todolist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.CountdownInfo
import todo.beigelwick.de.todolist.data.model.DisplayUnit
import todo.beigelwick.de.todolist.data.model.RecurrenceType
import todo.beigelwick.de.todolist.data.model.TIME_UNITS
import todo.beigelwick.de.todolist.data.model.buildDisplaySegments
import todo.beigelwick.de.todolist.data.model.calculateTimeRemaining
import todo.beigelwick.de.todolist.data.model.formatTime
import todo.beigelwick.de.todolist.ui.viewmodel.CountdownViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CardShape = RoundedCornerShape(16.dp)

@Composable
fun CountdownCard(
    countdown : Countdown,
    viewModel : CountdownViewModel = viewModel()
) {
    // Tick-Quelle: Sekunden wenn Zeit-Einheiten aktiv oder includeTime, sonst Minuten
    val hasTimeUnits = remember(countdown.displayFormat) {
        countdown.activeDisplayUnits.any { it in TIME_UNITS }
    }
    val tick by if (countdown.includeTime || hasTimeUnits)
        viewModel.tickSeconds.collectAsState()
    else
        viewModel.tickMinutes.collectAsState()

    val timeInfo = remember(countdown.id, countdown.includeTime, countdown.displayFormat, tick) {
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
            .clip(CardShape)
            .background(baseColor)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(darkerBar))

            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = countdown.icon.ifBlank { "⏰" }, fontSize = 26.sp)
                }

                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Titel + Badges
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text     = countdown.title,
                            fontSize = 13.sp,
                            color    = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (countdown.isRecurring) {
                            RecurringBadge(countdown.recurrenceType, baseColor)
                        }
                        if (isToday && !hasTimeUnits) {
                            TodayBadge(baseColor)
                        }
                    }

                    // Hauptanzeige
                    CountdownMainDisplay(
                        timeInfo = timeInfo,
                        units    = countdown.activeDisplayUnits
                    )

                    // HH:mm:ss Zeile – nur wenn includeTime aktiv UND keine Zeit-Einheiten
                    // selbst ausgewählt sind (sonst doppelt)
                    if (countdown.includeTime && !hasTimeUnits) {
                        Text(
                            text          = timeInfo.formatTime(),
                            fontSize      = 16.sp,
                            fontWeight    = FontWeight.SemiBold,
                            color         = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 1.sp
                        )
                    }

                    // Subinfo + Datum
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = buildSubInfo(timeInfo, countdown, isToday),
                            fontSize = 12.sp,
                            color    = Color.White.copy(alpha = 0.65f)
                        )
                        Text(
                            text     = if (countdown.includeTime)
                                countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                            else
                                countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            fontSize = 12.sp,
                            color    = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(darkerBar))
        }
    }
}

// ─── Badges ───────────────────────────────────────────────────────────────────

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
private fun RecurringBadge(recurrenceType: RecurrenceType, cardColor: Color) {
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
fun CountdownMainDisplay(timeInfo: CountdownInfo, units: Set<DisplayUnit>) {
    val segments = remember(timeInfo, units) { timeInfo.buildDisplaySegments(units) }

    // Einheitsbezeichnungen – mit korrekter Pluralisierung
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
                withStyle(SpanStyle(fontSize = 16.sp, color = Color.White.copy(alpha = 0.75f))) {
                    append("  ")
                }
            }
            // Zahl
            withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                // Sekunden/Minuten immer 2-stellig wenn nicht erste Einheit
                val formatted = if (index > 0 && seg.unit in TIME_UNITS)
                    "%02d".format(seg.value)
                else
                    "${seg.value}"
                append(formatted)
            }
            // Einheit
            val label = labelMap[seg.unit]?.invoke(seg.value) ?: ""
            withStyle(SpanStyle(fontSize = 16.sp, color = Color.White.copy(alpha = 0.85f))) {
                append(" $label")
            }
        }
    }

    Text(text = text, lineHeight = 34.sp)
}

// ─── Subinfo ──────────────────────────────────────────────────────────────────

@Composable
private fun buildSubInfo(
    timeInfo  : CountdownInfo,
    countdown : Countdown,
    isToday   : Boolean
): String {
    val hasTimeUnits = countdown.activeDisplayUnits.any { it in TIME_UNITS }
    if (isToday && !hasTimeUnits) return stringResource(R.string.card_today_message)
    if (countdown.isRecurring) return stringResource(
        R.string.card_next_occurrence,
        countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    )
    if (timeInfo.isPast) return ""

    val units = countdown.activeDisplayUnits
    // Subinfo: Gesamttage anzeigen wenn Tage nicht die einzige Einheit sind
    return if (DisplayUnit.DAYS !in units || units.size > 1) {
        val dayLabel   = stringResource(if (timeInfo.days == 1L) R.string.day else R.string.days)
        val totalLabel = stringResource(R.string.label_total)
        "${timeInfo.days} $dayLabel $totalLabel"
    } else {
        val weekLabel = stringResource(if (timeInfo.weeks == 1L) R.string.week else R.string.weeks)
        "${timeInfo.weeks} $weekLabel"
    }
}