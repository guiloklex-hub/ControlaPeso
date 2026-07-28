package br.com.paivalab.controlapeso.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class StableMeasurementDetectorTest {
    private val detector = StableMeasurementDetector()

    @Test
    fun isolatedMeasurement_neverBecomesStable() {
        assertNull(detector.accept(reading(75.0, 0)).stableEvent)
    }

    @Test
    fun eightCloseReadingsAcrossTwoSeconds_emitOnce() {
        val values = listOf(75.00, 75.04, 75.02, 75.03, 75.01, 75.02, 75.02, 75.01)
        var event: StableWeightEvent? = null
        values.forEachIndexed { index, value ->
            event = detector.accept(reading(value, index * 300L)).stableEvent ?: event
        }

        assertNotNull(event)
        assertEquals(8, event?.sampleCount)
        assertNull(detector.accept(reading(75.01, 2_500L)).stableEvent)
    }

    @Test
    fun oscillation_restartsCandidateWindow() {
        repeat(7) { index ->
            detector.accept(reading(75.0, index * 300L))
        }
        val restarted = detector.accept(reading(76.0, 2_100L))
        assertNull(restarted.stableEvent)
        assertEquals(1, restarted.readingsCollected)
    }

    @Test
    fun idlePacket_releasesNextSession() {
        emitStable(startMillis = 0)
        detector.accept(reading(0.0, 2_500L, property = 0x24))

        assertNotNull(emitStable(startMillis = 3_000L))
    }

    @Test
    fun longGap_releasesNextSession() {
        emitStable(startMillis = 0)

        assertNotNull(emitStable(startMillis = 5_000L))
    }

    private fun emitStable(startMillis: Long): StableWeightEvent? {
        var event: StableWeightEvent? = null
        repeat(8) { index ->
            event = detector.accept(
                reading(75.0 + (index % 2) * 0.01, startMillis + index * 300L)
            ).stableEvent ?: event
        }
        return event
    }

    private fun reading(
        value: Double,
        time: Long,
        property: Int = 0x25
    ) = BleWeightReading(
        deviceAddress = "AA:BB:CC:DD:EE:FF",
        deviceName = "Yoda1",
        advertisedValue = value,
        rawWeight = (value * 100).toInt(),
        property = property,
        sequenceNumber = (time / 300L).toInt(),
        rssi = -55,
        observedAtEpochMillis = time,
        rawPayloadHex = "C0"
    )
}
