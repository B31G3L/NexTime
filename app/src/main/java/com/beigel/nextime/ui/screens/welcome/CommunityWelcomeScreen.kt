package com.beigel.nextime.ui.screens.welcome

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.beigel.nextime.ui.theme.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.beigel.nextime.R
import com.beigel.nextime.ui.theme.dataStore

// ─── Preference Key ───────────────────────────────────────────────────────────

private val COMMUNITY_SEEN = booleanPreferencesKey("community_seen")

/** Liefert, ob der Community-Screen bereits gesehen wurde (für die Navigation). */
fun isCommunitySeen(context: Context): Flow<Boolean> =
    context.dataStore.data.map { it[COMMUNITY_SEEN] ?: false }

suspend fun markCommunitySeen(context: Context) {
    context.dataStore.edit { it[COMMUNITY_SEEN] = true }
}

private fun openDiscord(context: Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/ww9DEnFUJp")))
}

// ─── CommunityWelcomeScreen ───────────────────────────────────────────────────

/**
 * Zweiter Onboarding-Screen, direkt nach dem WelcomeScreen. Wird nur einmal
 * gezeigt (siehe isCommunitySeen) und stellt kurz den Discord-Server vor.
 * "Beitreten" öffnet Discord, "Vielleicht später" überspringt – beides führt
 * danach direkt in die App.
 */
@Composable
fun CommunityWelcomeScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.Groups,
                contentDescription = null,
                modifier           = Modifier.size(40.dp),
                tint               = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text       = stringResource(R.string.community_title),
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text      = stringResource(R.string.community_subtitle),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier  = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                openDiscord(context)
                scope.launch {
                    markCommunitySeen(context)
                    onDone()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.Groups, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.community_cta), fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(
            onClick = {
                scope.launch {
                    markCommunitySeen(context)
                    onDone()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.community_skip))
        }

        Spacer(Modifier.weight(1.4f))
    }
}