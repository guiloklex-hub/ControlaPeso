package br.com.paivalab.controlapeso.ui.designsystem.components

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductComponentsInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun clickableMeasurementItemMergesReadableContentIntoButton() {
        val clicked = AtomicBoolean(false)
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                MeasurementListItem(
                    value = "72,1 kg",
                    dateTime = "31/07/2026 · 12:00",
                    source = "Manual",
                    onClick = { clicked.set(true) }
                )
            }
        }

        composeRule.onNodeWithText("72,1 kg")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked.get())
    }

    @Test
    fun actionCardMergesReadableContentIntoButton() {
        val clicked = AtomicBoolean(false)
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                PrimaryActionCard(
                    title = "Medir com a balança",
                    body = "Inicie uma busca manual.",
                    onClick = { clicked.set(true) }
                )
            }
        }

        composeRule.onNodeWithText("Medir com a balança")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertIsDisplayed()
            .performClick()

        assertTrue(clicked.get())
    }
}
