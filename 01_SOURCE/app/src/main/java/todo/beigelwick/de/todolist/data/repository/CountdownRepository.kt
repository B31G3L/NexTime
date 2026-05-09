package todo.beigelwick.de.todolist.data.repository

import kotlinx.coroutines.flow.Flow
import todo.beigelwick.de.todolist.data.database.CountdownDao
import todo.beigelwick.de.todolist.data.model.Countdown

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