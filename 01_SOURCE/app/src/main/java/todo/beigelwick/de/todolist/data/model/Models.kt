package todo.beigelwick.de.todolist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// ─── Anzeigeeinheiten ─────────────────────────────────────────────────────────

enum class DisplayUnit {
    YEARS, MONTHS, WEEKS, DAYS, HOURS, MINUTES, SECONDS
}

/** Einheiten, die nur bei includeTime = true Sinn ergeben */
val TIME_UNITS = setOf(DisplayUnit.HOURS, DisplayUnit.MINUTES, DisplayUnit.SECONDS)

/** Kanonische Sortierreihenfolge groß → klein */
val DISPLAY_UNIT_ORDER = listOf(
    DisplayUnit.YEARS, DisplayUnit.MONTHS, DisplayUnit.WEEKS, DisplayUnit.DAYS,
    DisplayUnit.HOURS, DisplayUnit.MINUTES, DisplayUnit.SECONDS
)

object DisplayFormat {

    fun encode(units: Set<DisplayUnit>): String =
        if (units.isEmpty()) DisplayUnit.DAYS.name
        else units.joinToString(",") { it.name }

    fun decode(raw: String): Set<DisplayUnit> {
        if (raw.isBlank()) return setOf(DisplayUnit.DAYS)
        // Legacy-Kompatibilität mit alten CountdownDisplayFormat-Werten
        return when (raw.trim()) {
            "DAYS_ONLY"         -> setOf(DisplayUnit.DAYS)
            "WEEKS_DAYS"        -> setOf(DisplayUnit.WEEKS, DisplayUnit.DAYS)
            "MONTHS_DAYS"       -> setOf(DisplayUnit.MONTHS, DisplayUnit.DAYS)
            "YEARS_MONTHS_DAYS" -> setOf(DisplayUnit.YEARS, DisplayUnit.MONTHS, DisplayUnit.DAYS)
            else -> raw.split(",").mapNotNull { name ->
                try { DisplayUnit.valueOf(name.trim()) } catch (e: Exception) { null }
            }.toSet().ifEmpty { setOf(DisplayUnit.DAYS) }
        }
    }

    fun sorted(units: Set<DisplayUnit>): List<DisplayUnit> =
        DISPLAY_UNIT_ORDER.filter { it in units }
}

// ─── Erinnerungsoptionen ──────────────────────────────────────────────────────

enum class ReminderOption(val minutes: Long) {
    NONE(0), AT_TIME(0), MINUTES_30(30), HOUR_1(60), HOURS_3(180),
    HOURS_6(360), HOURS_12(720), DAY_1(1440), DAYS_2(2880), DAYS_3(4320),
    WEEK_1(10080), WEEKS_2(20160), MONTH_1(43200)
}

// ─── Wiederholungstyp ─────────────────────────────────────────────────────────

enum class RecurrenceType { NONE, DAILY, WEEKLY, MONTHLY, YEARLY }

// ─── Filtermodus ──────────────────────────────────────────────────────────────

enum class FilterMode { ALL, COUNTDOWN, COUNTUP }

// ─── Countdown Entity ─────────────────────────────────────────────────────────

@Entity(tableName = "countdowns")
data class Countdown(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetDateTime: LocalDateTime,
    val displayFormat: String = DisplayUnit.DAYS.name,
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
        get() = try { RecurrenceType.valueOf(recurrence) } catch (e: Exception) { RecurrenceType.NONE }

    val isRecurring: Boolean
        get() = recurrenceType != RecurrenceType.NONE

    val activeDisplayUnits: Set<DisplayUnit>
        get() = DisplayFormat.decode(displayFormat)

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

    val effectiveTarget: LocalDateTime
        get() = if (isRecurring) nextOccurrence() else targetDateTime
}

// ─── CountdownInfo ────────────────────────────────────────────────────────────

