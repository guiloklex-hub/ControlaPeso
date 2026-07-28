package br.com.paivalab.controlapeso.bluetooth

/**
 * Leitura de peso derivada de um anúncio BLE já interpretado.
 *
 * [advertisedValue] reproduz o valor observado no OKOK, porém sua unidade não
 * é declarada aqui porque ainda depende de validação física.
 */
data class BleWeightReading(
    val deviceAddress: String,
    val deviceName: String?,
    val advertisedValue: Double,
    val rawWeight: Int,
    val property: Int,
    val sequenceNumber: Int,
    val rssi: Int,
    val observedAtEpochMillis: Long,
    val rawPayloadHex: String
) {
    val isMeasurementPacket: Boolean
        get() = property == PROPERTY_MEASUREMENT

    val isIdlePacket: Boolean
        get() = property == PROPERTY_IDLE

    companion object {
        const val PROPERTY_IDLE = 0x24
        const val PROPERTY_MEASUREMENT = 0x25
    }
}
