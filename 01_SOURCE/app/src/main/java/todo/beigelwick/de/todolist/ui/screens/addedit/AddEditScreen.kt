package todo.beigelwick.de.todolist.ui.screens.addedit

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.DISPLAY_UNIT_ORDER
import todo.beigelwick.de.todolist.data.model.DisplayFormat
import todo.beigelwick.de.todolist.data.model.DisplayUnit
import todo.beigelwick.de.todolist.data.model.RecurrenceType
import todo.beigelwick.de.todolist.data.model.ReminderOption
import todo.beigelwick.de.todolist.ui.components.ALL_NEXTIME_ICONS
import todo.beigelwick.de.todolist.ui.components.CountdownCard
import todo.beigelwick.de.todolist.ui.components.DEFAULT_ICON_NAME
import todo.beigelwick.de.todolist.ui.components.IconCategory
import todo.beigelwick.de.todolist.ui.components.iconByName
import todo.beigelwick.de.todolist.ui.theme.AppPreferences
import todo.beigelwick.de.todolist.ui.viewmodel.CountdownViewModel
import todo.beigelwick.de.todolist.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val REMINDER_GROUPS = listOf(
    R.string.reminder_group_attime to listOf(ReminderOption.AT_TIME),
    R.string.reminder_group_hours  to listOf(
        ReminderOption.MINUTES_30, ReminderOption.HOUR_1, ReminderOption.HOURS_3,
        ReminderOption.HOURS_6, ReminderOption.HOURS_12
    ),
    R.string.reminder_group_days   to listOf(
        ReminderOption.DAY_1, ReminderOption.DAYS_2, ReminderOption.DAYS_3,
        ReminderOption.WEEK_1, ReminderOption.WEEKS_2, ReminderOption.MONTH_1
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditScreen(
    countdownId : Long,
    onBack      : () -> Unit,
    viewModel   : CountdownViewModel = viewModel()
) {
    val context     = LocalContext.current
    val haptic      = remember { HapticFeedback(context) }
    val scrollState = rememberScrollState()

    var existingCountdown by remember { mutableStateOf<Countdown?>(null) }
    val isEdit = countdownId != -1L

    LaunchedEffect(countdownId) {
        if (isEdit) existingCountdown = viewModel.getCountdownById(countdownId)
    }

    val defaultColor    by AppPreferences.getDefaultColor(context).collectAsState(initial = "#FF7043")
    val defaultTime     by AppPreferences.getDefaultTime(context).collectAsState(initial = LocalTime.of(12, 0))
    val globalDateUnits by AppPreferences.getDefaultDateUnits(context).collectAsState(initial = setOf(DisplayUnit.DAYS))
    val globalShowTime  by AppPreferences.getShowTimeOnCard(context).collectAsState(initial = false)

    // ── Formular-State ────────────────────────────────────────────────────────
    var title               by remember { mutableStateOf("") }
    var icon                by remember { mutableStateOf(DEFAULT_ICON_NAME) }
    var selectedDate        by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime        by remember { mutableStateOf(LocalTime.of(12, 0)) }
    var showTime            by remember { mutableStateOf(false) }
    var selectedRecurrence  by remember { mutableStateOf(RecurrenceType.NONE) }
    var selectedColor       by remember { mutableStateOf("#FF7043") }
    var notificationEnabled by remember { mutableStateOf(false) }
    val selectedReminders   = remember { mutableStateListOf<ReminderOption>() }
    val initialized         = remember { mutableStateOf(false) }

    // ── Custom-Format-State ───────────────────────────────────────────────────
    var useCustomFormat   by remember { mutableStateOf(false) }
    val customFormatUnits = remember { mutableStateListOf<DisplayUnit>() }

    // ── Inline-Picker-States ──────────────────────────────────────────────────
    var showInlineDatePicker by remember { mutableStateOf(false) }
    var showInlineTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(existingCountdown, defaultColor, defaultTime) {
        if (initialized.value) return@LaunchedEffect
        if (isEdit && existingCountdown == null) return@LaunchedEffect
        val cd = existingCountdown
        title               = cd?.title ?: ""
        icon                = cd?.icon?.ifEmpty { DEFAULT_ICON_NAME } ?: DEFAULT_ICON_NAME
        selectedDate        = cd?.targetDateTime?.toLocalDate() ?: LocalDate.now().plusDays(1)
        selectedTime        = cd?.targetDateTime?.toLocalTime() ?: defaultTime
        showTime            = cd?.targetDateTime?.toLocalTime()?.let { it != LocalTime.MIDNIGHT } ?: false
        selectedRecurrence  = cd?.recurrenceType ?: RecurrenceType.NONE
        selectedColor       = cd?.color ?: defaultColor
        notificationEnabled = cd?.notificationEnabled ?: false
        if (cd != null && cd.reminderOptions.isNotEmpty()) {
            selectedReminders.clear()
            cd.reminderOptions.split(",").forEach { name ->
                try { selectedReminders.add(ReminderOption.valueOf(name.trim())) } catch (e: Exception) { }
            }
        }
        // Bestehendes Custom-Format laden falls vorhanden
        if (cd != null && cd.displayFormat.isNotBlank()) {
            useCustomFormat = true
            customFormatUnits.clear()
            customFormatUnits.addAll(DisplayFormat.decodeOrdered(cd.displayFormat))
        }
        initialized.value = true
    }

    // Beim ersten Aktivieren des Toggles: globale Einstellung als Startwert
    LaunchedEffect(useCustomFormat) {
        if (useCustomFormat && customFormatUnits.isEmpty()) {
            val sorted = DISPLAY_UNIT_ORDER.filter { it in globalDateUnits }
            customFormatUnits.addAll(sorted)
            if (globalShowTime) {
                listOf(DisplayUnit.HOURS, DisplayUnit.MINUTES, DisplayUnit.SECONDS).forEach {
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
        if (isCountUp && selectedRecurrence != RecurrenceType.NONE) selectedRecurrence = RecurrenceType.NONE
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

    // displayFormat für Vorschau: custom wenn aktiv, sonst leer (→ globale Einstellung greift)
    val previewDisplayFormat = if (useCustomFormat && customFormatUnits.isNotEmpty())
        DisplayFormat.encodeOrdered(customFormatUnits.toList())
    else ""

    val previewCountdown = Countdown(
        id             = existingCountdown?.id ?: 0L,
        title          = title.ifBlank { stringResource(R.string.preview_placeholder) },
        icon           = icon.ifEmpty { DEFAULT_ICON_NAME },
        targetDateTime = LocalDateTime.of(selectedDate, if (showTime) selectedTime else LocalTime.MIDNIGHT),
        displayFormat  = previewDisplayFormat,
        color          = selectedColor,
        recurrence     = selectedRecurrence.name
    )

    fun buildCountdown(): Countdown {
        val target = LocalDateTime.of(selectedDate, if (showTime) selectedTime else LocalTime.MIDNIGHT)
        return Countdown(
            id                  = existingCountdown?.id ?: 0L,
            title               = title,
            icon                = icon.ifEmpty { DEFAULT_ICON_NAME },
            targetDateTime      = target,
            displayFormat       = if (useCustomFormat && customFormatUnits.isNotEmpty())
                DisplayFormat.encodeOrdered(customFormatUnits.toList())
            else "",
            color               = selectedColor,
            notificationEnabled = notificationEnabled,
            reminderOptions     = selectedReminders.joinToString(",") { it.name },
            recurrence          = selectedRecurrence.name
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
                    CountdownCard(countdown = previewCountdown)
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
                            Icon(iconByName(icon), stringResource(R.string.section_icon), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
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

                    // Toggle: Eigenes Format verwenden
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
                                text  = if (useCustomFormat)
                                    stringResource(R.string.format_overrides_global)
                                else
                                    stringResource(R.string.format_uses_global),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (useCustomFormat)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked         = useCustomFormat,
                            onCheckedChange = {
                                haptic.tick()
                                useCustomFormat = it
                                if (!it) customFormatUnits.clear()
                            }
                        )
                    }

                    // Chip-Auswahl — klappt auf wenn Toggle aktiv
                    AnimatedVisibility(
                        visible = useCustomFormat,
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically()
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement   = Arrangement.spacedBy(8.dp),
                            modifier              = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            DISPLAY_UNIT_ORDER.forEach { unit ->
                                val isSelected = customFormatUnits.contains(unit)
                                FilterChip(
                                    selected = isSelected,
                                    onClick  = {
                                        haptic.tick()
                                        if (isSelected) {
                                            // Mind. 1 Einheit muss aktiv bleiben
                                            if (customFormatUnits.size > 1) customFormatUnits.remove(unit)
                                        } else {
                                            customFormatUnits.add(unit)
                                            // Kanonische Reihenfolge wiederherstellen
                                            val sorted = DISPLAY_UNIT_ORDER.filter { customFormatUnits.contains(it) }
                                            customFormatUnits.clear()
                                            customFormatUnits.addAll(sorted)
                                        }
                                    },
                                    label = {
                                        Text(
                                            unitLabel(unit),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
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
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.notification_enable), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Switch(checked = notificationEnabled, onCheckedChange = { haptic.tick(); notificationEnabled = it })
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
                            if (selectedRecurrence != RecurrenceType.NONE) {
                                Text(recurrenceLabel(selectedRecurrence), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Switch(
                            checked         = selectedRecurrence != RecurrenceType.NONE,
                            onCheckedChange = { enabled -> haptic.tick(); selectedRecurrence = if (enabled) RecurrenceType.YEARLY else RecurrenceType.NONE },
                            enabled         = !isCountUp
                        )
                    }
                    AnimatedVisibility(visible = isCountUp, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                            Text(stringResource(R.string.recurrence_countup_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    AnimatedVisibility(visible = selectedRecurrence != RecurrenceType.NONE && !isCountUp, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            listOf(
                                RecurrenceType.DAILY   to stringResource(R.string.recurrence_daily),
                                RecurrenceType.WEEKLY  to stringResource(R.string.recurrence_weekly),
                                RecurrenceType.MONTHLY to stringResource(R.string.recurrence_monthly),
                                RecurrenceType.YEARLY  to stringResource(R.string.recurrence_yearly),
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
        var selectedCategory by remember { mutableStateOf<IconCategory?>(null) }
        val visibleIcons     = remember(selectedCategory) { if (selectedCategory == null) ALL_NEXTIME_ICONS else ALL_NEXTIME_ICONS.filter { it.category == selectedCategory } }
        ModalBottomSheet(onDismissRequest = { haptic.tick(); showIconSheet = false }, sheetState = sheetState, containerColor = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.section_icon), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    item { FilterChip(selected = selectedCategory == null, onClick = { haptic.tick(); selectedCategory = null }, label = { Text(stringResource(R.string.template_cat_all)) }) }
                    items(IconCategory.values().toList()) { category ->
                        FilterChip(selected = selectedCategory == category, onClick = { haptic.tick(); selectedCategory = category }, label = {
                            Text(when (category) {
                                IconCategory.TIME      -> stringResource(R.string.icon_cat_time)
                                IconCategory.TRAVEL    -> stringResource(R.string.icon_cat_travel)
                                IconCategory.CELEBRATE -> stringResource(R.string.icon_cat_celebrate)
                                IconCategory.WORK      -> stringResource(R.string.icon_cat_work)
                                IconCategory.SPORT     -> stringResource(R.string.icon_cat_sport)
                                IconCategory.NATURE    -> stringResource(R.string.icon_cat_nature)
                                IconCategory.HOME      -> stringResource(R.string.icon_cat_home)
                                IconCategory.OTHER     -> stringResource(R.string.icon_cat_other)
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

// ─── Einheitenlabel ───────────────────────────────────────────────────────────

@Composable
private fun unitLabel(unit: DisplayUnit): String = when (unit) {
    DisplayUnit.YEARS   -> stringResource(R.string.format_unit_years)
    DisplayUnit.MONTHS  -> stringResource(R.string.format_unit_months)
    DisplayUnit.WEEKS   -> stringResource(R.string.format_unit_weeks)
    DisplayUnit.DAYS    -> stringResource(R.string.format_unit_days)
    DisplayUnit.HOURS   -> stringResource(R.string.format_unit_hours)
    DisplayUnit.MINUTES -> stringResource(R.string.format_unit_minutes)
    DisplayUnit.SECONDS -> stringResource(R.string.format_unit_seconds)
}

// ─── Hilfsfunktionen ──────────────────────────────────────────────────────────

@Composable
private fun reminderLabel(option: ReminderOption): String = when (option) {
    ReminderOption.AT_TIME    -> stringResource(R.string.reminder_at_time)
    ReminderOption.MINUTES_30 -> stringResource(R.string.reminder_30_minutes)
    ReminderOption.HOUR_1     -> stringResource(R.string.reminder_1_hour)
    ReminderOption.HOURS_3    -> stringResource(R.string.reminder_3_hours)
    ReminderOption.HOURS_6    -> stringResource(R.string.reminder_6_hours)
    ReminderOption.HOURS_12   -> stringResource(R.string.reminder_12_hours)
    ReminderOption.DAY_1      -> stringResource(R.string.reminder_1_day)
    ReminderOption.DAYS_2     -> stringResource(R.string.reminder_2_days)
    ReminderOption.DAYS_3     -> stringResource(R.string.reminder_3_days)
    ReminderOption.WEEK_1     -> stringResource(R.string.reminder_1_week)
    ReminderOption.WEEKS_2    -> stringResource(R.string.reminder_2_weeks)
    ReminderOption.MONTH_1    -> stringResource(R.string.reminder_1_month)
    else                      -> stringResource(R.string.reminder_none)
}

@Composable
private fun recurrenceLabel(type: RecurrenceType): String = when (type) {
    RecurrenceType.DAILY   -> stringResource(R.string.recurrence_daily)
    RecurrenceType.WEEKLY  -> stringResource(R.string.recurrence_weekly)
    RecurrenceType.MONTHLY -> stringResource(R.string.recurrence_monthly)
    RecurrenceType.YEARLY  -> stringResource(R.string.recurrence_yearly)
    RecurrenceType.NONE    -> ""
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