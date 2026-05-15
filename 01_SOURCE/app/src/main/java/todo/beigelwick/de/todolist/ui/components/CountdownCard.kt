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
import todo.beigelwick.de.todolist.data.model.CountdownDisplayFormat
import todo.beigelwick.de.todolist.data.model.CountdownInfo
import todo.beigelwick.de.todolist.data.model.RecurrenceType
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
    val tick by if (countdown.includeTime)
        viewModel.tickSeconds.collectAsState()
    else
        viewModel.tickMinutes.collectAsState()

    val timeInfo = remember(countdown.id, countdown.includeTime, tick) {
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

    // Box + clip statt Card, damit der untere Balken nicht abgeschnitten wird
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(baseColor)
    ) {
        Column {
            // Akzentbalken oben
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
                            RecurringBadge(
                                recurrenceType = countdown.recurrenceType,
                                cardColor      = baseColor
                            )
                        }
                        if (isToday && !countdown.includeTime) {
                            TodayBadge(cardColor = baseColor)
                        }
                    }

                    CountdownMainDisplay(
                        timeInfo      = timeInfo,
                        displayFormat = countdown.displayFormat
                    )

                    if (countdown.includeTime) {
                        Text(
                            text          = timeInfo.formatTime(),
                            fontSize      = 16.sp,
                            fontWeight    = FontWeight.SemiBold,
                            color         = Color.White.copy(alpha = 0.9f),
                            letterSpacing = 1.sp
                        )
                    }

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
                                countdown.effectiveTarget.format(
                                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                                )
                            else
                                countdown.effectiveTarget.format(
                                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                                ),
                            fontSize = 12.sp,
                            color    = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            // Akzentbalken unten – identisch mit dem oberen
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
private fun CountdownMainDisplay(timeInfo: CountdownInfo, displayFormat: String) {
    val format = remember(displayFormat) {
        try { CountdownDisplayFormat.valueOf(displayFormat) }
        catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }
    }

    val dayLabel   = stringResource(if (timeInfo.days   == 1L) R.string.day   else R.string.days)
    val weekLabel  = stringResource(if (timeInfo.weeks  == 1L) R.string.week  else R.string.weeks)
    val monthLabel = stringResource(if (timeInfo.months == 1L) R.string.month else R.string.months)
    val yearLabel  = stringResource(if (timeInfo.years  == 1L) R.string.year  else R.string.years)

    val remainingDaysLabel = stringResource(
        if (timeInfo.remainingDaysAfterWeeks == 1L) R.string.day else R.string.days
    )
    val remainingDaysAfterMonthsLabel = stringResource(
        if (timeInfo.remainingDaysAfterMonths == 1L) R.string.day else R.string.days
    )
    val remainingMonthsLabel = stringResource(
        if (timeInfo.remainingMonthsAfterYears == 1L) R.string.month else R.string.months
    )

    val text = buildAnnotatedString {
        when (format) {
            CountdownDisplayFormat.DAYS_ONLY -> {
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                    append("${timeInfo.days}")
                }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) {
                    append(" $dayLabel")
                }
            }
            CountdownDisplayFormat.WEEKS_DAYS -> {
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                    append("${timeInfo.weeks}")
                }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) {
                    append(" $weekLabel, ")
                }
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                    append("${timeInfo.remainingDaysAfterWeeks}")
                }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) {
                    append(" $remainingDaysLabel")
                }
            }
            CountdownDisplayFormat.MONTHS_DAYS -> {
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                    append("${timeInfo.months}")
                }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) {
                    append(" $monthLabel, ")
                }
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                    append("${timeInfo.remainingDaysAfterMonths}")
                }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) {
                    append(" $remainingDaysAfterMonthsLabel")
                }
            }
            CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                if (timeInfo.years > 0) {
                    withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                        append("${timeInfo.years}")
                    }
                    withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) {
                        append(" $yearLabel, ")
                    }
                }
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) {
                    append("${timeInfo.remainingMonthsAfterYears}")
                }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) {
                    append(" $remainingMonthsLabel")
                }
            }
        }
    }
    Text(text = text, lineHeight = 30.sp)
}

// ─── Hilfsfunktionen ──────────────────────────────────────────────────────────

@Composable
private fun buildSubInfo(
    timeInfo  : CountdownInfo,
    countdown : Countdown,
    isToday   : Boolean
): String {
    if (isToday && !countdown.includeTime) return stringResource(R.string.card_today_message)
    if (countdown.isRecurring) return stringResource(
        R.string.card_next_occurrence,
        countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    )
    if (timeInfo.isPast) return ""

    val format = try { CountdownDisplayFormat.valueOf(countdown.displayFormat) }
    catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }

    val weekLabel  = stringResource(if (timeInfo.weeks == 1L) R.string.week  else R.string.weeks)
    val dayLabel   = stringResource(if (timeInfo.days  == 1L) R.string.day   else R.string.days)
    val totalLabel = stringResource(R.string.label_total)
    val remainingDaysAfterYearsLabel = stringResource(
        if (timeInfo.remainingDaysAfterYears == 1L) R.string.day else R.string.days
    )

    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY         -> "${timeInfo.weeks} $weekLabel"
        CountdownDisplayFormat.WEEKS_DAYS        -> "${timeInfo.days} $dayLabel $totalLabel"
        CountdownDisplayFormat.MONTHS_DAYS       -> "${timeInfo.days} $dayLabel $totalLabel"
        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> "${timeInfo.remainingDaysAfterYears} $remainingDaysAfterYearsLabel"
    }
}