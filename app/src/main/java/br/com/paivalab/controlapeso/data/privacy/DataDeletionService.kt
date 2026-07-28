package br.com.paivalab.controlapeso.data.privacy

import androidx.room.withTransaction
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.worker.MeasurementReminderWorker
import br.com.paivalab.controlapeso.worker.ReportCacheCleaner
import androidx.work.WorkManager

class DataDeletionService(private val container: AppContainer) {
    suspend fun deleteAllLocalData() {
        container.database.withTransaction {
            container.database.measurementDao().deleteAll()
            container.database.goalDao().deleteAll()
            container.database.profileDao().deleteAll()
            container.database.scaleDeviceDao().deleteAll()
        }
        container.preferencesRepository.clear()
        WorkManager.getInstance(container.applicationContext)
            .cancelUniqueWork(MeasurementReminderWorker.UNIQUE_WORK_NAME)
        ReportCacheCleaner.clean(
            directory = container.shareFileService.sharedDirectory,
            nowEpochMillis = Long.MAX_VALUE,
            maxAgeMillis = 0
        )
    }
}
