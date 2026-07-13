package todo.beigelwick.de.todolist.ui.screens.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.play.core.review.ReviewManagerFactory
import todo.beigelwick.de.todolist.R
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.FilterMode
import todo.beigelwick.de.todolist.data.model.calculateTimeRemaining
import todo.beigelwick.de.todolist.ui.components.CountdownCard
import todo.beigelwick.de.todolist.ui.components.CountdownCardDialog
import todo.beigelwick.de.todolist.ui.components.EmptyStateView
import todo.beigelwick.de.todolist.ui.components.ExpandableFab
import todo.beigelwick.de.todolist.ui.viewmodel.CountdownViewModel
import todo.beigelwick.de.todolist.ui.viewmodel.SortMode
import todo.beigelwick.de.todolist.utils.HapticFeedback
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToAddEdit  : () -> Unit,
    onNavigateToEdit     : (Long) -> Unit,
    onNavigateToSettings : () -> Unit,
    onNavigateToInfo     : () -> Unit,
    viewModel            : CountdownViewModel = viewModel()
) {
    val context     = LocalContext.current
    val haptic      = remember { HapticFeedback(context) }

    val countdowns  by viewModel.countdowns.collectAsState()
    val filterMode  by viewModel.filterMode.collectAsState()
    val sortMode    by viewModel.sortMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showSearch      by remember { mutableStateOf(false) }
    var showSortMenu    by remember { mutableStateOf(false) }
    var dialogCountdown by remember { mutableStateOf<Countdown?>(null) }

    LaunchedEffect(Unit) {
        viewModel.triggerReview.collect {
            launchReviewFlow(context)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (showSearch) {
                            SearchField(
                                query         = searchQuery,
                                onQueryChange = { viewModel.setSearchQuery(it) },
                                onClose       = { showSearch = false; viewModel.clearSearch() }
                            )
                        } else {
                            Text(stringResource(R.string.topbar_nextime))
                        }
                    },
                    actions = {
                        if (!showSearch) {
                            IconButton(onClick = { haptic.tick(); showSearch = true }) {
                                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_placeholder))
                            }
                            Box {
                                IconButton(onClick = { haptic.tick(); showSortMenu = true }) {
                                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.sort_date_asc))
                                }
                                SortDropdownMenu(
                                    expanded       = showSortMenu,
                                    currentSort    = sortMode,
                                    onSortSelected = { mode ->
                                        haptic.tick()
                                        viewModel.setSortMode(mode)
                                        showSortMenu = false
                                    },
                                    onDismiss = { showSortMenu = false }
                                )
                            }
                            IconButton(onClick = { haptic.tick(); onNavigateToSettings() }) {
                                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.nav_settings))
                            }
                            IconButton(onClick = { haptic.tick(); onNavigateToInfo() }) {
                                Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.nav_info))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
                AnimatedVisibility(visible = !showSearch) {
                    FilterChipRow(
                        currentMode    = filterMode,
                        onModeSelected = { mode -> haptic.tick(); viewModel.setFilterMode(mode) }
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !showSearch,
                enter   = scaleIn(animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                )) + fadeIn(),
                exit    = scaleOut(animationSpec = tween(200)) + fadeOut()
            ) {
                ExpandableFab(
                    onCreateCustom     = { onNavigateToAddEdit() },
                    onTemplateSelected = { countdown ->
                        viewModel.addCountdown(countdown)
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        MainListContent(
            countdowns       = countdowns,
            filterMode       = filterMode,
            searchQuery      = searchQuery,
            paddingValues    = paddingValues,
            onCountdownClick = { countdown -> haptic.tick(); dialogCountdown = countdown },
            onAddCountdown   = { onNavigateToAddEdit() }
        )
    }

    dialogCountdown?.let { countdown ->
        CountdownCardDialog(
            countdown = countdown,
            onDismiss = { dialogCountdown = null },
            onEdit    = { c -> dialogCountdown = null; onNavigateToEdit(c.id) },
            onDelete  = { c -> viewModel.deleteCountdown(c); dialogCountdown = null },
            onShare   = { c -> shareCountdown(context, c); dialogCountdown = null }
        )
    }
}

// ─── In-App Review starten ────────────────────────────────────────────────────

private fun launchReviewFlow(context: Context) {
    val activity = context as? Activity ?: return
    val manager  = ReviewManagerFactory.create(context)
    val request  = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            manager.launchReviewFlow(activity, reviewInfo)
        }
    }
}

// ─── Hauptliste ───────────────────────────────────────────────────────────────

