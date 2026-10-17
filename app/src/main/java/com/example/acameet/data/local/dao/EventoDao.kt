
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

    // Obtener todos los eventos
    @Query("SELECT * FROM eventos ORDER BY fecha ASC, hora ASC")
    fun obtenerEventos(): Flow<List<Evento>>

    // Obtener un evento por su ID
    @Query("SELECT * FROM eventos WHERE id = :id LIMIT 1")
    suspend fun obtenerEventoPorId(id: Int): Evento?

    // Registrar un nuevo evento
    @Insert
    suspend fun insertarEvento(evento: Evento)

    // Actualizar un evento existente
    @Update
    suspend fun actualizarEvento(evento: Evento)

    // Eliminar un evento
    @Delete
    suspend fun eliminarEvento(evento: Evento)

    // Buscar eventos por nombre o descripción
    @Query("""
        SELECT * FROM eventos
        WHERE nombre LIKE '%' || :texto || '%'
           OR descripcion LIKE '%' || :texto || '%'
        ORDER BY fecha ASC, hora ASC
    """)
    fun buscarEventos(texto: String): Flow<List<Evento>>

    // Filtrar eventos por categoría
    @Query("""
        SELECT * FROM eventos
        WHERE categoriaId = :categoriaId
        ORDER BY fecha ASC, hora ASC
    """)
    fun obtenerEventosPorCategoria(
        categoriaId: Int
    ): Flow<List<Evento>>
}
