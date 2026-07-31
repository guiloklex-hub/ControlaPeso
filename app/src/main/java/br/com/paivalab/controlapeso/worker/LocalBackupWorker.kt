package br.com.paivalab.controlapeso.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.paivalab.controlapeso.app.ControlaPesoApplication
import br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency
import java.io.IOException
import kotlinx.coroutines.flow.first

class LocalBackupWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val application = applicationContext as? ControlaPesoApplication ?: return Result.failure()
        val preferences = application.container.preferencesRepository.preferences.first()
        if (preferences.localBackupFrequency == LocalBackupFrequency.OFF) return Result.success()
        return try {
            application.container.localBackupService.createLatest()
            Result.success()
        } catch (_: IOException) {
            Result.retry()
        } catch (_: Throwable) {
            Result.failure()
        }
    }

    companion object {
        const val UNIQUE_WORK_NAME = "local_backup"
    }
}
