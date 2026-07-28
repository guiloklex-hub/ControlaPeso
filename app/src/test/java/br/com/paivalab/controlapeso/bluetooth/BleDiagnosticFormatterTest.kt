package br.com.paivalab.controlapeso.bluetooth

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BleDiagnosticFormatterTest {
    @Test
    fun `masks central address bytes`() {
        assertEquals(
            "80:F4:••:••:6E:5B",
            BleDiagnosticFormatter.maskAddress("80:F4:12:34:6E:5B")
        )
    }

    @Test
    fun `export masks address without dropping payload`() {
        val device = BleDeviceResult(
            advertisedName = "Yoda1",
            address = "80:F4:12:34:6E:5B",
            rssi = -50,
            lastSeenEpochMillis = 1_700_000_000_000,
            serviceUuids = emptyList(),
            txPower = null,
            manufacturerData = mapOf(0x70C0 to byteArrayOf(0x28, 0x8C.toByte())),
            serviceData = emptyMap(),
            rawScanRecord = byteArrayOf(0x10, 0xFF.toByte()),
            chipseaDetectionSignals = listOf("OKOK"),
            okOkAdvertisement = null
        )

        val text = BleDiagnosticFormatter.format(
            devices = listOf(device),
            history = emptyMap(),
            maskAddresses = true,
            generatedAt = Instant.parse("2026-07-27T20:00:00Z")
        )

        assertFalse(text.contains(device.address))
        assertTrue(text.contains("80:F4:••:••:6E:5B"))
        assertTrue(text.contains("28 8C"))
        assertTrue(text.contains("10 FF"))
    }
}
