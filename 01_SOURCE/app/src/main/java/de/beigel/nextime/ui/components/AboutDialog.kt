package de.beigel.nextime.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    var selectedTab by remember { mutableStateOf(0) }

    Dialog(
        onDismissRequest = {
            haptic.tick()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header mit Gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    // Close Button
                    IconButton(
                        onClick = {
                            haptic.tick()
                            onDismiss()
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // App Icon mit Gradient-Hintergrund
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = "⏰",
                                    fontSize = 40.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "NexTime",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Build ${BuildConfig.VERSION_CODE}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            haptic.tick()
                            selectedTab = 0
                        },
                        text = { Text("Info") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            haptic.tick()
                            selectedTab = 1
                        },
                        text = { Text("Support") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = {
                            haptic.tick()
                            selectedTab = 2
                        },
                        text = { Text("Details") }
                    )
                }

                // Content basierend auf Tab
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(24.dp)
                        .animateContentSize()
                ) {
                    when (selectedTab) {
                        0 -> InfoTab(haptic)
                        1 -> SupportTab(haptic, context)
                        2 -> DetailsTab()
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoTab(haptic: HapticFeedback) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Entwickler Card
        DeveloperCard()

        // Features
        FeaturesCard()

    }
}

@Composable
private fun SupportTab(haptic: HapticFeedback, context: android.content.Context) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Bewertung
        SupportActionCard(
            icon = Icons.Outlined.Star,
            iconColor = Color(0xFFFFC107),
            title = "App bewerten",
            description = "Unterstütze die Weiterentwicklung mit einer Bewertung",
            actionText = "Zum Play Store",
            onClick = {
                haptic.click()
                openPlayStore(context)
            }
        )

        // Ko-fi Support
        SupportActionCard(
            icon = Icons.Outlined.Coffee,
            iconColor = Color(0xFF5C4033),
            title = "Buy me a Coffee",
            description = "Unterstütze die Entwicklung mit einer kleinen Spende",
            actionText = "Zu Ko-fi",
            onClick = {
                haptic.click()
                openKofi(context)
            }
        )

        // Bug Report
        SupportActionCard(
            icon = Icons.Outlined.BugReport,
            iconColor = MaterialTheme.colorScheme.error,
            title = "Fehler melden",
            description = "Hilf dabei, die App noch besser zu machen",
            actionText = "E-Mail senden",
            onClick = {
                haptic.click()
                reportBug(context)
            }
        )

        // Feature Request
        SupportActionCard(
            icon = Icons.Outlined.Lightbulb,
            iconColor = Color(0xFF4CAF50),
            title = "Feature vorschlagen",
            description = "Hast du eine Idee für eine neue Funktion?",
            actionText = "Idee teilen",
            onClick = {
                haptic.click()
                suggestFeature(context)
            }
        )
    }
}

@Composable
private fun DetailsTab() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Technische Details
        TechnicalDetailsCard()

        // Berechtigungen
        PermissionsCard()

        // Credits
        CreditsCard()
    }
}

@Composable
private fun DeveloperCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "👨‍💻",
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Entwickelt mit ❤️ und ☕",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "von einem leidenschaftlichen Solo-Entwickler",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"Zeit ist kostbar - behalte sie im Blick\"",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FeaturesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Features",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            FeatureItem(Icons.Outlined.Timer, "Präzise Countdowns bis zur Sekunde")
            FeatureItem(Icons.Outlined.Palette, "Individuelle Farben für jeden Countdown")
            FeatureItem(Icons.Outlined.ViewCarousel, "7 verschiedene Anzeigeformate")
            FeatureItem(Icons.Outlined.Notifications, "Flexible Erinnerungen")
            FeatureItem(Icons.Outlined.Widgets, "Widget für den Homescreen")
            FeatureItem(Icons.Outlined.DarkMode, "Dark Mode Support")
            FeatureItem(Icons.Outlined.Share, "Teile deine Countdowns")
        }
    }
}

@Composable
private fun SupportActionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    actionText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconColor.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TechnicalDetailsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Technische Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            DetailRow("App Version", BuildConfig.VERSION_NAME)
            DetailRow("Build Nummer", "${BuildConfig.VERSION_CODE}")
        }
    }
}

@Composable
private fun PermissionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Berechtigungen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            PermissionItem(
                Icons.Outlined.Notifications,
                "Benachrichtigungen",
                "Für Countdown-Erinnerungen"
            )
            PermissionItem(
                Icons.Outlined.Vibration,
                "Vibration",
                "Für haptisches Feedback"
            )
            PermissionItem(
                Icons.Outlined.Widgets,
                "Widget-Aktualisierung",
                "Für Homescreen-Widgets"
            )
        }
    }
}

@Composable
private fun CreditsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Credits & Danksagung",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Ein herzliches Dankeschön an:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            CreditItem(
                "☕",
                "Alle Unterstützer",
                "Die diese App möglich machen"
            )
            CreditItem(
                "💡",
                "Die Community",
                "Für Feedback und Ideen"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Abschluss-Nachricht
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Danke, dass du NexTime verwendest! 🙏\nJeder Countdown zählt.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PermissionItem(icon: ImageVector, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CreditItem(emoji: String, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Hilfsfunktionen
private fun openPlayStore(context: android.content.Context) {
    val packageName = context.packageName
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        context.startActivity(intent)
    }
}

private fun openKofi(context: android.content.Context) {
    val kofiUrl = "https://ko-fi.com/beigel"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(kofiUrl))
    context.startActivity(intent)
}

private fun reportBug(context: android.content.Context) {
    val email = "beigel.dev@gmail.com"
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
        android.widget.Toast.makeText(
            context,
            "Keine E-Mail App gefunden",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}

private fun suggestFeature(context: android.content.Context) {
    val email = "beigel.dev@gmail.com"
    val subject = "NexTime Feature-Idee"
    val body = """
        Hallo,
        
        ich habe eine Idee für NexTime:
        
        Feature-Beschreibung:
        [Beschreibe deine Idee]
        
        Warum wäre das nützlich?
        [Erkläre den Nutzen]
        
        Wie stellst du dir die Umsetzung vor?
        [Optional: Deine Vorstellung der Implementierung]
        
        App Version: ${BuildConfig.VERSION_NAME}
        
        Vielen Dank für die tolle App!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Feature vorschlagen via..."))
    } catch (e: ActivityNotFoundException) {
        android.widget.Toast.makeText(
            context,
            "Keine E-Mail App gefunden",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}