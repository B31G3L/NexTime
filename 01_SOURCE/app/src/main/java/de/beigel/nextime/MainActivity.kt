package de.beigel.nextime

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.notifications.CountdownNotificationManager
import de.beigel.nextime.ui.theme.NexTimeTheme
import de.beigel.nextime.ui.theme.ThemePreferences
import de.beigel.nextime.ui.screens.MainScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    // Permission Launcher für Benachrichtigungen
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Optional: Feedback an den User
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Notification Channel erstellen
        CountdownNotificationManager.createNotificationChannel(this)

        // Benachrichtigungs-Permission anfragen (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val isDarkTheme by ThemePreferences.getDarkMode(context).collectAsState(initial = false)

            NexTimeTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = {
                            scope.launch {
                                ThemePreferences.setDarkMode(context, !isDarkTheme)
                            }
                        }
                    )
                }
            }
        }
        insertTestData()
    }

    // In MainActivity.kt nach onCreate() hinzufügen:

    private fun insertTestData() {
        val database = CountdownDatabase.getDatabase(this)
        val dao = database.countdownDao()

        lifecycleScope.launch {
            // Prüfen ob schon Daten vorhanden
            val existing = dao.getAllCountdowns().first()
            if (existing.isNotEmpty()) return@launch

            // Test-Countdowns
            val testCountdowns = listOf(
                // 1. Nahe Zukunft mit Zeit
                Countdown(
                    title = "🎂 Geburtstag",
                    targetDateTime = LocalDateTime.now().plusDays(7).withHour(18).withMinute(0),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.FULL_DETAILED.name,
                    color = "#FF7043",
                    notificationEnabled = true,
                    reminderOptions = "DAY_1,HOUR_1"
                ),

                // 2. Weit in der Zukunft
                Countdown(
                    title = "🏖️ Sommerurlaub 2026",
                    targetDateTime = LocalDateTime.of(2026, 7, 15, 10, 0),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.FULL_DETAILED.name,
                    color = "#42A5F5",
                    showNights = true
                ),

                // 3. Nur Tage-Format
                Countdown(
                    title = "🎄 Weihnachten",
                    targetDateTime = LocalDateTime.of(2025, 12, 24, 0, 0),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.DAYS_ONLY.name,
                    color = "#66BB6A"
                ),

                // 4. Wochen-Format
                Countdown(
                    title = "🎓 Prüfung",
                    targetDateTime = LocalDateTime.now().plusWeeks(3),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.WEEKS_DAYS.name,
                    color = "#AB47BC"
                ),

                // 5. Monate-Format
                Countdown(
                    title = "🏠 Umzug",
                    targetDateTime = LocalDateTime.now().plusMonths(2).plusDays(5),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.MONTHS_DAYS.name,
                    color = "#FFA726"
                ),

                // 6. Stunden-Format (sehr nah)
                Countdown(
                    title = "⚽ Fußballspiel",
                    targetDateTime = LocalDateTime.now().plusHours(5).plusMinutes(30),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.HOURS_MINUTES.name,
                    color = "#EF5350"
                ),

                // 7. Count-up (Vergangenheit)
                Countdown(
                    title = "💍 Hochzeitstag",
                    targetDateTime = LocalDateTime.now().minusYears(2).minusMonths(3),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.FULL_DETAILED.name,
                    color = "#EC407A"
                ),

                // 8. Heute
                Countdown(
                    title = "🍕 Pizza-Abend",
                    targetDateTime = LocalDateTime.now().withHour(19).withMinute(30),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.FULL_TIME.name,
                    color = "#26A69A"
                ),

                // 9. Silvester
                Countdown(
                    title = "🎆 Silvester 2025",
                    targetDateTime = LocalDateTime.of(2025, 12, 31, 23, 59),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.DAYS_HOURS.name,
                    color = "#5C6BC0",
                    showNights = true
                ),

                // 10. Langfristig
                Countdown(
                    title = "🚀 Mars Mission 2030",
                    targetDateTime = LocalDateTime.of(2030, 1, 1, 0, 0),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.FULL_DETAILED.name,
                    color = "#8D6E63"
                )
            )

            testCountdowns.forEach { countdown ->
                dao.insertCountdown(countdown)
            }

            Toast.makeText(
                this@MainActivity,
                "✅ ${testCountdowns.size} Test-Countdowns erstellt!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}