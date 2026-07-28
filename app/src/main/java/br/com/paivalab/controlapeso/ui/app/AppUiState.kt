package br.com.paivalab.controlapeso.ui.app

import br.com.paivalab.controlapeso.data.preferences.AppPreferences
import br.com.paivalab.controlapeso.domain.model.Profile

data class AppUiState(
    val isLoading: Boolean = true,
    val preferences: AppPreferences = AppPreferences(),
    val activeProfile: Profile? = null
)
