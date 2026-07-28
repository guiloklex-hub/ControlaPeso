package br.com.paivalab.controlapeso.data.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

data class SharedReportFile(
    val file: File,
    val uri: Uri,
    val mimeType: String,
    val displayName: String
)

class ShareFileService(private val context: Context) {
    val sharedDirectory: File
        get() = File(context.cacheDir, SHARED_DIRECTORY).apply { mkdirs() }

    fun create(
        displayName: String,
        mimeType: String,
        bytes: ByteArray
    ): SharedReportFile {
        val safeName = displayName.substringAfterLast('/').substringAfterLast('\\')
        require(safeName.isNotBlank() && safeName != "." && safeName != "..")
        val file = File(sharedDirectory, safeName)
        file.outputStream().use { it.write(bytes) }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return SharedReportFile(file, uri, mimeType, safeName)
    }

    companion object {
        const val SHARED_DIRECTORY = "shared-reports"
    }
}
