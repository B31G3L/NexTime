package de.beigel.nextime

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import de.beigel.nextime.notifications.CountdownNotificationManager
import de.beigel.nextime.ui.theme.NexTimeTheme
import de.beigel.nextime.ui.theme.ThemePreferences
import de.beigel.nextime.ui.screens.MainScreen
import kotlinx.coroutines.launch

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
    }
}