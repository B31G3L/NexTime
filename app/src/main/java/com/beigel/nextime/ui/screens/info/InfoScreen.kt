package com.beigel.nextime.ui.screens.info

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beigel.nextime.BuildConfig
import com.beigel.nextime.R
import androidx.compose.material.icons.automirrored.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(onBack: () -> Unit) {
    val context     = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.topbar_info)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier              = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.spacedBy(24.dp)
        ) {
            // ── App-Name & Version ────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text       = stringResource(R.string.app_name),
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text     = "v${BuildConfig.VERSION_NAME}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style    = MaterialTheme.typography.labelMedium,
                        color    = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // ── Tagline ───────────────────────────────────────────────────────
            Text(
                text      = stringResource(R.string.about_tagline),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            // ── Solo Dev ──────────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "👨\u200d💻", fontSize = 18.sp)
                    Text(
                        text  = stringResource(R.string.about_solo_dev),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Kostenlos ─────────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier          = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text       = stringResource(R.string.about_free),
                        style      = MaterialTheme.typography.bodySmall,
                        color      = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ── Support ───────────────────────────────────────────────────────
            Text(
                text       = stringResource(R.string.about_support_title),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier.align(Alignment.Start)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SupportButton(
                    icon     = Icons.Outlined.Groups,
                    iconColor = Color(0xFF5865F2),
                    title    = stringResource(R.string.support_discord),
                    subtitle = stringResource(R.string.about_discord_subtitle),
                    onClick  = { openDiscord(context) }
                )
                SupportButton(
                    icon     = Icons.Outlined.Star,
                    iconColor = Color(0xFFFFC107),
                    title    = stringResource(R.string.support_rate),
                    subtitle = stringResource(R.string.about_rate_subtitle),
                    onClick  = { openPlayStore(context) }
                )
                SupportButton(
                    icon     = Icons.Outlined.Coffee,
                    iconColor = Color(0xFF795548),
                    title    = stringResource(R.string.support_coffee),
                    subtitle = stringResource(R.string.about_coffee_subtitle),
                    onClick  = { openKofi(context) }
                )
                SupportButton(
                    icon     = Icons.Outlined.BugReport,
                    iconColor = MaterialTheme.colorScheme.error,
                    title    = stringResource(R.string.support_bug),
                    subtitle = stringResource(R.string.about_bug_subtitle),
                    onClick  = { reportBug(context) }
                )
                SupportButton(
                    icon     = Icons.Outlined.Lightbulb,
                    iconColor = Color(0xFF4CAF50),
                    title    = stringResource(R.string.support_feature),
                    subtitle = stringResource(R.string.about_feature_subtitle),
                    onClick  = { suggestFeature(context) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Footer ────────────────────────────────────────────────────────
            Text(
                text      = stringResource(R.string.about_footer),
                style     = MaterialTheme.typography.bodySmall,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─── Support Button ───────────────────────────────────────────────────────────

@Composable
private fun SupportButton(
    icon      : ImageVector,
    iconColor : Color,
    title     : String,
    subtitle  : String,
    onClick   : () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onClick  = onClick
    ) {
        Row(
            modifier              = Modifier.padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape    = RoundedCornerShape(10.dp),
                color    = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = title,    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall,  color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
            }
        }
    }
}

// ─── Aktionen ─────────────────────────────────────────────────────────────────

private fun openPlayStore(context: Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
    }
}

private fun openKofi(context: Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("ko-fi.com/beigelapps")))
}

private fun openDiscord(context: Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/ww9DEnFUJp")))
}

private fun reportBug(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("beigel.dev@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, "NexTime Bug Report - v${BuildConfig.VERSION_NAME}")
        putExtra(Intent.EXTRA_TEXT, """
Hallo,

ich möchte einen Bug in NexTime melden:

App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
Gerät: ${Build.MANUFACTURER} ${Build.MODEL}

Beschreibung:
[Bitte hier beschreiben]

Schritte zur Reproduktion:
1. 
2. 

Erwartetes / Tatsächliches Verhalten:

        """.trimIndent())
    }
    try {
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.support_bug)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.no_email_app), Toast.LENGTH_SHORT).show()
    }
}

private fun suggestFeature(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("beigel.dev@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, "NexTime Feature-Idee")
        putExtra(Intent.EXTRA_TEXT, "Hallo,\n\nich habe eine Idee für NexTime:\n\n")
    }
    try {
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.support_feature)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.no_email_app), Toast.LENGTH_SHORT).show()
    }
}