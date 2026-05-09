package todo.beigelwick.de.todolist.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.CountdownDisplayFormat
import todo.beigelwick.de.todolist.data.model.FilterMode
import todo.beigelwick.de.todolist.data.model.calculateTimeRemaining
import todo.beigelwick.de.todolist.ui.components.CountdownCard
import todo.beigelwick.de.todolist.ui.theme.AppLanguage
import todo.beigelwick.de.todolist.ui.theme.AppPreferences
import todo.beigelwick.de.todolist.ui.theme.CustomTheme
import todo.beigelwick.de.todolist.ui.theme.CustomThemePreferences
import todo.beigelwick.de.todolist.ui.theme.LanguageManager
import todo.beigelwick.de.todolist.ui.theme.ThemeMode
import todo.beigelwick.de.todolist.ui.theme.ThemePreferences
import todo.beigelwick.de.todolist.ui.viewmodel.CountdownViewModel
import todo.beigelwick.de.todolist.ui.viewmodel.SortMode
import todo.beigelwick.de.todolist.ui.components.AboutPageContent
import todo.beigelwick.de.todolist.ui.components.AddEditCountdownScreen
import todo.beigelwick.de.todolist.ui.components.CountdownCardDialog
import todo.beigelwick.de.todolist.ui.components.EmptyStateView
import todo.beigelwick.de.todolist.ui.components.ExpandableFab
import todo.beigelwick.de.todolist.ui.theme.getThemeConfig
import todo.beigelwick.de.todolist.utils.HapticFeedback
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.absoluteValue
import todo.beigelwick.de.todolist.R


// ─── Nav-Item Datenklasse ─────────────────────────────────────────────────────

private data class NavItem(
    val icon: ImageVector,
    val label: String,
    val page: Int
)

