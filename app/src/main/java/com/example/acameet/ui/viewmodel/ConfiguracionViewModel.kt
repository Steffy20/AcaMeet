
package com.example.acameet.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Usuario
import com.example.acameet.data.repository.UsuarioRepository
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
    private val preferencesRepository: UserPreferencesRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    val uiState: StateFlow<ConfiguracionUiState> =
        preferencesRepository.userPreferencesFlow
            .map { preferences ->
                ConfiguracionUiState(
                    preferences = preferences,
                    isLoading = false
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ConfiguracionUiState(
                    isLoading = true
                )
            )

    fun onRecordarSesionChanged(
        enabled: Boolean,
        usuarioId: Int?
    ) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveRecordarSesion(enabled)

                if (enabled && usuarioId != null) {
                    preferencesRepository.guardarUsuarioSesion(
                        usuarioId
                    )
                }
            } catch (e: Exception) {
                // Se conserva la preferencia anterior si falla DataStore.
            }
        }
    }

    fun onTemaOscuroChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveTemaOscuro(enabled)
        }
    }

    fun onNotificacionesChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.saveNotificaciones(enabled)
        }
    }

    fun onIdiomaChanged(nuevoIdioma: String) {
        viewModelScope.launch {
            preferencesRepository.saveIdioma(nuevoIdioma)
        }
    }

    fun cargarUsuario(
        usuarioId: Int,
        onResultado: (Usuario?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val usuario =
                    usuarioRepository.obtenerUsuarioPorId(usuarioId)

                onResultado(usuario)
            } catch (e: Exception) {
                onResultado(null)
            }
        }
    }

    fun actualizarPerfil(
        usuarioId: Int,
        nombre: String,
        correo: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        val nombreLimpio =
            nombre.trim().replace(Regex("\\s+"), " ")

        val correoLimpio =
            correo.trim().lowercase()

        if (
            nombreLimpio.split(" ").size < 2 ||
            !nombreLimpio.matches(
                Regex("^[\\p{L}]+(?:[ '-][\\p{L}]+)*$")
            )
        ) {
            onResultado(
                false,
                "Ingresa al menos un nombre y un apellido válidos"
            )
            return
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(correoLimpio)
                .matches()
        ) {
            onResultado(
                false,
                "Ingresa un correo electrónico válido"
            )
            return
        }

        viewModelScope.launch {
            try {
                val usuario =
                    usuarioRepository.obtenerUsuarioPorId(usuarioId)

                if (usuario == null) {
                    onResultado(
                        false,
                        "No se encontró el usuario"
                    )
                    return@launch
                }

                val usuarioExistente =
                    usuarioRepository.obtenerUsuarioPorCorreo(
                        correoLimpio
                    )

                if (
                    usuarioExistente != null &&
                    usuarioExistente.id != usuarioId
                ) {
                    onResultado(
                        false,
                        "Este correo ya está registrado"
                    )
                    return@launch
                }

                usuarioRepository.actualizarUsuario(
                    usuario.copy(
                        nombre = nombreLimpio,
                        correo = correoLimpio
                    )
                )

                onResultado(
                    true,
                    "Perfil actualizado correctamente"
                )
            } catch (e: Exception) {
                onResultado(
                    false,
                    "No se pudo actualizar el perfil"
                )
            }
        }
    }

    fun cambiarContrasena(
        usuarioId: Int,
        actual: String,
        nueva: String,
        confirmar: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        if (actual.isBlank()) {
            onResultado(
                false,
                "Ingresa tu contraseña actual"
            )
            return
        }

        if (
            nueva.length < 8 ||
            !nueva.any { it.isUpperCase() } ||
            !nueva.any { it.isLowerCase() } ||
            !nueva.any { it.isDigit() }
        ) {
            onResultado(
                false,
                "La nueva contraseña debe tener 8 caracteres, mayúscula, minúscula y número"
            )
            return
        }

        if (nueva != confirmar) {
            onResultado(
                false,
                "Las contraseñas no coinciden"
            )
            return
        }

        if (nueva == actual) {
            onResultado(
                false,
                "La nueva contraseña debe ser diferente de la actual"
            )
            return
        }

        viewModelScope.launch {
            try {
                val usuario =
                    usuarioRepository.obtenerUsuarioPorId(usuarioId)

                if (usuario == null) {
                    onResultado(
                        false,
                        "No se encontró el usuario"
                    )
                    return@launch
                }

                if (usuario.contrasena != actual) {
                    onResultado(
                        false,
                        "La contraseña actual es incorrecta"
                    )
                    return@launch
                }

                usuarioRepository.actualizarUsuario(
                    usuario.copy(
                        contrasena = nueva
                    )
                )

                onResultado(
                    true,
                    "Contraseña actualizada correctamente"
                )
            } catch (e: Exception) {
                onResultado(
                    false,
                    "No se pudo actualizar la contraseña"
                )
            }
        }
    }
}

class ConfiguracionViewModelFactory(
    private val preferencesRepository: UserPreferencesRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                ConfiguracionViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return ConfiguracionViewModel(
                preferencesRepository,
                usuarioRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}
