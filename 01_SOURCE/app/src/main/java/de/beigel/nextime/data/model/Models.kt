package de.beigel.nextime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class CountdownDisplayFormat {
    DAYS_ONLY,           // "42 Tage"
    WEEKS_DAYS,          // "6 Wochen, 0 Tage"
    MONTHS_DAYS,          // "1 Monat, 12 Tage"

    YEARS_MONTHS_DAYS     // "2 Jahre, 1 Monat, 3 Tage"
}

// Erinnerungsoptionen
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

@Entity(tableName = "countdowns")
data class Countdown(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateTime: LocalDateTime,
    val includeTime: Boolean = false,
    val showNights: Boolean = false,
    val displayFormat: String = CountdownDisplayFormat.DAYS_ONLY.name,
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

    // Basis-Berechnungen
    val totalDays = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate())
    val years = ChronoUnit.YEARS.between(start.toLocalDate(), end.toLocalDate())
    val months = ChronoUnit.MONTHS.between(start.toLocalDate(), end.toLocalDate())
    val weeks = totalDays / 7

    // Zeit-Komponenten (Stunden, Minuten, Sekunden)
    val totalSeconds = ChronoUnit.SECONDS.between(start, end)
    val daysInSeconds = totalDays * 86400L
    val remainingSeconds = totalSeconds - daysInSeconds

    val hours = remainingSeconds / 3600
    val minutes = (remainingSeconds % 3600) / 60
    val seconds = remainingSeconds % 60

    val nights = if (showNights) {
        totalDays
    } else {
        0L
    }

    return CountdownInfo(
        days = totalDays,
        hours = hours,
        minutes = minutes,
        seconds = seconds,
        weeks = weeks,
        months = months,
        years = years,
        nights = nights,
        isPast = isPast,
    )
}

fun Countdown.getFormattedTime(timeInfo: CountdownInfo): String {
    val format = try {
        CountdownDisplayFormat.valueOf(displayFormat)
    } catch (e: Exception) {
        CountdownDisplayFormat.DAYS_ONLY
    }

    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY -> {
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
        CountdownDisplayFormat.WEEKS_DAYS -> {
            val remainingDays = timeInfo.days % 7
            "${timeInfo.weeks} Wochen, $remainingDays Tage"
        }
        CountdownDisplayFormat.MONTHS_DAYS -> {
            val remainingDays = timeInfo.days - (timeInfo.months * 30)
            "${timeInfo.months} Monate, $remainingDays Tage"
        }
        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
            val remainingDays = timeInfo.days - (timeInfo.months * 30)
            "${timeInfo.months} Monate, $remainingDays Tage"
        }
    }
}

fun Countdown.getFormattedTimeLabel(timeInfo: CountdownInfo): String {
    val format = try {
        CountdownDisplayFormat.valueOf(displayFormat)
    } catch (e: Exception) {
        CountdownDisplayFormat.DAYS_ONLY
    }

    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY -> {
            if (timeInfo.days == 1L) "Tag" else "Tage"
        }
        CountdownDisplayFormat.WEEKS_DAYS -> ""
        CountdownDisplayFormat.MONTHS_DAYS -> ""
        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> TODO()
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