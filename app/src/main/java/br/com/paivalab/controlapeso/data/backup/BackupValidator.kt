package br.com.paivalab.controlapeso.data.backup

import br.com.paivalab.controlapeso.data.preferences.ChartSize
import br.com.paivalab.controlapeso.data.preferences.HistoryGrouping
import br.com.paivalab.controlapeso.data.preferences.HistoryPeriod
import br.com.paivalab.controlapeso.data.preferences.LocalBackupFrequency
import br.com.paivalab.controlapeso.data.preferences.ThemeMode
import br.com.paivalab.controlapeso.data.preferences.VisualEffects
import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.MeasurementSource
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class BackupValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

object BackupValidator {
    fun validate(document: BackupDocument): BackupValidationResult {
        val errors = mutableListOf<String>()
        if (document.schemaVersion != BackupDocument.CURRENT_SCHEMA_VERSION) {
            errors += "Versão de esquema não suportada: ${document.schemaVersion}."
        }
        validateInstant(document.exportedAt, "exportedAt", errors)
        validateUniqueIds("perfis", document.profiles.map(BackupProfile::id), errors)
        validateUniqueIds("medições", document.measurements.map(BackupMeasurement::id), errors)
        validateUniqueIds("metas", document.goals.map(BackupGoal::id), errors)
        validateUniqueIds("dispositivos", document.devices.map(BackupDevice::id), errors)
        if (document.profiles.isNotEmpty() && document.profiles.count { it.isActive } != 1) {
            errors += "O backup deve possuir exatamente um perfil ativo."
        }
        val profileIds = document.profiles.mapTo(mutableSetOf(), BackupProfile::id)
        val deviceIds = document.devices.mapTo(mutableSetOf(), BackupDevice::id)

        document.profiles.forEach { profile ->
            validateUuid(profile.id, "perfil", errors)
            if (profile.name.isBlank() || profile.name.length > 80) {
                errors += "Perfil ${profile.id} possui nome inválido."
            }
            if (profile.heightCm != null &&
                (!profile.heightCm.isFinite() || profile.heightCm !in 80.0..250.0)
            ) {
                errors += "Perfil ${profile.id} possui altura inválida."
            }
            profile.birthDate?.let { validateDate(it, "nascimento do perfil", errors) }
            validateEnum<WeightUnit>(profile.preferredWeightUnit, "unidade do perfil", errors)
            validateInstant(profile.createdAt, "criação do perfil", errors)
            validateInstant(profile.updatedAt, "atualização do perfil", errors)
        }
        document.measurements.forEach { measurement ->
            validateUuid(measurement.id, "medição", errors)
            if (measurement.profileId != null && measurement.profileId !in profileIds) {
                errors += "Medição ${measurement.id} referencia perfil inexistente."
            }
            if (measurement.deviceId != null && measurement.deviceId !in deviceIds) {
                errors += "Medição ${measurement.id} referencia dispositivo inexistente."
            }
            if (!measurement.weightKg.isFinite() || measurement.weightKg !in 2.0..500.0) {
                errors += "Medição ${measurement.id} possui peso inválido."
            }
            if ((measurement.note?.length ?: 0) > 500) {
                errors += "Medição ${measurement.id} possui observação longa demais."
            }
            if (
                measurement.zoneOffsetSeconds != null &&
                measurement.zoneOffsetSeconds !in -64_800..64_800
            ) {
                errors += "Medição ${measurement.id} possui fuso horário inválido."
            }
            validateEnum<MeasurementSource>(measurement.source, "origem", errors)
            validateInstant(measurement.measuredAt, "data da medição", errors)
            validateInstant(measurement.createdAt, "criação da medição", errors)
            validateInstant(measurement.updatedAt, "atualização da medição", errors)
            listOf(
                measurement.impedanceOne,
                measurement.impedanceTwo,
                measurement.bodyFatPercent,
                measurement.muscleMassKg,
                measurement.bodyWaterPercent,
                measurement.boneMassKg,
                measurement.visceralFatLevel
            ).filterNotNull().forEach { value ->
                if (!value.isFinite()) {
                    errors += "Medição ${measurement.id} possui métrica não finita."
                }
            }
        }
        document.goals.forEach { goal ->
            validateUuid(goal.id, "meta", errors)
            if (goal.profileId !in profileIds) {
                errors += "Meta ${goal.id} referencia perfil inexistente."
            }
            if (!goal.startWeightKg.isFinite() || goal.startWeightKg !in 2.0..500.0) {
                errors += "Meta ${goal.id} possui peso inicial inválido."
            }
            if (!goal.targetWeightKg.isFinite() || goal.targetWeightKg !in 2.0..500.0) {
                errors += "Meta ${goal.id} possui destino inválido."
            }
            validateDate(goal.startDate, "início da meta", errors)
            goal.targetDate?.let { validateDate(it, "prazo da meta", errors) }
            validateEnum<GoalStatus>(goal.status, "status da meta", errors)
            validateInstant(goal.createdAt, "criação da meta", errors)
            validateInstant(goal.updatedAt, "atualização da meta", errors)
        }
        document.devices.forEach { device ->
            validateUuid(device.id, "dispositivo", errors)
            if (device.bluetoothAddress.isBlank()) {
                errors += "Dispositivo ${device.id} possui endereço vazio."
            }
            device.lastConnectedAt?.let { validateInstant(it, "última conexão", errors) }
            device.lastSeenAt?.let { validateInstant(it, "última detecção", errors) }
            validateInstant(device.createdAt, "criação do dispositivo", errors)
            validateInstant(device.updatedAt, "atualização do dispositivo", errors)
        }
        validatePreferences(document.preferences, errors)
        return BackupValidationResult(errors.isEmpty(), errors.distinct())
    }

