package de.beigel.nextime.ui.components

import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

data class CountdownTemplate(
    val title: String,
    val icon: String,
    val color: String,
    val displayFormat: CountdownDisplayFormat,
    val category: TemplateCategory,
    val targetDateProvider: () -> LocalDate
)

enum class TemplateCategory(val label: String, val emoji: String) {
    FEIERTAGE("Feiertage", "🎉"),
    JAHRESZEITEN("Jahreszeiten", "🌸"),
    SPORT("Sport & Events", "🏆"),
    PERSOENLICH("Persönlich", "❤️"),
    SCHULE("Schule & Arbeit", "📚")
}

val ALL_TEMPLATES: List<CountdownTemplate> = listOf(

    // ── Feiertage ─────────────────────────────────────────────────────────────
    CountdownTemplate(
        title = "Weihnachten",
        icon = "🎄",
        color = "#EF5350",
        displayFormat = CountdownDisplayFormat.DAYS_ONLY,
        category = TemplateCategory.FEIERTAGE,
        targetDateProvider = {
            val now = LocalDate.now()
            val thisYear = LocalDate.of(now.year, Month.DECEMBER, 24)
            if (now > thisYear) LocalDate.of(now.year + 1, Month.DECEMBER, 24)
            else thisYear
        }
    ),
    CountdownTemplate(
        title = "Silvester",
        icon = "🎆",
        color = "#5C6BC0",
        displayFormat = CountdownDisplayFormat.DAYS_ONLY,
        category = TemplateCategory.FEIERTAGE,
        targetDateProvider = {
            val now = LocalDate.now()
            val thisYear = LocalDate.of(now.year, Month.DECEMBER, 31)
            if (now > thisYear) LocalDate.of(now.year + 1, Month.DECEMBER, 31)
            else thisYear
        }
    ),
    CountdownTemplate(
        title = "Neujahr",
        icon = "🥂",
        color = "#FFD700".let { "#FFA726" },
        displayFormat = CountdownDisplayFormat.DAYS_ONLY,
        category = TemplateCategory.FEIERTAGE,
        targetDateProvider = {
            val now = LocalDate.now()
            val thisYear = LocalDate.of(now.year, Month.JANUARY, 1)
            if (now >= thisYear) LocalDate.of(now.year + 1, Month.JANUARY, 1)
            else thisYear
        }
    ),
    CountdownTemplate(
        title = "Valentinstag",
        icon = "💝",
        color = "#EC407A",
        displayFormat = CountdownDisplayFormat.DAYS_ONLY,
        category = TemplateCategory.FEIERTAGE,
        targetDateProvider = {
            val now = LocalDate.now()
            val thisYear = LocalDate.of(now.year, Month.FEBRUARY, 14)
            if (now > thisYear) LocalDate.of(now.year + 1, Month.FEBRUARY, 14)
            else thisYear
        }
    ),
    CountdownTemplate(
        title = "Halloween",
        icon = "🎃",
        color = "#FF7043",
        displayFormat = CountdownDisplayFormat.DAYS_ONLY,
        category = TemplateCategory.FEIERTAGE,
        targetDateProvider = {
            val now = LocalDate.now()
            val thisYear = LocalDate.of(now.year, Month.OCTOBER, 31)
            if (now > thisYear) LocalDate.of(now.year + 1, Month.OCTOBER, 31)
            else thisYear
        }
    ),
    CountdownTemplate(
        title = "Ostern",
        icon = "🐣",
        color = "#66BB6A",
        displayFormat = CountdownDisplayFormat.DAYS_ONLY,
        category = TemplateCategory.FEIERTAGE,
        targetDateProvider = { nextEaster() }
    ),

    // ── Jahreszeiten ──────────────────────────────────────────────────────────
    CountdownTemplate(
        title = "Sommeranfang",
        icon = "☀️",
        color = "#FFA726",
        displayFormat = CountdownDisplayFormat.MONTHS_DAYS,
        category = TemplateCategory.JAHRESZEITEN,
        targetDateProvider = {
            val now = LocalDate.now()
            val thisYear = LocalDate.of(now.year, Month.JUNE, 21)
            if (now > thisYear) LocalDate.of(now.year + 1, Month.JUNE, 21)
            else thisYear
        }
    ),
    CountdownTemplate(
        title = "Winteranfang",
        icon = "❄️",
        color = "#42A5F5",
        displayFormat = CountdownDisplayFormat.MONTHS_DAYS,
        category = TemplateCategory.JAHRESZEITEN,
        targetDateProvider = {
            val now = LocalDate.now()
            val thisYear = LocalDate.of(now.year, Month.DECEMBER, 21)
            if (now > thisYear) LocalDate.of(now.year + 1, Month.DECEMBER, 21)
            else thisYear
        }
    ),
    CountdownTemplate(
        title = "Frühlingsanfang",
        icon = "🌸",
        color = "#EC407A",
        displayFormat = CountdownDisplayFormat.MONTHS_DAYS,
        category = TemplateCategory.JAHRESZEITEN,
        targetDateProvider = {
            val now = LocalDate.now()
            val thisYear = LocalDate.of(now.year, Month.MARCH, 20)
            if (now > thisYear) LocalDate.of(now.year + 1, Month.MARCH, 20)
            else thisYear
        }
    ),
    CountdownTemplate(
        title = "Herbstanfang",
        icon = "🍂",
        color = "#8D6E63",
        displayFormat = CountdownDisplayFormat.MONTHS_DAYS,
        category = TemplateCategory.JAHRESZEITEN,
        targetDateProvider = {
            val now = LocalDate.now()
            val thisYear = LocalDate.of(now.year, Month.SEPTEMBER, 22)
            if (now > thisYear) LocalDate.of(now.year + 1, Month.SEPTEMBER, 22)
            else thisYear
        }
    ),

    // ── Sport & Events ────────────────────────────────────────────────────────
    CountdownTemplate(
        title = "Nächste WM 2026",
        icon = "⚽",
        color = "#26A69A",
        displayFormat = CountdownDisplayFormat.MONTHS_DAYS,
        category = TemplateCategory.SPORT,
        targetDateProvider = { LocalDate.of(2026, Month.JUNE, 11) }
    ),
    CountdownTemplate(
        title = "Olympia 2028",
        icon = "🏅",
        color = "#5C6BC0",
        displayFormat = CountdownDisplayFormat.YEARS_MONTHS_DAYS,
        category = TemplateCategory.SPORT,
        targetDateProvider = { LocalDate.of(2028, Month.JULY, 14) }
    ),

    // ── Persönlich ────────────────────────────────────────────────────────────
    CountdownTemplate(
        title = "Mein Geburtstag",
        icon = "🎂",
        color = "#AB47BC",
        displayFormat = CountdownDisplayFormat.DAYS_ONLY,
        category = TemplateCategory.PERSOENLICH,
        targetDateProvider = {
            // Platzhalter — Nutzer wählt das Datum selbst
            LocalDate.now().plusDays(30)
        }
    ),
    CountdownTemplate(
        title = "Urlaub",
        icon = "✈️",
        color = "#42A5F5",
        displayFormat = CountdownDisplayFormat.WEEKS_DAYS,
        category = TemplateCategory.PERSOENLICH,
        targetDateProvider = { LocalDate.now().plusDays(60) }
    ),
    CountdownTemplate(
        title = "Hochzeit",
        icon = "💍",
        color = "#EC407A",
        displayFormat = CountdownDisplayFormat.MONTHS_DAYS,
        category = TemplateCategory.PERSOENLICH,
        targetDateProvider = { LocalDate.now().plusDays(180) }
    ),

    // ── Schule & Arbeit ───────────────────────────────────────────────────────
    CountdownTemplate(
        title = "Sommerferien",
        icon = "🏖️",
        color = "#FFA726",
        displayFormat = CountdownDisplayFormat.WEEKS_DAYS,
        category = TemplateCategory.SCHULE,
        targetDateProvider = {
            val now = LocalDate.now()
            // Bayern: Ende Juni
            val thisYear = LocalDate.of(now.year, Month.JUNE, 27)
            if (now > thisYear) LocalDate.of(now.year + 1, Month.JUNE, 27)
            else thisYear
        }
    ),
    CountdownTemplate(
        title = "Prüfung",
        icon = "📝",
        color = "#EF5350",
        displayFormat = CountdownDisplayFormat.DAYS_ONLY,
        category = TemplateCategory.SCHULE,
        targetDateProvider = { LocalDate.now().plusDays(14) }
    ),
    CountdownTemplate(
        title = "Projektabgabe",
        icon = "💼",
        color = "#8D6E63",
        displayFormat = CountdownDisplayFormat.DAYS_ONLY,
        category = TemplateCategory.SCHULE,
        targetDateProvider = { LocalDate.now().plusDays(7) }
    )
)

fun CountdownTemplate.toCountdown(): Countdown {
    val date = targetDateProvider()
    return Countdown(
        title = title,
        icon = icon,
        color = color,
        displayFormat = displayFormat.name,
        targetDateTime = LocalDateTime.of(date, LocalDateTime.now().toLocalTime())
    )
}

// Oster-Algorithmus (Anonymus Gregorianisch)
private fun nextEaster(): LocalDate {
    val now = LocalDate.now()
    fun easterForYear(y: Int): LocalDate {
        val a = y % 19
        val b = y / 100
        val c = y % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(y, month, day)
    }
    val thisYear = easterForYear(now.year)
    return if (now > thisYear) easterForYear(now.year + 1) else thisYear
}