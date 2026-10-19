
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

    // =====================================================
    // OBTENER EVENTOS DEL USUARIO
    // =====================================================

    override fun obtenerEventos(
        usuarioId: Int
    ): Flow<List<Evento>> {

        return eventoDao.obtenerEventos(
            usuarioId = usuarioId
        )
    }

    // =====================================================
    // OBTENER EVENTO POR ID Y USUARIO
    // =====================================================

    override suspend fun obtenerEventoPorId(
        id: Int,
        usuarioId: Int
    ): Evento? {

        if (usuarioId <= 0 || id <= 0) {
            return null
        }

        return eventoDao.obtenerEventoPorId(
            id = id,
            usuarioId = usuarioId
        )
    }

    // =====================================================
    // VALIDAR CATEGORÍA Y LUGAR DEL USUARIO
    // =====================================================

    private suspend fun validarReferencias(
        evento: Evento,
        usuarioId: Int
    ) {

        require(usuarioId > 0) {
            "Debes iniciar sesión para administrar eventos"
        }

        require(evento.usuarioId == usuarioId) {
            "Este evento no pertenece a tu cuenta"
        }

        require(evento.categoriaId > 0) {
            "Selecciona una categoría válida"
        }

        require(evento.lugarId > 0) {
            "Selecciona un lugar válido"
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

    // =====================================================
    // REGISTRAR EVENTO
    // =====================================================

    override suspend fun insertarEvento(
        evento: Evento
    ): Long {

        validarReferencias(
            evento = evento,
            usuarioId = evento.usuarioId
        )

        require(evento.nombre.isNotBlank()) {
            "El nombre del evento es obligatorio"
        }

        require(evento.descripcion.isNotBlank()) {
            "La descripción del evento es obligatoria"
        }

        require(evento.fecha.isNotBlank()) {
            "La fecha del evento es obligatoria"
        }

        require(evento.hora.isNotBlank()) {
            "La hora del evento es obligatoria"
        }

        return eventoDao.insertarEvento(
            evento = evento
        )
    }

    // =====================================================
    // ACTUALIZAR EVENTO
    // =====================================================

    override suspend fun actualizarEvento(
        evento: Evento,
        usuarioId: Int
    ): Int {

        validarReferencias(
            evento = evento,
            usuarioId = usuarioId
        )

        require(evento.id > 0) {
            "El evento seleccionado no es válido"
        }

        require(evento.nombre.isNotBlank()) {
            "El nombre del evento es obligatorio"
        }

        require(evento.descripcion.isNotBlank()) {
            "La descripción del evento es obligatoria"
        }

        require(evento.fecha.isNotBlank()) {
            "La fecha del evento es obligatoria"
        }

        require(evento.hora.isNotBlank()) {
            "La hora del evento es obligatoria"
        }

        val eventoExistente = eventoDao.obtenerEventoPorId(
            id = evento.id,
            usuarioId = usuarioId
        )

        if (eventoExistente == null) {
            return 0
        }

        return eventoDao.actualizarEvento(
            id = evento.id,
            usuarioId = usuarioId,
            nombre = evento.nombre.trim(),
            descripcion = evento.descripcion.trim(),
            fecha = evento.fecha,
            hora = evento.hora,
            categoriaId = evento.categoriaId,
            lugarId = evento.lugarId,
            imagenUrl = evento.imagenUrl
        )
    }

    // =====================================================
    // ELIMINAR EVENTO
    // =====================================================

    override suspend fun eliminarEvento(
        evento: Evento,
        usuarioId: Int
    ): Int {

        require(usuarioId > 0) {
            "Debes iniciar sesión para eliminar eventos"
        }

        require(evento.id > 0) {
            "El evento seleccionado no es válido"
        }

        require(evento.usuarioId == usuarioId) {
            "No puedes eliminar eventos de otra cuenta"
        }

        val eventoExistente = eventoDao.obtenerEventoPorId(
            id = evento.id,
            usuarioId = usuarioId
        )

        if (eventoExistente == null) {
            return 0
        }

        return eventoDao.eliminarEvento(
            id = evento.id,
            usuarioId = usuarioId
        )
    }

    // =====================================================
    // BUSCAR EVENTOS DEL USUARIO
    // =====================================================

    override fun buscarEventos(
        texto: String,
        usuarioId: Int
    ): Flow<List<Evento>> {

        return eventoDao.buscarEventos(
            texto = texto.trim(),
            usuarioId = usuarioId
        )
    }

    // =====================================================
    // FILTRAR EVENTOS POR CATEGORÍA Y USUARIO
    // =====================================================

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
