
package com.example.acameet.data.repository

import com.example.acameet.data.local.entity.Notificacion
import kotlinx.coroutines.flow.Flow

interface NotificacionRepository {

    // OBTENER NOTIFICACIONES DEL USUARIO
    fun obtenerNotificaciones(
        usuarioId: Int
    ): Flow<List<Notificacion>>

    // CONTAR NOTIFICACIONES SIN LEER
    fun contarNoLeidas(
        usuarioId: Int
    ): Flow<Int>

    // GUARDAR UNA NOTIFICACIÓN
    suspend fun insertarNotificacion(
        notificacion: Notificacion
    ): Long

    // MARCAR UNA NOTIFICACIÓN COMO LEÍDA
    suspend fun marcarComoLeida(
        notificacionId: Int,
        usuarioId: Int
    )

    // MARCAR TODAS COMO LEÍDAS
    suspend fun marcarTodasComoLeidas(
        usuarioId: Int
    )

    // ELIMINAR UNA NOTIFICACIÓN
    suspend fun eliminarNotificacion(
        notificacionId: Int,
        usuarioId: Int
    )

    // ELIMINAR TODAS LAS NOTIFICACIONES
    suspend fun eliminarTodas(
        usuarioId: Int
    )
}
