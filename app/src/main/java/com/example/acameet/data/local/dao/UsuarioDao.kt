
package com.example.acameet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.acameet.data.local.entity.Usuario

@Dao
interface UsuarioDao {

    // REGISTRAR USUARIO
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: Usuario)

    // BUSCAR USUARIO POR CORREO
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun obtenerUsuarioPorCorreo(correo: String): Usuario?

    // INICIAR SESIÓN
    @Query(
        "SELECT * FROM usuarios " +
                "WHERE correo = :correo AND contrasena = :contrasena LIMIT 1"
    )
    suspend fun iniciarSesion(
        correo: String,
        contrasena: String
    ): Usuario?

    // OBTENER USUARIO POR ID
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerUsuarioPorId(id: Int): Usuario?

    // ACTUALIZAR PERFIL O CONTRASEÑA
    @Update
    suspend fun actualizarUsuario(usuario: Usuario)
}
