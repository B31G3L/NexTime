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

/** v1 → v2: includeTime entfernt */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `countdowns_new` (
                `id`                    INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `title`                 TEXT NOT NULL,
                `targetDateTime`        TEXT NOT NULL,
                `displayFormat`         TEXT NOT NULL,
                `createdAt`             TEXT NOT NULL,
                `color`                 TEXT NOT NULL,
                `icon`                  TEXT NOT NULL,
                `notificationEnabled`   INTEGER NOT NULL,
                `reminderOptions`       TEXT NOT NULL,
                `lastNotificationSent`  TEXT,
                `showNights`            INTEGER NOT NULL,
                `recurrence`            TEXT NOT NULL
            )
        """.trimIndent())
        database.execSQL("""
            INSERT INTO `countdowns_new` (
                `id`, `title`, `targetDateTime`, `displayFormat`, `createdAt`,
                `color`, `icon`, `notificationEnabled`, `reminderOptions`,
                `lastNotificationSent`, `showNights`, `recurrence`
            )
            SELECT
                `id`, `title`, `targetDateTime`, `displayFormat`, `createdAt`,
                `color`, `icon`, `notificationEnabled`, `reminderOptions`,
                `lastNotificationSent`, `showNights`, `recurrence`
            FROM `countdowns`
        """.trimIndent())
        database.execSQL("DROP TABLE `countdowns`")
        database.execSQL("ALTER TABLE `countdowns_new` RENAME TO `countdowns`")
    }
}

/** v2 → v3: isPinned Spalte hinzugefügt, Standard false */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE `countdowns` ADD COLUMN `isPinned` INTEGER NOT NULL DEFAULT 0"
        )
    }
}

/** v3 → v4: no schema change, version bump only */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) { }
}

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
    entities     = [Countdown::class],
    version      = 4,
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}