package br.com.paivalab.controlapeso.domain.repository

import br.com.paivalab.controlapeso.domain.model.ScaleDevice
import kotlinx.coroutines.flow.Flow

interface ScaleDeviceRepository {
    fun observeAll(): Flow<List<ScaleDevice>>
    suspend fun findByAddress(address: String): ScaleDevice?
    suspend fun getAll(): List<ScaleDevice>
    suspend fun insert(device: ScaleDevice)
    suspend fun update(device: ScaleDevice)
    suspend fun delete(device: ScaleDevice)
    suspend fun setPreferred(device: ScaleDevice)
}
