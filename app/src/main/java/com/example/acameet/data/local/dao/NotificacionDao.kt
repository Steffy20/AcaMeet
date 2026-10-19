
package com.example.acameet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.acameet.data.local.entity.Notificacion
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {

    // =====================================================
    // OBTENER NOTIFICACIONES DEL USUARIO
    // =====================================================

    @Query(
        """
        SELECT * FROM notificaciones
        WHERE usuarioId = :usuarioId
        ORDER BY fechaCreacion DESC, id DESC
        """
    )
    fun obtenerNotificaciones(
        usuarioId: Int
    ): Flow<List<Notificacion>>

    // =====================================================
    // CONTAR NOTIFICACIONES SIN LEER
    // =====================================================

    @Query(
        """
        SELECT COUNT(*) FROM notificaciones
        WHERE usuarioId = :usuarioId
          AND leida = 0
        """
    )
    fun contarNoLeidas(
        usuarioId: Int
    ): Flow<Int>

    // =====================================================
    // GUARDAR NOTIFICACIÓN
    // =====================================================

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarNotificacion(
        notificacion: Notificacion
    ): Long

    // =====================================================
    // MARCAR UNA NOTIFICACIÓN COMO LEÍDA
    // =====================================================

    @Query(
        """
        UPDATE notificaciones
        SET leida = 1
        WHERE id = :notificacionId
          AND usuarioId = :usuarioId
        """
    )
    suspend fun marcarComoLeida(
        notificacionId: Int,
        usuarioId: Int
    ): Int

    // =====================================================
    // MARCAR TODAS COMO LEÍDAS
    // =====================================================

    @Query(
        """
        UPDATE notificaciones
        SET leida = 1
        WHERE usuarioId = :usuarioId
          AND leida = 0
        """
    )
    suspend fun marcarTodasComoLeidas(
        usuarioId: Int
    ): Int

    // =====================================================
    // ELIMINAR UNA NOTIFICACIÓN
    // =====================================================

    @Query(
        """
        DELETE FROM notificaciones
        WHERE id = :notificacionId
          AND usuarioId = :usuarioId
        """
    )
    suspend fun eliminarNotificacion(
        notificacionId: Int,
        usuarioId: Int
    ): Int

    // =====================================================
    // ELIMINAR TODAS LAS NOTIFICACIONES DEL USUARIO
    // =====================================================

    @Query(
        """
        DELETE FROM notificaciones
        WHERE usuarioId = :usuarioId
        """
    )
    suspend fun eliminarTodas(
        usuarioId: Int
    ): Int
}
