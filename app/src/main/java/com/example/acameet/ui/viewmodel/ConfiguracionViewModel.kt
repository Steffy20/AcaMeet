
package com.example.acameet.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Usuario
import com.example.acameet.data.repository.UsuarioRepository
import com.example.acameet.data.repository.UserPreferences
import com.example.acameet.data.repository.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ESTADO DE LA PANTALLA DE CONFIGURACIÓN
data class ConfiguracionUiState(
    val preferences: UserPreferences = UserPreferences(),
    val isLoading: Boolean = false
)

// VIEWMODEL DE CONFIGURACIÓN
class ConfiguracionViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // ESTADO REACTIVO CON STATEFLOW
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

    // RECORDAR SESIÓN
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // No se modifica la interfaz si falla DataStore.
            }
        }
    }

    // CAMBIAR TEMA
    fun onTemaOscuroChanged(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveTemaOscuro(enabled)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Se conserva el último valor guardado.
            }
        }
    }

    // ACTIVAR O DESACTIVAR NOTIFICACIONES
    fun onNotificacionesChanged(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveNotificaciones(enabled)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Se conserva el último valor guardado.
            }
        }
    }

    // CAMBIAR IDIOMA
    fun onIdiomaChanged(nuevoIdioma: String) {
        viewModelScope.launch {
            try {
                preferencesRepository.saveIdioma(nuevoIdioma)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Se conserva el último valor guardado.
            }
        }
    }

    // CARGAR DATOS DEL USUARIO
    fun cargarUsuario(
        usuarioId: Int,
        onResultado: (Usuario?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val usuario =
                    usuarioRepository.obtenerUsuarioPorId(
                        usuarioId
                    )

                onResultado(usuario)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onResultado(null)
            }
        }
    }

    // ACTUALIZAR PERFIL
    fun actualizarPerfil(
        usuarioId: Int,
        nombre: String,
        correo: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        val nombreLimpio = nombre.trim()
            .replace(Regex("\\s+"), " ")

        val correoLimpio = correo.trim().lowercase()

        // VALIDAR NOMBRE
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

        // VALIDAR CORREO
        if (
            !Patterns.EMAIL_ADDRESS
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
                    usuarioRepository.obtenerUsuarioPorId(
                        usuarioId
                    )

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

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onResultado(
                    false,
                    "No se pudo actualizar el perfil"
                )
            }
        }
    }

    // CAMBIAR CONTRASEÑA
    fun cambiarContrasena(
        usuarioId: Int,
        actual: String,
        nueva: String,
        confirmar: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        // VALIDAR CONTRASEÑA ACTUAL
        if (actual.isBlank()) {
            onResultado(
                false,
                "Ingresa tu contraseña actual"
            )
            return
        }

        // VALIDAR NUEVA CONTRASEÑA
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

        // CONFIRMAR NUEVA CONTRASEÑA
        if (nueva != confirmar) {
            onResultado(
                false,
                "Las contraseñas no coinciden"
            )
            return
        }

        // EVITAR REPETIR CONTRASEÑA
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
                    usuarioRepository.obtenerUsuarioPorId(
                        usuarioId
                    )

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

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onResultado(
                    false,
                    "No se pudo actualizar la contraseña"
                )
            }
        }
    }
}

// FÁBRICA MANUAL DEL VIEWMODEL
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
                preferencesRepository = preferencesRepository,
                usuarioRepository = usuarioRepository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
