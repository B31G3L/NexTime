package de.beigel.nextime.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Celebration
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun EmptyStateView(
    onAddCountdown: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .alpha(alpha)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animierte Icon-Gruppe
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingIcons()
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Haupttext
        Text(
            text = "Noch keine Countdowns",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Erstelle deinen ersten Countdown und behalte wichtige Ereignisse im Blick!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Get Started Button
        Button(
            onClick = onAddCountdown,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Timer,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ersten Countdown erstellen",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hilfreiche Tipps
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "💡 Tipps für den Start",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                TipItem(
                    icon = Icons.Outlined.CalendarMonth,
                    text = "Erstelle Countdowns für Geburtstage, Urlaube oder wichtige Termine"
                )

                TipItem(
                    icon = Icons.Outlined.Event,
                    text = "Aktiviere die Uhrzeit für präzise Countdowns"
                )

                TipItem(
                    icon = Icons.Outlined.Celebration,
                    text = "Wähle individuelle Farben für jeden Countdown"
                )
            }
        }
    }
}

@Composable
private fun FloatingIcons() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")

    // Verschiedene Icons mit unterschiedlichen Animationen
    val icons = listOf(
        Icons.Outlined.CalendarMonth,
        Icons.Outlined.Event,
        Icons.Outlined.Celebration,
        Icons.Outlined.Timer
    )

    val offsets = listOf(
        Offset(-60f, -60f),
        Offset(60f, -60f),
        Offset(-60f, 60f),
        Offset(60f, 60f)
    )

    icons.forEachIndexed { index, icon ->
        val offset = offsets[index]

        val animatedOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 2000 + (index * 200),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset$index"
        )

        val rotation by infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 3000 + (index * 300),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotation$index"
        )

        FloatingIcon(
            icon = icon,
            offsetX = offset.x,
            offsetY = offset.y + animatedOffset,
            rotation = rotation,
            index = index
        )
    }
}

@Composable
private fun FloatingIcon(
    icon: ImageVector,
    offsetX: Float,
    offsetY: Float,
    rotation: Float,
    index: Int
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )

    Surface(
        modifier = Modifier
            .offset(x = offsetX.dp, y = offsetY.dp)
            .size(60.dp),
        shape = MaterialTheme.shapes.medium,
        color = colors[index].copy(alpha = 0.1f),
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = colors[index].copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun TipItem(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class Offset(val x: Float, val y: Float)