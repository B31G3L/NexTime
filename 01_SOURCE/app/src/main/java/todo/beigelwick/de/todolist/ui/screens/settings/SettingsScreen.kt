package todo.beigelwick.de.todolist.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.DisplayUnit
import todo.beigelwick.de.todolist.data.model.RecurrenceType
import todo.beigelwick.de.todolist.ui.components.CountdownCard
import todo.beigelwick.de.todolist.ui.theme.AccentColor
import todo.beigelwick.de.todolist.ui.theme.AccentColorPreferences
import todo.beigelwick.de.todolist.ui.theme.AppLanguage
import todo.beigelwick.de.todolist.ui.theme.AppPreferences
import todo.beigelwick.de.todolist.ui.theme.DisplayStyle
import todo.beigelwick.de.todolist.ui.theme.LanguageManager
import todo.beigelwick.de.todolist.ui.theme.ThemeMode
import todo.beigelwick.de.todolist.ui.theme.ThemePreferences
import todo.beigelwick.de.todolist.utils.HapticFeedback
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ─── Dummy-Countdown für Preview ─────────────────────────────────────────────

private val PREVIEW_COUNTDOWN = Countdown(
    id             = -1L,
    title          = "Sommerurlaub",
    icon           = "✈️",
    targetDateTime = LocalDateTime.now().plusDays(42),
    displayFormat  = "",
    color          = "#FF7043",
    recurrence     = RecurrenceType.NONE.name
)

