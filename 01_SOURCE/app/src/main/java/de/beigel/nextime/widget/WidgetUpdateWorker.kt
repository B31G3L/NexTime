package de.beigel.nextime.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker für regelmäßige Widget-Updates
 * Aktualisiert alle Widgets alle 15 Minuten
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val glanceManager = GlanceAppWidgetManager(applicationContext)

            // Small Widgets aktualisieren
            glanceManager.getGlanceIds(SmallCountdownWidget::class.java).forEach { glanceId ->
                SmallCountdownWidget().update(applicationContext, glanceId)
            }

            // Medium Widgets aktualisieren
            glanceManager.getGlanceIds(MediumCountdownWidget::class.java).forEach { glanceId ->
                MediumCountdownWidget().update(applicationContext, glanceId)
            }

            // Large Widgets aktualisieren
            glanceManager.getGlanceIds(LargeCountdownWidget::class.java).forEach { glanceId ->
                LargeCountdownWidget().update(applicationContext, glanceId)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "WidgetUpdateWork"

        /**
         * Plant regelmäßige Widget-Updates
         */
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        /**
         * Stoppt regelmäßige Widget-Updates
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Löst ein sofortiges Widget-Update aus
         */
        fun triggerUpdate(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
