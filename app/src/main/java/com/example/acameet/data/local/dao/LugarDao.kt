
package com.example.acameet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.acameet.data.local.entity.Lugar
import kotlinx.coroutines.flow.Flow

@Dao
interface LugarDao {

    // REGISTRAR UN LUGAR
    @Insert
    suspend fun insertarLugar(
        lugar: Lugar
    )

    // OBTENER LUGARES DEL USUARIO ACTIVO
    @Query(
        """
        SELECT * FROM lugares
        WHERE usuarioId = :usuarioId
        ORDER BY nombre ASC
        """
    )
    fun obtenerLugares(
        usuarioId: Int
    ): Flow<List<Lugar>>

    // BUSCAR LUGAR POR ID Y USUARIO
    @Query(
        """
        SELECT * FROM lugares
        WHERE id = :id
          AND usuarioId = :usuarioId
        LIMIT 1
        """
    )
    suspend fun obtenerLugarPorId(
        id: Int,
        usuarioId: Int
    ): Lugar?

    // BUSCAR LUGAR POR NOMBRE Y USUARIO
    @Query(
        """
        SELECT * FROM lugares
        WHERE LOWER(nombre) = LOWER(:nombre)
          AND usuarioId = :usuarioId
        LIMIT 1
        """
    )
    suspend fun obtenerLugarPorNombre(
        nombre: String,
        usuarioId: Int
    ): Lugar?

    // ACTUALIZAR LUGAR DEL USUARIO
    @Query(
        """
        UPDATE lugares
        SET nombre = :nombre
        WHERE id = :id
          AND usuarioId = :usuarioId
        """
    )
    suspend fun actualizarLugar(
        id: Int,
        usuarioId: Int,
        nombre: String
    ): Int

    // ELIMINAR LUGAR DEL USUARIO
    @Query(
        """
        DELETE FROM lugares
        WHERE id = :id
          AND usuarioId = :usuarioId
        """
    )
    suspend fun eliminarLugar(
        id: Int,
        usuarioId: Int
    ): Int
}
