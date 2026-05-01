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

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CountdownRepository
    private val context = application.applicationContext

    // Alle Einträge aus der DB (ungefiltert)
    private val _allCountdowns = MutableStateFlow<List<Countdown>>(emptyList())

    // Aktuell gewählter Filtermodus
    private val _filterMode = MutableStateFlow(FilterMode.ALL)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    // Gefilterte + sortierte Liste für die UI
    private val _countdowns = MutableStateFlow<List<Countdown>>(emptyList())
    val countdowns: StateFlow<List<Countdown>> = _countdowns.asStateFlow()

    // Für den Card-Dialog
    private val _selectedCountdown = MutableStateFlow<Countdown?>(null)
    val selectedCountdown: StateFlow<Countdown?> = _selectedCountdown.asStateFlow()

    init {
        val database = CountdownDatabase.getDatabase(application)
        repository = CountdownRepository(database.countdownDao())

        viewModelScope.launch {
            // Kombiniere DB-Daten und Filtermodus reaktiv
            combine(_allCountdowns, _filterMode) { all, filter ->
                applyFilter(all, filter)
            }.collect { filtered ->
                _countdowns.value = filtered
                WidgetUpdateWorker.updateNow(context)
            }
        }

        viewModelScope.launch {
            repository.allCountdowns.collect { list ->
                _allCountdowns.value = list
            }
        }
    }

    // ─── Filter ───────────────────────────────────────────────────────────────

    fun setFilterMode(mode: FilterMode) {
        _filterMode.value = mode
    }

    private fun applyFilter(list: List<Countdown>, filter: FilterMode): List<Countdown> {
        return when (filter) {
            FilterMode.COUNTDOWN -> {
                // Nur zukünftige, sortiert nach Datum aufsteigend (nächster zuerst)
                list
                    .filter { !it.isCountUp }
                    .sortedBy { it.targetDateTime }
            }
            FilterMode.COUNTUP -> {
                // Nur vergangene, sortiert nach Datum absteigend (zuletzt vergangen zuerst)
                list
                    .filter { it.isCountUp }
                    .sortedByDescending { it.targetDateTime }
            }
            FilterMode.ALL -> {
                // Countdowns oben (aufsteigend), Count-ups unten (absteigend)
                val countdowns = list
                    .filter { !it.isCountUp }
                    .sortedBy { it.targetDateTime }
                val countups = list
                    .filter { it.isCountUp }
                    .sortedByDescending { it.targetDateTime }
                countdowns + countups
            }
        }
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    fun addCountdown(countdown: Countdown) {
        viewModelScope.launch {
            val id = repository.insertCountdown(countdown)
            val savedCountdown = repository.getCountdownById(id)
            savedCountdown?.let {
                NotificationScheduler.scheduleNotifications(context, it)
            }
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

    fun selectCountdown(countdown: Countdown?) {
        _selectedCountdown.value = countdown
    }

    fun getCountdownById(id: Long) {
        viewModelScope.launch {
            val countdown = repository.getCountdownById(id)
            _selectedCountdown.value = countdown
        }
    }
}