@Composable
private fun MainListContent(
    countdowns       : List<Countdown>,
    filterMode       : FilterMode,
    searchQuery      : String,
    paddingValues    : PaddingValues,
    onCountdownClick : (Countdown) -> Unit,
    onAddCountdown   : () -> Unit
) {
    if (countdowns.isEmpty()) {
        EmptyStateView(
            modifier       = Modifier.padding(paddingValues),
            onAddCountdown = onAddCountdown
        )
        return
    }

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start  = 16.dp,
            end    = 16.dp,
            top    = paddingValues.calculateTopPadding() + 8.dp,
            bottom = paddingValues.calculateBottomPadding() + 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (searchQuery.isNotBlank() || filterMode != FilterMode.ALL) {
            items(items = countdowns, key = { it.id }) { countdown ->
                CountdownCardItem(countdown, onCountdownClick)
            }
        } else {
            val futureItems = countdowns.filter { !it.isCountUp }
            val pastItems   = countdowns.filter {  it.isCountUp }

            if (futureItems.isNotEmpty()) {
                item(key = "header_countdown") {
                    SectionHeader(label = stringResource(R.string.section_countdown), count = futureItems.size)
                }
                items(items = futureItems, key = { it.id }) { countdown ->
                    CountdownCardItem(countdown, onCountdownClick)
                }
            }
            if (pastItems.isNotEmpty()) {
                item(key = "header_countup") {
                    SectionHeader(label = stringResource(R.string.section_countup), count = pastItems.size)
                }
                items(items = pastItems, key = { it.id }) { countdown ->
                    CountdownCardItem(countdown, onCountdownClick)
                }
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(label: String, count: Int) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Text(
                text       = "$count",
                modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style      = MaterialTheme.typography.labelSmall,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}

// ─── Card Item ────────────────────────────────────────────────────────────────

@Composable
private fun CountdownCardItem(
    countdown        : Countdown,
    onCountdownClick : (Countdown) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().clickable { onCountdownClick(countdown) }) {
        CountdownCard(countdown = countdown)
    }
}

// ─── Filter Chips ─────────────────────────────────────────────────────────────

@Composable
private fun FilterChipRow(currentMode: FilterMode, onModeSelected: (FilterMode) -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = currentMode == FilterMode.ALL,       onClick = { onModeSelected(FilterMode.ALL) },       label = { Text(stringResource(R.string.filter_all)) })
        FilterChip(selected = currentMode == FilterMode.COUNTDOWN, onClick = { onModeSelected(FilterMode.COUNTDOWN) }, label = { Text(stringResource(R.string.filter_countdown)) })
        FilterChip(selected = currentMode == FilterMode.COUNTUP,   onClick = { onModeSelected(FilterMode.COUNTUP) },   label = { Text(stringResource(R.string.filter_countup)) })
    }
}

// ─── Sort Dropdown ────────────────────────────────────────────────────────────

@Composable
private fun SortDropdownMenu(
    expanded: Boolean, currentSort: SortMode,
    onSortSelected: (SortMode) -> Unit, onDismiss: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        listOf(
            SortMode.DATE_ASC   to Pair(Icons.Outlined.CalendarToday, stringResource(R.string.sort_date_asc)),
            SortMode.DATE_DESC  to Pair(Icons.Outlined.CalendarToday, stringResource(R.string.sort_date_desc)),
            SortMode.TITLE_ASC  to Pair(Icons.Outlined.SortByAlpha,   stringResource(R.string.sort_title_asc)),
            SortMode.TITLE_DESC to Pair(Icons.Outlined.SortByAlpha,   stringResource(R.string.sort_title_desc)),
            SortMode.CREATED    to Pair(Icons.Outlined.AccessTime,     stringResource(R.string.sort_created))
        ).forEach { (mode, iconAndLabel) ->
            val (icon, label) = iconAndLabel
            DropdownMenuItem(
                leadingIcon = {
                    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp),
                        tint = if (currentSort == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                },
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = label, fontWeight = if (currentSort == mode) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (currentSort == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        if (currentSort == mode) {
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                onClick = { onSortSelected(mode) }
            )
        }
    }
}

// ─── Suchfeld ─────────────────────────────────────────────────────────────────

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val focusManager   = LocalFocusManager.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        placeholder   = { Text(stringResource(R.string.search_placeholder)) },
        modifier      = Modifier.fillMaxWidth().focusRequester(focusRequester),
        singleLine    = true,
        trailingIcon  = {
            IconButton(onClick = { focusManager.clearFocus(); onClose() }) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor   = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedBorderColor      = Color.Transparent,
            unfocusedBorderColor    = Color.Transparent
        )
    )
}

// ─── Share ────────────────────────────────────────────────────────────────────

private fun shareCountdown(context: Context, countdown: Countdown) {
    val locale    = Locale.getDefault()
    val timeInfo  = countdown.calculateTimeRemaining()
    val dateStr   = countdown.effectiveTarget.format(
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    )
    val shareText = buildString {
        append("${countdown.title}\n\n")
        if (timeInfo.isPast) append(context.getString(R.string.share_days_ago, timeInfo.days))
        else                 append(context.getString(R.string.share_days_remaining, timeInfo.days))
        append("\n\n$dateStr")
        append("\n\n${context.getString(R.string.share_created_with)}")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)))
}