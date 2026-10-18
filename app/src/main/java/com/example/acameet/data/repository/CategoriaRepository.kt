
package com.example.acameet.data.repository

import com.example.acameet.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

interface CategoriaRepository {

    // OBTENER LAS CATEGORÍAS DE UN USUARIO
    fun obtenerCategorias(
        usuarioId: Int
    ): Flow<List<Categoria>>

    // REGISTRAR UNA CATEGORÍA
    suspend fun insertarCategoria(
        categoria: Categoria
    )

    // BUSCAR CATEGORÍA POR ID Y USUARIO
    suspend fun obtenerCategoriaPorId(
        id: Int,
        usuarioId: Int
    ): Categoria?

    // BUSCAR CATEGORÍA POR NOMBRE Y USUARIO
    suspend fun obtenerCategoriaPorNombre(
        nombre: String,
        usuarioId: Int
    ): Categoria?

    // ACTUALIZAR UNA CATEGORÍA DEL USUARIO
    suspend fun actualizarCategoria(
        categoria: Categoria,
        usuarioId: Int
    ): Int

    // ELIMINAR UNA CATEGORÍA DEL USUARIO
    suspend fun eliminarCategoria(
        categoria: Categoria,
        usuarioId: Int
    ): Int
}
