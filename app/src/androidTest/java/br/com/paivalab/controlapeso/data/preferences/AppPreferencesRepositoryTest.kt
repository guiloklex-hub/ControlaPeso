package br.com.paivalab.controlapeso.data.preferences

import androidx.test.platform.app.InstrumentationRegistry
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppPreferencesRepositoryTest {
    private val context
        get() = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun clearBefore() = runBlocking {
        AppPreferencesRepository(context).clear()
    }

    @After
    fun clearAfter() = runBlocking {
        AppPreferencesRepository(context).clear()
    }

    @Test
    fun preferencesRemainAvailableThroughANewRepositoryReference() = runBlocking {
        AppPreferencesRepository(context).apply {
            setThemeMode(ThemeMode.DARK)
            setDefaultWeightUnit(WeightUnit.POUND)
            setVisualEffects(VisualEffects.REDUCED)
        }

        val restored = AppPreferencesRepository(context).preferences.first()

        assertEquals(ThemeMode.DARK, restored.themeMode)
        assertEquals(WeightUnit.POUND, restored.defaultWeightUnit)
        assertEquals(VisualEffects.REDUCED, restored.visualEffects)
    }

    @Test
    fun localBackupFrequencyAndTimestampPersistTogether() = runBlocking {
        val backedUpAt = Instant.parse("2026-07-29T12:34:00Z")
        AppPreferencesRepository(context).apply {
            setLocalBackupFrequency(LocalBackupFrequency.WEEKLY)
            setLastLocalBackupAt(backedUpAt)
        }

        val restored = AppPreferencesRepository(context).preferences.first()

        assertEquals(LocalBackupFrequency.WEEKLY, restored.localBackupFrequency)
        assertEquals(backedUpAt, restored.lastLocalBackupAt)
    }
}
