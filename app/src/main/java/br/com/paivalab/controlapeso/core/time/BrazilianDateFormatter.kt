package br.com.paivalab.controlapeso.core.time

import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

/**
 * Mantém a entrada de datas do app no padrão brasileiro, sem expor o formato
 * ISO usado internamente pelo domínio, banco e backups.
 */
object BrazilianDateFormatter {
    const val PATTERN: String = "dd-MM-uuuu"

    private val formatter = DateTimeFormatter.ofPattern(PATTERN)
        .withResolverStyle(ResolverStyle.STRICT)

    fun format(date: LocalDate): String = date.format(formatter)

    /**
     * Mantém o estado de edição sem separadores para que a transformação visual
     * não mova o cursor enquanto a pessoa digita cada número.
     */
    fun inputDigits(date: LocalDate): String =
        format(date).filter { it.isDigit() }

    fun inputDigits(value: String): String {
        parseIsoOrNull(value.trim())?.let(::inputDigits)?.let { return it }
        return value.filter { it.isDigit() }.take(MAX_DIGITS)
    }

    fun parse(value: String): LocalDate {
        val trimmed = value.trim()
        val normalized = if (
            trimmed.length == MAX_DIGITS && trimmed.all { it.isDigit() }
        ) {
            maskDigits(trimmed)
        } else {
            trimmed
        }
        return LocalDate.parse(normalized, formatter)
    }

    fun parseOrNull(value: String): LocalDate? = try {
        parse(value)
    } catch (_: DateTimeException) {
        null
    }

    /**
     * Aplica a máscara DD-MM-AAAA enquanto a pessoa digita. Colagens ISO
     * completas continuam sendo convertidas para facilitar a transição da UI
     * anterior, mas validação aceita apenas o formato brasileiro exibido.
     */
    fun mask(value: String): String {
        return maskDigits(inputDigits(value))
    }

    private fun maskDigits(digits: String): String {
        return when (digits.length) {
            0, 1, 2 -> digits
            3, 4 -> "${digits.take(2)}-${digits.drop(2)}"
            else -> "${digits.take(2)}-${digits.substring(2, 4)}-${digits.drop(4)}"
        }
    }

    private fun parseIsoOrNull(value: String): LocalDate? = try {
        LocalDate.parse(value)
    } catch (_: DateTimeException) {
        null
    }

    private const val MAX_DIGITS = 8
}
