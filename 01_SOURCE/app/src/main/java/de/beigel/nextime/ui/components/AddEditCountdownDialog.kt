package de.beigel.nextime.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.unit.sp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.ReminderOption
import de.beigel.nextime.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val QUICK_EMOJIS = listOf(
    "⏰", "🎂", "✈️", "🎄", "🎃", "🎵", "🏖️", "💍",
    "🎓", "🏆", "🎉", "❤️", "🚀", "🌍", "🏠", "💼"
)

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
    var icon by remember { mutableStateOf(countdown?.icon ?: "⏰") }
    var selectedDate by remember {
        mutableStateOf(countdown?.targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1))
    }
    var selectedTime by remember {
        mutableStateOf(countdown?.targetDateTime?.toLocalTime() ?: LocalTime.of(12, 0))
    }
    var includeTime by remember { mutableStateOf(countdown?.includeTime ?: false) }
    var selectedFormat by remember {
        mutableStateOf(
            try { CountdownDisplayFormat.valueOf(countdown?.displayFormat ?: CountdownDisplayFormat.DAYS_ONLY.name) }
            catch (e: Exception) { CountdownDisplayFormat.DAYS_ONLY }
        )
    }
    var notificationEnabled by remember { mutableStateOf(countdown?.notificationEnabled ?: false) }
    val selectedReminders = remember {
        mutableStateListOf<ReminderOption>().apply {
            countdown?.let { cd ->
                if (cd.reminderOptions.isNotEmpty()) {
                    cd.reminderOptions.split(",").forEach { name ->
                        try { add(ReminderOption.valueOf(name.trim())) } catch (e: Exception) { }
                    }
                }
            }
        }
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCustomColorPicker by remember { mutableStateOf(false) }
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
        ReminderOption.AT_TIME, ReminderOption.DAY_1, ReminderOption.DAYS_2,
        ReminderOption.DAYS_3, ReminderOption.WEEK_1, ReminderOption.WEEKS_2,
        ReminderOption.MONTH_1
    )

    val previewCountdown by remember(title, icon, selectedDate, selectedTime, includeTime, selectedFormat, selectedColor) {
        derivedStateOf {
            val targetDateTime = LocalDateTime.of(selectedDate, if (includeTime) selectedTime else LocalTime.MIDNIGHT)
            countdown?.copy(
                title = title.ifBlank { "Vorschau" },
                icon = icon.ifBlank { "⏰" },
                targetDateTime = targetDateTime,
                displayFormat = selectedFormat.name,
                color = selectedColor,
                includeTime = includeTime
            ) ?: Countdown(
                title = title.ifBlank { "Vorschau" },
                icon = icon.ifBlank { "⏰" },
                targetDateTime = targetDateTime,
                displayFormat = selectedFormat.name,
                color = selectedColor,
                includeTime = includeTime
            )
        }
    }

    fun buildCountdown(): Countdown {
        val targetDateTime = LocalDateTime.of(selectedDate, if (includeTime) selectedTime else LocalTime.MIDNIGHT)
        return countdown?.copy(
            title = title,
            icon = icon.ifBlank { "⏰" },
            targetDateTime = targetDateTime,
            displayFormat = selectedFormat.name,
            color = selectedColor,
            notificationEnabled = notificationEnabled,
            reminderOptions = selectedReminders.joinToString(",") { it.name },
            includeTime = includeTime
        ) ?: Countdown(
            title = title,
            icon = icon.ifBlank { "⏰" },
            targetDateTime = targetDateTime,
            displayFormat = selectedFormat.name,
            color = selectedColor,
            notificationEnabled = notificationEnabled,
            reminderOptions = selectedReminders.joinToString(",") { it.name },
            includeTime = includeTime
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
                }
            )
        }
    ) { paddingValues ->
        var swipeOffset by remember { mutableStateOf(0f) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            swipeOffset += dragAmount
                            change.consumePositionChange()
                        },
                        onDragEnd = {
                            if (swipeOffset > 150f) { haptic.tick(); onBack() }
                            swipeOffset = 0f
                        },
                        onDragCancel = { swipeOffset = 0f }
                    )
                }
        ) {
            // ── STICKY PREVIEW ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Vorschau",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                CountdownCard(countdown = previewCountdown)
                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // ── SCROLLBARER INHALT ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Titel
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

                // Icon
                SectionCardCompact(title = "Icon") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = icon.ifBlank { "⏰" }, fontSize = 24.sp)
                            }
                            OutlinedTextField(
                                value = icon,
                                onValueChange = { if (it.length <= 2) icon = it },
                                placeholder = { Text("Emoji eingeben") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                        Text(
                            text = "Schnellauswahl",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(QUICK_EMOJIS.size) { index ->
                                val emoji = QUICK_EMOJIS[index]
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (icon == emoji) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .then(
                                            if (icon == emoji) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                            else Modifier
                                        )
                                        .clickable { haptic.tick(); icon = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }

                // Datum & Uhrzeit
                SectionCardCompact(title = "Datum & Uhrzeit") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Datum-Zeile
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { haptic.tick(); showDatePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                            }
                            TextButton(onClick = { selectedDate = LocalDate.now() }) {
                                Text("Heute")
                            }
                        }

                        // Uhrzeit Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Uhrzeit einbeziehen",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Switch(
                                checked = includeTime,
                                onCheckedChange = { haptic.tick(); includeTime = it }
                            )
                        }

                        // Uhrzeit-Picker — nur sichtbar wenn includeTime aktiv
                        AnimatedVisibility(
                            visible = includeTime,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            OutlinedButton(
                                onClick = { haptic.tick(); showTimePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr")
                            }
                        }
                    }
                }

                // Anzeigeformat
                SectionCardCompact(title = "Anzeigeformat") {
                    var formatExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { formatExpanded = !formatExpanded }
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            availableFormats.first { it.first == selectedFormat }.second,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            if (formatExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                    AnimatedVisibility(visible = formatExpanded, enter = fadeIn(), exit = fadeOut()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            availableFormats.forEach { (format, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { haptic.tick(); selectedFormat = format; formatExpanded = false }
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (selectedFormat == format) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            getFormatExample(format),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (selectedFormat == format) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                // Farbe
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
                        OutlinedButton(
                            onClick = { haptic.tick(); showCustomColorPicker = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        try { Color(android.graphics.Color.parseColor(selectedColor)) }
                                        catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Eigene Farbe")
                        }
                    }
                }

                // Benachrichtigungen
                SectionCardCompact(title = "Benachrichtigungen") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                haptic.tick()
                                                if (selectedReminders.contains(option)) selectedReminders.remove(option)
                                                else selectedReminders.add(option)
                                            }
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(option.displayName, style = MaterialTheme.typography.bodyMedium)
                                        if (selectedReminders.contains(option)) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(onClick = { haptic.tick(); onBack() }, modifier = Modifier.weight(1f)) {
                    Text("Abbrechen")
                }
                Button(
                    onClick = {
                        if (title.isNotBlank()) { haptic.success(); onSave(buildCountdown()) }
                        else haptic.error()
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("SPEICHERN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Custom Color Picker
    if (showCustomColorPicker) {
        val initialColorInt = try { android.graphics.Color.parseColor(selectedColor) }
        catch (e: Exception) { android.graphics.Color.parseColor("#FF7043") }
        var r by remember { mutableStateOf(android.graphics.Color.red(initialColorInt)) }
        var g by remember { mutableStateOf(android.graphics.Color.green(initialColorInt)) }
        var b by remember { mutableStateOf(android.graphics.Color.blue(initialColorInt)) }

        AlertDialog(
            onDismissRequest = { haptic.tick(); showCustomColorPicker = false },
            title = { Text("Eigene Farbe") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(android.graphics.Color.rgb(r, g, b))))
                    Text("R: $r", style = MaterialTheme.typography.bodySmall)
                    Slider(value = r.toFloat(), onValueChange = { r = it.toInt() }, valueRange = 0f..255f)
                    Text("G: $g", style = MaterialTheme.typography.bodySmall)
                    Slider(value = g.toFloat(), onValueChange = { g = it.toInt() }, valueRange = 0f..255f)
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

    // Date Picker
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 86400000
        )
        DatePickerDialog(
            onDismissRequest = { haptic.tick(); showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    haptic.click()
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / 86400000)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { haptic.tick(); showDatePicker = false }) { Text("Abbrechen") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { haptic.tick(); showTimePicker = false },
            title = { Text("Uhrzeit wählen") },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    haptic.click()
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { haptic.tick(); showTimePicker = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun SectionCardCompact(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable
private fun ColorCircleCompact(color: Color, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

private fun getFormatExample(format: CountdownDisplayFormat): String = when (format) {
    CountdownDisplayFormat.DAYS_ONLY         -> "42 Tage"
    CountdownDisplayFormat.WEEKS_DAYS        -> "6 Wochen, 0 Tage"
    CountdownDisplayFormat.MONTHS_DAYS       -> "1 Monat, 12 Tage"
    CountdownDisplayFormat.YEARS_MONTHS_DAYS -> "2 Jahre, 1 Monat, 3 Tage"
}