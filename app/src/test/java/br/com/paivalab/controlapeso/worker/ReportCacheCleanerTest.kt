package br.com.paivalab.controlapeso.worker

import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportCacheCleanerTest {
    @Test
    fun clean_removesOnlyExpiredFilesInsideGivenDirectory() {
        val directory = Files.createTempDirectory("controla-peso-cache-test").toFile()
        val old = directory.resolve("old.pdf").apply {
            writeText("old")
            setLastModified(1_000L)
        }
        val recent = directory.resolve("recent.csv").apply {
            writeText("recent")
            setLastModified(9_000L)
        }

        val removed = ReportCacheCleaner.clean(
            directory = directory,
            nowEpochMillis = 10_000L,
            maxAgeMillis = 5_000L
        )

        assertEquals(1, removed)
        assertFalse(old.exists())
        assertTrue(recent.exists())
        recent.delete()
        directory.delete()
    }
}
