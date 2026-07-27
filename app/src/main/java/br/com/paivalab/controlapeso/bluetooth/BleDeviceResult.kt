package br.com.paivalab.controlapeso.bluetooth

import java.util.UUID

data class BleDeviceResult(
    val advertisedName: String?,
    val address: String,
    val rssi: Int,
    val lastSeenEpochMillis: Long,
    val serviceUuids: List<UUID>,
    val txPower: Int?,
    val manufacturerData: Map<Int, ByteArray>,
    val serviceData: Map<UUID, ByteArray>,
    val rawScanRecord: ByteArray?,
    val chipseaDetectionSignals: List<String>,
    val okOkAdvertisement: OkOkAdvertisement?
) {
    val isPossibleChipseaOrOkok: Boolean
        get() = chipseaDetectionSignals.isNotEmpty() || okOkAdvertisement != null
}
