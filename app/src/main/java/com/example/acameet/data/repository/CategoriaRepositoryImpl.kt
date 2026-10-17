package com.example.acameet.data.repository


import com.example.acameet.data.local.dao.CategoriaDao
import com.example.acameet.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

class CategoriaRepositoryImpl(
    private val categoriaDao: CategoriaDao
) : CategoriaRepository {

    override fun obtenerCategorias(): Flow<List<Categoria>> {
        return categoriaDao.obtenerCategorias()
    }

    override suspend fun insertarCategoria(categoria: Categoria) {
        categoriaDao.insertarCategoria(categoria)
    }

    override suspend fun obtenerCategoriaPorId(id: Int): Categoria? {
        return categoriaDao.obtenerCategoriaPorId(id)
    }

    override suspend fun obtenerCategoriaPorNombre(nombre: String): Categoria? {
        return categoriaDao.obtenerCategoriaPorNombre(nombre)
    }

    override suspend fun actualizarCategoria(categoria: Categoria) {
        categoriaDao.actualizarCategoria(categoria)
    }

    override suspend fun eliminarCategoria(categoria: Categoria) {
        categoriaDao.eliminarCategoria(categoria)
    }
}
