package br.com.paivalab.controlapeso.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class MeasurementReminderScheduler(context: Context) {
    private val applicationContext = context.applicationContext

    fun update(preferences: AppPreferences) {
        val workManager = WorkManager.getInstance(applicationContext)
        if (!preferences.remindersEnabled || preferences.reminderDaysMask == 0) {
            workManager.cancelUniqueWork(MeasurementReminderWorker.UNIQUE_WORK_NAME)
            return
        }
        val initialDelay = ReminderPolicy.delayUntilNext(
            now = ZonedDateTime.now(),
            daysMask = preferences.reminderDaysMask,
            hour = preferences.reminderHour,
            minute = preferences.reminderMinute
        )
        val request = PeriodicWorkRequestBuilder<MeasurementReminderWorker>(
            1,
            TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            MeasurementReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
