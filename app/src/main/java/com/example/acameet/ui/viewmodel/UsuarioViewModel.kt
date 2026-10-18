
package com.example.acameet.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Usuario
import com.example.acameet.data.repository.UsuarioRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ESTADOS DE AUTENTICACIÓN
sealed interface UsuarioUiState {

    data object Idle : UsuarioUiState

    data object Loading : UsuarioUiState

    data class Success(
        val mensaje: String
    ) : UsuarioUiState

    data class Error(
        val mensaje: String
    ) : UsuarioUiState
}

// EVENTOS DE UNA SOLA EJECUCIÓN
sealed interface UsuarioEvento {

    data class RegistroExitoso(
        val mensaje: String
    ) : UsuarioEvento

    data class LoginExitoso(
        val usuarioId: Int
    ) : UsuarioEvento

    data class MostrarError(
        val mensaje: String
    ) : UsuarioEvento
}

class UsuarioViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<UsuarioUiState>(UsuarioUiState.Idle)

    val uiState: StateFlow<UsuarioUiState> =
        _uiState.asStateFlow()

    private val _eventos =
        MutableSharedFlow<UsuarioEvento>(
            extraBufferCapacity = 1
        )

    val eventos: SharedFlow<UsuarioEvento> =
        _eventos.asSharedFlow()

    // EVITAR OPERACIONES DUPLICADAS
    private var operacionEnCurso = false

    // RESTABLECER ESTADO
    fun limpiarEstado() {
        if (!operacionEnCurso) {
            _uiState.value = UsuarioUiState.Idle
        }
    }

    // REGISTRAR USUARIO
    fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        confirmarContrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        if (operacionEnCurso) return

        val nombreLimpio = nombre.trim()
            .replace(Regex("\\s+"), " ")

        val correoLimpio = correo.trim().lowercase()

        fun error(mensaje: String) {
            _uiState.value = UsuarioUiState.Error(mensaje)
            onResultado(false, mensaje)

            viewModelScope.launch {
                _eventos.emit(
                    UsuarioEvento.MostrarError(mensaje)
                )
            }
        }

        // VALIDAR NOMBRE
        if (nombreLimpio.isEmpty()) {
            error("Ingresa tus nombres y apellidos completos")
            return
        }

        val partesNombre = nombreLimpio.split(" ")

        if (partesNombre.size < 2) {
            error("Ingresa al menos un nombre y un apellido")
            return
        }

        if (!nombreLimpio.matches(
                Regex("^[\\p{L}]+(?:[ '-][\\p{L}]+)*$")
            )
        ) {
            error("El nombre solo debe contener letras")
            return
        }

        // VALIDAR CORREO
        if (correoLimpio.isEmpty()) {
            error("Ingresa tu correo electrónico")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(
                correoLimpio
            ).matches()
        ) {
            error("Ingresa un correo electrónico válido")
            return
        }

        // VALIDAR CONTRASEÑA
        if (contrasena.isEmpty()) {
            error("Ingresa una contraseña")
            return
        }

        if (contrasena.length < 8) {
            error("La contraseña debe tener al menos 8 caracteres")
            return
        }

        if (!contrasena.any { it.isUpperCase() }) {
            error(
                "La contraseña debe contener al menos una letra mayúscula"
            )
            return
        }

        if (!contrasena.any { it.isLowerCase() }) {
            error(
                "La contraseña debe contener al menos una letra minúscula"
            )
            return
        }

        if (!contrasena.any { it.isDigit() }) {
            error(
                "La contraseña debe contener al menos un número"
            )
            return
        }

        // CONFIRMAR CONTRASEÑA
        if (confirmarContrasena.isEmpty()) {
            error("Confirma tu contraseña")
            return
        }

        if (contrasena != confirmarContrasena) {
            error("Las contraseñas no coinciden")
            return
        }

        operacionEnCurso = true
        _uiState.value = UsuarioUiState.Loading

        viewModelScope.launch {
            try {
                val usuarioExistente =
                    repository.obtenerUsuarioPorCorreo(
                        correoLimpio
                    )

                if (usuarioExistente != null) {
                    val mensaje =
                        "Este correo electrónico ya está registrado"

                    _uiState.value =
                        UsuarioUiState.Error(mensaje)

                    onResultado(false, mensaje)

                    _eventos.emit(
                        UsuarioEvento.MostrarError(mensaje)
                    )

                    return@launch
                }

                val nuevoUsuario = Usuario(
                    nombre = nombreLimpio,
                    correo = correoLimpio,
                    contrasena = contrasena
                )

                repository.registrarUsuario(nuevoUsuario)

                val mensaje =
                    "¡Cuenta creada correctamente!"

                _uiState.value =
                    UsuarioUiState.Success(mensaje)

                onResultado(true, mensaje)

                _eventos.emit(
                    UsuarioEvento.RegistroExitoso(mensaje)
                )

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                val mensaje =
                    "No se pudo crear la cuenta. Inténtalo nuevamente"

                _uiState.value =
                    UsuarioUiState.Error(mensaje)

                onResultado(false, mensaje)

                _eventos.emit(
                    UsuarioEvento.MostrarError(mensaje)
                )

            } finally {
                operacionEnCurso = false
            }
        }
    }

    // INICIAR SESIÓN ORIGINAL
    fun iniciarSesion(
        correo: String,
        contrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        iniciarSesionConUsuario(
            correo = correo,
            contrasena = contrasena
        ) { usuario, mensaje ->
            onResultado(
                usuario != null,
                mensaje
            )
        }
    }

    // INICIAR SESIÓN Y DEVOLVER USUARIO AUTENTICADO
    fun iniciarSesionConUsuario(
        correo: String,
        contrasena: String,
        onResultado: (Usuario?, String) -> Unit
    ) {
        if (operacionEnCurso) return

        val correoLimpio = correo.trim().lowercase()

        fun error(mensaje: String) {
            _uiState.value = UsuarioUiState.Error(mensaje)
            onResultado(null, mensaje)

            viewModelScope.launch {
                _eventos.emit(
                    UsuarioEvento.MostrarError(mensaje)
                )
            }
        }

        // VALIDAR CORREO
        if (correoLimpio.isEmpty()) {
            error("Ingresa tu correo electrónico")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(
                correoLimpio
            ).matches()
        ) {
            error("Ingresa un correo electrónico válido")
            return
        }

        // VALIDAR CONTRASEÑA
        if (contrasena.isEmpty()) {
            error("Ingresa tu contraseña")
            return
        }

        operacionEnCurso = true
        _uiState.value = UsuarioUiState.Loading

        viewModelScope.launch {
            try {
                val usuario = repository.iniciarSesion(
                    correo = correoLimpio,
                    contrasena = contrasena
                )

                if (usuario != null) {
                    val mensaje =
                        "Inicio de sesión correcto"

                    _uiState.value =
                        UsuarioUiState.Success(mensaje)

                    onResultado(usuario, mensaje)

                    _eventos.emit(
                        UsuarioEvento.LoginExitoso(
                            usuarioId = usuario.id
                        )
                    )

                } else {
                    val mensaje =
                        "Usuario o contraseña incorrectos"

                    _uiState.value =
                        UsuarioUiState.Error(mensaje)

                    onResultado(null, mensaje)

                    _eventos.emit(
                        UsuarioEvento.MostrarError(mensaje)
                    )
                }

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                val mensaje =
                    "No se pudo iniciar sesión. Inténtalo nuevamente"

                _uiState.value =
                    UsuarioUiState.Error(mensaje)

                onResultado(null, mensaje)

                _eventos.emit(
                    UsuarioEvento.MostrarError(mensaje)
                )

            } finally {
                operacionEnCurso = false
            }
        }
    }
}
