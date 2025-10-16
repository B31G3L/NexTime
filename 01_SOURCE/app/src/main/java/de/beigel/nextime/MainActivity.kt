package de.beigel.nextime

import android.Manifest
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
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
import de.beigel.nextime.ui.theme.ThemeMode
import de.beigel.nextime.ui.theme.ThemePreferences
import de.beigel.nextime.ui.screens.MainScreen
import de.beigel.nextime.widget.CountdownWidget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Optional: Feedback an den User
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CountdownNotificationManager.createNotificationChannel(this)

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

            val themeMode by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
            val systemDarkTheme = isSystemInDarkTheme()

            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            NexTimeTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { }
                    )
                }
            }
        }
        insertTestData()
    }

    private fun insertTestData() {
        val database = CountdownDatabase.getDatabase(this)
        val dao = database.countdownDao()

        lifecycleScope.launch {
            val existing = dao.getAllCountdowns().first()
            if (existing.isNotEmpty()) return@launch

            val testCountdowns = listOf(
                // 1. FULL_DETAILED Format
                Countdown(
                    title = "🎂 Geburtstag (FULL_DETAILED)",
                    targetDateTime = LocalDateTime.now().plusDays(7).withHour(18).withMinute(0),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.FULL_DETAILED.name,
                    color = "#FF7043",
                    notificationEnabled = true,
                    reminderOptions = "DAY_1,HOUR_1"
                ),

                // 2. DAYS_ONLY Format
                Countdown(
                    title = "🎄 Weihnachten (DAYS_ONLY)",
                    targetDateTime = LocalDateTime.of(2025, 12, 24, 0, 0),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.DAYS_ONLY.name,
                    color = "#66BB6A"
                ),

                // 3. DAYS_HOURS Format
                Countdown(
                    title = "🎆 Silvester (DAYS_HOURS)",
                    targetDateTime = LocalDateTime.of(2025, 12, 31, 23, 59),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.DAYS_HOURS.name,
                    color = "#5C6BC0"
                ),

                // 4. HOURS_MINUTES Format
                Countdown(
                    title = "⚽ Fußballspiel (HOURS_MINUTES)",
                    targetDateTime = LocalDateTime.now().plusHours(5).plusMinutes(30),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.HOURS_MINUTES.name,
                    color = "#EF5350"
                ),

                // 5. FULL_TIME Format
                Countdown(
                    title = "🍕 Pizza-Abend (FULL_TIME)",
                    targetDateTime = LocalDateTime.now().withHour(19).withMinute(30),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.FULL_TIME.name,
                    color = "#26A69A"
                ),

                // 6. WEEKS_DAYS Format
                Countdown(
                    title = "🎓 Prüfung (WEEKS_DAYS)",
                    targetDateTime = LocalDateTime.now().plusWeeks(3),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.WEEKS_DAYS.name,
                    color = "#AB47BC"
                ),

                // 7. MONTHS_DAYS Format
                Countdown(
                    title = "🏠 Umzug (MONTHS_DAYS)",
                    targetDateTime = LocalDateTime.now().plusMonths(2).plusDays(5),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.MONTHS_DAYS.name,
                    color = "#FFA726"
                ),

                // 8. Count-up Beispiel
                Countdown(
                    title = "💍 Hochzeitstag (FULL_DETAILED)",
                    targetDateTime = LocalDateTime.now().minusYears(2).minusMonths(3),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.FULL_DETAILED.name,
                    color = "#EC407A"
                ),

                // 9. Langfristig
                Countdown(
                    title = "🏖️ Sommerurlaub (FULL_DETAILED)",
                    targetDateTime = LocalDateTime.of(2026, 7, 15, 10, 0),
                    includeTime = true,
                    displayFormat = CountdownDisplayFormat.FULL_DETAILED.name,
                    color = "#42A5F5"
                ),

                // 10. Sehr langfristig
                Countdown(
                    title = "🚀 Mars Mission (MONTHS_DAYS)",
                    targetDateTime = LocalDateTime.of(2030, 1, 1, 0, 0),
                    includeTime = false,
                    displayFormat = CountdownDisplayFormat.MONTHS_DAYS.name,
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

            CountdownWidget.updateAllWidgets(this@MainActivity)
        }
    }
}