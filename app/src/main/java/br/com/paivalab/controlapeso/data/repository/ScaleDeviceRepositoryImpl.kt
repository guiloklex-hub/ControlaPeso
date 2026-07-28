package br.com.paivalab.controlapeso.data.repository

import br.com.paivalab.controlapeso.data.local.dao.ScaleDeviceDao
import br.com.paivalab.controlapeso.data.local.mapper.toDomain
import br.com.paivalab.controlapeso.data.local.mapper.toEntity
import br.com.paivalab.controlapeso.domain.model.ScaleDevice
import br.com.paivalab.controlapeso.domain.repository.ScaleDeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScaleDeviceRepositoryImpl(
    private val dao: ScaleDeviceDao
) : ScaleDeviceRepository {
    override fun observeAll(): Flow<List<ScaleDevice>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun findByAddress(address: String): ScaleDevice? =
        dao.findByAddress(address)?.toDomain()

    override suspend fun getAll(): List<ScaleDevice> = dao.getAll().map { it.toDomain() }

    override suspend fun insert(device: ScaleDevice) = dao.insert(device.toEntity())

    override suspend fun update(device: ScaleDevice) = dao.update(device.toEntity())

    override suspend fun delete(device: ScaleDevice) = dao.delete(device.toEntity())

    override suspend fun setPreferred(device: ScaleDevice) {
        dao.setPreferred(device.id)
    }
}
