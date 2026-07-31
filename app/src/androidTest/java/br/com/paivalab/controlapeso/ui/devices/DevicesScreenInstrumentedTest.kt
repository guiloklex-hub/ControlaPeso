package br.com.paivalab.controlapeso.ui.devices

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import org.junit.Rule
import org.junit.Test

class DevicesScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyStateExplainsHowToAddAScaleWithoutTechnicalDiagnostics() {
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                DevicesScreen(
                    state = DevicesUiState(isLoading = false),
                    onPreferred = {},
                    onForget = {},
                    onDismissForget = {},
                    onConfirmForget = {},
                    onMeasure = {},
                    onRetry = {}
                )
            }
        }

        composeRule.onNodeWithText("Balanças conhecidas")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule.onNodeWithText("Como adicionar uma balança").assertIsDisplayed()
        composeRule.onNodeWithText(
            "1. Permita o Bluetooth quando o Android solicitar."
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            "3. Salve a leitura; a balança aparecerá aqui."
        ).assertIsDisplayed()
        composeRule.onAllNodesWithText("Abrir diagnóstico Bluetooth").assertCountEquals(0)
    }
}
