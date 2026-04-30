package de.beigel.nextime.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
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

class CountdownWidget : GlanceAppWidget() {

    companion object {
        private val MINI_SIZE    = DpSize(70.dp, 70.dp)
        private val SMALL_SIZE   = DpSize(140.dp, 70.dp)
        private val SMALL_XL_SIZE = DpSize(210.dp, 70.dp)
        private val MEDIUM_SIZE  = DpSize(140.dp, 140.dp)

        private val LIGHT_BACKGROUND     = Color(0xFFFAFAFA)
        private val LIGHT_TEXT_PRIMARY   = Color(0xFF333333)
        private val LIGHT_TEXT_SECONDARY = Color(0xFF666666)
        private val LIGHT_TEXT_TERTIARY  = Color(0xFF888888)
        private val LIGHT_EMPTY          = Color(0xFFBBBBBB)

        private val DARK_BACKGROUND     = Color(0xFF1E1E1E)
        private val DARK_TEXT_PRIMARY   = Color(0xFFE0E0E0)
        private val DARK_TEXT_SECONDARY = Color(0xFFB0B0B0)
        private val DARK_TEXT_TERTIARY  = Color(0xFF808080)
        private val DARK_EMPTY          = Color(0xFF606060)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(MINI_SIZE, SMALL_SIZE, SMALL_XL_SIZE, MEDIUM_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // FIX: Korrekte appWidgetId für dieses spezifische GlanceId ermitteln
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val glanceManager = GlanceAppWidgetManager(context)

        val appWidgetId = try {
            glanceManager.getAppWidgetId(id)
        } catch (e: Exception) {
            // Fallback falls getAppWidgetId nicht verfügbar
            appWidgetManager
                .getAppWidgetIds(ComponentName(context, CountdownWidgetReceiver::class.java))
                .firstOrNull() ?: AppWidgetManager.INVALID_APPWIDGET_ID
        }

        val database = CountdownDatabase.getDatabase(context)
        val selectedCountdownId = WidgetConfigActivity.loadCountdownId(context, appWidgetId)

        val countdown = if (selectedCountdownId != null) {
            database.countdownDao().getCountdownById(selectedCountdownId)
        } else {
            database.countdownDao().getAllCountdowns().first().firstOrNull()
        }

        val isDarkMode = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        provideContent {
            val size = LocalSize.current
            val clickAction = actionStartActivity<MainActivity>()

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(if (isDarkMode) DARK_BACKGROUND else LIGHT_BACKGROUND)
                    .clickable(clickAction)
            ) {
                when {
                    size.width >= MEDIUM_SIZE.width || size.height >= MEDIUM_SIZE.height ->
                        MediumLayout(countdown, isDarkMode)
                    size.width <= MINI_SIZE.width && size.height <= MINI_SIZE.height ->
                        MiniLayout(countdown, isDarkMode)
                    size.width >= SMALL_SIZE.width && size.width < SMALL_XL_SIZE.width && size.height <= MINI_SIZE.height ->
                        SmallLayout(countdown, isDarkMode)
                    size.width >= SMALL_XL_SIZE.width && size.height <= MINI_SIZE.height ->
                        SmallXLLayout(countdown, isDarkMode)
                    else -> MediumLayout(countdown, isDarkMode)
                }
            }
        }
    }

