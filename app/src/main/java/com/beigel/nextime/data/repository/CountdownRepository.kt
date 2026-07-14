package com.beigel.nextime.data.repository

import kotlinx.coroutines.flow.Flow
import todo.beigelwick.de.todolist.data.database.CountdownDao
import todo.beigelwick.de.todolist.data.model.Countdown

class CountdownRepository(private val dao: com.beigel.nextime.data.database.CountdownDao) {

    val allCountdowns: Flow<List<com.beigel.nextime.data.model.Countdown>> = dao.getAllCountdowns()

    suspend fun getCountdownById(id: Long): com.beigel.nextime.data.model.Countdown? =
        dao.getCountdownById(id)

    suspend fun insertCountdown(countdown: com.beigel.nextime.data.model.Countdown): Long =
        dao.insertCountdown(countdown)

    suspend fun updateCountdown(countdown: com.beigel.nextime.data.model.Countdown) =
        dao.updateCountdown(countdown)

    suspend fun deleteCountdown(countdown: com.beigel.nextime.data.model.Countdown) =
        dao.deleteCountdown(countdown)

    suspend fun deleteCountdownById(id: Long) =
        dao.deleteCountdownById(id)
}