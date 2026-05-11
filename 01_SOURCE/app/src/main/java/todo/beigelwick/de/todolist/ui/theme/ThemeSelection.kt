package todo.beigelwick.de.todolist.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ─── Theme Enum & Config ──────────────────────────────────────────────────────

enum class CustomTheme {
    BURGUNDY, SAGE, PUMPKIN, OCEAN, VIOLET, PEACH  // neu
}

data class ThemeConfig(
    val name: String,
    val lightColorScheme: ColorScheme,
    val darkColorScheme: ColorScheme
)

// ─── PlanIt (Teal) ────────────────────────────────────────────────────────────

val BurgundyLightTheme = lightColorScheme(
    primary              = Color(0xFF5B0E14),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFF8C2A33),
    onPrimaryContainer   = Color(0xFFF1E194),
    secondary            = Color(0xFFF1E194),
    onSecondary          = Color(0xFF3A2E00),
    secondaryContainer   = Color(0xFFD4C570),
    onSecondaryContainer = Color(0xFF2A2200),
    tertiary             = Color(0xFFB5860D),
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFF1E194),
    onTertiaryContainer  = Color(0xFF3A2E00),
    background           = Color(0xFFFFFBF0),
    onBackground         = Color(0xFF1E1209),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF1E1209),
    surfaceVariant       = Color(0xFFF2E0D0),
    onSurfaceVariant     = Color(0xFF4F3A35),
    outline              = Color(0xFF7A5A55),
    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002)
)

val BurgundyDarkTheme = darkColorScheme(
    primary              = Color(0xFFFFB3B8),
    onPrimary            = Color(0xFF5B0E14),
    primaryContainer     = Color(0xFF8C2A33),
    onPrimaryContainer   = Color(0xFFF1E194),
    secondary            = Color(0xFFE8D880),
    onSecondary          = Color(0xFF3A2E00),
    secondaryContainer   = Color(0xFF564500),
    onSecondaryContainer = Color(0xFFF1E194),
    tertiary             = Color(0xFFD4C570),
    onTertiary           = Color(0xFF3A2E00),
    tertiaryContainer    = Color(0xFF564500),
    onTertiaryContainer  = Color(0xFFF1E194),
    background           = Color(0xFF16100A),
    onBackground         = Color(0xFFEDE0D4),
    surface              = Color(0xFF1E1510),
    onSurface            = Color(0xFFEDE0D4),
    surfaceVariant       = Color(0xFF4F3A35),
    onSurfaceVariant     = Color(0xFFD6C0BB),
    outline              = Color(0xFF9E8A85),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6)
)

// ─── Soft Sage + Deep Olive (Combo 02) ───────────────────────────────────────

val SageLightTheme = lightColorScheme(
    primary              = Color(0xFF1A2517),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFF3A4F35),
    onPrimaryContainer   = Color(0xFFACC8A2),
    secondary            = Color(0xFFACC8A2),
    onSecondary          = Color(0xFF1A2517),
    secondaryContainer   = Color(0xFFCFE5C8),
    onSecondaryContainer = Color(0xFF0E1E0C),
    tertiary             = Color(0xFF557A50),
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFACC8A2),
    onTertiaryContainer  = Color(0xFF0E1E0C),
    background           = Color(0xFFF5FAF3),
    onBackground         = Color(0xFF151E14),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF151E14),
    surfaceVariant       = Color(0xFFDCEDD8),
    onSurfaceVariant     = Color(0xFF404E3D),
    outline              = Color(0xFF6F7E6B),
    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002)
)

val SageDarkTheme = darkColorScheme(
    primary              = Color(0xFFACC8A2),
    onPrimary            = Color(0xFF1A2517),
    primaryContainer     = Color(0xFF2D4229),
    onPrimaryContainer   = Color(0xFFACC8A2),
    secondary            = Color(0xFF90B087),
    onSecondary          = Color(0xFF1A2517),
    secondaryContainer   = Color(0xFF2D4229),
    onSecondaryContainer = Color(0xFFCFE5C8),
    tertiary             = Color(0xFFACC8A2),
    onTertiary           = Color(0xFF1A2517),
    tertiaryContainer    = Color(0xFF3A4F35),
    onTertiaryContainer  = Color(0xFFCFE5C8),
    background           = Color(0xFF0E150D),
    onBackground         = Color(0xFFDCEDD8),
    surface              = Color(0xFF151E14),
    onSurface            = Color(0xFFDCEDD8),
    surfaceVariant       = Color(0xFF404E3D),
    onSurfaceVariant     = Color(0xFFBFCDBB),
    outline              = Color(0xFF8A9886),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6)
)

// ─── Pumpkin + Charcoal (Combo 03) ───────────────────────────────────────────