private val NAV_ITEMS = listOf(
    NavItem(Icons.Outlined.Info, "nav_label_info", 0),
    NavItem(Icons.Outlined.AvTimer, "nav_label_timer", 1),
    NavItem(Icons.Outlined.Settings, "nav_label_settings", 2)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreenWithBottomNav(
    viewModel: CountdownViewModel = viewModel()
) {
    val context = LocalContext.current
    val haptic = remember {
        HapticFeedback(
            context
        )
    }
    val scope = rememberCoroutineScope()

    val countdowns by viewModel.countdowns.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddEditScreen by remember { mutableStateOf(false) }
    var editingCountdown by remember { mutableStateOf<Countdown?>(null) }
    var dialogCountdown by remember { mutableStateOf<Countdown?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var selectedCustomTheme by remember { mutableStateOf(CustomTheme.NEXTIME) }

    LaunchedEffect(Unit) {
        CustomThemePreferences.getCustomTheme(context).collect { theme ->
            selectedCustomTheme = theme
        }
    }

    // Pager startet auf Seite 1 (Timer-Liste)
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    val currentScreen = when {
        showAddEditScreen || editingCountdown != null -> "add_edit"
        else -> "main"
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState != "main") {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(380)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(280))
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(380, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(380)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(380, easing = FastOutSlowInEasing)
                        ) + fadeOut(tween(280))
            }
        },
        label = "screen_navigation"
    ) { screen ->
        when (screen) {
            "add_edit" -> {
                AddEditCountdownScreen(
                    countdown = editingCountdown,
                    onSave = { countdown: Countdown ->
                        // id == 0L → neuer Eintrag (auch Vorlagen), sonst Update
                        if (countdown.id != 0L) viewModel.updateCountdown(countdown)
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
                                                0 -> stringResource(R.string.topbar_info)
                                                1 -> stringResource(R.string.topbar_nextime)
                                                2 -> "Einstellungen"
                                                else -> stringResource(R.string.topbar_nextime)
                                            }
                                        )
                                    }
                                },
                                actions = {
                                    if (pagerState.currentPage == 1 && !showSearch) {
                                        IconButton(onClick = { haptic.tick(); showSearch = true }) {
                                            Icon(Icons.Default.Search, contentDescription = "Suchen")
                                        }
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
                        // ── Schöne BottomNav ──────────────────────────────────
                        BeautifulBottomNav(
                            pagerState = pagerState,
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
                    // ── HorizontalPager mit Parallax-Effekt ───────────────────
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) { page ->
                        // Leichte Parallax-Transformation je nach Scroll-Offset
                        val pageOffset = (
                                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                ).absoluteValue

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    alpha = 1f - (pageOffset * 0.25f).coerceIn(0f, 0.25f)
                                    translationX = pageOffset * 40f * (
                                            if (page < pagerState.currentPage) -1f else 1f
                                            )
                                }
                        ) {
                            when (page) {
                                0 -> AboutPageContent()
                                1 -> MainListContent(
                                    countdowns = countdowns,
                                    filterMode = filterMode,
                                    searchQuery = searchQuery,
                                    onCountdownClick = { countdown -> haptic.tick(); dialogCountdown = countdown },
                                    onAddCountdown = { showAddEditScreen = true }
                                )
                                2 -> SettingsPageContent(
                                    onThemeChanged = { newTheme ->
                                        selectedCustomTheme = newTheme
                                        scope.launch {
                                            CustomThemePreferences.setCustomTheme(context, newTheme)
                                        }
                                        haptic.success()
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.theme_changed, getThemeConfig(
                                                newTheme
                                            ).name),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    currentTheme = selectedCustomTheme,
                                    isVisible = pagerState.currentPage == 2
                                )
                            }
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
}

// ─── Schöne BottomNav ─────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BeautifulBottomNav(
    pagerState: PagerState,
    onPageSelected: (Int) -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NAV_ITEMS.forEach { item ->
                val isSelected = pagerState.currentPage == item.page

                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.88f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "nav_scale_${item.page}"
                )

                val pillWidth by animateDpAsState(
                    targetValue = if (isSelected) 64.dp else 0.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label = "pill_${item.page}"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.large)
                        .clickable { onPageSelected(item.page) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
                    ) {
                        // Pill-Indicator + Icon
                        Box(contentAlignment = Alignment.Center) {
                            // Animierte Pill im Hintergrund
                            Box(
                                modifier = Modifier
                                    .width(pillWidth)
                                    .height(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        MaterialTheme.colorScheme.secondaryContainer
                                    )
                            )
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        // Label
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(tween(150)) + expandVertically(tween(150)),
                            exit = fadeOut(tween(100)) + shrinkVertically(tween(100))
                        ) {
                            Text(
                                text = when(item.label) {
                                    "nav_label_info" -> stringResource(R.string.nav_label_info)
                                    "nav_label_timer" -> stringResource(R.string.nav_label_timer)
                                    else -> stringResource(R.string.nav_label_settings)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
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
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
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
            SortMode.DATE_ASC   to Pair(Icons.Outlined.CalendarToday, stringResource(R.string.sort_date_asc)),
            SortMode.DATE_DESC  to Pair(Icons.Outlined.CalendarToday, stringResource(R.string.sort_date_desc)),
            SortMode.TITLE_ASC  to Pair(Icons.Outlined.SortByAlpha,   stringResource(R.string.sort_title_asc)),
            SortMode.TITLE_DESC to Pair(Icons.Outlined.SortByAlpha,   stringResource(R.string.sort_title_desc)),
            SortMode.CREATED    to Pair(Icons.Outlined.AccessTime,     stringResource(R.string.sort_created))
        ).forEach { (mode, iconAndLabel) ->
            val (icon, label) = iconAndLabel
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (currentSort == mode) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontWeight = if (currentSort == mode) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (currentSort == mode) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                        if (currentSort == mode) {
                            Spacer(Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
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
        FilterChip(selected = currentMode == FilterMode.ALL, onClick = { onModeSelected(
            FilterMode.ALL) }, label = { Text(stringResource(R.string.filter_all)) })
        FilterChip(selected = currentMode == FilterMode.COUNTDOWN, onClick = { onModeSelected(
            FilterMode.COUNTDOWN) }, label = { Text(stringResource(R.string.filter_countdown)) })
        FilterChip(selected = currentMode == FilterMode.COUNTUP, onClick = { onModeSelected(
            FilterMode.COUNTUP) }, label = { Text(stringResource(R.string.filter_countup)) })
    }
}

// ─── Hauptliste ───────────────────────────────────────────────────────────────

@Composable
private fun MainListContent(
    countdowns: List<Countdown>,
    filterMode: FilterMode,
    searchQuery: String,
    onCountdownClick: (Countdown) -> Unit,
    onAddCountdown: () -> Unit = {}
) {
    if (countdowns.isEmpty()) {
        EmptyStateView(
            onAddCountdown = onAddCountdown
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (searchQuery.isNotBlank() || filterMode != FilterMode.ALL) {
            items(items = countdowns, key = { it.id }) { countdown ->
                AnimatedCountdownCard(countdown, onCountdownClick)
            }
        } else {
            val futureItems = countdowns.filter { !it.isCountUp }
            val pastItems = countdowns.filter { it.isCountUp }

            if (futureItems.isNotEmpty()) {
                item(key = "header_countdown") {
                    SectionHeader(label = stringResource(R.string.section_countdown), count = futureItems.size)
                }
                items(items = futureItems, key = { it.id }) { countdown ->
                    AnimatedCountdownCard(countdown, onCountdownClick)
                }
            }
            if (pastItems.isNotEmpty()) {
                item(key = "header_countup") {
                    SectionHeader(label = stringResource(R.string.section_countup), count = pastItems.size)
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
    // Kein extra visible-State nötig — LazyColumn mit key= verwaltet
    // das Ein-/Ausblenden selbst. AnimatedVisibility wird direkt mit true gestartet.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCountdownClick(countdown) }
    ) {
        CountdownCard(
            countdown = countdown
        )
    }
}

// ─── Einstellungen ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsPageContent(
    onThemeChanged: (CustomTheme) -> Unit,
    currentTheme: CustomTheme,
    isVisible: Boolean = true
) {
    // Lazy-Guard: Einstellungen werden nur gerendert wenn der Tab aktiv ist.
    // So laufen die collectAsState-Flows nicht wenn der Nutzer auf Tab 0 oder 2 ist.
    if (!isVisible) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = remember {
        HapticFeedback(
            context
        )
    }
    val scrollState = rememberScrollState()

    val themeMode by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
    val defaultFormat by AppPreferences
        .getDefaultFormat(context).collectAsState(initial = CountdownDisplayFormat.DAYS_ONLY)
    val defaultColor by AppPreferences
        .getDefaultColor(context).collectAsState(initial = "#FF7043")
    val defaultTime by AppPreferences
        .getDefaultTime(context).collectAsState(initial = LocalTime.of(12, 0))

    var showTimePicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showFormatMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // ── Theme-Auswahl (Inline-Grid, kein Dialog) ──────────────────────────
        SettingsSectionTitle(stringResource(R.string.settings_color_scheme))
        Text(
            text = stringResource(R.string.settings_color_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ThemePickerGrid(
            currentTheme = currentTheme,
            onThemeSelected = { theme ->
                haptic.success()
                onThemeChanged(theme)
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Hell / Dunkel ─────────────────────────────────────────────────────
        SettingsSectionTitle(stringResource(R.string.settings_light_dark))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeModeOption("⚙️  Systemeinstellung", themeMode == ThemeMode.SYSTEM) {
                haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.SYSTEM) }
            }
            ThemeModeOption("🌞  Hell", themeMode == ThemeMode.LIGHT) {
                haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.LIGHT) }
            }
            ThemeModeOption("🌙  Dunkel", themeMode == ThemeMode.DARK) {
                haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.DARK) }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Sprache ───────────────────────────────────────────────────────────
        SettingsSectionTitle(stringResource(R.string.settings_language))
        LanguagePickerSection(
            onLanguageSelected = { language ->
                haptic.tick()
                LanguageManager.persistLanguageSync(context, language)
                scope.launch {
                    LanguageManager.setLanguage(context, language)
                }

                val localeList = if (language == AppLanguage.SYSTEM || language.tag.isEmpty()) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(language.tag)
                }

                Log.d("LANGUAGE", "Setting locale: ${language.tag}")
                Log.d("LANGUAGE", "LocaleList: $localeList")

                AppCompatDelegate.setApplicationLocales(localeList)

                Log.d("LANGUAGE", "After set: ${AppCompatDelegate.getApplicationLocales()}")

                // Erzwinge Activity-Neustart auf allen Versionen
                (context as? Activity)?.let { activity ->
                    activity.window.decorView.postDelayed({ activity.recreate() }, 150)
                }
            }
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // ── Standard-Einstellungen ────────────────────────────────────────────
        SettingsSectionTitle(stringResource(R.string.settings_defaults))
        Text(
            text = stringResource(R.string.settings_defaults_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SettingsRow(
            label = stringResource(R.string.settings_format_label),
            value = when (defaultFormat) {
                CountdownDisplayFormat.DAYS_ONLY         -> stringResource(R.string.settings_format_days)
                CountdownDisplayFormat.WEEKS_DAYS        -> stringResource(R.string.settings_format_weeks)
                CountdownDisplayFormat.MONTHS_DAYS       -> stringResource(R.string.settings_format_months)
                CountdownDisplayFormat.YEARS_MONTHS_DAYS -> stringResource(R.string.settings_format_years)
            },
            onClick = { haptic.tick(); showFormatMenu = true }
        )
        SettingsRowColor(
            label = stringResource(R.string.settings_color_label),
            color = defaultColor,
            onClick = { haptic.tick(); showColorPicker = true }
        )
        SettingsRow(
            label = stringResource(R.string.settings_time_label),
            value = defaultTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " Uhr",
            onClick = { haptic.tick(); showTimePicker = true }
        )

        Spacer(Modifier.height(16.dp))
    }

    // ── Format-Picker ─────────────────────────────────────────────────────────
    if (showFormatMenu) {
        AlertDialog(
            onDismissRequest = { showFormatMenu = false },
            title = { Text(stringResource(R.string.dialog_default_format)) },
            text = {
                Column {
                    listOf(
                        CountdownDisplayFormat.DAYS_ONLY to stringResource(R.string.settings_format_days),
                        CountdownDisplayFormat.WEEKS_DAYS to stringResource(R.string.settings_format_weeks),
                        CountdownDisplayFormat.MONTHS_DAYS to stringResource(R.string.settings_format_months),
                        CountdownDisplayFormat.YEARS_MONTHS_DAYS to stringResource(R.string.settings_format_years)
                    ).forEach { (format, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic.tick()
                                    scope.launch { AppPreferences.setDefaultFormat(context, format) }
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
            confirmButton = { TextButton(onClick = { showFormatMenu = false }) { Text(stringResource(R.string.close)) } }
        )
    }

    // ── Farb-Picker ───────────────────────────────────────────────────────────
    if (showColorPicker) {
        val initialColorInt = try { android.graphics.Color.parseColor(defaultColor) }
        catch (e: Exception) { android.graphics.Color.parseColor("#FF7043") }
        var r by remember { mutableStateOf(android.graphics.Color.red(initialColorInt)) }
        var g by remember { mutableStateOf(android.graphics.Color.green(initialColorInt)) }
        var b by remember { mutableStateOf(android.graphics.Color.blue(initialColorInt)) }

        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text(stringResource(R.string.dialog_default_color)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf("#FF7043","#EF5350","#EC407A","#AB47BC","#5C6BC0","#42A5F5","#26A69A","#66BB6A","#FFA726","#8D6E63").forEach { hex ->
                            val c = try {
                                Color(android.graphics.Color.parseColor(hex))
                            }
                            catch (e: Exception) { MaterialTheme.colorScheme.primary }
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = c,
                                onClick = {
                                    scope.launch { AppPreferences.setDefaultColor(context, hex) }
                                    showColorPicker = false
                                }
                            ) {
                                if (defaultColor == hex) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                        Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
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
                    val hex = String.format("#%02X%02X%02X", r, g, b)
                    scope.launch { AppPreferences.setDefaultColor(context, hex) }
                    showColorPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showColorPicker = false }) { Text("Abbrechen") } }
        )
    }

    // ── Zeit-Picker ───────────────────────────────────────────────────────────
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = defaultTime.hour,
            initialMinute = defaultTime.minute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.dialog_default_time)) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(onClick = {
                    haptic.click()
                    scope.launch {
                        AppPreferences.setDefaultTime(
                            context,
                            LocalTime.of(timePickerState.hour, timePickerState.minute)
                        )
                    }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Abbrechen") } }
        )
    }
}

// ─── Theme-Picker Grid (kein Dialog) ─────────────────────────────────────────

@Composable
private fun ThemePickerGrid(
    currentTheme: CustomTheme,
    onThemeSelected: (CustomTheme) -> Unit
) {
    // Themes in 2-Spalten-Grid
    val themes = CustomTheme.values().toList()
    val chunked = themes.chunked(2)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        chunked.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { theme ->
                    ThemePickerCard(
                        theme = theme,
                        isSelected = currentTheme == theme,
                        onClick = { onThemeSelected(theme) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Falls ungerade Anzahl, leere Box als Platzhalter
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ThemePickerCard(
    theme: CustomTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val config =
        getThemeConfig(theme)
    val primaryLight = config.lightColorScheme.primary
    val secondary = config.lightColorScheme.secondary
    val tertiary = config.lightColorScheme.tertiary

    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        animationSpec = tween(200),
        label = "bg_${theme.name}"
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outlineVariant
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Farbstreifen
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Große primäre Farbe
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(primaryLight)
                )
                // Sekundäre + Tertiär
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(11.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .background(secondary)
                    )
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(11.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                            .background(tertiary)
                    )
                }
                if (isSelected) {
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                "✓",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Name
            Text(
                text = config.name.substringBefore(" ("),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            // Untertitel
            Text(
                text = config.name.substringAfter("(").trimEnd(')'),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Settings-Hilfsfunktionen ─────────────────────────────────────────────────

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun ThemeModeOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
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
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SettingsRowColor(label: String, color: String, onClick: () -> Unit) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) { MaterialTheme.colorScheme.primary }

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

private fun shareCountdown(context: Context, countdown: Countdown) {
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

// ─── Sprachauswahl ────────────────────────────────────────────────────────────

@Composable
private fun LanguagePickerSection(
    onLanguageSelected: (AppLanguage) -> Unit
) {
    val context = LocalContext.current
    val currentLanguage by LanguageManager.getLanguage(context)
        .collectAsState(initial = AppLanguage.SYSTEM)

    // Flaggen-Emojis als Deko
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
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                onClick = { onLanguageSelected(language) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = flags[language] ?: "🌐",
                            fontSize = 20.sp
                        )
                        Text(
                            text = language.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}