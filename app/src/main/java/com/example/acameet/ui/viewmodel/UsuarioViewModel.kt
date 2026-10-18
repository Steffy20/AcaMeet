
package com.example.acameet.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Usuario
import com.example.acameet.data.repository.UsuarioRepository
import kotlinx.coroutines.launch

class UsuarioViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    // REGISTRAR USUARIO
    fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        confirmarContrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        val nombreLimpio = nombre.trim().replace(Regex("\\s+"), " ")
        val correoLimpio = correo.trim().lowercase()

        // VALIDAR NOMBRE
        if (nombreLimpio.isEmpty()) {
            onResultado(false, "Ingresa tus nombres y apellidos completos")
            return
        }

        val partesNombre = nombreLimpio.split(" ")

        if (partesNombre.size < 2) {
            onResultado(false, "Ingresa al menos un nombre y un apellido")
            return
        }

        if (!nombreLimpio.matches(Regex("^[\\p{L}]+(?:[ '-][\\p{L}]+)*$"))) {
            onResultado(false, "El nombre solo debe contener letras")
            return
        }

        // VALIDAR CORREO
        if (correoLimpio.isEmpty()) {
            onResultado(false, "Ingresa tu correo electrónico")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            onResultado(false, "Ingresa un correo electrónico válido")
            return
        }

        // VALIDAR CONTRASEÑA
        if (contrasena.isEmpty()) {
            onResultado(false, "Ingresa una contraseña")
            return
        }

        if (contrasena.length < 8) {
            onResultado(false, "La contraseña debe tener al menos 8 caracteres")
            return
        }

        if (!contrasena.any { it.isUpperCase() }) {
            onResultado(false, "La contraseña debe contener al menos una letra mayúscula")
            return
        }

        if (!contrasena.any { it.isLowerCase() }) {
            onResultado(false, "La contraseña debe contener al menos una letra minúscula")
            return
        }

        if (!contrasena.any { it.isDigit() }) {
            onResultado(false, "La contraseña debe contener al menos un número")
            return
        }

        // CONFIRMAR CONTRASEÑA
        if (confirmarContrasena.isEmpty()) {
            onResultado(false, "Confirma tu contraseña")
            return
        }

        if (contrasena != confirmarContrasena) {
            onResultado(false, "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            try {
                val usuarioExistente =
                    repository.obtenerUsuarioPorCorreo(correoLimpio)

                if (usuarioExistente != null) {
                    onResultado(
                        false,
                        "Este correo electrónico ya está registrado"
                    )
                    return@launch
                }

                val nuevoUsuario = Usuario(
                    nombre = nombreLimpio,
                    correo = correoLimpio,
                    contrasena = contrasena
                )

                repository.registrarUsuario(nuevoUsuario)

                onResultado(
                    true,
                    "¡Cuenta creada correctamente!"
                )

            } catch (e: Exception) {
                onResultado(
                    false,
                    "No se pudo crear la cuenta. Inténtalo nuevamente"
                )
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

    // INICIAR SESIÓN Y DEVOLVER EL USUARIO AUTENTICADO
    fun iniciarSesionConUsuario(
        correo: String,
        contrasena: String,
        onResultado: (Usuario?, String) -> Unit
    ) {
        val correoLimpio = correo.trim().lowercase()

        if (correoLimpio.isEmpty()) {
            onResultado(null, "Ingresa tu correo electrónico")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            onResultado(null, "Ingresa un correo electrónico válido")
            return
        }

        if (contrasena.isEmpty()) {
            onResultado(null, "Ingresa tu contraseña")
            return
        }

        viewModelScope.launch {
            try {
                val usuario = repository.iniciarSesion(
                    correo = correoLimpio,
                    contrasena = contrasena
                )

                if (usuario != null) {
                    onResultado(
                        usuario,
                        "Inicio de sesión correcto"
                    )
                } else {
                    onResultado(
                        null,
                        "Usuario o contraseña incorrectos"
                    )
                }

            } catch (e: Exception) {
                onResultado(
                    null,
                    "No se pudo iniciar sesión. Inténtalo nuevamente"
                )
            }
        }
    }
}
