package de.beigel.nextime

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import de.beigel.nextime.ui.theme.CustomThemePreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import de.beigel.nextime.notifications.CountdownNotificationManager
import de.beigel.nextime.notifications.NotificationScheduler
import de.beigel.nextime.ui.theme.NexTimeTheme
import de.beigel.nextime.ui.theme.ThemeMode
import de.beigel.nextime.ui.theme.ThemePreferences
import de.beigel.nextime.ui.screens.MainScreenWithBottomNav
import de.beigel.nextime.ui.theme.CustomTheme
import de.beigel.nextime.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, getString(R.string.perm_notif_ok), Toast.LENGTH_SHORT).show()
            checkExactAlarmPermission()
        } else {
            Toast.makeText(this, getString(R.string.perm_notif_denied), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CountdownNotificationManager.createNotificationChannel(this)
        requestNotificationPermissionIfNeeded()
        scheduleAllPendingNotifications()

        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val themeMode by ThemePreferences.getThemeMode(context).collectAsState(initial = ThemeMode.SYSTEM)
            val systemDarkTheme = isSystemInDarkTheme()
            val customTheme by CustomThemePreferences.getCustomTheme(context).collectAsState(initial = CustomTheme.NEXTIME)

            val isDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDarkTheme
                ThemeMode.LIGHT  -> false
                ThemeMode.DARK   -> true
            }

            NexTimeTheme(darkTheme = isDarkTheme, customTheme = customTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreenWithBottomNav()
                }
            }
        }
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
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    checkExactAlarmPermission()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionRationaleDialog()
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            checkExactAlarmPermission()
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                showExactAlarmPermissionDialog()
            }
        }
    }

    private fun showExactAlarmPermissionDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.alarm_dialog_title))
            .setMessage(getString(R.string.alarm_dialog_msg))
            .setPositiveButton(getString(R.string.alarm_to_settings)) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    try {
                        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.parse("package:$packageName")
                        })
                    } catch (e: Exception) {
                        Toast.makeText(this, getString(R.string.alarm_fallback), Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.alarm_later)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showPermissionRationaleDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.notif_dialog_title))
            .setMessage(getString(R.string.notif_dialog_msg))
            .setPositiveButton(getString(R.string.notif_allow)) { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton(getString(R.string.alarm_later)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}