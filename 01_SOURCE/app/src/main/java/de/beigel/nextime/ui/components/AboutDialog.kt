package de.beigel.nextime.ui.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.beigel.nextime.BuildConfig

// ─── Shared Composables (internal → nutzbar in MainScreenWithBottomNav) ───────

@Composable
internal fun DeveloperCard() {
    // Wird in AboutPageContent nicht mehr separat gebraucht
    // Bleibt für eventuelle andere Verwendung erhalten
}

@Composable
internal fun FeaturesCard() {
    // Bleibt für eventuelle andere Verwendung erhalten
}

// ─── About-Seite Hauptinhalt ──────────────────────────────────────────────────

@Composable
fun AboutPageContent() {
    val context = LocalContext.current

    val scrollState = androidx.compose.foundation.rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // App-Icon + Name + Version
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = "NexTime",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Kurzbeschreibung
        Text(
            text = "Behalte wichtige Momente im Blick —\nCountdowns & Count-ups für alles was zählt.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        // Solo-Entwickler Info
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "👨‍💻", fontSize = 18.sp)
                Text(
                    text = "Solo-Entwickler • Mit ❤️ und ☕ gebaut",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Kostenlos-Hinweis
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🎁", fontSize = 18.sp)
                Text(
                    text = "NexTime ist und bleibt für immer kostenlos",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Support-Aktionen
        Text(
            text = "Support",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Start)
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SupportButton(
                icon = Icons.Outlined.Star,
                iconColor = Color(0xFFFFC107),
                title = "App bewerten",
                subtitle = "Deine Bewertung hilft anderen Nutzern die App zu finden",
                onClick = { openPlayStore(context) }
            )
            SupportButton(
                icon = Icons.Outlined.Coffee,
                iconColor = Color(0xFF795548),
                title = "Buy me a Coffee",
                subtitle = "Als Solo-Entwickler freue ich mich über jede Unterstützung ☕",
                onClick = { openKofi(context) }
            )
            SupportButton(
                icon = Icons.Outlined.BugReport,
                iconColor = MaterialTheme.colorScheme.error,
                title = "Fehler melden",
                subtitle = "Etwas stimmt nicht? Schreib mir direkt — ich kümmere mich darum",
                onClick = { reportBug(context) }
            )
            SupportButton(
                icon = Icons.Outlined.Lightbulb,
                iconColor = Color(0xFF4CAF50),
                title = "Idee oder Wunsch",
                subtitle = "Du hast eine Idee für ein neues Feature? Ich freue mich über jede Nachricht!",
                onClick = { suggestFeature(context) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fußzeile
        Text(
            text = "Entwickelt mit ❤️ von Beigel",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SupportButton(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            }
        }
    }
}

// ─── Shared Helper Functions ──────────────────────────────────────────────────

internal fun openPlayStore(context: android.content.Context) {
    val packageName = context.packageName
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
    }
}

internal fun openKofi(context: android.content.Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/beigel")))
}

internal fun reportBug(context: android.content.Context) {
    val subject = "NexTime Bug Report - v${BuildConfig.VERSION_NAME}"
    val body = """
        Hallo,
        
        ich möchte einen Bug in NexTime melden:
        
        App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
        Android Version: ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})
        Gerät: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}
        
        Beschreibung:
        [Bitte hier beschreiben]
        
        Schritte zur Reproduktion:
        1. 
        2. 
        
        Erwartetes / Tatsächliches Verhalten:
        
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("beigel.dev@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Bug melden via..."))
    } catch (e: ActivityNotFoundException) {
        android.widget.Toast.makeText(context, "Keine E-Mail App gefunden", android.widget.Toast.LENGTH_SHORT).show()
    }
}

internal fun suggestFeature(context: android.content.Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("beigel.dev@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, "NexTime Feature-Idee")
        putExtra(Intent.EXTRA_TEXT, "Hallo,\n\nich habe eine Idee für NexTime:\n\n")
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Feature vorschlagen via..."))
    } catch (e: ActivityNotFoundException) {
        android.widget.Toast.makeText(context, "Keine E-Mail App gefunden", android.widget.Toast.LENGTH_SHORT).show()
    }
}