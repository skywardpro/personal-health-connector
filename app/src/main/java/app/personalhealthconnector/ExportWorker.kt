package app.personalhealthconnector

import android.content.Context
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

/** Runs one export and records the outcome in plain words. */
class ExportWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        runAndRecord(applicationContext)
        return Result.success() // errors are shown in the app, no endless retries
    }

    companion object {
        fun runAndRecord(context: Context) {
            val prefs = Prefs(context)
            try {
                prefs.lastNights = Exporter.run(context)
                prefs.huaweiGranted = true
                prefs.lastError = null
                prefs.lastExportAt = System.currentTimeMillis()
            } catch (e: UserFacingException) {
                prefs.lastError = e.message
            } catch (e: Exception) {
                prefs.lastError = "Export failed: ${e.javaClass.simpleName}."
            }
        }

        /** Schedules a daily export at [minutes] after midnight, or cancels it when negative. */
        fun schedule(context: Context, minutes: Int) {
            val wm = WorkManager.getInstance(context)
            if (minutes < 0) { wm.cancelUniqueWork("daily"); return }
            val now = Calendar.getInstance()
            val next = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, minutes / 60); set(Calendar.MINUTE, minutes % 60)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val req = PeriodicWorkRequestBuilder<ExportWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(next.timeInMillis - now.timeInMillis, TimeUnit.MILLISECONDS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            wm.enqueueUniquePeriodicWork("daily", ExistingPeriodicWorkPolicy.UPDATE, req)
        }
    }
}
