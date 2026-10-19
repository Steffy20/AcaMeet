
package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.NotificacionDao
import com.example.acameet.data.local.entity.Notificacion
import kotlinx.coroutines.flow.Flow

class NotificacionRepositoryImpl(
    private val dao: NotificacionDao
) : NotificacionRepository {

    // OBTENER NOTIFICACIONES
    override fun obtenerNotificaciones(
        usuarioId: Int
    ): Flow<List<Notificacion>> {
        return dao.obtenerNotificaciones(usuarioId)
    }

    // CONTAR NOTIFICACIONES SIN LEER
    override fun contarNoLeidas(
        usuarioId: Int
    ): Flow<Int> {
        return dao.contarNoLeidas(usuarioId)
    }

    // GUARDAR NOTIFICACIÓN
    override suspend fun insertarNotificacion(
        notificacion: Notificacion
    ): Long {
        require(notificacion.usuarioId > 0) {
            "Debes iniciar sesión para guardar notificaciones"
        }

        return dao.insertarNotificacion(notificacion)
    }

    // MARCAR UNA NOTIFICACIÓN COMO LEÍDA
    override suspend fun marcarComoLeida(
        notificacionId: Int,
        usuarioId: Int
    ) {
        dao.marcarComoLeida(
            notificacionId = notificacionId,
            usuarioId = usuarioId
        )
    }

    // MARCAR TODAS COMO LEÍDAS
    override suspend fun marcarTodasComoLeidas(
        usuarioId: Int
    ) {
        dao.marcarTodasComoLeidas(usuarioId)
    }

    // ELIMINAR UNA NOTIFICACIÓN
    override suspend fun eliminarNotificacion(
        notificacionId: Int,
        usuarioId: Int
    ) {
        dao.eliminarNotificacion(
            notificacionId = notificacionId,
            usuarioId = usuarioId
        )
    }

    // ELIMINAR TODAS LAS NOTIFICACIONES
    override suspend fun eliminarTodas(
        usuarioId: Int
    ) {
        dao.eliminarTodas(usuarioId)
    }
}
