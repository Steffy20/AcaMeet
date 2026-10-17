
package com.example.acameet.data.repository

import com.example.acameet.data.local.entity.Lugar
import kotlinx.coroutines.flow.Flow

interface LugarRepository {

    // Obtener todos los lugares
    fun obtenerLugares(): Flow<List<Lugar>>

    // Registrar un nuevo lugar
    suspend fun insertarLugar(lugar: Lugar)

    // Buscar un lugar por ID
    suspend fun obtenerLugarPorId(id: Int): Lugar?

    // Buscar un lugar por nombre
    suspend fun obtenerLugarPorNombre(nombre: String): Lugar?

    // Actualizar un lugar existente
    suspend fun actualizarLugar(lugar: Lugar)

    // Eliminar un lugar
    suspend fun eliminarLugar(lugar: Lugar)
}
