package todo.beigelwick.de.todolist.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import todo.beigelwick.de.todolist.data.database.CountdownDatabase
import todo.beigelwick.de.todolist.data.model.Countdown
import todo.beigelwick.de.todolist.data.model.FilterMode
import todo.beigelwick.de.todolist.data.repository.CountdownRepository
import todo.beigelwick.de.todolist.notifications.NotificationScheduler
import todo.beigelwick.de.todolist.widget.WidgetUpdateWorker

// ─── Sortiermodi ──────────────────────────────────────────────────────────────

enum class SortMode {
    DATE_ASC,
    DATE_DESC,
    TITLE_ASC,
    TITLE_DESC,
    CREATED
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val context    = application.applicationContext
    private val repository : CountdownRepository

    private val _allCountdowns = MutableStateFlow<List<Countdown>>(emptyList())

    private val _filterMode  = MutableStateFlow(FilterMode.ALL)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    private val _sortMode    = MutableStateFlow(SortMode.DATE_ASC)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _countdowns  = MutableStateFlow<List<Countdown>>(emptyList())
    val countdowns: StateFlow<List<Countdown>> = _countdowns.asStateFlow()

    private val _selectedCountdown = MutableStateFlow<Countdown?>(null)
    val selectedCountdown: StateFlow<Countdown?> = _selectedCountdown.asStateFlow()

    private val _tickSeconds = MutableStateFlow(0L)
    val tickSeconds: StateFlow<Long> = _tickSeconds.asStateFlow()

    private val _tickMinutes = MutableStateFlow(0L)
    val tickMinutes: StateFlow<Long> = _tickMinutes.asStateFlow()

    init {
        val database = CountdownDatabase.getDatabase(application)
        repository   = CountdownRepository(database.countdownDao())

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
                WidgetUpdateWorker.updateNow(context)
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

    fun setFilterMode(mode: FilterMode)  { _filterMode.value  = mode }
    fun setSortMode(mode: SortMode)      { _sortMode.value    = mode }
    fun setSearchQuery(query: String)    { _searchQuery.value = query }
    fun clearSearch()                    { _searchQuery.value = "" }

    private fun applyFilterSortSearch(
        list   : List<Countdown>,
        filter : FilterMode,
        sort   : SortMode,
        query  : String
    ): List<Countdown> {
        // 1. Suche
        val searched = if (query.isBlank()) list
        else list.filter { it.title.contains(query.trim(), ignoreCase = true) }

        // 2. Filter
        val filtered = when (filter) {
            FilterMode.COUNTDOWN -> searched.filter { !it.isCountUp }
            FilterMode.COUNTUP   -> searched.filter {  it.isCountUp }
            FilterMode.ALL       -> searched
        }

        // 3. Sortierung
        val sorted = when (sort) {
            SortMode.DATE_ASC   -> filtered.sortedBy          { it.effectiveTarget }
            SortMode.DATE_DESC  -> filtered.sortedByDescending { it.effectiveTarget }
            SortMode.TITLE_ASC  -> filtered.sortedBy          { it.title.lowercase() }
            SortMode.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            SortMode.CREATED    -> filtered.sortedByDescending { it.createdAt }
        }

        // 4. Gepinnte immer ganz oben — innerhalb der Pinned-Gruppe gilt die gewählte Sortierung
        val pinned   = sorted.filter {  it.isPinned }
        val unpinned = sorted.filter { !it.isPinned }

        // 5. Bei "Alle" ohne Suche: Countdowns vor Count-ups (jeweils Pinned zuerst)
        return if (filter == FilterMode.ALL && query.isBlank()) {
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

    fun addCountdown(countdown: Countdown) {
        viewModelScope.launch {
            val id    = repository.insertCountdown(countdown)
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

    /** Toggelt isPinned und speichert direkt */
    fun togglePin(countdown: Countdown) {
        viewModelScope.launch {
            repository.updateCountdown(countdown.copy(isPinned = !countdown.isPinned))
        }
    }

    // ─── Einzelner Countdown ──────────────────────────────────────────────────

    fun selectCountdown(countdown: Countdown?) {
        _selectedCountdown.value = countdown
    }

    suspend fun getCountdownById(id: Long): Countdown? =
        repository.getCountdownById(id)
}