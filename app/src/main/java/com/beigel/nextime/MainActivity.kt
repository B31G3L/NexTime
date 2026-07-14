package com.beigel.nextime

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.beigel.nextime.data.database.CountdownDatabase
import com.beigel.nextime.notifications.CountdownNotificationManager
import com.beigel.nextime.notifications.NotificationScheduler
import com.beigel.nextime.ui.navigation.AppNavigation
import com.beigel.nextime.ui.theme.AccentColor
import com.beigel.nextime.ui.theme.AccentColorPreferences
import com.beigel.nextime.ui.theme.NexTimeTheme
import com.beigel.nextime.ui.theme.ThemeMode
import com.beigel.nextime.ui.theme.ThemePreferences
import kotlin.collections.forEach

class MainActivity : androidx.appcompat.app.AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        _root_ide_package_.com.beigel.nextime.notifications.CountdownNotificationManager.createNotificationChannel(this)

        // Bestehende Benachrichtigungen nach Neustart neu einplanen —
        // kein Permission-Dialog hier, nur stille Wiederherstellung.
        scheduleAllPendingNotifications()

        setContent {
            val themeMode   by _root_ide_package_.com.beigel.nextime.ui.theme.ThemePreferences.getThemeMode(this).collectAsState(initial = _root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.SYSTEM)
            val accentColor by _root_ide_package_.com.beigel.nextime.ui.theme.AccentColorPreferences.getAccentColor(this).collectAsState(initial = _root_ide_package_.com.beigel.nextime.ui.theme.AccentColor.ORANGE)
            val systemDark  = isSystemInDarkTheme()

            val isDark = when (themeMode) {
                _root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.SYSTEM -> systemDark
                _root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.LIGHT  -> false
                _root_ide_package_.com.beigel.nextime.ui.theme.ThemeMode.DARK   -> true
            }

            val view = LocalView.current
            SideEffect {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars    = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }

            _root_ide_package_.com.beigel.nextime.ui.theme.NexTimeTheme(
                darkTheme = isDark,
                accentColor = accentColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun scheduleAllPendingNotifications() {
        val database = _root_ide_package_.com.beigel.nextime.data.database.CountdownDatabase.Companion.getDatabase(this)
        lifecycleScope.launch {
            val countdowns = database.countdownDao().getAllCountdowns().first()
            countdowns.forEach { countdown ->
                if (countdown.notificationEnabled) {
                    _root_ide_package_.com.beigel.nextime.notifications.NotificationScheduler.scheduleNotifications(this@MainActivity, countdown)
                }
            }
        }
    }
}