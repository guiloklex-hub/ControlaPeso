package br.com.paivalab.controlapeso.ui.designsystem.components

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import org.junit.Rule
import org.junit.Test

class SectionHeaderInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun titleIsExposedAsAnAccessibilityHeading() {
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                SectionHeader(
                    title = "Seção de teste",
                    supportingText = "Descrição"
                )
            }
        }

        composeRule.onNodeWithText("Seção de teste")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }
}
