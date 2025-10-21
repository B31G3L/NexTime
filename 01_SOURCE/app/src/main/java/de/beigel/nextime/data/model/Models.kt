package de.beigel.nextime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class CountdownDisplayFormat {
    DAYS_ONLY,           // "42 Tage"
    WEEKS_DAYS,          // "6 Wochen, 0 Tage"
    MONTHS_DAYS,         // "1 Monat, 12 Tage"
    YEARS_MONTHS_DAYS    // "2 Jahre, 1 Monat, 3 Tage"
}

// Erinnerungsoptionen (vorerst ohne zeitbasierte Erinnerungen)
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
    val targetDateTime: LocalDateTime,  // DB-Feld bleibt, wird aber OHNE Zeit verwendet
    val displayFormat: String = CountdownDisplayFormat.DAYS_ONLY.name,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val color: String = "#FF7043",

    // Notifikationen
    val notificationEnabled: Boolean = false,
    val reminderOptions: String = "",
    val lastNotificationSent: String? = null,

    // Zukünftige Felder (noch nicht genutzt, aber in DB reserviert)
    val includeTime: Boolean = false,
    val showNights: Boolean = false
)

// Hilfsfunktionen für Countdown-Berechnungen
data class CountdownInfo(
    val days: Long,
    val weeks: Long,
    val months: Long,
    val years: Long,
    val isPast: Boolean
)

fun Countdown.calculateTimeRemaining(): CountdownInfo {
    val now = LocalDateTime.now().toLocalDate().atStartOfDay()
    val targetDate = targetDateTime.toLocalDate().atStartOfDay()
    val isPast = targetDate.isBefore(now)

    val start = if (isPast) targetDate else now
    val end = if (isPast) now else targetDate

    // Basis-Berechnungen (nur Tage, keine Uhrzeiten)
    val totalDays = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate())
    val years = ChronoUnit.YEARS.between(start.toLocalDate(), end.toLocalDate())
    val months = ChronoUnit.MONTHS.between(start.toLocalDate(), end.toLocalDate())
    val weeks = totalDays / 7

    return CountdownInfo(
        days = totalDays,
        weeks = weeks,
        months = months,
        years = years,
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
            "${timeInfo.days}"
        }

        CountdownDisplayFormat.WEEKS_DAYS -> {
            val remainingDays = timeInfo.days % 7
            "${timeInfo.weeks} Wochen, $remainingDays Tage"
        }

        CountdownDisplayFormat.MONTHS_DAYS -> {
            val remainingDays = timeInfo.days % 30
            "${timeInfo.months} Monate, $remainingDays Tage"
        }

        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
            val remainingMonths = timeInfo.months % 12
            val remainingDays = timeInfo.days % 30
            "${timeInfo.years} Jahre, $remainingMonths Monate, $remainingDays Tage"
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
        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> ""
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