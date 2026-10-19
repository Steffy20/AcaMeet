
package com.example.acameet.data.repository

import com.example.acameet.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

interface EventoRepository {

    // OBTENER LOS EVENTOS DE UN USUARIO
    fun obtenerEventos(
        usuarioId: Int
    ): Flow<List<Evento>>

    // OBTENER UN EVENTO POR ID Y USUARIO
    suspend fun obtenerEventoPorId(
        id: Int,
        usuarioId: Int
    ): Evento?

    // REGISTRAR UN EVENTO Y DEVOLVER SU ID
    suspend fun insertarEvento(
        evento: Evento
    ): Long

    // ACTUALIZAR UN EVENTO DEL USUARIO
    suspend fun actualizarEvento(
        evento: Evento,
        usuarioId: Int
    ): Int

    // ELIMINAR UN EVENTO DEL USUARIO
    suspend fun eliminarEvento(
        evento: Evento,
        usuarioId: Int
    ): Int

    // BUSCAR EVENTOS DEL USUARIO
    fun buscarEventos(
        texto: String,
        usuarioId: Int
    ): Flow<List<Evento>>

    // FILTRAR EVENTOS POR CATEGORÍA Y USUARIO
    fun obtenerEventosPorCategoria(
        categoriaId: Int,
        usuarioId: Int
    ): Flow<List<Evento>>
}
