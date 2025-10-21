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
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }

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
                    }
                )
        ) {
            CountdownCard(countdown = countdown)
        }

    }
}