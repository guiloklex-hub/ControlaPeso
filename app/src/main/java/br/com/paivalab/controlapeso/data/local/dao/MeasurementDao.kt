package br.com.paivalab.controlapeso.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.paivalab.controlapeso.data.local.entity.WeightMeasurementEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM weight_measurements ORDER BY measuredAt, id")
    fun observeAll(): Flow<List<WeightMeasurementEntity>>

    @Query(
        "SELECT * FROM weight_measurements WHERE profileId = :profileId " +
            "ORDER BY measuredAt DESC, id DESC"
    )
    fun observeForProfile(profileId: String): Flow<List<WeightMeasurementEntity>>

    @Query(
        "SELECT * FROM weight_measurements WHERE profileId = :profileId " +
            "AND measuredAt >= :startInclusive AND measuredAt < :endExclusive " +
            "ORDER BY measuredAt ASC, id ASC"
    )
    fun observeInRange(
        profileId: String,
        startInclusive: Instant,
        endExclusive: Instant
    ): Flow<List<WeightMeasurementEntity>>

    @Query("SELECT * FROM weight_measurements WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<WeightMeasurementEntity?>

    @Query("SELECT * FROM weight_measurements WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): WeightMeasurementEntity?

    @Query("SELECT * FROM weight_measurements ORDER BY measuredAt, id")
    suspend fun getAll(): List<WeightMeasurementEntity>

    @Query(
        "SELECT * FROM weight_measurements WHERE profileId = :profileId " +
            "AND measuredAt BETWEEN :windowStart AND :windowEnd " +
            "AND ABS(weightKg - :weightKg) <= :toleranceKg " +
            "AND (:deviceAddress IS NULL OR deviceAddress = :deviceAddress) " +
            "ORDER BY ABS(measuredAt - :centerTime), id LIMIT 1"
    )
    suspend fun findProbableDuplicate(
        profileId: String,
        weightKg: Double,
        toleranceKg: Double,
        centerTime: Instant,
        windowStart: Instant,
        windowEnd: Instant,
        deviceAddress: String?
    ): WeightMeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: WeightMeasurementEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnoringConflicts(entities: List<WeightMeasurementEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<WeightMeasurementEntity>)

    @Update
    suspend fun update(entity: WeightMeasurementEntity)

    @Delete
    suspend fun delete(entity: WeightMeasurementEntity)

    @Query("DELETE FROM weight_measurements WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("DELETE FROM weight_measurements WHERE source = 'DEMO'")
    suspend fun deleteDemoData(): Int

    @Query("DELETE FROM weight_measurements")
    suspend fun deleteAll()
}
