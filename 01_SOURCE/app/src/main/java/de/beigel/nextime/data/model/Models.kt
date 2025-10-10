package de.beigel.nextime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// Anzeigeformate für Countdowns
enum class CountdownDisplayFormat {
    DAYS_ONLY,           // "42 Tage"
    DAYS_HOURS,          // "42 Tage, 5h 30m"
    HOURS_MINUTES,       // "1020h 30m"
    FULL_TIME,           // "42 Tage, 5h 30m 45s"
    WEEKS_DAYS,          // "6 Wochen, 0 Tage"
    MONTHS_DAYS          // "1 Monat, 12 Tage"
}

@Entity(tableName = "countdowns")
data class Countdown(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateTime: LocalDateTime,
    val includeTime: Boolean = false,
    val showNights: Boolean = false,
    val displayFormat: String = CountdownDisplayFormat.DAYS_ONLY.name,  // NEU
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val color: String = "#FF7043" // Orange als Standard
)

// Hilfsfunktionen für Countdown-Berechnungen
data class CountdownInfo(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val weeks: Long,
    val months: Long,
    val nights: Long,
    val isPast: Boolean
)

fun Countdown.calculateTimeRemaining(): CountdownInfo {
    val now = LocalDateTime.now()
    val isPast = targetDateTime.isBefore(now)

    // WICHTIG: Für vergangene Daten ohne Uhrzeit, verwende nur das Datum
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

    // Tagesberechnung: Verwende ChronoUnit.DAYS für präzise Berechnung
    val days = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate())

    // Wochen und Monate berechnen
    val weeks = days / 7
    val months = ChronoUnit.MONTHS.between(start.toLocalDate(), end.toLocalDate())
    val remainingDaysAfterMonths = days - (months * 30) // Approximation

    // Für die Zeit-Berechnung (Stunden, Minuten, Sekunden)
    val totalSeconds = ChronoUnit.SECONDS.between(start, end)
    val remainingSecondsAfterDays = totalSeconds - (days * 86400)

    val hours = remainingSecondsAfterDays / 3600
    val minutes = (remainingSecondsAfterDays % 3600) / 60
    val seconds = remainingSecondsAfterDays % 60

    // Gesamtstunden berechnen (für HOURS_MINUTES Format)
    val totalHours = totalSeconds / 3600

    // Nächte berechnen
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
        nights = nights,
        isPast = isPast
    )
}

// Formatierung basierend auf dem gewählten Format
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
        CountdownDisplayFormat.DAYS_ONLY
    }

    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY -> {
            if (timeInfo.days == 1L) "Tag" else "Tage"
        }
        CountdownDisplayFormat.WEEKS_DAYS -> ""
        CountdownDisplayFormat.MONTHS_DAYS -> ""
        else -> ""
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