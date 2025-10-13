package de.beigel.nextime.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.beigel.nextime.BuildConfig
import de.beigel.nextime.ui.theme.DesignSystem
import de.beigel.nextime.utils.HapticFeedback

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptic = remember { HapticFeedback(context) }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = {
            haptic.tick()
            onDismiss()
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⏰",
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xSmall))
                Text(
                    text = "NexTime",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium)
            ) {
                // Entwickler-Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(DesignSystem.Spacing.medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👨‍💻",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(DesignSystem.Spacing.xSmall))
                        Text(
                            text = "Entwickelt mit ❤️",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "von einem Solo-Entwickler",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Bewertung
                ActionCard(
                    icon = "⭐",
                    title = "App bewerten",
                    description = "Unterstütze die Entwicklung mit deiner Bewertung im Play Store",
                    onClick = {
                        haptic.click()
                        openPlayStore(context)
                    }
                )

                // Ko-fi Support
                ActionCard(
                    icon = "☕",
                    title = "Kaffee spendieren",
                    description = "Unterstütze die Entwicklung mit einer kleinen Spende auf Ko-fi",
                    onClick = {
                        haptic.click()
                        openKofi(context)
                    }
                )

                // Bug melden
                ActionCard(
                    icon = "🐛",
                    title = "Bug melden",
                    description = "Hilf mir, die App zu verbessern - melde Fehler per E-Mail",
                    onClick = {
                        haptic.click()
                        reportBug(context)
                    }
                )

                Divider(modifier = Modifier.padding(vertical = DesignSystem.Spacing.xSmall))

                // Info-Bereich
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.small)
                ) {
                    InfoRow(
                        icon = Icons.Default.Build,
                        label = "Build",
                        value = "${BuildConfig.VERSION_CODE}"
                    )
                    InfoRow(
                        icon = Icons.Default.Info,
                        label = "Package",
                        value = "de.beigel.nextime"
                    )
                }

                // Danke-Text
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(DesignSystem.CornerRadius.medium)
                ) {
                    Text(
                        text = "Vielen Dank, dass du NexTime verwendest! 🙏",
                        modifier = Modifier.padding(DesignSystem.Spacing.medium),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                haptic.click()
                onDismiss()
            }) {
                Text("Schließen")
            }
        }
    )
}

@Composable
private fun ActionCard(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.medium))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(DesignSystem.Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(DesignSystem.CornerRadius.small))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // Text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(DesignSystem.Spacing.xxSmall))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(DesignSystem.Icon.small),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

// Hilfsfunktionen
private fun openPlayStore(context: android.content.Context) {
    val packageName = context.packageName
    try {
        // Versuche Play Store App zu öffnen
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Fallback: Browser öffnen
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        context.startActivity(intent)
    }
}

private fun openKofi(context: android.content.Context) {
    // Ersetze "deinusername" mit deinem echten Ko-fi Username
    val kofiUrl = "https://ko-fi.com/beigel"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(kofiUrl))
    context.startActivity(intent)
}

private fun reportBug(context: android.content.Context) {
    val email = "deine.email@example.com" // Ersetze mit deiner echten E-Mail
    val subject = "NexTime Bug Report - v${BuildConfig.VERSION_NAME}"
    val body = """
        Hallo,
        
        ich möchte einen Bug in NexTime melden:
        
        App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
        Android Version: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})
        Gerät: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
        
        Beschreibung des Problems:
        [Bitte beschreibe hier den Fehler]
        
        Schritte zur Reproduktion:
        1. 
        2. 
        3. 
        
        Erwartetes Verhalten:
        [Was sollte passieren?]
        
        Tatsächliches Verhalten:
        [Was passiert stattdessen?]
        
        Zusätzliche Informationen:
        [Screenshots, weitere Details...]
        
        Vielen Dank!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Bug melden via..."))
    } catch (e: ActivityNotFoundException) {
        // Fallback wenn keine E-Mail App installiert ist
        android.widget.Toast.makeText(
            context,
            "Keine E-Mail App gefunden",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}