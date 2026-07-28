package br.com.paivalab.controlapeso.bluetooth

import java.time.Instant
import java.util.Locale

object BleDiagnosticFormatter {
    const val PARSER_VERSION = "OKOK-C0-advertising/1"

    fun format(
        devices: List<BleDeviceResult>,
        history: Map<String, List<BleDeviceResult>>,
        maskAddresses: Boolean,
        generatedAt: Instant = Instant.now()
    ): String = buildString {
        appendLine("Controla Peso — diagnóstico BLE")
        appendLine("Parser: $PARSER_VERSION")
        appendLine("Gerado em: $generatedAt")
        appendLine("Dispositivos: ${devices.size}")
        devices.forEach { device ->
            appendLine()
            appendLine("Nome: ${device.advertisedName ?: "Dispositivo sem nome"}")
            appendLine(
                "Endereço: ${
                    if (maskAddresses) maskAddress(device.address) else device.address
                }"
            )
            appendLine("RSSI: ${device.rssi} dBm")
            appendLine("Última detecção: ${Instant.ofEpochMilli(device.lastSeenEpochMillis)}")
            appendLine("Possível Chipsea/OKOK: ${device.isPossibleChipseaOrOkok}")
            appendLine("UUIDs: ${device.serviceUuids.joinToString().ifEmpty { "-" }}")
            appendLine("Tx Power: ${device.txPower ?: "-"}")
            appendLine(
                "Manufacturer IDs: ${
                    device.manufacturerData.keys.sorted().joinToString {
                        String.format(Locale.ROOT, "0x%04X", it)
                    }.ifEmpty { "-" }
                }"
            )
            device.manufacturerData.toSortedMap().forEach { (id, bytes) ->
                appendLine(
                    "Manufacturer ${String.format(Locale.ROOT, "0x%04X", id)}: " +
                        BleHexFormatter.format(bytes)
                )
            }
            device.serviceData.entries.sortedBy { it.key.toString() }
                .forEach { (uuid, bytes) ->
                    appendLine("Service data $uuid: ${BleHexFormatter.format(bytes)}")
                }
            appendLine(
                "ScanRecord: ${
                    device.rawScanRecord?.let(BleHexFormatter::format) ?: "-"
                }"
            )
            device.okOkAdvertisement?.let { parsed ->
                appendLine("OKOK peso bruto: ${parsed.rawWeight}")
                appendLine("OKOK valor anunciado: ${parsed.weightValue}")
                appendLine(
                    "OKOK propriedade: ${
                        String.format(Locale.ROOT, "0x%02X", parsed.property)
                    }"
                )
                appendLine("OKOK payload: ${parsed.rawManufacturerDataHex}")
                appendLine("Notas: ${parsed.parserNotes.joinToString(" | ")}")
            }
            appendLine("Variações preservadas: ${history[device.address].orEmpty().size}")
        }
    }.trimEnd()

    fun maskAddress(address: String): String {
        val parts = address.split(':')
        if (parts.size != 6) return "••:••:••:••"
        return "${parts[0]}:${parts[1]}:••:••:${parts[4]}:${parts[5]}"
    }
}
