package br.com.paivalab.controlapeso.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.paivalab.controlapeso.app.AppContainer
import br.com.paivalab.controlapeso.data.preferences.AppPreferencesRepository
import br.com.paivalab.controlapeso.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AppViewModel(
    preferencesRepository: AppPreferencesRepository,
    profileRepository: ProfileRepository
) : ViewModel() {
    val uiState = combine(
        preferencesRepository.preferences,
        profileRepository.observeActive()
    ) { preferences, activeProfile ->
        AppUiState(
            isLoading = false,
            preferences = preferences,
            activeProfile = activeProfile
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppUiState()
    )

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AppViewModel(
                preferencesRepository = container.preferencesRepository,
                profileRepository = container.profileRepository
            ) as T
    }
}
