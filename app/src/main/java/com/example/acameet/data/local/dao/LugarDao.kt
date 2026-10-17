
package com.example.acameet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.acameet.data.local.entity.Lugar
import kotlinx.coroutines.flow.Flow

@Dao
interface LugarDao {

    // Registrar un lugar
    @Insert
    suspend fun insertarLugar(lugar: Lugar)

    // Obtener todos los lugares
    @Query("SELECT * FROM lugares ORDER BY nombre ASC")
    fun obtenerLugares(): Flow<List<Lugar>>

    // Buscar un lugar por ID
    @Query("SELECT * FROM lugares WHERE id = :id LIMIT 1")
    suspend fun obtenerLugarPorId(id: Int): Lugar?

    // Actualizar un lugar
    @Update
    suspend fun actualizarLugar(lugar: Lugar)

    // Eliminar un lugar
    @Delete
    suspend fun eliminarLugar(lugar: Lugar)

    // Comprobar si ya existe un lugar con ese nombre
    @Query(
        "SELECT * FROM lugares " +
                "WHERE LOWER(nombre) = LOWER(:nombre) LIMIT 1"
    )
    suspend fun obtenerLugarPorNombre(
        nombre: String
    ): Lugar?
}
