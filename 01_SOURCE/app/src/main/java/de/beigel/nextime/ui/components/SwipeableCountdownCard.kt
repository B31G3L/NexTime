package de.beigel.nextime.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.utils.HapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableCountdownCard(
    countdown: Countdown,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }

    // Track ob wir bereits vibriert haben für diesen Swipe
    var hasVibrated by remember { mutableStateOf(false) }

    // Key für das Zurücksetzen der Card
    var dismissKey by remember { mutableStateOf(0) }

    val dismissState = rememberDismissState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                DismissValue.DismissedToStart -> {
                    haptic.heavy()
                    onDelete()
                    true
                }
                DismissValue.DismissedToEnd -> {
                    haptic.click()
                    onEdit()
                    dismissKey++
                    false
                }
                else -> false
            }
        },
        positionalThreshold = { it * 0.25f }
    )

    // Haptic feedback während des Swipens
    LaunchedEffect(dismissState.progress) {
        val progress = dismissState.progress

        if (progress > 0.25f && !hasVibrated) {
            haptic.tick()
            hasVibrated = true
        } else if (progress < 0.1f && hasVibrated) {
            hasVibrated = false
        }
    }

    key(dismissKey) {
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
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

                        if (dismissState.progress > 0.25f) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (direction) {
                                    DismissDirection.StartToEnd -> "Bearbeiten"
                                    DismissDirection.EndToStart -> "Löschen"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when (direction) {
                                    DismissDirection.StartToEnd -> MaterialTheme.colorScheme.primary
                                    DismissDirection.EndToStart -> MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
            },
            dismissContent = {
                // Wrap CountdownCard with clickable modifier if onClick is provided
                Box(
                    modifier = if (onClick != null) {
                        Modifier.clickable {
                            haptic.tick()
                            onClick()
                        }
                    } else {
                        Modifier
                    }
                ) {
                    CountdownCard(
                        countdown = countdown

                    )
                }
            },
            directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart)
        )
    }
}