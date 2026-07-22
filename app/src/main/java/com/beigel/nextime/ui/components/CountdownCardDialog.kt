package com.beigel.nextime.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Event
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
import com.beigel.nextime.data.model.calculateTimeRemaining
import com.beigel.nextime.R
import com.beigel.nextime.data.model.Countdown
import com.beigel.nextime.ui.viewmodel.CountdownViewModel
import com.beigel.nextime.utils.HapticFeedback
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


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

    val isCompleted = remember(countdown.id) {
        countdown.calculateTimeRemaining().let { info ->
            info.isPast && !countdown.isRecurring &&
                    countdown.targetDateTime.isAfter(countdown.createdAt)
        }
    }

    // Lokalisiert formatiertes Erstelldatum, z. B. "22.07.2026" bzw. "Jul 22, 2026"
    val createdAtFormatted = remember(countdown.id, countdown.createdAt) {
        try {
            countdown.createdAt.toLocalDate()
                .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        } catch (e: Exception) {
            countdown.createdAt.toLocalDate().toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { haptic.tick(); onDismiss() },
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle       = {
            Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Surface(modifier = Modifier.width(40.dp).height(4.dp),
                    shape = RoundedCornerShape(2.dp), color = MaterialTheme.colorScheme.outlineVariant) {}
            }
        }
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Abgeschlossen-Banner
            if (isCompleted) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier              = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint               = MaterialTheme.colorScheme.tertiary,
                            modifier           = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = stringResource(R.string.dialog_expired_banner),
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Card-Vorschau
            CountdownCard(countdown = countdown)

            // Erstelldatum
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Event,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text  = stringResource(R.string.dialog_created_at, createdAtFormatted),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Aktionsbuttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CardActionButton(
                    icon     = if (countdown.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    label    = stringResource(if (countdown.isPinned) R.string.action_unpin else R.string.action_pin),
                    color    = if (countdown.isPinned) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick  = { haptic.click(); viewModel.togglePin(countdown); onDismiss() }
                )
                CardActionButton(
                    icon     = Icons.Default.Share,
                    label    = stringResource(R.string.action_share),
                    color    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick  = { haptic.click(); onShare(countdown) }
                )
                CardActionButton(
                    icon     = Icons.Default.Edit,
                    label    = stringResource(R.string.action_edit),
                    color    = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick  = { haptic.click(); onEdit(countdown) }
                )
                CardActionButton(
                    icon     = Icons.Default.Delete,
                    label    = stringResource(R.string.action_delete),
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    onClick  = { haptic.tick(); showDeleteConfirm = true }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { haptic.tick(); showDeleteConfirm = false },
            icon             = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title   = { Text(stringResource(R.string.confirm_delete_title)) },
            text    = { Text(stringResource(R.string.confirm_delete_msg, countdown.title), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = { haptic.heavy(); showDeleteConfirm = false; onDelete(countdown); onDismiss() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
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

@Composable
private fun CardActionButton(
    icon     : ImageVector,
    label    : String,
    color    : Color,
    modifier : Modifier = Modifier,
    onClick  : () -> Unit
) {
    Surface(modifier = modifier.height(64.dp), shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.1f), onClick = onClick) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        }
    }
}