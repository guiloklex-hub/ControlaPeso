package br.com.paivalab.controlapeso.data.backup

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalBackupStore(context: Context) {
    private val directory = File(context.applicationContext.filesDir, "local-backups")
    private val latest = File(directory, "controla-peso-backup-latest.json")

    fun latestFile(): File? = latest.takeIf(File::isFile)

    suspend fun writeLatest(content: String): File = withContext(Dispatchers.IO) {
        directory.mkdirs()
        val temporary = File(directory, "controla-peso-backup-latest.json.tmp")
        temporary.writeText(content, Charsets.UTF_8)
        check(temporary.renameTo(latest)) { "Não foi possível concluir o backup local." }
        latest
    }

    fun deleteAll() {
        directory.deleteRecursively()
    }
}
