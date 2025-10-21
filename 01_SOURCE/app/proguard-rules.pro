# In proguard-rules.pro:

# Keep Room entities
-keep class de.beigel.nextime.data.model.** { *; }

# Keep Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Keep Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Material3
-keep class androidx.compose.material3.** { *; }

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep BuildConfig
-keep class de.beigel.nextime.BuildConfig { *; }

# Optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose