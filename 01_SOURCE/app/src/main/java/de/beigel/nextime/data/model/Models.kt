package de.beigel.nextime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// Anzeigeformate für Countdowns
enum class CountdownDisplayFormat {
    FULL_DETAILED,       // "1 Jahr, 2 Monate, 15 Tage, 5h 30m 45s" (NEU - Standard)
    DAYS_ONLY,           // "42 Tage"
    DAYS_HOURS,          // "42 Tage, 5h 30m"
    HOURS_MINUTES,       // "1020h 30m"
    FULL_TIME,           // "42 Tage, 5h 30m 45s"
    WEEKS_DAYS,          // "6 Wochen, 0 Tage"
    MONTHS_DAYS          // "1 Monat, 12 Tage"
}

// Erinnerungsoptionen
enum class ReminderOption(val displayName: String, val minutes: Long) {
    NONE("Keine", 0),
    AT_TIME("Zum Zeitpunkt", 0),
    MINUTES_5("5 Minuten vorher", 5),
    MINUTES_15("15 Minuten vorher", 15),
    MINUTES_30("30 Minuten vorher", 30),
    HOUR_1("1 Stunde vorher", 60),
    HOURS_3("3 Stunden vorher", 180),
    HOURS_6("6 Stunden vorher", 360),
    HOURS_12("12 Stunden vorher", 720),
    DAY_1("1 Tag vorher", 1440),
    DAYS_2("2 Tage vorher", 2880),
    DAYS_3("3 Tage vorher", 4320),
    WEEK_1("1 Woche vorher", 10080),
    WEEKS_2("2 Wochen vorher", 20160),
    MONTH_1("1 Monat vorher", 43200)
}

@Entity(tableName = "countdowns")
data class Countdown(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateTime: LocalDateTime,
    val includeTime: Boolean = false,
    val showNights: Boolean = false,
    val displayFormat: String = CountdownDisplayFormat.FULL_DETAILED.name,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val color: String = "#FF7043",

    // Notifikationen
    val notificationEnabled: Boolean = false,
    val reminderOptions: String = "",
    val lastNotificationSent: String? = null
)

// Hilfsfunktionen für Countdown-Berechnungen
data class CountdownInfo(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val weeks: Long,
    val months: Long,
    val years: Long,
    val nights: Long,
    val isPast: Boolean
)

fun Countdown.calculateTimeRemaining(): CountdownInfo {
    val now = LocalDateTime.now()
    val isPast = targetDateTime.isBefore(now)

    val start = if (isPast) {
        if (!includeTime) targetDateTime.toLocalDate().atStartOfDay()
        else targetDateTime
    } else {
        now
    }

    val end = if (isPast) {
        now
    } else {
        if (!includeTime) targetDateTime.toLocalDate().atStartOfDay()
        else targetDateTime
    }

    val days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate())
    val weeks = days / 7
    val months = ChronoUnit.MONTHS.between(start.toLocalDate(), end.toLocalDate())
    val years = ChronoUnit.YEARS.between(start.toLocalDate(), end.toLocalDate())

    val totalSeconds = ChronoUnit.SECONDS.between(start, end)
    val remainingSecondsAfterDays = totalSeconds - (days * 86400)

    val hours = remainingSecondsAfterDays / 3600
    val minutes = (remainingSecondsAfterDays % 3600) / 60
    val seconds = remainingSecondsAfterDays % 60

    val totalHours = totalSeconds / 3600

    val nights = if (showNights) {
        ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate())
    } else {
        0L
    }

    return CountdownInfo(
        days = days,
        hours = if (includeTime) hours else totalHours,
        minutes = if (includeTime) minutes else (totalSeconds % 3600) / 60,
        seconds = seconds,
        weeks = weeks,
        months = months,
        years = years,
        nights = nights,
        isPast = isPast
    )
}

