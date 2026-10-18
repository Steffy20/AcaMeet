
package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.UsuarioDao
import com.example.acameet.data.local.entity.Usuario

/**
 * Implementación del repositorio de usuarios.
 *
 * Utiliza UsuarioDao para realizar las operaciones
 * correspondientes en la base de datos Room.
 */
class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao
) : UsuarioRepository {

    // REGISTRAR UN NUEVO USUARIO
    override suspend fun registrarUsuario(
        usuario: Usuario
    ) {
        usuarioDao.insertarUsuario(usuario)
    }

    // BUSCAR USUARIO POR CORREO
    override suspend fun obtenerUsuarioPorCorreo(
        correo: String
    ): Usuario? {
        return usuarioDao.obtenerUsuarioPorCorreo(
            correo
        )
    }

    // INICIAR SESIÓN
    override suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Usuario? {
        return usuarioDao.iniciarSesion(
            correo = correo,
            contrasena = contrasena
        )
    }

    // OBTENER USUARIO POR ID
    override suspend fun obtenerUsuarioPorId(
        id: Int
    ): Usuario? {
        return usuarioDao.obtenerUsuarioPorId(
            id
        )
    }

    // ACTUALIZAR DATOS DEL USUARIO
    override suspend fun actualizarUsuario(
        usuario: Usuario
    ) {
        usuarioDao.actualizarUsuario(
            usuario
        )
    }
}
