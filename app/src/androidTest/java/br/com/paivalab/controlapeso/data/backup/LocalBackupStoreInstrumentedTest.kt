package br.com.paivalab.controlapeso.data.backup

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class LocalBackupStoreInstrumentedTest {
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var store: LocalBackupStore

    @Before
    fun setUp() {
        store = LocalBackupStore(context)
        store.deleteAll()
    }

    @After
    fun tearDown() {
        store.deleteAll()
    }

    @Test
    fun latestBackupReplacesPreviousContent() = runBlocking {
        val first = store.writeLatest("first")
        val second = store.writeLatest("second")

        assertEquals(first.path, second.path)
        assertEquals("second", File(second.path).readText())
        assertNotNull(store.latestFile())
    }
}
