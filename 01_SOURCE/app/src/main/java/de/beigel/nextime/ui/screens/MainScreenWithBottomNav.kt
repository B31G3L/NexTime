package de.beigel.nextime.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.beigel.nextime.BuildConfig
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.FilterMode
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.components.AddEditCountdownScreen
import de.beigel.nextime.ui.components.CountdownCard
import de.beigel.nextime.ui.components.CountdownCardDialog
import de.beigel.nextime.ui.components.DeveloperCard
import de.beigel.nextime.ui.components.EmptyStateView
import de.beigel.nextime.ui.components.ExpandableFab
import de.beigel.nextime.ui.components.FeaturesCard
import de.beigel.nextime.ui.components.ThemeSettingsDialog
import de.beigel.nextime.ui.components.openKofi
import de.beigel.nextime.ui.components.openPlayStore
import de.beigel.nextime.ui.components.reportBug
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
                                onCountdownDelete = { countdown -> viewModel.deleteCountdown(countdown) }
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
    onCountdownDelete: (Countdown) -> Unit
) {
    if (countdowns.isEmpty()) {
        EmptyStateView()
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

// ─── About-Seite ──────────────────────────────────────────────────────────────

@Composable
private fun AboutPageContent() {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("NexTime", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text("v${BuildConfig.VERSION_NAME}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        DeveloperCard()
        FeaturesCard()
        SupportActionsCard()
    }
}

@Composable
private fun SupportActionsCard() {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { haptic.click(); openPlayStore(context) }, modifier = Modifier.fillMaxWidth()) { Text("⭐ App bewerten") }
            OutlinedButton(onClick = { haptic.click(); openKofi(context) }, modifier = Modifier.fillMaxWidth()) { Text("☕ Buy me a Coffee") }
            OutlinedButton(onClick = { haptic.click(); reportBug(context) }, modifier = Modifier.fillMaxWidth()) { Text("🐛 Fehler melden") }
        }
    }
}

// ─── Einstellungen ────────────────────────────────────────────────────────────

@Composable
private fun SettingsPageContent(onThemeDialogOpen: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeMode by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
    val haptic = remember { HapticFeedback(context) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Design & Theme", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = onThemeDialogOpen, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Outlined.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("🎨 Theme auswählen")
        }
        Spacer(Modifier.height(8.dp))
        Text("Helles/Dunkles Design", style = MaterialTheme.typography.titleMedium)
        ThemeModeOption("⚙️ Systemeinstellung", themeMode == ThemeMode.SYSTEM) { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.SYSTEM) } }
        ThemeModeOption("🌞 Hell", themeMode == ThemeMode.LIGHT) { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.LIGHT) } }
        ThemeModeOption("🌙 Dunkel", themeMode == ThemeMode.DARK) { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.DARK) } }
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