package br.com.paivalab.controlapeso.data.repository

import br.com.paivalab.controlapeso.data.local.dao.GoalDao
import br.com.paivalab.controlapeso.data.local.mapper.toDomain
import br.com.paivalab.controlapeso.data.local.mapper.toEntity
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import br.com.paivalab.controlapeso.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GoalRepositoryImpl(private val dao: GoalDao) : GoalRepository {
    override fun observeForProfile(profileId: String): Flow<List<WeightGoal>> =
        dao.observeForProfile(profileId).map { entities -> entities.map { it.toDomain() } }

    override fun observeActive(profileId: String): Flow<WeightGoal?> =
        dao.observeActive(profileId).map { it?.toDomain() }

    override suspend fun getAll(): List<WeightGoal> = dao.getAll().map { it.toDomain() }

    override suspend fun insert(goal: WeightGoal) {
        dao.insertWithActiveConstraint(goal.toEntity())
    }

    override suspend fun update(goal: WeightGoal) {
        dao.updateWithActiveConstraint(goal.toEntity())
    }

    override suspend fun delete(goal: WeightGoal) = dao.delete(goal.toEntity())
}
