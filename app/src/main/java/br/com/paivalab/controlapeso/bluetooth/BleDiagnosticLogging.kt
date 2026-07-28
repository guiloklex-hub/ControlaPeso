package br.com.paivalab.controlapeso.bluetooth

import br.com.paivalab.controlapeso.BuildConfig

object BleDiagnosticLogging {
    @Volatile
    private var userEnabled: Boolean = false

    val isDetailedLoggingEnabled: Boolean
        get() = BuildConfig.DEBUG && userEnabled

    fun updateUserPreference(enabled: Boolean) {
        userEnabled = enabled
    }
}
