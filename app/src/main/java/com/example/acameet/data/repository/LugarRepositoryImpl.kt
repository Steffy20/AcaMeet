
package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.LugarDao
import com.example.acameet.data.local.entity.Lugar
import kotlinx.coroutines.flow.Flow

class LugarRepositoryImpl(
    private val lugarDao: LugarDao
) : LugarRepository {

    // OBTENER LUGARES DEL USUARIO
    override fun obtenerLugares(
        usuarioId: Int
    ): Flow<List<Lugar>> {
        require(usuarioId > 0) {
            "Debes iniciar sesión para consultar lugares"
        }

        return lugarDao.obtenerLugares(usuarioId)
    }

    // REGISTRAR UN LUGAR
    override suspend fun insertarLugar(
        lugar: Lugar
    ) {
        require(lugar.usuarioId > 0) {
            "Debes iniciar sesión para registrar lugares"
        }

        lugarDao.insertarLugar(lugar)
    }

    // BUSCAR LUGAR POR ID
    override suspend fun obtenerLugarPorId(
        id: Int,
        usuarioId: Int
    ): Lugar? {
        require(usuarioId > 0) {
            "Debes iniciar sesión para consultar lugares"
        }

        return lugarDao.obtenerLugarPorId(
            id = id,
            usuarioId = usuarioId
        )
    }

    // BUSCAR LUGAR POR NOMBRE
    override suspend fun obtenerLugarPorNombre(
        nombre: String,
        usuarioId: Int
    ): Lugar? {
        require(usuarioId > 0) {
            "Debes iniciar sesión para consultar lugares"
        }

        return lugarDao.obtenerLugarPorNombre(
            nombre = nombre,
            usuarioId = usuarioId
        )
    }

    // ACTUALIZAR LUGAR
    override suspend fun actualizarLugar(
        lugar: Lugar,
        usuarioId: Int
    ): Int {
        require(usuarioId > 0) {
            "Debes iniciar sesión para actualizar lugares"
        }

        return lugarDao.actualizarLugar(
            id = lugar.id,
            usuarioId = usuarioId,
            nombre = lugar.nombre
        )
    }

    // ELIMINAR LUGAR
    override suspend fun eliminarLugar(
        lugar: Lugar,
        usuarioId: Int
    ): Int {
        require(usuarioId > 0) {
            "Debes iniciar sesión para eliminar lugares"
        }

        return lugarDao.eliminarLugar(
            id = lugar.id,
            usuarioId = usuarioId
        )
    }
}
