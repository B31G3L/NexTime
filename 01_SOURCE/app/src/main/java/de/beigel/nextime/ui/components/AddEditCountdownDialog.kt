package de.beigel.nextime.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.ReminderOption
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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

    val defaultTime by de.beigel.nextime.ui.theme.ThemePreferences.getDefaultTime(context)
        .collectAsState(initial = LocalTime.of(0, 0))

    var title by remember { mutableStateOf(countdown?.title ?: "") }
    var selectedDate by remember { mutableStateOf(countdown?.targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1)) }
    var selectedTime by remember {
        mutableStateOf(
            countdown?.targetDateTime?.toLocalTime() ?: defaultTime
        )
    }
    var includeTime by remember { mutableStateOf(countdown?.includeTime ?: false) }
    var selectedColor by remember { mutableStateOf(countdown?.color ?: "#FF7043") }
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
                            // Ignoriere ungültige Optionen
                        }
                    }
                }
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showCustomColorPicker by remember { mutableStateOf(false) }
    var customColorInput by remember { mutableStateOf("") }

    val colorOptions = listOf(
        "#FF7043", "#EF5350", "#EC407A", "#AB47BC", "#5C6BC0",
        "#42A5F5", "#26A69A", "#66BB6A", "#FFA726", "#8D6E63"
    )

    val availableFormats = if (includeTime) {
        listOf(
            CountdownDisplayFormat.DAYS_ONLY to "Nur Tage",
            CountdownDisplayFormat.FULL_DETAILED to "Detailliert",
            CountdownDisplayFormat.DAYS_HOURS to "Tage + Stunden",
            CountdownDisplayFormat.HOURS_MINUTES to "Nur Stunden",
            CountdownDisplayFormat.FULL_TIME to "Vollständig",
            CountdownDisplayFormat.WEEKS_DAYS to "Wochen + Tage",
            CountdownDisplayFormat.MONTHS_DAYS to "Monate + Tage"
        )
    } else {
        listOf(
            CountdownDisplayFormat.DAYS_ONLY to "Nur Tage",
            CountdownDisplayFormat.WEEKS_DAYS to "Wochen + Tage",
            CountdownDisplayFormat.MONTHS_DAYS to "Monate + Tage"
        )
    }

    val availableReminders = remember(includeTime) {
        if (includeTime) {
            listOf(
                ReminderOption.AT_TIME,
                ReminderOption.MINUTES_5,
                ReminderOption.MINUTES_15,
                ReminderOption.MINUTES_30,
                ReminderOption.HOUR_1,
                ReminderOption.HOURS_3,
                ReminderOption.HOURS_6,
                ReminderOption.HOURS_12,
                ReminderOption.DAY_1,
                ReminderOption.DAYS_2,
                ReminderOption.DAYS_3,
                ReminderOption.WEEK_1,
                ReminderOption.WEEKS_2,
                ReminderOption.MONTH_1
            )
        } else {
            listOf(
                ReminderOption.AT_TIME,
                ReminderOption.DAY_1,
                ReminderOption.DAYS_2,
                ReminderOption.DAYS_3,
                ReminderOption.WEEK_1,
                ReminderOption.WEEKS_2,
                ReminderOption.MONTH_1
            )
        }
    }

    LaunchedEffect(includeTime) {
        if (!availableFormats.map { it.first }.contains(selectedFormat)) {
            selectedFormat = CountdownDisplayFormat.DAYS_ONLY
        }
        selectedReminders.removeAll { !availableReminders.contains(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (countdown == null) "Countdown erstellen" else "Countdown bearbeiten") },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.tick()
                        onBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Schließen")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                haptic.success()

                                val targetDateTime = if (includeTime) {
                                    LocalDateTime.of(selectedDate, selectedTime)
                                } else {
                                    LocalDateTime.of(selectedDate, defaultTime)
                                }

                                val newCountdown = countdown?.copy(
                                    title = title,
                                    targetDateTime = targetDateTime,
                                    includeTime = includeTime,
                                    showNights = false,
                                    displayFormat = selectedFormat.name,
                                    color = selectedColor,
                                    notificationEnabled = notificationEnabled,
                                    reminderOptions = selectedReminders.joinToString(",") { it.name }
                                ) ?: Countdown(
                                    title = title,
                                    targetDateTime = targetDateTime,
                                    includeTime = includeTime,
                                    showNights = false,
                                    displayFormat = selectedFormat.name,
                                    color = selectedColor,
                                    notificationEnabled = notificationEnabled,
                                    reminderOptions = selectedReminders.joinToString(",") { it.name }
                                )

                                onSave(newCountdown)
                            } else {
                                haptic.error()
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text(
                            "SPEICHERN",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. TITEL SECTION
            SectionCard(title = "Titel") {
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

            // 2. DATUM & ZEIT SECTION
            SectionCard(title = "Datum & Zeit") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Datum Button
                    OutlinedButton(
                        onClick = {
                            haptic.tick()
                            showDatePicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                    }

                    // Uhrzeit Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Uhrzeit einbeziehen",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Switch(
                            checked = includeTime,
                            onCheckedChange = {
                                haptic.tick()
                                includeTime = it
                            }
                        )
                    }

                    // Zeit Button (nur wenn includeTime)
                    if (includeTime) {
                        OutlinedButton(
                            onClick = {
                                haptic.tick()
                                showTimePicker = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))} Uhr")
                        }
                    }
                }
            }

            // 3. ANZEIGEFORMAT SECTION
            SectionCard(title = "Anzeigeformat") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    availableFormats.forEach { (format, label) ->
                        FormatOption(
                            label = label,
                            example = getFormatExample(format),
                            isSelected = selectedFormat == format,
                            onClick = {
                                haptic.tick()
                                selectedFormat = format
                            }
                        )
                    }
                }
            }

            // 4. FARBE SECTION
            SectionCard(title = "Farbe") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Vordefinierte Farben
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.take(5).forEach { colorHex ->
                            ColorCircle(
                                color = Color(android.graphics.Color.parseColor(colorHex)),
                                isSelected = selectedColor == colorHex,
                                onClick = {
                                    haptic.tick()
                                    selectedColor = colorHex
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.drop(5).forEach { colorHex ->
                            ColorCircle(
                                color = Color(android.graphics.Color.parseColor(colorHex)),
                                isSelected = selectedColor == colorHex,
                                onClick = {
                                    haptic.tick()
                                    selectedColor = colorHex
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Divider()

                    // Custom Color Button
                    OutlinedButton(
                        onClick = {
                            haptic.tick()
                            showCustomColorPicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    try {
                                        Color(android.graphics.Color.parseColor(selectedColor))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Eigene Farbe wählen")
                    }
                }
            }

            // 5. BENACHRICHTIGUNGEN SECTION
            SectionCard(title = "Benachrichtigungen") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Enable Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Aktivieren",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = {
                                haptic.tick()
                                notificationEnabled = it
                            }
                        )
                    }

                    // Erinnerungen (nur wenn aktiviert)
                    if (notificationEnabled) {
                        Divider()

                        Text(
                            "Erinnerungen",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            availableReminders.forEach { option ->
                                ReminderOption(
                                    option = option,
                                    isSelected = selectedReminders.contains(option),
                                    onClick = {
                                        haptic.tick()
                                        if (selectedReminders.contains(option)) {
                                            selectedReminders.remove(option)
                                        } else {
                                            selectedReminders.add(option)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Spacer
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Custom Color Picker Dialog
    if (showCustomColorPicker) {
        AlertDialog(
            onDismissRequest = {
                haptic.tick()
                showCustomColorPicker = false
            },
            title = { Text("Eigene Farbe") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Gib einen Hex-Farbcode ein (z.B. #FF5733)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = customColorInput,
                        onValueChange = {
                            customColorInput = it
                        },
                        label = { Text("Hex-Code") },
                        placeholder = { Text("#FF5733") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Vorschau
                    if (customColorInput.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Vorschau:", style = MaterialTheme.typography.bodySmall)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        try {
                                            Color(android.graphics.Color.parseColor(customColorInput))
                                        } catch (e: Exception) {
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.click()
                        try {
                            android.graphics.Color.parseColor(customColorInput)
                            selectedColor = customColorInput
                            customColorInput = ""
                            showCustomColorPicker = false
                        } catch (e: Exception) {
                            haptic.error()
                        }
                    },
                    enabled = customColorInput.isNotEmpty()
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.tick()
                    customColorInput = ""
                    showCustomColorPicker = false
                }) {
                    Text("Abbrechen")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // DatePicker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 86400000
        )

        DatePickerDialog(
            onDismissRequest = {
                haptic.tick()
                showDatePicker = false
            },
            confirmButton = {
                TextButton(onClick = {
                    haptic.click()
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / 86400000)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.tick()
                    showDatePicker = false
                }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // TimePicker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = {
                haptic.tick()
                showTimePicker = false
            },
            confirmButton = {
                TextButton(onClick = {
                    haptic.click()
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptic.tick()
                    showTimePicker = false
                }) {
                    Text("Abbrechen")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun FormatOption(
    label: String,
    example: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Text(
                    text = example,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Ausgewählt",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ReminderOption(
    option: ReminderOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected)
            MaterialTheme.colorScheme.tertiaryContainer
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Ausgewählt",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorCircle(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun getFormatExample(format: CountdownDisplayFormat): String {
    return when (format) {
        CountdownDisplayFormat.FULL_DETAILED -> "1J 2M 15T 05:30:45"
        CountdownDisplayFormat.DAYS_ONLY -> "42 Tage"
        CountdownDisplayFormat.DAYS_HOURS -> "42 Tage, 5h 30m"
        CountdownDisplayFormat.HOURS_MINUTES -> "1020h 30m"
        CountdownDisplayFormat.FULL_TIME -> "42 Tage, 5h 30m 45s"
        CountdownDisplayFormat.WEEKS_DAYS -> "6 Wochen, 0 Tage"
        CountdownDisplayFormat.MONTHS_DAYS -> "1 Monat, 12 Tage"
    }
}