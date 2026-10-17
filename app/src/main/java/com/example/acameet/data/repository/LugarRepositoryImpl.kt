
package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.LugarDao
import com.example.acameet.data.local.entity.Lugar
import kotlinx.coroutines.flow.Flow

class LugarRepositoryImpl(
    private val lugarDao: LugarDao
) : LugarRepository {

    override fun obtenerLugares(): Flow<List<Lugar>> {
        return lugarDao.obtenerLugares()
    }

    override suspend fun insertarLugar(lugar: Lugar) {
        lugarDao.insertarLugar(lugar)
    }

    override suspend fun obtenerLugarPorId(id: Int): Lugar? {
        return lugarDao.obtenerLugarPorId(id)
    }

    override suspend fun obtenerLugarPorNombre(nombre: String): Lugar? {
        return lugarDao.obtenerLugarPorNombre(nombre)
    }

    override suspend fun actualizarLugar(lugar: Lugar) {
        lugarDao.actualizarLugar(lugar)
    }

    override suspend fun eliminarLugar(lugar: Lugar) {
        lugarDao.eliminarLugar(lugar)
    }
}