// ─── Settings Screen ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()
    val haptic      = remember { HapticFeedback(context) }
    val scrollState = rememberScrollState()

    // ── Preferences ──────────────────────────────────────────────────────────
    val themeMode        by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
    val accentColor      by AccentColorPreferences.getAccentColor(context).collectAsState(initial = AccentColor.ORANGE)
    val defaultColor     by AppPreferences.getDefaultColor(context).collectAsState(initial = "#FF7043")
    val defaultTime      by AppPreferences.getDefaultTime(context).collectAsState(initial = LocalTime.of(12, 0))
    val defaultDateUnits by AppPreferences.getDefaultDateUnits(context).collectAsState(initial = setOf(DisplayUnit.DAYS))
    val showTimeOnCard   by AppPreferences.getShowTimeOnCard(context).collectAsState(initial = false)
    val currentLang      by LanguageManager.getLanguage(context).collectAsState(initial = AppLanguage.SYSTEM)
    val displayStyle     by AppPreferences.getDisplayStyle(context).collectAsState(initial = DisplayStyle.NORMAL)

    // ── Expanded States ───────────────────────────────────────────────────────
    var expandedDarstellung   by remember { mutableStateOf(false) }
    var expandedAkzentfarbe   by remember { mutableStateOf(false) }
    var expandedHellDunkel    by remember { mutableStateOf(false) }
    var expandedSprache       by remember { mutableStateOf(false) }
    var expandedStandards     by remember { mutableStateOf(false) }
    var expandedAnzeigeformat by remember { mutableStateOf(false) }

    var showTimePicker  by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    // ── Summaries ─────────────────────────────────────────────────────────────
    val darstellungSummary = when (displayStyle) {
        DisplayStyle.COMPACT  -> stringResource(R.string.display_style_compact)
        DisplayStyle.NORMAL   -> stringResource(R.string.display_style_normal)
        DisplayStyle.GENEROUS -> stringResource(R.string.display_style_generous)
    }
    val akzentfarbeSummary = when (accentColor) {
        AccentColor.ORANGE  -> stringResource(R.string.accent_orange)
        AccentColor.SAGE    -> stringResource(R.string.accent_sage)
        AccentColor.VIOLET  -> stringResource(R.string.accent_violet)
        AccentColor.CRIMSON -> stringResource(R.string.accent_crimson)
        AccentColor.TEAL    -> stringResource(R.string.accent_teal)
        AccentColor.GOLD    -> stringResource(R.string.accent_gold)
        AccentColor.SLATE   -> stringResource(R.string.accent_slate)
    }
    val hellDunkelSummary = when (themeMode) {
        ThemeMode.SYSTEM -> stringResource(R.string.design_system)
        ThemeMode.LIGHT  -> stringResource(R.string.design_light)
        ThemeMode.DARK   -> stringResource(R.string.design_dark)
    }
    val spracheSummary = currentLang.displayName
    val standardsSummary = defaultTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr"
    val dateUnitLabels = mapOf(
        DisplayUnit.YEARS  to stringResource(R.string.format_unit_years),
        DisplayUnit.MONTHS to stringResource(R.string.format_unit_months),
        DisplayUnit.WEEKS  to stringResource(R.string.format_unit_weeks),
        DisplayUnit.DAYS   to stringResource(R.string.format_unit_days),
    )
    val anzeigeformatSummary = buildString {
        val sorted = listOf(DisplayUnit.YEARS, DisplayUnit.MONTHS, DisplayUnit.WEEKS, DisplayUnit.DAYS)
            .filter { it in defaultDateUnits }
            .mapNotNull { dateUnitLabels[it] }
        append(sorted.joinToString(" + "))
        if (showTimeOnCard) append(" + Uhrzeit")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.topbar_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── 1. Darstellung ────────────────────────────────────────────────
            ExpandableSection(
                title    = stringResource(R.string.settings_display_style),
                summary  = if (!expandedDarstellung) darstellungSummary else null,
                expanded = expandedDarstellung,
                onToggle = { haptic.tick(); expandedDarstellung = !expandedDarstellung }
            ) {
                Text(
                    text  = stringResource(R.string.settings_display_style_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DisplayStylePicker(
                    currentStyle    = displayStyle,
                    onStyleSelected = { style ->
                        haptic.success()
                        scope.launch { AppPreferences.setDisplayStyle(context, style) }
                    }
                )
            }

            // ── 2. Anzeigeformat ──────────────────────────────────────────────
            ExpandableSection(
                title    = stringResource(R.string.settings_format_label),
                summary  = if (!expandedAnzeigeformat) anzeigeformatSummary else null,
                expanded = expandedAnzeigeformat,
                onToggle = { haptic.tick(); expandedAnzeigeformat = !expandedAnzeigeformat }
            ) {
                // Datumseinheiten
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    color    = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        listOf(
                            DisplayUnit.YEARS  to stringResource(R.string.format_unit_years),
                            DisplayUnit.MONTHS to stringResource(R.string.format_unit_months),
                            DisplayUnit.WEEKS  to stringResource(R.string.format_unit_weeks),
                            DisplayUnit.DAYS   to stringResource(R.string.format_unit_days),
                        ).forEach { (unit, label) ->
                            val isChecked = unit in defaultDateUnits
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val newUnits = if (isChecked) {
                                            if (defaultDateUnits.size > 1) defaultDateUnits - unit
                                            else defaultDateUnits
                                        } else {
                                            defaultDateUnits + unit
                                        }
                                        haptic.tick()
                                        scope.launch { AppPreferences.setDefaultDateUnits(context, newUnits) }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text       = label,
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                    color      = if (isChecked) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Checkbox(checked = isChecked, onCheckedChange = null)
                            }
                        }
                    }
                }

                // Uhrzeit-Toggle
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(10.dp),
                    color    = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text       = "Uhrzeit (HH:mm:ss)",
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (showTimeOnCard) FontWeight.SemiBold else FontWeight.Normal,
                                color      = if (showTimeOnCard) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text  = "Wird unter den Datumseinheiten angezeigt",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked         = showTimeOnCard,
                            onCheckedChange = { checked ->
                                haptic.tick()
                                scope.launch { AppPreferences.setShowTimeOnCard(context, checked) }
                            }
                        )
                    }
                }

                // Vorschau
                HorizontalDivider(
                    color    = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    stringResource(R.string.preview_label),
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                key(defaultDateUnits, showTimeOnCard) {
                    CountdownCard(countdown = PREVIEW_COUNTDOWN)
                }
            }

            // ── 3. Akzentfarbe ────────────────────────────────────────────────
            ExpandableSection(
                title    = stringResource(R.string.settings_color_scheme),
                summary  = if (!expandedAkzentfarbe) akzentfarbeSummary else null,
                expanded = expandedAkzentfarbe,
                onToggle = { haptic.tick(); expandedAkzentfarbe = !expandedAkzentfarbe }
            ) {
                Text(
                    text  = stringResource(R.string.settings_accent_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AccentColorPicker(
                    currentAccent    = accentColor,
                    onAccentSelected = { accent ->
                        haptic.success()
                        scope.launch { AccentColorPreferences.setAccentColor(context, accent) }
                    }
                )
            }

            // ── 4. Hell / Dunkel ──────────────────────────────────────────────
            ExpandableSection(
                title    = stringResource(R.string.settings_light_dark),
                summary  = if (!expandedHellDunkel) hellDunkelSummary else null,
                expanded = expandedHellDunkel,
                onToggle = { haptic.tick(); expandedHellDunkel = !expandedHellDunkel }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemeModeOption(
                        label      = "⚙️  ${stringResource(R.string.design_system)}",
                        isSelected = themeMode == ThemeMode.SYSTEM,
                        onClick    = { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.SYSTEM) } }
                    )
                    ThemeModeOption(
                        label      = "🌞  ${stringResource(R.string.design_light)}",
                        isSelected = themeMode == ThemeMode.LIGHT,
                        onClick    = { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.LIGHT) } }
                    )
                    ThemeModeOption(
                        label      = "🌙  ${stringResource(R.string.design_dark)}",
                        isSelected = themeMode == ThemeMode.DARK,
                        onClick    = { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.DARK) } }
                    )
                }
            }

            // ── 5. Sprache ────────────────────────────────────────────────────
            ExpandableSection(
                title    = stringResource(R.string.settings_language),
                summary  = if (!expandedSprache) spracheSummary else null,
                expanded = expandedSprache,
                onToggle = { haptic.tick(); expandedSprache = !expandedSprache }
            ) {
                LanguagePickerSection(
                    currentLanguage    = currentLang,
                    onLanguageSelected = { language ->
                        haptic.tick()
                        LanguageManager.persistLanguageSync(context, language)
                        scope.launch { LanguageManager.setLanguage(context, language) }
                        val localeList = if (language == AppLanguage.SYSTEM || language.tag.isEmpty())
                            LocaleListCompat.getEmptyLocaleList()
                        else
                            LocaleListCompat.forLanguageTags(language.tag)
                        AppCompatDelegate.setApplicationLocales(localeList)
                    }
                )
            }

            // ── 6. Standards für neue Einträge ────────────────────────────────
            ExpandableSection(
                title    = stringResource(R.string.settings_defaults),
                summary  = if (!expandedStandards) standardsSummary else null,
                expanded = expandedStandards,
                onToggle = { haptic.tick(); expandedStandards = !expandedStandards }
            ) {
                Text(
                    text  = stringResource(R.string.settings_defaults_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SettingsRowColor(
                    label   = stringResource(R.string.settings_color_label),
                    color   = defaultColor,
                    onClick = { haptic.tick(); showColorPicker = true }
                )
                SettingsRow(
                    label   = stringResource(R.string.settings_time_label),
                    value   = defaultTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr",
                    onClick = { haptic.tick(); showTimePicker = true }
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── Farb-Picker Dialog ────────────────────────────────────────────────────
    if (showColorPicker) {
        val initialColor = try { android.graphics.Color.parseColor(defaultColor) }
        catch (e: Exception) { android.graphics.Color.parseColor("#FF7043") }
        var r by remember { mutableStateOf(android.graphics.Color.red(initialColor)) }
        var g by remember { mutableStateOf(android.graphics.Color.green(initialColor)) }
        var b by remember { mutableStateOf(android.graphics.Color.blue(initialColor)) }

        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text(stringResource(R.string.dialog_default_color)) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Schnellauswahl
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            "#FF7043","#EF5350","#EC407A","#AB47BC","#5C6BC0",
                            "#42A5F5","#26A69A","#66BB6A","#FFA726","#8D6E63"
                        ).forEach { hex ->
                            val c = try { Color(android.graphics.Color.parseColor(hex)) }
                            catch (e: Exception) { MaterialTheme.colorScheme.primary }
                            Surface(
                                modifier = Modifier.size(28.dp),
                                shape    = CircleShape,
                                color    = c,
                                onClick  = {
                                    scope.launch { AppPreferences.setDefaultColor(context, hex) }
                                    showColorPicker = false
                                }
                            ) {
                                if (defaultColor == hex) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(android.graphics.Color.rgb(r, g, b))))
                    Slider(value = r.toFloat(), onValueChange = { r = it.toInt() }, valueRange = 0f..255f)
                    Slider(value = g.toFloat(), onValueChange = { g = it.toInt() }, valueRange = 0f..255f)
                    Slider(value = b.toFloat(), onValueChange = { b = it.toInt() }, valueRange = 0f..255f)
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch { AppPreferences.setDefaultColor(context, String.format("#%02X%02X%02X", r, g, b)) }
                    showColorPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showColorPicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // ── Zeit-Picker Dialog ────────────────────────────────────────────────────
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour   = defaultTime.hour,
            initialMinute = defaultTime.minute,
            is24Hour      = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.dialog_default_time)) },
            text  = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        AppPreferences.setDefaultTime(
                            context,
                            LocalTime.of(timePickerState.hour, timePickerState.minute)
                        )
                    }
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

