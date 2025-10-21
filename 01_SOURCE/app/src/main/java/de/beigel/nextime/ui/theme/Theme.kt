package de.beigel.nextime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun NexTimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    customTheme: CustomTheme = CustomTheme.NEXTIME,  // ← WICHTIG: Parameter hinzufügen
    content: @Composable () -> Unit
) {
    // Hole Theme-Konfiguration basierend auf ausgewähltem Theme
    val themeConfig = getThemeConfig(customTheme)

    // Wähle Light oder Dark Variant
    val colorScheme = if (darkTheme) {
        themeConfig.darkColorScheme
    } else {
        themeConfig.lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// Typography
private val AppTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Normal
    ),
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold
    ),
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    )
)
