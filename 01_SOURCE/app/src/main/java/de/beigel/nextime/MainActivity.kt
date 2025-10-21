package de.beigel.nextime

import android.Manifest
import android.content.pm.PackageManager
import de.beigel.nextime.ui.theme.CustomThemePreferences
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
import de.beigel.nextime.ui.theme.CustomTheme
import de.beigel.nextime.widget.CountdownWidget
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
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

            val themeMode by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
            val systemDarkTheme = isSystemInDarkTheme()

            val customTheme by CustomThemePreferences.getCustomTheme(context).collectAsState(initial = CustomTheme.NEXTIME)

            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            NexTimeTheme(
                darkTheme = isDarkTheme,
                customTheme = customTheme
            ) {
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
                // 1. DAYS_ONLY Format
                Countdown(
                    title = "🎂 Geburtstag",
                    targetDateTime = LocalDateTime.now().plusDays(7),
                    displayFormat = CountdownDisplayFormat.DAYS_ONLY.name,
                    color = "#FF7043",
                    notificationEnabled = true,
                    reminderOptions = "DAY_1"
                ),

                // 2. WEEKS_DAYS Format
                Countdown(
                    title = "🎄 Weihnachten",
                    targetDateTime = LocalDateTime.of(2025, 12, 24, 0, 0),
                    displayFormat = CountdownDisplayFormat.WEEKS_DAYS.name,
                    color = "#66BB6A"
                ),

                // 3. MONTHS_DAYS Format
                Countdown(
                    title = "🎆 Silvester",
                    targetDateTime = LocalDateTime.of(2025, 12, 31, 0, 0),
                    displayFormat = CountdownDisplayFormat.MONTHS_DAYS.name,
                    color = "#5C6BC0"
                ),

                // 4. YEARS_MONTHS_DAYS Format
                Countdown(
                    title = "🏖️ Sommerurlaub",
                    targetDateTime = LocalDateTime.of(2026, 7, 15, 0, 0),
                    displayFormat = CountdownDisplayFormat.YEARS_MONTHS_DAYS.name,
                    color = "#42A5F5"
                ),

                // 5. Count-up Beispiel
                Countdown(
                    title = "💍 Hochzeitstag",
                    targetDateTime = LocalDateTime.now().minusYears(2).minusMonths(3),
                    displayFormat = CountdownDisplayFormat.DAYS_ONLY.name,
                    color = "#EC407A"
                ),

                // 6. Weiteres Beispiel
                Countdown(
                    title = "🚀 Mars Mission",
                    targetDateTime = LocalDateTime.of(2030, 1, 1, 0, 0),
                    displayFormat = CountdownDisplayFormat.YEARS_MONTHS_DAYS.name,
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