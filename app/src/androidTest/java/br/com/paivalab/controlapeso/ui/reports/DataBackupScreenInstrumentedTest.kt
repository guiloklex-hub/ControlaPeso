package br.com.paivalab.controlapeso.ui.reports

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DataBackupScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun localBackupOffersFrequencyAndShareOfLatestFile() {
        val selectedFrequency = AtomicReference<LocalBackupFrequency>()
        var shareClicked = false
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                DataBackupScreen(
                    state = ReportsUiState(),
                    localBackupState = LocalBackupUiState(
                        latestAt = Instant.parse("2026-07-31T12:00:00Z"),
                        latestAvailable = true
                    ),
                    onLocalBackupFrequencyChange = selectedFrequency::set,
                    onCreateLocalBackup = {},
                    onShareLatestBackup = { shareClicked = true },
                    onDismissLocalFeedback = {},
                    onImport = {},
                    onClearTemporaryFiles = {},
                    onDismissImport = {},
                    onRestore = {},
                    onDismissMessage = {}
                )
            }
        }

        composeRule.onNodeWithText("Backup local automático").assertIsDisplayed()
        composeRule.onNodeWithText("Semanal").performClick()
        composeRule.onNodeWithText("Compartilhar último backup")
            .assertIsDisplayed()
            .performClick()

        assertEquals(LocalBackupFrequency.WEEKLY, selectedFrequency.get())
        assertEquals(true, shareClicked)
    }
}
