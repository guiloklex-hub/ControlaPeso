package br.com.paivalab.controlapeso.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import br.com.paivalab.controlapeso.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY createdAt, id")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ProfileEntity?

    @Query("SELECT * FROM profiles ORDER BY createdAt, id")
    suspend fun getAll(): List<ProfileEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ProfileEntity>)

    @Update
    suspend fun update(entity: ProfileEntity)

    @Delete
    suspend fun delete(entity: ProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun deleteAll()

    @Query("UPDATE profiles SET isActive = 0, updatedAt = :updatedAt")
    suspend fun deactivateAll(updatedAt: java.time.Instant)

    @Query("UPDATE profiles SET isActive = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun activateById(id: String, updatedAt: java.time.Instant): Int

    @Transaction
    suspend fun setActive(id: String, updatedAt: java.time.Instant): Boolean {
        if (findById(id) == null) return false
        deactivateAll(updatedAt)
        return activateById(id, updatedAt) == 1
    }

    @Transaction
    suspend fun insertWithActiveConstraint(entity: ProfileEntity) {
        if (entity.isActive) deactivateAll(entity.updatedAt)
        insert(entity)
    }

    @Transaction
    suspend fun updateWithActiveConstraint(entity: ProfileEntity) {
        if (entity.isActive) deactivateAll(entity.updatedAt)
        update(entity)
    }
}
