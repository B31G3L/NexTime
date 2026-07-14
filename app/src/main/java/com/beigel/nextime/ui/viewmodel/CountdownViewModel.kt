package com.beigel.nextime.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.beigel.nextime.ui.theme.dataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import com.beigel.nextime.data.database.CountdownDatabase
import com.beigel.nextime.data.model.Countdown
import com.beigel.nextime.data.model.FilterMode
import com.beigel.nextime.data.repository.CountdownRepository
import com.beigel.nextime.notifications.NotificationScheduler
import com.beigel.nextime.ui.theme.dataStore
import com.beigel.nextime.widget.WidgetUpdateWorker

// ─── Sortiermodi ──────────────────────────────────────────────────────────────

enum class SortMode {
    DATE_ASC,
    DATE_DESC,
    TITLE_ASC,
    TITLE_DESC,
    CREATED
}

// ─── Review-Schwellenwerte ────────────────────────────────────────────────────
// Bei diesen Gesamtzahlen erstellter Countdowns wird der Review-Dialog angefragt.

private val REVIEW_THRESHOLDS = setOf(3, 10)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val context    = application.applicationContext
    private val repository : com.beigel.nextime.data.repository.CountdownRepository

    // DataStore-Key für den Zähler gesamt erstellter Countdowns
    private val COUNTDOWN_CREATE_COUNT = intPreferencesKey("countdown_create_count")

    private val _allCountdowns = MutableStateFlow<List<com.beigel.nextime.data.model.Countdown>>(emptyList())

    private val _filterMode  = MutableStateFlow(_root_ide_package_.com.beigel.nextime.data.model.FilterMode.ALL)
    val filterMode: StateFlow<com.beigel.nextime.data.model.FilterMode> = _filterMode.asStateFlow()

    private val _sortMode    = MutableStateFlow(SortMode.DATE_ASC)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _countdowns  = MutableStateFlow<List<com.beigel.nextime.data.model.Countdown>>(emptyList())
    val countdowns: StateFlow<List<com.beigel.nextime.data.model.Countdown>> = _countdowns.asStateFlow()

    private val _selectedCountdown = MutableStateFlow<com.beigel.nextime.data.model.Countdown?>(null)
    val selectedCountdown: StateFlow<com.beigel.nextime.data.model.Countdown?> = _selectedCountdown.asStateFlow()

    private val _tickSeconds = MutableStateFlow(0L)
    val tickSeconds: StateFlow<Long> = _tickSeconds.asStateFlow()

    private val _tickMinutes = MutableStateFlow(0L)
    val tickMinutes: StateFlow<Long> = _tickMinutes.asStateFlow()

    // ── Review-Event (SharedFlow, einmalig konsumiert) ────────────────────────
    // Die UI lauscht darauf und startet bei Emission den ReviewFlow.
    private val _triggerReview = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val triggerReview: SharedFlow<Unit> = _triggerReview.asSharedFlow()

    init {
        val database = _root_ide_package_.com.beigel.nextime.data.database.CountdownDatabase.Companion.getDatabase(application)
        repository   =
            _root_ide_package_.com.beigel.nextime.data.repository.CountdownRepository(database.countdownDao())

        viewModelScope.launch {
            repository.allCountdowns.collect { list ->
                _allCountdowns.value = list
            }
        }

        viewModelScope.launch {
            combine(
                _allCountdowns,
                _filterMode,
                _sortMode,
                _searchQuery
            ) { all, filter, sort, query ->
                applyFilterSortSearch(all, filter, sort, query)
            }.collect { result ->
                _countdowns.value = result
                _root_ide_package_.com.beigel.nextime.widget.WidgetUpdateWorker.Companion.updateNow(context)
            }
        }

        viewModelScope.launch {
            var tick = 0L
            while (true) {
                delay(1_000L)
                tick++
                _tickSeconds.value = tick
                if (tick % 60L == 0L) {
                    _tickMinutes.value = tick / 60L
                }
            }
        }
    }

    // ─── Filter / Sort / Suche ────────────────────────────────────────────────

    fun setFilterMode(mode: com.beigel.nextime.data.model.FilterMode)  { _filterMode.value  = mode }
    fun setSortMode(mode: SortMode)      { _sortMode.value    = mode }
    fun setSearchQuery(query: String)    { _searchQuery.value = query }
    fun clearSearch()                    { _searchQuery.value = "" }

    private fun applyFilterSortSearch(
        list   : List<com.beigel.nextime.data.model.Countdown>,
        filter : com.beigel.nextime.data.model.FilterMode,
        sort   : SortMode,
        query  : String
    ): List<com.beigel.nextime.data.model.Countdown> {
        val searched = if (query.isBlank()) list
        else list.filter { it.title.contains(query.trim(), ignoreCase = true) }

        val filtered = when (filter) {
            _root_ide_package_.com.beigel.nextime.data.model.FilterMode.COUNTDOWN -> searched.filter { !it.isCountUp }
            _root_ide_package_.com.beigel.nextime.data.model.FilterMode.COUNTUP   -> searched.filter {  it.isCountUp }
            _root_ide_package_.com.beigel.nextime.data.model.FilterMode.ALL       -> searched
        }

        val sorted = when (sort) {
            SortMode.DATE_ASC   -> filtered.sortedBy          { it.effectiveTarget }
            SortMode.DATE_DESC  -> filtered.sortedByDescending { it.effectiveTarget }
            SortMode.TITLE_ASC  -> filtered.sortedBy          { it.title.lowercase() }
            SortMode.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortMode.CREATED    -> filtered.sortedByDescending { it.createdAt }
        }

        val pinned   = sorted.filter {  it.isPinned }
        val unpinned = sorted.filter { !it.isPinned }

        return if (filter == _root_ide_package_.com.beigel.nextime.data.model.FilterMode.ALL && query.isBlank()) {
            val pinnedFuture   = pinned.filter   { !it.isCountUp }
            val pinnedPast     = pinned.filter   {  it.isCountUp }
            val unpinnedFuture = unpinned.filter { !it.isCountUp }
            val unpinnedPast   = unpinned.filter {  it.isCountUp }
            pinnedFuture + pinnedPast + unpinnedFuture + unpinnedPast
        } else {
            pinned + unpinned
        }
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    fun addCountdown(countdown: com.beigel.nextime.data.model.Countdown) {
        viewModelScope.launch {
            val id    = repository.insertCountdown(countdown)
            val saved = repository.getCountdownById(id)
            saved?.let { _root_ide_package_.com.beigel.nextime.notifications.NotificationScheduler.scheduleNotifications(context, it) }
            _root_ide_package_.com.beigel.nextime.widget.WidgetUpdateWorker.Companion.updateNow(context)

            // ── Review-Zähler erhöhen und ggf. Dialog anfragen ────────────────
            checkAndTriggerReview()
        }
    }

    /**
     * Erhöht den Erstellungs-Zähler im DataStore.
     * Bei Erreichen eines Schwellenwerts wird ein Review-Event gesendet.
     */
    private suspend fun checkAndTriggerReview() {
        val currentCount = context.dataStore.data
            .map { it[COUNTDOWN_CREATE_COUNT] ?: 0 }
            .first()

        val newCount = currentCount + 1

        context.dataStore.edit { it[COUNTDOWN_CREATE_COUNT] = newCount }

        if (newCount in REVIEW_THRESHOLDS) {
            _triggerReview.emit(Unit)
        }
    }

    fun updateCountdown(countdown: com.beigel.nextime.data.model.Countdown) {
        viewModelScope.launch {
            repository.updateCountdown(countdown)
            _root_ide_package_.com.beigel.nextime.notifications.NotificationScheduler.cancelAllNotifications(context, countdown)
            _root_ide_package_.com.beigel.nextime.notifications.NotificationScheduler.scheduleNotifications(context, countdown)
            _root_ide_package_.com.beigel.nextime.widget.WidgetUpdateWorker.Companion.updateNow(context)
        }
    }

    fun deleteCountdown(countdown: com.beigel.nextime.data.model.Countdown) {
        viewModelScope.launch {
            _root_ide_package_.com.beigel.nextime.notifications.NotificationScheduler.cancelAllNotifications(context, countdown)
            repository.deleteCountdown(countdown)
            _root_ide_package_.com.beigel.nextime.widget.WidgetUpdateWorker.Companion.updateNow(context)
        }
    }

    fun togglePin(countdown: com.beigel.nextime.data.model.Countdown) {
        viewModelScope.launch {
            repository.updateCountdown(countdown.copy(isPinned = !countdown.isPinned))
        }
    }

    // ─── Einzelner Countdown ──────────────────────────────────────────────────

    fun selectCountdown(countdown: com.beigel.nextime.data.model.Countdown?) {
        _selectedCountdown.value = countdown
    }

    suspend fun getCountdownById(id: Long): com.beigel.nextime.data.model.Countdown? =
        repository.getCountdownById(id)
}