package br.com.paivalab.controlapeso.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import br.com.paivalab.controlapeso.ui.theme.ControlaPesoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BrazilianDateTextFieldInstrumentedTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun typingDigitsSequentially_keepsTheDateInDayMonthYearOrder() {
        var value by mutableStateOf("")
        composeRule.setContent {
            ControlaPesoTheme(dynamicColor = false) {
                BrazilianDateTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = "Data",
                    modifier = Modifier.testTag("brazilian_date")
                )
            }
        }

        val field = composeRule.onNodeWithTag("brazilian_date")
        "20072026".forEach { digit -> field.performTextInput(digit.toString()) }

        composeRule.runOnIdle {
            assertEquals("20072026", value)
        }
    }
}
