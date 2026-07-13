package todo.beigelwick.de.todolist.ui.screens.welcome

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import todo.beigelwick.de.todolist.BuildConfig
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.ui.theme.dataStore
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ArrowForward

// ─── Preference Key ───────────────────────────────────────────────────────────

private val WELCOME_SEEN = booleanPreferencesKey("welcome_seen")

/** Liefert, ob der Welcome-Screen bereits gesehen wurde (für die Navigation). */
fun isWelcomeSeen(context: Context): Flow<Boolean> =
    context.dataStore.data.map { it[WELCOME_SEEN] ?: false }

suspend fun markWelcomeSeen(context: Context) {
    context.dataStore.edit { it[WELCOME_SEEN] = true }
}

// ─── Feature-Eintrag ─────────────────────────────────────────────────────────

private data class Feature(
    val icon        : ImageVector,
    @StringRes val titleRes    : Int,
    @StringRes val subtitleRes : Int
)

private val FEATURES = listOf(
    Feature(Icons.Outlined.Timer,         R.string.welcome_feat_countdown_title, R.string.welcome_feat_countdown_sub),
    Feature(Icons.Outlined.Palette,       R.string.welcome_feat_themes_title,    R.string.welcome_feat_themes_sub),
    Feature(Icons.Outlined.Notifications, R.string.welcome_feat_reminders_title, R.string.welcome_feat_reminders_sub),
    Feature(Icons.Outlined.Widgets,       R.string.welcome_feat_widget_title,    R.string.welcome_feat_widget_sub),
    Feature(Icons.Outlined.Tune,          R.string.welcome_feat_format_title,    R.string.welcome_feat_format_sub),
    Feature(Icons.Outlined.PushPin,       R.string.welcome_feat_pin_title,       R.string.welcome_feat_pin_sub),
)

// ─── WelcomeScreen ────────────────────────────────────────────────────────────

@Composable
fun WelcomeScreen(onDone: () -> Unit) {
    val context     = LocalContext.current
    val scope       = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val primary     = MaterialTheme.colorScheme.primary

    // Puls-Animation für das App-Icon
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.06f,
        animationSpec  = infiniteRepeatable(tween(1200, easing = EaseInOut), RepeatMode.Reverse),
        label          = "icon_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))

        // ── App-Icon ──────────────────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .size(96.dp)
                .scale(scale)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        listOf(primary, primary.copy(red = (primary.red * 0.7f).coerceIn(0f, 1f)))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Outlined.Timer,
                contentDescription = null,
                tint               = Color.White,
                modifier           = Modifier.size(52.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text       = stringResource(R.string.welcome_title),
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = stringResource(R.string.about_tagline),
            style     = MaterialTheme.typography.bodyMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(40.dp))

        // ── Features ──────────────────────────────────────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(20.dp),
            color    = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(
                modifier            = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                FEATURES.forEachIndexed { index, feature ->
                    FeatureRow(feature = feature)
                    if (index < FEATURES.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // ── CTA Button ────────────────────────────────────────────────────────
        Button(
            onClick  = {
                scope.launch {
                    markWelcomeSeen(context)
                    onDone()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = stringResource(R.string.welcome_cta),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector        = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Footer ────────────────────────────────────────────────────────────
        Text(
            text      = stringResource(R.string.welcome_footer, BuildConfig.VERSION_NAME),
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ─── Feature Row ─────────────────────────────────────────────────────────────

@Composable
private fun FeatureRow(feature: Feature) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape    = RoundedCornerShape(12.dp),
            color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector        = feature.icon,
                    contentDescription = null,
                    tint               = MaterialTheme.colorScheme.primary,
                    modifier           = Modifier.size(22.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = stringResource(feature.titleRes),
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = stringResource(feature.subtitleRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}