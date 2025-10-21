package de.beigel.nextime.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.components.*
import de.beigel.nextime.ui.theme.CustomTheme
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.ui.theme.ThemeMode
import de.beigel.nextime.ui.theme.ThemePreferences
import de.beigel.nextime.ui.theme.getThemeConfig
import de.beigel.nextime.ui.viewmodel.CountdownViewModel
import de.beigel.nextime.utils.HapticFeedback
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CountdownViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    currentCustomTheme: CustomTheme = CustomTheme.NEXTIME
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }

    val countdowns by viewModel.countdowns.collectAsState()
    val selectedCountdown by viewModel.selectedCountdown.collectAsState()

    var showAddEditScreen by remember { mutableStateOf(false) }
    var editingCountdown by remember { mutableStateOf<Countdown?>(null) }
    var countdownToDelete by remember { mutableStateOf<Countdown?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var selectedCustomTheme by remember { mutableStateOf(currentCustomTheme) }

    // Screen State Management
    val currentScreen = when {
        showAddEditScreen || editingCountdown != null -> "add_edit"
        selectedCountdown != null -> "detail"
        else -> "main"
    }

    // Animierte Screen-Navigation
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
                BackHandler {
                    showAddEditScreen = false
                    editingCountdown = null
                }

                AddEditCountdownScreen(
                    countdown = editingCountdown,
                    onSave = { countdown ->
                        if (editingCountdown != null) {
                            viewModel.updateCountdown(countdown)
                        } else {
                            viewModel.addCountdown(countdown)
                        }
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
                val currentCountdown = selectedCountdown
                if (currentCountdown == null) {
                    return@AnimatedContent
                }

                BackHandler {
                    viewModel.selectCountdown(null)
                }

                CountdownDetailScreen(
                    countdown = currentCountdown,
                    onBack = {
                        viewModel.selectCountdown(null)
                    },
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
                MainScreenContent(
                    countdowns = countdowns,
                    onCountdownClick = { countdown ->
                        haptic.tick()
                        viewModel.selectCountdown(countdown)
                    },
                    onCountdownEdit = { countdown ->
                        editingCountdown = countdown
                    },
                    onCountdownDelete = { countdown ->
                        countdownToDelete = countdown
                    },
                    onAddCountdown = {
                        showAddEditScreen = true
                    },
                    onSettingsClick = {
                        haptic.tick()
                        showSettingsDialog = true
                    },
                    onAboutClick = {
                        haptic.tick()
                        showAboutDialog = true
                    }
                )
            }
        }
    }

    // Lösch-Bestätigungsdialog
    AnimatedVisibility(
        visible = countdownToDelete != null,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        countdownToDelete?.let { countdown ->
            AlertDialog(
                onDismissRequest = {
                    haptic.tick()
                    countdownToDelete = null
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text("Countdown löschen?")
                },
                text = {
                    Text(
                        "Möchtest du \"${countdown.title}\" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            haptic.heavy()
                            viewModel.deleteCountdown(countdown)
                            countdownToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Löschen")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        haptic.tick()
                        countdownToDelete = null
                    }) {
                        Text("Abbrechen")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }

    // Settings Dialog mit Theme Integration
    if (showSettingsDialog) {
        val scope = rememberCoroutineScope()
        val themeMode by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
        val defaultTime by ThemePreferences.getDefaultTime(context).collectAsState(initial = java.time.LocalTime.of(0, 0))
        var showTimePickerSettings by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                haptic.tick()
                showSettingsDialog = false
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Text("Einstellungen")
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // === THEME SECTION ===
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Design & Theme",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Theme Selector Button
                        OutlinedButton(
                            onClick = {
                                haptic.tick()
                                showThemeDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("🎨 Theme: ${getThemeConfig(selectedCustomTheme).name}")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Helles/Dunkles Design",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // System
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (themeMode == ThemeMode.SYSTEM)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚙️ Systemeinstellung")
                                RadioButton(
                                    selected = themeMode == ThemeMode.SYSTEM,
                                    onClick = {
                                        haptic.tick()
                                        scope.launch {
                                            ThemePreferences.setThemeMode(context, ThemeMode.SYSTEM)
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Light
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (themeMode == ThemeMode.LIGHT)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌞 Hell")
                                RadioButton(
                                    selected = themeMode == ThemeMode.LIGHT,
                                    onClick = {
                                        haptic.tick()
                                        scope.launch {
                                            ThemePreferences.setThemeMode(context, ThemeMode.LIGHT)
                                        }
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Dark
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = if (themeMode == ThemeMode.DARK)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌙 Dunkel")
                                RadioButton(
                                    selected = themeMode == ThemeMode.DARK,
                                    onClick = {
                                        haptic.tick()
                                        scope.launch {
                                            ThemePreferences.setThemeMode(context, ThemeMode.DARK)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Divider()

                    // === DEFAULT TIME SECTION ===
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Standard-Uhrzeit",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Wird verwendet wenn keine Uhrzeit angegeben ist",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                haptic.tick()
                                showTimePickerSettings = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🕐 ${defaultTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))} Uhr")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    haptic.click()
                    showSettingsDialog = false
                }) {
                    Text("OK")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )

        // TimePicker für Standard-Uhrzeit
        if (showTimePickerSettings) {
            val timePickerState = rememberTimePickerState(
                initialHour = defaultTime.hour,
                initialMinute = defaultTime.minute,
                is24Hour = true
            )

            AlertDialog(
                onDismissRequest = {
                    haptic.tick()
                    showTimePickerSettings = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        haptic.click()
                        val newTime = java.time.LocalTime.of(timePickerState.hour, timePickerState.minute)
                        scope.launch {
                            ThemePreferences.setDefaultTime(context, newTime)
                        }
                        showTimePickerSettings = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        haptic.tick()
                        showTimePickerSettings = false
                    }) {
                        Text("Abbrechen")
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }

    // Theme Dialog
    if (showThemeDialog) {
        ThemeSettingsDialog(
            onDismiss = { showThemeDialog = false },
            currentTheme = selectedCustomTheme,
            onThemeChanged = { newTheme ->
                haptic.success()
                selectedCustomTheme = newTheme  // ← State wird SOFORT aktualisiert
                Toast.makeText(
                    context,
                    "Theme geändert zu ${getThemeConfig(newTheme).name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(
    countdowns: List<Countdown>,
    onCountdownClick: (Countdown) -> Unit,
    onCountdownEdit: (Countdown) -> Unit,
    onCountdownDelete: (Countdown) -> Unit,
    onAddCountdown: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NexTime") },
                actions = {
                    IconButton(onClick = onAboutClick) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = "Info & Support"
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Einstellungen"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (countdowns.isEmpty()) {
                EmptyStateView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = DesignSystem.Spacing.medium,
                        end = DesignSystem.Spacing.medium,
                        top = DesignSystem.Spacing.xSmall,
                        bottom = 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
                ) {
                    items(
                        items = countdowns,
                        key = { it.id }
                    ) { countdown ->
                        var visible by remember { mutableStateOf(false) }

                        LaunchedEffect(countdown.id) {
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(
                                animationSpec = tween(300)
                            ) + expandVertically(
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ),
                            exit = fadeOut(
                                animationSpec = tween(200)
                            ) + shrinkVertically(
                                animationSpec = tween(200)
                            )
                        ) {
                            SwipeableCountdownCard(
                                countdown = countdown,
                                onEdit = { onCountdownEdit(countdown) },
                                onDelete = { onCountdownDelete(countdown) },
                                onClick = { onCountdownClick(countdown) }
                            )
                        }
                    }
                }
            }

            SimpleFab(
                onAddCountdown = onAddCountdown
            )
        }
    }
}

private fun shareCountdown(context: android.content.Context, countdown: Countdown) {
    val timeInfo = countdown.calculateTimeRemaining()
    val shareText = buildString {
        append("⏰ ${countdown.title}\n\n")
        if (timeInfo.isPast) {
            append("Ist vor ${timeInfo.days} Tagen vergangen")
        } else {
            append("Noch ${timeInfo.days} Tage")
            if (countdown.includeTime) {
                append(" und ${timeInfo.hours}:${timeInfo.minutes} Stunden")
            }
        }
        append("\n\n📅 ${countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
        if (countdown.includeTime) {
            append(" um ${countdown.targetDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))} Uhr")
        }
        append("\n\nErstellt mit NexTime")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Countdown teilen"))
}