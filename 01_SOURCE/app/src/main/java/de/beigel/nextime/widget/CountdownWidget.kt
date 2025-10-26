package de.beigel.nextime.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import de.beigel.nextime.MainActivity
import de.beigel.nextime.data.database.CountdownDatabase
import de.beigel.nextime.data.model.Countdown
import de.beigel.nextime.data.model.CountdownDisplayFormat
import de.beigel.nextime.data.model.calculateTimeRemaining
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter

/**
 * Dynamisches Widget mit 4 verschiedenen Größen:
 * - MINI (1×1):   Nur die Zahl, minimalistisch
 * - SMALL (2×2):  Zahl + Label + Titel
 * - MEDIUM (4×2): Mit Datum und Format
 * - LARGE (4×3):  Vollständig mit Statistiken
 */
class CountdownWidget : GlanceAppWidget() {

    companion object {
        // Größen-Definitionen
        private val MINI_SIZE = DpSize(70.dp, 70.dp)       // 1×1
        private val SMALL_SIZE = DpSize(140.dp, 70.dp)     // 2×1
        private val SMALL_XL_SIZE = DpSize(210.dp, 70.dp)  // 3×1 und 4×1
        private val MEDIUM_SIZE = DpSize(140.dp, 140.dp)   // 2×2, 2×3, 2×4
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(MINI_SIZE, SMALL_SIZE, SMALL_XL_SIZE, MEDIUM_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = CountdownDatabase.getDatabase(context)
        val countdowns = database.countdownDao().getAllCountdowns().first()
        val countdown = countdowns.firstOrNull()

        provideContent {
            val size = LocalSize.current
            val clickAction = actionStartActivity<MainActivity>()

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(clickAction)
            ) {
                when {
                    // MINI: 1×1 - Titel + Farbbalken + Zahl + "Tage"
                    size.width < 100.dp && size.height < 100.dp -> MiniLayout(countdown)

                    // SMALL: 2×1 - Titel + Zahl + "Tage"
                    size.width < 180.dp && size.height < 100.dp -> SmallLayout(countdown)

                    // MEDIUM: 2×2, 2×3, 2×4 - Titel links + Datum rechts + Format + "Tage"
                    size.width < 180.dp -> MediumLayout(countdown)

                    // SMALL_XL: 3×1 und 4×1 - Titel links + Datum rechts + Zahl + "Tage"
                    else -> SmallXLLayout(countdown)
                }
            }
        }
    }

