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
//
// Alle nicht-trivialen Migrationen rufen rebuildCountdownsTable() auf. Diese
// Funktion ist schema-agnostisch: Sie liest die tatsächlich vorhandenen Spalten
// aus und kopiert nur die Schnittmenge mit dem v5-Zielschema. Dadurch funktioniert
// sie unabhängig davon, ob das Quellschema überzählige Spalten (z. B. includeTime)
// oder fehlende Spalten (z. B. icon) hat. Kein Datenverlust für vorhandene Spalten.

/** v1 → v2 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) = rebuildCountdownsTable(database)
}

/** v2 → v3 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) = rebuildCountdownsTable(database)
}

/** v3 → v5 */
val MIGRATION_3_5 = object : Migration(3, 5) {
    override fun migrate(database: SupportSQLiteDatabase) = rebuildCountdownsTable(database)
}

/** v4 → v5 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) = rebuildCountdownsTable(database)
}

/** Zusätzliche Pfade, damit jede Ausgangsversion das v5-Schema erreicht */
val MIGRATION_1_5 = object : Migration(1, 5) {
    override fun migrate(database: SupportSQLiteDatabase) = rebuildCountdownsTable(database)
}
val MIGRATION_2_5 = object : Migration(2, 5) {
    override fun migrate(database: SupportSQLiteDatabase) = rebuildCountdownsTable(database)
}

/**
 * Alle Spalten des aktuellen v5-Entity in kanonischer Reihenfolge.
 * Bei künftigen Schema-Änderungen hier UND im CREATE-Statement mitpflegen.
 */
private val TARGET_COLUMNS = listOf(
    "id", "title", "targetDateTime", "displayFormat", "createdAt", "color",
    "icon", "notificationEnabled", "reminderOptions", "lastNotificationSent",
    "showNights", "recurrence", "isPinned"
)

/**
 * Baut die Tabelle `countdowns` schema-agnostisch nach dem v5-Entity neu auf.
 *
 * Vorgehen:
 *  1. Vorhandene Spalten der aktuellen Tabelle per PRAGMA table_info auslesen.
 *  2. Neue Tabelle mit vollständigem v5-Schema erstellen — alle NOT-NULL-Spalten
 *     mit DEFAULT, damit fehlende Quell-Spalten (z. B. icon) automatisch einen
 *     gültigen Wert erhalten.
 *  3. Nur die Schnittmenge aus Quell- und Zielspalten kopieren. Überzählige
 *     Quell-Spalten (z. B. includeTime) werden dabei ignoriert.
 *  4. Alte Tabelle ersetzen.
 *
 * Hinweis: Die DEFAULT-Klauseln stören Room beim Schema-Check nicht, da die
 * Countdown-Entity keine @ColumnInfo(defaultValue=…) definiert — Room prüft
 * Defaults nur, wenn die Entity selbst welche vorgibt.
 */
private fun rebuildCountdownsTable(database: SupportSQLiteDatabase) {
    // 1. Vorhandene Spalten ermitteln
    val existing = mutableSetOf<String>()
    database.query("PRAGMA table_info(`countdowns`)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (nameIndex >= 0) existing.add(cursor.getString(nameIndex))
        }
    }

    // 2. Zieltabelle mit vollständigem Schema (Defaults sichern fehlende Spalten ab)
    database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `countdowns_new` (
            `id`                    INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            `title`                 TEXT NOT NULL DEFAULT '',
            `targetDateTime`        TEXT NOT NULL DEFAULT '2024-01-01T00:00:00',
            `displayFormat`         TEXT NOT NULL DEFAULT '',
            `createdAt`             TEXT NOT NULL DEFAULT '2024-01-01T00:00:00',
            `color`                 TEXT NOT NULL DEFAULT '#FF7043',
            `icon`                  TEXT NOT NULL DEFAULT 'Timer',
            `notificationEnabled`   INTEGER NOT NULL DEFAULT 0,
            `reminderOptions`       TEXT NOT NULL DEFAULT '',
            `lastNotificationSent`  TEXT,
            `showNights`            INTEGER NOT NULL DEFAULT 0,
            `recurrence`            TEXT NOT NULL DEFAULT 'NONE',
            `isPinned`              INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
    )

    // 3. Nur gemeinsame Spalten kopieren (fehlende erhalten ihren DEFAULT)
    val common = TARGET_COLUMNS.filter { it in existing }
    if (common.isNotEmpty()) {
        val colList = common.joinToString(", ") { "`$it`" }
        database.execSQL(
            "INSERT INTO `countdowns_new` ($colList) SELECT $colList FROM `countdowns`"
        )
    }

    // 4. Alte Tabelle ersetzen
    database.execSQL("DROP TABLE `countdowns`")
    database.execSQL("ALTER TABLE `countdowns_new` RENAME TO `countdowns`")
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
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_5,
                        MIGRATION_4_5,
                        MIGRATION_1_5,
                        MIGRATION_2_5
                    )
                    // KEIN fallbackToDestructiveMigration* — verhindert Datenverlust.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}