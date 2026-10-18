
package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.EventoDao
import com.example.acameet.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

// IMPLEMENTACIÓN DEL REPOSITORIO DE EVENTOS
class EventoRepositoryImpl(
    private val eventoDao: EventoDao
) : EventoRepository {

    // OBTENER TODOS LOS EVENTOS
    override fun obtenerEventos(): Flow<List<Evento>> {
        return eventoDao.obtenerEventos()
    }

    // OBTENER UN EVENTO POR SU ID
    override suspend fun obtenerEventoPorId(
        id: Int
    ): Evento? {
        return eventoDao.obtenerEventoPorId(id)
    }

    // REGISTRAR UN NUEVO EVENTO
    override suspend fun insertarEvento(
        evento: Evento
    ) {
        eventoDao.insertarEvento(evento)
    }

    // ACTUALIZAR UN EVENTO EXISTENTE
    override suspend fun actualizarEvento(
        evento: Evento
    ) {
        eventoDao.actualizarEvento(evento)
    }

    // ELIMINAR UN EVENTO
    override suspend fun eliminarEvento(
        evento: Evento
    ) {
        eventoDao.eliminarEvento(evento)
    }

    // BUSCAR EVENTOS POR NOMBRE O DESCRIPCIÓN
    override fun buscarEventos(
        texto: String
    ): Flow<List<Evento>> {
        return eventoDao.buscarEventos(texto)
    }

    // FILTRAR EVENTOS POR CATEGORÍA
    override fun obtenerEventosPorCategoria(
        categoriaId: Int
    ): Flow<List<Evento>> {
        return eventoDao.obtenerEventosPorCategoria(categoriaId)
    }
}
