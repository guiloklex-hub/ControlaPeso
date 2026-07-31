package br.com.paivalab.controlapeso.data.export

import br.com.paivalab.controlapeso.domain.model.Profile
import br.com.paivalab.controlapeso.domain.model.WeightMeasurement
import br.com.paivalab.controlapeso.domain.model.WeightUnit
import br.com.paivalab.controlapeso.domain.usecase.statistics.WeightStatistics
import java.time.Instant

enum class ReportFormat {
    PDF,
    CSV,
    JSON
}

data class ReportOptions(
    val profileId: String,
    val startInclusive: Instant?,
    val endExclusive: Instant?,
    val format: ReportFormat,
    val includeChart: Boolean = true,
    val includeTable: Boolean = true,
    val includeNotes: Boolean = true,
    /** Kept for file/API compatibility; unvalidated body metrics are excluded. */
    val includeAdditionalMetrics: Boolean = false,
    val unit: WeightUnit = WeightUnit.KILOGRAM
)

data class ReportData(
    val profile: Profile,
    val measurements: List<WeightMeasurement>,
    val statistics: WeightStatistics?,
    val generatedAt: Instant,
    val startInclusive: Instant?,
    val endExclusive: Instant?
)
