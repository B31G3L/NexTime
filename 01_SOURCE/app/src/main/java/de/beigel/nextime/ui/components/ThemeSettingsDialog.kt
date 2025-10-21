package de.beigel.nextime.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.beigel.nextime.ui.theme.*
import de.beigel.nextime.utils.HapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsDialog(
    onDismiss: () -> Unit,
    currentTheme: CustomTheme,
    onThemeChanged: (CustomTheme) -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedTheme by remember { mutableStateOf(currentTheme) }

    AlertDialog(
        onDismissRequest = {
            haptic.tick()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Theme auswählen")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Beschreibung
                Text(
                    text = "Wähle eines der vordefinierten Farbschemas für deine App",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Theme Options
                CustomTheme.values().forEach { theme ->
                    ThemeOptionCard(
                        theme = theme,
                        isSelected = selectedTheme == theme,
                        onClick = {
                            haptic.tick()
                            selectedTheme = theme
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.click()
                    scope.launch {
                        CustomThemePreferences.setCustomTheme(context, selectedTheme)
                        onThemeChanged(selectedTheme)  // ← Callback für sofortiges Update
                        onDismiss()
                    }
                    onDismiss()
                }
            ) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic.tick()
                onDismiss()
            }) {
                Text("Abbrechen")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun ThemeOptionCard(
    theme: CustomTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val themeConfig = getThemeConfig(theme)
    val lightColor = themeConfig.lightColorScheme.primary
    val darkColor = themeConfig.darkColorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        else
            null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header mit Icon und Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = themeConfig.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (theme) {
                            CustomTheme.PLANIT -> "Klassisches Teal Design"
                            CustomTheme.NEXTIME -> "Warmes Orange Design"
                            CustomTheme.LEETSPEAK -> "Elegantes Violett Design"
                            CustomTheme.DAILYLIST -> "Frisches Grün Design"
                            CustomTheme.UNKNOWN -> "Kräftiges Rot Design"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Selection Indicator
                if (isSelected) {
                    Surface(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                "✓",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Farb-Vorschau
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Light Theme Preview
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    color = lightColor
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            "Hell",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Dark Theme Preview
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    color = darkColor
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            "Dunkel",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Primary Color
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    color = lightColor
                ) {}

                // Secondary Color
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp)),
                    color = themeConfig.lightColorScheme.secondary
                ) {}
            }

            // Emoji Icon für Theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (theme) {
                        CustomTheme.PLANIT -> "🌊"
                        CustomTheme.NEXTIME -> "🔥"
                        CustomTheme.LEETSPEAK -> "💜"
                        CustomTheme.DAILYLIST -> "🌿"
                        CustomTheme.UNKNOWN -> "❓"
                    },
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = when (theme) {
                        CustomTheme.PLANIT -> "Ozean & Wasser"
                        CustomTheme.NEXTIME -> "Sonne & Wärme"
                        CustomTheme.LEETSPEAK -> "Magie & Geheimnis"
                        CustomTheme.DAILYLIST -> "Natur & Wachstum"
                        CustomTheme.UNKNOWN -> "Abenteuer & Rätsel"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}