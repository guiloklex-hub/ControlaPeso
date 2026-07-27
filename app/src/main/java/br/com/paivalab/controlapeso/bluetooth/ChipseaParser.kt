package br.com.paivalab.controlapeso.bluetooth

import br.com.paivalab.controlapeso.domain.ScaleMeasurement
import java.time.DateTimeException
import java.time.LocalDateTime
import java.util.Locale

/**
 * Leitura inicial e deliberadamente conservadora do pacote conectado Chipsea.
 *
 * Hipóteses atuais, ainda dependentes de validação com uma balança real:
 * - inteiros de 16 bits usam little-endian;
 * - os bytes 2..8 representam data e hora;
 * - bytes 9..14 contêm dois valores de impedância e um peso bruto;
 * - o byte 15 contém propriedades ainda não decodificadas.
 *
 * Nenhuma unidade, escala decimal ou bit de estabilidade é inferido.
 */
object ChipseaParser {
    private const val MINIMUM_KNOWN_PACKET_SIZE = 16

    fun parse(payload: ByteArray): ScaleMeasurement {
        val rawPayloadHex = BleHexFormatter.format(payload)
        val notes = mutableListOf<String>()

        if (payload.size < MINIMUM_KNOWN_PACKET_SIZE) {
            notes +=
                "Payload com ${payload.size} byte(s); a estrutura conhecida exige pelo menos " +
                    "$MINIMUM_KNOWN_PACKET_SIZE bytes."
            return partialMeasurement(rawPayloadHex, notes)
        }

        val flags = readUnsignedInt16LittleEndian(payload, 0)
        val year = readUnsignedInt16LittleEndian(payload, 2)
        val impedanceOne = readUnsignedInt16LittleEndian(payload, 9)
        val rawWeight = readUnsignedInt16LittleEndian(payload, 11)
        val impedanceTwo = readUnsignedInt16LittleEndian(payload, 13)
        val properties = payload[15].toInt() and 0xFF

        notes += String.format(
            Locale.ROOT,
            "Flags brutas (bytes 0 e 1): 0x%04X; significado ainda não validado.",
            flags
        )
        notes +=
            "rawWeight foi preservado sem conversão: unidade e escala decimal ainda não " +
                "foram confirmadas."
        notes +=
            "Os valores de impedância foram lidos como inteiros brutos little-endian; " +
                "unidade e semântica ainda não foram confirmadas."
        notes += String.format(
            Locale.ROOT,
            "Propriedades brutas (byte 15): 0x%02X; estabilidade não foi inferida.",
            properties
        )
        if (payload.size > MINIMUM_KNOWN_PACKET_SIZE) {
            notes +=
                "${payload.size - MINIMUM_KNOWN_PACKET_SIZE} byte(s) adicional(is) foram " +
                    "preservados sem interpretação."
        }

        val timestamp = parseTimestamp(payload, year, notes)

        return ScaleMeasurement(
            weightKg = null,
            rawWeight = rawWeight,
            impedanceOne = impedanceOne,
            impedanceTwo = impedanceTwo,
            isStable = null,
            timestamp = timestamp,
            rawPayloadHex = rawPayloadHex,
            parserNotes = notes.toList()
        )
    }

    internal fun readUnsignedInt16LittleEndian(payload: ByteArray, offset: Int): Int? {
        if (offset < 0 || offset >= payload.size - 1) return null
        val low = payload[offset].toInt() and 0xFF
        val high = payload[offset + 1].toInt() and 0xFF
        return low or (high shl 8)
    }

    private fun parseTimestamp(
        payload: ByteArray,
        year: Int?,
        notes: MutableList<String>
    ): LocalDateTime? {
        if (year == null) {
            notes += "Ano indisponível no payload."
            return null
        }

        val month = payload[4].toInt() and 0xFF
        val day = payload[5].toInt() and 0xFF
        val hour = payload[6].toInt() and 0xFF
        val minute = payload[7].toInt() and 0xFF
        val second = payload[8].toInt() and 0xFF

        return try {
            LocalDateTime.of(year, month, day, hour, minute, second)
        } catch (_: DateTimeException) {
            notes +=
                "Data/hora inválida: ano=$year, mês=$month, dia=$day, " +
                    "hora=$hour, minuto=$minute, segundo=$second."
            null
        }
    }

    private fun partialMeasurement(
        rawPayloadHex: String,
        notes: List<String>
    ): ScaleMeasurement = ScaleMeasurement(
        weightKg = null,
        rawWeight = null,
        impedanceOne = null,
        impedanceTwo = null,
        isStable = null,
        timestamp = null,
        rawPayloadHex = rawPayloadHex,
        parserNotes = notes.toList()
    )
}