    /**
     * MINI Layout (1×1) - 70x70dp
     * Titel + Farbbalken oben + Zahl + "Tage" unten + Farbbalken unten
     */
    @Composable
    private fun MiniLayout(countdown: Countdown?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),
            contentAlignment = Alignment.Center
        ) {
            if (countdown == null) {
                // Leeres Widget
                Text(
                    text = "⏰",
                    style = TextStyle(
                        fontSize = 32.sp,
                        color = ColorProvider(Color(0xFFBBBBBB))
                    )
                )
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val accentColor = getAccentColor(countdown.color)

                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Top
                ) {
                    // Farbbalken oben
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.height(2.dp))

                    // Titel (sehr klein, 1 Zeile)
                    Text(
                        text = countdown.title,
                        style = TextStyle(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF333333))
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Hauptzahl
                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(accentColor)
                        )
                    )

                    // "Tage" Label
                    Text(
                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                        style = TextStyle(
                            fontSize = 8.sp,
                            color = ColorProvider(Color(0xFF666666))
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Farbbalken unten
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }
                }
            }
        }
    }

    /**
     * SMALL Layout (2×1) - 140x70dp
     * Titel zentriert + Zahl + "Tage" unten
     */
    @Composable
    private fun SmallLayout(countdown: Countdown?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),
            contentAlignment = Alignment.Center
        ) {
            if (countdown == null) {
                EmptyState()
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val accentColor = getAccentColor(countdown.color)

                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Top
                ) {
                    // Farbbalken oben
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Titel - zentriert, 1 Zeile
                    Text(
                        text = countdown.title,
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF333333))
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Hauptzahl
                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(accentColor)
                        )
                    )

                    // "Tage" Label
                    Text(
                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = ColorProvider(Color(0xFF666666))
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Farbbalken unten
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }
                }
            }
        }
    }

    /**
     * SMALL_XL Layout (3×1 und 4×1) - ab 210x70dp
     * Titel linksbündig + Datum rechtsbündig + Zahl + "Tage" unten
     */
    @Composable
    private fun SmallXLLayout(countdown: Countdown?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),
            contentAlignment = Alignment.Center
        ) {
            if (countdown == null) {
                EmptyState()
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val accentColor = getAccentColor(countdown.color)

                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Top
                ) {
                    // Farbbalken oben
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Header: Titel links + Datum rechts
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Titel linksbündig
                        Text(
                            text = countdown.title,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            maxLines = 1,
                            modifier = GlanceModifier.defaultWeight()
                        )

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // Datum rechtsbündig
                        Text(
                            text = countdown.targetDateTime.format(
                                DateTimeFormatter.ofPattern("dd.MM.yy")
                            ),
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = ColorProvider(Color(0xFF666666))
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Hauptzahl
                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(accentColor)
                        )
                    )

                    // "Tage" Label
                    Text(
                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = ColorProvider(Color(0xFF666666))
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Farbbalken unten
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }
                }
            }
        }
    }

    /**
     * MEDIUM Layout (2×2, 2×3, 2×4) - 140x140dp+
     * Titel linksbündig + Datum rechtsbündig + Formatierte Anzeige + "Tage" darunter
     */
    @Composable
    private fun MediumLayout(countdown: Countdown?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),
            contentAlignment = Alignment.Center
        ) {
            if (countdown == null) {
                EmptyState()
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val accentColor = getAccentColor(countdown.color)
                val format = getDisplayFormat(countdown.displayFormat)

                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.Top
                ) {
                    // Farbbalken oben
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.height(6.dp))

                    // Header: Titel links + Datum rechts
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Titel linksbündig
                        Text(
                            text = countdown.title,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            maxLines = 1,
                            modifier = GlanceModifier.defaultWeight()
                        )

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // Datum rechtsbündig
                        Text(
                            text = countdown.targetDateTime.format(
                                DateTimeFormatter.ofPattern("dd.MM.yy")
                            ),
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = ColorProvider(Color(0xFF666666))
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Formatierte Anzeige (größer als SmallXL)
                    FormatDisplay(
                        timeInfo = timeInfo,
                        format = format,
                        accentColor = accentColor
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // "X Tage" darunter (als Zusatzinfo)
                    Text(
                        text = "${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"}",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = ColorProvider(Color(0xFF888888))
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    // Farbbalken unten
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }
                }
            }
        }
    }

    /**
     * Leeres Widget (wenn keine Countdowns vorhanden)
     */
    @Composable
    private fun EmptyState() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏰",
                style = TextStyle(
                    fontSize = 24.sp,
                    color = ColorProvider(Color(0xFFBBBBBB))
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "Kein\nCountdown",
                style = TextStyle(
                    fontSize = 9.sp,
                    color = ColorProvider(Color(0xFF999999))
                )
            )
        }
    }

    /**
     * Formatierte Zeitanzeige für MEDIUM Widget
     */
    @Composable
    private fun FormatDisplay(
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        format: CountdownDisplayFormat,
        accentColor: Color
    ) {
        when (format) {
            CountdownDisplayFormat.DAYS_ONLY -> {
                // Bei DAYS_ONLY zeige nur die Zahl
                Text(
                    text = "${timeInfo.days}",
                    style = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(accentColor)
                    )
                )
            }

            CountdownDisplayFormat.WEEKS_DAYS -> {
                val remainingDays = timeInfo.days % 7
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${timeInfo.weeks}",
                            style = TextStyle(
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor)
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = if (timeInfo.weeks == 1L) "Woche" else "Wochen",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            modifier = GlanceModifier.padding(bottom = 6.dp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$remainingDays",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor.copy(alpha = 0.7f))
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(3.dp))
                        Text(
                            text = if (remainingDays == 1L) "Tag" else "Tage",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            modifier = GlanceModifier.padding(bottom = 2.dp)
                        )
                    }
                }
            }

            CountdownDisplayFormat.MONTHS_DAYS -> {
                val remainingDays = timeInfo.days % 30
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${timeInfo.months}",
                            style = TextStyle(
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor)
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(
                            text = if (timeInfo.months == 1L) "Monat" else "Monate",
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            modifier = GlanceModifier.padding(bottom = 6.dp)
                        )
                    }
                    if (remainingDays > 0) {
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$remainingDays",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(accentColor.copy(alpha = 0.7f))
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(
                                text = if (remainingDays == 1L) "Tag" else "Tage",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = ColorProvider(Color(0xFF333333))
                                ),
                                modifier = GlanceModifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }

            CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (timeInfo.years > 0) {
                            Text(
                                text = "${timeInfo.years}",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(accentColor)
                                )
                            )
                            Text(
                                text = "J ",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = ColorProvider(Color(0xFF333333))
                                ),
                                modifier = GlanceModifier.padding(bottom = 4.dp)
                            )
                        }
                        if (timeInfo.months > 0 || timeInfo.years > 0) {
                            val remainingMonths = timeInfo.months % 12
                            if (remainingMonths > 0) {
                                Text(
                                    text = "$remainingMonths",
                                    style = TextStyle(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(accentColor)
                                    )
                                )
                                Text(
                                    text = "M ",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        color = ColorProvider(Color(0xFF333333))
                                    ),
                                    modifier = GlanceModifier.padding(bottom = 4.dp)
                                )
                            }
                        }
                        val remainingDays = timeInfo.days % 30
                        Text(
                            text = "$remainingDays",
                            style = TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor)
                            )
                        )
                        Text(
                            text = "T",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            modifier = GlanceModifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }

    /**
     * Hilfsfunktionen
     */
    private fun getAccentColor(colorHex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color(0xFFFF9800)
        }
    }

    private fun getDisplayFormat(formatString: String): CountdownDisplayFormat {
        return try {
            CountdownDisplayFormat.valueOf(formatString)
        } catch (e: Exception) {
            CountdownDisplayFormat.DAYS_ONLY
        }
    }
}


/**
 * Widget Receiver
 */
class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}