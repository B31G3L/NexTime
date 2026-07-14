package com.beigel.nextime.ui.screens.settings

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
import kotlinx.coroutines.launch
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
import com.beigel.nextime.utils.HapticFeedback
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ─── Settings Screen ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val haptic  = remember { _root_ide_package_.com.beigel.nextime.utils.HapticFeedback(context) }

    // ── Lokalisierter Preview-Titel ───────────────────────────────────────────
    val previewTitle = stringResource(R.string.preview_countdown_title)
    val previewCountdown = remember(previewTitle) {
        _root_ide_package_.com.beigel.nextime.data.model.Countdown(
            id = -1L,
            title = previewTitle,
            icon = "FlightTakeoff",
            targetDateTime = LocalDateTime.now().plusDays(42),
            displayFormat = "",
            color = "#FF7043",
            recurrence = _root_ide_package_.com.beigel.nextime.data.model.RecurrenceType.NONE.name
        )
    }

    // ── Preferences ──────────────────────────────────────────────────────────
    val themeMode        by _root_ide_package_.com.beigel.nextime.ui.theme.ThemePreferences.getThemeMode(context).collectAsState(initial = _root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.SYSTEM)
    val accentColor      by _root_ide_package_.com.beigel.nextime.ui.theme.AccentColorPreferences.getAccentColor(context).collectAsState(initial = _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.ORANGE)
    val defaultColor     by _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.getDefaultColor(context).collectAsState(initial = "#FF7043")
    val defaultTime      by _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.getDefaultTime(context).collectAsState(initial = LocalTime.of(12, 0))
    val defaultDateUnits by _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.getDefaultDateUnits(context).collectAsState(initial = setOf(
        _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.DAYS))
    val showTimeOnCard   by _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.getShowTimeOnCard(context).collectAsState(initial = false)
    val currentLang      by _root_ide_package_.com.beigel.nextime.ui.theme.LanguageManager.getLanguage(context).collectAsState(initial = _root_ide_package_.com.beigel.nextime.ui.theme.AppLanguage.SYSTEM)
    val displayStyle     by _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.getDisplayStyle(context).collectAsState(initial = _root_ide_package_.com.beigel.nextime.ui.theme.DisplayStyle.STANDARD)

    // ── Sheet-States ──────────────────────────────────────────────────────────
    var activeSheet by remember { mutableStateOf<SettingsSheet?>(null) }

    // ── Zusammenfassungstexte ─────────────────────────────────────────────────
    val darstellungSummary = when (displayStyle) {
        _root_ide_package_.com.beigel.nextime.ui.theme.DisplayStyle.STANDARD   -> stringResource(R.string.card_style_standard)
        _root_ide_package_.com.beigel.nextime.ui.theme.DisplayStyle.KOMPAKT    -> stringResource(R.string.card_style_kompakt)
        _root_ide_package_.com.beigel.nextime.ui.theme.DisplayStyle.BANNER     -> stringResource(R.string.card_style_banner)
        _root_ide_package_.com.beigel.nextime.ui.theme.DisplayStyle.INVERTIERT -> stringResource(R.string.card_style_invertiert)
    }
    val accentSummary = when (accentColor) {
        _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.ORANGE  -> stringResource(R.string.accent_orange)
        _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.SAGE    -> stringResource(R.string.accent_sage)
        _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.VIOLET  -> stringResource(R.string.accent_violet)
        _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.CRIMSON -> stringResource(R.string.accent_crimson)
        _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.TEAL    -> stringResource(R.string.accent_teal)
        _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.GOLD    -> stringResource(R.string.accent_gold)
        _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.SLATE   -> stringResource(R.string.accent_slate)
    }
    val themeSummary = when (themeMode) {
        _root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.SYSTEM -> stringResource(R.string.design_system)
        _root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.LIGHT  -> stringResource(R.string.design_light)
        _root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.DARK   -> stringResource(R.string.design_dark)
    }
    val dateUnitLabels = mapOf(
        _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.YEARS  to stringResource(R.string.format_unit_years),
        _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.MONTHS to stringResource(R.string.format_unit_months),
        _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.WEEKS  to stringResource(R.string.format_unit_weeks),
        _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.DAYS   to stringResource(R.string.format_unit_days),
    )
    val formatSummary = buildString {
        val sorted = listOf(_root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.YEARS, _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.MONTHS, _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.WEEKS, _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.DAYS)
            .filter { it in defaultDateUnits }
            .mapNotNull { dateUnitLabels[it] }
        append(sorted.joinToString(" + "))
        if (showTimeOnCard) append(" + ${stringResource(R.string.format_unit_hours)}")
    }
    val standardsSummary = stringResource(R.string.time_oclock, defaultTime.format(DateTimeFormatter.ofPattern("HH:mm")))

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
        ) {
            // ── Darstellung ───────────────────────────────────────────────────
            SettingsListItem(
                title    = stringResource(R.string.settings_display_style),
                value    = darstellungSummary,
                onClick  = { haptic.tick(); activeSheet = SettingsSheet.DARSTELLUNG }
            )
            SettingsDivider()

            // ── Anzeigeformat ─────────────────────────────────────────────────
            SettingsListItem(
                title   = stringResource(R.string.settings_format_label),
                value   = formatSummary,
                onClick = { haptic.tick(); activeSheet = SettingsSheet.FORMAT }
            )
            SettingsDivider()

            // ── Akzentfarbe ───────────────────────────────────────────────────
            SettingsListItem(
                title        = stringResource(R.string.settings_color_scheme),
                value        = accentSummary,
                trailingColor = accentColor.light,
                onClick      = { haptic.tick(); activeSheet = SettingsSheet.AKZENTFARBE }
            )
            SettingsDivider()

            // ── Hell / Dunkel ─────────────────────────────────────────────────
            SettingsListItem(
                title   = stringResource(R.string.settings_light_dark),
                value   = themeSummary,
                onClick = { haptic.tick(); activeSheet = SettingsSheet.HELL_DUNKEL }
            )
            SettingsDivider()

            // ── Sprache ───────────────────────────────────────────────────────
            SettingsListItem(
                title   = stringResource(R.string.settings_language),
                value   = currentLang.displayName,
                onClick = { haptic.tick(); activeSheet = SettingsSheet.SPRACHE }
            )
            SettingsDivider()

            // ── Standards ─────────────────────────────────────────────────────
            SettingsListItem(
                title   = stringResource(R.string.settings_defaults),
                value   = standardsSummary,
                onClick = { haptic.tick(); activeSheet = SettingsSheet.STANDARDS }
            )

            Spacer(Modifier.height(32.dp))
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
                            _root_ide_package_.com.beigel.nextime.ui.theme.DisplayStyle.STANDARD   to stringResource(R.string.card_style_standard),
                            _root_ide_package_.com.beigel.nextime.ui.theme.DisplayStyle.KOMPAKT    to stringResource(R.string.card_style_kompakt),
                            _root_ide_package_.com.beigel.nextime.ui.theme.DisplayStyle.BANNER     to stringResource(R.string.card_style_banner),
                            _root_ide_package_.com.beigel.nextime.ui.theme.DisplayStyle.INVERTIERT to stringResource(R.string.card_style_invertiert),
                        ).forEach { (style, label) ->
                            val isSelected = displayStyle == style
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp),
                                color    = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f) else MaterialTheme.colorScheme.surface,
                                border   = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                onClick  = { haptic.success(); scope.launch { _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.setDisplayStyle(context, style) } }
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
                                        _root_ide_package_.com.beigel.nextime.ui.components.CountdownCard(
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
                            _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.YEARS  to stringResource(R.string.format_unit_years),
                            _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.MONTHS to stringResource(R.string.format_unit_months),
                            _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.WEEKS  to stringResource(R.string.format_unit_weeks),
                            _root_ide_package_.com.beigel.nextime.data.model.DisplayUnit.DAYS   to stringResource(R.string.format_unit_days),
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
                                        scope.launch { _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.setDefaultDateUnits(context, newUnits) }
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
                            Switch(checked = showTimeOnCard, onCheckedChange = { checked -> haptic.tick(); scope.launch { _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.setShowTimeOnCard(context, checked) } })
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(stringResource(R.string.preview_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        key(defaultDateUnits, showTimeOnCard) {
                            _root_ide_package_.com.beigel.nextime.ui.components.CountdownCard(
                                countdown = previewCountdown
                            )
                        }
                    }

                    // ── Akzentfarbe ───────────────────────────────────────────
                    SettingsSheet.AKZENTFARBE -> {
                        Text(stringResource(R.string.settings_accent_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val rows = _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.values().toList().chunked(4)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            rows.forEach { row ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { accent ->
                                        val isSelected = accentColor == accent
                                        val accentName = when (accent) {
                                            _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.ORANGE  -> stringResource(R.string.accent_orange)
                                            _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.SAGE    -> stringResource(R.string.accent_sage)
                                            _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.VIOLET  -> stringResource(R.string.accent_violet)
                                            _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.CRIMSON -> stringResource(R.string.accent_crimson)
                                            _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.TEAL    -> stringResource(R.string.accent_teal)
                                            _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.GOLD    -> stringResource(R.string.accent_gold)
                                            _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.SLATE   -> stringResource(R.string.accent_slate)
                                        }
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape    = RoundedCornerShape(12.dp),
                                            color    = if (isSelected) accent.light.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface,
                                            border   = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent.light else Color.Transparent.copy(alpha = 0.15f)),
                                            onClick  = { haptic.success(); scope.launch { _root_ide_package_.com.beigel.nextime.ui.theme.AccentColorPreferences.setAccentColor(context, accent) } }
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
                            Triple(_root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.SYSTEM, stringResource(R.string.design_system), Icons.Default.BrightnessAuto),
                            Triple(_root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.LIGHT,  stringResource(R.string.design_light),  Icons.Default.Brightness7),
                            Triple(_root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.DARK,   stringResource(R.string.design_dark),   Icons.Default.Brightness4),
                        ).forEach { (mode, label, icon) ->
                            val isSelected = themeMode == mode
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = MaterialTheme.shapes.medium,
                                color    = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
                                border   = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                onClick  = { haptic.tick(); scope.launch { _root_ide_package_.com.beigel.nextime.ui.theme.ThemePreferences.setThemeMode(context, mode) } }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(icon, null, modifier = Modifier.size(20.dp), tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                    }
                                    RadioButton(selected = isSelected, onClick = { haptic.tick(); scope.launch { _root_ide_package_.com.beigel.nextime.ui.theme.ThemePreferences.setThemeMode(context, mode) } })
                                }
                            }
                        }
                    }

                    // ── Sprache ───────────────────────────────────────────────
                    SettingsSheet.SPRACHE -> {
                        _root_ide_package_.com.beigel.nextime.ui.theme.AppLanguage.values().forEach { language ->
                            val isSelected = currentLang == language
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = MaterialTheme.shapes.medium,
                                color    = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
                                border   = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                onClick  = {
                                    haptic.tick()
                                    _root_ide_package_.com.beigel.nextime.ui.theme.LanguageManager.persistLanguageSync(context, language)
                                    scope.launch { _root_ide_package_.com.beigel.nextime.ui.theme.LanguageManager.setLanguage(context, language) }
                                    val localeList = if (language == _root_ide_package_.com.beigel.nextime.ui.theme.AppLanguage.SYSTEM || language.tag.isEmpty())
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
                                        Surface(modifier = Modifier.size(28.dp), shape = CircleShape, color = c, onClick = { scope.launch { _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.setDefaultColor(context, hex) }; showColorPicker = false }) {
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
                                    onClick  = { scope.launch { _root_ide_package_.com.beigel.nextime.ui.theme.AppPreferences.setDefaultTime(context, LocalTime.of(timePickerState.hour, timePickerState.minute)) }; showTimePicker = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text(stringResource(R.string.ok)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Sheet-Typen ──────────────────────────────────────────────────────────────

private enum class SettingsSheet {
    DARSTELLUNG, FORMAT, AKZENTFARBE, HELL_DUNKEL, SPRACHE, STANDARDS
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
                Text(
                    text  = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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