package de.beigel.nextime.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.beigel.nextime.data.model.Countdown
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
    var title by remember { mutableStateOf(countdown?.title ?: "") }
    var selectedDate by remember { mutableStateOf(countdown?.targetDateTime?.toLocalDate() ?: LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(countdown?.targetDateTime?.toLocalTime() ?: LocalTime.of(12, 0)) }
    var includeTime by remember { mutableStateOf(countdown?.includeTime ?: false) }
    var showNights by remember { mutableStateOf(countdown?.showNights ?: false) }
    var selectedColor by remember { mutableStateOf(countdown?.color ?: "#FF7043") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Vordefinierte Farben
    val colorOptions = listOf(
        "#FF7043" to "Orange",
        "#EF5350" to "Rot",
        "#EC407A" to "Pink",
        "#AB47BC" to "Lila",
        "#5C6BC0" to "Indigo",
        "#42A5F5" to "Blau",
        "#26A69A" to "Türkis",
        "#66BB6A" to "Grün",
        "#FFA726" to "Gold",
        "#8D6E63" to "Braun"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (countdown == null) "Countdown erstellen" else "Countdown bearbeiten")
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Titel
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titel") },
                    placeholder = { Text("z.B. Urlaub, Geburtstag...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Datum
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Datum: ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                }

                // Uhrzeit einbeziehen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Uhrzeit einbeziehen", modifier = Modifier.weight(1f))
                    Switch(
                        checked = includeTime,
                        onCheckedChange = { includeTime = it }
                    )
                }

                // Uhrzeit (nur wenn aktiviert)
                if (includeTime) {
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Uhrzeit: ${selectedTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
                    }
                }

                // Nächte anzeigen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Nächte anzeigen", modifier = Modifier.weight(1f))
                    Switch(
                        checked = showNights,
                        onCheckedChange = { showNights = it }
                    )
                }

                // Farbauswahl
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Farbe wählen",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Farb-Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.take(5).forEach { (colorHex, _) ->
                            ColorCircle(
                                color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorHex)),
                                isSelected = selectedColor == colorHex,
                                onClick = { selectedColor = colorHex },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.drop(5).forEach { (colorHex, _) ->
                            ColorCircle(
                                color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorHex)),
                                isSelected = selectedColor == colorHex,
                                onClick = { selectedColor = colorHex },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val targetDateTime = if (includeTime) {
                            LocalDateTime.of(selectedDate, selectedTime)
                        } else {
                            LocalDateTime.of(selectedDate, LocalTime.of(0, 0))
                        }

                        val newCountdown = countdown?.copy(
                            title = title,
                            targetDateTime = targetDateTime,
                            includeTime = includeTime,
                            showNights = showNights,
                            color = selectedColor
                        ) ?: Countdown(
                            title = title,
                            targetDateTime = targetDateTime,
                            includeTime = includeTime,
                            showNights = showNights,
                            color = selectedColor
                        )

                        onSave(newCountdown)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        selectedDate = LocalDate.ofEpochDay(millis / 86400000)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
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
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
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
private fun ColorCircle(
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Check,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}