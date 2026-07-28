package br.com.paivalab.controlapeso.data.healthconnect

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Mass
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.ZoneOffset

object HealthConnectWeightMapper {
    @RequiresApi(Build.VERSION_CODES.O)
    fun toRecord(measurement: WeightMeasurement): WeightRecord {
        val metadata = when (measurement.source) {
            MeasurementSource.BLE -> Metadata.autoRecorded(
                device = Device(
                    type = Device.TYPE_SCALE
                ),
                clientRecordId = measurement.id,
                clientRecordVersion = measurement.updatedAt.toEpochMilli()
            )
            else -> Metadata.manualEntry(
                clientRecordId = measurement.id,
                clientRecordVersion = measurement.updatedAt.toEpochMilli()
            )
        }
        return WeightRecord(
            time = measurement.measuredAt,
            zoneOffset = measurement.zoneOffsetSeconds?.let(ZoneOffset::ofTotalSeconds),
            weight = Mass.kilograms(measurement.weightKg),
            metadata = metadata
        )
    }
}
