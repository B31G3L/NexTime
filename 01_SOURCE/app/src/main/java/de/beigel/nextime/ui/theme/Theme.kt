package de.beigel.nextime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Farbpalette
val NexTimeOrange = Color(0xFFFF7043)
val NexTimeDarkGray = Color(0xFF424242)
val NexTimeLightGray = Color(0xFFBDBDBD)
val NexTimeWhite = Color(0xFFFFFFFF)
val NexTimeBlack = Color(0xFF000000)

private val LightColorScheme = lightColorScheme(
    primary = NexTimeOrange,
    onPrimary = NexTimeWhite,
    secondary = NexTimeDarkGray,
    onSecondary = NexTimeWhite,
    background = NexTimeWhite,
    onBackground = NexTimeDarkGray,
    surface = NexTimeWhite,
    onSurface = NexTimeDarkGray,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = NexTimeDarkGray,
    outline = NexTimeLightGray
)

private val DarkColorScheme = darkColorScheme(
    primary = NexTimeOrange,
    onPrimary = NexTimeWhite,
    secondary = NexTimeLightGray,
    onSecondary = NexTimeBlack,
    background = Color(0xFF121212),
    onBackground = NexTimeWhite,
    surface = Color(0xFF1E1E1E),
    onSurface = NexTimeWhite,
    surfaceVariant = NexTimeDarkGray,
    onSurfaceVariant = NexTimeWhite,
    outline = NexTimeLightGray
)

@Composable
fun NexTimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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