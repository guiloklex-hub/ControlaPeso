package br.com.paivalab.controlapeso.bluetooth

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChipseaParserTest {
    @Test
    fun readUnsignedInt16LittleEndian_readsLowByteFirst() {
        val payload = byteArrayOf(0x34, 0x12)

        assertEquals(
            0x1234,
            ChipseaParser.readUnsignedInt16LittleEndian(payload, 0)
        )
        assertNull(ChipseaParser.readUnsignedInt16LittleEndian(payload, 1))
        assertNull(ChipseaParser.readUnsignedInt16LittleEndian(payload, -1))
    }

    @Test
    fun parse_emptyPayload_returnsSafePartialResult() {
        val measurement = ChipseaParser.parse(byteArrayOf())

        assertEquals("", measurement.rawPayloadHex)
        assertNull(measurement.rawWeight)
        assertNull(measurement.timestamp)
        assertTrue(measurement.parserNotes.isNotEmpty())
    }

    @Test
    fun parse_shortPayload_preservesOriginalBytesWithoutReadingFields() {
        val measurement = ChipseaParser.parse(byteArrayOf(0x01, 0x02, 0x03))

        assertEquals("01 02 03", measurement.rawPayloadHex)
        assertNull(measurement.rawWeight)
        assertNull(measurement.impedanceOne)
        assertNull(measurement.impedanceTwo)
    }

    @Test
    fun parse_valid16BytePayload_readsRawFieldsWithoutInventingWeightScale() {
        val measurement = ChipseaParser.parse(valid16BytePayload())

        assertEquals(0x5678, measurement.rawWeight)
        assertEquals(0x1234, measurement.impedanceOne)
        assertEquals(0x9ABC, measurement.impedanceTwo)
        assertEquals(
            LocalDateTime.of(2024, 5, 20, 14, 30, 45),
            measurement.timestamp
        )
        assertNull(measurement.weightKg)
        assertNull(measurement.isStable)
        assertTrue(
            measurement.parserNotes.any { note ->
                note.contains("escala decimal")
            }
        )
    }

    @Test
    fun parse_valid20BytePayload_preservesTrailingBytes() {
        val payload = valid16BytePayload() + byteArrayOf(
            0xDE.toByte(),
            0xAD.toByte(),
            0xBE.toByte(),
            0xEF.toByte()
        )

        val measurement = ChipseaParser.parse(payload)

        assertTrue(measurement.rawPayloadHex.endsWith("DE AD BE EF"))
        assertEquals(0x5678, measurement.rawWeight)
        assertTrue(
            measurement.parserNotes.any { note ->
                note.contains("4 byte(s) adicional(is)")
            }
        )
    }

    @Test
    fun parse_invalidDate_keepsRawValuesAndReturnsNullTimestamp() {
        val payload = valid16BytePayload().also {
            it[4] = 13
            it[5] = 32
        }

        val measurement = ChipseaParser.parse(payload)

        assertNull(measurement.timestamp)
        assertEquals(0x5678, measurement.rawWeight)
        assertTrue(
            measurement.parserNotes.any { note ->
                note.contains("Data/hora inválida")
            }
        )
    }

    @Test
    fun parse_doesNotThrowForMalformedPayloads() {
        val malformedPayloads = buildList {
            for (size in 0..32) {
                add(ByteArray(size) { 0xFF.toByte() })
            }
            add(byteArrayOf(Byte.MIN_VALUE, Byte.MAX_VALUE))
        }

        malformedPayloads.forEach { payload ->
            val measurement = ChipseaParser.parse(payload)
            assertEquals(BleHexFormatter.format(payload), measurement.rawPayloadHex)
        }
    }

    @Test
    fun chipseaPacketShape_requiresPlausibleTimestamp() {
        assertTrue(ChipseaUuids.looksLikeKnownConnectedPacket(valid16BytePayload()))

        val invalid = valid16BytePayload().also { it[4] = 0 }
        assertFalse(ChipseaUuids.looksLikeKnownConnectedPacket(invalid))
    }

    private fun valid16BytePayload(): ByteArray = byteArrayOf(
        0x34,
        0x12,
        0xE8.toByte(),
        0x07,
        0x05,
        0x14,
        0x0E,
        0x1E,
        0x2D,
        0x34,
        0x12,
        0x78,
        0x56,
        0xBC.toByte(),
        0x9A.toByte(),
        0x01
    )
}
