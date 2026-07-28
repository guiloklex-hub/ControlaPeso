package br.com.paivalab.controlapeso.data.repository

import br.com.paivalab.controlapeso.data.local.dao.MeasurementDao
import br.com.paivalab.controlapeso.data.local.mapper.toDomain
import br.com.paivalab.controlapeso.data.local.mapper.toEntity
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.repository.MeasurementRepository
import br.com.paivalab.controlapeso.domain.usecase.measurement.ProbableDuplicatePolicy
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MeasurementRepositoryImpl(
    private val dao: MeasurementDao
) : MeasurementRepository {
    override fun observeAll(): Flow<List<WeightMeasurement>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeForProfile(profileId: String): Flow<List<WeightMeasurement>> =
        dao.observeForProfile(profileId).map { entities -> entities.map { it.toDomain() } }

    override fun observeInRange(
        profileId: String,
        startInclusive: Instant,
        endExclusive: Instant
    ): Flow<List<WeightMeasurement>> =
        dao.observeInRange(profileId, startInclusive, endExclusive)
            .map { entities -> entities.map { it.toDomain() } }

    override fun observeById(id: String): Flow<WeightMeasurement?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun findById(id: String): WeightMeasurement? =
        dao.findById(id)?.toDomain()

    override suspend fun getAll(): List<WeightMeasurement> =
        dao.getAll().map { it.toDomain() }

    override suspend fun insert(measurement: WeightMeasurement) =
        dao.insert(measurement.toEntity())

    override suspend fun insertIgnoringConflicts(
        measurements: List<WeightMeasurement>
    ): Int = dao.insertAllIgnoringConflicts(measurements.map { it.toEntity() })
        .count { rowId -> rowId != -1L }

    override suspend fun update(measurement: WeightMeasurement) =
        dao.update(measurement.toEntity())

    override suspend fun delete(measurement: WeightMeasurement) =
        dao.delete(measurement.toEntity())

    override suspend fun deleteById(id: String): Boolean = dao.deleteById(id) == 1

    override suspend fun deleteDemoData(): Int = dao.deleteDemoData()

    override suspend fun findProbableDuplicate(
        profileId: String,
        weightKg: Double,
        measuredAt: Instant,
        deviceAddress: String?,
        window: Duration,
        toleranceKg: Double
    ): WeightMeasurement? {
        val bounds = ProbableDuplicatePolicy.window(measuredAt, window)
        return dao.findProbableDuplicate(
            profileId = profileId,
            weightKg = weightKg,
            toleranceKg = toleranceKg,
            centerTime = measuredAt,
            windowStart = bounds.first,
            windowEnd = bounds.second,
            deviceAddress = deviceAddress
        )?.toDomain()
    }
}
