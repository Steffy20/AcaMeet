package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.repository.UserPreferences
import com.example.acameet.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConfiguracionUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false
)

class ConfiguracionViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<ConfiguracionUiState> = preferencesRepository.userPreferencesFlow
        .map { preferences -> ConfiguracionUiState(preferences = preferences) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConfiguracionUiState(isLoading = true)
        )

    fun onRecordarSesionChanged(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.saveRecordarSesion(enabled) }
    }

    fun onTemaOscuroChanged(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.saveTemaOscuro(enabled) }
    }

    fun onNotificacionesChanged(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.saveNotificaciones(enabled) }
    }

    fun onIdiomaChanged(nuevoIdioma: String) {
        viewModelScope.launch { preferencesRepository.saveIdioma(nuevoIdioma) }
    }
}

class ConfiguracionViewModelFactory(
    private val repository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfiguracionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfiguracionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}