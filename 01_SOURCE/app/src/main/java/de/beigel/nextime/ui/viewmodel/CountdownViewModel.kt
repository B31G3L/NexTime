package de.beigel.nextime.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.FilterMode
import de.beigel.nextime.data.repository.CountdownRepository
import de.beigel.nextime.notifications.NotificationScheduler
import de.beigel.nextime.widget.WidgetUpdateWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class SortMode {
    DATE_ASC,    // Datum aufsteigend (Standard)
    DATE_DESC,   // Datum absteigend
    TITLE_ASC,   // Alphabetisch A→Z
    TITLE_DESC,  // Alphabetisch Z→A
    CREATED      // Erstellungsdatum
}

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CountdownRepository
    private val context = application.applicationContext

    private val _allCountdowns = MutableStateFlow<List<Countdown>>(emptyList())

    private val _filterMode = MutableStateFlow(FilterMode.ALL)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.DATE_ASC)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _countdowns = MutableStateFlow<List<Countdown>>(emptyList())
    val countdowns: StateFlow<List<Countdown>> = _countdowns.asStateFlow()

    private val _selectedCountdown = MutableStateFlow<Countdown?>(null)
    val selectedCountdown: StateFlow<Countdown?> = _selectedCountdown.asStateFlow()

    init {
        val database = CountdownDatabase.getDatabase(application)
        repository = CountdownRepository(database.countdownDao())

        viewModelScope.launch {
            combine(_allCountdowns, _filterMode, _sortMode, _searchQuery) { all, filter, sort, query ->
                applyFilterSortSearch(all, filter, sort, query)
            }.collect { result ->
                _countdowns.value = result
                WidgetUpdateWorker.updateNow(context)
            }
        }

        viewModelScope.launch {
            repository.allCountdowns.collect { list ->
                _allCountdowns.value = list
            }
        }
    }

    // ─── Filter / Sort / Search ───────────────────────────────────────────────

    fun setFilterMode(mode: FilterMode) { _filterMode.value = mode }
    fun setSortMode(mode: SortMode) { _sortMode.value = mode }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun clearSearch() { _searchQuery.value = "" }

    private fun applyFilterSortSearch(
        list: List<Countdown>,
        filter: FilterMode,
        sort: SortMode,
        query: String
    ): List<Countdown> {
        // 1. Suche
        val searched = if (query.isBlank()) list
        else list.filter { it.title.contains(query.trim(), ignoreCase = true) }

        // 2. Filter
        val filtered = when (filter) {
            FilterMode.COUNTDOWN -> searched.filter { !it.isCountUp }
            FilterMode.COUNTUP   -> searched.filter { it.isCountUp }
            FilterMode.ALL       -> searched
        }

        // 3. Sortierung
        val sorted = when (sort) {
            SortMode.DATE_ASC  -> filtered.sortedBy { it.effectiveTarget }
            SortMode.DATE_DESC -> filtered.sortedByDescending { it.effectiveTarget }
            SortMode.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortMode.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortMode.CREATED   -> filtered.sortedByDescending { it.createdAt }
        }

        // 4. Bei ALL: Countdowns oben, Count-ups unten (innerhalb je sortiert)
        return if (filter == FilterMode.ALL && query.isBlank()) {
            val countdowns = sorted.filter { !it.isCountUp }
            val countups   = sorted.filter { it.isCountUp }
            countdowns + countups
        } else sorted
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    fun addCountdown(countdown: Countdown) {
        viewModelScope.launch {
            val id = repository.insertCountdown(countdown)
            val saved = repository.getCountdownById(id)
            saved?.let { NotificationScheduler.scheduleNotifications(context, it) }
            WidgetUpdateWorker.updateNow(context)
        }
    }

    fun updateCountdown(countdown: Countdown) {
        viewModelScope.launch {
            repository.updateCountdown(countdown)
            NotificationScheduler.cancelAllNotifications(context, countdown)
            NotificationScheduler.scheduleNotifications(context, countdown)
            WidgetUpdateWorker.updateNow(context)
        }
    }

    fun deleteCountdown(countdown: Countdown) {
        viewModelScope.launch {
            NotificationScheduler.cancelAllNotifications(context, countdown)
            repository.deleteCountdown(countdown)
            WidgetUpdateWorker.updateNow(context)
        }
    }

    fun selectCountdown(countdown: Countdown?) { _selectedCountdown.value = countdown }

    fun getCountdownById(id: Long) {
        viewModelScope.launch {
            _selectedCountdown.value = repository.getCountdownById(id)
        }
    }
}