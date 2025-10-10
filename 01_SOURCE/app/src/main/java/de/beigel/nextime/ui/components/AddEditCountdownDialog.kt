package de.beigel.nextime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCountdownDialog(
    countdown: Countdown?,
    onDismiss: () -> Unit,
    onSave: (Countdown) -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }

    var title by remember { mutableStateOf(countdown?.title ?: "") }
    var selectedDate by remember { mutableStateOf(countdown?.targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1)) }
    var selectedTime by remember { mutableStateOf(countdown?.targetDateTime?.toLocalTime() ?: LocalTime.of(12, 0)) }
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

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showFormatPicker by remember { mutableStateOf(false) }

    // Vordefinierte Farben
    val colorOptions = listOf(
        "#FF7043", "#EF5350", "#EC407A", "#AB47BC", "#5C6BC0",
        "#42A5F5", "#26A69A", "#66BB6A", "#FFA726", "#8D6E63"
    )

    // Prüfe ob Datum in Vergangenheit oder Zukunft liegt
    val isPast = selectedDate.isBefore(LocalDate.now()) ||
            (selectedDate.isEqual(LocalDate.now()) && includeTime && selectedTime.isBefore(LocalTime.now()))

    val countdownType = if (isPast) "Count-up" else "Countdown"

    // Verfügbare Formate basierend auf includeTime
    val availableFormats = if (includeTime) {
        listOf(
            CountdownDisplayFormat.DAYS_ONLY to "Nur Tage",
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

    // Stelle sicher, dass ausgewähltes Format verfügbar ist
    LaunchedEffect(includeTime) {
        if (!availableFormats.map { it.first }.contains(selectedFormat)) {
            selectedFormat = CountdownDisplayFormat.DAYS_ONLY
        }
    }

    AlertDialog(
        onDismissRequest = {
            haptic.tick()
            onDismiss()
        },
        title = {
            Column {
                Text(if (countdown == null) "Countdown erstellen" else "Countdown bearbeiten")
                // Live-Vorschau des Typs
                Surface(
                    shape = RoundedCornerShape(DesignSystem.CornerRadius.small),
                    color = if (isPast)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(top = DesignSystem.Spacing.xSmall)
                ) {
                    Text(
                        text = "✨ $countdownType",
                        modifier = Modifier.padding(
                            horizontal = DesignSystem.Spacing.small,
                            vertical = DesignSystem.Spacing.xxSmall
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPast)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = DesignSystem.Spacing.xSmall),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
            ) {
                // 1. Titel
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    placeholder = { Text("z.B. Urlaub, Geburtstag...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 2. Datum
                OutlinedButton(
                    onClick = {
                        haptic.tick()
                        showDatePicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📅 ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                }

                // 3. Uhrzeit einbeziehen
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius.medium),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DesignSystem.Spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Uhrzeit einbeziehen")
                        Switch(
                            checked = includeTime,
                            onCheckedChange = {
                                haptic.tick()
                                includeTime = it
                            }
                        )
                    }
                }

                // 4. Uhrzeit (nur wenn aktiviert)
                if (includeTime) {
                    OutlinedButton(
                        onClick = {
                            haptic.tick()
                            showTimePicker = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🕐 ${selectedTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))} Uhr")
                    }
                }

                // 5. Anzeigeformat (ausklappbar)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(DesignSystem.CornerRadius.medium))
                            .clickable {
                                haptic.tick()
                                showFormatPicker = !showFormatPicker
                            },
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(DesignSystem.Spacing.medium),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Anzeigeformat",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = getFormatExample(selectedFormat),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Icon(
                                imageVector = if (showFormatPicker)
                                    Icons.Default.KeyboardArrowUp
                                else
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Format-Optionen (ausklappbar)
                    if (showFormatPicker) {
                        Column(
                            modifier = Modifier.padding(top = DesignSystem.Spacing.xSmall),
                            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xSmall)
                        ) {
                            availableFormats.forEach { (format, label) ->
                                FormatOptionCompact(
                                    label = label,
                                    example = getFormatExample(format),
                                    isSelected = selectedFormat == format,
                                    onClick = {
                                        haptic.tick()
                                        selectedFormat = format
                                        showFormatPicker = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 6. Farbauswahl
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Farbe wählen",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xSmall))

                    // Farb-Grid - Erste Reihe
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xSmall)
                    ) {
                        colorOptions.take(5).forEach { colorHex ->
                            ColorCircle(
                                color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorHex)),
                                isSelected = selectedColor == colorHex,
                                onClick = {
                                    haptic.tick()
                                    selectedColor = colorHex
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(DesignSystem.Spacing.xSmall))

                    // Farb-Grid - Zweite Reihe
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xSmall)
                    ) {
                        colorOptions.drop(5).forEach { colorHex ->
                            ColorCircle(
                                color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorHex)),
                                isSelected = selectedColor == colorHex,
                                onClick = {
                                    haptic.tick()
                                    selectedColor = colorHex
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        haptic.success()

                        val targetDateTime = if (includeTime) {
                            LocalDateTime.of(selectedDate, selectedTime)
                        } else {
                            LocalDateTime.of(selectedDate, LocalTime.of(0, 0))
                        }

                        val newCountdown = countdown?.copy(
                            title = title,
                            targetDateTime = targetDateTime,
                            includeTime = includeTime,
                            showNights = false,
                            displayFormat = selectedFormat.name,
                            color = selectedColor
                        ) ?: Countdown(
                            title = title,
                            targetDateTime = targetDateTime,
                            includeTime = includeTime,
                            showNights = false,
                            displayFormat = selectedFormat.name,
                            color = selectedColor
                        )

                        onSave(newCountdown)
                    } else {
                        haptic.error()
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic.tick()
                onDismiss()
            }) {
                Text("Abbrechen")
            }
        }
    )

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
            }
        )
    }
}

@Composable
private fun FormatOptionCompact(
    label: String,
    example: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.small))
            .clickable(onClick = onClick),
        color = if (isSelected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(DesignSystem.Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
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
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorCircle(
    color: androidx.compose.ui.graphics.Color,
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
                        width = 3.dp,
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
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Hilfsfunktion für Format-Beispiele
private fun getFormatExample(format: CountdownDisplayFormat): String {
    return when (format) {
        CountdownDisplayFormat.DAYS_ONLY -> "42 Tage"
        CountdownDisplayFormat.DAYS_HOURS -> "42 Tage, 5h 30m"
        CountdownDisplayFormat.HOURS_MINUTES -> "1020h 30m"
        CountdownDisplayFormat.FULL_TIME -> "42 Tage, 5h 30m 45s"
        CountdownDisplayFormat.WEEKS_DAYS -> "6 Wochen, 0 Tage"
        CountdownDisplayFormat.MONTHS_DAYS -> "1 Monat, 12 Tage"
    }
}