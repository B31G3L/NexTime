package com.beigel.nextime.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.beigel.nextime.R
import com.beigel.nextime.data.model.Countdown
import com.beigel.nextime.data.model.DisplayUnit
import com.beigel.nextime.data.model.RecurrenceType
import com.beigel.nextime.ui.components.CountdownCard
import com.beigel.nextime.ui.theme.AccentColor
import com.beigel.nextime.ui.theme.AccentColorPreferences
import com.beigel.nextime.ui.theme.AppLanguage
import com.beigel.nextime.ui.theme.AppPreferences
import com.beigel.nextime.ui.theme.DisplayStyle
import com.beigel.nextime.ui.theme.LanguageManager
import com.beigel.nextime.ui.theme.ThemeMode
import com.beigel.nextime.ui.theme.ThemePreferences
import com.beigel.nextime.ui.viewmodel.CountdownViewModel
import com.beigel.nextime.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ─── Settings Screen ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack    : () -> Unit,
    viewModel : CountdownViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val haptic  = remember { HapticFeedback(context) }

    // ── Lokalisierter Preview-Titel ───────────────────────────────────────────
    val previewTitle = stringResource(R.string.preview_countdown_title)
    val previewCountdown = remember(previewTitle) {
        Countdown(
            id = -1L,
            title = previewTitle,
            icon = "FlightTakeoff",
            targetDateTime = LocalDateTime.now().plusDays(42),
            displayFormat = "",
            color = "#FF7043",
            recurrence = RecurrenceType.NONE.name
        )
    }

    // ── Preferences ──────────────────────────────────────────────────────────
    val themeMode        by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
    val accentColor      by AccentColorPreferences.getAccentColor(context).collectAsState(initial = AccentColor.ORANGE)
    val defaultColor     by AppPreferences.getDefaultColor(context).collectAsState(initial = "#FF7043")
    val defaultTime      by AppPreferences.getDefaultTime(context).collectAsState(initial = LocalTime.of(12, 0))
    val defaultDateUnits by AppPreferences.getDefaultDateUnits(context).collectAsState(initial = setOf(
        DisplayUnit.DAYS))
    val showTimeOnCard   by AppPreferences.getShowTimeOnCard(context).collectAsState(initial = false)
    val currentLang      by LanguageManager.getLanguage(context).collectAsState(initial = AppLanguage.SYSTEM)
    val displayStyle     by AppPreferences.getDisplayStyle(context).collectAsState(initial = DisplayStyle.STANDARD)

    // ── Sheet-States ──────────────────────────────────────────────────────────
    var activeSheet by remember { mutableStateOf<SettingsSheet?>(null) }

    // ── Zusammenfassungstexte ─────────────────────────────────────────────────
    val darstellungSummary = when (displayStyle) {
        DisplayStyle.STANDARD   -> stringResource(R.string.card_style_standard)
        DisplayStyle.KOMPAKT    -> stringResource(R.string.card_style_kompakt)
        DisplayStyle.BANNER     -> stringResource(R.string.card_style_banner)
        DisplayStyle.INVERTIERT -> stringResource(R.string.card_style_invertiert)
    }
    val accentSummary = when (accentColor) {
        AccentColor.ORANGE  -> stringResource(R.string.accent_orange)
        AccentColor.SAGE    -> stringResource(R.string.accent_sage)
        AccentColor.VIOLET  -> stringResource(R.string.accent_violet)
        AccentColor.CRIMSON -> stringResource(R.string.accent_crimson)
        AccentColor.TEAL    -> stringResource(R.string.accent_teal)
        AccentColor.GOLD    -> stringResource(R.string.accent_gold)
        AccentColor.SLATE   -> stringResource(R.string.accent_slate)
    }
    val themeSummary = when (themeMode) {
        ThemeMode.SYSTEM -> stringResource(R.string.design_system)
        ThemeMode.LIGHT  -> stringResource(R.string.design_light)
        ThemeMode.DARK   -> stringResource(R.string.design_dark)
    }
    val dateUnitLabels = mapOf(
        DisplayUnit.YEARS  to stringResource(R.string.format_unit_years),
        DisplayUnit.MONTHS to stringResource(R.string.format_unit_months),
        DisplayUnit.WEEKS  to stringResource(R.string.format_unit_weeks),
        DisplayUnit.DAYS   to stringResource(R.string.format_unit_days),
    )
    val formatSummary = buildString {
        val sorted = listOf(DisplayUnit.YEARS, DisplayUnit.MONTHS, DisplayUnit.WEEKS, DisplayUnit.DAYS)
            .filter { it in defaultDateUnits }
            .mapNotNull { dateUnitLabels[it] }
        append(sorted.joinToString(" + "))
        if (showTimeOnCard) append(" + ${stringResource(R.string.format_unit_hours)}")
    }
    val standardsSummary = stringResource(R.string.time_oclock, defaultTime.format(DateTimeFormatter.ofPattern("HH:mm")))

    // ── Backup: Export/Import Launcher (auf Screen-Ebene, damit sie über
    //    Recompositions der Sheet-Inhalte hinweg stabil bleiben) ───────────────
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = viewModel.exportBackupJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                }
                haptic.success()
                Toast.makeText(context, context.getString(R.string.backup_export_success), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                haptic.tick()
                Toast.makeText(context, context.getString(R.string.backup_export_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader(Charsets.UTF_8).readText()
                    }
                }
                if (json.isNullOrBlank()) {
                    haptic.tick()
                    Toast.makeText(context, context.getString(R.string.backup_import_error), Toast.LENGTH_SHORT).show()
                    return@launch
                }
                viewModel.importBackupJson(json) { result ->
                    result.onSuccess { count ->
                        if (count == 0) {
                            haptic.tick()
                            Toast.makeText(context, context.getString(R.string.backup_import_empty), Toast.LENGTH_SHORT).show()
                        } else {
                            haptic.success()
                            Toast.makeText(
                                context,
                                context.getString(R.string.backup_import_success, count),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.onFailure {
                        haptic.tick()
                        Toast.makeText(context, context.getString(R.string.backup_import_error), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                haptic.tick()
                Toast.makeText(context, context.getString(R.string.backup_import_error), Toast.LENGTH_SHORT).show()
            }
        }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // ── Gruppe: Darstellung ──────────────────────────────────────────────
            SettingsGroupHeader(stringResource(R.string.settings_group_display))
            SettingsGroup {
                SettingsListItem(
                    title    = stringResource(R.string.settings_display_style),
                    value    = darstellungSummary,
                    onClick  = { haptic.tick(); activeSheet = SettingsSheet.DARSTELLUNG }
                )
                SettingsDivider()
                SettingsListItem(
                    title   = stringResource(R.string.settings_format_label),
                    value   = formatSummary,
                    onClick = { haptic.tick(); activeSheet = SettingsSheet.FORMAT }
                )
                SettingsDivider()
                SettingsListItem(
                    title        = stringResource(R.string.settings_color_scheme),
                    value        = accentSummary,
                    trailingColor = accentColor.light,
                    onClick      = { haptic.tick(); activeSheet = SettingsSheet.AKZENTFARBE }
                )
                SettingsDivider()
                SettingsListItem(
                    title   = stringResource(R.string.settings_light_dark),
                    value   = themeSummary,
                    onClick = { haptic.tick(); activeSheet = SettingsSheet.HELL_DUNKEL }
                )
            }

            // ── Gruppe: Allgemein ────────────────────────────────────────────────
            SettingsGroupHeader(stringResource(R.string.settings_group_general))
            SettingsGroup {
                SettingsListItem(
                    title   = stringResource(R.string.settings_language),
                    value   = currentLang.displayName,
                    onClick = { haptic.tick(); activeSheet = SettingsSheet.SPRACHE }
                )
                SettingsDivider()
                SettingsListItem(
                    title   = stringResource(R.string.settings_defaults),
                    value   = standardsSummary,
                    onClick = { haptic.tick(); activeSheet = SettingsSheet.STANDARDS }
                )
            }

            // ── Gruppe: Daten ────────────────────────────────────────────────────
            SettingsGroupHeader(stringResource(R.string.settings_group_data))
            SettingsGroup {
                SettingsListItem(
                    title   = stringResource(R.string.settings_backup),
                    value   = "",
                    onClick = { haptic.tick(); activeSheet = SettingsSheet.BACKUP }
                )
            }
        }
    }

    // ── Bottom Sheets ─────────────────────────────────────────────────────────

    activeSheet?.let { sheet ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { haptic.tick(); activeSheet = null },
            sheetState       = sheetState,
            containerColor   = MaterialTheme.colorScheme.surface,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sheet-Titel
                Text(
                    text = when (sheet) {
                        SettingsSheet.DARSTELLUNG  -> stringResource(R.string.settings_display_style)
                        SettingsSheet.FORMAT       -> stringResource(R.string.settings_format_label)
                        SettingsSheet.AKZENTFARBE  -> stringResource(R.string.settings_color_scheme)
                        SettingsSheet.HELL_DUNKEL  -> stringResource(R.string.settings_light_dark)
                        SettingsSheet.SPRACHE      -> stringResource(R.string.settings_language)
                        SettingsSheet.STANDARDS    -> stringResource(R.string.settings_defaults)
                        SettingsSheet.BACKUP       -> stringResource(R.string.settings_backup)
                    },
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(top = 4.dp)
                )

                when (sheet) {

                    // ── Darstellung (Karten-Art) ──────────────────────────────
                    SettingsSheet.DARSTELLUNG -> {
                        Text(stringResource(R.string.settings_display_style_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf(
                            DisplayStyle.STANDARD   to stringResource(R.string.card_style_standard),
                            DisplayStyle.KOMPAKT    to stringResource(R.string.card_style_kompakt),
                            DisplayStyle.BANNER     to stringResource(R.string.card_style_banner),
                            DisplayStyle.INVERTIERT to stringResource(R.string.card_style_invertiert),
                        ).forEach { (style, label) ->
                            val isSelected = displayStyle == style
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp),
                                color    = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
                                border   = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                onClick  = { haptic.success(); scope.launch { AppPreferences.setDisplayStyle(context, style) } }
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                        if (isSelected) Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(11.dp))
                                            }
                                        }
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))) {
                                        CountdownCard(
                                            countdown = previewCountdown,
                                            previewStyle = style
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Anzeigeformat ─────────────────────────────────────────
                    SettingsSheet.FORMAT -> {
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
                                            if (defaultDateUnits.size > 1) defaultDateUnits - unit else defaultDateUnits
                                        } else defaultDateUnits + unit
                                        haptic.tick()
                                        scope.launch { AppPreferences.setDefaultDateUnits(context, newUnits) }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 10.dp),
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal, color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                Checkbox(checked = isChecked, onCheckedChange = null)
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(
                            modifier              = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.settings_time_display_label), style = MaterialTheme.typography.bodyMedium, fontWeight = if (showTimeOnCard) FontWeight.SemiBold else FontWeight.Normal, color = if (showTimeOnCard) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                Text(stringResource(R.string.settings_time_display_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = showTimeOnCard, onCheckedChange = { checked -> haptic.tick(); scope.launch { AppPreferences.setShowTimeOnCard(context, checked) } })
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(stringResource(R.string.preview_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        key(defaultDateUnits, showTimeOnCard) {
                            CountdownCard(
                                countdown = previewCountdown
                            )
                        }
                    }

                    // ── Akzentfarbe ───────────────────────────────────────────
                    SettingsSheet.AKZENTFARBE -> {
                        Text(stringResource(R.string.settings_accent_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val rows = AccentColor.values().toList().chunked(4)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rows.forEach { row ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { accent ->
                                        val isSelected = accentColor == accent
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
                                            modifier = Modifier.weight(1f),
                                            shape    = RoundedCornerShape(12.dp),
                                            color    = if (isSelected) accent.light.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                            border   = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent.light else Color.Transparent.copy(alpha = 0.15f)),
                                            onClick  = { haptic.success(); scope.launch { AccentColorPreferences.setAccentColor(context, accent) } }
                                        ) {
                                            Column(modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(accent.light), contentAlignment = Alignment.Center) {
                                                    if (isSelected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                }
                                                Text(accentName, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) accent.light else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, fontSize = 10.sp)
                                            }
                                        }
                                    }
                                    repeat(4 - row.size) { Box(modifier = Modifier.weight(1f)) }
                                }
                            }
                        }
                    }

                    // ── Hell / Dunkel ─────────────────────────────────────────
                    SettingsSheet.HELL_DUNKEL -> {
                        listOf(
                            Triple(ThemeMode.SYSTEM, stringResource(R.string.design_system), Icons.Default.BrightnessAuto),
                            Triple(ThemeMode.LIGHT,  stringResource(R.string.design_light),  Icons.Default.Brightness7),
                            Triple(ThemeMode.DARK,   stringResource(R.string.design_dark),   Icons.Default.Brightness4),
                        ).forEach { (mode, label, icon) ->
                            val isSelected = themeMode == mode
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = MaterialTheme.shapes.medium,
                                color    = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
                                border   = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                onClick  = { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, mode) } }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icon, null, modifier = Modifier.size(20.dp), tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                    }
                                    RadioButton(selected = isSelected, onClick = { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, mode) } })
                                }
                            }
                        }
                    }

                    // ── Sprache ───────────────────────────────────────────────
                    SettingsSheet.SPRACHE -> {
                        AppLanguage.values().forEach { language ->
                            val isSelected = currentLang == language
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = MaterialTheme.shapes.medium,
                                color    = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
                                border   = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                onClick  = {
                                    haptic.tick()
                                    LanguageManager.persistLanguageSync(context, language)
                                    scope.launch { LanguageManager.setLanguage(context, language) }
                                    val localeList = if (language == AppLanguage.SYSTEM || language.tag.isEmpty())
                                        LocaleListCompat.getEmptyLocaleList()
                                    else
                                        LocaleListCompat.forLanguageTags(language.tag)
                                    AppCompatDelegate.setApplicationLocales(localeList)
                                }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(shape = RoundedCornerShape(6.dp), color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.size(32.dp)) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text(language.tag.uppercase().ifEmpty { "SY" }.take(2), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                            }
                                        }
                                        Text(language.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                    }
                                    if (isSelected) Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    // ── Standards ─────────────────────────────────────────────
                    SettingsSheet.STANDARDS -> {
                        Text(stringResource(R.string.settings_defaults_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        var showColorPicker by remember { mutableStateOf(false) }
                        val parsedColor = try { Color(android.graphics.Color.parseColor(defaultColor)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }

                        Row(
                            modifier              = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { haptic.tick(); showColorPicker = !showColorPicker }.padding(horizontal = 4.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.settings_color_label), style = MaterialTheme.typography.bodyMedium)
                            Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(parsedColor))
                        }

                        if (showColorPicker) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    listOf("#FF7043","#EF5350","#EC407A","#AB47BC","#5C6BC0","#42A5F5","#26A69A","#66BB6A","#FFA726","#8D6E63").forEach { hex ->
                                        val c = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                                        Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = c, onClick = { scope.launch { AppPreferences.setDefaultColor(context, hex) }; showColorPicker = false }) {
                                            if (defaultColor == hex) Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        var showTimePicker by remember { mutableStateOf(false) }
                        Row(
                            modifier              = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { haptic.tick(); showTimePicker = !showTimePicker }.padding(horizontal = 4.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.settings_time_label), style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.time_oclock, defaultTime.format(DateTimeFormatter.ofPattern("HH:mm"))), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        }

                        if (showTimePicker) {
                            val timePickerState = rememberTimePickerState(initialHour = defaultTime.hour, initialMinute = defaultTime.minute, is24Hour = true)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                TimePicker(state = timePickerState)
                                Button(
                                    onClick  = { scope.launch { AppPreferences.setDefaultTime(context, LocalTime.of(timePickerState.hour, timePickerState.minute)) }; showTimePicker = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(stringResource(R.string.ok)) }
                            }
                        }
                    }

                    // ── Backup (Export / Import) ──────────────────────────────
                    SettingsSheet.BACKUP -> {
                        Text(stringResource(R.string.settings_backup_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        BackupActionRow(
                            icon    = Icons.Default.FileDownload,
                            title   = stringResource(R.string.backup_export),
                            hint    = stringResource(R.string.backup_export_hint),
                            onClick = {
                                haptic.tick()
                                val filename = "nextime_backup_${LocalDate.now()}.json"
                                exportLauncher.launch(filename)
                            }
                        )

                        SettingsDivider()

                        BackupActionRow(
                            icon    = Icons.Default.FileUpload,
                            title   = stringResource(R.string.backup_import),
                            hint    = stringResource(R.string.backup_import_hint),
                            onClick = {
                                haptic.tick()
                                importLauncher.launch(arrayOf("application/json"))
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── Sheet-Typen ──────────────────────────────────────────────────────────────

private enum class SettingsSheet {
    DARSTELLUNG, FORMAT, AKZENTFARBE, HELL_DUNKEL, SPRACHE, STANDARDS, BACKUP
}

// ─── Gruppen-Überschrift + Gruppen-Container ───────────────────────────────────

@Composable
private fun SettingsGroupHeader(title: String) {
    Text(
        text       = title.uppercase(),
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.primary,
        modifier   = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    ) {
        Column(content = content)
    }
}

// ─── ListItem-Komponente ──────────────────────────────────────────────────────

@Composable
private fun SettingsListItem(
    title         : String,
    value         : String,
    trailingColor : Color? = null,
    onClick       : () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text  = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        trailingContent = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (value.isNotEmpty()) {
                    Text(
                        text  = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (trailingColor != null) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(trailingColor)
                    )
                }
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier  = Modifier.padding(horizontal = 16.dp),
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

// ─── Backup-Aktionszeile ───────────────────────────────────────────────────────

@Composable
private fun BackupActionRow(
    icon    : ImageVector,
    title   : String,
    hint    : String,
    onClick : () -> Unit
) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Surface(
            shape    = CircleShape,
            color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector        = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(18.dp)
        )
    }
}