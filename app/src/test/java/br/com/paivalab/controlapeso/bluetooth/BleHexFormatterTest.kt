package br.com.paivalab.controlapeso.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Test

class BleHexFormatterTest {
    @Test
    fun format_convertsEveryByteAsUnsignedUppercaseHex() {
        val payload = byteArrayOf(
            0xFF.toByte(),
            0xF0.toByte(),
            0x02,
            0x00,
            0x34,
            0x12
        )

        assertEquals("FF F0 02 00 34 12", BleHexFormatter.format(payload))
    }

    @Test
    fun format_emptyPayload_returnsEmptyString() {
        assertEquals("", BleHexFormatter.format(byteArrayOf()))
    }
}
