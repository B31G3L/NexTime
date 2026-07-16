# Room, Compose, Kotlin, Coroutines, WorkManager, Glance und Navigation liefern
# ihre R8/Proguard-Regeln bereits selbst über die Consumer-Rules ihrer AARs mit.
# Pauschale "-keep class x.** { *; }"-Regeln für diese Pakete verhindern
# Shrinking, Obfuscation UND Optimierung für den kompletten Abhängigkeitsbaum
# und wurden daher entfernt (Ursache der Play-Console-Warnung).

# ── Stacktraces bei Crashreports lesbar halten ──────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Sicherheitsnetz für die Room-Datenbankklasse ────────────────────────────
# (normalerweise bereits durch Rooms eigene Consumer-Rules abgedeckt)
-keep class * extends androidx.room.RoomDatabase