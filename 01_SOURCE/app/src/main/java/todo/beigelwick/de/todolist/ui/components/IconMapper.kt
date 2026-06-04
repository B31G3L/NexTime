package todo.beigelwick.de.todolist.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

// ─── Icon-Kategorien ──────────────────────────────────────────────────────────

enum class IconCategory(val labelKey: String) {
    TIME("icon_cat_time"),
    TRAVEL("icon_cat_travel"),
    CELEBRATE("icon_cat_celebrate"),
    WORK("icon_cat_work"),
    SPORT("icon_cat_sport"),
    NATURE("icon_cat_nature"),
    HOME("icon_cat_home"),
    OTHER("icon_cat_other"),
}

data class NexTimeIcon(
    val name     : String,
    val vector   : ImageVector,
    val category : IconCategory,
)

// ─── Vollständige Icon-Liste ──────────────────────────────────────────────────

val ALL_NEXTIME_ICONS: List<NexTimeIcon> = listOf(

    // ── Zeit & Countdown ──────────────────────────────────────────────────────
    NexTimeIcon("Timer",         Icons.Outlined.Timer,         IconCategory.TIME),
    NexTimeIcon("Alarm",         Icons.Outlined.Alarm,         IconCategory.TIME),
    NexTimeIcon("HourglassEmpty",Icons.Outlined.HourglassEmpty,IconCategory.TIME),
    NexTimeIcon("HourglassFull", Icons.Outlined.HourglassFull, IconCategory.TIME),
    NexTimeIcon("Schedule",      Icons.Outlined.Schedule,      IconCategory.TIME),
    NexTimeIcon("WatchLater",    Icons.Outlined.WatchLater,    IconCategory.TIME),
    NexTimeIcon("Timelapse",     Icons.Outlined.Timelapse,     IconCategory.TIME),
    NexTimeIcon("AvTimer",       Icons.Outlined.AvTimer,       IconCategory.TIME),
    NexTimeIcon("Event",         Icons.Outlined.Event,         IconCategory.TIME),
    NexTimeIcon("EventAvailable",Icons.Outlined.EventAvailable,IconCategory.TIME),
    NexTimeIcon("Today",         Icons.Outlined.Today,         IconCategory.TIME),
    NexTimeIcon("CalendarMonth", Icons.Outlined.CalendarMonth, IconCategory.TIME),

    // ── Reisen & Transport ────────────────────────────────────────────────────
    NexTimeIcon("Flight",        Icons.Outlined.Flight,        IconCategory.TRAVEL),
    NexTimeIcon("FlightTakeoff", Icons.Outlined.FlightTakeoff, IconCategory.TRAVEL),
    NexTimeIcon("FlightLand",    Icons.Outlined.FlightLand,    IconCategory.TRAVEL),
    NexTimeIcon("Train",         Icons.Outlined.Train,         IconCategory.TRAVEL),
    NexTimeIcon("DirectionsCar", Icons.Outlined.DirectionsCar, IconCategory.TRAVEL),
    NexTimeIcon("Luggage",       Icons.Outlined.Luggage,       IconCategory.TRAVEL),
    NexTimeIcon("Map",           Icons.Outlined.Map,           IconCategory.TRAVEL),
    NexTimeIcon("Place",         Icons.Outlined.Place,         IconCategory.TRAVEL),
    NexTimeIcon("TravelExplore", Icons.Outlined.TravelExplore, IconCategory.TRAVEL),
    NexTimeIcon("BeachAccess",   Icons.Outlined.BeachAccess,   IconCategory.TRAVEL),
    NexTimeIcon("Sailing",       Icons.Outlined.Sailing,       IconCategory.TRAVEL),

    // ── Feiern & Persönlich ───────────────────────────────────────────────────
    NexTimeIcon("Cake",          Icons.Outlined.Cake,          IconCategory.CELEBRATE),
    NexTimeIcon("Celebration",   Icons.Outlined.Celebration,   IconCategory.CELEBRATE),
    NexTimeIcon("CardGiftcard",  Icons.Outlined.CardGiftcard,  IconCategory.CELEBRATE),
    NexTimeIcon("Favorite",      Icons.Outlined.Favorite,      IconCategory.CELEBRATE),
    NexTimeIcon("Star",          Icons.Outlined.Star,          IconCategory.CELEBRATE),
    NexTimeIcon("EmojiEvents",   Icons.Outlined.EmojiEvents,   IconCategory.CELEBRATE),
    NexTimeIcon("MilitaryTech",  Icons.Outlined.MilitaryTech,  IconCategory.CELEBRATE),
    NexTimeIcon("Redeem",        Icons.Outlined.Redeem,        IconCategory.CELEBRATE),
    NexTimeIcon("LocalBar",      Icons.Outlined.LocalBar,      IconCategory.CELEBRATE),
    NexTimeIcon("Nightlife",     Icons.Outlined.Nightlife,     IconCategory.CELEBRATE),

    // ── Arbeit & Schule ───────────────────────────────────────────────────────
    NexTimeIcon("Work",          Icons.Outlined.Work,          IconCategory.WORK),
    NexTimeIcon("School",        Icons.Outlined.School,        IconCategory.WORK),
    NexTimeIcon("MenuBook",      Icons.Outlined.MenuBook,      IconCategory.WORK),
    NexTimeIcon("Assignment",    Icons.Outlined.Assignment,    IconCategory.WORK),
    NexTimeIcon("Task",          Icons.Outlined.Task,          IconCategory.WORK),
    NexTimeIcon("Checklist",     Icons.Outlined.Checklist,     IconCategory.WORK),
    NexTimeIcon("Laptop",        Icons.Outlined.Laptop,        IconCategory.WORK),
    NexTimeIcon("Savings",       Icons.Outlined.Savings,       IconCategory.WORK),
    NexTimeIcon("RequestQuote",  Icons.Outlined.RequestQuote,  IconCategory.WORK),
    NexTimeIcon("HealthAndSafety",Icons.Outlined.HealthAndSafety,IconCategory.WORK),

    // ── Sport & Fitness ───────────────────────────────────────────────────────
    NexTimeIcon("FitnessCenter", Icons.Outlined.FitnessCenter, IconCategory.SPORT),
    NexTimeIcon("SportsScore",   Icons.Outlined.SportsScore,   IconCategory.SPORT),
    NexTimeIcon("DirectionsRun", Icons.Outlined.DirectionsRun, IconCategory.SPORT),
    NexTimeIcon("Pool",          Icons.Outlined.Pool,          IconCategory.SPORT),
    NexTimeIcon("SelfImprovement",Icons.Outlined.SelfImprovement,IconCategory.SPORT),
    NexTimeIcon("Hiking",        Icons.Outlined.Hiking,        IconCategory.SPORT),
    NexTimeIcon("Skateboarding", Icons.Outlined.Skateboarding, IconCategory.SPORT),
    NexTimeIcon("Sports",        Icons.Outlined.Sports,        IconCategory.SPORT),
    NexTimeIcon("SportsMartialArts",Icons.Outlined.SportsMartialArts,IconCategory.SPORT),

    // ── Natur & Jahreszeiten ──────────────────────────────────────────────────
    NexTimeIcon("WbSunny",       Icons.Outlined.WbSunny,       IconCategory.NATURE),
    NexTimeIcon("AcUnit",        Icons.Outlined.AcUnit,        IconCategory.NATURE),
    NexTimeIcon("Spa",           Icons.Outlined.Spa,           IconCategory.NATURE),
    NexTimeIcon("LocalFlorist",  Icons.Outlined.LocalFlorist,  IconCategory.NATURE),
    NexTimeIcon("Park",          Icons.Outlined.Park,          IconCategory.NATURE),
    NexTimeIcon("Eco",           Icons.Outlined.Eco,           IconCategory.NATURE),
    NexTimeIcon("Thunderstorm",  Icons.Outlined.Thunderstorm,  IconCategory.NATURE),
    NexTimeIcon("Pets",          Icons.Outlined.Pets,          IconCategory.NATURE),

    // ── Zuhause & Familie ─────────────────────────────────────────────────────
    NexTimeIcon("Home",          Icons.Outlined.Home,          IconCategory.HOME),
    NexTimeIcon("Cottage",       Icons.Outlined.Cottage,       IconCategory.HOME),
    NexTimeIcon("People",        Icons.Outlined.People,        IconCategory.HOME),
    NexTimeIcon("Person",        Icons.Outlined.Person,        IconCategory.HOME),
    NexTimeIcon("ChildCare",     Icons.Outlined.ChildCare,     IconCategory.HOME),
    NexTimeIcon("Handshake",     Icons.Outlined.Handshake,     IconCategory.HOME),
    NexTimeIcon("VolunteerActivism",Icons.Outlined.VolunteerActivism,IconCategory.HOME),

    // ── Sonstiges ─────────────────────────────────────────────────────────────
    NexTimeIcon("Rocket",        Icons.Outlined.Rocket,        IconCategory.OTHER),
    NexTimeIcon("MusicNote",     Icons.Outlined.MusicNote,     IconCategory.OTHER),
    NexTimeIcon("Movie",         Icons.Outlined.Movie,         IconCategory.OTHER),
    NexTimeIcon("SportsEsports", Icons.Outlined.SportsEsports, IconCategory.OTHER),
    NexTimeIcon("Restaurant",    Icons.Outlined.Restaurant,    IconCategory.OTHER),
    NexTimeIcon("LocalCafe",     Icons.Outlined.LocalCafe,     IconCategory.OTHER),
    NexTimeIcon("Flag",          Icons.Outlined.Flag,          IconCategory.OTHER),
    NexTimeIcon("Bolt",          Icons.Outlined.Bolt,          IconCategory.OTHER),
    NexTimeIcon("Diamond",       Icons.Outlined.Diamond,       IconCategory.OTHER),
    NexTimeIcon("AutoAwesome",   Icons.Outlined.AutoAwesome,   IconCategory.OTHER),
)

// ─── Icon-Index für schnellen Zugriff ────────────────────────────────────────

private val ICON_MAP: Map<String, ImageVector> by lazy {
    ALL_NEXTIME_ICONS.associate { it.name to it.vector }
}

// ─── Öffentliche Hilfsfunktionen ──────────────────────────────────────────────

/** Liefert das ImageVector zum gespeicherten Icon-Namen. Fallback: Timer. */
fun iconByName(name: String): ImageVector =
    ICON_MAP[name] ?: Icons.Outlined.Timer

/** Standard-Icon für neue Einträge */
val DEFAULT_ICON_NAME = "Timer"

/** Icons nach Kategorie gruppiert (für den Picker) */
val ICONS_BY_CATEGORY: Map<IconCategory, List<NexTimeIcon>> by lazy {
    ALL_NEXTIME_ICONS.groupBy { it.category }
}