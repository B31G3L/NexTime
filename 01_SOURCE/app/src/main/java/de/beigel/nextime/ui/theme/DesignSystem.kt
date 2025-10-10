package de.beigel.nextime.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Zentrales Design-System für einheitliche UI-Werte
 */
object DesignSystem {

    // Spacing - Einheitliche Abstände
    object Spacing {
        val xxSmall = 4.dp
        val xSmall = 8.dp
        val small = 12.dp
        val medium = 16.dp
        val large = 20.dp
        val xLarge = 24.dp
        val xxLarge = 32.dp
    }

    // Card - Einheitliche Card-Werte
    object Card {
        val cornerRadius = 24.dp
        val elevation = 4.dp
        val minHeight = 200.dp
        val minHeightWithTime = 220.dp
    }

    // Icon - Einheitliche Icon-Größen
    object Icon {
        val small = 16.dp
        val medium = 20.dp
        val large = 24.dp
        val xLarge = 32.dp
        val xxLarge = 48.dp
    }

    // Typography - Zusätzliche Größen
    object Typography {
        val countdownLarge = 48.sp
        val countdownXLarge = 64.sp
        val countdownXXLarge = 96.sp
    }

    // Alpha - Einheitliche Transparenzwerte
    object Alpha {
        const val disabled = 0.38f
        const val medium = 0.5f
        const val subtle = 0.7f
        const val verySubtle = 0.05f
        const val cardBackground = 0.15f
        const val surface = 0.1f
        const val divider = 0.3f
    }

    // Corner Radius - Einheitliche Rundungen
    object CornerRadius {
        val small = 8.dp
        val medium = 12.dp
        val large = 16.dp
        val xLarge = 20.dp
        val xxLarge = 24.dp
        val circle = 50.dp
    }

    // Border - Einheitliche Rahmen
    object Border {
        val thin = 1.dp
        val medium = 2.dp
        val thick = 3.dp
    }

    // Widget - Spezifische Widget-Werte
    object Widget {
        val padding = 16.dp
        val smallColorBarWidth = 40.dp
        val smallColorBarHeight = 4.dp
        val mediumColorBarWidth = 60.dp
    }
}