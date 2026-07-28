package br.com.paivalab.controlapeso.data.export

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ReportFileNamesTest {
    @Test
    fun create_sanitizesProfileAndUsesDeterministicUtcTimestamp() {
        val result = ReportFileNames.create(
            profileName = "../José da Silva",
            extension = ".P/D/F",
            instant = Instant.parse("2026-07-27T21:30:45Z")
        )

        assertEquals("controla-peso_jos-da-silva_20260727-213045.pdf", result)
        assertFalse(result.contains(".."))
        assertFalse(result.contains('/'))
    }
}