fun Countdown.getFormattedTime(timeInfo: CountdownInfo): String {
    val format = try {
        CountdownDisplayFormat.valueOf(displayFormat)
    } catch (e: Exception) {
        CountdownDisplayFormat.FULL_DETAILED
    }

    return when (format) {
        CountdownDisplayFormat.FULL_DETAILED -> {
            buildString {
                if (timeInfo.years > 0) {
                    append("${timeInfo.years}J ")
                }
                if (timeInfo.months > 0 || timeInfo.years > 0) {
                    val remainingMonths = timeInfo.months % 12
                    if (remainingMonths > 0) {
                        append("${remainingMonths}M ")
                    }
                }
                val remainingDays = timeInfo.days % 30
                if (remainingDays > 0 || (timeInfo.years == 0L && timeInfo.months == 0L)) {
                    append("${remainingDays}T ")
                }
                append(String.format("%02d:%02d:%02d", timeInfo.hours, timeInfo.minutes, timeInfo.seconds))
            }.trim()
        }
        CountdownDisplayFormat.DAYS_ONLY -> {
            "${timeInfo.days}"
        }
        CountdownDisplayFormat.DAYS_HOURS -> {
            if (timeInfo.days > 0) {
                "${timeInfo.days} Tage, ${timeInfo.hours}h ${timeInfo.minutes}m"
            } else {
                "${timeInfo.hours}h ${timeInfo.minutes}m"
            }
        }
        CountdownDisplayFormat.HOURS_MINUTES -> {
            "${timeInfo.hours}h ${timeInfo.minutes}m"
        }
        CountdownDisplayFormat.FULL_TIME -> {
            "${timeInfo.days} Tage, ${timeInfo.hours}h ${timeInfo.minutes}m ${timeInfo.seconds}s"
        }
        CountdownDisplayFormat.WEEKS_DAYS -> {
            val remainingDays = timeInfo.days % 7
            "${timeInfo.weeks} Wochen, $remainingDays Tage"
        }
        CountdownDisplayFormat.MONTHS_DAYS -> {
            val remainingDays = timeInfo.days - (timeInfo.months * 30)
            "${timeInfo.months} Monate, $remainingDays Tage"
        }
    }
}

fun Countdown.getFormattedTimeLabel(timeInfo: CountdownInfo): String {
    val format = try {
        CountdownDisplayFormat.valueOf(displayFormat)
    } catch (e: Exception) {
        CountdownDisplayFormat.FULL_DETAILED
    }

    return when (format) {
        CountdownDisplayFormat.FULL_DETAILED -> ""
        CountdownDisplayFormat.DAYS_ONLY -> {
            if (timeInfo.days == 1L) "Tag" else "Tage"
        }
        CountdownDisplayFormat.DAYS_HOURS -> ""
        CountdownDisplayFormat.HOURS_MINUTES -> ""
        CountdownDisplayFormat.FULL_TIME -> ""
        CountdownDisplayFormat.WEEKS_DAYS -> ""
        CountdownDisplayFormat.MONTHS_DAYS -> ""
    }
}

fun CountdownInfo.toDisplayString(includeTime: Boolean, showNights: Boolean): String {
    return buildString {
        if (isPast) append("vor ")

        if (days > 0) {
            append("$days Tag")
            if (days > 1) append("e")
        }

        if (includeTime) {
            if (days > 0) append(", ")
            append("${hours}h ${minutes}m")
        }

        if (showNights && nights > 0) {
            append(" (${nights} Nacht")
            if (nights > 1) append("e")
            append(")")
        }
    }
}

// Hilfsfunktionen für Erinnerungen
fun Countdown.getReminderOptionsList(): List<ReminderOption> {
    if (reminderOptions.isEmpty()) return emptyList()

    return reminderOptions.split(",").mapNotNull { name ->
        try {
            ReminderOption.valueOf(name.trim())
        } catch (e: Exception) {
            null
        }
    }
}