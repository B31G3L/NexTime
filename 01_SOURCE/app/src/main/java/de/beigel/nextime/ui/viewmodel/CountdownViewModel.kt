package de.beigel.nextime.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.repository.CountdownRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CountdownRepository

    private val _countdowns = MutableStateFlow<List<Countdown>>(emptyList())
    val countdowns: StateFlow<List<Countdown>> = _countdowns.asStateFlow()

    private val _selectedCountdown = MutableStateFlow<Countdown?>(null)
    val selectedCountdown: StateFlow<Countdown?> = _selectedCountdown.asStateFlow()

    init {
        val database = CountdownDatabase.getDatabase(application)
        repository = CountdownRepository(database.countdownDao())

        viewModelScope.launch {
            repository.allCountdowns.collect { list ->
                _countdowns.value = list
            }
        }
    }

    fun addCountdown(countdown: Countdown) {
        viewModelScope.launch {
            repository.insertCountdown(countdown)
        }
    }

    fun updateCountdown(countdown: Countdown) {
        viewModelScope.launch {
            repository.updateCountdown(countdown)
        }
    }

    fun deleteCountdown(countdown: Countdown) {
        viewModelScope.launch {
            repository.deleteCountdown(countdown)
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