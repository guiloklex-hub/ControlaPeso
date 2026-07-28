package br.com.paivalab.controlapeso.data.healthconnect

import android.content.Context
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.WeightRecord
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement

enum class HealthConnectAvailability {
    AVAILABLE,
    NOT_INSTALLED,
    UPDATE_REQUIRED,
    ANDROID_VERSION_UNSUPPORTED
}

sealed interface HealthConnectWriteResult {
    data object Written : HealthConnectWriteResult
    data object PermissionRequired : HealthConnectWriteResult
    data object Unavailable : HealthConnectWriteResult
    data class Failed(val cause: Throwable) : HealthConnectWriteResult
}

class HealthConnectWeightWriter(context: Context) {
    private val applicationContext = context.applicationContext

    val requiredPermissions: Set<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setOf(HealthPermission.getWritePermission(WeightRecord::class))
        } else {
            emptySet()
        }

    fun availability(): HealthConnectAvailability {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return HealthConnectAvailability.ANDROID_VERSION_UNSUPPORTED
        }
        return when (HealthConnectClient.getSdkStatus(applicationContext)) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.UPDATE_REQUIRED
            else -> HealthConnectAvailability.NOT_INSTALLED
        }
    }

    fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    suspend fun hasWritePermission(): Boolean {
        if (availability() != HealthConnectAvailability.AVAILABLE) return false
        return runCatching {
            client().permissionController.getGrantedPermissions()
                .containsAll(requiredPermissions)
        }.getOrDefault(false)
    }

    suspend fun write(measurement: WeightMeasurement): HealthConnectWriteResult {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return HealthConnectWriteResult.Unavailable
        }
        if (availability() != HealthConnectAvailability.AVAILABLE) {
            return HealthConnectWriteResult.Unavailable
        }
        if (!hasWritePermission()) return HealthConnectWriteResult.PermissionRequired
        return writeApi26(measurement)
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    private suspend fun writeApi26(
        measurement: WeightMeasurement
    ): HealthConnectWriteResult = try {
        client().insertRecords(
            listOf(HealthConnectWeightMapper.toRecord(measurement))
        )
        HealthConnectWriteResult.Written
    } catch (error: Exception) {
        HealthConnectWriteResult.Failed(error)
    }

    private fun client(): HealthConnectClient =
        HealthConnectClient.getOrCreate(applicationContext)
}
