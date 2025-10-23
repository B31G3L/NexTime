package de.beigel.nextime.widget

import android.content.Context
import android.util.Log
import androidx.work.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "WidgetUpdateWorker"

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "doWork: Widget update triggered")
            CountdownWidget.updateAllWidgets(applicationContext)

            // Falls dieses Update um Mitternacht geplant wurde, plane den nächsten
            if (tags.contains(MIDNIGHT_WORK_TAG)) {
                Log.d(TAG, "doWork: Midnight update executed, scheduling next one")
                scheduleMidnightUpdate(applicationContext)
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in widget update worker", e)
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "widget_update_work"
        private const val MIDNIGHT_WORK_NAME = "widget_midnight_update"
        private const val MIDNIGHT_WORK_TAG = "midnight_update"
        private const val TAG = "WidgetUpdateWorker"

        /**
         * Plane regelmäßige Widget-Updates (alle 15 Minuten)
         */
        fun schedule(context: Context) {
            Log.d(TAG, "schedule: Scheduling regular widget updates every 15 minutes")

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    updateRequest
                )

            Log.d(TAG, "schedule: Regular widget updates scheduled")
        }

        /**
         * ========= PHASE 1: MIDNIGHT UPDATE ==========
         * Plane ein Update um Mitternacht (00:00)
         * Damit wird sichergestellt, dass der Tagewechsel sofort erkannt wird
         * Wichtig: Das Widget zählt nur TAGE, keine Stunden/Minuten
         * Ohne Mitternacht-Update würde der nächste Tag erst beim nächsten 15min Update angezeigt
         */
        fun scheduleMidnightUpdate(context: Context) {
            try {
                val now = LocalDateTime.now()
                val tomorrow = now.toLocalDate().plusDays(1)
                val midnight = tomorrow.atStartOfDay()

                val delayUntilMidnight = Duration.between(now, midnight)
                val delayMillis = delayUntilMidnight.toMillis()

                Log.d(TAG, "scheduleMidnightUpdate: Scheduled for ${midnight}")
                Log.d(TAG, "scheduleMidnightUpdate: Time until midnight: ${delayUntilMidnight.toMinutes()} minutes")

                // OneTime-Update um Mitternacht
                val midnightUpdate = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .addTag(MIDNIGHT_WORK_TAG)
                    .build()

                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        MIDNIGHT_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        midnightUpdate
                    )

                Log.d(TAG, "scheduleMidnightUpdate: Midnight update scheduled successfully")

            } catch (e: Exception) {
                Log.e(TAG, "scheduleMidnightUpdate: Error scheduling midnight update", e)
            }
        }

        /**
         * Cancel alle Widget-Updates
         */
        fun cancel(context: Context) {
            Log.d(TAG, "cancel: Canceling all widget updates")
            WorkManager.getInstance(context).apply {
                cancelUniqueWork(WORK_NAME)
                cancelUniqueWork(MIDNIGHT_WORK_NAME)
            }
        }
    }
}