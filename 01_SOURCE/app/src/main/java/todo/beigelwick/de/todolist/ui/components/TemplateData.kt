package todo.beigelwick.de.todolist.ui.components

import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.CountdownDisplayFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

// ─── Template Datenklassen ────────────────────────────────────────────────────

data class CountdownTemplate(
    val titleKey          : String,
    val icon              : String,
    val color             : String,
    val displayFormat     : CountdownDisplayFormat,
    val category          : TemplateCategory,
    val targetDateProvider : () -> LocalDate
)

enum class TemplateCategory {
    FEIERTAGE,
    JAHRESZEITEN,
    SPORT,
    PERSOENLICH,
    SCHULE
}

// ─── Alle Templates ───────────────────────────────────────────────────────────

val ALL_TEMPLATES: List<CountdownTemplate> = listOf(

    // ── Feiertage ─────────────────────────────────────────────────────────────
    CountdownTemplate("Weihnachten", "🎄", "#EF5350", CountdownDisplayFormat.DAYS_ONLY, TemplateCategory.FEIERTAGE) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.DECEMBER, 24)
        if (now > t) LocalDate.of(now.year + 1, Month.DECEMBER, 24) else t
    },
    CountdownTemplate("Silvester", "🎆", "#5C6BC0", CountdownDisplayFormat.DAYS_ONLY, TemplateCategory.FEIERTAGE) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.DECEMBER, 31)
        if (now > t) LocalDate.of(now.year + 1, Month.DECEMBER, 31) else t
    },
    CountdownTemplate("Neujahr", "🥂", "#FFA726", CountdownDisplayFormat.DAYS_ONLY, TemplateCategory.FEIERTAGE) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.JANUARY, 1)
        if (now >= t) LocalDate.of(now.year + 1, Month.JANUARY, 1) else t
    },
    CountdownTemplate("Valentinstag", "💝", "#EC407A", CountdownDisplayFormat.DAYS_ONLY, TemplateCategory.FEIERTAGE) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.FEBRUARY, 14)
        if (now > t) LocalDate.of(now.year + 1, Month.FEBRUARY, 14) else t
    },
    CountdownTemplate("Halloween", "🎃", "#FF7043", CountdownDisplayFormat.DAYS_ONLY, TemplateCategory.FEIERTAGE) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.OCTOBER, 31)
        if (now > t) LocalDate.of(now.year + 1, Month.OCTOBER, 31) else t
    },
    CountdownTemplate("Ostern", "🐣", "#66BB6A", CountdownDisplayFormat.DAYS_ONLY, TemplateCategory.FEIERTAGE) {
        nextEaster()
    },

    // ── Jahreszeiten ──────────────────────────────────────────────────────────
    CountdownTemplate("Sommeranfang", "☀️", "#FFA726", CountdownDisplayFormat.MONTHS_DAYS, TemplateCategory.JAHRESZEITEN) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.JUNE, 21)
        if (now > t) LocalDate.of(now.year + 1, Month.JUNE, 21) else t
    },
    CountdownTemplate("Winteranfang", "❄️", "#42A5F5", CountdownDisplayFormat.MONTHS_DAYS, TemplateCategory.JAHRESZEITEN) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.DECEMBER, 21)
        if (now > t) LocalDate.of(now.year + 1, Month.DECEMBER, 21) else t
    },
    CountdownTemplate("Frühlingsanfang", "🌸", "#EC407A", CountdownDisplayFormat.MONTHS_DAYS, TemplateCategory.JAHRESZEITEN) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.MARCH, 20)
        if (now > t) LocalDate.of(now.year + 1, Month.MARCH, 20) else t
    },
    CountdownTemplate("Herbstanfang", "🍂", "#8D6E63", CountdownDisplayFormat.MONTHS_DAYS, TemplateCategory.JAHRESZEITEN) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.SEPTEMBER, 22)
        if (now > t) LocalDate.of(now.year + 1, Month.SEPTEMBER, 22) else t
    },

    // ── Sport & Events ────────────────────────────────────────────────────────
    CountdownTemplate("Nächste WM 2026", "⚽", "#26A69A", CountdownDisplayFormat.MONTHS_DAYS, TemplateCategory.SPORT) {
        LocalDate.of(2026, Month.JUNE, 11)
    },
    CountdownTemplate("Olympia 2028", "🏅", "#5C6BC0", CountdownDisplayFormat.YEARS_MONTHS_DAYS, TemplateCategory.SPORT) {
        LocalDate.of(2028, Month.JULY, 14)
    },

    // ── Persönlich ────────────────────────────────────────────────────────────
    CountdownTemplate("Mein Geburtstag", "🎂", "#AB47BC", CountdownDisplayFormat.DAYS_ONLY, TemplateCategory.PERSOENLICH) {
        LocalDate.now().plusDays(30)
    },
    CountdownTemplate("Urlaub", "✈️", "#42A5F5", CountdownDisplayFormat.WEEKS_DAYS, TemplateCategory.PERSOENLICH) {
        LocalDate.now().plusDays(60)
    },
    CountdownTemplate("Hochzeit", "💍", "#EC407A", CountdownDisplayFormat.MONTHS_DAYS, TemplateCategory.PERSOENLICH) {
        LocalDate.now().plusDays(180)
    },

    // ── Schule & Arbeit ───────────────────────────────────────────────────────
    CountdownTemplate("Sommerferien", "🏖️", "#FFA726", CountdownDisplayFormat.WEEKS_DAYS, TemplateCategory.SCHULE) {
        val now = LocalDate.now(); val t = LocalDate.of(now.year, Month.JUNE, 27)
        if (now > t) LocalDate.of(now.year + 1, Month.JUNE, 27) else t
    },
    CountdownTemplate("Prüfung", "📝", "#EF5350", CountdownDisplayFormat.DAYS_ONLY, TemplateCategory.SCHULE) {
        LocalDate.now().plusDays(14)
    },
    CountdownTemplate("Projektabgabe", "💼", "#8D6E63", CountdownDisplayFormat.DAYS_ONLY, TemplateCategory.SCHULE) {
        LocalDate.now().plusDays(7)
    }
)

// ─── Template → Countdown ─────────────────────────────────────────────────────
// BUG FIX: defaultTime als Parameter statt LocalDateTime.now().toLocalTime(),
// damit die in den Einstellungen konfigurierte Standard-Uhrzeit verwendet wird.

fun CountdownTemplate.toCountdown(defaultTime: LocalTime = LocalTime.of(12, 0)): Countdown {
    val date = targetDateProvider()
    return Countdown(
        title          = titleKey,
        icon           = icon,
        color          = color,
        displayFormat  = displayFormat.name,
        targetDateTime = LocalDateTime.of(date, defaultTime)
    )
}

// ─── Oster-Algorithmus ────────────────────────────────────────────────────────

private fun nextEaster(): LocalDate {
    val now = LocalDate.now()
    fun easterForYear(y: Int): LocalDate {
        val a = y % 19; val b = y / 100; val c = y % 100
        val d = b / 4;  val e = b % 4;   val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4;  val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day   = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(y, month, day)
    }
    val thisYear = easterForYear(now.year)
    return if (now > thisYear) easterForYear(now.year + 1) else thisYear
}