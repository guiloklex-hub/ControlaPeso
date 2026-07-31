package br.com.paivalab.controlapeso.ui.privacy

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import org.junit.Rule
import org.junit.Test

class PrivacyScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun legalContactSectionIsExplicitlyPendingAndAccessible() {
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                PrivacyScreen(
                    state = PrivacyUiState(),
                    onExport = {},
                    onClearTemporaryFiles = {},
                    onConsumeTemporaryNotice = {},
                    onRequestDelete = {},
                    onDismissDelete = {},
                    onContinueDelete = {},
                    onConfirmationTextChange = {},
                    onConfirmDelete = {}
                )
            }
        }

        composeRule.onNodeWithText("Privacidade e dados")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("Direitos e contato"))
        composeRule.onNodeWithText("Direitos e contato").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Aviso de privacidade genérico, sujeito a revisão jurídica. Controlador/responsável " +
                "informado: Guilherme Silva Paiva. Contato para direitos e encarregado: " +
                "contato@paivalab.com.br. Versão do aviso: 1.0. " +
                "Data de publicação: 31/07/2026."
        ).assertIsDisplayed()
    }
}
