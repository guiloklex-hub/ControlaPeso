package br.com.paivalab.controlapeso.data.profile

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

/**
 * Keeps profile photos in app-private storage.
 *
 * The file is deliberately not part of the Room profile model or the JSON
 * backup contract until photo backup has a defined product decision.
 */
interface ProfilePhotoStore {
    fun pathFor(profileId: String): String?
    fun hasPhoto(profileId: String): Boolean
    suspend fun save(profileId: String, sourceUri: String): String
    suspend fun <T> withPhotoChange(
        profileId: String,
        sourceUri: String?,
        removePhoto: Boolean,
        block: suspend () -> T
    ): T
    fun delete(profileId: String)
    fun deleteAll()
}

class PrivateProfilePhotoStore(context: Context) : ProfilePhotoStore {
    private val contentResolver = context.applicationContext.contentResolver
    private val directory = File(context.applicationContext.filesDir, DIRECTORY_NAME)

    override fun pathFor(profileId: String): String? =
        fileFor(profileId).takeIf(File::isFile)?.absolutePath

    override fun hasPhoto(profileId: String): Boolean = pathFor(profileId) != null

    override suspend fun save(profileId: String, sourceUri: String): String =
        withContext(Dispatchers.IO) {
            ensureDirectory()
            val destination = fileFor(profileId)
            val temporary = temporaryFile(destination, "save")
            try {
                copySourceTo(sourceUri, temporary)
                replaceWithTemporary(temporary, destination)
                destination.absolutePath
            } catch (failure: Throwable) {
                temporary.delete()
                throw failure
            }
        }

    override suspend fun <T> withPhotoChange(
        profileId: String,
        sourceUri: String?,
        removePhoto: Boolean,
        block: suspend () -> T
    ): T = withContext(Dispatchers.IO) {
        require(!removePhoto || sourceUri == null) {
            "Uma alteração de foto não pode remover e selecionar outra ao mesmo tempo."
        }
        if (!removePhoto && sourceUri == null) {
            return@withContext withContext(NonCancellable) { block() }
        }

        ensureDirectory()
        val destination = fileFor(profileId)
        val temporary = temporaryFile(destination, "change")
        val backup = temporaryFile(destination, "backup")
        val hadPhoto = destination.isFile
        var destinationChanged = false

        try {
            if (hadPhoto) destination.copyTo(backup, overwrite = true)
            sourceUri?.let { copySourceTo(it, temporary) }

            if (destination.exists() && !destination.delete()) {
                throw IOException("Não foi possível preparar a foto do perfil.")
            }
            destinationChanged = true
            sourceUri?.let { replaceWithTemporary(temporary, destination) }

            // A persistência do perfil e a troca do arquivo devem terminar juntas
            // mesmo se a tela for cancelada no meio da operação.
            val result = withContext(NonCancellable) { block() }
            backup.delete()
            result
        } catch (failure: Throwable) {
            if (destinationChanged) {
                destination.delete()
                if (hadPhoto && backup.isFile) {
                    restoreBackup(backup, destination)
                }
            }
            throw failure
        } finally {
            temporary.delete()
            backup.delete()
        }
    }

    override fun delete(profileId: String) {
        fileFor(profileId).delete()
    }

    override fun deleteAll() {
        directory.listFiles()
            ?.filter(File::isFile)
            ?.forEach(File::delete)
    }

    private fun ensureDirectory() {
        check(directory.exists() || directory.mkdirs()) {
            "Não foi possível criar o armazenamento privado de fotos."
        }
    }

    private fun copySourceTo(sourceUri: String, destination: File) {
        val input = contentResolver.openInputStream(Uri.parse(sourceUri))
            ?: throw IOException("Não foi possível ler a foto selecionada.")
        input.use { sourceStream ->
            destination.outputStream().use { destinationStream ->
                sourceStream.copyTo(destinationStream)
            }
        }
    }

    private fun replaceWithTemporary(temporary: File, destination: File) {
        if (!temporary.renameTo(destination)) {
            temporary.copyTo(destination, overwrite = true)
            check(temporary.delete()) {
                "Não foi possível guardar a foto selecionada."
            }
        }
    }

    private fun restoreBackup(backup: File, destination: File) {
        if (!backup.renameTo(destination)) {
            backup.copyTo(destination, overwrite = true)
        }
    }

    private fun temporaryFile(destination: File, operation: String): File =
        File(directory, ".${destination.name}.$operation.${UUID.randomUUID()}.tmp")

    private fun fileFor(profileId: String): File =
        File(directory, "${profileIdDigest(profileId)}.photo")

    private fun profileIdDigest(profileId: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(profileId.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(byte) }

    private companion object {
        const val DIRECTORY_NAME = "profile-photos"
    }
}
