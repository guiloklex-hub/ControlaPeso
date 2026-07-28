package br.com.paivalab.controlapeso.domain.repository

import br.com.paivalab.controlapeso.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeAll(): Flow<List<Profile>>
    fun observeActive(): Flow<Profile?>
    suspend fun findById(id: String): Profile?
    suspend fun getAll(): List<Profile>
    suspend fun insert(profile: Profile)
    suspend fun update(profile: Profile)
    suspend fun delete(profile: Profile)
    suspend fun setActive(id: String): Boolean
}
