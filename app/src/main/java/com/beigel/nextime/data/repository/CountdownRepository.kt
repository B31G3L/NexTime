package com.beigel.nextime.data.repository

import kotlinx.coroutines.flow.Flow
import com.beigel.nextime.data.database.CountdownDao
import com.beigel.nextime.data.model.Countdown

class CountdownRepository(private val dao: CountdownDao) {

    val allCountdowns: Flow<List<Countdown>> = dao.getAllCountdowns()

    suspend fun getCountdownById(id: Long): Countdown? =
        dao.getCountdownById(id)

    suspend fun insertCountdown(countdown: Countdown): Long =
        dao.insertCountdown(countdown)

    suspend fun updateCountdown(countdown: Countdown) =
        dao.updateCountdown(countdown)

    suspend fun deleteCountdown(countdown: Countdown) =
        dao.deleteCountdown(countdown)

    suspend fun deleteCountdownById(id: Long) =
        dao.deleteCountdownById(id)
}