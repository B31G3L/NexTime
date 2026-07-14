package com.beigel.nextime.ui.screens.addedit

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beigel.nextime.R
import com.beigel.nextime.data.model.Countdown
import com.beigel.nextime.data.model.DISPLAY_UNIT_ORDER
import com.beigel.nextime.data.model.DisplayFormat
import com.beigel.nextime.data.model.DisplayUnit
import com.beigel.nextime.data.model.RecurrenceType
import com.beigel.nextime.data.model.ReminderOption
import com.beigel.nextime.ui.components.ALL_NEXTIME_ICONS
import com.beigel.nextime.ui.components.CountdownCard
import com.beigel.nextime.ui.components.DEFAULT_ICON_NAME
import com.beigel.nextime.ui.components.IconCategory
import com.beigel.nextime.ui.components.iconByName
import com.beigel.nextime.ui.theme.AppPreferences
import com.beigel.nextime.ui.viewmodel.CountdownViewModel
import com.beigel.nextime.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

private val REMINDER_GROUPS = listOf(
    R.string.reminder_group_attime to listOf(_root_ide_package_.com.beigel.nextime.data.model.ReminderOption.AT_TIME),
    R.string.reminder_group_hours  to listOf(
        _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.MINUTES_30, _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.HOUR_1, _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.HOURS_3,
        _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.HOURS_6, _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.HOURS_12
    ),
    R.string.reminder_group_days   to listOf(
        _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.DAY_1, _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.DAYS_2, _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.DAYS_3,
        _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.WEEK_1, _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.WEEKS_2, _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.MONTH_1
    )
)

// ─── Permission-Hilfsfunktionen ───────────────────────────────────────────────

private fun hasNotificationPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

private fun hasExactAlarmPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return alarmManager.canScheduleExactAlarms()
}

private fun showExactAlarmDialog(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.alarm_dialog_title))
        .setMessage(context.getString(R.string.alarm_dialog_msg))
        .setPositiveButton(context.getString(R.string.alarm_to_settings)) { _, _ ->
            try {
                context.startActivity(
                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data  = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            } catch (e: Exception) {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data  = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                )
            }
        }
        .setNegativeButton(context.getString(R.string.alarm_later)) { dialog, _ -> dialog.dismiss() }
        .show()
}

