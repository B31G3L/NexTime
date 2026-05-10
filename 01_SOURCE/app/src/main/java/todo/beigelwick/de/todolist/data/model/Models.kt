package todo.beigelwick.de.todolist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// ─── Anzeigeformat ────────────────────────────────────────────────────────────

enum class CountdownDisplayFormat {
    DAYS_ONLY,
    WEEKS_DAYS,
    MONTHS_DAYS,
    YEARS_MONTHS_DAYS
}

// ─── Erinnerungsoptionen ──────────────────────────────────────────────────────

enum class ReminderOption(val minutes: Long) {
    NONE(0),
    AT_TIME(0),
    MINUTES_30(30),
    HOUR_1(60),
    HOURS_3(180),
    HOURS_6(360),
    HOURS_12(720),
    DAY_1(1440),
    DAYS_2(2880),
    DAYS_3(4320),
    WEEK_1(10080),
    WEEKS_2(20160),
    MONTH_1(43200)
}

// ─── Wiederholungstyp ─────────────────────────────────────────────────────────

enum class RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

// ─── Filtermodus ──────────────────────────────────────────────────────────────

enum class FilterMode {
    ALL,
    COUNTDOWN,
    COUNTUP
}

// ─── Countdown Entity ─────────────────────────────────────────────────────────

@Entity(tableName = "countdowns")
data class Countdown(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateTime: LocalDateTime,
    val displayFormat: String = CountdownDisplayFormat.DAYS_ONLY.name,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val color: String = "#FF7043",
    val icon: String = "⏰",
    val notificationEnabled: Boolean = false,
    val reminderOptions: String = "",
    val lastNotificationSent: String? = null,
    val includeTime: Boolean = false,
    val showNights: Boolean = false,
    val recurrence: String = RecurrenceType.NONE.name
) {
    val isCountUp: Boolean
        get() = targetDateTime.isBefore(LocalDateTime.now())

    val recurrenceType: RecurrenceType
        get() = try {
            RecurrenceType.valueOf(recurrence)
        } catch (e: Exception) {
            RecurrenceType.NONE
        }

    val isRecurring: Boolean
        get() = recurrenceType != RecurrenceType.NONE

    /** Nächstes Vorkommen bei wiederkehrenden Einträgen */
    fun nextOccurrence(): LocalDateTime {
        if (!isRecurring) return targetDateTime
        val now = LocalDateTime.now()
        var next = targetDateTime
        while (next.isBefore(now)) {
            next = when (recurrenceType) {
                RecurrenceType.DAILY   -> next.plusDays(1)
                RecurrenceType.WEEKLY  -> next.plusWeeks(1)
                RecurrenceType.MONTHLY -> next.plusMonths(1)
                RecurrenceType.YEARLY  -> next.plusYears(1)
                RecurrenceType.NONE    -> return next
            }
        }
        return next
    }

    /** Effektives Zieldatum – bei Wiederholung das nächste, sonst das gespeicherte */
    val effectiveTarget: LocalDateTime
        get() = if (isRecurring) nextOccurrence() else targetDateTime
}

// ─── CountdownInfo ────────────────────────────────────────────────────────────

data class CountdownInfo(
    val days: Long,
    val weeks: Long,
    val months: Long,
    val years: Long,
    val remainingDaysAfterMonths: Long,
    val remainingDaysAfterYears: Long,
    val remainingMonthsAfterYears: Long,
    val remainingDaysAfterWeeks: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalSeconds: Long,
    val isPast: Boolean
)

// ─── Zeitberechnung ───────────────────────────────────────────────────────────

fun Countdown.calculateTimeRemaining(): CountdownInfo {
    val now    = LocalDateTime.now()
    val target = effectiveTarget

    return if (includeTime) {
        val isPast = target.isBefore(now)
        val start  = if (isPast) target else now
        val end    = if (isPast) now else target

        val totalSeconds = ChronoUnit.SECONDS.between(start, end)
        val totalDays    = totalSeconds / 86400
        val remaining    = totalSeconds % 86400
        val hours        = remaining / 3600
        val minutes      = (remaining % 3600) / 60
        val seconds      = remaining % 60

        val startDate = start.toLocalDate()
        val endDate   = end.toLocalDate()
        val totalMonths              = ChronoUnit.MONTHS.between(startDate, endDate)
        val totalYears               = ChronoUnit.YEARS.between(startDate, endDate)
        val remainingDaysAfterMonths = ChronoUnit.DAYS.between(startDate.plusMonths(totalMonths), endDate)
        val remainingMonthsAfterYears = ChronoUnit.MONTHS.between(startDate.plusYears(totalYears), endDate)
        val remainingDaysAfterYears  = ChronoUnit.DAYS.between(
            startDate.plusYears(totalYears).plusMonths(remainingMonthsAfterYears), endDate
        )

        CountdownInfo(
            days                      = totalDays,
            weeks                     = totalDays / 7,
            months                    = totalMonths,
            years                     = totalYears,
            remainingDaysAfterMonths  = remainingDaysAfterMonths,
            remainingDaysAfterYears   = remainingDaysAfterYears,
            remainingMonthsAfterYears = remainingMonthsAfterYears,
            remainingDaysAfterWeeks   = totalDays % 7,
            hours                     = hours,
            minutes                   = minutes,
            seconds                   = seconds,
            totalSeconds              = totalSeconds,
            isPast                    = isPast
        )
    } else {
        val nowDate    = now.toLocalDate()
        val targetDate = target.toLocalDate()
        val isPast     = targetDate.isBefore(nowDate)

        val start = if (isPast) targetDate else nowDate
        val end   = if (isPast) nowDate else targetDate

        val totalDays                = ChronoUnit.DAYS.between(start, end)
        val totalMonths              = ChronoUnit.MONTHS.between(start, end)
        val totalYears               = ChronoUnit.YEARS.between(start, end)
        val remainingDaysAfterMonths = ChronoUnit.DAYS.between(start.plusMonths(totalMonths), end)
        val remainingMonthsAfterYears = ChronoUnit.MONTHS.between(start.plusYears(totalYears), end)
        val remainingDaysAfterYears  = ChronoUnit.DAYS.between(
            start.plusYears(totalYears).plusMonths(remainingMonthsAfterYears), end
        )

        CountdownInfo(
            days                      = totalDays,
            weeks                     = ChronoUnit.WEEKS.between(start, end),
            months                    = totalMonths,
            years                     = totalYears,
            remainingDaysAfterMonths  = remainingDaysAfterMonths,
            remainingDaysAfterYears   = remainingDaysAfterYears,
            remainingMonthsAfterYears = remainingMonthsAfterYears,
            remainingDaysAfterWeeks   = totalDays % 7,
            hours                     = 0L,
            minutes                   = 0L,
            seconds                   = 0L,
            totalSeconds              = totalDays * 86400,
            isPast                    = isPast
        )
    }
}

// ─── Formatierungs-Extensions ─────────────────────────────────────────────────

fun CountdownInfo.formatTime(): String =
    "%02d:%02d:%02d".format(hours, minutes, seconds)

fun Countdown.getReminderOptionsList(): List<ReminderOption> {
    if (reminderOptions.isEmpty()) return emptyList()
    return reminderOptions.split(",").mapNotNull { name ->
        try { ReminderOption.valueOf(name.trim()) } catch (e: Exception) { null }
    }
}