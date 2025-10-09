package de.beigel.nextime.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.beigel.nextime.data.model.Countdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableCountdownCard(
    countdown: Countdown,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    onDelete()
                    true
                }
                DismissValue.DismissedToEnd -> {
                    onEdit()
                    true
                }
                else -> false
            }
        },
        positionalThreshold = { it * 0.25f }
    )

    SwipeToDismiss(
        state = dismissState,
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.Transparent
                    DismissValue.DismissedToEnd -> MaterialTheme.colorScheme.primaryContainer
                    DismissValue.DismissedToStart -> MaterialTheme.colorScheme.errorContainer
                },
                label = "color"
            )

            val alignment = when (direction) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
                DismissDirection.EndToStart -> Alignment.CenterEnd
            }

            val icon = when (direction) {
                DismissDirection.StartToEnd -> Icons.Default.Edit
                DismissDirection.EndToStart -> Icons.Default.Delete
            }

            val scale by animateFloatAsState(
                if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f,
                label = "scale"
            )

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.scale(scale),
                    tint = when (direction) {
                        DismissDirection.StartToEnd -> MaterialTheme.colorScheme.primary
                        DismissDirection.EndToStart -> MaterialTheme.colorScheme.error
                    }
                )
            }
        },
        dismissContent = {
            CountdownCard(
                countdown = countdown,
                onEdit = onEdit,
                onDelete = onDelete
            )
        },
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart)
    )
}