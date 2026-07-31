package br.com.paivalab.controlapeso.ui.update

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.paivalab.controlapeso.data.update.ReleaseAsset
import br.com.paivalab.controlapeso.data.update.ReleaseInfo
import br.com.paivalab.controlapeso.data.update.SemanticVersion
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.ui.about.AboutScreen
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReleaseUpdateDialogInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun updateDialogRendersChangelogInLightTheme() {
        composeRule.setContent {
            ControlaPesoTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                ReleaseUpdateDialog(
                    state = ReleaseUpdateUiState(release = release()),
                    onDismiss = {},
                    onDownloadAndInstall = {},
                    onDismissInstallFeedback = {}
                )
            }
        }

        composeRule.onNodeWithText("Atualização disponível").assertIsDisplayed()
        composeRule.onNodeWithText("Correção de segurança.").assertIsDisplayed()
        composeRule.onNodeWithText("Baixar e instalar").assertIsDisplayed()
    }

    @Test
    fun unknownSourcesFeedbackRendersInDarkTheme() {
        composeRule.setContent {
            ControlaPesoTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
                ReleaseUpdateDialog(
                    state = ReleaseUpdateUiState(
                        release = release(),
                        installFeedback = UpdateInstallFeedback.UNKNOWN_SOURCES
                    ),
                    onDismiss = {},
                    onDownloadAndInstall = {},
                    onDismissInstallFeedback = {}
                )
            }
        }

        composeRule.onNodeWithText(
            "Permita instalações desta fonte nas configurações do Android para continuar. " +
                "A confirmação final continua sendo do sistema."
        ).assertIsDisplayed()
    }

    @Test
    fun aboutCanReopenReleaseNotesAfterTheGlobalDialogWasDismissed() {
        val opened = AtomicBoolean(false)
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                AboutScreen(
                    updateState = ReleaseUpdateUiState(
                        release = release(),
                        isDialogVisible = false
                    ),
                    onViewReleaseNotes = { opened.set(true) }
                )
            }
        }

        composeRule.onNodeWithText("Ver novidades")
            .assertIsDisplayed()
            .performClick()

        assertTrue(opened.get())
    }

    private fun release(): ReleaseInfo = ReleaseInfo(
        tagName = "v1.1.0",
        version = requireNotNull(SemanticVersion.parse("1.1.0")),
        publishedAt = Instant.parse("2026-07-28T12:00:00Z"),
        body = "Correção de segurança.",
        universalApk = ReleaseAsset("ControlaPeso-v1.1.0-universal.apk", "https://example.test/apk"),
        checksumFile = ReleaseAsset("SHA256SUMS.txt", "https://example.test/sums")
    )
}
