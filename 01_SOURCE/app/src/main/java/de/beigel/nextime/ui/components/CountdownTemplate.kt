package de.beigel.nextime.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.utils.HapticFeedback
import java.time.LocalDateTime
import java.time.LocalTime

// Vordefinierte Vorlagen
data class CountdownTemplate(
    val title: String,
    val emoji: String,
    val color: String,
    val daysOffset: Long = 30,
    val includeTime: Boolean = false,
    val showNights: Boolean = false,
    val description: String
)

val countdownTemplates = listOf(
    CountdownTemplate(
        title = "Geburtstag",
        emoji = "🎂",
        color = "#EC407A",
        daysOffset = 365,
        description = "Feiere deinen besonderen Tag"
    ),
    CountdownTemplate(
        title = "Urlaub",
        emoji = "✈️",
        color = "#42A5F5",
        daysOffset = 60,
        showNights = true,
        description = "Zähle die Tage bis zur Erholung"
    ),
    CountdownTemplate(
        title = "Prüfung",
        emoji = "📚",
        color = "#FFA726",
        daysOffset = 14,
        includeTime = true,
        description = "Bereite dich rechtzeitig vor"
    ),
    CountdownTemplate(
        title = "Event",
        emoji = "🎉",
        color = "#AB47BC",
        daysOffset = 7,
        includeTime = true,
        description = "Für besondere Veranstaltungen"
    ),
    CountdownTemplate(
        title = "Hochzeit",
        emoji = "💍",
        color = "#EF5350",
        daysOffset = 180,
        showNights = true,
        description = "Der schönste Tag im Leben"
    ),
    CountdownTemplate(
        title = "Projekt-Deadline",
        emoji = "⏰",
        color = "#FF7043",
        daysOffset = 21,
        includeTime = true,
        description = "Bleib im Zeitplan"
    )
)

@Composable
fun ExpandableFab(
    onTemplateSelected: (CountdownTemplate) -> Unit,
    onCustom: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    var isExpanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Overlay
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {
                        haptic.tick()
                        isExpanded = false
                    }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            // Template-Optionen
            AnimatedVisibility(
                visible = isExpanded,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it / 2 }
                ) + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    // Vorlagen-Grid
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Vorlage wählen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Template Items
                            countdownTemplates.forEach { template ->
                                TemplateItem(
                                    template = template,
                                    onClick = {
                                        haptic.click()
                                        isExpanded = false
                                        onTemplateSelected(template)
                                    }
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            // Eigener Countdown
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptic.click()
                                        isExpanded = false
                                        onCustom()
                                    },
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✨",
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        Column {
                                            Text(
                                                text = "Eigener Countdown",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "Ganz individuell gestalten",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Haupt-FAB
            FloatingActionButton(
                onClick = {
                    haptic.click()
                    isExpanded = !isExpanded
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = if (isExpanded) "Schließen" else "Hinzufügen",
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun TemplateItem(
    template: CountdownTemplate,
    onClick: () -> Unit
) {
    val cardColor = try {
        Color(android.graphics.Color.parseColor(template.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = cardColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji in farbigem Kreis
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(cardColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = template.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Column {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Info-Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (template.includeTime) {
                    InfoBadge("🕐", cardColor)
                }
                if (template.showNights) {
                    InfoBadge("🌙", cardColor)
                }
            }
        }
    }
}

@Composable
private fun InfoBadge(
    emoji: String,
    color: Color
) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.2f),
        modifier = Modifier.size(28.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// Hilfsfunktion zum Erstellen eines Countdowns aus einer Vorlage
fun CountdownTemplate.toCountdown(): Countdown {
    val targetDate = LocalDateTime.now().plusDays(daysOffset)
    val targetDateTime = if (includeTime) {
        targetDate.withHour(12).withMinute(0)
    } else {
        targetDate.with(LocalTime.of(0, 0))
    }

    return Countdown(
        title = title,
        targetDateTime = targetDateTime,
        includeTime = includeTime,
        showNights = showNights,
        color = color
    )
}