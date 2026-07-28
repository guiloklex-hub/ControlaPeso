package br.com.paivalab.controlapeso.data.repository

import br.com.paivalab.controlapeso.core.time.AppClock
import br.com.paivalab.controlapeso.data.local.dao.ProfileDao
import br.com.paivalab.controlapeso.data.local.mapper.toDomain
import br.com.paivalab.controlapeso.data.local.mapper.toEntity
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProfileRepositoryImpl(
    private val dao: ProfileDao,
    private val clock: AppClock
) : ProfileRepository {
    override fun observeAll(): Flow<List<Profile>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeActive(): Flow<Profile?> =
        dao.observeActive().map { it?.toDomain() }

    override suspend fun findById(id: String): Profile? = dao.findById(id)?.toDomain()

    override suspend fun getAll(): List<Profile> = dao.getAll().map { it.toDomain() }

    override suspend fun insert(profile: Profile) =
        dao.insertWithActiveConstraint(profile.toEntity())

    override suspend fun update(profile: Profile) =
        dao.updateWithActiveConstraint(profile.toEntity())

    override suspend fun delete(profile: Profile) = dao.delete(profile.toEntity())

    override suspend fun setActive(id: String): Boolean = dao.setActive(id, clock.now())
}