// ─── Expandable Section ───────────────────────────────────────────────────────

@Composable
private fun ExpandableSection(
    title    : String,
    summary  : String? = null,
    expanded : Boolean,
    onToggle : () -> Unit,
    content  : @Composable ColumnScope.() -> Unit
) {
    val arrowRotation by animateFloatAsState(
        targetValue   = if (expanded) 180f else 0f,
        animationSpec = tween(250),
        label         = "arrow_$title"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border   = BorderStroke(
            width = 1.dp,
            color = if (expanded)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.30f)
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            // Header
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (expanded) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (!summary.isNullOrBlank()) {
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text  = summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector        = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint               = if (expanded) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(20.dp).rotate(arrowRotation)
                )
            }

            // Inhalt
            androidx.compose.animation.AnimatedVisibility(
                visible = expanded,
                enter   = expandVertically(tween(280)) + fadeIn(tween(220)),
                exit    = shrinkVertically(tween(220)) + fadeOut(tween(180))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )
            }
        }
    }
}

// ─── Akzentfarben-Picker ──────────────────────────────────────────────────────

@Composable
private fun AccentColorPicker(
    currentAccent    : AccentColor,
    onAccentSelected : (AccentColor) -> Unit
) {
    val allAccents = AccentColor.values().toList()
    // 4 pro Reihe
    val rows = allAccents.chunked(4)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { accent ->
                    AccentColorTile(
                        accent      = accent,
                        isSelected  = currentAccent == accent,
                        onClick     = { onAccentSelected(accent) },
                        modifier    = Modifier.weight(1f)
                    )
                }
                repeat(4 - row.size) { Box(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun AccentColorTile(
    accent     : AccentColor,
    isSelected : Boolean,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) accent.light else Color.Transparent,
        animationSpec = tween(200),
        label         = "border_${accent.name}"
    )
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected)
            accent.light.copy(alpha = 0.08f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label         = "bg_${accent.name}"
    )
    val accentName = when (accent) {
        AccentColor.ORANGE  -> stringResource(R.string.accent_orange)
        AccentColor.SAGE    -> stringResource(R.string.accent_sage)
        AccentColor.VIOLET  -> stringResource(R.string.accent_violet)
        AccentColor.CRIMSON -> stringResource(R.string.accent_crimson)
        AccentColor.TEAL    -> stringResource(R.string.accent_teal)
        AccentColor.GOLD    -> stringResource(R.string.accent_gold)
        AccentColor.SLATE   -> stringResource(R.string.accent_slate)
    }

    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(12.dp),
        color    = bgColor,
        border   = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor.copy(
            alpha = if (isSelected) 1f else 0.15f
        )),
        onClick  = onClick
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(accent.light),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text       = accentName,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color      = if (isSelected) accent.light
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines   = 1,
                fontSize   = 10.sp
            )
        }
    }
}

