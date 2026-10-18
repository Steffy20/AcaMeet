
package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.UsuarioDao
import com.example.acameet.data.local.entity.Usuario

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {

    // REGISTRAR UN NUEVO USUARIO
    suspend fun registrarUsuario(usuario: Usuario) {
        usuarioDao.insertarUsuario(usuario)
    }

    // BUSCAR USUARIO POR CORREO
    suspend fun obtenerUsuarioPorCorreo(
        correo: String
    ): Usuario? {
        return usuarioDao.obtenerUsuarioPorCorreo(correo)
    }

    // INICIAR SESIÓN
    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Usuario? {
        return usuarioDao.iniciarSesion(
            correo = correo,
            contrasena = contrasena
        )
    }

    // OBTENER USUARIO POR ID
    suspend fun obtenerUsuarioPorId(
        id: Int
    ): Usuario? {
        return usuarioDao.obtenerUsuarioPorId(id)
    }

    // ACTUALIZAR DATOS DEL USUARIO
    suspend fun actualizarUsuario(
        usuario: Usuario
    ) {
        usuarioDao.actualizarUsuario(usuario)
    }
}
