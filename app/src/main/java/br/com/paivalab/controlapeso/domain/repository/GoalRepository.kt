package br.com.paivalab.controlapeso.domain.repository

import br.com.paivalab.controlapeso.domain.model.WeightGoal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeForProfile(profileId: String): Flow<List<WeightGoal>>
    fun observeActive(profileId: String): Flow<WeightGoal?>
    suspend fun getAll(): List<WeightGoal>
    suspend fun insert(goal: WeightGoal)
    suspend fun update(goal: WeightGoal)
    suspend fun delete(goal: WeightGoal)
}
