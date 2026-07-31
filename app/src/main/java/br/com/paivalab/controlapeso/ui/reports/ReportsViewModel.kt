package br.com.paivalab.controlapeso.ui.reports

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.core.time.BrazilianDateFormatter
import br.com.paivalab.controlapeso.data.backup.BackupPreview
import br.com.paivalab.controlapeso.data.backup.BackupPreviewResult
import br.com.paivalab.controlapeso.data.backup.BackupRestoreResult
import br.com.paivalab.controlapeso.data.backup.LimitedTextReader
import br.com.paivalab.controlapeso.data.backup.RestoreMode
import br.com.paivalab.controlapeso.data.export.ReportData
import br.com.paivalab.controlapeso.data.export.ReportFileNames
import br.com.paivalab.controlapeso.data.export.ReportFormat
import br.com.paivalab.controlapeso.data.export.ReportOptions
import br.com.paivalab.controlapeso.data.export.ReportTextSummaryFormatter
import br.com.paivalab.controlapeso.data.export.SharedReportFile
import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.statistics.MeasurementStatistics
import br.com.paivalab.controlapeso.worker.ReportCacheCleaner
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ReportPeriod {
    DAYS_7,
    DAYS_30,
    MONTHS_3,
    MONTHS_6,
    YEAR_1,
    ALL,
    CUSTOM
}

enum class ReportsMessage {
    GENERATED,
    SAVED,
    RESTORED_MERGE,
    RESTORED_REPLACE,
    CACHE_CLEARED
}

enum class ReportsError {
    PROFILE_REQUIRED,
    INVALID_PERIOD,
    NO_MEASUREMENTS,
    GENERATION_FAILED,
    FILE_READ_FAILED,
    FILE_WRITE_FAILED,
    RESTORE_FAILED
}

data class ReportsUiState(
    val profiles: List<Profile> = emptyList(),
    val selectedProfileId: String? = null,
    val period: ReportPeriod = ReportPeriod.DAYS_30,
    val customStartText: String = "",
    val customEndText: String = "",
    val format: ReportFormat = ReportFormat.PDF,
    val includeChart: Boolean = true,
    val includeTable: Boolean = true,
    val includeNotes: Boolean = true,
    /** Reserved until the scale protocol and domain policy validate metrics. */
    val includeAdditionalMetrics: Boolean = false,
    val unit: WeightUnit = WeightUnit.KILOGRAM,
    val isWorking: Boolean = false,
    val generatedFile: SharedReportFile? = null,
    val generatedSummaryText: String? = null,
    val importPreview: BackupPreview? = null,
    val message: ReportsMessage? = null,
    val error: ReportsError? = null,
    val validationErrors: List<String> = emptyList()
)

