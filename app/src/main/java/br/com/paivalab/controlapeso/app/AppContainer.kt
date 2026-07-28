package br.com.paivalab.controlapeso.app

import android.content.Context
import br.com.paivalab.controlapeso.bluetooth.BleMeasurementSource
import br.com.paivalab.controlapeso.bluetooth.OkOkBleMeasurementSource
import br.com.paivalab.controlapeso.core.id.IdGenerator
import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.core.time.SystemAppClock
import br.com.paivalab.controlapeso.data.local.ControlaPesoDatabase
import br.com.paivalab.controlapeso.data.backup.JsonBackupManager
import br.com.paivalab.controlapeso.data.export.CsvExportService
import br.com.paivalab.controlapeso.data.export.PdfReportService
import br.com.paivalab.controlapeso.data.export.ShareFileService
import br.com.paivalab.controlapeso.data.healthconnect.HealthConnectWeightWriter
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import br.com.paivalab.controlapeso.data.repository.GoalRepositoryImpl
import br.com.paivalab.controlapeso.data.repository.MeasurementRepositoryImpl
import br.com.paivalab.controlapeso.data.repository.ProfileRepositoryImpl
import br.com.paivalab.controlapeso.data.repository.ScaleDeviceRepositoryImpl
import br.com.paivalab.controlapeso.domain.repository.GoalRepository
import br.com.paivalab.controlapeso.domain.repository.MeasurementRepository
import br.com.paivalab.controlapeso.domain.repository.ProfileRepository
import br.com.paivalab.controlapeso.domain.repository.ScaleDeviceRepository
import br.com.paivalab.controlapeso.worker.MeasurementReminderScheduler

class AppContainer(context: Context) {
    val applicationContext: Context = context.applicationContext
    val clock: AppClock = SystemAppClock
    val idGenerator: IdGenerator = IdGenerator.UUID
    val database: ControlaPesoDatabase = ControlaPesoDatabase.create(applicationContext)
    val preferencesRepository = AppPreferencesRepository(applicationContext)
    val profileRepository: ProfileRepository =
        ProfileRepositoryImpl(database.profileDao(), clock)
    val measurementRepository: MeasurementRepository =
        MeasurementRepositoryImpl(database.measurementDao())
    val goalRepository: GoalRepository = GoalRepositoryImpl(database.goalDao())
    val scaleDeviceRepository: ScaleDeviceRepository =
        ScaleDeviceRepositoryImpl(database.scaleDeviceDao())
    val bleMeasurementSource: BleMeasurementSource =
        OkOkBleMeasurementSource(applicationContext)
    val csvExportService = CsvExportService()
    val pdfReportService = PdfReportService()
    val shareFileService = ShareFileService(applicationContext)
    val healthConnectWeightWriter: HealthConnectWeightWriter by lazy {
        HealthConnectWeightWriter(applicationContext)
    }
    val reminderScheduler = MeasurementReminderScheduler(applicationContext)
    val jsonBackupManager = JsonBackupManager(
        database = database,
        profileRepository = profileRepository,
        measurementRepository = measurementRepository,
        goalRepository = goalRepository,
        deviceRepository = scaleDeviceRepository,
        preferencesRepository = preferencesRepository,
        clock = clock
    )
}