// ─── DisplayStyle Picker ──────────────────────────────────────────────────────

@Composable
private fun DisplayStylePicker(
    currentStyle    : DisplayStyle,
    onStyleSelected : (DisplayStyle) -> Unit
) {
    val styles = listOf(
        DisplayStyle.COMPACT  to stringResource(R.string.display_style_compact),
        DisplayStyle.NORMAL   to stringResource(R.string.display_style_normal),
        DisplayStyle.GENEROUS to stringResource(R.string.display_style_generous),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        styles.forEach { (style, label) ->
            DisplayStyleCard(
                style           = style,
                label           = label,
                isSelected      = currentStyle == style,
                onStyleSelected = onStyleSelected
            )
        }
    }
}

@Composable
private fun DisplayStyleCard(
    style           : DisplayStyle,
    label           : String,
    isSelected      : Boolean,
    onStyleSelected : (DisplayStyle) -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(200),
        label         = "border_${style.name}"
    )
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label         = "bg_${style.name}"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        color    = bgColor,
        border   = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        onClick  = { onStyleSelected(style) }
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = label,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                )
                if (isSelected) {
                    Surface(
                        shape    = CircleShape,
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onPrimary,
                                modifier           = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))) {
                CountdownCard(countdown = PREVIEW_COUNTDOWN, previewStyle = style)
            }
        }
    }
}