class ReportsViewModel(
    private val container: AppContainer,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) : ViewModel() {
    private val local = MutableStateFlow(ReportsUiState())
    private var pendingImportContent: String? = null

    val uiState: StateFlow<ReportsUiState> = combine(
        container.profileRepository.observeAll(),
        container.preferencesRepository.preferences,
        local
    ) { profiles, preferences, state ->
        val selected = state.selectedProfileId
            ?: profiles.firstOrNull(Profile::isActive)?.id
            ?: profiles.firstOrNull()?.id
        state.copy(
            profiles = profiles,
            selectedProfileId = selected,
            unit = preferences.defaultWeightUnit
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ReportsUiState()
    )

    fun setProfile(id: String) {
        local.update {
            it.copy(
                selectedProfileId = id,
                generatedFile = null,
                generatedSummaryText = null,
                error = null
            )
        }
    }
    fun setPeriod(value: ReportPeriod) = update { copy(period = value) }
    fun setCustomStart(value: String) = update {
        copy(customStartText = BrazilianDateFormatter.inputDigits(value))
    }
    fun setCustomEnd(value: String) = update {
        copy(customEndText = BrazilianDateFormatter.inputDigits(value))
    }
    fun setFormat(value: ReportFormat) = update { copy(format = value) }
    fun setIncludeChart(value: Boolean) = update { copy(includeChart = value) }
    fun setIncludeTable(value: Boolean) = update { copy(includeTable = value) }
    fun setIncludeNotes(value: Boolean) = update { copy(includeNotes = value) }
    @Suppress("UNUSED_PARAMETER")
    fun setIncludeAdditionalMetrics(value: Boolean) =
        update { copy(includeAdditionalMetrics = false) }
    fun dismissMessage() = local.update {
        it.copy(message = null, error = null, validationErrors = emptyList())
    }

    fun generate() {
        val state = uiState.value
        if (state.isWorking) return
        val profile = state.profiles.firstOrNull { it.id == state.selectedProfileId }
        if (state.format != ReportFormat.JSON && profile == null) {
            local.update { it.copy(error = ReportsError.PROFILE_REQUIRED) }
            return
        }
        val bounds = periodBounds(state)
        if (state.period == ReportPeriod.CUSTOM && bounds == null) {
            local.update { it.copy(error = ReportsError.INVALID_PERIOD) }
            return
        }
        local.update {
            it.copy(
                isWorking = true,
                error = null,
                message = null,
                validationErrors = emptyList()
            )
        }
        viewModelScope.launch {
            try {
                val artifact = withContext(Dispatchers.IO) {
                    if (state.format == ReportFormat.JSON) {
                        generateJson()
                    } else {
                        requireNotNull(profile)
                        generateReport(state, profile, bounds)
                    }
                }
                local.update {
                    it.copy(
                        isWorking = false,
                        generatedFile = artifact.file,
                        generatedSummaryText = artifact.summaryText,
                        message = ReportsMessage.GENERATED
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Throwable) {
                local.update {
                    it.copy(
                        isWorking = false,
                        error = if (failure is NoSuchElementException) {
                            ReportsError.NO_MEASUREMENTS
                        } else {
                            ReportsError.GENERATION_FAILED
                        }
                    )
                }
            }
        }
    }

    fun saveGeneratedTo(uri: Uri) {
        val generated = uiState.value.generatedFile ?: return
        viewModelScope.launch {
            val success = try {
                withContext(Dispatchers.IO) {
                    container.applicationContext.contentResolver.openOutputStream(uri)
                        ?.use { output ->
                            generated.file.inputStream().use { input -> input.copyTo(output) }
                        } ?: error("Destino indisponível")
                }
                true
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                false
            }
            local.update {
                if (success) {
                    it.copy(message = ReportsMessage.SAVED, error = null)
                } else {
                    it.copy(error = ReportsError.FILE_WRITE_FAILED)
                }
            }
        }
    }

    fun importFrom(uri: Uri) {
        local.update { it.copy(isWorking = true, error = null, validationErrors = emptyList()) }
        viewModelScope.launch {
            val content = try {
                withContext(Dispatchers.IO) {
                    container.applicationContext.contentResolver.openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { LimitedTextReader.read(it) }
                        ?: error("Arquivo indisponível")
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                local.update { state ->
                    state.copy(isWorking = false, error = ReportsError.FILE_READ_FAILED)
                }
                return@launch
            }
            previewImportContent(content)
        }
    }

    /** Used by the Drive workflow after it has downloaded a remote JSON file. */
    fun importContent(content: String) {
        if (uiState.value.isWorking) return
        local.update { it.copy(isWorking = true, error = null, validationErrors = emptyList()) }
        viewModelScope.launch { previewImportContent(content) }
    }

    fun dismissImportPreview() {
        pendingImportContent = null
        local.update { it.copy(importPreview = null) }
    }

    fun restore(mode: RestoreMode) {
        val content = pendingImportContent ?: return
        local.update { it.copy(isWorking = true, importPreview = null) }
        viewModelScope.launch {
            val operation: Result<Pair<BackupRestoreResult, SharedReportFile?>> = try {
                Result.success(
                    withContext(Dispatchers.IO) {
                        val safetyFile = if (mode == RestoreMode.REPLACE) {
                            val now = container.clock.now()
                            val safetyJson = container.jsonBackupManager.exportJson(now)
                            container.shareFileService.create(
                                ReportFileNames.create(
                                    "backup-seguranca",
                                    "json",
                                    now
                                ),
                                JSON_MIME,
                                safetyJson.toByteArray(Charsets.UTF_8)
                            )
                        } else {
                            null
                        }
                        container.jsonBackupManager.restore(content, mode) to safetyFile
                    }
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Throwable) {
                Result.failure(failure)
            }
            if (operation.isFailure) {
                local.update {
                    it.copy(isWorking = false, error = ReportsError.RESTORE_FAILED)
                }
                return@launch
            }
            val (result, safetyFile) = operation.getOrThrow()
            when (result) {
                is BackupRestoreResult.Success -> {
                    pendingImportContent = null
                    local.update {
                        it.copy(
                            isWorking = false,
                            generatedFile = safetyFile ?: it.generatedFile,
                            generatedSummaryText = if (safetyFile != null) {
                                null
                            } else {
                                it.generatedSummaryText
                            },
                            message = if (mode == RestoreMode.MERGE) {
                                ReportsMessage.RESTORED_MERGE
                            } else {
                                ReportsMessage.RESTORED_REPLACE
                            }
                        )
                    }
                }
                is BackupRestoreResult.Invalid -> local.update {
                    it.copy(
                        isWorking = false,
                        generatedFile = safetyFile ?: it.generatedFile,
                        error = ReportsError.RESTORE_FAILED,
                        validationErrors = result.errors
                    )
                }
                is BackupRestoreResult.Failure -> local.update {
                    it.copy(
                        isWorking = false,
                        generatedFile = safetyFile ?: it.generatedFile,
                        error = ReportsError.RESTORE_FAILED
                    )
                }
            }
        }
    }

    fun clearTemporaryFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            ReportCacheCleaner.clean(
                container.shareFileService.sharedDirectory,
                Long.MAX_VALUE,
                maxAgeMillis = 0
            )
            local.update {
                it.copy(
                    generatedFile = null,
                    generatedSummaryText = null,
                    message = ReportsMessage.CACHE_CLEARED
                )
            }
        }
    }

    private suspend fun previewImportContent(content: String) {
        when (val result = withContext(Dispatchers.IO) {
            container.jsonBackupManager.preview(content)
        }) {
            is BackupPreviewResult.Invalid -> local.update {
                it.copy(
                    isWorking = false,
                    validationErrors = result.errors,
                    error = ReportsError.RESTORE_FAILED
                )
            }
            is BackupPreviewResult.Valid -> {
                pendingImportContent = content
                local.update {
                    it.copy(isWorking = false, importPreview = result.preview)
                }
            }
        }
    }

    private suspend fun generateJson(): GeneratedArtifact {
        val now = container.clock.now()
        val bytes = container.jsonBackupManager.exportJson(now).toByteArray(Charsets.UTF_8)
        return GeneratedArtifact(
            file = container.shareFileService.create(
                ReportFileNames.create("backup-completo", "json", now),
                JSON_MIME,
                bytes
            ),
            summaryText = null
        )
    }

    private suspend fun generateReport(
        state: ReportsUiState,
        profile: Profile,
        bounds: Pair<Instant, Instant>?
    ): GeneratedArtifact {
        val all = container.measurementRepository.getAll()
        val measurements = all.filter { measurement ->
            measurement.profileId == profile.id &&
                (bounds == null ||
                    (measurement.measuredAt >= bounds.first &&
                        measurement.measuredAt < bounds.second))
        }.sortedBy(WeightMeasurement::measuredAt)
        if (measurements.isEmpty()) throw NoSuchElementException("Sem medições")
        val now = container.clock.now()
        val data = ReportData(
            profile = profile,
            measurements = measurements,
            statistics = MeasurementStatistics.calculate(measurements),
            generatedAt = now,
            startInclusive = bounds?.first,
            endExclusive = bounds?.second
        )
        val options = ReportOptions(
            profileId = profile.id,
            startInclusive = bounds?.first,
            endExclusive = bounds?.second,
            format = state.format,
            includeChart = state.includeChart,
            includeTable = state.includeTable,
            includeNotes = state.includeNotes,
            includeAdditionalMetrics = false,
            unit = state.unit
        )
        val (extension, mimeType, bytes) = when (state.format) {
            ReportFormat.CSV -> Triple(
                "csv",
                CSV_MIME,
                container.csvExportService.generate(
                    data,
                    state.unit,
                    state.includeNotes,
                    false
                )
            )
            ReportFormat.PDF -> {
                val output = ByteArrayOutputStream()
                container.pdfReportService.generate(data, options, output)
                Triple("pdf", PDF_MIME, output.toByteArray())
            }
            ReportFormat.JSON -> error("JSON tratado separadamente")
        }
        return GeneratedArtifact(
            file = container.shareFileService.create(
                ReportFileNames.create(profile.name, extension, now),
                mimeType,
                bytes
            ),
            summaryText = ReportTextSummaryFormatter.format(data, state.unit)
        )
    }

    private fun periodBounds(state: ReportsUiState): Pair<Instant, Instant>? {
        if (state.period == ReportPeriod.ALL) return null
        val today = container.clock.now().atZone(zoneId).toLocalDate()
        val start = when (state.period) {
            ReportPeriod.DAYS_7 -> today.minusDays(6)
            ReportPeriod.DAYS_30 -> today.minusDays(29)
            ReportPeriod.MONTHS_3 -> today.minusMonths(3)
            ReportPeriod.MONTHS_6 -> today.minusMonths(6)
            ReportPeriod.YEAR_1 -> today.minusYears(1)
            ReportPeriod.CUSTOM ->
                BrazilianDateFormatter.parseOrNull(state.customStartText) ?: return null
            ReportPeriod.ALL -> return null
        }
        val end = if (state.period == ReportPeriod.CUSTOM) {
            BrazilianDateFormatter.parseOrNull(state.customEndText) ?: return null
        } else {
            today
        }
        if (end < start) return null
        return start.atStartOfDay(zoneId).toInstant() to
            end.plusDays(1).atStartOfDay(zoneId).toInstant()
    }

    private fun update(transform: ReportsUiState.() -> ReportsUiState) {
        local.update {
            it.transform().copy(
                generatedFile = null,
                generatedSummaryText = null,
                error = null,
                message = null,
                validationErrors = emptyList()
            )
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ReportsViewModel(container) as T
    }

    companion object {
        const val PDF_MIME = "application/pdf"
        const val CSV_MIME = "text/csv"
        const val JSON_MIME = "application/json"
    }
}

private data class GeneratedArtifact(
    val file: SharedReportFile,
    val summaryText: String?
)
