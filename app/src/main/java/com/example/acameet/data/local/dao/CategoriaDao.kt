package com.example.acameet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.acameet.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    // Registrar una categoría
    @Insert
    suspend fun insertarCategoria(categoria: Categoria)

    // Obtener todas las categorías
    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun obtenerCategorias(): Flow<List<Categoria>>

    // Buscar una categoría por ID
    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    suspend fun obtenerCategoriaPorId(id: Int): Categoria?

    // Actualizar una categoría
    @Update
    suspend fun actualizarCategoria(categoria: Categoria)

    // Eliminar una categoría
    @Delete
    suspend fun eliminarCategoria(categoria: Categoria)

    // Comprobar si ya existe una categoría con ese nombre
    @Query(
        "SELECT * FROM categorias " +
                "WHERE LOWER(nombre) = LOWER(:nombre) LIMIT 1"
    )
    suspend fun obtenerCategoriaPorNombre(
        nombre: String
    ): Categoria?
}