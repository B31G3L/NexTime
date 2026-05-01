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

// Filtermodus für die Hauptliste
enum class FilterMode {
    ALL,        // Countdown + Count-up gemischt (Countdowns oben, Count-ups unten)
    COUNTDOWN,  // Nur zukünftige Einträge
    COUNTUP     // Nur vergangene Einträge
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
    // Computed property: true wenn Zieldatum in der Vergangenheit liegt
    val isCountUp: Boolean
        get() = targetDateTime.toLocalDate().isBefore(LocalDate.now())
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
    val isPast: Boolean
)

fun Countdown.calculateTimeRemaining(): CountdownInfo {
    val now = LocalDateTime.now().toLocalDate()
    val targetDate = targetDateTime.toLocalDate()
    val isPast = targetDate.isBefore(now)

    val start: LocalDate = if (isPast) targetDate else now
    val end: LocalDate   = if (isPast) now        else targetDate

    val totalDays    = ChronoUnit.DAYS.between(start, end)
    val totalWeeks   = ChronoUnit.WEEKS.between(start, end)
    val totalMonths  = ChronoUnit.MONTHS.between(start, end)
    val totalYears   = ChronoUnit.YEARS.between(start, end)

    val remainingDaysAfterMonths = ChronoUnit.DAYS.between(
        start.plusMonths(totalMonths), end
    )
    val remainingMonthsAfterYears = ChronoUnit.MONTHS.between(
        start.plusYears(totalYears), end
    )
    val remainingDaysAfterYears = ChronoUnit.DAYS.between(
        start.plusYears(totalYears).plusMonths(remainingMonthsAfterYears), end
    )
    val remainingDaysAfterWeeks = totalDays % 7

    return CountdownInfo(
        days                      = totalDays,
        weeks                     = totalWeeks,
        months                    = totalMonths,
        years                     = totalYears,
        remainingDaysAfterMonths  = remainingDaysAfterMonths,
        remainingDaysAfterYears   = remainingDaysAfterYears,
        remainingMonthsAfterYears = remainingMonthsAfterYears,
        remainingDaysAfterWeeks   = remainingDaysAfterWeeks,
        isPast                    = isPast
    )
}

fun Countdown.getFormattedTime(timeInfo: CountdownInfo): String {
    val format = try {
        CountdownDisplayFormat.valueOf(displayFormat)
    } catch (e: Exception) {
        CountdownDisplayFormat.DAYS_ONLY
    }
    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY ->
            "${timeInfo.days}"
        CountdownDisplayFormat.WEEKS_DAYS ->
            "${timeInfo.weeks} Wochen, ${timeInfo.remainingDaysAfterWeeks} Tage"
        CountdownDisplayFormat.MONTHS_DAYS ->
            "${timeInfo.months} Monate, ${timeInfo.remainingDaysAfterMonths} Tage"
        CountdownDisplayFormat.YEARS_MONTHS_DAYS ->
            "${timeInfo.years} Jahre, ${timeInfo.remainingMonthsAfterYears} Monate, ${timeInfo.remainingDaysAfterYears} Tage"
    }
}

fun Countdown.getFormattedTimeLabel(timeInfo: CountdownInfo): String {
    val format = try {
        CountdownDisplayFormat.valueOf(displayFormat)
    } catch (e: Exception) {
        CountdownDisplayFormat.DAYS_ONLY
    }
    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY -> if (timeInfo.days == 1L) "Tag" else "Tage"
        else -> ""
    }
}

fun CountdownInfo.toDisplayString(): String {
    return buildString {
        if (isPast) append("vor ")
        if (days > 0) {
            append("$days Tag")
            if (days > 1) append("e")
        }
    }
}

fun Countdown.getReminderOptionsList(): List<ReminderOption> {
    if (reminderOptions.isEmpty()) return emptyList()
    return reminderOptions.split(",").mapNotNull { name ->
        try { ReminderOption.valueOf(name.trim()) } catch (e: Exception) { null }
    }
}