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
        database.execSQL(
            """
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
            """.trimIndent()
        )
        database.execSQL(
            """
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
            """.trimIndent()
        )
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

/**
 * v3 → v4: Idempotente Angleichung des Schemas an die aktuelle Entity.
 *
 * Hintergrund: Es existierte ein nicht dokumentierter v4-Build, dessen Schema
 * unbekannt ist. Damit Geräte, die NIE auf v4 waren, sauber von 3 → 4 → 5 laufen
 * UND Geräte, die zufällig auf v4 waren, von 4 → 5 nicht crashen, stellt diese
 * Migration sicher, dass alle erwarteten Spalten existieren. Fehlende Spalten
 * werden additiv ergänzt (ADD COLUMN ist in SQLite verlustfrei).
 *
 * Da auf den meisten Geräten v3 == aktuelles Schema ist, sind diese ADD COLUMNs
 * dort No-Ops bzw. fügen nur bereits vorhandene Defaults hinzu. Wir prüfen daher
 * vorhandene Spalten und ergänzen nur das Fehlende.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        ensureColumns(database)
    }
}

/**
 * v4 → v5: Stellt ebenfalls nur sicher, dass das Schema vollständig ist.
 * Kein Datenverlust — ersetzt das frühere fallbackToDestructiveMigrationFrom(4).
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        ensureColumns(database)
    }
}

/**
 * Direkter Pfad v3 → v5 für Geräte, die nie eine v4-Installation hatten.
 * Room nutzt bei Vorhandensein die direkte Migration; ansonsten ketten sich
 * 3→4 und 4→5 automatisch. Wir bieten beides an, um jeden Pfad abzudecken.
 */
val MIGRATION_3_5 = object : Migration(3, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        ensureColumns(database)
    }
}

/**
 * Hilfsfunktion: Liest die vorhandenen Spalten der Tabelle `countdowns` aus und
 * ergänzt nur die Spalten, die fehlen. Dadurch ist die Migration idempotent und
 * funktioniert unabhängig vom genauen Ausgangsschema (v3 oder unbekanntes v4).
 *
 * WICHTIG: Die hier ergänzten Spalten und ihre Defaults müssen exakt zur
 * Countdown-Entity passen (Models.kt). Bei künftigen Schema-Änderungen diese
 * Liste mitpflegen.
 */
private fun ensureColumns(database: SupportSQLiteDatabase) {
    val existing = mutableSetOf<String>()
    database.query("PRAGMA table_info(`countdowns`)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (nameIndex >= 0) existing.add(cursor.getString(nameIndex))
        }
    }

    // Spalte → SQL-Definition (inkl. NOT NULL + DEFAULT, damit ADD COLUMN gültig ist)
    val expected = linkedMapOf(
        "displayFormat"        to "TEXT NOT NULL DEFAULT ''",
        "createdAt"            to "TEXT NOT NULL DEFAULT ''",
        "color"                to "TEXT NOT NULL DEFAULT '#FF7043'",
        "icon"                 to "TEXT NOT NULL DEFAULT 'Timer'",
        "notificationEnabled"  to "INTEGER NOT NULL DEFAULT 0",
        "reminderOptions"      to "TEXT NOT NULL DEFAULT ''",
        "lastNotificationSent" to "TEXT",
        "showNights"           to "INTEGER NOT NULL DEFAULT 0",
        "recurrence"           to "TEXT NOT NULL DEFAULT 'NONE'",
        "isPinned"             to "INTEGER NOT NULL DEFAULT 0"
    )

    expected.forEach { (column, definition) ->
        if (column !in existing) {
            database.execSQL("ALTER TABLE `countdowns` ADD COLUMN `$column` $definition")
        }
    }
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
    version      = 5,
    exportSchema = true
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_3_5
                    )
                    // KEIN fallbackToDestructiveMigration* mehr — verhindert Datenverlust.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}