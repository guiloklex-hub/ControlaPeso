package br.com.paivalab.controlapeso.domain.repository

import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {
    fun observeAll(): Flow<List<WeightMeasurement>>
    fun observeForProfile(profileId: String): Flow<List<WeightMeasurement>>
    fun observeUnassigned(): Flow<List<WeightMeasurement>>
    fun observeInRange(
        profileId: String,
        startInclusive: Instant,
        endExclusive: Instant
    ): Flow<List<WeightMeasurement>>
    fun observeById(id: String): Flow<WeightMeasurement?>
    suspend fun findById(id: String): WeightMeasurement?
    suspend fun getAll(): List<WeightMeasurement>
    suspend fun insert(measurement: WeightMeasurement)
    suspend fun insertIgnoringConflicts(measurements: List<WeightMeasurement>): Int
    suspend fun update(measurement: WeightMeasurement)
    suspend fun delete(measurement: WeightMeasurement)
    suspend fun deleteById(id: String): Boolean
    suspend fun deleteDemoData(): Int
    suspend fun assignToProfile(
        measurementIds: List<String>,
        profileId: String,
        updatedAt: Instant
    ): Int
    suspend fun findProbableDuplicate(
        profileId: String?,
        weightKg: Double,
        measuredAt: Instant,
        deviceAddress: String?,
        window: Duration = Duration.ofMinutes(2),
        toleranceKg: Double = 0.05
    ): WeightMeasurement?
}
