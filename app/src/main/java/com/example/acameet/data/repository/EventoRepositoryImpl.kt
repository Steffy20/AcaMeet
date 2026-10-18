
package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.EventoDao
import com.example.acameet.data.local.dao.CategoriaDao
import com.example.acameet.data.local.dao.LugarDao
import com.example.acameet.data.local.entity.Evento
import kotlinx.coroutines.flow.Flow

class EventoRepositoryImpl(
    private val eventoDao: EventoDao,
    private val categoriaDao: CategoriaDao,
    private val lugarDao: LugarDao
) : EventoRepository {

    // OBTENER EVENTOS DEL USUARIO
    override fun obtenerEventos(
        usuarioId: Int
    ): Flow<List<Evento>> {
        return eventoDao.obtenerEventos(usuarioId)
    }

    // OBTENER EVENTO POR ID
    override suspend fun obtenerEventoPorId(
        id: Int,
        usuarioId: Int
    ): Evento? {
        return eventoDao.obtenerEventoPorId(id, usuarioId)
    }

    // VALIDAR CATEGORÍA Y LUGAR DEL USUARIO
    private suspend fun validarReferencias(
        evento: Evento,
        usuarioId: Int
    ) {
        require(usuarioId > 0) {
            "Debes iniciar sesión para administrar eventos"
        }

        val categoria = categoriaDao.obtenerCategoriaPorId(
            evento.categoriaId,
            usuarioId
        )

        require(categoria != null) {
            "La categoría seleccionada no pertenece a tu cuenta"
        }

        val lugar = lugarDao.obtenerLugarPorId(
            evento.lugarId,
            usuarioId
        )

        require(lugar != null) {
            "El lugar seleccionado no pertenece a tu cuenta"
        }
    }

    // REGISTRAR EVENTO
    override suspend fun insertarEvento(
        evento: Evento
    ) {
        validarReferencias(
            evento = evento,
            usuarioId = evento.usuarioId
        )

        eventoDao.insertarEvento(evento)
    }

    // ACTUALIZAR EVENTO
    override suspend fun actualizarEvento(
        evento: Evento,
        usuarioId: Int
    ): Int {
        validarReferencias(
            evento = evento,
            usuarioId = usuarioId
        )

        return eventoDao.actualizarEvento(
            id = evento.id,
            usuarioId = usuarioId,
            nombre = evento.nombre,
            descripcion = evento.descripcion,
            fecha = evento.fecha,
            hora = evento.hora,
            categoriaId = evento.categoriaId,
            lugarId = evento.lugarId,
            imagenUrl = evento.imagenUrl
        )
    }

    // ELIMINAR EVENTO
    override suspend fun eliminarEvento(
        evento: Evento,
        usuarioId: Int
    ): Int {
        require(usuarioId > 0) {
            "Debes iniciar sesión para eliminar eventos"
        }

        return eventoDao.eliminarEvento(
            id = evento.id,
            usuarioId = usuarioId
        )
    }

    // BUSCAR EVENTOS DEL USUARIO
    override fun buscarEventos(
        texto: String,
        usuarioId: Int
    ): Flow<List<Evento>> {
        return eventoDao.buscarEventos(
            texto = texto,
            usuarioId = usuarioId
        )
    }

    // FILTRAR EVENTOS POR CATEGORÍA
    override fun obtenerEventosPorCategoria(
        categoriaId: Int,
        usuarioId: Int
    ): Flow<List<Evento>> {
        return eventoDao.obtenerEventosPorCategoria(
            categoriaId = categoriaId,
            usuarioId = usuarioId
        )
    }
}
