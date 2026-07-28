package br.com.paivalab.controlapeso.bluetooth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BleWeightReadingTest {
    @Test
    fun properties_areClassifiedWithoutInferringStability() {
        assertTrue(reading(property = 0x25).isMeasurementPacket)
        assertFalse(reading(property = 0x25).isIdlePacket)
        assertTrue(reading(property = 0x24).isIdlePacket)
        assertFalse(reading(property = 0x23).isMeasurementPacket)
    }

    private fun reading(property: Int) = BleWeightReading(
        deviceAddress = "00:00:00:00:00:00",
        deviceName = "Yoda1",
        advertisedValue = 75.0,
        rawWeight = 7_500,
        property = property,
        sequenceNumber = 1,
        rssi = -60,
        observedAtEpochMillis = 0,
        rawPayloadHex = "C0"
    )
}
