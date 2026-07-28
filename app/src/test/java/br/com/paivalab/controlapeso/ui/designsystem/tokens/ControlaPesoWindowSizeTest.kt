package br.com.paivalab.controlapeso.ui.designsystem.tokens

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class ControlaPesoWindowSizeTest {
    @Test
    fun classifiesCompactWidthsBelow600Dp() {
        assertEquals(
            ControlaPesoWindowSize.COMPACT,
            controlaPesoWindowSize(599.dp)
        )
    }

    @Test
    fun classifiesMediumWidthsFrom600To839Dp() {
        assertEquals(
            ControlaPesoWindowSize.MEDIUM,
            controlaPesoWindowSize(600.dp)
        )
        assertEquals(
            ControlaPesoWindowSize.MEDIUM,
            controlaPesoWindowSize(839.dp)
        )
    }

    @Test
    fun classifiesExpandedWidthsFrom840Dp() {
        assertEquals(
            ControlaPesoWindowSize.EXPANDED,
            controlaPesoWindowSize(840.dp)
        )
    }
}
