package br.com.paivalab.controlapeso.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import br.com.paivalab.controlapeso.bluetooth.BleDiagnosticLogging
import br.com.paivalab.controlapeso.worker.ReportCleanupWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ControlaPesoApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val container: AppContainer by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Build the optional network stack away from the main thread; it never gates onboarding.
        applicationScope.launch {
            container.releaseUpdateCoordinator.check(applicationScope)
        }
        val cleanup = PeriodicWorkRequestBuilder<ReportCleanupWorker>(
            1,
            TimeUnit.DAYS
        ).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ReportCleanupWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanup
        )
        applicationScope.launch {
            container.preferencesRepository.preferences
                .map {
                    ReminderSettings(
                        enabled = it.remindersEnabled,
                        daysMask = it.reminderDaysMask,
                        hour = it.reminderHour,
                        minute = it.reminderMinute
                    ) to it
                }
                .distinctUntilChanged { old, new -> old.first == new.first }
                .collect { (_, preferences) ->
                    container.reminderScheduler.update(preferences)
                }
        }
        applicationScope.launch {
            container.preferencesRepository.preferences
                .map { it.localBackupFrequency }
                .distinctUntilChanged()
                .collect {
                    container.localBackupScheduler.update(
                        container.preferencesRepository.preferences.first()
                    )
                }
        }
        applicationScope.launch {
            container.preferencesRepository.preferences
                .map { it.detailedBleLogs }
                .distinctUntilChanged()
                .collect(BleDiagnosticLogging::updateUserPreference)
        }
    }

    private data class ReminderSettings(
        val enabled: Boolean,
        val daysMask: Int,
        val hour: Int,
        val minute: Int
    )
}
