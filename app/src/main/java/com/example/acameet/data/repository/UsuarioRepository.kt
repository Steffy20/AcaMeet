
package com.example.acameet.data.repository

import com.example.acameet.data.local.entity.Usuario

/**
 * Contrato del repositorio de usuarios.
 *
 * Define las operaciones disponibles para el ViewModel,
 * sin depender directamente de Room.
 */
interface UsuarioRepository {

    // REGISTRAR UN NUEVO USUARIO
    suspend fun registrarUsuario(usuario: Usuario)

    // BUSCAR USUARIO POR CORREO
    suspend fun obtenerUsuarioPorCorreo(
        correo: String
    ): Usuario?

    // INICIAR SESIÓN
    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Usuario?

    // OBTENER USUARIO POR ID
    suspend fun obtenerUsuarioPorId(
        id: Int
    ): Usuario?

    // ACTUALIZAR DATOS DEL USUARIO
    suspend fun actualizarUsuario(
        usuario: Usuario
    )
}
