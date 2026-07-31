package br.com.paivalab.controlapeso.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import java.util.concurrent.TimeUnit

class LocalBackupScheduler(context: Context) {
    private val applicationContext = context.applicationContext

    fun update(preferences: AppPreferences) {
        val workManager = WorkManager.getInstance(applicationContext)
        val frequency = preferences.localBackupFrequency
        if (frequency == br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency.OFF) {
            workManager.cancelUniqueWork(LocalBackupWorker.UNIQUE_WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<LocalBackupWorker>(
            frequency.intervalDays,
            TimeUnit.DAYS
        ).build()
        workManager.enqueueUniquePeriodicWork(
            LocalBackupWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