data class CountdownInfo(
    // ── Gesamtwerte ───────────────────────────────────────────────────────────
    val totalSeconds : Long,
    val totalMinutes : Long,
    val totalHours   : Long,
    val days         : Long,
    val weeks        : Long,
    val months       : Long,
    val years        : Long,
    // ── Restwerte für Kombinationsanzeige ────────────────────────────────────
    val remSecondsAfterMinutes : Long,  // Sek. nach vollen Minuten
    val remMinutesAfterHours   : Long,  // Min. nach vollen Stunden
    val remHoursAfterDays      : Long,  // Std. nach vollen Tagen
    val remDaysAfterWeeks      : Long,  // Tage nach vollen Wochen
    val remDaysAfterMonths     : Long,  // Tage nach vollen Monaten
    val remWeeksAfterMonths    : Long,  // Wochen nach vollen Monaten
    val remMonthsAfterYears    : Long,  // Monate nach vollen Jahren
    val remDaysAfterYears      : Long,  // Tage nach Jahren+Monaten
    // ── Für formatTime() (HH:mm:ss) ─────────────────────────────────────────
    val hours   : Long,
    val minutes : Long,
    val seconds : Long,
    val isPast  : Boolean
) {
    // Rückwärtskompatible Aliase
    val remainingDaysAfterMonths  get() = remDaysAfterMonths
    val remainingDaysAfterYears   get() = remDaysAfterYears
    val remainingMonthsAfterYears get() = remMonthsAfterYears
    val remainingDaysAfterWeeks   get() = remDaysAfterWeeks
    val remainingWeeksAfterMonths get() = remWeeksAfterMonths
}

// ─── Zeitberechnung ───────────────────────────────────────────────────────────

fun Countdown.calculateTimeRemaining(): CountdownInfo {
    val now    = LocalDateTime.now()
    val target = effectiveTarget

    val isPast = target.isBefore(now)
    val start  = if (isPast) target else now
    val end    = if (isPast) now    else target

    val totalSec  = ChronoUnit.SECONDS.between(start, end)
    val totalMin  = totalSec / 60
    val totalHrs  = totalSec / 3600
    val totalDays = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate())

    val startDate = start.toLocalDate()
    val endDate   = end.toLocalDate()

    val totalMonths = ChronoUnit.MONTHS.between(startDate, endDate)
    val totalYears  = ChronoUnit.YEARS.between(startDate, endDate)
    val totalWeeks  = ChronoUnit.WEEKS.between(startDate, endDate)

    val remDaysAfterMonths  = ChronoUnit.DAYS.between(startDate.plusMonths(totalMonths), endDate)
    val remMonthsAfterYears = ChronoUnit.MONTHS.between(startDate.plusYears(totalYears), endDate)
    val remDaysAfterYears   = ChronoUnit.DAYS.between(
        startDate.plusYears(totalYears).plusMonths(remMonthsAfterYears), endDate
    )
    val remWeeksAfterMonths = ChronoUnit.WEEKS.between(startDate.plusMonths(totalMonths), endDate)
    val remDaysAfterWeeks   = totalDays % 7

    val remSecondsAfterMinutes = totalSec % 60
    val remMinutesAfterHours   = (totalSec % 3600) / 60
    val remHoursAfterDays      = (totalSec % 86400) / 3600

    // Separate Uhrzeitstellen für formatTime()
    val timePart = totalSec % 86400
    val hrs  = timePart / 3600
    val mins = (timePart % 3600) / 60
    val secs = timePart % 60

    return CountdownInfo(
        totalSeconds           = totalSec,
        totalMinutes           = totalMin,
        totalHours             = totalHrs,
        days                   = totalDays,
        weeks                  = totalWeeks,
        months                 = totalMonths,
        years                  = totalYears,
        remSecondsAfterMinutes = remSecondsAfterMinutes,
        remMinutesAfterHours   = remMinutesAfterHours,
        remHoursAfterDays      = remHoursAfterDays,
        remDaysAfterWeeks      = remDaysAfterWeeks,
        remDaysAfterMonths     = remDaysAfterMonths,
        remWeeksAfterMonths    = remWeeksAfterMonths,
        remMonthsAfterYears    = remMonthsAfterYears,
        remDaysAfterYears      = remDaysAfterYears,
        hours                  = hrs,
        minutes                = mins,
        seconds                = secs,
        isPast                 = isPast
    )
}

// ─── Formatierung ─────────────────────────────────────────────────────────────

fun CountdownInfo.formatTime(): String =
    "%02d:%02d:%02d".format(hours, minutes, seconds)

fun Countdown.getReminderOptionsList(): List<ReminderOption> {
    if (reminderOptions.isEmpty()) return emptyList()
    return reminderOptions.split(",").mapNotNull { name ->
        try { ReminderOption.valueOf(name.trim()) } catch (e: Exception) { null }
    }
}

// ─── Anzeigesegmente ──────────────────────────────────────────────────────────

data class DisplaySegment(val value: Long, val unit: DisplayUnit)

/**
 * Berechnet die Segmente für die Anzeige.
 * Jede Einheit zeigt den Restwert relativ zur vorherigen größeren Einheit.
 */
