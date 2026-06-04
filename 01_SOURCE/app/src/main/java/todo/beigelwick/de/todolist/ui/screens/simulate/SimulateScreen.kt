package todo.beigelwick.de.todolist.ui.screens.simulate

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.*
import todo.beigelwick.de.todolist.ui.components.CountdownMainDisplay
import todo.beigelwick.de.todolist.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ─── Alle sinnvollen Formate ──────────────────────────────────────────────────

private data class FormatEntry(val label: String, val units: List<DisplayUnit>)

private val ALL_FORMATS = listOf(
    FormatEntry("Tage",                           listOf(DisplayUnit.DAYS)),
    FormatEntry("Wochen",                         listOf(DisplayUnit.WEEKS)),
    FormatEntry("Monate",                         listOf(DisplayUnit.MONTHS)),
    FormatEntry("Jahre",                          listOf(DisplayUnit.YEARS)),
    FormatEntry("Wochen + Tage",                  listOf(DisplayUnit.WEEKS,  DisplayUnit.DAYS)),
    FormatEntry("Monate + Tage",                  listOf(DisplayUnit.MONTHS, DisplayUnit.DAYS)),
    FormatEntry("Monate + Wochen",                listOf(DisplayUnit.MONTHS, DisplayUnit.WEEKS)),
    FormatEntry("Jahre + Tage",                   listOf(DisplayUnit.YEARS,  DisplayUnit.DAYS)),
    FormatEntry("Jahre + Wochen",                 listOf(DisplayUnit.YEARS,  DisplayUnit.WEEKS)),
    FormatEntry("Jahre + Monate",                 listOf(DisplayUnit.YEARS,  DisplayUnit.MONTHS)),
    FormatEntry("Monate + Wochen + Tage",         listOf(DisplayUnit.MONTHS, DisplayUnit.WEEKS, DisplayUnit.DAYS)),
    FormatEntry("Jahre + Monate + Tage",          listOf(DisplayUnit.YEARS,  DisplayUnit.MONTHS, DisplayUnit.DAYS)),
    FormatEntry("Jahre + Wochen + Tage",          listOf(DisplayUnit.YEARS,  DisplayUnit.WEEKS,  DisplayUnit.DAYS)),
    FormatEntry("Jahre + Monate + Wochen + Tage", listOf(DisplayUnit.YEARS,  DisplayUnit.MONTHS, DisplayUnit.WEEKS, DisplayUnit.DAYS)),
    FormatEntry("Stunden",                        listOf(DisplayUnit.HOURS)),
    FormatEntry("Tage + Stunden",                 listOf(DisplayUnit.DAYS,   DisplayUnit.HOURS)),
    FormatEntry("Tage + Std + Min",               listOf(DisplayUnit.DAYS,   DisplayUnit.HOURS, DisplayUnit.MINUTES)),
    FormatEntry("Tage + Std + Min + Sek",         listOf(DisplayUnit.DAYS,   DisplayUnit.HOURS, DisplayUnit.MINUTES, DisplayUnit.SECONDS)),
    FormatEntry("Std + Min + Sek",                listOf(DisplayUnit.HOURS,  DisplayUnit.MINUTES, DisplayUnit.SECONDS)),
)

// ─── SimulateScreen ───────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulateScreen(onBack: () -> Unit) {
    val context     = LocalContext.current
    val haptic      = remember { HapticFeedback(context) }
    val scrollState = rememberScrollState()

    var selectedDate   by remember { mutableStateOf(LocalDate.now().plusDays(65)) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Sekunden-Tick für Zeitformate
    var tick by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1_000L)
            tick++
        }
    }

    val today    = LocalDate.now()
    val isPast   = selectedDate.isBefore(today)
    val totalDays = ChronoUnit.DAYS.between(
        if (isPast) selectedDate else today,
        if (isPast) today else selectedDate
    )

    val dummyCountdown = remember(selectedDate) {
        Countdown(
            id             = 0L,
            title          = "Simulation",
            targetDateTime = LocalDateTime.of(selectedDate, LocalTime.MIDNIGHT),
            displayFormat  = "",
            color          = "#FF7043"
        )
    }
    // tick als Key damit Zeitformate live aktualisieren
    val timeInfo = remember(selectedDate, tick) { dummyCountdown.calculateTimeRemaining() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anzeigeformat-Simulation") },
                navigationIcon = {
                    IconButton(onClick = { haptic.tick(); onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // ── Datumswähler ──────────────────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Zieldatum", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedButton(
                        onClick  = { haptic.tick(); showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy")))
                    }

                    // Status-Badge
                    val badgeColor = if (isPast) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    val badgeTextColor = if (isPast) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.primary

                    Surface(shape = RoundedCornerShape(8.dp), color = badgeColor, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text       = if (isPast) "⬆️  Count-up (vergangen)" else "⬇️  Countdown (zukünftig)",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = badgeTextColor
                            )
                            Text("$totalDays Tage", style = MaterialTheme.typography.bodyMedium,
                                color = badgeTextColor, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quick-Buttons
                    Text("Schnellauswahl", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf(
                            "−2 J"  to today.minusYears(2),
                            "−6 M"  to today.minusMonths(6),
                            "−30 T" to today.minusDays(30),
                            "Heute" to today,
                            "+30 T" to today.plusDays(30),
                            "+6 M"  to today.plusMonths(6),
                            "+2 J"  to today.plusYears(2),
                        ).forEach { (label, date) ->
                            val isSelected = selectedDate == date
                            val bg by animateColorAsState(
                                targetValue   = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                animationSpec = tween(150), label = label
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bg)
                                    .clickable { haptic.tick(); selectedDate = date },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text       = label,
                                    fontSize   = 10.sp,
                                    color      = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier   = Modifier.padding(vertical = 7.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Alle Formate ──────────────────────────────────────────────────
            Text(
                text       = "Alle Formate im Vergleich",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            ALL_FORMATS.forEach { entry ->
                FormatRow(entry = entry, timeInfo = timeInfo)
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── Date Picker ───────────────────────────────────────────────────────────
    if (showDatePicker) {
        val zoneOffset      = java.time.ZoneId.systemDefault().rules
            .getOffset(java.time.Instant.now()).totalSeconds * 1000L
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 86400000L + zoneOffset
        )
        DatePickerDialog(
            onDismissRequest = { haptic.tick(); showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    haptic.click()
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.of("UTC")).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { haptic.tick(); showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) { DatePicker(state = datePickerState) }
    }
}

// ─── FormatRow ────────────────────────────────────────────────────────────────

@Composable
private fun FormatRow(entry: FormatEntry, timeInfo: CountdownInfo) {
    val hasTime = entry.units.any { it in TIME_UNITS }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(10.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text     = entry.label,
                style    = MaterialTheme.typography.bodySmall,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(140.dp)
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                CountdownMainDisplay(
                    timeInfo   = timeInfo,
                    units      = entry.units,
                    numberSize = if (hasTime) 17.sp else 20.sp,
                    unitSize   = if (hasTime) 11.sp else 12.sp
                )
            }
        }
    }
}