package br.com.paivalab.controlapeso.data.preferences

import br.com.paivalab.controlapeso.domain.model.WeightUnit
import java.time.Instant

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class VisualEffects {
    FULL,
    REDUCED
}

enum class HistoryPeriod {
    DAYS_7,
    DAYS_30,
    MONTHS_3,
    MONTHS_6,
    YEAR_1,
    ALL
}

enum class HistoryGrouping {
    NONE,
    DAY,
    MONTH
}

enum class ChartSize {
    COMPACT,
    COMFORTABLE,
    LARGE
}

enum class LocalBackupFrequency(val intervalDays: Long) {
    OFF(0),
    DAILY(1),
    WEEKLY(7),
    MONTHLY(30)
}

data class AppPreferences(
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColors: Boolean = true,
    val visualEffects: VisualEffects = VisualEffects.FULL,
    val defaultWeightUnit: WeightUnit = WeightUnit.KILOGRAM,
    val autoSaveStableMeasurement: Boolean = false,
    val confirmBeforeSaving: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val defaultHistoryPeriod: HistoryPeriod = HistoryPeriod.DAYS_30,
    val movingAverageEnabled: Boolean = true,
    val historyGrouping: HistoryGrouping = HistoryGrouping.NONE,
    val chartSize: ChartSize = ChartSize.COMFORTABLE,
    val highContrast: Boolean = false,
    val detailedBleLogs: Boolean = false,
    val confirmedBleUnit: WeightUnit? = null,
    val pendingGoalTargetKg: Double? = null,
    val remindersEnabled: Boolean = false,
    val reminderDaysMask: Int = 0b1111111,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    /** Cached public release metadata used with the GitHub ETag; not user data. */
    val releaseUpdateEtag: String? = null,
    val releaseUpdateCachedRelease: String? = null,
    /** Last local confirmation that a Health Connect history action finished. */
    val healthLastSyncAt: Instant? = null,
    val localBackupFrequency: LocalBackupFrequency = LocalBackupFrequency.OFF,
    val lastLocalBackupAt: Instant? = null
)
