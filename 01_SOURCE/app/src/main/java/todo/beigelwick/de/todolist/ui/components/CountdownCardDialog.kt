package todo.beigelwick.de.todolist.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.calculateTimeRemaining
import todo.beigelwick.de.todolist.ui.viewmodel.CountdownViewModel
import todo.beigelwick.de.todolist.utils.HapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownCardDialog(
    countdown : Countdown,
    onDismiss : () -> Unit,
    onEdit    : (Countdown) -> Unit,
    onDelete  : (Countdown) -> Unit,
    onShare   : (Countdown) -> Unit,
    viewModel : CountdownViewModel = viewModel()
) {
    val context    = LocalContext.current
    val haptic     = remember { HapticFeedback(context) }
    val sheetState = rememberModalBottomSheetState()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    val timeInfo     = remember { countdown.calculateTimeRemaining() }
    val showConfetti = remember {
        timeInfo.isPast &&
                !countdown.isRecurring &&
                countdown.targetDateTime.isAfter(countdown.createdAt)
    }

    ModalBottomSheet(
        onDismissRequest = { haptic.tick(); onDismiss() },
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle       = {
            Box(
                modifier         = Modifier.padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.width(40.dp).height(4.dp),
                    shape    = RoundedCornerShape(2.dp),
                    color    = MaterialTheme.colorScheme.outlineVariant
                ) {}
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Abgelaufen-Banner
                if (showConfetti) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier              = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(text = "🎉", fontSize = 20.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text       = stringResource(R.string.dialog_expired_banner),
                                style      = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color      = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = "🎊", fontSize = 20.sp)
                        }
                    }
                }

                // Card-Vorschau
                CountdownCard(countdown = countdown)

                // Aktionsbuttons — 4 Buttons in einer Reihe
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pin / Unpin
                    CardActionButton(
                        icon     = if (countdown.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        label    = stringResource(
                            if (countdown.isPinned) R.string.action_unpin else R.string.action_pin
                        ),
                        color    = if (countdown.isPinned) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick  = {
                            haptic.click()
                            viewModel.togglePin(countdown)
                            onDismiss()
                        }
                    )
                    // Teilen
                    CardActionButton(
                        icon     = Icons.Default.Share,
                        label    = stringResource(R.string.action_share),
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick  = { haptic.click(); onShare(countdown) }
                    )
                    // Bearbeiten
                    CardActionButton(
                        icon     = Icons.Default.Edit,
                        label    = stringResource(R.string.action_edit),
                        color    = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick  = { haptic.click(); onEdit(countdown) }
                    )
                    // Löschen
                    CardActionButton(
                        icon     = Icons.Default.Delete,
                        label    = stringResource(R.string.action_delete),
                        color    = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                        onClick  = { haptic.tick(); showDeleteConfirm = true }
                    )
                }
            }

            // Konfetti
            if (showConfetti) {
                ConfettiOverlay(active = true, modifier = Modifier.matchParentSize())
            }
        }
    }

    // Löschen-Bestätigung
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { haptic.tick(); showDeleteConfirm = false },
            icon             = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.error
                )
            },
            title   = { Text(stringResource(R.string.confirm_delete_title)) },
            text    = {
                Text(
                    stringResource(R.string.confirm_delete_msg, countdown.title),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.heavy()
                        showDeleteConfirm = false
                        onDelete(countdown)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = {
                TextButton(onClick = { haptic.tick(); showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

// ─── Action Button ────────────────────────────────────────────────────────────

@Composable
private fun CardActionButton(
    icon     : ImageVector,
    label    : String,
    color    : Color,
    modifier : Modifier = Modifier,
    onClick  : () -> Unit
) {
    Surface(
        modifier = modifier.height(64.dp),
        shape    = RoundedCornerShape(16.dp),
        color    = color.copy(alpha = 0.1f),
        onClick  = onClick
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = color,
                modifier           = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text       = label,
                style      = MaterialTheme.typography.labelMedium,
                color      = color,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 11.sp
            )
        }
    }
}