// ─── Sprach-Auswahl ───────────────────────────────────────────────────────────

@Composable
private fun LanguagePickerSection(
    currentLanguage    : AppLanguage,
    onLanguageSelected : (AppLanguage) -> Unit
) {
    val flags = mapOf(
        AppLanguage.SYSTEM  to "🌐",
        AppLanguage.GERMAN  to "🇩🇪",
        AppLanguage.ENGLISH to "🇬🇧",
        AppLanguage.FRENCH  to "🇫🇷",
        AppLanguage.SPANISH to "🇪🇸",
        AppLanguage.ITALIAN to "🇮🇹"
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AppLanguage.values().forEach { language ->
            val isSelected = currentLanguage == language
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = MaterialTheme.shapes.medium,
                color    = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                else
                    MaterialTheme.colorScheme.surface,
                border   = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                onClick  = { onLanguageSelected(language) }
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(text = flags[language] ?: "🌐", fontSize = 18.sp)
                        Text(
                            text       = language.displayName,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier           = Modifier.size(16.dp),
                            tint               = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun ThemeModeOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        color    = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else
            MaterialTheme.colorScheme.surface,
        border   = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        onClick  = onClick
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color      = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surface,
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        onClick  = onClick
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text     = label,
                style    = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text       = value,
                style      = MaterialTheme.typography.bodyMedium,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SettingsRowColor(label: String, color: String, onClick: () -> Unit) {
    val parsedColor = try { Color(android.graphics.Color.parseColor(color)) }
    catch (e: Exception) { MaterialTheme.colorScheme.primary }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surface,
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        onClick  = onClick
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(parsedColor)
            )
        }
    }
}