val PumpkinLightTheme = lightColorScheme(
    primary              = Color(0xFFFD802E),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFFFB98A),
    onPrimaryContainer   = Color(0xFF3A1800),
    secondary            = Color(0xFF233D4C),
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFF3D5F70),
    onSecondaryContainer = Color(0xFFD0E8F5),
    tertiary             = Color(0xFFF55F00),
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFFFDBCA),
    onTertiaryContainer  = Color(0xFF3A1800),
    background           = Color(0xFFFFFBFF),
    onBackground         = Color(0xFF201A17),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF201A17),
    surfaceVariant       = Color(0xFFEDDDD4),
    onSurfaceVariant     = Color(0xFF4E403A),
    outline              = Color(0xFF806F68),
    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002)
)

val PumpkinDarkTheme = darkColorScheme(
    primary              = Color(0xFFFFB07A),
    onPrimary            = Color(0xFF3A1800),
    primaryContainer     = Color(0xFFBF5C10),
    onPrimaryContainer   = Color(0xFFFFDBCA),
    secondary            = Color(0xFFA8C8D8),
    onSecondary          = Color(0xFF0F2530),
    secondaryContainer   = Color(0xFF1A3545),
    onSecondaryContainer = Color(0xFFD0E8F5),
    tertiary             = Color(0xFFFFB07A),
    onTertiary           = Color(0xFF3A1800),
    tertiaryContainer    = Color(0xFFBF5C10),
    onTertiaryContainer  = Color(0xFFFFDBCA),
    background           = Color(0xFF171210),
    onBackground         = Color(0xFFEDE0DA),
    surface              = Color(0xFF201A17),
    onSurface            = Color(0xFFEDE0DA),
    surfaceVariant       = Color(0xFF4E403A),
    onSurfaceVariant     = Color(0xFFD3C4BC),
    outline              = Color(0xFF9C8E87),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6)
)

// ─── Cloudy Sky + Ocean Blue (Combo 04) ──────────────────────────────────────

val OceanLightTheme = lightColorScheme(
    primary              = Color(0xFF2872A1),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFCBDDE9),
    onPrimaryContainer   = Color(0xFF00344F),
    secondary            = Color(0xFFCBDDE9),
    onSecondary          = Color(0xFF00344F),
    secondaryContainer   = Color(0xFFE5EFF6),
    onSecondaryContainer = Color(0xFF001E2E),
    tertiary             = Color(0xFF4A90C0),
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFCBDDE9),
    onTertiaryContainer  = Color(0xFF00344F),
    background           = Color(0xFFF5F9FC),
    onBackground         = Color(0xFF181C1F),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF181C1F),
    surfaceVariant       = Color(0xFFDCE8F0),
    onSurfaceVariant     = Color(0xFF3D4D57),
    outline              = Color(0xFF6D7D87),
    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002)
)

val OceanDarkTheme = darkColorScheme(
    primary              = Color(0xFF8DC6E8),
    onPrimary            = Color(0xFF00344F),
    primaryContainer     = Color(0xFF005278),
    onPrimaryContainer   = Color(0xFFCBDDE9),
    secondary            = Color(0xFFB0CDD8),
    onSecondary          = Color(0xFF001E2E),
    secondaryContainer   = Color(0xFF002D42),
    onSecondaryContainer = Color(0xFFCBDDE9),
    tertiary             = Color(0xFF8DC6E8),
    onTertiary           = Color(0xFF00344F),
    tertiaryContainer    = Color(0xFF005278),
    onTertiaryContainer  = Color(0xFFCBDDE9),
    background           = Color(0xFF0F1417),
    onBackground         = Color(0xFFDDE3E8),
    surface              = Color(0xFF181C1F),
    onSurface            = Color(0xFFDDE3E8),
    surfaceVariant       = Color(0xFF3D4D57),
    onSurfaceVariant     = Color(0xFFBDCAD5),
    outline              = Color(0xFF87979F),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6)
)

// ─── Lemon Chiffon + Ultra Violet (Combo 05) ─────────────────────────────────

val VioletLightTheme = lightColorScheme(
    primary              = Color(0xFF5F4A8B),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFEADDFF),
    onPrimaryContainer   = Color(0xFF21005D),
    secondary            = Color(0xFFFEFACD),
    onSecondary          = Color(0xFF3A3500),
    secondaryContainer   = Color(0xFFFEFACD),
    onSecondaryContainer = Color(0xFF3A3500),
    tertiary             = Color(0xFF7C5FB5),
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFEADDFF),
    onTertiaryContainer  = Color(0xFF21005D),
    background           = Color(0xFFFFFBFF),
    onBackground         = Color(0xFF1D1A22),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF1D1A22),
    surfaceVariant       = Color(0xFFE9E0F0),
    onSurfaceVariant     = Color(0xFF4A4454),
    outline              = Color(0xFF7A7484),
    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002)
)

