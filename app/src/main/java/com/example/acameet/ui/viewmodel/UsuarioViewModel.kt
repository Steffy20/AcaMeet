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

        // Validar campos vacíos
        if (
            nombre.isBlank() ||
            correo.isBlank() ||
            contrasena.isBlank() ||
            confirmarContrasena.isBlank()
        ) {
            onResultado(false, "Todos los campos son obligatorios")
            return
        }

        // Validar que las contraseñas coincidan
        if (contrasena != confirmarContrasena) {
            onResultado(false, "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {

            // Comprobar si el correo ya está registrado
            val usuarioExistente =
                repository.obtenerUsuarioPorCorreo(correo)

            if (usuarioExistente != null) {

                onResultado(
                    false,
                    "El correo ya está registrado"
                )

            } else {

                val nuevoUsuario = Usuario(
                    nombre = nombre,
                    correo = correo,
                    contrasena = contrasena
                )

                repository.registrarUsuario(nuevoUsuario)

                onResultado(
                    true,
                    "Cuenta creada correctamente"
                )
            }
        }
    }
}