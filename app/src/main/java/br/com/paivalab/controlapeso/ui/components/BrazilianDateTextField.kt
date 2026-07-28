package br.com.paivalab.controlapeso.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import br.com.paivalab.controlapeso.R
import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter

@Composable
fun BrazilianDateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val inputDigits = BrazilianDateFormatter.inputDigits(value)
    OutlinedTextField(
        value = inputDigits,
        onValueChange = { onValueChange(BrazilianDateFormatter.inputDigits(it)) },
        label = { Text(label) },
        supportingText = { Text(stringResource(R.string.date_format_hint)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = BrazilianDateVisualTransformation,
        singleLine = true,
        enabled = enabled,
        modifier = modifier
    )
}

private object BrazilianDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = BrazilianDateFormatter.inputDigits(text.text)
        val masked = BrazilianDateFormatter.mask(digits)
        return TransformedText(
            text = AnnotatedString(masked),
            offsetMapping = BrazilianDateOffsetMapping(
                originalLength = digits.length,
                transformedLength = masked.length
            )
        )
    }
}

private class BrazilianDateOffsetMapping(
    private val originalLength: Int,
    private val transformedLength: Int
) : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int =
        when {
            offset <= 2 -> offset
            offset <= 4 -> offset + 1
            else -> offset + 2
        }.coerceIn(0, transformedLength)

    override fun transformedToOriginal(offset: Int): Int =
        when {
            offset <= 2 -> offset
            offset <= 5 -> offset - 1
            else -> offset - 2
        }.coerceIn(0, originalLength)
}
