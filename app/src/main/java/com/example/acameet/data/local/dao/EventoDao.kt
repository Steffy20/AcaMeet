
package com.example.acameet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.acameet.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    // =====================================================
    // OBTENER EVENTOS DEL USUARIO ACTIVO
    // =====================================================

    @Query(
        """
        SELECT * FROM eventos
        WHERE usuarioId = :usuarioId
        ORDER BY fecha ASC, hora ASC
        """
    )
    fun obtenerEventos(
        usuarioId: Int
    ): Flow<List<Evento>>

    // =====================================================
    // OBTENER UN EVENTO POR ID Y USUARIO
    // =====================================================

    @Query(
        """
        SELECT * FROM eventos
        WHERE id = :id
          AND usuarioId = :usuarioId
        LIMIT 1
        """
    )
    suspend fun obtenerEventoPorId(
        id: Int,
        usuarioId: Int
    ): Evento?

    // =====================================================
    // REGISTRAR EVENTO Y DEVOLVER SU ID
    // =====================================================

    @Insert
    suspend fun insertarEvento(
        evento: Evento
    ): Long

    // =====================================================
    // ACTUALIZAR SOLO EVENTOS DEL USUARIO
    // =====================================================

    @Query(
        """
        UPDATE eventos
        SET nombre = :nombre,
            descripcion = :descripcion,
            fecha = :fecha,
            hora = :hora,
            categoriaId = :categoriaId,
            lugarId = :lugarId,
            imagenUrl = :imagenUrl
        WHERE id = :id
          AND usuarioId = :usuarioId
        """
    )
    suspend fun actualizarEvento(
        id: Int,
        usuarioId: Int,
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        categoriaId: Int,
        lugarId: Int,
        imagenUrl: String
    ): Int

    // =====================================================
    // ELIMINAR SOLO EVENTOS DEL USUARIO
    // =====================================================

    @Query(
        """
        DELETE FROM eventos
        WHERE id = :id
          AND usuarioId = :usuarioId
        """
    )
    suspend fun eliminarEvento(
        id: Int,
        usuarioId: Int
    ): Int

    // =====================================================
    // BUSCAR EVENTOS DEL USUARIO
    // =====================================================

    @Query(
        """
        SELECT * FROM eventos
        WHERE usuarioId = :usuarioId
          AND (
              nombre LIKE '%' || :texto || '%'
              OR descripcion LIKE '%' || :texto || '%'
          )
        ORDER BY fecha ASC, hora ASC
        """
    )
    fun buscarEventos(
        texto: String,
        usuarioId: Int
    ): Flow<List<Evento>>

    // =====================================================
    // FILTRAR EVENTOS POR CATEGORÍA Y USUARIO
    // =====================================================

    @Query(
        """
        SELECT * FROM eventos
        WHERE categoriaId = :categoriaId
          AND usuarioId = :usuarioId
        ORDER BY fecha ASC, hora ASC
        """
    )
    fun obtenerEventosPorCategoria(
        categoriaId: Int,
        usuarioId: Int
    ): Flow<List<Evento>>
}