fun CountdownInfo.buildDisplaySegments(units: Set<DisplayUnit>): List<DisplaySegment> {
    val sorted = DisplayFormat.sorted(units)
    if (sorted.isEmpty()) return listOf(DisplaySegment(days, DisplayUnit.DAYS))
    return sorted.mapIndexed { index, unit ->
        val prev  = if (index > 0) sorted[index - 1] else null
        DisplaySegment(getValueFor(unit, prev), unit)
    }
}

private fun CountdownInfo.getValueFor(unit: DisplayUnit, prev: DisplayUnit?): Long = when {
    prev == null -> when (unit) {
        DisplayUnit.YEARS   -> years
        DisplayUnit.MONTHS  -> months
        DisplayUnit.WEEKS   -> weeks
        DisplayUnit.DAYS    -> days
        DisplayUnit.HOURS   -> totalHours
        DisplayUnit.MINUTES -> totalMinutes
        DisplayUnit.SECONDS -> totalSeconds
    }
    prev == DisplayUnit.YEARS   && unit == DisplayUnit.MONTHS  -> remMonthsAfterYears
    prev == DisplayUnit.YEARS   && unit == DisplayUnit.WEEKS   -> remDaysAfterYears / 7
    prev == DisplayUnit.YEARS   && unit == DisplayUnit.DAYS    -> remDaysAfterYears
    prev == DisplayUnit.YEARS   && unit == DisplayUnit.HOURS   -> remDaysAfterYears * 24 + remHoursAfterDays
    prev == DisplayUnit.YEARS   && unit == DisplayUnit.MINUTES -> remDaysAfterYears * 1440 + remHoursAfterDays * 60 + remMinutesAfterHours
    prev == DisplayUnit.YEARS   && unit == DisplayUnit.SECONDS -> remDaysAfterYears * 86400 + remHoursAfterDays * 3600 + remMinutesAfterHours * 60 + remSecondsAfterMinutes
    prev == DisplayUnit.MONTHS  && unit == DisplayUnit.WEEKS   -> remWeeksAfterMonths
    prev == DisplayUnit.MONTHS  && unit == DisplayUnit.DAYS    -> remDaysAfterMonths
    prev == DisplayUnit.MONTHS  && unit == DisplayUnit.HOURS   -> remDaysAfterMonths * 24 + remHoursAfterDays
    prev == DisplayUnit.MONTHS  && unit == DisplayUnit.MINUTES -> remDaysAfterMonths * 1440 + remHoursAfterDays * 60 + remMinutesAfterHours
    prev == DisplayUnit.MONTHS  && unit == DisplayUnit.SECONDS -> remDaysAfterMonths * 86400 + remHoursAfterDays * 3600 + remMinutesAfterHours * 60 + remSecondsAfterMinutes
    prev == DisplayUnit.WEEKS   && unit == DisplayUnit.DAYS    -> remDaysAfterWeeks
    prev == DisplayUnit.WEEKS   && unit == DisplayUnit.HOURS   -> remDaysAfterWeeks * 24 + remHoursAfterDays
    prev == DisplayUnit.WEEKS   && unit == DisplayUnit.MINUTES -> remDaysAfterWeeks * 1440 + remHoursAfterDays * 60 + remMinutesAfterHours
    prev == DisplayUnit.WEEKS   && unit == DisplayUnit.SECONDS -> remDaysAfterWeeks * 86400 + remHoursAfterDays * 3600 + remMinutesAfterHours * 60 + remSecondsAfterMinutes
    prev == DisplayUnit.DAYS    && unit == DisplayUnit.HOURS   -> remHoursAfterDays
    prev == DisplayUnit.DAYS    && unit == DisplayUnit.MINUTES -> remHoursAfterDays * 60 + remMinutesAfterHours
    prev == DisplayUnit.DAYS    && unit == DisplayUnit.SECONDS -> remHoursAfterDays * 3600 + remMinutesAfterHours * 60 + remSecondsAfterMinutes
    prev == DisplayUnit.HOURS   && unit == DisplayUnit.MINUTES -> remMinutesAfterHours
    prev == DisplayUnit.HOURS   && unit == DisplayUnit.SECONDS -> remMinutesAfterHours * 60 + remSecondsAfterMinutes
    prev == DisplayUnit.MINUTES && unit == DisplayUnit.SECONDS -> remSecondsAfterMinutes
    else -> when (unit) {
        DisplayUnit.YEARS   -> years
        DisplayUnit.MONTHS  -> months
        DisplayUnit.WEEKS   -> weeks
        DisplayUnit.DAYS    -> days
        DisplayUnit.HOURS   -> totalHours
        DisplayUnit.MINUTES -> totalMinutes
        DisplayUnit.SECONDS -> totalSeconds
    }
}