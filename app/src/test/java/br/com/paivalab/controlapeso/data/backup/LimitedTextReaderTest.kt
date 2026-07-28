package br.com.paivalab.controlapeso.data.backup

import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LimitedTextReaderTest {
    @Test
    fun readsContentWithinLimit() {
        assertEquals(
            "ábc",
            LimitedTextReader.read(StringReader("ábc"), maximumCharacters = 3)
        )
    }

    @Test
    fun rejectsContentOverLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            LimitedTextReader.read(StringReader("quatro"), maximumCharacters = 3)
        }
    }
}
