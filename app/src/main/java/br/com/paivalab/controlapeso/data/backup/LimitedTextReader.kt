package br.com.paivalab.controlapeso.data.backup

import java.io.Reader

object LimitedTextReader {
    const val DEFAULT_MAXIMUM_CHARACTERS: Int = 20 * 1024 * 1024

    fun read(
        reader: Reader,
        maximumCharacters: Int = DEFAULT_MAXIMUM_CHARACTERS
    ): String {
        require(maximumCharacters > 0) {
            "O limite máximo de caracteres deve ser positivo."
        }

        val output = StringBuilder(minOf(maximumCharacters, 8 * 1024))
        val buffer = CharArray(8 * 1024)
        while (true) {
            val count = reader.read(buffer)
            if (count < 0) break
            if (output.length > maximumCharacters - count) {
                throw IllegalArgumentException(
                    "O arquivo excede o limite de $maximumCharacters caracteres."
                )
            }
            output.append(buffer, 0, count)
        }
        return output.toString()
    }
}
