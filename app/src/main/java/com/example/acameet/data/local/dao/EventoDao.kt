
package com.example.acameet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import com.example.acameet.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    // OBTENER TODOS LOS EVENTOS
    // Los eventos se muestran ordenados por fecha y hora.
    @Query(
        """
        SELECT * FROM eventos
        ORDER BY fecha ASC, hora ASC
        """
    )
    fun obtenerEventos(): Flow<List<Evento>>

    // OBTENER UN EVENTO POR SU ID
    @Query(
        """
        SELECT * FROM eventos
        WHERE id = :id
        LIMIT 1
        """
    )
    suspend fun obtenerEventoPorId(
        id: Int
    ): Evento?

    // REGISTRAR UN NUEVO EVENTO
    @Insert
    suspend fun insertarEvento(
        evento: Evento
    )

    // ACTUALIZAR UN EVENTO EXISTENTE
    @Update
    suspend fun actualizarEvento(
        evento: Evento
    )

    // ELIMINAR UN EVENTO
    @Delete
    suspend fun eliminarEvento(
        evento: Evento
    )

    // BUSCAR EVENTOS POR NOMBRE O DESCRIPCIÓN
    @Query(
        """
        SELECT * FROM eventos
        WHERE nombre LIKE '%' || :texto || '%'
           OR descripcion LIKE '%' || :texto || '%'
        ORDER BY fecha ASC, hora ASC
        """
    )
    fun buscarEventos(
        texto: String
    ): Flow<List<Evento>>

    // FILTRAR EVENTOS POR CATEGORÍA
    @Query(
        """
        SELECT * FROM eventos
        WHERE categoriaId = :categoriaId
        ORDER BY fecha ASC, hora ASC
        """
    )
    fun obtenerEventosPorCategoria(
        categoriaId: Int
    ): Flow<List<Evento>>
}
