package de.beigel.nextime.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.ui.components.*
import de.beigel.nextime.ui.viewmodel.CountdownViewModel
import de.beigel.nextime.utils.HapticFeedback

enum class SortOption {
    DATE_ASC, DATE_DESC, NAME_ASC, NAME_DESC
}

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

    var showAddDialog by remember { mutableStateOf(false) }
    var editingCountdown by remember { mutableStateOf<Countdown?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.DATE_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Einstellungen
    var showPercentage by remember { mutableStateOf(true) }

    // Sortierte Liste
    val sortedCountdowns = remember(countdowns, sortOption) {
        when (sortOption) {
            SortOption.DATE_ASC -> countdowns.sortedBy { it.targetDateTime }
            SortOption.DATE_DESC -> countdowns.sortedByDescending { it.targetDateTime }
            SortOption.NAME_ASC -> countdowns.sortedBy { it.title.lowercase() }
            SortOption.NAME_DESC -> countdowns.sortedByDescending { it.title.lowercase() }
        }
    }

    // Detail-Screen anzeigen wenn ein Countdown ausgewählt ist
    if (selectedCountdown != null) {
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
                viewModel.deleteCountdown(selectedCountdown!!)
                viewModel.selectCountdown(null)
            },
            onShare = {
                shareCountdown(context, selectedCountdown!!)
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NexTime") },
                actions = {
                    IconButton(onClick = {
                        haptic.tick()
                        showSettingsDialog = true
                    }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Einstellungen"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                // Neuer verbesserter Empty State
                EmptyStateView(
                    onAddCountdown = {
                        haptic.click()
                        showAddDialog = true
                    }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Sortier-Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Meine Countdowns",
                            style = MaterialTheme.typography.headlineMedium
                        )

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
                    }

                    // Liste der Countdowns
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = sortedCountdowns,
                            key = { it.id }
                        ) { countdown ->
                            SwipeableCountdownCard(
                                countdown = countdown,
                                onEdit = {
                                    editingCountdown = countdown
                                },
                                onDelete = {
                                    viewModel.deleteCountdown(countdown)
                                },
                                showPercentage = showPercentage,
                                onClick = {
                                    viewModel.selectCountdown(countdown)
                                }
                            )
                        }
                    }
                }
            }

            // Neuer Expandable FAB mit Vorlagen
            ExpandableFab(
                onTemplateSelected = { template ->
                    val countdown = template.toCountdown()
                    viewModel.addCountdown(countdown)
                },
                onCustom = {
                    showAddDialog = true
                }
            )
        }
    }

    // Dialog zum Hinzufügen
    if (showAddDialog) {
        AddEditCountdownDialog(
            countdown = null,
            onDismiss = { showAddDialog = false },
            onSave = { countdown ->
                viewModel.addCountdown(countdown)
                showAddDialog = false
            }
        )
    }

    // Dialog zum Bearbeiten
    editingCountdown?.let { countdown ->
        AddEditCountdownDialog(
            countdown = countdown,
            onDismiss = { editingCountdown = null },
            onSave = { updatedCountdown ->
                viewModel.updateCountdown(updatedCountdown)
                editingCountdown = null
            }
        )
    }

    // Einstellungen Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = {
                haptic.tick()
                showSettingsDialog = false
            },
            title = { Text("Einstellungen") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Dark Mode Toggle
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

                    Divider()

                    // Prozentanzeige Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fortschritt anzeigen")
                            Text(
                                "Zeigt Prozent unter dem Fortschrittsbalken",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showPercentage,
                            onCheckedChange = {
                                haptic.tick()
                                showPercentage = it
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
}

// Hilfsfunktion zum Teilen von Countdowns
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