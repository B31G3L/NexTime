# ── Room ──────────────────────────────────────────────────────────────────────
-keep class todo.beigelwick.de.todolist.data.model.** { *; }

# ── Kotlin ────────────────────────────────────────────────────────────────────
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ── Compose ───────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── Material3 ─────────────────────────────────────────────────────────────────
-keep class androidx.compose.material3.** { *; }

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── Navigation ────────────────────────────────────────────────────────────────
-keep class androidx.navigation.** { *; }

# ── BuildConfig ───────────────────────────────────────────────────────────────
-keep class todo.beigelwick.de.todolist.BuildConfig { *; }

# ── Optimierung ───────────────────────────────────────────────────────────────
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose