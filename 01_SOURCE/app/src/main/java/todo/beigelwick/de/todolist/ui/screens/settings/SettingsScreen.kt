package todo.beigelwick.de.todolist.ui.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.launch
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.DisplayFormat
import todo.beigelwick.de.todolist.data.model.DisplayUnit
import todo.beigelwick.de.todolist.ui.theme.AppLanguage
import todo.beigelwick.de.todolist.ui.theme.AppPreferences
import todo.beigelwick.de.todolist.ui.theme.CustomTheme
import todo.beigelwick.de.todolist.ui.theme.CustomThemePreferences
import todo.beigelwick.de.todolist.ui.theme.LanguageManager
import todo.beigelwick.de.todolist.ui.theme.ThemeMode
import todo.beigelwick.de.todolist.ui.theme.ThemePreferences
import todo.beigelwick.de.todolist.ui.theme.getThemeConfig
import todo.beigelwick.de.todolist.utils.HapticFeedback
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()
    val haptic      = remember { HapticFeedback(context) }
    val scrollState = rememberScrollState()

    val themeMode    by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
    val customTheme  by CustomThemePreferences.getCustomTheme(context).collectAsState(initial = CustomTheme.BURGUNDY)
    val defaultUnits by AppPreferences.getDefaultUnits(context).collectAsState(initial = setOf(DisplayUnit.DAYS))
    val defaultColor by AppPreferences.getDefaultColor(context).collectAsState(initial = "#FF7043")
    val defaultTime  by AppPreferences.getDefaultTime(context).collectAsState(initial = LocalTime.of(12, 0))
    val currentLang  by LanguageManager.getLanguage(context).collectAsState(initial = AppLanguage.SYSTEM)

    var showTimePicker   by remember { mutableStateOf(false) }
    var showColorPicker  by remember { mutableStateOf(false) }
    var showFormatPicker by remember { mutableStateOf(false) }

    // ── Einheits-Labels als Map im @Composable-Kontext aufbauen ──────────────
    // stringResource() darf NICHT in joinToString/map/forEach-Lambdas aufgerufen
    // werden – nur direkt in @Composable-Funktionen oder deren Content-Lambdas.
    val unitLabelMap = mapOf(
        DisplayUnit.YEARS   to stringResource(R.string.format_unit_years),
        DisplayUnit.MONTHS  to stringResource(R.string.format_unit_months),
        DisplayUnit.WEEKS   to stringResource(R.string.format_unit_weeks),
        DisplayUnit.DAYS    to stringResource(R.string.format_unit_days),
        DisplayUnit.HOURS   to stringResource(R.string.format_unit_hours),
        DisplayUnit.MINUTES to stringResource(R.string.format_unit_minutes),
        DisplayUnit.SECONDS to stringResource(R.string.format_unit_seconds),
    )

    // Jetzt normales joinToString ohne stringResource()-Aufruf darin
    val formatSummary = DisplayFormat.sorted(defaultUnits)
        .joinToString(" + ") { unit -> unitLabelMap[unit] ?: unit.name }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.topbar_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Farbschema ────────────────────────────────────────────────────
            SettingsSectionTitle(stringResource(R.string.settings_color_scheme))
            Text(stringResource(R.string.settings_color_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ThemePickerGrid(
                currentTheme    = customTheme,
                onThemeSelected = { theme ->
                    haptic.success()
                    scope.launch { CustomThemePreferences.setCustomTheme(context, theme) }
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Hell / Dunkel ─────────────────────────────────────────────────
            SettingsSectionTitle(stringResource(R.string.settings_light_dark))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeModeOption("⚙️  ${stringResource(R.string.design_system)}", themeMode == ThemeMode.SYSTEM) {
                    haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.SYSTEM) }
                }
                ThemeModeOption("🌞  ${stringResource(R.string.design_light)}", themeMode == ThemeMode.LIGHT) {
                    haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.LIGHT) }
                }
                ThemeModeOption("🌙  ${stringResource(R.string.design_dark)}", themeMode == ThemeMode.DARK) {
                    haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.DARK) }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Sprache ───────────────────────────────────────────────────────
            SettingsSectionTitle(stringResource(R.string.settings_language))
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Standard-Einstellungen ────────────────────────────────────────
            SettingsSectionTitle(stringResource(R.string.settings_defaults))
            Text(stringResource(R.string.settings_defaults_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            SettingsRow(
                label   = stringResource(R.string.settings_format_label),
                value   = formatSummary,
                onClick = { haptic.tick(); showFormatPicker = true }
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

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ── Format-Picker Dialog ──────────────────────────────────────────────────
    if (showFormatPicker) {
        FormatPickerDialog(
            currentUnits = defaultUnits,
            unitLabelMap = unitLabelMap,
            onDismiss    = { showFormatPicker = false },
            onConfirm    = { units ->
                scope.launch { AppPreferences.setDefaultUnits(context, units) }
                showFormatPicker = false
            }
        )
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("#FF7043","#EF5350","#EC407A","#AB47BC","#5C6BC0",
                            "#42A5F5","#26A69A","#66BB6A","#FFA726","#8D6E63"
                        ).forEach { hex ->
                            val c = try { Color(android.graphics.Color.parseColor(hex)) }
                            catch (e: Exception) { MaterialTheme.colorScheme.primary }
                            Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = c, onClick = {
                                scope.launch { AppPreferences.setDefaultColor(context, hex) }
                                showColorPicker = false
                            }) {
                                if (defaultColor == hex) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(android.graphics.Color.rgb(r, g, b))))
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
                    scope.launch { AppPreferences.setDefaultColor(context, String.format("#%02X%02X%02X", r, g, b)) }
                    showColorPicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showColorPicker = false }) { Text(stringResource(R.string.cancel)) } }
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
                    scope.launch { AppPreferences.setDefaultTime(context, LocalTime.of(timePickerState.hour, timePickerState.minute)) }
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

