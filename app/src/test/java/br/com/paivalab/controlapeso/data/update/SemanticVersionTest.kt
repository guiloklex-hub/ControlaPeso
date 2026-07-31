package br.com.paivalab.controlapeso.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticVersionTest {
    @Test
    fun `accepts conventional v tag and compares stable versions`() {
        val current = requireNotNull(SemanticVersion.parse("1.2.3"))
        val next = requireNotNull(SemanticVersion.parse("v1.3.0"))

        assertTrue(next > current)
        assertEquals("1.3.0", next.toString())
        assertTrue(next.isStable)
    }

    @Test
    fun `pre release has lower precedence than stable release`() {
        val preRelease = requireNotNull(SemanticVersion.parse("1.0.0-rc.1"))
        val stable = requireNotNull(SemanticVersion.parse("1.0.0"))

        assertFalse(preRelease.isStable)
        assertTrue(preRelease < stable)
    }

    @Test
    fun `rejects non semantic and leading zero versions`() {
        assertNull(SemanticVersion.parse("1.0"))
        assertNull(SemanticVersion.parse("01.0.0"))
        assertNull(SemanticVersion.parse("1.0.0-01"))
    }
}
