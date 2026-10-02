package com.example.acameet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.acameet.data.local.entity.Usuario

@Dao
interface UsuarioDao {

    // Registrar un nuevo usuario
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: Usuario)

    // Buscar un usuario por correo
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): Usuario?

    // Validar correo y contraseña para iniciar sesión
    @Query(
        "SELECT * FROM usuarios " +
                "WHERE correo = :correo AND contrasena = :contrasena LIMIT 1"
    )
    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Usuario?
}