// ─── Format-Picker Dialog ─────────────────────────────────────────────────────

@Composable
private fun FormatPickerDialog(
    currentUnits : Set<DisplayUnit>,
    unitLabelMap : Map<DisplayUnit, String>,
    onDismiss    : () -> Unit,
    onConfirm    : (Set<DisplayUnit>) -> Unit
) {
    val dialogUnits = remember { currentUnits.toMutableStateList() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_default_format)) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    text     = stringResource(R.string.format_checkbox_hint),
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                // Alle Einheiten in der kanonischen Reihenfolge
                DisplayUnit.values().forEach { unit ->
                    val label     = unitLabelMap[unit] ?: unit.name
                    val isChecked = dialogUnits.contains(unit)
                    FormatCheckboxRow(
                        label     = label,
                        isChecked = isChecked,
                        onToggle  = {
                            if (isChecked) { if (dialogUnits.size > 1) dialogUnits.remove(unit) }
                            else dialogUnits.add(unit)
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(dialogUnits.toSet()) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun FormatCheckboxRow(
    label     : String,
    isChecked : Boolean,
    onToggle  : () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Checkbox(
            checked         = isChecked,
            onCheckedChange = { onToggle() },
            colors          = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
    }
}

// ─── Theme Picker Grid ────────────────────────────────────────────────────────

@Composable
private fun ThemePickerGrid(currentTheme: CustomTheme, onThemeSelected: (CustomTheme) -> Unit) {
    val chunks = CustomTheme.values().toList().chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        chunks.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { theme ->
                    ThemePickerCard(theme = theme, isSelected = currentTheme == theme, onClick = { onThemeSelected(theme) }, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ThemePickerCard(theme: CustomTheme, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val config       = getThemeConfig(theme)
    val primaryLight = config.lightColorScheme.primary
    val secondary    = config.lightColorScheme.secondary
    val tertiary     = config.lightColorScheme.tertiary

    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        animationSpec = tween(200),
        label         = "bg_${theme.name}"
    )

    Surface(
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        color    = bgColor,
        border   = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        onClick  = onClick
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(primaryLight))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.width(22.dp).height(11.dp).clip(RoundedCornerShape(4.dp)).background(secondary))
                    Box(modifier = Modifier.width(22.dp).height(11.dp).clip(RoundedCornerShape(4.dp)).background(tertiary))
                }
                if (isSelected) {
                    Spacer(Modifier.weight(1f))
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp)) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            val (name, desc) = when (theme) {
                CustomTheme.BURGUNDY -> stringResource(R.string.theme_burgundy_name) to stringResource(R.string.theme_burgundy_desc)
                CustomTheme.SAGE     -> stringResource(R.string.theme_sage_name)     to stringResource(R.string.theme_sage_desc)
                CustomTheme.PUMPKIN  -> stringResource(R.string.theme_pumpkin_name)  to stringResource(R.string.theme_pumpkin_desc)
                CustomTheme.OCEAN    -> stringResource(R.string.theme_ocean_name)    to stringResource(R.string.theme_ocean_desc)
                CustomTheme.VIOLET   -> stringResource(R.string.theme_violet_name)   to stringResource(R.string.theme_violet_desc)
                CustomTheme.PEACH    -> stringResource(R.string.theme_peach_name)    to stringResource(R.string.theme_peach_desc)
            }
            Text(text = name, style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Sprach-Auswahl ───────────────────────────────────────────────────────────

@Composable
private fun LanguagePickerSection(currentLanguage: AppLanguage, onLanguageSelected: (AppLanguage) -> Unit) {
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
                color    = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                onClick  = { onLanguageSelected(language) }
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = flags[language] ?: "🌐", fontSize = 20.sp)
                        Text(
                            text       = language.displayName,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color      = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// ─── Settings Helpers ─────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun ThemeModeOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        color    = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        onClick  = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick  = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SettingsRowColor(label: String, color: String, onClick: () -> Unit) {
    val parsedColor = try { Color(android.graphics.Color.parseColor(color)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium,
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick  = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(parsedColor))
        }
    }
}