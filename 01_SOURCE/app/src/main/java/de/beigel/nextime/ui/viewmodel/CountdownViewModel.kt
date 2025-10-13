package de.beigel.nextime.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.repository.CountdownRepository
import de.beigel.nextime.notifications.NotificationScheduler
import de.beigel.nextime.widget.CountdownWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CountdownRepository
    private val context = application.applicationContext

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
                // Widget bei Änderungen aktualisieren
                CountdownWidget.updateAllWidgets(context)
            }
        }
    }

    fun addCountdown(countdown: Countdown) {
        viewModelScope.launch {
            val id = repository.insertCountdown(countdown)
            val savedCountdown = repository.getCountdownById(id)

            // Notifikationen planen
            savedCountdown?.let {
                NotificationScheduler.scheduleNotifications(context, it)
            }

            // Widget aktualisieren
            CountdownWidget.updateAllWidgets(context)
        }
    }

    fun updateCountdown(countdown: Countdown) {
        viewModelScope.launch {
            repository.updateCountdown(countdown)

            // Notifikationen neu planen
            NotificationScheduler.cancelAllNotifications(context, countdown)
            NotificationScheduler.scheduleNotifications(context, countdown)

            // Widget aktualisieren
            CountdownWidget.updateAllWidgets(context)
        }
    }

    fun deleteCountdown(countdown: Countdown) {
        viewModelScope.launch {
            // Notifikationen abbrechen
            NotificationScheduler.cancelAllNotifications(context, countdown)
            repository.deleteCountdown(countdown)

            // Widget aktualisieren
            CountdownWidget.updateAllWidgets(context)
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