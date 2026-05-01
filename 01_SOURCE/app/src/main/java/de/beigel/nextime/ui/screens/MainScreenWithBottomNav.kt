package de.beigel.nextime.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.FilterMode
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.components.AddEditCountdownScreen
import de.beigel.nextime.ui.components.CountdownCard
import de.beigel.nextime.ui.components.CountdownCardDialog
import de.beigel.nextime.ui.components.AboutPageContent
import de.beigel.nextime.ui.components.EmptyStateView
import de.beigel.nextime.ui.components.ExpandableFab
import de.beigel.nextime.ui.components.ThemeSettingsDialog
import de.beigel.nextime.ui.theme.CustomTheme
import de.beigel.nextime.ui.theme.CustomThemePreferences
import de.beigel.nextime.ui.theme.ThemeMode
import de.beigel.nextime.ui.theme.ThemePreferences
import de.beigel.nextime.ui.theme.getThemeConfig
import de.beigel.nextime.ui.viewmodel.CountdownViewModel
import de.beigel.nextime.ui.viewmodel.SortMode
import de.beigel.nextime.utils.HapticFeedback
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreenWithBottomNav(
    viewModel: CountdownViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    val scope = rememberCoroutineScope()

    val countdowns by viewModel.countdowns.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddEditScreen by remember { mutableStateOf(false) }
    var editingCountdown by remember { mutableStateOf<Countdown?>(null) }
    var dialogCountdown by remember { mutableStateOf<Countdown?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var selectedCustomTheme by remember { mutableStateOf(CustomTheme.NEXTIME) }

    LaunchedEffect(Unit) {
        CustomThemePreferences.getCustomTheme(context).collect { theme ->
            selectedCustomTheme = theme
        }
    }

    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    val currentScreen = when {
        showAddEditScreen || editingCountdown != null -> "add_edit"
        else -> "main"
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState != "main") {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                        fadeIn(animationSpec = tween(400)) togetherWith
                        slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                        fadeOut(animationSpec = tween(300))
            } else {
                slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                        fadeIn(animationSpec = tween(400)) togetherWith
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400, easing = FastOutSlowInEasing)) +
                        fadeOut(animationSpec = tween(300))
            }
        },
        label = "screen_navigation"
    ) { screen ->
        when (screen) {
            "add_edit" -> {
                AddEditCountdownScreen(
                    countdown = editingCountdown,
                    onSave = { countdown: Countdown ->
                        if (editingCountdown != null) viewModel.updateCountdown(countdown)
                        else viewModel.addCountdown(countdown)
                        showAddEditScreen = false
                        editingCountdown = null
                    },
                    onBack = {
                        showAddEditScreen = false
                        editingCountdown = null
                    }
                )
            }

            else -> {
                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    // Suchfeld oder Titel
                                    if (showSearch && pagerState.currentPage == 1) {
                                        SearchField(
                                            query = searchQuery,
                                            onQueryChange = { viewModel.setSearchQuery(it) },
                                            onClose = {
                                                showSearch = false
                                                viewModel.clearSearch()
                                            }
                                        )
                                    } else {
                                        Text(
                                            when (pagerState.currentPage) {
                                                0 -> "Info & Support"
                                                1 -> "NexTime"
                                                2 -> "Einstellungen"
                                                else -> "NexTime"
                                            }
                                        )
                                    }
                                },
                                actions = {
                                    if (pagerState.currentPage == 1 && !showSearch) {
                                        // Suche
                                        IconButton(onClick = { haptic.tick(); showSearch = true }) {
                                            Icon(Icons.Default.Search, contentDescription = "Suchen")
                                        }
                                        // Sortierung
                                        Box {
                                            IconButton(onClick = { haptic.tick(); showSortMenu = true }) {
                                                Icon(Icons.Default.Sort, contentDescription = "Sortieren")
                                            }
                                            SortDropdownMenu(
                                                expanded = showSortMenu,
                                                currentSort = sortMode,
                                                onSortSelected = { mode ->
                                                    haptic.tick()
                                                    viewModel.setSortMode(mode)
                                                    showSortMenu = false
                                                },
                                                onDismiss = { showSortMenu = false }
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                            )
                            AnimatedVisibility(visible = pagerState.currentPage == 1 && !showSearch) {
                                FilterChipRow(
                                    currentMode = filterMode,
                                    onModeSelected = { mode -> haptic.tick(); viewModel.setFilterMode(mode) }
                                )
                            }
                        }
                    },
                    bottomBar = {
                        BottomNavigationBar(
                            selectedPage = pagerState.currentPage,
                            onPageSelected = { page ->
                                haptic.tick()
                                scope.launch { pagerState.animateScrollToPage(page) }
                            }
                        )
                    },
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = pagerState.currentPage == 1 && !showSearch,
                            enter = scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(),
                            exit = scaleOut(animationSpec = tween(200)) + fadeOut()
                        ) {
                            ExpandableFab(
                                onCreateCustom = { showAddEditScreen = true },
                                onTemplateSelected = { countdown ->
                                    editingCountdown = countdown.copy(id = 0)
                                    showAddEditScreen = true
                                }
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { paddingValues ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().padding(paddingValues)
                    ) { page ->
                        when (page) {
                            0 -> AboutPageContent()
                            1 -> MainListContent(
                                countdowns = countdowns,
                                filterMode = filterMode,
                                searchQuery = searchQuery,
                                onCountdownClick = { countdown -> haptic.tick(); dialogCountdown = countdown },
                                onCountdownEdit = { countdown -> editingCountdown = countdown },
                                onCountdownDelete = { countdown -> viewModel.deleteCountdown(countdown) },
                                onAddCountdown = { showAddEditScreen = true }
                            )
                            2 -> SettingsPageContent(onThemeDialogOpen = { haptic.tick(); showThemeDialog = true })
                        }
                    }
                }
            }
        }
    }

    dialogCountdown?.let { countdown ->
        CountdownCardDialog(
            countdown = countdown,
            onDismiss = { dialogCountdown = null },
            onEdit = { c -> editingCountdown = c; dialogCountdown = null },
            onDelete = { c -> viewModel.deleteCountdown(c); dialogCountdown = null },
            onShare = { c -> shareCountdown(context, c) }
        )
    }

    if (showThemeDialog) {
        ThemeSettingsDialog(
            onDismiss = { showThemeDialog = false },
            currentTheme = selectedCustomTheme,
            onThemeChanged = { newTheme: CustomTheme ->
                haptic.success()
                selectedCustomTheme = newTheme
                scope.launch { CustomThemePreferences.setCustomTheme(context, newTheme) }
                Toast.makeText(context, "Theme geändert zu ${getThemeConfig(newTheme).name}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

// ─── Suchfeld ─────────────────────────────────────────────────────────────────

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Countdown suchen...") },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { focusManager.clearFocus(); onClose() }) {
                Icon(Icons.Default.Close, contentDescription = "Suche schließen")
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

// ─── Sortier-Dropdown ─────────────────────────────────────────────────────────

@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    currentSort: SortMode,
    onSortSelected: (SortMode) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        listOf(
            SortMode.DATE_ASC   to "📅 Datum (nächster zuerst)",
            SortMode.DATE_DESC  to "📅 Datum (spätester zuerst)",
            SortMode.TITLE_ASC  to "🔤 Titel A → Z",
            SortMode.TITLE_DESC to "🔤 Titel Z → A",
            SortMode.CREATED    to "🕐 Zuletzt erstellt"
        ).forEach { (mode, label) ->
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label)
                        if (currentSort == mode) {
                            Spacer(Modifier.weight(1f))
                            Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                onClick = { onSortSelected(mode) }
            )
        }
    }
}

// ─── Filter-Chips ─────────────────────────────────────────────────────────────

@Composable
private fun FilterChipRow(currentMode: FilterMode, onModeSelected: (FilterMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = currentMode == FilterMode.ALL, onClick = { onModeSelected(FilterMode.ALL) }, label = { Text("Alle") })
        FilterChip(selected = currentMode == FilterMode.COUNTDOWN, onClick = { onModeSelected(FilterMode.COUNTDOWN) }, label = { Text("Countdown") })
        FilterChip(selected = currentMode == FilterMode.COUNTUP, onClick = { onModeSelected(FilterMode.COUNTUP) }, label = { Text("Count-up") })
    }
}

