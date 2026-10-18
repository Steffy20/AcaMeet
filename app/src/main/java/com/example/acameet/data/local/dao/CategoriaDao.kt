
package com.example.acameet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.acameet.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    // REGISTRAR UNA CATEGORÍA
    @Insert
    suspend fun insertarCategoria(
        categoria: Categoria
    )

    // OBTENER CATEGORÍAS DEL USUARIO ACTIVO
    @Query(
        """
        SELECT * FROM categorias
        WHERE usuarioId = :usuarioId
        ORDER BY nombre ASC
        """
    )
    fun obtenerCategorias(
        usuarioId: Int
    ): Flow<List<Categoria>>

    // BUSCAR CATEGORÍA POR ID Y USUARIO
    @Query(
        """
        SELECT * FROM categorias
        WHERE id = :id
          AND usuarioId = :usuarioId
        LIMIT 1
        """
    )
    suspend fun obtenerCategoriaPorId(
        id: Int,
        usuarioId: Int
    ): Categoria?

    // BUSCAR CATEGORÍA POR NOMBRE Y USUARIO
    @Query(
        """
        SELECT * FROM categorias
        WHERE LOWER(nombre) = LOWER(:nombre)
          AND usuarioId = :usuarioId
        LIMIT 1
        """
    )
    suspend fun obtenerCategoriaPorNombre(
        nombre: String,
        usuarioId: Int
    ): Categoria?

    // ACTUALIZAR CATEGORÍA DEL USUARIO
    @Query(
        """
        UPDATE categorias
        SET nombre = :nombre
        WHERE id = :id
          AND usuarioId = :usuarioId
        """
    )
    suspend fun actualizarCategoria(
        id: Int,
        usuarioId: Int,
        nombre: String
    ): Int

    // ELIMINAR CATEGORÍA DEL USUARIO
    @Query(
        """
        DELETE FROM categorias
        WHERE id = :id
          AND usuarioId = :usuarioId
        """
    )
    suspend fun eliminarCategoria(
        id: Int,
        usuarioId: Int
    ): Int
}
