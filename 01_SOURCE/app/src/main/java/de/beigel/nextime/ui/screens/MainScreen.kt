package de.beigel.nextime.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
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
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.ui.theme.ThemeMode
import de.beigel.nextime.ui.theme.ThemePreferences
import de.beigel.nextime.ui.viewmodel.CountdownViewModel
import de.beigel.nextime.utils.HapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CountdownViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
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
                // Öffnen: Slide von rechts
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(animationSpec = tween(400)) togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { -it / 3 },
                            animationSpec = tween(400, easing = FastOutSlowInEasing)
                        ) + fadeOut(animationSpec = tween(300))
            } else {
                // Schließen: Slide nach rechts
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
                // Sicherheitsprüfung
                val currentCountdown = selectedCountdown
                if (currentCountdown == null) {
                    // Fallback zur Hauptansicht wenn kein Countdown ausgewählt
                    LaunchedEffect(Unit) {
                        // Diese wird ausgelöst und führt zurück zur Hauptansicht
                    }
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
                // Hauptansicht
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
                        imageVector = androidx.compose.material.icons.Icons.Default.Delete,
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

    // Settings Dialog
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
            title = { Text("Einstellungen") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
                ) {
                    // Theme-Auswahl
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Design",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))

                        // System
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignSystem.CornerRadius.medium),
                            color = if (themeMode == ThemeMode.SYSTEM)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(DesignSystem.Spacing.medium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Systemeinstellung")
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

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xSmall))

                        // Hell
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignSystem.CornerRadius.medium),
                            color = if (themeMode == ThemeMode.LIGHT)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(DesignSystem.Spacing.medium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hell")
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

                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xSmall))

                        // Dunkel
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignSystem.CornerRadius.medium),
                            color = if (themeMode == ThemeMode.DARK)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(DesignSystem.Spacing.medium),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Dunkel")
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

                    // Standard-Uhrzeit
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
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.small))
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
            val timePickerState = androidx.compose.material3.rememberTimePickerState(
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
                    androidx.compose.material3.TimePicker(state = timePickerState)
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
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
        append("\n\n📅 ${countdown.targetDateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
        if (countdown.includeTime) {
            append(" um ${countdown.targetDateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))} Uhr")
        }
        append("\n\nErstellt mit NexTime")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Countdown teilen"))
}