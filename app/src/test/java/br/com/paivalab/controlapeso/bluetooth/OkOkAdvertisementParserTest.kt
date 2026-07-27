package br.com.paivalab.controlapeso.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class OkOkAdvertisementParserTest {
    @Test
    fun parse_capturedMeasurement_reproducesValuesLoggedByOkOk() {
        val result = OkOkAdvertisementParser.parse(
            manufacturerId = 0x68C0,
            payload = measurementPayload()
        )

        assertNotNull(result)
        requireNotNull(result)
        assertEquals(0xC0, result.protocolMarker)
        assertEquals(104, result.sequenceNumber)
        assertEquals(0x288C, result.rawWeight)
        assertEquals(103.8, result.weightValue, 0.0)
        assertEquals(0x1388, result.rawSecondaryValue)
        assertEquals(500.0, result.secondaryValue, 0.0)
        assertEquals(0x25, result.property)
        assertEquals(1, result.commandId)
        assertEquals(
            "C0 68 28 8C 13 88 00 00 25 00 00 00 00 00 00",
            result.rawManufacturerDataHex
        )
    }

    @Test
    fun parse_capturedIdlePacket_preservesZeroValuesAndSequence() {
        val result = OkOkAdvertisementParser.parse(
            manufacturerId = 0x69C0,
            payload = idlePayload()
        )

        assertNotNull(result)
        requireNotNull(result)
        assertEquals(105, result.sequenceNumber)
        assertEquals(0, result.rawWeight)
        assertEquals(0.0, result.weightValue, 0.0)
        assertEquals(0x24, result.property)
        assertEquals(0, result.commandId)
    }

    @Test
    fun parse_nextCapturedMeasurement_readsChangingSequence() {
        val result = OkOkAdvertisementParser.parse(
            mapOf(0x69C0 to measurementPayload())
        )

        assertNotNull(result)
        requireNotNull(result)
        assertEquals(105, result.sequenceNumber)
        assertEquals(103.8, result.weightValue, 0.0)
    }

    @Test
    fun parse_rejectsUnknownMarkerPropertyAndShortPayload() {
        assertNull(
            OkOkAdvertisementParser.parse(
                manufacturerId = 0x6801,
                payload = measurementPayload()
            )
        )
        assertNull(
            OkOkAdvertisementParser.parse(
                manufacturerId = 0x68C0,
                payload = measurementPayload().also { it[6] = 0x01 }
            )
        )
        assertNull(
            OkOkAdvertisementParser.parse(
                manufacturerId = 0x68C0,
                payload = byteArrayOf(0x28, 0x8C.toByte())
            )
        )
    }

    @Test
    fun readUnsignedInt16BigEndian_readsHighByteFirst() {
        val payload = byteArrayOf(0x28, 0x8C.toByte())

        assertEquals(
            0x288C,
            OkOkAdvertisementParser.readUnsignedInt16BigEndian(payload, 0)
        )
        assertNull(OkOkAdvertisementParser.readUnsignedInt16BigEndian(payload, 1))
        assertNull(OkOkAdvertisementParser.readUnsignedInt16BigEndian(payload, -1))
    }

    @Test
    fun parse_doesNotThrowForMalformedPayloads() {
        for (size in 0..32) {
            val payload = ByteArray(size) { 0xFF.toByte() }
            assertNull(OkOkAdvertisementParser.parse(0xFFC0, payload))
        }
    }

    private fun measurementPayload(): ByteArray = byteArrayOf(
        0x28,
        0x8C.toByte(),
        0x13,
        0x88.toByte(),
        0x00,
        0x00,
        0x25,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00
    )

    private fun idlePayload(): ByteArray = byteArrayOf(
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x24,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00,
        0x00
    )
}