val VioletDarkTheme = darkColorScheme(
    primary              = Color(0xFFCFBDFF),
    onPrimary            = Color(0xFF38006D),
    primaryContainer     = Color(0xFF4F3380),
    onPrimaryContainer   = Color(0xFFEADDFF),
    secondary            = Color(0xFFE8E090),
    onSecondary          = Color(0xFF3A3500),
    secondaryContainer   = Color(0xFF524D00),
    onSecondaryContainer = Color(0xFFFEFACD),
    tertiary             = Color(0xFFCFBDFF),
    onTertiary           = Color(0xFF38006D),
    tertiaryContainer    = Color(0xFF4F3380),
    onTertiaryContainer  = Color(0xFFEADDFF),
    background           = Color(0xFF131018),
    onBackground         = Color(0xFFE6E1EB),
    surface              = Color(0xFF1D1A22),
    onSurface            = Color(0xFFE6E1EB),
    surfaceVariant       = Color(0xFF4A4454),
    onSurfaceVariant     = Color(0xFFCCC4D6),
    outline              = Color(0xFF958F9F),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6)
)

// ─── Peach Ice + Aqua Mist (Combo 06) ────────────────────────────────────────

val PeachLightTheme = lightColorScheme(
    primary              = Color(0xFF789A99),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFFFD2C2),
    onPrimaryContainer   = Color(0xFF2C1500),
    secondary            = Color(0xFFFFD2C2),
    onSecondary          = Color(0xFF2C1500),
    secondaryContainer   = Color(0xFFFFEAE0),
    onSecondaryContainer = Color(0xFF1A0A00),
    tertiary             = Color(0xFF5A8180),
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFB8D8D8),
    onTertiaryContainer  = Color(0xFF002020),
    background           = Color(0xFFFFF8F6),
    onBackground         = Color(0xFF1E1B1A),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF1E1B1A),
    surfaceVariant       = Color(0xFFEDD8D0),
    onSurfaceVariant     = Color(0xFF4E3D3A),
    outline              = Color(0xFF806C69),
    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002)
)

val PeachDarkTheme = darkColorScheme(
    primary              = Color(0xFF9DBDBC),
    onPrimary            = Color(0xFF002020),
    primaryContainer     = Color(0xFF3D6666),
    onPrimaryContainer   = Color(0xFFB8D8D8),
    secondary            = Color(0xFFFFB59A),
    onSecondary          = Color(0xFF2C1500),
    secondaryContainer   = Color(0xFF4A2800),
    onSecondaryContainer = Color(0xFFFFD2C2),
    tertiary             = Color(0xFF9DBDBC),
    onTertiary           = Color(0xFF002020),
    tertiaryContainer    = Color(0xFF3D6666),
    onTertiaryContainer  = Color(0xFFB8D8D8),
    background           = Color(0xFF161210),
    onBackground         = Color(0xFFEDE0DC),
    surface              = Color(0xFF1E1B1A),
    onSurface            = Color(0xFFEDE0DC),
    surfaceVariant       = Color(0xFF4E3D3A),
    onSurfaceVariant     = Color(0xFFD3C2BE),
    outline              = Color(0xFF9C8C89),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6)
)

// ─── Theme Config Selector ────────────────────────────────────────────────────

fun getThemeConfig(theme: CustomTheme): ThemeConfig = when (theme) {
    CustomTheme.BURGUNDY -> ThemeConfig("Deep Burgundy", BurgundyLightTheme, BurgundyDarkTheme)
    CustomTheme.SAGE     -> ThemeConfig("Soft Sage",      SageLightTheme,     SageDarkTheme)
    CustomTheme.PUMPKIN  -> ThemeConfig("Pumpkin",        PumpkinLightTheme,  PumpkinDarkTheme)
    CustomTheme.OCEAN    -> ThemeConfig("Ocean Blue",     OceanLightTheme,    OceanDarkTheme)
    CustomTheme.VIOLET   -> ThemeConfig("Ultra Violet",   VioletLightTheme,   VioletDarkTheme)
    CustomTheme.PEACH    -> ThemeConfig("Peach Ice",      PeachLightTheme,    PeachDarkTheme)
}

// ─── CustomTheme Preferences ──────────────────────────────────────────────────

object CustomThemePreferences {
    private val CUSTOM_THEME = stringPreferencesKey("custom_theme")

    fun getCustomTheme(context: Context): Flow<CustomTheme> =
        context.dataStore.data.map { prefs ->
            try {
                CustomTheme.valueOf(prefs[CUSTOM_THEME] ?: CustomTheme.BURGUNDY.name)
            } catch (e: Exception) {
                CustomTheme.BURGUNDY
            }
        }

    suspend fun setCustomTheme(context: Context, theme: CustomTheme) {
        context.dataStore.edit { prefs ->
            prefs[CUSTOM_THEME] = theme.name
        }
    }
}