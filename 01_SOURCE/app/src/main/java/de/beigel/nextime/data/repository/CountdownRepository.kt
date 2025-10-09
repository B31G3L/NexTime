package de.beigel.nextime.data.repository

import de.beigel.nextime.data.database.CountdownDao
import de.beigel.nextime.data.model.Countdown
import kotlinx.coroutines.flow.Flow

class CountdownRepository(private val countdownDao: CountdownDao) {

    val allCountdowns: Flow<List<Countdown>> = countdownDao.getAllCountdowns()

    suspend fun getCountdownById(id: Long): Countdown? {
        return countdownDao.getCountdownById(id)
    }

    suspend fun insertCountdown(countdown: Countdown): Long {
        return countdownDao.insertCountdown(countdown)
    }

    suspend fun updateCountdown(countdown: Countdown) {
        countdownDao.updateCountdown(countdown)
    }

    suspend fun deleteCountdown(countdown: Countdown) {
        countdownDao.deleteCountdown(countdown)
    }

    suspend fun deleteCountdownById(id: Long) {
        countdownDao.deleteCountdownById(id)
    }
}