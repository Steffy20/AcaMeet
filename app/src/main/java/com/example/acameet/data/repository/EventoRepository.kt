
package com.example.acameet.data.repository

import com.example.acameet.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

interface EventoRepository {

    fun obtenerEventos(): Flow<List<Evento>>

    suspend fun obtenerEventoPorId(id: Int): Evento?

    suspend fun insertarEvento(evento: Evento)

    suspend fun actualizarEvento(evento: Evento)

    suspend fun eliminarEvento(evento: Evento)

    fun buscarEventos(texto: String): Flow<List<Evento>>

    fun obtenerEventosPorCategoria(
        categoriaId: Int
    ): Flow<List<Evento>>
}
