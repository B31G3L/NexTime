package de.beigel.nextime.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.calculateTimeRemaining
import de.beigel.nextime.utils.HapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownCardDialog(
    countdown: Countdown,
    onDismiss: () -> Unit,
    onEdit: (Countdown) -> Unit,
    onDelete: (Countdown) -> Unit,
    onShare: (Countdown) -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()

    // Prüfen ob Countdown abgelaufen ist → Konfetti
    val timeInfo = remember { countdown.calculateTimeRemaining() }
    val showConfetti = remember { timeInfo.isPast && !countdown.isRecurring }

    ModalBottomSheet(
        onDismissRequest = { haptic.tick(); onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.width(40.dp).height(4.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                ) {}
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Abgelaufen-Banner
                if (showConfetti) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎉", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Dieser Countdown ist abgelaufen!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = "🎊", fontSize = 20.sp)
                        }
                    }
                }

                // Card-Vorschau
                CountdownCard(countdown = countdown)

                // Aktionen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CardActionButton(
                        icon = Icons.Default.Share,
                        label = "Teilen",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { haptic.click(); onShare(countdown); onDismiss() }
                    )
                    CardActionButton(
                        icon = Icons.Default.Edit,
                        label = "Bearbeiten",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { haptic.click(); onEdit(countdown); onDismiss() }
                    )
                    CardActionButton(
                        icon = Icons.Default.Delete,
                        label = "Löschen",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        onClick = { haptic.tick(); showDeleteConfirm = true }
                    )
                }
            }

            // Konfetti-Overlay über dem Sheet-Inhalt
            if (showConfetti) {
                ConfettiOverlay(
                    active = true,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { haptic.tick(); showDeleteConfirm = false },
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Countdown löschen?") },
            text = {
                Text(
                    "Möchtest du \"${countdown.title}\" wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { haptic.heavy(); showDeleteConfirm = false; onDelete(countdown); onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { haptic.tick(); showDeleteConfirm = false }) { Text("Abbrechen") }
            }
        )
    }
}

@Composable
private fun CardActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        }
    }
}