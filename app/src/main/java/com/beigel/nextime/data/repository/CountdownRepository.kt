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

    // ─── Backup (Export / Import) ─────────────────────────────────────────────

    /** Einmalige Liste aller Countdowns – für den JSON-Export. */
    suspend fun getAllCountdownsOnce(): List<Countdown> =
        dao.getAllCountdownsOnce()

    /** Fügt mehrere Countdowns auf einmal ein (z. B. beim Import) und liefert die neuen IDs. */
    suspend fun insertCountdowns(countdowns: List<Countdown>): List<Long> =
        dao.insertCountdowns(countdowns)
}