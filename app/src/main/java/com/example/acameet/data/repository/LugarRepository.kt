
package com.example.acameet.data.repository

import com.example.acameet.data.local.entity.Lugar
import kotlinx.coroutines.flow.Flow

interface LugarRepository {

    // OBTENER LOS LUGARES DEL USUARIO ACTIVO
    fun obtenerLugares(
        usuarioId: Int
    ): Flow<List<Lugar>>

    // REGISTRAR UN LUGAR
    suspend fun insertarLugar(
        lugar: Lugar
    )

    // BUSCAR UN LUGAR POR ID Y USUARIO
    suspend fun obtenerLugarPorId(
        id: Int,
        usuarioId: Int
    ): Lugar?

    // BUSCAR UN LUGAR POR NOMBRE Y USUARIO
    suspend fun obtenerLugarPorNombre(
        nombre: String,
        usuarioId: Int
    ): Lugar?

    // ACTUALIZAR UN LUGAR DEL USUARIO
    suspend fun actualizarLugar(
        lugar: Lugar,
        usuarioId: Int
    ): Int

    // ELIMINAR UN LUGAR DEL USUARIO
    suspend fun eliminarLugar(
        lugar: Lugar,
        usuarioId: Int
    ): Int
}
