
package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.EventoDao
import com.example.acameet.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

class EventoRepositoryImpl(
    private val eventoDao: EventoDao
) : EventoRepository {

    override fun obtenerEventos(): Flow<List<Evento>> {
        return eventoDao.obtenerEventos()
    }

    override suspend fun obtenerEventoPorId(
        id: Int
    ): Evento? {
        return eventoDao.obtenerEventoPorId(id)
    }

    override suspend fun insertarEvento(
        evento: Evento
    ) {
        eventoDao.insertarEvento(evento)
    }

    override suspend fun actualizarEvento(
        evento: Evento
    ) {
        eventoDao.actualizarEvento(evento)
    }

    override suspend fun eliminarEvento(
        evento: Evento
    ) {
        eventoDao.eliminarEvento(evento)
    }

    override fun buscarEventos(
        texto: String
    ): Flow<List<Evento>> {
        return eventoDao.buscarEventos(texto)
    }

    override fun obtenerEventosPorCategoria(
        categoriaId: Int
    ): Flow<List<Evento>> {
        return eventoDao.obtenerEventosPorCategoria(categoriaId)
    }
}
