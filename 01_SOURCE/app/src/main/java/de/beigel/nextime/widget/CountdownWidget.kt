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
 * Dynamisches Widget, das sich automatisch an die Größe anpasst
 * Öffnet die App beim Klick
 */
class CountdownWidget : GlanceAppWidget() {

    companion object {
        private val MINI_SIZE = DpSize(70.dp, 70.dp)
        private val SMALL_SIZE = DpSize(110.dp, 110.dp)
        private val MEDIUM_SIZE = DpSize(250.dp, 110.dp)
        private val LARGE_SIZE = DpSize(250.dp, 150.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(MINI_SIZE, SMALL_SIZE, MEDIUM_SIZE, LARGE_SIZE)
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
                    size.width <= 90.dp && size.height <= 90.dp -> MiniLayout(countdown)      // 1×1
                    size.width <= 170.dp && size.height <= 170.dp -> SmallLayout(countdown)    // 2×2
                    size.height < 200.dp -> MediumLayout(countdown)                             // 4×2 und kleiner
                    else -> LargeLayout(countdown)
                }
        }  }
    }

    @Composable
    private fun MiniLayout(countdown: Countdown?) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),
            contentAlignment = Alignment.Center
        ) {
            if (countdown == null) {
                Text(
                    text = "⏰",
                    style = TextStyle(
                        fontSize = 24.sp,
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
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Text(
                        text = countdown.title,
                        style = TextStyle(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF333333))
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(accentColor)
                        )
                    )

                    Text(
                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = ColorProvider(Color(0xFF666666))
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

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
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    Text(
                        text = countdown.title,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color(0xFF333333))
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(accentColor)
                        )
                    )

                    Text(
                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorProvider(Color(0xFF666666))
                        )
                    )

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentColor)
                    ) { }
                }
            }
        }
    }

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
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = countdown.title,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color(0xFF333333))
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(10.dp))

                        Text(
                            text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = ColorProvider(Color(0xFF666666))
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    FormatDisplay(timeInfo, format, accentColor, false)

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentColor)
                    ) { }
                }
            }
        }
    }

    @Composable
    private fun LargeLayout(countdown: Countdown?) {
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
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentColor)
                    ) { }

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = countdown.title,
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(Color(0xFF333333))
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(10.dp))

                        Text(
                            text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = ColorProvider(Color(0xFF666666))
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    FormatDisplay(timeInfo, format, accentColor, true)

                    Spacer(modifier = GlanceModifier.defaultWeight())

                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F0F0))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Erstellt:",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF666666))
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Text(
                                text = countdown.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(Color(0xFF333333))
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(6.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verbleibend:",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = ColorProvider(Color(0xFF666666))
                                ),
                                modifier = GlanceModifier.defaultWeight()
                            )
                            Text(
                                text = calculatePercentage(countdown, timeInfo),
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorProvider(accentColor)
                                )
                            )
                        }
                    }

                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(accentColor)
                    ) { }
                }
            }
        }
    }

    @Composable
    private fun EmptyState() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⏰",
                style = TextStyle(
                    fontSize = 32.sp,
                    color = ColorProvider(Color(0xFFBBBBBB))
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Kein Countdown",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(Color(0xFF999999))
                )
            )
        }
    }

    @Composable
    private fun FormatDisplay(
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        format: CountdownDisplayFormat,
        accentColor: Color,
        isLarge: Boolean
    ) {
        val fontSize = if (isLarge) 70.sp else 64.sp
        val labelSize = if (isLarge) 18.sp else 16.sp
        val secondarySize = if (isLarge) 42.sp else 32.sp
        val secondaryLabelSize = if (isLarge) 14.sp else 12.sp

        when (format) {
            CountdownDisplayFormat.DAYS_ONLY -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${timeInfo.days}",
                        style = TextStyle(
                            fontSize = fontSize,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(accentColor)
                        )
                    )
                    Text(
                        text = if (timeInfo.days == 1L) "Tag" else "Tage",
                        style = TextStyle(
                            fontSize = labelSize,
                            color = ColorProvider(Color(0xFF333333))
                        )
                    )
                }
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
                                fontSize = (fontSize.value - 16f).sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor)
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = if (timeInfo.weeks == 1L) "Woche" else "Wochen",
                            style = TextStyle(
                                fontSize = (fontSize.value - 2f).sp,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            modifier = GlanceModifier.padding(bottom = 8.dp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.height(if (isLarge) 8.dp else 4.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$remainingDays",
                            style = TextStyle(
                                fontSize = secondarySize,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor.copy(alpha = 0.7f))
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = if (remainingDays == 1L) "Tag" else "Tage",
                            style = TextStyle(
                                fontSize = secondaryLabelSize,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            modifier = GlanceModifier.padding(bottom = if (isLarge) 4.dp else 2.dp)
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
                                fontSize = (fontSize.value - 16f).sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor)
                            )
                        )
                        Spacer(modifier = GlanceModifier.width(6.dp))
                        Text(
                            text = if (timeInfo.months == 1L) "Monat" else "Monate",
                            style = TextStyle(
                                fontSize = (fontSize.value - 2f).sp,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            modifier = GlanceModifier.padding(bottom = 8.dp)
                        )
                    }
                    if (remainingDays > 0) {
                        Spacer(modifier = GlanceModifier.height(if (isLarge) 8.dp else 4.dp))
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$remainingDays",
                                style = TextStyle(
                                    fontSize = secondarySize,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(accentColor.copy(alpha = 0.7f))
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Text(
                                text = if (remainingDays == 1L) "Tag" else "Tage",
                                style = TextStyle(
                                    fontSize = secondaryLabelSize,
                                    color = ColorProvider(Color(0xFF333333))
                                ),
                                modifier = GlanceModifier.padding(bottom = if (isLarge) 4.dp else 2.dp)
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
                                    fontSize = if (isLarge) 48.sp else 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorProvider(accentColor)
                                )
                            )
                            Text(
                                text = "J ",
                                style = TextStyle(
                                    fontSize = if (isLarge) 16.sp else 14.sp,
                                    color = ColorProvider(Color(0xFF333333))
                                ),
                                modifier = GlanceModifier.padding(bottom = if (isLarge) 6.dp else 4.dp)
                            )
                        }
                        if (timeInfo.months > 0 || timeInfo.years > 0) {
                            val remainingMonths = timeInfo.months % 12
                            if (remainingMonths > 0) {
                                Text(
                                    text = "$remainingMonths",
                                    style = TextStyle(
                                        fontSize = if (isLarge) 48.sp else 38.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorProvider(accentColor)
                                    )
                                )
                                Text(
                                    text = "M ",
                                    style = TextStyle(
                                        fontSize = if (isLarge) 16.sp else 14.sp,
                                        color = ColorProvider(Color(0xFF333333))
                                    ),
                                    modifier = GlanceModifier.padding(bottom = if (isLarge) 6.dp else 4.dp)
                                )
                            }
                        }
                        val remainingDays = timeInfo.days % 30
                        Text(
                            text = "$remainingDays",
                            style = TextStyle(
                                fontSize = if (isLarge) 48.sp else 38.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(accentColor)
                            )
                        )
                        Text(
                            text = "T",
                            style = TextStyle(
                                fontSize = if (isLarge) 16.sp else 14.sp,
                                color = ColorProvider(Color(0xFF333333))
                            ),
                            modifier = GlanceModifier.padding(bottom = if (isLarge) 6.dp else 4.dp)
                        )
                    }
                }
            }
        }
    }

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

    private fun calculatePercentage(
        countdown: Countdown,
        timeInfo: de.beigel.nextime.data.model.CountdownInfo
    ): String {
        val now = java.time.LocalDateTime.now()
        val total = java.time.temporal.ChronoUnit.DAYS.between(
            countdown.createdAt.toLocalDate(),
            countdown.targetDateTime.toLocalDate()
        )
        val remaining = timeInfo.days

        return if (total > 0) {
            val percentage = (remaining.toFloat() / total.toFloat() * 100).toInt()
            "$percentage%"
        } else {
            "0%"
        }
    }
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()
}