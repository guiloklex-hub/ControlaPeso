package br.com.paivalab.controlapeso.bluetooth

/**
 * Parser específico para o Manufacturer Advertising comprovado em captura real.
 *
 * O Android remove os bytes de tamanho/tipo do AD structure e interpreta os
 * dois bytes seguintes como Manufacturer ID little-endian. Na variante
 * observada, esses bytes são C0 + número de sequência, por isso o ID muda entre
 * medições e não deve ser tratado como identificador fixo de fabricante.
 */
object OkOkAdvertisementParser {
    private const val PROTOCOL_MARKER = 0xC0
    private const val MINIMUM_PAYLOAD_SIZE = 13
    private const val PROPERTY_IDLE = 0x24
    private const val PROPERTY_MEASUREMENT = 0x25

    fun parse(manufacturerData: Map<Int, ByteArray>): OkOkAdvertisement? =
        manufacturerData.entries.firstNotNullOfOrNull { (manufacturerId, payload) ->
            parse(manufacturerId, payload)
        }

    fun parse(manufacturerId: Int, payload: ByteArray): OkOkAdvertisement? {
        if (payload.size < MINIMUM_PAYLOAD_SIZE) return null

        val protocolMarker = manufacturerId and 0xFF
        if (protocolMarker != PROTOCOL_MARKER) return null

        val property = payload[6].toInt() and 0xFF
        if (property != PROPERTY_IDLE && property != PROPERTY_MEASUREMENT) return null

        val rawWeight = readUnsignedInt16BigEndian(payload, 0) ?: return null
        val rawSecondaryValue = readUnsignedInt16BigEndian(payload, 2) ?: return null
        val sequenceNumber = manufacturerId ushr 8 and 0xFF
        val commandId = when (property) {
            PROPERTY_IDLE -> 0
            PROPERTY_MEASUREMENT -> 1
            else -> null
        }
        val reconstructedManufacturerData = byteArrayOf(
            protocolMarker.toByte(),
            sequenceNumber.toByte()
        ) + payload

        return OkOkAdvertisement(
            manufacturerId = manufacturerId,
            protocolMarker = protocolMarker,
            sequenceNumber = sequenceNumber,
            rawWeight = rawWeight,
            weightValue = rawWeight / 100.0,
            rawSecondaryValue = rawSecondaryValue,
            secondaryValue = rawSecondaryValue / 10.0,
            property = property,
            commandId = commandId,
            rawManufacturerDataHex = BleHexFormatter.format(reconstructedManufacturerData),
            parserNotes = listOf(
                "Formato Manufacturer Advertising OKOK observado na balança Yoda1.",
                "O peso bruto foi dividido por 100 para reproduzir o valor registrado " +
                    "pelo OKOK; a unidade ainda precisa ser confirmada.",
                "O valor secundário foi dividido por 10 para reproduzir o campo r1 do " +
                    "OKOK; unidade e semântica ainda não foram confirmadas.",
                "Propriedades 0x24 e 0x25 corresponderam, respectivamente, aos comandos " +
                    "0 e 1 na captura observada."
            )
        )
    }

    internal fun readUnsignedInt16BigEndian(payload: ByteArray, offset: Int): Int? {
        if (offset < 0 || offset >= payload.size - 1) return null
        val high = payload[offset].toInt() and 0xFF
        val low = payload[offset + 1].toInt() and 0xFF
        return (high shl 8) or low
    }
}
