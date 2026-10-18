
package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Usuario
import com.example.acameet.data.repository.UsuarioRepository
import kotlinx.coroutines.launch

class UsuarioViewModel(
    private val repository: UsuarioRepository
) : ViewModel() {

    fun registrarUsuario(
        nombre: String,
        correo: String,
        contrasena: String,
        confirmarContrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        val nombreLimpio = nombre.trim().replace(Regex("\\s+"), " ")
        val correoLimpio = correo.trim().lowercase()

        // VALIDACIÓN DEL NOMBRE
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

        // VALIDACIÓN DEL CORREO
        if (correoLimpio.isEmpty()) {
            onResultado(false, "Ingresa tu correo electrónico")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            onResultado(false, "Ingresa un correo electrónico válido")
            return
        }

        // VALIDACIÓN DE LA CONTRASEÑA
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

        // VALIDACIÓN DE CONFIRMACIÓN
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

    fun iniciarSesion(
        correo: String,
        contrasena: String,
        onResultado: (Boolean, String) -> Unit
    ) {
        val correoLimpio = correo.trim().lowercase()

        if (correoLimpio.isEmpty()) {
            onResultado(false, "Ingresa tu correo electrónico")
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoLimpio).matches()) {
            onResultado(false, "Ingresa un correo electrónico válido")
            return
        }

        if (contrasena.isEmpty()) {
            onResultado(false, "Ingresa tu contraseña")
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
                        true,
                        "Inicio de sesión correcto"
                    )
                } else {
                    onResultado(
                        false,
                        "Usuario o contraseña incorrectos"
                    )
                }
            } catch (e: Exception) {
                onResultado(
                    false,
                    "No se pudo iniciar sesión. Inténtalo nuevamente"
                )
            }
        }
    }
}
