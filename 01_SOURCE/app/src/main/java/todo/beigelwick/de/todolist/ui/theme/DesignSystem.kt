package todo.beigelwick.de.todolist.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Zentrales Design-System für einheitliche UI-Werte
 */
object DesignSystem {

    object Spacing {
        val xxSmall = 4.dp
        val xSmall  = 8.dp
        val small   = 12.dp
        val medium  = 16.dp
        val large   = 20.dp
        val xLarge  = 24.dp
        val xxLarge = 32.dp
    }

    object Card {
        val cornerRadius         = 16.dp
        val elevation            = 0.dp
        val minHeight            = 100.dp
    }

    object Icon {
        val small   = 16.dp
        val medium  = 20.dp
        val large   = 24.dp
        val xLarge  = 32.dp
        val xxLarge = 48.dp
    }

    object Typography {
        val countdownLarge   = 28.sp
        val countdownXLarge  = 48.sp
    }

    object Alpha {
        const val disabled      = 0.38f
        const val medium        = 0.5f
        const val subtle        = 0.7f
        const val verySubtle    = 0.05f
        const val cardBackground = 0.15f
        const val surface       = 0.1f
        const val divider       = 0.3f
    }

    object CornerRadius {
        val small   = 8.dp
        val medium  = 12.dp
        val large   = 16.dp
        val xLarge  = 20.dp
        val xxLarge = 24.dp
        val circle  = 50.dp
    }

    object Border {
        val thin   = 1.dp
        val medium = 2.dp
        val thick  = 3.dp
    }

    object Widget {
        val padding            = 16.dp
        val smallColorBarWidth = 40.dp
        val colorBarHeight     = 3.dp
    }
}