package de.beigel.nextime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import de.beigel.nextime.ui.theme.NexTimeTheme
import de.beigel.nextime.ui.theme.ThemePreferences
import de.beigel.nextime.ui.screens.MainScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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