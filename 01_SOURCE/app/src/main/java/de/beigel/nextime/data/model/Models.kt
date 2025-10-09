package de.beigel.nextime.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity(tableName = "countdowns")
data class Countdown(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateTime: LocalDateTime,
    val includeTime: Boolean = false,
    val showNights: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val color: String = "#FF7043" // Orange als Standard
)

// Hilfsfunktionen für Countdown-Berechnungen
data class CountdownInfo(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val nights: Long,
    val isPast: Boolean
)

fun Countdown.calculateTimeRemaining(): CountdownInfo {
    val now = LocalDateTime.now()
    val isPast = targetDateTime.isBefore(now)

    val start = if (isPast) targetDateTime else now
    val end = if (isPast) now else targetDateTime

    val totalSeconds = ChronoUnit.SECONDS.between(start, end)
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    // Nächte berechnen (Mitternächte zwischen den Daten)
    val nights = if (showNights) {
        ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate())
    } else {
        0L
    }

    return CountdownInfo(
        days = days,
        hours = if (includeTime) hours else 0,
        minutes = if (includeTime) minutes else 0,
        seconds = if (includeTime) seconds else 0,
        nights = nights,
        isPast = isPast
    )
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