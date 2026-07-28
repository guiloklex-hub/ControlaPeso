package br.com.paivalab.controlapeso.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import br.com.paivalab.controlapeso.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE profileId = :profileId ORDER BY createdAt DESC, id")
    fun observeForProfile(profileId: String): Flow<List<GoalEntity>>

    @Query(
        "SELECT * FROM goals WHERE profileId = :profileId AND isActive = 1 " +
            "ORDER BY updatedAt DESC LIMIT 1"
    )
    fun observeActive(profileId: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals ORDER BY createdAt, id")
    suspend fun getAll(): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<GoalEntity>)

    @Update
    suspend fun update(entity: GoalEntity)

    @Delete
    suspend fun delete(entity: GoalEntity)

    @Query(
        "UPDATE goals SET isActive = 0, " +
            "status = CASE WHEN status = 'ACTIVE' THEN 'PAUSED' ELSE status END " +
            "WHERE profileId = :profileId"
    )
    suspend fun deactivateForProfile(profileId: String)

    @Query("DELETE FROM goals")
    suspend fun deleteAll()

    @Transaction
    suspend fun insertWithActiveConstraint(entity: GoalEntity) {
        if (entity.isActive) deactivateForProfile(entity.profileId)
        insert(entity)
    }

    @Transaction
    suspend fun updateWithActiveConstraint(entity: GoalEntity) {
        if (entity.isActive) deactivateForProfile(entity.profileId)
        update(entity)
    }
}
