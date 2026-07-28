package br.com.paivalab.controlapeso.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.paivalab.controlapeso.data.export.ShareFileService
import java.io.File

class ReportCleanupWorker(
    appContext: Context,
    parameters: WorkerParameters
) : CoroutineWorker(appContext, parameters) {
    override suspend fun doWork(): Result {
        val directory = File(
            applicationContext.cacheDir,
            ShareFileService.SHARED_DIRECTORY
        )
        ReportCacheCleaner.clean(directory, System.currentTimeMillis())
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "controla-peso-report-cleanup"
    }
}

object ReportCacheCleaner {
    const val MAX_AGE_MILLIS = 24L * 60L * 60L * 1_000L

    fun clean(
        directory: File,
        nowEpochMillis: Long,
        maxAgeMillis: Long = MAX_AGE_MILLIS
    ): Int {
        if (!directory.isDirectory) return 0
        return directory.listFiles()
            .orEmpty()
            .filter(File::isFile)
            .count { file ->
                val age = nowEpochMillis - file.lastModified()
                age > maxAgeMillis && file.delete()
            }
    }
}
