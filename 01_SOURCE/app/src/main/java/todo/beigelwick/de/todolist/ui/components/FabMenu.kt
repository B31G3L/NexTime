package todo.beigelwick.de.todolist.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.ui.theme.AppPreferences
import todo.beigelwick.de.todolist.utils.HapticFeedback
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// ─── Expandable FAB ───────────────────────────────────────────────────────────

@Composable
fun ExpandableFab(
    onCreateCustom     : () -> Unit,
    onTemplateSelected : (Countdown) -> Unit
) {
    val context  = LocalContext.current
    val haptic   = remember { HapticFeedback(context) }
    var expanded by remember { mutableStateOf(false) }
    var showTemplateDialog by remember { mutableStateOf(false) }

    // BUG FIX: Default-Zeit aus AppPreferences lesen, damit toCountdown() sie
    // verwenden kann – statt der aktuellen Sekunde zur Laufzeit.
    val defaultTime by AppPreferences.getDefaultTime(context).collectAsState(initial = LocalTime.of(12, 0))

    val rotation by animateFloatAsState(
        targetValue   = if (expanded) 45f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "fab_rotation"
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter   = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(250, easing = FastOutSlowInEasing)),
            exit    = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { it / 2 }, animationSpec = tween(200))
        ) {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniFabItem(
                    label   = stringResource(R.string.fab_template),
                    icon    = Icons.Outlined.ContentCopy,
                    color   = MaterialTheme.colorScheme.primary,
                    onClick = { haptic.click(); expanded = false; showTemplateDialog = true }
                )
                MiniFabItem(
                    label   = stringResource(R.string.fab_custom),
                    icon    = Icons.Outlined.Edit,
                    color   = MaterialTheme.colorScheme.primary,
                    onClick = { haptic.click(); expanded = false; onCreateCustom() }
                )
            }
        }

        FloatingActionButton(
            onClick        = { haptic.click(); expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier       = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.Add,
                contentDescription = stringResource(R.string.fab_add),
                modifier           = Modifier.size(28.dp).rotate(rotation),
                tint               = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    if (showTemplateDialog) {
        TemplatePickerDialog(
            onDismiss          = { showTemplateDialog = false },
            onTemplateSelected = { template ->
                showTemplateDialog = false
                // defaultTime weitergeben statt aktuelle Uhrzeit zu verwenden
                onTemplateSelected(template.toCountdown(defaultTime))
            }
        )
    }
}

// ─── Mini FAB Item ────────────────────────────────────────────────────────────

@Composable
private fun MiniFabItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape           = RoundedCornerShape(20.dp),
            color           = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Text(
                text     = label,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style    = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color    = MaterialTheme.colorScheme.onSurface
            )
        }
        SmallFloatingActionButton(
            onClick        = onClick,
            containerColor = color,
            modifier       = Modifier.size(48.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

// ─── Template Picker Dialog ───────────────────────────────────────────────────

@Composable
private fun TemplatePickerDialog(
    onDismiss          : () -> Unit,
    onTemplateSelected : (CountdownTemplate) -> Unit
) {
    val context  = LocalContext.current
    val haptic   = remember { HapticFeedback(context) }
    var selectedCategory by remember { mutableStateOf<TemplateCategory?>(null) }

    val visibleTemplates = remember(selectedCategory) {
        if (selectedCategory == null) ALL_TEMPLATES
        else ALL_TEMPLATES.filter { it.category == selectedCategory }
    }

    Dialog(
        onDismissRequest = { haptic.tick(); onDismiss() },
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.82f),
            shape    = RoundedCornerShape(24.dp),
            color    = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = stringResource(R.string.template_picker_title),
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { haptic.tick(); onDismiss() }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }

                // Kategorie-Filter
                LazyRow(
                    modifier            = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick  = { haptic.tick(); selectedCategory = null },
                            label    = { Text(stringResource(R.string.template_cat_all)) }
                        )
                    }
                    items(TemplateCategory.values()) { category ->
                        val label = when (category) {
                            TemplateCategory.FEIERTAGE    -> stringResource(R.string.template_cat_holidays)
                            TemplateCategory.JAHRESZEITEN -> stringResource(R.string.template_cat_seasons)
                            TemplateCategory.SPORT        -> stringResource(R.string.template_cat_sports)
                            TemplateCategory.PERSOENLICH  -> stringResource(R.string.template_cat_personal)
                            TemplateCategory.SCHULE       -> stringResource(R.string.template_cat_school)
                        }
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick  = { haptic.tick(); selectedCategory = category },
                            label    = { Text(label) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Template-Liste
                LazyColumn(
                    modifier              = Modifier.fillMaxSize(),
                    contentPadding        = PaddingValues(16.dp),
                    verticalArrangement   = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedCategory == null) {
                        TemplateCategory.values().forEach { category ->
                            val categoryTemplates = visibleTemplates.filter { it.category == category }
                            if (categoryTemplates.isNotEmpty()) {
                                item(key = "header_${category.name}") {
                                    val label = when (category) {
                                        TemplateCategory.FEIERTAGE    -> stringResource(R.string.template_cat_holidays)
                                        TemplateCategory.JAHRESZEITEN -> stringResource(R.string.template_cat_seasons)
                                        TemplateCategory.SPORT        -> stringResource(R.string.template_cat_sports)
                                        TemplateCategory.PERSOENLICH  -> stringResource(R.string.template_cat_personal)
                                        TemplateCategory.SCHULE       -> stringResource(R.string.template_cat_school)
                                    }
                                    Text(
                                        text     = label,
                                        style    = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color    = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                                    )
                                }
                                items(items = categoryTemplates, key = { "${category.name}_${it.titleKey}" }) { template ->
                                    TemplateCard(
                                        template = template,
                                        onClick  = { haptic.click(); onTemplateSelected(template) }
                                    )
                                }
                            }
                        }
                    } else {
                        items(items = visibleTemplates, key = { it.titleKey }) { template ->
                            TemplateCard(
                                template = template,
                                onClick  = { haptic.click(); onTemplateSelected(template) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Template Card ────────────────────────────────────────────────────────────

@Composable
private fun TemplateCard(template: CountdownTemplate, onClick: () -> Unit) {
    val cardColor = try { Color(android.graphics.Color.parseColor(template.color)) }
    catch (e: Exception) { Color(0xFFFF7043) }
    val targetDate = remember { template.targetDateProvider() }
    val daysUntil  = remember { ChronoUnit.DAYS.between(LocalDate.now(), targetDate) }

    Card(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        colors   = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.08f)),
        shape    = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = template.icon, fontSize = 24.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = template.titleKey,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = targetDate.format(DateTimeFormatter.ofPattern("dd. MMMM yyyy")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = cardColor.copy(alpha = 0.15f)
            ) {
                Column(
                    modifier            = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text       = if (daysUntil >= 0) "$daysUntil" else "${-daysUntil}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = cardColor
                    )
                    Text(
                        text  = if (daysUntil >= 0) stringResource(R.string.template_days_label)
                        else stringResource(R.string.template_past_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = cardColor
                    )
                }
            }
        }
    }
}