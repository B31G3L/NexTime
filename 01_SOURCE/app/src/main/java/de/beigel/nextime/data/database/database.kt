package de.beigel.nextime.data.database

import android.content.Context
import androidx.room.*
import de.beigel.nextime.data.model.Countdown
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface CountdownDao {
    @Query("SELECT * FROM countdowns ORDER BY targetDateTime ASC")
    fun getAllCountdowns(): Flow<List<Countdown>>

    @Query("SELECT * FROM countdowns WHERE id = :id")
    suspend fun getCountdownById(id: Long): Countdown?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountdown(countdown: Countdown): Long

    @Update
    suspend fun updateCountdown(countdown: Countdown)

    @Delete
    suspend fun deleteCountdown(countdown: Countdown)

    @Query("DELETE FROM countdowns WHERE id = :id")
    suspend fun deleteCountdownById(id: Long)
}

@Database(
    entities = [Countdown::class],
    version = 3,  // Version erhöht für Notifikationen
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CountdownDatabase : RoomDatabase() {
    abstract fun countdownDao(): CountdownDao

    companion object {
        @Volatile
        private var INSTANCE: CountdownDatabase? = null

        fun getDatabase(context: Context): CountdownDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CountdownDatabase::class.java,
                    "nextime_database"
                )
                    .fallbackToDestructiveMigration()  // Alte Daten werden gelöscht
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }
}