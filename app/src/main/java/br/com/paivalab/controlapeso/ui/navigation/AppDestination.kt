package br.com.paivalab.controlapeso.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import br.com.paivalab.controlapeso.R

sealed class AppDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector? = null
) {
    data object Dashboard : AppDestination("dashboard", R.string.nav_dashboard, Icons.Filled.Home)
    data object History : AppDestination(
        "history",
        R.string.nav_history,
        Icons.AutoMirrored.Filled.List
    )
    data object Measure : AppDestination(
        "measure",
        R.string.nav_measure,
        Icons.Filled.AddCircle
    )
    data object Reports : AppDestination(
        "reports",
        R.string.nav_reports,
        Icons.Filled.Info
    )
    data object Settings : AppDestination(
        "settings",
        R.string.nav_settings,
        Icons.Filled.Settings
    )
    data object Diagnostic : AppDestination("diagnostic", R.string.bluetooth_diagnostic)
    data object Profiles : AppDestination("profiles", R.string.profiles_title)
    data object Goals : AppDestination("goals", R.string.goals_title)
    data object LiveMeasurement : AppDestination("measure/live", R.string.live_measurement_title)
    data object ManualMeasurement : AppDestination(
        "measure/manual",
        R.string.manual_measurement_title
    )
    data object DemoMeasurement : AppDestination(
        "debug/demo-measurement",
        R.string.demo_measurement
    )
    data object Devices : AppDestination("settings/devices", R.string.devices_title)
    data object Privacy : AppDestination("settings/privacy", R.string.privacy_title)
    data object About : AppDestination("settings/about", R.string.about_title)

    data object EditMeasurement : AppDestination(
        "measure/edit/{measurementId}",
        R.string.edit_measurement_title
    ) {
        fun route(measurementId: String) = "measure/edit/$measurementId"
    }

    data object MeasurementDetail : AppDestination(
        "measurement/{measurementId}",
        R.string.measurement_detail_title
    ) {
        fun route(measurementId: String) = "measurement/$measurementId"
    }
}

val primaryDestinations = listOf(
    AppDestination.Dashboard,
    AppDestination.History,
    AppDestination.Measure,
    AppDestination.Reports,
    AppDestination.Settings
)

val internalNotificationDestinations = setOf(
    AppDestination.LiveMeasurement.route,
    AppDestination.ManualMeasurement.route
)
