package br.com.paivalab.controlapeso.data.backup

import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface BackupDecodeResult {
    data class Valid(val document: BackupDocument) : BackupDecodeResult
    data class Invalid(val errors: List<String>) : BackupDecodeResult
}

object BackupCodec {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        explicitNulls = true
        ignoreUnknownKeys = false
    }

    fun encode(document: BackupDocument): String = json.encodeToString(document)

    fun decodeAndValidate(content: String): BackupDecodeResult {
        val document = try {
            json.decodeFromString<BackupDocument>(content)
        } catch (_: SerializationException) {
            return BackupDecodeResult.Invalid(listOf("JSON inválido ou truncado."))
        } catch (_: IllegalArgumentException) {
            return BackupDecodeResult.Invalid(listOf("Conteúdo do backup inválido."))
        }
        val validation = BackupValidator.validate(document)
        return if (validation.isValid) {
            BackupDecodeResult.Valid(document)
        } else {
            BackupDecodeResult.Invalid(validation.errors)
        }
    }
}
