package de.beigel.nextime.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.ReminderOption
import de.beigel.nextime.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCountdownScreen(
    countdown: Countdown?,
    onSave: (Countdown) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf(countdown?.title ?: "") }
    var selectedDate by remember { mutableStateOf(countdown?.targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1)) }
    var selectedFormat by remember {
        mutableStateOf(
            try {
                CountdownDisplayFormat.valueOf(countdown?.displayFormat ?: CountdownDisplayFormat.DAYS_ONLY.name)
            } catch (e: Exception) {
                CountdownDisplayFormat.DAYS_ONLY
            }
        )
    }

    var notificationEnabled by remember { mutableStateOf(countdown?.notificationEnabled ?: false) }
    val selectedReminders = remember {
        mutableStateListOf<ReminderOption>().apply {
            countdown?.let { cd ->
                val options = cd.reminderOptions
                if (options.isNotEmpty()) {
                    options.split(",").forEach { name ->
                        try {
                            val option = ReminderOption.valueOf(name.trim())
                            add(option)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomColorPicker by remember { mutableStateOf(false) }
    var customColorInput by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(countdown?.color ?: "#FF7043") }

    val colorOptions = listOf(
        "#FF7043", "#EF5350", "#EC407A", "#AB47BC", "#5C6BC0",
        "#42A5F5", "#26A69A", "#66BB6A", "#FFA726", "#8D6E63"
    )

    val availableFormats = listOf(
        CountdownDisplayFormat.DAYS_ONLY to "Nur Tage",
        CountdownDisplayFormat.WEEKS_DAYS to "Wochen + Tage",
        CountdownDisplayFormat.MONTHS_DAYS to "Monate + Tage",
        CountdownDisplayFormat.YEARS_MONTHS_DAYS to "Jahre + Monate + Tage"
    )

    val availableReminders = listOf(
        ReminderOption.AT_TIME,
        ReminderOption.DAY_1,
        ReminderOption.DAYS_2,
        ReminderOption.DAYS_3,
        ReminderOption.WEEK_1,
        ReminderOption.WEEKS_2,
        ReminderOption.MONTH_1
    )

    fun buildCountdown(): Countdown {
        val targetDateTime = LocalDateTime.of(selectedDate, LocalDateTime.now().toLocalTime())
        return countdown?.copy(
            title = title,
            targetDateTime = targetDateTime,
            displayFormat = selectedFormat.name,
            color = selectedColor,
            notificationEnabled = notificationEnabled,
            reminderOptions = selectedReminders.joinToString(",") { it.name }
        ) ?: Countdown(
            title = title,
            targetDateTime = targetDateTime,
            displayFormat = selectedFormat.name,
            color = selectedColor,
            notificationEnabled = notificationEnabled,
            reminderOptions = selectedReminders.joinToString(",") { it.name }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (countdown == null) "Countdown erstellen" else "Countdown bearbeiten") },
                navigationIcon = {
                    IconButton(onClick = { haptic.tick(); onBack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Schließen")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (title.isNotBlank()) {
                            haptic.success(); onSave(buildCountdown())
                        } else haptic.error()
                    }) {
                        Text("SPEICHERN", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->

        // compact layout: weniger Padding, dichtere Elemente
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    // Swipe nach rechts zum Zurück — einfach gehalten
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consumePositionChange()
                        if (dragAmount > 200f) { haptic.tick(); onBack() }
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionCardCompact(title = "Titel") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = { Text("z.B. Urlaub, Geburtstag...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                SectionCardCompact(title = "Datum") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { haptic.tick(); showDatePicker = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                        }
                        TextButton(onClick = { selectedDate = LocalDate.now() }) { Text("Heute") }
                    }
                }

                // ===== Anzeigeformat (ausklappbar) =====
                SectionCardCompact(title = "Anzeigeformat") {
                    var formatExpanded by remember { mutableStateOf(false) }

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clickable { formatExpanded = !formatExpanded }
                        .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(availableFormats.first { it.first == selectedFormat }.second, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Icon(if (formatExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                    }

                    AnimatedVisibility(visible = formatExpanded, enter = fadeIn(), exit = fadeOut()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            availableFormats.forEach { (format, label) ->
                                Row(modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { haptic.tick(); selectedFormat = format; formatExpanded = false }
                                    .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selectedFormat == format) FontWeight.Bold else FontWeight.Normal)
                                        Text(getFormatExample(format), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (selectedFormat == format) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                SectionCardCompact(title = "Farbe") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            colorOptions.forEach { colorHex ->
                                ColorCircleCompact(
                                    color = Color(android.graphics.Color.parseColor(colorHex)),
                                    isSelected = selectedColor == colorHex,
                                    onClick = { haptic.tick(); selectedColor = colorHex },
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { haptic.tick(); showCustomColorPicker = true }, modifier = Modifier.weight(1f)) {
                                Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(try { Color(android.graphics.Color.parseColor(selectedColor)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }))
                                Spacer(Modifier.width(8.dp))
                                Text("Eigene Farbe")
                            }
                        }
                    }
                }

                SectionCardCompact(title = "Benachrichtigungen") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Aktivieren", style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(checked = notificationEnabled, onCheckedChange = { haptic.tick(); notificationEnabled = it })
                        }

                        if (notificationEnabled) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                availableReminders.forEach { option ->
                                    Row(modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { haptic.tick(); if (selectedReminders.contains(option)) selectedReminders.remove(option) else selectedReminders.add(option) }
                                        .padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(option.displayName, style = MaterialTheme.typography.bodyMedium)
                                        if (selectedReminders.contains(option)) Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // compact footer: Save button floating-ish
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), horizontalArrangement = Arrangement.Center) {
                Button(onClick = { if (title.isNotBlank()) { haptic.success(); onSave(buildCountdown()) } else haptic.error() }, enabled = title.isNotBlank(), modifier = Modifier.fillMaxWidth(0.6f)) {
                    Text("SPEICHERN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Custom Color Picker Dialog (RGB Sliders)
    if (showCustomColorPicker) {
        // initial RGB from selectedColor
        val initialColorInt = try { android.graphics.Color.parseColor(selectedColor) } catch (e: Exception) { android.graphics.Color.parseColor("#FF7043") }
        var r by remember { mutableStateOf(android.graphics.Color.red(initialColorInt)) }
        var g by remember { mutableStateOf(android.graphics.Color.green(initialColorInt)) }
        var b by remember { mutableStateOf(android.graphics.Color.blue(initialColorInt)) }

        AlertDialog(
            onDismissRequest = { haptic.tick(); showCustomColorPicker = false },
            title = { Text("Eigene Farbe") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Wähle eine Farbe über RGB-Slider")

                    // Vorschau
                    Box(modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.rgb(r, g, b))))

                    // R
                    Text("R: $r", style = MaterialTheme.typography.bodySmall)
                    Slider(value = r.toFloat(), onValueChange = { r = it.toInt() }, valueRange = 0f..255f)

                    // G
                    Text("G: $g", style = MaterialTheme.typography.bodySmall)
                    Slider(value = g.toFloat(), onValueChange = { g = it.toInt() }, valueRange = 0f..255f)

                    // B
                    Text("B: $b", style = MaterialTheme.typography.bodySmall)
                    Slider(value = b.toFloat(), onValueChange = { b = it.toInt() }, valueRange = 0f..255f)
                }
            },
            confirmButton = {
                Button(onClick = {
                    haptic.click()
                    selectedColor = String.format("#%02X%02X%02X", r, g, b)
                    showCustomColorPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { haptic.tick(); showCustomColorPicker = false }) { Text("Abbrechen") } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toEpochDay() * 86400000)

        DatePickerDialog(onDismissRequest = { haptic.tick(); showDatePicker = false }, confirmButton = {
            TextButton(onClick = { haptic.click(); datePickerState.selectedDateMillis?.let { millis -> selectedDate = LocalDate.ofEpochDay(millis / 86400000) }; showDatePicker = false }) { Text("OK") }
        }, dismissButton = {
            TextButton(onClick = { haptic.tick(); showDatePicker = false }) { Text("Abbrechen") }
        }) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SectionCardCompact(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun ColorCircleCompact(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier
        .clip(CircleShape)
        .background(color)
        .then(if (isSelected) Modifier.border(width = 3.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape) else Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), shape = CircleShape))
        .clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

private fun getFormatExample(format: CountdownDisplayFormat): String {
    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY -> "42 Tage"
        CountdownDisplayFormat.WEEKS_DAYS -> "6 Wochen, 0 Tage"
        CountdownDisplayFormat.MONTHS_DAYS -> "1 Monat, 12 Tage"
        CountdownDisplayFormat.YEARS_MONTHS_DAYS -> "2 Jahre, 1 Monat, 3 Tage"
    }
}
