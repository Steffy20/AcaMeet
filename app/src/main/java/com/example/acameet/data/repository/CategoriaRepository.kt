package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.CategoriaDao
import com.example.acameet.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

class CategoriaRepository(
    private val categoriaDao: CategoriaDao
) {

    fun obtenerCategorias(): Flow<List<Categoria>> {
        return categoriaDao.obtenerCategorias()
    }

    suspend fun insertarCategoria(categoria: Categoria) {
        categoriaDao.insertarCategoria(categoria)
    }

    suspend fun obtenerCategoriaPorId(id: Int): Categoria? {
        return categoriaDao.obtenerCategoriaPorId(id)
    }

    suspend fun obtenerCategoriaPorNombre(
        nombre: String
    ): Categoria? {
        return categoriaDao.obtenerCategoriaPorNombre(nombre)
    }

    suspend fun actualizarCategoria(categoria: Categoria) {
        categoriaDao.actualizarCategoria(categoria)
    }

    suspend fun eliminarCategoria(categoria: Categoria) {
        categoriaDao.eliminarCategoria(categoria)
    }
}