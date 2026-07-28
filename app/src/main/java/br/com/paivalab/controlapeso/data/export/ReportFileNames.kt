package br.com.paivalab.controlapeso.data.export

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object ReportFileNames {
    fun create(
        profileName: String,
        extension: String,
        instant: Instant
    ): String {
        val safeProfile = profileName
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9_-]+"), "-")
            .trim('-')
            .take(40)
            .ifEmpty { "perfil" }
        val safeExtension = extension.lowercase(Locale.ROOT)
            .filter(Char::isLetterOrDigit)
            .take(8)
            .ifEmpty { "dat" }
        val timestamp = TIMESTAMP.format(instant)
        return "controla-peso_${safeProfile}_$timestamp.$safeExtension"
    }

    private val TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
        .withZone(ZoneOffset.UTC)
}
