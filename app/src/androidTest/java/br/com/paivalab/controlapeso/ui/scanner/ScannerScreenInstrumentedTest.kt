package br.com.paivalab.controlapeso.ui.scanner

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import br.com.paivalab.controlapeso.bluetooth.BleDiagnosticFormatter
import br.com.paivalab.controlapeso.bluetooth.BlePermissionStatus
import br.com.paivalab.controlapeso.bluetooth.BleSupportStatus
import br.com.paivalab.controlapeso.bluetooth.BluetoothPowerStatus
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import org.junit.Rule
import org.junit.Test

class ScannerScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun diagnosticShowsManualReadyStateBeforeTheFirstSearch() {
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                ScannerScreen(
                    uiState = ScannerUiState(
                        bluetoothSupport = BleSupportStatus.SUPPORTED,
                        bluetoothPower = BluetoothPowerStatus.ON,
                        permissionStatus = BlePermissionStatus.GRANTED
                    ),
                    onRequestPermissions = {},
                    onStartScan = {},
                    onStopScan = {},
                    onClearResults = {},
                    onDismissError = {},
                    onCopyText = {},
                    onShareText = {}
                )
            }
        }

        composeRule.onNodeWithText("Diagnóstico de balança Bluetooth")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule.onNodeWithText("Pronto para verificar").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Permissões e Bluetooth estão prontos. A busca só começa quando você tocar em " +
                "Verificar conexão."
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Verificar conexão").assertIsDisplayed()
        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("Exibir informações para suporte"))
        composeRule.onNodeWithText("Exibir informações para suporte").performClick()
        composeRule.onNodeWithText("Ocultar informações para suporte").assertIsDisplayed()
        composeRule.onNodeWithText(
            "Versão do parser: ${BleDiagnosticFormatter.PARSER_VERSION}"
        ).assertIsDisplayed()
    }

    @Test
    fun diagnosticExplainsWhenACompletedSearchFoundNoScale() {
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                ScannerScreen(
                    uiState = ScannerUiState(
                        bluetoothSupport = BleSupportStatus.SUPPORTED,
                        bluetoothPower = BluetoothPowerStatus.ON,
                        permissionStatus = BlePermissionStatus.GRANTED,
                        hasCompletedScan = true
                    ),
                    onRequestPermissions = {},
                    onStartScan = {},
                    onStopScan = {},
                    onClearResults = {},
                    onDismissError = {},
                    onCopyText = {},
                    onShareText = {}
                )
            }
        }

        composeRule.onNodeWithText("Nenhuma balança encontrada").assertIsDisplayed()
        composeRule.onNodeWithText(
            "A última busca terminou sem encontrar anúncios. Confirme que a balança está ligada " +
                "e tente novamente."
        ).assertIsDisplayed()
    }
}
