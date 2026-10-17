
package com.example.acameet.data.repository

import com.example.acameet.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

interface CategoriaRepository {

    fun obtenerCategorias(): Flow<List<Categoria>>

    suspend fun insertarCategoria(categoria: Categoria)

    suspend fun obtenerCategoriaPorId(id: Int): Categoria?

    suspend fun obtenerCategoriaPorNombre(nombre: String): Categoria?

    suspend fun actualizarCategoria(categoria: Categoria)

    suspend fun eliminarCategoria(categoria: Categoria)
}
