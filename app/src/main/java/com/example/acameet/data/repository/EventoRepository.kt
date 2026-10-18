
package com.example.acameet.data.repository

import com.example.acameet.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

// INTERFAZ DEL REPOSITORIO DE EVENTOS
interface EventoRepository {

    // OBTENER TODOS LOS EVENTOS
    fun obtenerEventos(): Flow<List<Evento>>

    // OBTENER UN EVENTO POR SU ID
    suspend fun obtenerEventoPorId(
        id: Int
    ): Evento?

    // REGISTRAR UN NUEVO EVENTO
    suspend fun insertarEvento(
        evento: Evento
    )

    // ACTUALIZAR UN EVENTO EXISTENTE
    suspend fun actualizarEvento(
        evento: Evento
    )

    // ELIMINAR UN EVENTO
    suspend fun eliminarEvento(
        evento: Evento
    )

    // BUSCAR EVENTOS POR NOMBRE O DESCRIPCIÓN
    fun buscarEventos(
        texto: String
    ): Flow<List<Evento>>

    // FILTRAR EVENTOS POR CATEGORÍA
    fun obtenerEventosPorCategoria(
        categoriaId: Int
    ): Flow<List<Evento>>
}