// ─── Navigation ───────────────────────────────────────────────────────────────

@Composable
private fun BottomNavigationBar(selectedPage: Int, onPageSelected: (Int) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.height(70.dp)) {
        NavigationBarItem(selected = selectedPage == 0, onClick = { onPageSelected(0) }, icon = { Icon(Icons.Outlined.Info, contentDescription = "Info", modifier = Modifier.size(26.dp)) }, label = null, alwaysShowLabel = false)
        NavigationBarItem(selected = selectedPage == 1, onClick = { onPageSelected(1) }, icon = { Icon(Icons.Outlined.AvTimer, contentDescription = "Liste", modifier = Modifier.size(26.dp)) }, label = null, alwaysShowLabel = false)
        NavigationBarItem(selected = selectedPage == 2, onClick = { onPageSelected(2) }, icon = { Icon(Icons.Outlined.Settings, contentDescription = "Einstellungen", modifier = Modifier.size(26.dp)) }, label = null, alwaysShowLabel = false)
    }
}

// ─── Hauptliste ───────────────────────────────────────────────────────────────

@Composable
private fun MainListContent(
    countdowns: List<Countdown>,
    filterMode: FilterMode,
    searchQuery: String,
    onCountdownClick: (Countdown) -> Unit,
    onCountdownEdit: (Countdown) -> Unit,
    onCountdownDelete: (Countdown) -> Unit,
    onAddCountdown: () -> Unit = {}
) {
    if (countdowns.isEmpty()) {
        EmptyStateView(onAddCountdown = onAddCountdown)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bei aktiver Suche: keine Sektions-Trenner
        if (searchQuery.isNotBlank() || filterMode != FilterMode.ALL) {
            items(items = countdowns, key = { it.id }) { countdown ->
                AnimatedCountdownCard(countdown, onCountdownClick)
            }
        } else {
            val futureItems = countdowns.filter { !it.isCountUp }
            val pastItems = countdowns.filter { it.isCountUp }

            if (futureItems.isNotEmpty()) {
                item(key = "header_countdown") {
                    SectionHeader(label = "Countdown", count = futureItems.size)
                }
                items(items = futureItems, key = { it.id }) { countdown ->
                    AnimatedCountdownCard(countdown, onCountdownClick)
                }
            }
            if (pastItems.isNotEmpty()) {
                item(key = "header_countup") {
                    SectionHeader(label = "Count-up", count = pastItems.size)
                }
                items(items = pastItems, key = { it.id }) { countdown ->
                    AnimatedCountdownCard(countdown, onCountdownClick)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
            Text(text = "$count", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun AnimatedCountdownCard(countdown: Countdown, onCountdownClick: (Countdown) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(countdown.id) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200))
    ) {
        Box(modifier = Modifier.fillMaxWidth().clickable { onCountdownClick(countdown) }) {
            CountdownCard(countdown = countdown)
        }
    }
}

// ─── About-Seite → kommt aus AboutDialog.kt ───────────────────────────────────

// ─── Einstellungen ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsPageContent(onThemeDialogOpen: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = remember { HapticFeedback(context) }
    val scrollState = rememberScrollState()

    // Theme
    val themeMode by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)

    // Standard-Einstellungen
    val defaultFormat by de.beigel.nextime.ui.theme.AppPreferences
        .getDefaultFormat(context).collectAsState(initial = de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY)
    val defaultColor by de.beigel.nextime.ui.theme.AppPreferences
        .getDefaultColor(context).collectAsState(initial = "#FF7043")
    val defaultTime by de.beigel.nextime.ui.theme.AppPreferences
        .getDefaultTime(context).collectAsState(initial = java.time.LocalTime.of(12, 0))

    var showTimePicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showFormatMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Design ────────────────────────────────────────────────────────────
        Text("Design & Theme", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = onThemeDialogOpen, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("🎨 Theme auswählen")
        }
        Spacer(Modifier.height(4.dp))
        Text("Helles/Dunkles Design", style = MaterialTheme.typography.titleMedium)
        ThemeModeOption("⚙️ Systemeinstellung", themeMode == ThemeMode.SYSTEM) { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.SYSTEM) } }
        ThemeModeOption("🌞 Hell", themeMode == ThemeMode.LIGHT) { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.LIGHT) } }
        ThemeModeOption("🌙 Dunkel", themeMode == ThemeMode.DARK) { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.DARK) } }

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)

        // ── Standard-Einstellungen ────────────────────────────────────────────
        Text("Standard für neue Einträge", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            text = "Diese Werte werden beim Erstellen neuer Einträge vorausgefüllt.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Standard-Anzeigeformat
        SettingsRow(
            label = "Anzeigeformat",
            value = when (defaultFormat) {
                de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY         -> "Nur Tage"
                de.beigel.nextime.data.model.CountdownDisplayFormat.WEEKS_DAYS        -> "Wochen + Tage"
                de.beigel.nextime.data.model.CountdownDisplayFormat.MONTHS_DAYS       -> "Monate + Tage"
                de.beigel.nextime.data.model.CountdownDisplayFormat.YEARS_MONTHS_DAYS -> "Jahre + Monate + Tage"
            },
            onClick = { haptic.tick(); showFormatMenu = true }
        )

        // Standard-Farbe
        SettingsRowColor(
            label = "Farbe",
            color = defaultColor,
            onClick = { haptic.tick(); showColorPicker = true }
        )

        // Standard-Uhrzeit
        SettingsRow(
            label = "Uhrzeit (wenn aktiv)",
            value = defaultTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + " Uhr",
            onClick = { haptic.tick(); showTimePicker = true }
        )
    }

    // Format-Picker
    if (showFormatMenu) {
        AlertDialog(
            onDismissRequest = { showFormatMenu = false },
            title = { Text("Standard-Anzeigeformat") },
            text = {
                Column {
                    listOf(
                        de.beigel.nextime.data.model.CountdownDisplayFormat.DAYS_ONLY to "Nur Tage",
                        de.beigel.nextime.data.model.CountdownDisplayFormat.WEEKS_DAYS to "Wochen + Tage",
                        de.beigel.nextime.data.model.CountdownDisplayFormat.MONTHS_DAYS to "Monate + Tage",
                        de.beigel.nextime.data.model.CountdownDisplayFormat.YEARS_MONTHS_DAYS to "Jahre + Monate + Tage"
                    ).forEach { (format, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.tick()
                                    scope.launch { de.beigel.nextime.ui.theme.AppPreferences.setDefaultFormat(context, format) }
                                    showFormatMenu = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                            if (defaultFormat == format) {
                                Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showFormatMenu = false }) { Text("Schließen") } }
        )
    }

    // Farb-Picker
    if (showColorPicker) {
        val initialColorInt = try { android.graphics.Color.parseColor(defaultColor) }
        catch (e: Exception) { android.graphics.Color.parseColor("#FF7043") }
        var r by remember { mutableStateOf(android.graphics.Color.red(initialColorInt)) }
        var g by remember { mutableStateOf(android.graphics.Color.green(initialColorInt)) }
        var b by remember { mutableStateOf(android.graphics.Color.blue(initialColorInt)) }

        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Standard-Farbe") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Schnellauswahl
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("#FF7043","#EF5350","#EC407A","#AB47BC","#5C6BC0","#42A5F5","#26A69A","#66BB6A","#FFA726","#8D6E63").forEach { hex ->
                            val c = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex)) }
                            catch (e: Exception) { MaterialTheme.colorScheme.primary }
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = c,
                                onClick = {
                                    scope.launch { de.beigel.nextime.ui.theme.AppPreferences.setDefaultColor(context, hex) }
                                    showColorPicker = false
                                }
                            ) {
                                if (defaultColor == hex) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text("✓", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                    // RGB-Slider
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color(android.graphics.Color.rgb(r, g, b))))
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
                    val hex = String.format("#%02X%02X%02X", r, g, b)
                    scope.launch { de.beigel.nextime.ui.theme.AppPreferences.setDefaultColor(context, hex) }
                    showColorPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showColorPicker = false }) { Text("Abbrechen") } }
        )
    }

    // Zeit-Picker
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = defaultTime.hour,
            initialMinute = defaultTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Standard-Uhrzeit") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    haptic.click()
                    scope.launch {
                        de.beigel.nextime.ui.theme.AppPreferences.setDefaultTime(
                            context,
                            java.time.LocalTime.of(timePickerState.hour, timePickerState.minute)
                        )
                    }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Abbrechen") } }
        )
    }
}

@Composable
private fun ThemeModeOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label)
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SettingsRowColor(label: String, color: String, onClick: () -> Unit) {
    val parsedColor = try { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(color)) }
    catch (e: Exception) { MaterialTheme.colorScheme.primary }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(parsedColor)
            )
        }
    }
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun shareCountdown(context: android.content.Context, countdown: Countdown) {
    val timeInfo = countdown.calculateTimeRemaining()
    val shareText = buildString {
        append("⏰ ${countdown.title}\n\n")
        if (timeInfo.isPast) append("Ist vor ${timeInfo.days} Tagen vergangen")
        else append("Noch ${timeInfo.days} Tage")
        append("\n\n📅 ${countdown.effectiveTarget.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
        append("\n\nErstellt mit NexTime")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Countdown teilen"))
}