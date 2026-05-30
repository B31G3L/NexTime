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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Repeat
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.RecurrenceType
import todo.beigelwick.de.todolist.data.model.ReminderOption
import todo.beigelwick.de.todolist.ui.components.CountdownCard
import todo.beigelwick.de.todolist.ui.theme.AppPreferences
import todo.beigelwick.de.todolist.ui.viewmodel.CountdownViewModel
import todo.beigelwick.de.todolist.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val QUICK_EMOJIS = listOf(
    "⏰", "🎂", "✈️", "🎄", "🎃", "🎵", "🏖️", "💍",
    "🎓", "🏆", "🎉", "❤️", "🚀", "🌍", "🏠", "💼",
    "🐶", "🎮", "⚽", "🍕", "🎁", "💪", "🌸", "🔥",
    "🎯", "📚", "🏋️", "🎸", "🌈", "🦋", "🍀", "⭐"
)

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

@OptIn(ExperimentalMaterial3Api::class)
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

    val defaultColor by AppPreferences.getDefaultColor(context).collectAsState(initial = "#FF7043")
    val defaultTime  by AppPreferences.getDefaultTime(context).collectAsState(initial = LocalTime.of(12, 0))

    // ── State ─────────────────────────────────────────────────────────────────
    var title               by remember { mutableStateOf("") }
    var icon                by remember { mutableStateOf("⏰") }
    var selectedDate        by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var selectedTime        by remember { mutableStateOf(LocalTime.of(12, 0)) }
    var showTime            by remember { mutableStateOf(false) }
    var selectedRecurrence  by remember { mutableStateOf(RecurrenceType.NONE) }
    var selectedColor       by remember { mutableStateOf("#FF7043") }
    var notificationEnabled by remember { mutableStateOf(false) }
    val selectedReminders   = remember { mutableStateListOf<ReminderOption>() }

    val initialized = remember { mutableStateOf(false) }

    LaunchedEffect(existingCountdown, defaultColor, defaultTime) {
        if (initialized.value) return@LaunchedEffect
        if (isEdit && existingCountdown == null) return@LaunchedEffect

        val cd = existingCountdown
        title               = cd?.title ?: ""
        icon                = cd?.icon ?: "⏰"
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
        initialized.value = true
    }

    val colorOptions = listOf(
        "#FF7043","#EF5350","#EC407A","#AB47BC","#5C6BC0",
        "#42A5F5","#26A69A","#66BB6A","#FFA726","#8D6E63"
    )

    val isCountUp = remember(selectedDate) { selectedDate.isBefore(LocalDate.now()) }

    LaunchedEffect(isCountUp) {
        if (isCountUp && selectedRecurrence != RecurrenceType.NONE) selectedRecurrence = RecurrenceType.NONE
    }

    val previewCountdown = Countdown(
        id             = existingCountdown?.id ?: 0L,
        title          = title.ifBlank { stringResource(R.string.preview_placeholder) },
        icon           = icon.ifBlank { "⏰" },
        targetDateTime = LocalDateTime.of(selectedDate, if (showTime) selectedTime else LocalTime.MIDNIGHT),
        displayFormat  = "",
        color          = selectedColor,
        recurrence     = selectedRecurrence.name
    )

    fun buildCountdown(): Countdown {
        val target = LocalDateTime.of(selectedDate, if (showTime) selectedTime else LocalTime.MIDNIGHT)
        return Countdown(
            id                  = existingCountdown?.id ?: 0L,
            title               = title,
            icon                = icon.ifBlank { "⏰" },
            targetDateTime      = target,
            displayFormat       = "",
            color               = selectedColor,
            notificationEnabled = notificationEnabled,
            reminderOptions     = selectedReminders.joinToString(",") { it.name },
            recurrence          = selectedRecurrence.name
        )
    }

    // ── Dialog-States ─────────────────────────────────────────────────────────
    var showIconSheet         by remember { mutableStateOf(false) }
    var showDatePicker        by remember { mutableStateOf(false) }
    var showTimePicker        by remember { mutableStateOf(false) }
    var showCustomColorPicker by remember { mutableStateOf(false) }

    // ── Scaffold ──────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) stringResource(R.string.topbar_edit)
                        else        stringResource(R.string.topbar_create)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { haptic.tick(); onBack() }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
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
                            change.consume()
                        },
                        onDragEnd    = {
                            if (swipeOffset > 150f) { haptic.tick(); onBack() }
                            swipeOffset = 0f
                        },
                        onDragCancel = { swipeOffset = 0f }
                    )
                }
        ) {

            // ── 1. Vorschau (sticky) ──────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text     = stringResource(R.string.preview_label),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                key(
                    previewCountdown.targetDateTime,
                    previewCountdown.color,
                    previewCountdown.title,
                    previewCountdown.icon
                ) {
                    CountdownCard(countdown = previewCountdown)
                }
                HorizontalDivider(
                    modifier = Modifier.padding(top = 12.dp),
                    color    = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // ── Scrollbarer Inhalt ────────────────────────────────────────────
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── 2. Icon + Name ────────────────────────────────────────────
                SectionCard {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier              = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .border(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { haptic.tick(); showIconSheet = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon.ifBlank { "⏰" }, fontSize = 28.sp)
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

                // ── 3. Datum & Uhrzeit ────────────────────────────────────────
                SectionCard(title = stringResource(R.string.section_datetime)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier              = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick  = { haptic.tick(); showDatePicker = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                            }
                            TextButton(onClick = { selectedDate = LocalDate.now() }) {
                                Text(stringResource(R.string.today_button))
                            }
                        }

                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text  = stringResource(R.string.time_toggle),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Switch(
                                checked         = showTime,
                                onCheckedChange = { haptic.tick(); showTime = it }
                            )
                        }

                        AnimatedVisibility(
                            visible = showTime,
                            enter   = fadeIn() + expandVertically(),
                            exit    = fadeOut() + shrinkVertically()
                        ) {
                            OutlinedButton(
                                onClick  = { haptic.tick(); showTimePicker = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr")
                            }
                        }
                    }
                }

                // ── 4. Farbe ──────────────────────────────────────────────────
                SectionCard(title = stringResource(R.string.section_color)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            colorOptions.forEach { colorHex ->
                                ColorCircle(
                                    color      = Color(android.graphics.Color.parseColor(colorHex)),
                                    isSelected = selectedColor == colorHex,
                                    onClick    = { haptic.tick(); selectedColor = colorHex },
                                    modifier   = Modifier.size(32.dp)
                                )
                            }
                        }
                        OutlinedButton(
                            onClick  = { haptic.tick(); showCustomColorPicker = true },
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
                            Text(stringResource(R.string.custom_color_label))
                        }
                    }
                }

                // ── 5. Benachrichtigung ───────────────────────────────────────
                SectionCard(title = stringResource(R.string.section_notifications)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Notifications,
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text  = stringResource(R.string.notification_enable),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Switch(
                                checked         = notificationEnabled,
                                onCheckedChange = { haptic.tick(); notificationEnabled = it }
                            )
                        }

                        AnimatedVisibility(
                            visible = notificationEnabled,
                            enter   = fadeIn() + expandVertically(),
                            exit    = fadeOut() + shrinkVertically()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                REMINDER_GROUPS.forEach { (groupLabelRes, options) ->
                                    Text(
                                        text     = stringResource(groupLabelRes),
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp, start = 4.dp)
                                    )
                                    options.forEach { option ->
                                        Row(
                                            modifier              = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    haptic.tick()
                                                    if (selectedReminders.contains(option))
                                                        selectedReminders.remove(option)
                                                    else
                                                        selectedReminders.add(option)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment     = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text  = reminderLabel(option),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            if (selectedReminders.contains(option)) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    null,
                                                    modifier = Modifier.size(18.dp),
                                                    tint     = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── 6. Wiederholung ───────────────────────────────────────────
                SectionCard(title = stringResource(R.string.section_recurrence)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier              = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector        = Icons.Outlined.Repeat,
                                    contentDescription = null,
                                    modifier           = Modifier.size(18.dp),
                                    tint               = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column {
                                    Text(
                                        text  = stringResource(R.string.recurrence_hint),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (selectedRecurrence != RecurrenceType.NONE) {
                                        Text(
                                            text  = recurrenceLabel(selectedRecurrence),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                            Switch(
                                checked         = selectedRecurrence != RecurrenceType.NONE,
                                onCheckedChange = { enabled ->
                                    haptic.tick()
                                    selectedRecurrence =
                                        if (enabled) RecurrenceType.YEARLY else RecurrenceType.NONE
                                },
                                enabled = !isCountUp
                            )
                        }

                        AnimatedVisibility(
                            visible = isCountUp,
                            enter   = fadeIn() + expandVertically(),
                            exit    = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                shape    = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier              = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info, null,
                                        modifier = Modifier.size(16.dp),
                                        tint     = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text  = stringResource(R.string.recurrence_countup_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        AnimatedVisibility(
                            visible = selectedRecurrence != RecurrenceType.NONE && !isCountUp,
                            enter   = fadeIn() + expandVertically(),
                            exit    = fadeOut() + shrinkVertically()
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color    = MaterialTheme.colorScheme.outlineVariant
                                )
                                listOf(
                                    RecurrenceType.DAILY   to stringResource(R.string.recurrence_daily),
                                    RecurrenceType.WEEKLY  to stringResource(R.string.recurrence_weekly),
                                    RecurrenceType.MONTHLY to stringResource(R.string.recurrence_monthly),
                                    RecurrenceType.YEARLY  to stringResource(R.string.recurrence_yearly),
                                ).forEach { (type, label) ->
                                    Row(
                                        modifier              = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { haptic.tick(); selectedRecurrence = type }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment     = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text     = when (type) {
                                                    RecurrenceType.DAILY   -> "📅"
                                                    RecurrenceType.WEEKLY  -> "🗓️"
                                                    RecurrenceType.MONTHLY -> "📆"
                                                    RecurrenceType.YEARLY  -> "🎯"
                                                    RecurrenceType.NONE    -> "🚫"
                                                },
                                                fontSize = 18.sp
                                            )
                                            Text(
                                                text       = label,
                                                style      = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (selectedRecurrence == type) FontWeight.Bold else FontWeight.Normal,
                                                color      = if (selectedRecurrence == type)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        if (selectedRecurrence == type) {
                                            Icon(
                                                Icons.Default.Check, null,
                                                modifier = Modifier.size(18.dp),
                                                tint     = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    shape    = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier              = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment     = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info, null,
                                            modifier = Modifier.size(16.dp),
                                            tint     = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text  = stringResource(R.string.recurrence_info),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ── Buttons ───────────────────────────────────────────────────────
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick  = { haptic.tick(); onBack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick  = {
                        if (title.isNotBlank()) {
                            haptic.success()
                            val countdown = buildCountdown()
                            if (isEdit) viewModel.updateCountdown(countdown)
                            else        viewModel.addCountdown(countdown)
                            onBack()
                        } else {
                            haptic.error()
                        }
                    },
                    enabled  = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save_button), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // ── Icon BottomSheet ──────────────────────────────────────────────────────
    if (showIconSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { haptic.tick(); showIconSheet = false },
            sheetState       = sheetState,
            containerColor   = MaterialTheme.colorScheme.surface,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text       = stringResource(R.string.section_icon),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyVerticalGrid(
                    columns               = GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(QUICK_EMOJIS) { emoji ->
                        Box(
                            modifier         = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (icon == emoji)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .then(
                                    if (icon == emoji)
                                        Modifier.border(
                                            1.5.dp,
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(10.dp)
                                        )
                                    else Modifier
                                )
                                .clickable {
                                    haptic.tick()
                                    icon = emoji
                                    showIconSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 22.sp)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                OutlinedTextField(
                    value         = icon,
                    onValueChange = { if (it.length <= 2) icon = it },
                    label         = { Text(stringResource(R.string.icon_placeholder)) },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    trailingIcon  = {
                        if (icon.isNotBlank()) {
                            Text(
                                text     = icon,
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }

    // ── Custom Color Picker ───────────────────────────────────────────────────
    if (showCustomColorPicker) {
        val init = try { android.graphics.Color.parseColor(selectedColor) }
        catch (e: Exception) { android.graphics.Color.parseColor("#FF7043") }
        var r by remember { mutableStateOf(android.graphics.Color.red(init)) }
        var g by remember { mutableStateOf(android.graphics.Color.green(init)) }
        var b by remember { mutableStateOf(android.graphics.Color.blue(init)) }

        AlertDialog(
            onDismissRequest = { haptic.tick(); showCustomColorPicker = false },
            title            = { Text(stringResource(R.string.custom_color_label)) },
            text             = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.rgb(r, g, b)))
                    )
                    Slider(value = r.toFloat(), onValueChange = { r = it.toInt() }, valueRange = 0f..255f)
                    Slider(value = g.toFloat(), onValueChange = { g = it.toInt() }, valueRange = 0f..255f)
                    Slider(value = b.toFloat(), onValueChange = { b = it.toInt() }, valueRange = 0f..255f)
                }
            },
            confirmButton = {
                Button(onClick = {
                    haptic.click()
                    selectedColor = String.format("#%02X%02X%02X", r, g, b)
                    showCustomColorPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { haptic.tick(); showCustomColorPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
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

    // ── Time Picker ───────────────────────────────────────────────────────────
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour   = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour      = true
        )
        AlertDialog(
            onDismissRequest = { haptic.tick(); showTimePicker = false },
            title            = { Text(stringResource(R.string.timepicker_title)) },
            text             = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    haptic.click()
                    selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { haptic.tick(); showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
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
private fun SectionCard(
    title   : String? = null,
    content : @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (title != null) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
private fun ColorCircle(
    color      : Color,
    isSelected : Boolean,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier
) {
    Box(
        modifier         = modifier
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected)
                    Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                null,
                tint     = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}