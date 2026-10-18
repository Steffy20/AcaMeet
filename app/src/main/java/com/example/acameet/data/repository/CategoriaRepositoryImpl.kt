
package com.example.acameet.data.repository

import com.example.acameet.data.local.dao.CategoriaDao
import com.example.acameet.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

class CategoriaRepositoryImpl(
    private val categoriaDao: CategoriaDao
) : CategoriaRepository {

    // OBTENER CATEGORÍAS DEL USUARIO
    override fun obtenerCategorias(
        usuarioId: Int
    ): Flow<List<Categoria>> {
        require(usuarioId > 0) {
            "Debes iniciar sesión para consultar categorías"
        }

        return categoriaDao.obtenerCategorias(usuarioId)
    }

    // REGISTRAR CATEGORÍA
    override suspend fun insertarCategoria(
        categoria: Categoria
    ) {
        require(categoria.usuarioId > 0) {
            "Debes iniciar sesión para registrar categorías"
        }

        categoriaDao.insertarCategoria(categoria)
    }

    // BUSCAR CATEGORÍA POR ID
    override suspend fun obtenerCategoriaPorId(
        id: Int,
        usuarioId: Int
    ): Categoria? {
        require(usuarioId > 0) {
            "Debes iniciar sesión para consultar categorías"
        }

        return categoriaDao.obtenerCategoriaPorId(
            id = id,
            usuarioId = usuarioId
        )
    }

    // BUSCAR CATEGORÍA POR NOMBRE
    override suspend fun obtenerCategoriaPorNombre(
        nombre: String,
        usuarioId: Int
    ): Categoria? {
        require(usuarioId > 0) {
            "Debes iniciar sesión para consultar categorías"
        }

        return categoriaDao.obtenerCategoriaPorNombre(
            nombre = nombre,
            usuarioId = usuarioId
        )
    }

    // ACTUALIZAR CATEGORÍA
    override suspend fun actualizarCategoria(
        categoria: Categoria,
        usuarioId: Int
    ): Int {
        require(usuarioId > 0) {
            "Debes iniciar sesión para actualizar categorías"
        }

        return categoriaDao.actualizarCategoria(
            id = categoria.id,
            usuarioId = usuarioId,
            nombre = categoria.nombre
        )
    }

    // ELIMINAR CATEGORÍA
    override suspend fun eliminarCategoria(
        categoria: Categoria,
        usuarioId: Int
    ): Int {
        require(usuarioId > 0) {
            "Debes iniciar sesión para eliminar categorías"
        }

        return categoriaDao.eliminarCategoria(
            id = categoria.id,
            usuarioId = usuarioId
        )
    }
}
