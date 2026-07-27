package br.com.paivalab.controlapeso.bluetooth

import java.time.DateTimeException
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID

/**
 * UUIDs observados em algumas implementações Chipsea.
 *
 * A presença deles é apenas um indício. Uma balança compatível com OKOK pode usar
 * outra variante de protocolo e não deve ser descartada por não anunciá-los.
 */
object ChipseaUuids {
    val PROPRIETARY_SERVICE: UUID = uuid16("FFF0")
    val NOTIFICATION_CHARACTERISTIC: UUID = uuid16("FFF1")
    val WRITE_CHARACTERISTIC: UUID = uuid16("FFF2")
    val BODY_COMPOSITION_SERVICE: UUID = uuid16("181B")
    val BODY_COMPOSITION_MEASUREMENT: UUID = uuid16("2A9C")
    val BATTERY_SERVICE: UUID = uuid16("180F")
    val BATTERY_LEVEL: UUID = uuid16("2A19")
    val CLIENT_CHARACTERISTIC_CONFIGURATION: UUID = uuid16("2902")

    fun detectionSignals(
        advertisedName: String?,
        serviceUuids: Collection<UUID>,
        manufacturerData: Map<Int, ByteArray>,
        serviceData: Map<UUID, ByteArray>
    ): List<String> = buildList {
        if (advertisedName?.contains("chipsea", ignoreCase = true) == true) {
            add("Nome anunciado contém Chipsea")
        }
        if (PROPRIETARY_SERVICE in serviceUuids) {
            add("Serviço proprietário FFF0 anunciado")
        }
        if (BODY_COMPOSITION_SERVICE in serviceUuids) {
            add("Body Composition Service 181B anunciado")
        }

        manufacturerData.forEach { (manufacturerId, payload) ->
            if (looksLikeKnownConnectedPacket(payload)) {
                val formattedId = String.format(Locale.ROOT, "0x%04X", manufacturerId)
                add("Manufacturer data $formattedId tem estrutura semelhante ao pacote conhecido")
            }
        }
        serviceData.forEach { (serviceUuid, payload) ->
            if (looksLikeKnownConnectedPacket(payload)) {
                add("Service data $serviceUuid tem estrutura semelhante ao pacote conhecido")
            }
        }
    }

    /**
     * Heurística conservadora: só considera a estrutura temporal descrita para o
     * pacote conectado de 16+ bytes. Não procura esse formato dentro do frame bruto
     * do anúncio, pois offsets de AD structures não equivalem aos offsets do pacote.
     */
    internal fun looksLikeKnownConnectedPacket(payload: ByteArray): Boolean {
        if (payload.size < 16) return false

        val year = readUnsignedInt16LittleEndian(payload, 2) ?: return false
        val month = payload[4].toInt() and 0xFF
        val day = payload[5].toInt() and 0xFF
        val hour = payload[6].toInt() and 0xFF
        val minute = payload[7].toInt() and 0xFF
        val second = payload[8].toInt() and 0xFF

        return try {
            LocalDateTime.of(year, month, day, hour, minute, second)
            true
        } catch (_: DateTimeException) {
            false
        }
    }

    private fun readUnsignedInt16LittleEndian(payload: ByteArray, offset: Int): Int? {
        if (offset < 0 || offset >= payload.size - 1) return null
        val low = payload[offset].toInt() and 0xFF
        val high = payload[offset + 1].toInt() and 0xFF
        return low or (high shl 8)
    }

    private fun uuid16(shortUuid: String): UUID =
        UUID.fromString("0000${shortUuid.lowercase(Locale.ROOT)}-0000-1000-8000-00805f9b34fb")
}
