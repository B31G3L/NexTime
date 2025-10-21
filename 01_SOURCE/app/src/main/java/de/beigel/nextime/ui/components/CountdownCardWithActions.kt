package de.beigel.nextime.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.utils.HapticFeedback

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CountdownCardWithActions(
    countdown: Countdown,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Die normale CountdownCard - aber mit Long-Press
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {
                        haptic.tick()
                        onClick()
                    },
                    onLongClick = {
                        haptic.heavy()
                        showMenu = true
                    }
                )
        ) {
            CountdownCard(countdown = countdown)
        }

        // Dropdown Menu für Edit/Delete
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Bearbeiten") },
                onClick = {
                    haptic.click()
                    showMenu = false
                    onEdit()
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Bearbeiten",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            Divider()

            DropdownMenuItem(
                text = { Text("Löschen") },
                onClick = {
                    haptic.heavy()
                    showMenu = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Löschen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }

        // Optional: Menu-Button in der Ecke (alternative zu Long-Press)
        IconButton(
            onClick = {
                haptic.tick()
                showMenu = true
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Mehr Optionen",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}