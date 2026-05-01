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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AvTimer
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.beigel.nextime.BuildConfig
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.FilterMode
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.components.AddEditCountdownScreen
import de.beigel.nextime.ui.components.CountdownCard
import de.beigel.nextime.ui.components.DeveloperCard
import de.beigel.nextime.ui.components.EmptyStateView
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
    val selectedCountdown by viewModel.selectedCountdown.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()

    var showAddEditScreen by remember { mutableStateOf(false) }
    var editingCountdown by remember { mutableStateOf<Countdown?>(null) }
    var countdownToDelete by remember { mutableStateOf<Countdown?>(null) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var selectedCustomTheme by remember { mutableStateOf(CustomTheme.NEXTIME) }

    LaunchedEffect(Unit) {
        CustomThemePreferences.getCustomTheme(context).collect { theme ->
            selectedCustomTheme = theme
        }
    }

    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    val currentScreen = when {
        showAddEditScreen || editingCountdown != null -> "add_edit"
        selectedCountdown != null -> "detail"
        else -> "main"
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState != "main") {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(300))
            } else {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(300))
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

            "detail" -> {
                val currentCountdown = selectedCountdown ?: return@AnimatedContent
                CountdownDetailScreen(
                    countdown = currentCountdown,
                    onBack = { viewModel.selectCountdown(null) },
                    onEdit = {
                        editingCountdown = currentCountdown
                        viewModel.selectCountdown(null)
                    },
                    onDelete = {
                        countdownToDelete = currentCountdown
                        viewModel.selectCountdown(null)
                    },
                    onShare = { shareCountdown(context, currentCountdown) }
                )
            }

            else -> {
                Scaffold(
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Text(
                                        when (pagerState.currentPage) {
                                            0 -> "Info & Support"
                                            1 -> "NexTime"
                                            2 -> "Einstellungen"
                                            else -> "NexTime"
                                        }
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                            )
                            AnimatedVisibility(visible = pagerState.currentPage == 1) {
                                FilterChipRow(
                                    currentMode = filterMode,
                                    onModeSelected = { mode ->
                                        haptic.tick()
                                        viewModel.setFilterMode(mode)
                                    }
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
                            visible = pagerState.currentPage == 1,
                            enter = scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) + fadeIn(),
                            exit = scaleOut(animationSpec = tween(200)) + fadeOut()
                        ) {
                            FloatingActionButton(
                                onClick = { haptic.click(); showAddEditScreen = true },
                                containerColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Countdown hinzufügen",
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { paddingValues ->
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) { page ->
                        when (page) {
                            0 -> AboutPageContent()
                            1 -> MainListContent(
                                countdowns = countdowns,
                                filterMode = filterMode,
                                onCountdownClick = { countdown ->
                                    haptic.tick()
                                    viewModel.selectCountdown(countdown)
                                },
                                onCountdownEdit = { countdown -> editingCountdown = countdown },
                                onCountdownDelete = { countdown -> countdownToDelete = countdown }
                            )
                            2 -> SettingsPageContent(
                                onThemeDialogOpen = { haptic.tick(); showThemeDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    if (countdownToDelete != null) {
        DeleteConfirmationDialog(
            countdown = countdownToDelete!!,
            onConfirm = {
                haptic.heavy()
                viewModel.deleteCountdown(countdownToDelete!!)
                countdownToDelete = null
            },
            onDismiss = { haptic.tick(); countdownToDelete = null }
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
                Toast.makeText(
                    context,
                    "Theme geändert zu ${getThemeConfig(newTheme).name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

// ─── Filter-Chip-Zeile ────────────────────────────────────────────────────────

@Composable
private fun FilterChipRow(currentMode: FilterMode, onModeSelected: (FilterMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentMode == FilterMode.ALL,
            onClick = { onModeSelected(FilterMode.ALL) },
            label = { Text("Alle") }
        )
        FilterChip(
            selected = currentMode == FilterMode.COUNTDOWN,
            onClick = { onModeSelected(FilterMode.COUNTDOWN) },
            label = { Text("Countdown") }
        )
        FilterChip(
            selected = currentMode == FilterMode.COUNTUP,
            onClick = { onModeSelected(FilterMode.COUNTUP) },
            label = { Text("Count-up") }
        )
    }
}

// ─── Navigation ───────────────────────────────────────────────────────────────

@Composable
private fun BottomNavigationBar(selectedPage: Int, onPageSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.height(70.dp)
    ) {
        NavigationBarItem(
            selected = selectedPage == 0,
            onClick = { onPageSelected(0) },
            icon = { Icon(Icons.Outlined.Info, contentDescription = "Info", modifier = Modifier.size(26.dp)) },
            label = null,
            alwaysShowLabel = false
        )
        NavigationBarItem(
            selected = selectedPage == 1,
            onClick = { onPageSelected(1) },
            icon = { Icon(Icons.Outlined.AvTimer, contentDescription = "Liste", modifier = Modifier.size(26.dp)) },
            label = null,
            alwaysShowLabel = false
        )
        NavigationBarItem(
            selected = selectedPage == 2,
            onClick = { onPageSelected(2) },
            icon = { Icon(Icons.Outlined.Settings, contentDescription = "Einstellungen", modifier = Modifier.size(26.dp)) },
            label = null,
            alwaysShowLabel = false
        )
    }
}

// ─── Hauptliste ───────────────────────────────────────────────────────────────

@Composable
private fun MainListContent(
    countdowns: List<Countdown>,
    filterMode: FilterMode,
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
        if (filterMode == FilterMode.ALL) {
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
        } else {
            items(items = countdowns, key = { it.id }) { countdown ->
                AnimatedCountdownCard(countdown, onCountdownClick)
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun AnimatedCountdownCard(countdown: Countdown, onCountdownClick: (Countdown) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(countdown.id) { visible = true }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                expandVertically(animationSpec = tween(300, easing = FastOutSlowInEasing)),
        exit = fadeOut(animationSpec = tween(200)) +
                shrinkVertically(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCountdownClick(countdown) }
        ) {
            CountdownCard(countdown = countdown)
        }
    }
}

// ─── About-Seite ──────────────────────────────────────────────────────────────

@Composable
private fun AboutPageContent() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "NexTime",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "v${BuildConfig.VERSION_NAME}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
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
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = { haptic.click(); openPlayStore(context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("⭐ App bewerten") }
            OutlinedButton(
                onClick = { haptic.click(); openKofi(context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("☕ Buy me a Coffee") }
            OutlinedButton(
                onClick = { haptic.click(); reportBug(context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("🐛 Fehler melden") }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Design & Theme", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        OutlinedButton(onClick = onThemeDialogOpen, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("🎨 Theme auswählen")
        }
        Spacer(Modifier.height(8.dp))
        Text("Helles/Dunkles Design", style = MaterialTheme.typography.titleMedium)
        ThemeModeOption(
            label = "⚙️ Systemeinstellung",
            isSelected = themeMode == ThemeMode.SYSTEM,
            onClick = { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.SYSTEM) } }
        )
        ThemeModeOption(
            label = "🌞 Hell",
            isSelected = themeMode == ThemeMode.LIGHT,
            onClick = { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.LIGHT) } }
        )
        ThemeModeOption(
            label = "🌙 Dunkel",
            isSelected = themeMode == ThemeMode.DARK,
            onClick = { haptic.tick(); scope.launch { ThemePreferences.setThemeMode(context, ThemeMode.DARK) } }
        )
    }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            RadioButton(selected = isSelected, onClick = onClick)
        }
    }
}

// ─── Dialoge ──────────────────────────────────────────────────────────────────

@Composable
private fun DeleteConfirmationDialog(
    countdown: Countdown,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Countdown löschen?") },
        text = {
            Text(
                "Möchtest du \"${countdown.title}\" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Löschen") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

// ─── Helper ───────────────────────────────────────────────────────────────────

private fun shareCountdown(context: android.content.Context, countdown: Countdown) {
    val timeInfo = countdown.calculateTimeRemaining()
    val shareText = buildString {
        append("⏰ ${countdown.title}\n\n")
        if (timeInfo.isPast) append("Ist vor ${timeInfo.days} Tagen vergangen")
        else append("Noch ${timeInfo.days} Tage")
        append("\n\n📅 ${countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
        append("\n\nErstellt mit NexTime")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Countdown teilen"))
}