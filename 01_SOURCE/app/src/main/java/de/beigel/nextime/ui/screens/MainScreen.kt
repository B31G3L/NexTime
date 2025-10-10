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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.components.*
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.ui.viewmodel.CountdownViewModel
import de.beigel.nextime.utils.HapticFeedback

enum class SortOption {
    DATE_ASC, DATE_DESC, NAME_ASC, NAME_DESC
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
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

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCountdown by remember { mutableStateOf<Countdown?>(null) }
    var countdownToDelete by remember { mutableStateOf<Countdown?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.DATE_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val sortedCountdowns = remember(countdowns, sortOption) {
        when (sortOption) {
            SortOption.DATE_ASC -> countdowns.sortedBy { it.targetDateTime }
            SortOption.DATE_DESC -> countdowns.sortedByDescending { it.targetDateTime }
            SortOption.NAME_ASC -> countdowns.sortedBy { it.title.lowercase() }
            SortOption.NAME_DESC -> countdowns.sortedByDescending { it.title.lowercase() }
        }
    }

    // Animierte Detailansicht
    AnimatedContent(
        targetState = selectedCountdown != null,
        transitionSpec = {
            if (targetState) {
                // Öffnen: Slide von rechts + Fade
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(400)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            } else {
                // Schließen: Slide nach rechts + Fade
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(400)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            }
        },
        label = "detail_animation"
    ) { showDetail ->
        if (showDetail && selectedCountdown != null) {
            // Back-Handler für Hardware-Zurück-Taste
            BackHandler {
                viewModel.selectCountdown(null)
            }

            CountdownDetailScreen(
                countdown = selectedCountdown!!,
                onBack = {
                    viewModel.selectCountdown(null)
                },
                onEdit = {
                    editingCountdown = selectedCountdown
                    viewModel.selectCountdown(null)
                },
                onDelete = {
                    countdownToDelete = selectedCountdown
                    viewModel.selectCountdown(null)
                },
                onShare = { shareCountdown(context, selectedCountdown!!) }
            )
        } else {
            // Hauptansicht
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("NexTime") },
                        actions = {
                            // Sort Button
                            Box {
                                IconButton(onClick = {
                                    haptic.tick()
                                    showSortMenu = true
                                }) {
                                    Icon(
                                        Icons.Default.Sort,
                                        contentDescription = "Sortieren"
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Datum ↑") },
                                        onClick = {
                                            haptic.tick()
                                            sortOption = SortOption.DATE_ASC
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Datum ↓") },
                                        onClick = {
                                            haptic.tick()
                                            sortOption = SortOption.DATE_DESC
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Name A-Z") },
                                        onClick = {
                                            haptic.tick()
                                            sortOption = SortOption.NAME_ASC
                                            showSortMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Name Z-A") },
                                        onClick = {
                                            haptic.tick()
                                            sortOption = SortOption.NAME_DESC
                                            showSortMenu = false
                                        }
                                    )
                                }
                            }
                            // Info Button
                            IconButton(onClick = {
                                haptic.tick()
                                showAboutDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Info & Support"
                                )
                            }
                            // Settings Button
                            IconButton(onClick = {
                                haptic.tick()
                                showSettingsDialog = true
                            }) {
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
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Liste der Countdowns mit Animation
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
                                    items = sortedCountdowns,
                                    key = { it.id }
                                ) { countdown ->
                                    // Jede Card erscheint mit Animation
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
                                            onEdit = { editingCountdown = countdown },
                                            onDelete = { countdownToDelete = countdown },
                                            onClick = {
                                                haptic.tick()
                                                viewModel.selectCountdown(countdown)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Einfacher FAB
                    SimpleFab(
                        onAddCountdown = {
                            showAddDialog = true
                        }
                    )
                }
            }
        }
    }

    // Lösch-Bestätigungsdialog mit Animation
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
                }
            )
        }
    }

    // Add/Edit Dialog mit Animation
    AnimatedVisibility(
        visible = showAddDialog || editingCountdown != null,
        enter = scaleIn(
            initialScale = 0.9f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        AddEditCountdownDialog(
            countdown = editingCountdown,
            onDismiss = {
                showAddDialog = false
                editingCountdown = null
            },
            onSave = { countdown ->
                if (editingCountdown != null) {
                    viewModel.updateCountdown(countdown)
                } else {
                    viewModel.addCountdown(countdown)
                }
                showAddDialog = false
                editingCountdown = null
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Mode")
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = {
                                haptic.tick()
                                onThemeToggle()
                            }
                        )
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