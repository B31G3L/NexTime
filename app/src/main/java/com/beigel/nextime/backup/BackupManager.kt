package com.beigel.nextime.backup

import com.beigel.nextime.data.model.Countdown
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime

/**
 * Export & Import der Countdown-Einträge als JSON.
 *
 * Format:
 * {
 *   "version": 1,
 *   "exportedAt": "2026-07-22T10:00:00",
 *   "countdowns": [ { ...Feld-für-Feld... }, ... ]
 * }
 *
 * Beim Import werden neue IDs vergeben (id = 0 → Room generiert eine neue
 * Primärschlüssel-ID), damit ein Import nie mit bestehenden Einträgen kollidiert.
 * Ein Import fügt die Einträge also zusätzlich hinzu, statt bestehende zu überschreiben.
 */
object BackupManager {

    private const val BACKUP_VERSION = 1

    // ─── Export ─────────────────────────────────────────────────────────────

    fun exportToJson(countdowns: List<Countdown>): String {
        val root = JSONObject()
        root.put("version", BACKUP_VERSION)
        root.put("exportedAt", LocalDateTime.now().toString())

        val array = JSONArray()
        countdowns.forEach { c ->
            val obj = JSONObject()
            obj.put("title", c.title)
            obj.put("targetDateTime", c.targetDateTime.toString())
            obj.put("displayFormat", c.displayFormat)
            obj.put("createdAt", c.createdAt.toString())
            obj.put("color", c.color)
            obj.put("icon", c.icon)
            obj.put("notificationEnabled", c.notificationEnabled)
            obj.put("reminderOptions", c.reminderOptions)
            obj.put("lastNotificationSent", c.lastNotificationSent ?: JSONObject.NULL)
            obj.put("showNights", c.showNights)
            obj.put("recurrence", c.recurrence)
            obj.put("isPinned", c.isPinned)
            array.put(obj)
        }
        root.put("countdowns", array)

        // Einrückung 2 Leerzeichen → gut lesbar, falls man reinschaut
        return root.toString(2)
    }

    // ─── Import ─────────────────────────────────────────────────────────────

    /**
     * Parst einen zuvor exportierten Backup-JSON-String.
     * Wirft eine [IllegalArgumentException] mit verständlicher Meldung,
     * wenn es sich nicht um eine gültige Sicherungsdatei handelt.
     */
    fun importFromJson(json: String): List<Countdown> {
        val root = JSONObject(json)
        val array = root.optJSONArray("countdowns")
            ?: throw IllegalArgumentException("Kein gültiges Backup-Format (Feld 'countdowns' fehlt)")

        val result = mutableListOf<Countdown>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val lastNotificationSent =
                if (obj.isNull("lastNotificationSent") || !obj.has("lastNotificationSent")) null
                else obj.getString("lastNotificationSent")

            result.add(
                Countdown(
                    id                   = 0L, // neue ID beim Import vergeben lassen
                    title                = obj.optString("title", ""),
                    targetDateTime       = parseDateOrNow(obj.optString("targetDateTime", "")),
                    displayFormat        = obj.optString("displayFormat", ""),
                    createdAt            = parseDateOrNow(obj.optString("createdAt", "")),
                    color                = obj.optString("color", "#FF7043"),
                    icon                 = obj.optString("icon", "Timer"),
                    notificationEnabled  = obj.optBoolean("notificationEnabled", false),
                    reminderOptions      = obj.optString("reminderOptions", ""),
                    lastNotificationSent = lastNotificationSent,
                    showNights           = obj.optBoolean("showNights", false),
                    recurrence           = obj.optString("recurrence", "NONE"),
                    isPinned             = obj.optBoolean("isPinned", false)
                )
            )
        }
        return result
    }

    private fun parseDateOrNow(value: String): LocalDateTime {
        if (value.isBlank()) return LocalDateTime.now()
        return try {
            LocalDateTime.parse(value)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    }
}