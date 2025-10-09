package de.beigel.nextime.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.beigel.nextime.ui.components.SwipeableCountdownCard
import de.beigel.nextime.ui.components.AddEditCountdownDialog
import de.beigel.nextime.ui.viewmodel.CountdownViewModel

enum class SortOption {
    DATE_ASC, DATE_DESC, NAME_ASC, NAME_DESC
}

@Composable
fun MainScreen(
    viewModel: CountdownViewModel = viewModel(),
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val countdowns by viewModel.countdowns.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCountdown by remember { mutableStateOf<de.beigel.nextime.data.model.Countdown?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.DATE_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Sortierte Liste
    val sortedCountdowns = remember(countdowns, sortOption) {
        when (sortOption) {
            SortOption.DATE_ASC -> countdowns.sortedBy { it.targetDateTime }
            SortOption.DATE_DESC -> countdowns.sortedByDescending { it.targetDateTime }
            SortOption.NAME_ASC -> countdowns.sortedBy { it.title.lowercase() }
            SortOption.NAME_DESC -> countdowns.sortedByDescending { it.title.lowercase() }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Countdown hinzufügen",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (countdowns.isEmpty()) {
                // Leerer Zustand
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Noch keine Countdowns",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tippe auf +, um deinen ersten Countdown zu erstellen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
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
                            IconButton(onClick = { showSortMenu = true }) {
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
                                        sortOption = SortOption.DATE_ASC
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Datum ↓") },
                                    onClick = {
                                        sortOption = SortOption.DATE_DESC
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Name A-Z") },
                                    onClick = {
                                        sortOption = SortOption.NAME_ASC
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Name Z-A") },
                                    onClick = {
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
                                }
                            )
                        }
                    }
                }
            }
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
}