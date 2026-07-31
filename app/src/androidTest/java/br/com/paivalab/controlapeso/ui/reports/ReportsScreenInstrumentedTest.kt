package br.com.paivalab.controlapeso.ui.reports

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import br.com.paivalab.controlapeso.data.export.ReportFormat
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReportsScreenInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun reportUsesGlobalUnitAndOffersSettingsShortcut() {
        val settingsOpened = AtomicBoolean(false)
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                ReportsScreen(
                    state = ReportsUiState(
                        format = ReportFormat.JSON,
                        unit = WeightUnit.POUND
                    ),
                    onProfileChange = {},
                    onPeriodChange = {},
                    onCustomStartChange = {},
                    onCustomEndChange = {},
                    onFormatChange = {},
                    onIncludeChartChange = {},
                    onIncludeTableChange = {},
                    onIncludeNotesChange = {},
                    onGenerate = {},
                    onShare = {},
                    onShareSummary = {},
                    onSaveFile = {},
                    onOpenDataBackup = {},
                    onDismissMessage = {},
                    onOpenSettings = { settingsOpened.set(true) }
                )
            }
        }

        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("Exibindo em lb"))
        composeRule.onNodeWithText("Exibindo em lb").assertIsDisplayed()
        composeRule.onNodeWithText("Alterar em Ajustes")
            .assertIsDisplayed()
            .performClick()

        assertTrue(settingsOpened.get())
    }

    @Test
    fun reportContentIsEditedInModalWithCheckboxes() {
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                ReportsScreen(
                    state = ReportsUiState(format = ReportFormat.PDF),
                    onProfileChange = {},
                    onPeriodChange = {},
                    onCustomStartChange = {},
                    onCustomEndChange = {},
                    onFormatChange = {},
                    onIncludeChartChange = {},
                    onIncludeTableChange = {},
                    onIncludeNotesChange = {},
                    onGenerate = {},
                    onShare = {},
                    onShareSummary = {},
                    onSaveFile = {},
                    onOpenDataBackup = {},
                    onDismissMessage = {}
                )
            }
        }

        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("Editar conteúdo"))
        composeRule.onNodeWithText("Editar conteúdo")
            .assertIsDisplayed()
            .performClick()
        composeRule.onNodeWithText("Conteúdo do relatório").assertIsDisplayed()
        composeRule.onNodeWithText("Incluir gráfico").assertIsDisplayed()
        composeRule.onNodeWithText("Incluir tabela").assertIsDisplayed()
        composeRule.onNodeWithText("Incluir observações").assertIsDisplayed()
    }

}
