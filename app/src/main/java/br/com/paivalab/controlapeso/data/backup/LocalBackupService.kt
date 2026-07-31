package br.com.paivalab.controlapeso.data.backup

import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.data.export.ShareFileService
import br.com.paivalab.controlapeso.data.export.SharedReportFile
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalBackupService(
    private val store: LocalBackupStore,
    private val backupManager: JsonBackupManager,
    private val preferencesRepository: AppPreferencesRepository,
    private val clock: AppClock,
    private val shareFileService: ShareFileService
) {
    suspend fun createLatest(): File {
        val createdAt = clock.now()
        val content = withContext(Dispatchers.IO) {
            backupManager.exportJson(createdAt)
        }
        val file = store.writeLatest(content)
        preferencesRepository.setLastLocalBackupAt(createdAt)
        return file
    }

    suspend fun shareLatest(): SharedReportFile? = withContext(Dispatchers.IO) {
        store.latestFile()?.let { file ->
            shareFileService.create(
                displayName = file.name,
                mimeType = "application/json",
                bytes = file.readBytes()
            )
        }
    }

    fun latestFile(): File? = store.latestFile()

    fun deleteAll() = store.deleteAll()
}
