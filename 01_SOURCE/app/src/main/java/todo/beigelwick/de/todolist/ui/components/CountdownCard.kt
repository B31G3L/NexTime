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
import java.time.temporal.ChronoUnit

@Composable
fun CountdownCard(
    countdown : Countdown,
    viewModel : CountdownViewModel = viewModel()
) {
    // Zentralen Ticker abonnieren statt eigenem Timer
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
    val progress = remember(countdown.id, countdown.createdAt, countdown.effectiveTarget) {
        calculateProgress(countdown)
    }
    val isToday = remember(countdown.id, countdown.effectiveTarget) {
        countdown.effectiveTarget.toLocalDate() == LocalDate.now()
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = baseColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                // Icon
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
                            RecurringBadge(recurrenceType = countdown.recurrenceType, cardColor = baseColor)
                        }
                        if (isToday && !countdown.includeTime) {
                            TodayBadge(cardColor = baseColor)
                        }
                    }

                    // Hauptanzeige
                    CountdownMainDisplay(timeInfo = timeInfo, displayFormat = countdown.displayFormat)

                    // Uhrzeit
                    if (countdown.includeTime) {
                        Text(
                            text         = timeInfo.formatTime(),
                            fontSize     = 16.sp,
                            fontWeight   = FontWeight.SemiBold,
                            color        = Color.White.copy(alpha = 0.9f),
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

            // Fortschrittsbalken
            ProgressBar(
                progress         = progress,
                backgroundColor  = darkerBar,
                foregroundColor  = Color.White.copy(alpha = 0.55f)
            )
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

// ─── Fortschrittsbalken ───────────────────────────────────────────────────────

@Composable
private fun ProgressBar(progress: Float, backgroundColor: Color, foregroundColor: Color) {
    Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(backgroundColor)) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(foregroundColor)
        )
    }
}

// ─── Hauptanzeige ─────────────────────────────────────────────────────────────

@Composable
private fun CountdownMainDisplay(timeInfo: CountdownInfo, displayFormat: String) {
    val format = remember(displayFormat) {
        try { CountdownDisplayFormat.valueOf(displayFormat) }
        catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }
    }

    val text = buildAnnotatedString {
        when (format) {
            CountdownDisplayFormat.DAYS_ONLY -> {
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) { append("${timeInfo.days}") }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) { append(if (timeInfo.days == 1L) " Tag" else " Tage") }
            }
            CountdownDisplayFormat.WEEKS_DAYS -> {
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) { append("${timeInfo.weeks}") }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) { append(if (timeInfo.weeks == 1L) " Woche, " else " Wochen, ") }
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) { append("${timeInfo.remainingDaysAfterWeeks}") }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) { append(if (timeInfo.remainingDaysAfterWeeks == 1L) " Tag" else " Tage") }
            }
            CountdownDisplayFormat.MONTHS_DAYS -> {
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) { append("${timeInfo.months}") }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) { append(if (timeInfo.months == 1L) " Monat, " else " Monate, ") }
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) { append("${timeInfo.remainingDaysAfterMonths}") }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) { append(if (timeInfo.remainingDaysAfterMonths == 1L) " Tag" else " Tage") }
            }
            CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                if (timeInfo.years > 0) {
                    withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) { append("${timeInfo.years}") }
                    withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) { append(if (timeInfo.years == 1L) " Jahr, " else " Jahre, ") }
                }
                withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)) { append("${timeInfo.remainingMonthsAfterYears}") }
                withStyle(SpanStyle(fontSize = 18.sp, color = Color.White)) { append(if (timeInfo.remainingMonthsAfterYears == 1L) " Monat" else " Monate") }
            }
        }
    }
    Text(text = text, lineHeight = 30.sp)
}

// ─── Hilfsfunktionen ──────────────────────────────────────────────────────────

private fun calculateProgress(countdown: Countdown): Float {
    val now       = LocalDate.now()
    val start     = countdown.createdAt.toLocalDate()
    val end       = countdown.effectiveTarget.toLocalDate()
    val totalDays = ChronoUnit.DAYS.between(start, end)
    if (totalDays <= 0L || start.isAfter(end)) return 1f
    val passedDays = ChronoUnit.DAYS.between(start, now)
    return (passedDays.toFloat() / totalDays.toFloat()).coerceIn(0f, 1f)
}

@Composable
private fun buildSubInfo(timeInfo: CountdownInfo, countdown: Countdown, isToday: Boolean): String {
    if (isToday && !countdown.includeTime) return stringResource(R.string.card_today_message)
    if (countdown.isRecurring) return stringResource(
        R.string.card_next_occurrence,
        countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    )
    if (timeInfo.isPast) return stringResource(R.string.card_already_past)
    val format = try { CountdownDisplayFormat.valueOf(countdown.displayFormat) }
    catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }
    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY         -> "${timeInfo.weeks} ${if (timeInfo.weeks == 1L) "Woche" else "Wochen"}"
        CountdownDisplayFormat.WEEKS_DAYS        -> "${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"} gesamt"
        CountdownDisplayFormat.MONTHS_DAYS       -> "${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"} gesamt"
        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> "${timeInfo.remainingDaysAfterYears} ${if (timeInfo.remainingDaysAfterYears == 1L) "Tag" else "Tage"}"
    }
}