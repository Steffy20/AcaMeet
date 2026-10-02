package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.UsuarioDao
import com.example.acameet.data.local.entity.Usuario

class UsuarioRepository(
    private val usuarioDao: UsuarioDao
) {

    // Registrar un nuevo usuario
    suspend fun registrarUsuario(usuario: Usuario) {
        usuarioDao.insertarUsuario(usuario)
    }

    // Buscar si un correo ya está registrado
    suspend fun obtenerUsuarioPorCorreo(correo: String): Usuario? {
        return usuarioDao.obtenerUsuarioPorCorreo(correo)
    }

    // Validar los datos para iniciar sesión
    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Usuario? {
        return usuarioDao.iniciarSesion(
            correo = correo,
            contrasena = contrasena
        )
    }
}