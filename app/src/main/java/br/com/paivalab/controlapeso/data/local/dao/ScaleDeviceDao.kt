package br.com.paivalab.controlapeso.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import br.com.paivalab.controlapeso.data.local.entity.ScaleDeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScaleDeviceDao {
    @Query("SELECT * FROM scale_devices ORDER BY isPreferred DESC, lastSeenAt DESC, id")
    fun observeAll(): Flow<List<ScaleDeviceEntity>>

    @Query("SELECT * FROM scale_devices WHERE bluetoothAddress = :address LIMIT 1")
    suspend fun findByAddress(address: String): ScaleDeviceEntity?

    @Query("SELECT * FROM scale_devices ORDER BY createdAt, id")
    suspend fun getAll(): List<ScaleDeviceEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ScaleDeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ScaleDeviceEntity>)

    @Update
    suspend fun update(entity: ScaleDeviceEntity)

    @Delete
    suspend fun delete(entity: ScaleDeviceEntity)

    @Query("UPDATE scale_devices SET isPreferred = 0")
    suspend fun clearPreferred()

    @Query("UPDATE scale_devices SET isPreferred = 1 WHERE id = :id")
    suspend fun markPreferred(id: String): Int

    @Transaction
    suspend fun setPreferred(id: String): Boolean {
        clearPreferred()
        return markPreferred(id) == 1
    }

    @Query("DELETE FROM scale_devices")
    suspend fun deleteAll()
}