// ─── AddEditScreen ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditScreen(
    countdownId : Long,
    onBack      : () -> Unit,
    viewModel   : com.beigel.nextime.ui.viewmodel.CountdownViewModel = viewModel()
) {
    val context      = LocalContext.current
    val haptic       = remember { _root_ide_package_.com.beigel.nextime.utils.HapticFeedback(context) }
    val focusManager = LocalFocusManager.current
    val scrollState  = rememberScrollState()

    var existingCountdown by remember { mutableStateOf<com.beigel.nextime.data.model.Countdown?>(null) }
    val isEdit = countdownId != -1L

    LaunchedEffect(countdownId) {
        if (isEdit) existingCountdown = viewModel.getCountdownById(countdownId)
    }

    val defaultColor    by _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.getDefaultColor(context).collectAsState(initial = "#FF7043")
    val defaultTime     by _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.getDefaultTime(context).collectAsState(initial = LocalTime.of(12, 0))
    val globalDateUnits by _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.getDefaultDateUnits(context).collectAsState(initial = setOf(
        _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.DAYS))
    val globalShowTime  by _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.getShowTimeOnCard(context).collectAsState(initial = false)

    // ── Formular-State ────────────────────────────────────────────────────────
    var title               by remember { mutableStateOf("") }
    var icon                by remember { mutableStateOf(_root_ide_package_.com.beigel.nextime.ui.components.DEFAULT_ICON_NAME) }
    var selectedDate        by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime        by remember { mutableStateOf(LocalTime.of(12, 0)) }
    var showTime            by remember { mutableStateOf(false) }
    var selectedRecurrence  by remember { mutableStateOf(_root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE) }
    var selectedColor       by remember { mutableStateOf("#FF7043") }
    var notificationEnabled by remember { mutableStateOf(false) }
    val selectedReminders   = remember { mutableStateListOf<com.beigel.nextime.data.model.ReminderOption>() }
    val initialized         = remember { mutableStateOf(false) }

    // ── "In X Tagen"-State ────────────────────────────────────────────────────
    var useDaysInput     by remember { mutableStateOf(false) }
    var daysInputText    by remember { mutableStateOf("") }
    var daysInputCountUp by remember { mutableStateOf(false) }

    // ── Custom-Format-State ───────────────────────────────────────────────────
    var useCustomFormat   by remember { mutableStateOf(false) }
    val customFormatUnits = remember { mutableStateListOf<com.beigel.nextime.data.model.DisplayUnit>() }

    // ── Inline-Picker-States ──────────────────────────────────────────────────
    var showInlineDatePicker by remember { mutableStateOf(false) }
    var showInlineTimePicker by remember { mutableStateOf(false) }

    // ── Notification Permission Launcher ──────────────────────────────────────
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationEnabled = true
            if (!hasExactAlarmPermission(context)) {
                showExactAlarmDialog(context)
            }
        } else {
            notificationEnabled = false
        }
    }

    LaunchedEffect(existingCountdown, defaultColor, defaultTime) {
        if (initialized.value) return@LaunchedEffect
        if (isEdit && existingCountdown == null) return@LaunchedEffect
        val cd = existingCountdown
        title               = cd?.title ?: ""
        icon                = cd?.icon?.ifEmpty { _root_ide_package_.com.beigel.nextime.ui.components.DEFAULT_ICON_NAME } ?: _root_ide_package_.com.beigel.nextime.ui.components.DEFAULT_ICON_NAME
        selectedDate        = cd?.targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1)
        selectedTime        = cd?.targetDateTime?.toLocalTime() ?: defaultTime
        showTime            = cd?.targetDateTime?.toLocalTime()?.let { it != LocalTime.MIDNIGHT } ?: false
        selectedRecurrence  = cd?.recurrenceType ?: _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE
        selectedColor       = cd?.color ?: defaultColor
        notificationEnabled = cd?.notificationEnabled ?: false
        if (cd != null && cd.reminderOptions.isNotEmpty()) {
            selectedReminders.clear()
            cd.reminderOptions.split(",").forEach { name ->
                try { selectedReminders.add(_root_ide_package_.com.beigel.nextime.data.model.ReminderOption.valueOf(name.trim())) } catch (e: Exception) { }
            }
        }
        if (cd != null && cd.displayFormat.isNotBlank()) {
            useCustomFormat = true
            customFormatUnits.clear()
            customFormatUnits.addAll(_root_ide_package_.com.beigel.nextime.data.model.DisplayFormat.decodeOrdered(cd.displayFormat))
        }
        initialized.value = true
    }

    LaunchedEffect(useCustomFormat) {
        if (useCustomFormat && customFormatUnits.isEmpty()) {
            val sorted = _root_ide_package_.com.beigel.nextime.data.model.DISPLAY_UNIT_ORDER.filter { it in globalDateUnits }
            customFormatUnits.addAll(sorted)
            if (globalShowTime) {
                listOf(_root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.HOURS, _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.MINUTES, _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.SECONDS).forEach {
                    if (!customFormatUnits.contains(it)) customFormatUnits.add(it)
                }
            }
        }
    }

    val colorOptions = listOf(
        "#FF7043","#EF5350","#EC407A","#AB47BC","#5C6BC0",
        "#42A5F5","#26A69A","#66BB6A","#FFA726","#8D6E63"
    )

    val isCountUp = remember(selectedDate) { selectedDate.isBefore(LocalDate.now()) }
    LaunchedEffect(isCountUp) {
        if (isCountUp && selectedRecurrence != _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE) selectedRecurrence = _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE
    }

    val zoneOffset = java.time.ZoneId.systemDefault().rules
        .getOffset(java.time.Instant.now()).totalSeconds * 1000L
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 86400000L + zoneOffset
    )
    val timePickerState = rememberTimePickerState(
        initialHour   = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour      = true
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val picked = java.time.Instant.ofEpochMilli(millis)
                .atZone(java.time.ZoneId.of("UTC")).toLocalDate()
            if (picked != selectedDate) selectedDate = picked
        }
    }

    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        val picked = LocalTime.of(timePickerState.hour, timePickerState.minute)
        if (picked != selectedTime) selectedTime = picked
    }

    val previewDisplayFormat = if (useCustomFormat && customFormatUnits.isNotEmpty())
        _root_ide_package_.com.beigel.nextime.data.model.DisplayFormat.encodeOrdered(customFormatUnits.toList())
    else ""

    val previewCountdown = _root_ide_package_.com.beigel.nextime.data.model.Countdown(
        id = existingCountdown?.id ?: 0L,
        title = title.ifBlank { stringResource(R.string.preview_placeholder) },
        icon = icon.ifEmpty { _root_ide_package_.com.beigel.nextime.ui.components.DEFAULT_ICON_NAME },
        targetDateTime = LocalDateTime.of(
            selectedDate,
            if (showTime) selectedTime else LocalTime.MIDNIGHT
        ),
        displayFormat = previewDisplayFormat,
        color = selectedColor,
        recurrence = selectedRecurrence.name
    )

    fun buildCountdown(): com.beigel.nextime.data.model.Countdown {
        val target = LocalDateTime.of(selectedDate, if (showTime) selectedTime else LocalTime.MIDNIGHT)
        return _root_ide_package_.com.beigel.nextime.data.model.Countdown(
            id = existingCountdown?.id ?: 0L,
            title = title,
            icon = icon.ifEmpty { _root_ide_package_.com.beigel.nextime.ui.components.DEFAULT_ICON_NAME },
            targetDateTime = target,
            displayFormat = if (useCustomFormat && customFormatUnits.isNotEmpty())
                _root_ide_package_.com.beigel.nextime.data.model.DisplayFormat.encodeOrdered(
                    customFormatUnits.toList()
                )
            else "",
            color = selectedColor,
            notificationEnabled = notificationEnabled,
            reminderOptions = selectedReminders.joinToString(",") { it.name },
            recurrence = selectedRecurrence.name
        )
    }

    var showIconSheet         by remember { mutableStateOf(false) }
    var showCustomColorPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) stringResource(R.string.topbar_edit)
                        else        stringResource(R.string.topbar_create),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { haptic.tick(); onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                        onHorizontalDrag = { change, dragAmount -> swipeOffset += dragAmount; change.consume() },
                        onDragEnd        = { if (swipeOffset > 150f) { haptic.tick(); onBack() }; swipeOffset = 0f },
                        onDragCancel     = { swipeOffset = 0f }
                    )
                }
        ) {

            // ── Vorschau ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text     = stringResource(R.string.preview_label),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                key(
                    previewCountdown.targetDateTime, previewCountdown.color,
                    previewCountdown.title, previewCountdown.icon, previewDisplayFormat
                ) {
                    _root_ide_package_.com.beigel.nextime.ui.components.CountdownCard(countdown = previewCountdown)
                }
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }

            // ── Formular ──────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {

                // ── 1. Icon + Titel ───────────────────────────────────────────
                FormSection {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .clickable { haptic.tick(); showIconSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(_root_ide_package_.com.beigel.nextime.ui.components.iconByName(icon), stringResource(R.string.section_icon), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                        }
                        OutlinedTextField(
                            value         = title,
                            onValueChange = { title = it },
                            placeholder   = { Text(stringResource(R.string.title_hint)) },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                FormDivider()

                // ── 2. Datum & Uhrzeit ────────────────────────────────────────
                FormSection(label = stringResource(R.string.section_datetime)) {

                    // Toggle: Datepicker ↔ Tage-Eingabe
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !useDaysInput,
                            onClick  = {
                                haptic.tick()
                                useDaysInput = false
                                daysInputText = ""
                                // Datum im DatePicker auf selectedDate synchronisieren
                                // (datePickerState aktualisiert sich über LaunchedEffect)
                            },
                            label    = { Text(stringResource(R.string.section_datetime)) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = useDaysInput,
                            onClick  = {
                                haptic.tick()
                                useDaysInput         = true
                                showInlineDatePicker = false
                                showInlineTimePicker = false
                                // Aktuelles Datum → Tage umrechnen und Richtung ermitteln
                                val diff = ChronoUnit.DAYS.between(LocalDate.now(), selectedDate)
                                daysInputCountUp = diff < 0
                                daysInputText    = abs(diff).toString()
                            },
                            label    = { Text("In X Tagen") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (useDaysInput) {
                        // ── Tage-Eingabe ──────────────────────────────────────
                        OutlinedTextField(
                            value         = daysInputText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() }) {
                                    daysInputText = input
                                    input.toLongOrNull()?.let { days ->
                                        if (days in 0L..36500L) {
                                            selectedDate = if (daysInputCountUp)
                                                LocalDate.now().minusDays(days)
                                            else
                                                LocalDate.now().plusDays(days)
                                        }
                                    }
                                }
                            },
                            placeholder     = { Text("z.B. 30") },
                            label           = { Text(if (daysInputCountUp) "Tage vor heute" else "Tage ab heute") },
                            suffix          = { Text("Tage") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction    = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine      = true,
                            modifier        = Modifier.fillMaxWidth(),
                            colors          = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor   = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        // Count-up-Toggle
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text  = if (daysInputCountUp) "Count-up (Vergangenheit)" else "Countdown (Zukunft)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text  = if (daysInputCountUp) "Datum liegt vor heute" else "Datum liegt nach heute",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            Switch(
                                checked         = daysInputCountUp,
                                onCheckedChange = { checked ->
                                    haptic.tick()
                                    daysInputCountUp = checked
                                    daysInputText.toLongOrNull()?.let { days ->
                                        if (days in 0L..36500L) {
                                            selectedDate = if (checked)
                                                LocalDate.now().minusDays(days)
                                            else
                                                LocalDate.now().plusDays(days)
                                        }
                                    }
                                }
                            )
                        }

                        // Berechnetes Datum anzeigen
                        AnimatedVisibility(
                            visible = daysInputText.isNotBlank(),
                            enter   = fadeIn() + expandVertically(),
                            exit    = fadeOut() + shrinkVertically()
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier              = Modifier.padding(top = 2.dp)
                            ) {
                                Icon(
                                    imageVector        = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint               = MaterialTheme.colorScheme.primary,
                                    modifier           = Modifier.size(14.dp)
                                )
                                Text(
                                    text       = selectedDate.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy")),
                                    style      = MaterialTheme.typography.bodySmall,
                                    color      = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // ── Klassischer Datepicker ────────────────────────────
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick  = { haptic.tick(); showInlineDatePicker = !showInlineDatePicker; if (showInlineDatePicker) showInlineTimePicker = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(if (showInlineDatePicker) Icons.Default.KeyboardArrowUp else Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                            }
                            TextButton(onClick = { selectedDate = LocalDate.now(); showInlineDatePicker = false }) {
                                Text(stringResource(R.string.today_button))
                            }
                        }
                        AnimatedVisibility(visible = showInlineDatePicker, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                            DatePicker(state = datePickerState, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    // ── Uhrzeit-Toggle (gilt für beide Modi) ──────────────────
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.time_toggle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Switch(checked = showTime, onCheckedChange = { haptic.tick(); showTime = it; if (!it) showInlineTimePicker = false })
                    }
                    AnimatedVisibility(visible = showTime, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            OutlinedButton(
                                onClick  = { haptic.tick(); showInlineTimePicker = !showInlineTimePicker; if (showInlineTimePicker) showInlineDatePicker = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(if (showInlineTimePicker) Icons.Default.KeyboardArrowUp else Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(stringResource(R.string.time_oclock, selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))))
                            }
                            AnimatedVisibility(visible = showInlineTimePicker, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                                    TimePicker(state = timePickerState)
                                }
                            }
                        }
                    }
                }

                FormDivider()

                // ── 3. Anzeigeformat ──────────────────────────────────────────
                FormSection(label = stringResource(R.string.settings_format_label)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text  = stringResource(R.string.format_custom_toggle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text  = if (useCustomFormat) stringResource(R.string.format_overrides_global)
                                else stringResource(R.string.format_uses_global),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (useCustomFormat) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked         = useCustomFormat,
                            onCheckedChange = { haptic.tick(); useCustomFormat = it; if (!it) customFormatUnits.clear() }
                        )
                    }
                    AnimatedVisibility(visible = useCustomFormat, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement   = Arrangement.spacedBy(8.dp),
                            modifier              = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            _root_ide_package_.com.beigel.nextime.data.model.DISPLAY_UNIT_ORDER.forEach { unit ->
                                val isSelected = customFormatUnits.contains(unit)
                                FilterChip(
                                    selected = isSelected,
                                    onClick  = {
                                        haptic.tick()
                                        if (isSelected) {
                                            if (customFormatUnits.size > 1) customFormatUnits.remove(unit)
                                        } else {
                                            customFormatUnits.add(unit)
                                            val sorted = _root_ide_package_.com.beigel.nextime.data.model.DISPLAY_UNIT_ORDER.filter { customFormatUnits.contains(it) }
                                            customFormatUnits.clear()
                                            customFormatUnits.addAll(sorted)
                                        }
                                    },
                                    label = { Text(unitLabel(unit), style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                FormDivider()

                // ── 4. Farbe ──────────────────────────────────────────────────
                FormSection(label = stringResource(R.string.section_color)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        colorOptions.forEach { colorHex ->
                            val parsed = try { Color(android.graphics.Color.parseColor(colorHex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                            Box(
                                modifier = Modifier.size(28.dp).clip(CircleShape).background(parsed)
                                    .then(if (selectedColor == colorHex) Modifier.border(2.5.dp, MaterialTheme.colorScheme.onBackground, CircleShape) else Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
                                    .clickable { haptic.tick(); selectedColor = colorHex }
                            ) {
                                if (selectedColor == colorHex) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape)
                                .background(try { Color(android.graphics.Color.parseColor(selectedColor)) } catch (e: Exception) { MaterialTheme.colorScheme.primary })
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                                .clickable { haptic.tick(); showCustomColorPicker = true },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                    }
                }

                FormDivider()

                // ── 5. Benachrichtigungen ─────────────────────────────────────
                FormSection(label = stringResource(R.string.section_notifications)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.notification_enable),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(
                            checked         = notificationEnabled,
                            onCheckedChange = { enabled ->
                                haptic.tick()
                                if (enabled) {
                                    when {
                                        hasNotificationPermission(context) -> {
                                            notificationEnabled = true
                                            if (!hasExactAlarmPermission(context)) {
                                                showExactAlarmDialog(context)
                                            }
                                        }
                                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                                            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                        else -> {
                                            notificationEnabled = true
                                        }
                                    }
                                } else {
                                    notificationEnabled = false
                                }
                            }
                        )
                    }
                    AnimatedVisibility(visible = notificationEnabled, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            REMINDER_GROUPS.forEach { (groupLabelRes, options) ->
                                Text(stringResource(groupLabelRes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 10.dp, bottom = 2.dp))
                                options.forEach { option ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                            .clickable { haptic.tick(); if (selectedReminders.contains(option)) selectedReminders.remove(option) else selectedReminders.add(option) }
                                            .padding(horizontal = 4.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        Text(reminderLabel(option), style = MaterialTheme.typography.bodyMedium)
                                        if (selectedReminders.contains(option)) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                FormDivider()

                // ── 6. Wiederholung ───────────────────────────────────────────
                FormSection(label = stringResource(R.string.section_recurrence)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.recurrence_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (selectedRecurrence != _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE) {
                                Text(recurrenceLabel(selectedRecurrence), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Switch(
                            checked         = selectedRecurrence != _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE,
                            onCheckedChange = { enabled -> haptic.tick(); selectedRecurrence = if (enabled) _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.YEARLY else _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE },
                            enabled         = !isCountUp
                        )
                    }
                    AnimatedVisibility(visible = isCountUp, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                            Text(stringResource(R.string.recurrence_countup_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    AnimatedVisibility(visible = selectedRecurrence != _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE && !isCountUp, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            listOf(
                                _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.DAILY   to stringResource(R.string.recurrence_daily),
                                _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.WEEKLY  to stringResource(R.string.recurrence_weekly),
                                _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.MONTHLY to stringResource(R.string.recurrence_monthly),
                                _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.YEARLY  to stringResource(R.string.recurrence_yearly),
                            ).forEach { (type, label) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { haptic.tick(); selectedRecurrence = type }.padding(horizontal = 4.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (selectedRecurrence == type) FontWeight.SemiBold else FontWeight.Normal, color = if (selectedRecurrence == type) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                    if (selectedRecurrence == type) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ── Speichern / Abbrechen ─────────────────────────────────────────
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = { haptic.tick(); onBack() }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.cancel)) }
                Button(
                    onClick  = {
                        if (title.isNotBlank()) { haptic.success(); val cd = buildCountdown(); if (isEdit) viewModel.updateCountdown(cd) else viewModel.addCountdown(cd); onBack() }
                        else haptic.error()
                    },
                    enabled  = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.save_button), fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    // ── Icon-BottomSheet ──────────────────────────────────────────────────────
    if (showIconSheet) {
        val sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var selectedCategory by remember { mutableStateOf<com.beigel.nextime.ui.components.IconCategory?>(null) }
        val visibleIcons     = remember(selectedCategory) { if (selectedCategory == null) _root_ide_package_.com.beigel.nextime.ui.components.ALL_NEXTIME_ICONS else _root_ide_package_.com.beigel.nextime.ui.components.ALL_NEXTIME_ICONS.filter { it.category == selectedCategory } }
        ModalBottomSheet(onDismissRequest = { haptic.tick(); showIconSheet = false }, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.section_icon), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    item { FilterChip(selected = selectedCategory == null, onClick = { haptic.tick(); selectedCategory = null }, label = { Text(stringResource(R.string.template_cat_all)) }) }
                    items(_root_ide_package_.com.beigel.nextime.ui.components.IconCategory.values().toList()) { category ->
                        FilterChip(selected = selectedCategory == category, onClick = { haptic.tick(); selectedCategory = category }, label = {
                            Text(when (category) {
                                _root_ide_package_.com.beigel.nextime.ui.components.IconCategory.TIME      -> stringResource(R.string.icon_cat_time)
                                _root_ide_package_.com.beigel.nextime.ui.components.IconCategory.TRAVEL    -> stringResource(R.string.icon_cat_travel)
                                _root_ide_package_.com.beigel.nextime.ui.components.IconCategory.CELEBRATE -> stringResource(R.string.icon_cat_celebrate)
                                _root_ide_package_.com.beigel.nextime.ui.components.IconCategory.WORK      -> stringResource(R.string.icon_cat_work)
                                _root_ide_package_.com.beigel.nextime.ui.components.IconCategory.SPORT     -> stringResource(R.string.icon_cat_sport)
                                _root_ide_package_.com.beigel.nextime.ui.components.IconCategory.NATURE    -> stringResource(R.string.icon_cat_nature)
                                _root_ide_package_.com.beigel.nextime.ui.components.IconCategory.HOME      -> stringResource(R.string.icon_cat_home)
                                _root_ide_package_.com.beigel.nextime.ui.components.IconCategory.OTHER     -> stringResource(R.string.icon_cat_other)
                            })
                        })
                    }
                }
                LazyVerticalGrid(columns = GridCells.Fixed(6), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp)) {
                    items(visibleIcons) { nexIcon ->
                        val isSelected = icon == nexIcon.name
                        Box(
                            modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant)
                                .then(if (isSelected) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)) else Modifier)
                                .clickable { haptic.tick(); icon = nexIcon.name; showIconSheet = false },
                            contentAlignment = Alignment.Center
                        ) { Icon(nexIcon.vector, nexIcon.name, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)) }
                    }
                }
            }
        }
    }

    // ── Custom Color Picker ───────────────────────────────────────────────────
    if (showCustomColorPicker) {
        val init = try { android.graphics.Color.parseColor(selectedColor) } catch (e: Exception) { android.graphics.Color.parseColor("#FF7043") }
        var r by remember { mutableStateOf(android.graphics.Color.red(init)) }
        var g by remember { mutableStateOf(android.graphics.Color.green(init)) }
        var b by remember { mutableStateOf(android.graphics.Color.blue(init)) }
        AlertDialog(
            onDismissRequest = { haptic.tick(); showCustomColorPicker = false },
            title = { Text(stringResource(R.string.custom_color_label)) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(android.graphics.Color.rgb(r, g, b))))
                    Slider(value = r.toFloat(), onValueChange = { r = it.toInt() }, valueRange = 0f..255f)
                    Slider(value = g.toFloat(), onValueChange = { g = it.toInt() }, valueRange = 0f..255f)
                    Slider(value = b.toFloat(), onValueChange = { b = it.toInt() }, valueRange = 0f..255f)
                }
            },
            confirmButton = { Button(onClick = { haptic.click(); selectedColor = String.format("#%02X%02X%02X", r, g, b); showCustomColorPicker = false }) { Text(stringResource(R.string.ok)) } },
            dismissButton = { TextButton(onClick = { haptic.tick(); showCustomColorPicker = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

// ─── Hilfsfunktionen ──────────────────────────────────────────────────────────

@Composable
private fun unitLabel(unit: com.beigel.nextime.data.model.DisplayUnit): String = when (unit) {
    _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.YEARS   -> stringResource(R.string.format_unit_years)
    _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.MONTHS  -> stringResource(R.string.format_unit_months)
    _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.WEEKS   -> stringResource(R.string.format_unit_weeks)
    _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.DAYS    -> stringResource(R.string.format_unit_days)
    _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.HOURS   -> stringResource(R.string.format_unit_hours)
    _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.MINUTES -> stringResource(R.string.format_unit_minutes)
    _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.SECONDS -> stringResource(R.string.format_unit_seconds)
}

@Composable
private fun reminderLabel(option: com.beigel.nextime.data.model.ReminderOption): String = when (option) {
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.AT_TIME    -> stringResource(R.string.reminder_at_time)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.MINUTES_30 -> stringResource(R.string.reminder_30_minutes)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.HOUR_1     -> stringResource(R.string.reminder_1_hour)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.HOURS_3    -> stringResource(R.string.reminder_3_hours)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.HOURS_6    -> stringResource(R.string.reminder_6_hours)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.HOURS_12   -> stringResource(R.string.reminder_12_hours)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.DAY_1      -> stringResource(R.string.reminder_1_day)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.DAYS_2     -> stringResource(R.string.reminder_2_days)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.DAYS_3     -> stringResource(R.string.reminder_3_days)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.WEEK_1     -> stringResource(R.string.reminder_1_week)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.WEEKS_2    -> stringResource(R.string.reminder_2_weeks)
    _root_ide_package_.com.beigel.nextime.data.model.ReminderOption.MONTH_1    -> stringResource(R.string.reminder_1_month)
    else                      -> stringResource(R.string.reminder_none)
}

@Composable
private fun recurrenceLabel(type: com.beigel.nextime.data.model.RecurrenceType): String = when (type) {
    _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.DAILY   -> stringResource(R.string.recurrence_daily)
    _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.WEEKLY  -> stringResource(R.string.recurrence_weekly)
    _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.MONTHLY -> stringResource(R.string.recurrence_monthly)
    _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.YEARLY  -> stringResource(R.string.recurrence_yearly)
    _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE    -> ""
}

@Composable
private fun FormSection(label: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (label != null) Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun FormDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
}