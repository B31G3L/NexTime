// MainActivity.kt

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
import de.beigel.nextime.notifications.NotificationScheduler
import de.beigel.nextime.ui.theme.NexTimeTheme
import de.beigel.nextime.ui.theme.ThemeMode
import de.beigel.nextime.ui.theme.ThemePreferences
import de.beigel.nextime.ui.screens.MainScreenWithBottomNav  // ← NEU!
import de.beigel.nextime.ui.theme.CustomTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "✅ Benachrichtigungen aktiviert", Toast.LENGTH_SHORT).show()
        } else {
            // Permission abgelehnt - zeige Erklärung
            showPermissionRationaleDialog()
        }
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
        scheduleAllPendingNotifications()
        requestNotificationPermissionIfNeeded()

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
                    // ← HIER DIE ÄNDERUNG:
                    MainScreenWithBottomNav(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { }
                    )
                }
            }
        }

        // Optional: Test-Daten nur beim ersten Start
        // insertTestData()
    }
    private fun scheduleAllPendingNotifications() {
        val database = CountdownDatabase.getDatabase(this)

        lifecycleScope.launch {
            val countdowns = database.countdownDao().getAllCountdowns().first()
            countdowns.forEach { countdown ->
                if (countdown.notificationEnabled) {
                    NotificationScheduler.scheduleNotifications(this@MainActivity, countdown)
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission bereits vorhanden
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Zeige Erklärung WARUM wir die Permission brauchen
                    showPermissionRationaleDialog()
                }
                else -> {
                    // Frage Permission ab
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📬 Benachrichtigungen")
            .setMessage(
                "NexTime möchte dir Erinnerungen für deine Countdowns senden.\n\n" +
                        "Du wirst rechtzeitig an wichtige Termine erinnert!"
            )
            .setPositiveButton("Erlauben") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Später") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // Optional: Test-Daten Funktion (kannst du später löschen)
    private fun insertTestData() {
        val database = CountdownDatabase.getDatabase(this)
        val dao = database.countdownDao()

        lifecycleScope.launch {
            val existing = dao.getAllCountdowns().first()
            if (existing.isNotEmpty()) return@launch

            val testCountdowns = listOf(
                Countdown(
                    title = "🎂 Geburtstag",
                    targetDateTime = LocalDateTime.now().plusDays(7),
                    displayFormat = CountdownDisplayFormat.DAYS_ONLY.name,
                    color = "#FF7043",
                    notificationEnabled = true,
                    reminderOptions = "DAY_1"
                ),
                Countdown(
                    title = "🎄 Weihnachten",
                    targetDateTime = LocalDateTime.of(2025, 12, 24, 0, 0),
                    displayFormat = CountdownDisplayFormat.WEEKS_DAYS.name,
                    color = "#66BB6A"
                ),
                Countdown(
                    title = "🎆 Silvester",
                    targetDateTime = LocalDateTime.of(2025, 12, 31, 0, 0),
                    displayFormat = CountdownDisplayFormat.MONTHS_DAYS.name,
                    color = "#5C6BC0"
                ),
                Countdown(
                    title = "🏖️ Sommerurlaub",
                    targetDateTime = LocalDateTime.of(2026, 7, 15, 0, 0),
                    displayFormat = CountdownDisplayFormat.YEARS_MONTHS_DAYS.name,
                    color = "#42A5F5"
                ),
                Countdown(
                    title = "💍 Hochzeitstag",
                    targetDateTime = LocalDateTime.now().minusYears(2).minusMonths(3),
                    displayFormat = CountdownDisplayFormat.DAYS_ONLY.name,
                    color = "#EC407A"
                ),
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
        }
    }
}