    @Composable
    private fun MiniLayout(countdown: Countdown?, isDarkMode: Boolean) {
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (countdown == null) {
                Text(text = "⏰", style = TextStyle(fontSize = 32.sp, color = ColorProvider(if (isDarkMode) DARK_EMPTY else LIGHT_EMPTY)))
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val accentColor = getAccentColor(countdown.color)
                val textPrimary = if (isDarkMode) DARK_TEXT_PRIMARY else LIGHT_TEXT_PRIMARY
                val textSecondary = if (isDarkMode) DARK_TEXT_SECONDARY else LIGHT_TEXT_SECONDARY

                Column(modifier = GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.Top) {
                    Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(accentColor)) {}
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(text = countdown.title, style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ColorProvider(textPrimary)), maxLines = 1)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(text = "${timeInfo.days}", style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor)))
                    Text(text = if (timeInfo.days == 1L) "Tag" else "Tage", style = TextStyle(fontSize = 8.sp, color = ColorProvider(textSecondary)))
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(accentColor)) {}
                }
            }
        }
    }

    @Composable
    private fun SmallLayout(countdown: Countdown?, isDarkMode: Boolean) {
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (countdown == null) {
                EmptyState(isDarkMode)
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val accentColor = getAccentColor(countdown.color)
                val textPrimary = if (isDarkMode) DARK_TEXT_PRIMARY else LIGHT_TEXT_PRIMARY
                val textSecondary = if (isDarkMode) DARK_TEXT_SECONDARY else LIGHT_TEXT_SECONDARY

                Column(modifier = GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.Top) {
                    Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(accentColor)) {}
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(text = countdown.title, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(textPrimary)), maxLines = 1)
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(text = "${timeInfo.days}", style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor)))
                    Text(text = if (timeInfo.days == 1L) "Tag" else "Tage", style = TextStyle(fontSize = 9.sp, color = ColorProvider(textSecondary)))
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(accentColor)) {}
                }
            }
        }
    }

    @Composable
    private fun SmallXLLayout(countdown: Countdown?, isDarkMode: Boolean) {
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (countdown == null) {
                EmptyState(isDarkMode)
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val accentColor = getAccentColor(countdown.color)
                val textPrimary = if (isDarkMode) DARK_TEXT_PRIMARY else LIGHT_TEXT_PRIMARY
                val textSecondary = if (isDarkMode) DARK_TEXT_SECONDARY else LIGHT_TEXT_SECONDARY

                Column(modifier = GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.Top) {
                    Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(accentColor)) {}
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = Alignment.Start, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = countdown.title, style = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorProvider(textPrimary)), maxLines = 1, modifier = GlanceModifier.defaultWeight())
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yy")), style = TextStyle(fontSize = 10.sp, color = ColorProvider(textSecondary)))
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(text = "${timeInfo.days}", style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor)))
                    Text(text = if (timeInfo.days == 1L) "Tag" else "Tage", style = TextStyle(fontSize = 9.sp, color = ColorProvider(textSecondary)))
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(accentColor)) {}
                }
            }
        }
    }

    @Composable
    private fun MediumLayout(countdown: Countdown?, isDarkMode: Boolean) {
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (countdown == null) {
                EmptyState(isDarkMode)
            } else {
                val timeInfo = countdown.calculateTimeRemaining()
                val accentColor = getAccentColor(countdown.color)
                val format = getDisplayFormat(countdown.displayFormat)
                val textPrimary = if (isDarkMode) DARK_TEXT_PRIMARY else LIGHT_TEXT_PRIMARY
                val textSecondary = if (isDarkMode) DARK_TEXT_SECONDARY else LIGHT_TEXT_SECONDARY
                val textTertiary = if (isDarkMode) DARK_TEXT_TERTIARY else LIGHT_TEXT_TERTIARY

                Column(modifier = GlanceModifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.Top) {
                    Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(accentColor)) {}
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Row(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalAlignment = Alignment.Start, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = countdown.title, style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorProvider(textPrimary)), maxLines = 1, modifier = GlanceModifier.defaultWeight())
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Text(text = countdown.targetDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yy")), style = TextStyle(fontSize = 11.sp, color = ColorProvider(textSecondary)))
                    }
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    FormatDisplay(timeInfo = timeInfo, format = format, accentColor = accentColor, textPrimary = textPrimary)
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(text = "${timeInfo.days} ${if (timeInfo.days == 1L) "Tag" else "Tage"}", style = TextStyle(fontSize = 11.sp, color = ColorProvider(textTertiary)))
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Box(modifier = GlanceModifier.fillMaxWidth().height(3.dp).background(accentColor)) {}
                }
            }
        }
    }

    @Composable
    private fun EmptyState(isDarkMode: Boolean) {
        val emptyColor = if (isDarkMode) DARK_EMPTY else LIGHT_EMPTY
        val textColor = if (isDarkMode) DARK_TEXT_TERTIARY else Color(0xFF999999)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.CenterVertically) {
            Text(text = "⏰", style = TextStyle(fontSize = 24.sp, color = ColorProvider(emptyColor)))
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(text = "Kein\nCountdown", style = TextStyle(fontSize = 9.sp, color = ColorProvider(textColor)))
        }
    }

    @Composable
    private fun FormatDisplay(
        timeInfo: de.beigel.nextime.data.model.CountdownInfo,
        format: CountdownDisplayFormat,
        accentColor: Color,
        textPrimary: Color
    ) {
        when (format) {
            CountdownDisplayFormat.DAYS_ONLY -> {
                Text(text = "${timeInfo.days}", style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor)))
            }

            CountdownDisplayFormat.WEEKS_DAYS -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${timeInfo.weeks}", style = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor)))
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(text = if (timeInfo.weeks == 1L) "Woche" else "Wochen", style = TextStyle(fontSize = 13.sp, color = ColorProvider(textPrimary)), modifier = GlanceModifier.padding(bottom = 6.dp))
                    }
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${timeInfo.remainingDaysAfterWeeks}", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor.copy(alpha = 0.7f))))
                        Spacer(modifier = GlanceModifier.width(3.dp))
                        Text(text = if (timeInfo.remainingDaysAfterWeeks == 1L) "Tag" else "Tage", style = TextStyle(fontSize = 11.sp, color = ColorProvider(textPrimary)), modifier = GlanceModifier.padding(bottom = 2.dp))
                    }
                }
            }

            CountdownDisplayFormat.MONTHS_DAYS -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${timeInfo.months}", style = TextStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor)))
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Text(text = if (timeInfo.months == 1L) "Monat" else "Monate", style = TextStyle(fontSize = 13.sp, color = ColorProvider(textPrimary)), modifier = GlanceModifier.padding(bottom = 6.dp))
                    }
                    if (timeInfo.remainingDaysAfterMonths > 0) {
                        Spacer(modifier = GlanceModifier.height(2.dp))
                        Row(verticalAlignment = Alignment.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${timeInfo.remainingDaysAfterMonths}", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor.copy(alpha = 0.7f))))
                            Spacer(modifier = GlanceModifier.width(3.dp))
                            Text(text = if (timeInfo.remainingDaysAfterMonths == 1L) "Tag" else "Tage", style = TextStyle(fontSize = 11.sp, color = ColorProvider(textPrimary)), modifier = GlanceModifier.padding(bottom = 2.dp))
                        }
                    }
                }
            }

            CountdownDisplayFormat.YEARS_MONTHS_DAYS -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalAlignment = Alignment.CenterHorizontally, verticalAlignment = Alignment.Bottom) {
                        if (timeInfo.years > 0) {
                            Text(text = "${timeInfo.years}", style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor)))
                            Text(text = "J ", style = TextStyle(fontSize = 11.sp, color = ColorProvider(textPrimary)), modifier = GlanceModifier.padding(bottom = 4.dp))
                        }
                        if (timeInfo.remainingMonthsAfterYears > 0) {
                            Text(text = "${timeInfo.remainingMonthsAfterYears}", style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor)))
                            Text(text = "M ", style = TextStyle(fontSize = 11.sp, color = ColorProvider(textPrimary)), modifier = GlanceModifier.padding(bottom = 4.dp))
                        }
                        Text(text = "${timeInfo.remainingDaysAfterYears}", style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = ColorProvider(accentColor)))
                        Text(text = "T", style = TextStyle(fontSize = 11.sp, color = ColorProvider(textPrimary)), modifier = GlanceModifier.padding(bottom = 4.dp))
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
}

class CountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CountdownWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { appWidgetId ->
            WidgetConfigActivity.deleteCountdownId(context, appWidgetId)
        }
    }
}