    private fun validatePreferences(value: BackupPreferences, errors: MutableList<String>) {
        validateEnum<ThemeMode>(value.themeMode, "tema", errors)
        validateEnum<VisualEffects>(value.visualEffects, "efeitos", errors)
        validateEnum<WeightUnit>(value.defaultWeightUnit, "unidade padrão", errors)
        validateEnum<HistoryPeriod>(value.defaultHistoryPeriod, "período", errors)
        validateEnum<HistoryGrouping>(value.historyGrouping, "agrupamento", errors)
        validateEnum<ChartSize>(value.chartSize, "tamanho do gráfico", errors)
        validateEnum<LocalBackupFrequency>(
            value.localBackupFrequency,
            "frequência do backup local",
            errors
        )
        if (value.reminderDaysMask !in 0..0b1111111) errors += "Dias do lembrete inválidos."
        if (value.reminderHour !in 0..23) errors += "Hora do lembrete inválida."
        if (value.reminderMinute !in 0..59) errors += "Minuto do lembrete inválido."
    }

    private fun validateUniqueIds(
        type: String,
        ids: List<String>,
        errors: MutableList<String>
    ) {
        if (ids.distinct().size != ids.size) errors += "Há IDs duplicados em $type."
    }

    private fun validateUuid(value: String, type: String, errors: MutableList<String>) {
        try {
            UUID.fromString(value)
        } catch (_: IllegalArgumentException) {
            errors += "ID de $type inválido: $value."
        }
    }

    private fun validateInstant(value: String, field: String, errors: MutableList<String>) {
        try {
            Instant.parse(value)
        } catch (_: DateTimeException) {
            errors += "Instante inválido em $field."
        }
    }

    private fun validateDate(value: String, field: String, errors: MutableList<String>) {
        try {
            LocalDate.parse(value)
        } catch (_: DateTimeException) {
            errors += "Data inválida em $field."
        }
    }

    private inline fun <reified T : Enum<T>> validateEnum(
        value: String,
        field: String,
        errors: MutableList<String>
    ) {
        if (enumValues<T>().none { it.name == value }) {
            errors += "Valor inválido em $field: $value."
        }
    }
}
