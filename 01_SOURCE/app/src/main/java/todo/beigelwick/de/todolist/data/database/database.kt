package todo.beigelwick.de.todolist.data.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import todo.beigelwick.de.todolist.data.model.Countdown
import java.time.LocalDateTime

// ─── DAO ──────────────────────────────────────────────────────────────────────

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

// ─── Migrations ───────────────────────────────────────────────────────────────

/** v1 → v2: Frisches Projekt, keine Migration nötig – Startversion ist 1 */

// Für zukünftige Migrationen Beispiel:
// val MIGRATION_1_2 = object : Migration(1, 2) {
//     override fun migrate(database: SupportSQLiteDatabase) {
//         database.execSQL("ALTER TABLE countdowns ADD COLUMN newColumn TEXT NOT NULL DEFAULT ''")
//     }
// }

// ─── Type Converters ──────────────────────────────────────────────────────────

class Converters {
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? =
        date?.toString()
}

// ─── Database ─────────────────────────────────────────────────────────────────

@Database(
    entities = [Countdown::class],
    version  = 1,
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
                    // .addMigrations(MIGRATION_1_2)  ← hier zukünftige Migrationen eintragen
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}