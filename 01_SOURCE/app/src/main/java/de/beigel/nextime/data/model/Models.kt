package de.beigel.nextime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class CountdownDisplayFormat {
    DAYS_ONLY,
    WEEKS_DAYS,
    MONTHS_DAYS,
    YEARS_MONTHS_DAYS
}

enum class ReminderOption(val displayName: String, val minutes: Long) {
    NONE("Keine", 0),
    AT_TIME("Zum Zeitpunkt", 0),
    DAY_1("1 Tag vorher", 1440),
    DAYS_2("2 Tage vorher", 2880),
    DAYS_3("3 Tage vorher", 4320),
    WEEK_1("1 Woche vorher", 10080),
    WEEKS_2("2 Wochen vorher", 20160),
    MONTH_1("1 Monat vorher", 43200)
}

enum class FilterMode {
    ALL,
    COUNTDOWN,
    COUNTUP
}

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
    val showNights: Boolean = false
) {
    val isCountUp: Boolean
        get() = targetDateTime.isBefore(LocalDateTime.now())
}

data class CountdownInfo(
    val days: Long,
    val weeks: Long,
    val months: Long,
    val years: Long,
    val remainingDaysAfterMonths: Long,
    val remainingDaysAfterYears: Long,
    val remainingMonthsAfterYears: Long,
    val remainingDaysAfterWeeks: Long,
    // Neu: sekundengenaue Zeit-Komponenten
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val totalSeconds: Long,
    val isPast: Boolean
)

fun Countdown.calculateTimeRemaining(): CountdownInfo {
    val now = LocalDateTime.now()

    return if (includeTime) {
        // Sekundengenaue Berechnung
        val isPast = targetDateTime.isBefore(now)
        val start: LocalDateTime = if (isPast) targetDateTime else now
        val end: LocalDateTime   = if (isPast) now else targetDateTime

        val totalSeconds = ChronoUnit.SECONDS.between(start, end)
        val totalDays    = totalSeconds / 86400
        val remaining    = totalSeconds % 86400
        val hours        = remaining / 3600
        val minutes      = (remaining % 3600) / 60
        val seconds      = remaining % 60

        val startDate = start.toLocalDate()
        val endDate   = end.toLocalDate()
        val totalMonths = ChronoUnit.MONTHS.between(startDate, endDate)
        val totalYears  = ChronoUnit.YEARS.between(startDate, endDate)
        val remainingDaysAfterMonths = ChronoUnit.DAYS.between(startDate.plusMonths(totalMonths), endDate)
        val remainingMonthsAfterYears = ChronoUnit.MONTHS.between(startDate.plusYears(totalYears), endDate)
        val remainingDaysAfterYears = ChronoUnit.DAYS.between(
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
        // Nur Datum (wie bisher)
        val nowDate    = now.toLocalDate()
        val targetDate = targetDateTime.toLocalDate()
        val isPast     = targetDate.isBefore(nowDate)

        val start: LocalDate = if (isPast) targetDate else nowDate
        val end: LocalDate   = if (isPast) nowDate else targetDate

        val totalDays   = ChronoUnit.DAYS.between(start, end)
        val totalMonths = ChronoUnit.MONTHS.between(start, end)
        val totalYears  = ChronoUnit.YEARS.between(start, end)
        val remainingDaysAfterMonths = ChronoUnit.DAYS.between(start.plusMonths(totalMonths), end)
        val remainingMonthsAfterYears = ChronoUnit.MONTHS.between(start.plusYears(totalYears), end)
        val remainingDaysAfterYears = ChronoUnit.DAYS.between(
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

// Formatiert HH:MM:SS
fun CountdownInfo.formatTime(): String =
    "%02d:%02d:%02d".format(hours, minutes, seconds)

fun Countdown.getFormattedTime(timeInfo: CountdownInfo): String {
    val format = try { CountdownDisplayFormat.valueOf(displayFormat) }
    catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }
    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY          -> "${timeInfo.days}"
        CountdownDisplayFormat.WEEKS_DAYS         -> "${timeInfo.weeks} Wochen, ${timeInfo.remainingDaysAfterWeeks} Tage"
        CountdownDisplayFormat.MONTHS_DAYS        -> "${timeInfo.months} Monate, ${timeInfo.remainingDaysAfterMonths} Tage"
        CountdownDisplayFormat.YEARS_MONTHS_DAYS  -> "${timeInfo.years} Jahre, ${timeInfo.remainingMonthsAfterYears} Monate, ${timeInfo.remainingDaysAfterYears} Tage"
    }
}

fun Countdown.getFormattedTimeLabel(timeInfo: CountdownInfo): String {
    val format = try { CountdownDisplayFormat.valueOf(displayFormat) }
    catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }
    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY -> if (timeInfo.days == 1L) "Tag" else "Tage"
        else -> ""
    }
}

fun CountdownInfo.toDisplayString(): String = buildString {
    if (isPast) append("vor ")
    if (days > 0) { append("$days Tag"); if (days > 1) append("e") }
}

fun Countdown.getReminderOptionsList(): List<ReminderOption> {
    if (reminderOptions.isEmpty()) return emptyList()
    return reminderOptions.split(",").mapNotNull { name ->
        try { ReminderOption.valueOf(name.trim()) } catch (e: Exception) { null }
    }
}