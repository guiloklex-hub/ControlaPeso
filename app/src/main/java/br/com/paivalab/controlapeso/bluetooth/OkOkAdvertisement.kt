package br.com.paivalab.controlapeso.bluetooth

/**
 * Interpretação do Manufacturer Advertising observado na balança Yoda1.
 *
 * As escalas numéricas reproduzem exatamente a interpretação registrada pelo
 * OKOK International na captura de 27/07/2026. A unidade do peso e a semântica
 * do valor secundário ainda não foram confirmadas no dispositivo físico.
 */
data class OkOkAdvertisement(
    val manufacturerId: Int,
    val protocolMarker: Int,
    val sequenceNumber: Int,
    val rawWeight: Int,
    val weightValue: Double,
    val rawSecondaryValue: Int,
    val secondaryValue: Double,
    val property: Int,
    val commandId: Int?,
    val rawManufacturerDataHex: String,
    val parserNotes: List<String>
)
