package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.repository.CategoriaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriaViewModel(
    private val repository: CategoriaRepository
) : ViewModel() {

    // Lista de categorías almacenadas en Room
    val categorias: StateFlow<List<Categoria>> =
        repository.obtenerCategorias()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Registrar una categoría
    fun registrarCategoria(
        nombre: String,
        onResultado: (Boolean, String) -> Unit
    ) {

        val nombreLimpio = nombre.trim()

        if (nombreLimpio.isBlank()) {
            onResultado(
                false,
                "Ingresa el nombre de la categoría"
            )
            return
        }

        viewModelScope.launch {

            val categoriaExistente =
                repository.obtenerCategoriaPorNombre(nombreLimpio)

            if (categoriaExistente != null) {

                onResultado(
                    false,
                    "La categoría ya existe"
                )

            } else {

                val nuevaCategoria = Categoria(
                    nombre = nombreLimpio
                )

                repository.insertarCategoria(nuevaCategoria)

                onResultado(
                    true,
                    "Categoría registrada correctamente"
                )
            }
        }
    }

    // Actualizar una categoría
    fun actualizarCategoria(
        categoria: Categoria,
        nuevoNombre: String,
        onResultado: (Boolean, String) -> Unit
    ) {

        val nombreLimpio = nuevoNombre.trim()

        if (nombreLimpio.isBlank()) {
            onResultado(
                false,
                "Ingresa el nombre de la categoría"
            )
            return
        }

        viewModelScope.launch {

            val categoriaExistente =
                repository.obtenerCategoriaPorNombre(nombreLimpio)

            if (
                categoriaExistente != null &&
                categoriaExistente.id != categoria.id
            ) {

                onResultado(
                    false,
                    "La categoría ya existe"
                )

            } else {

                val categoriaActualizada = categoria.copy(
                    nombre = nombreLimpio
                )

                repository.actualizarCategoria(
                    categoriaActualizada
                )

                onResultado(
                    true,
                    "Categoría actualizada correctamente"
                )
            }
        }
    }

    // Eliminar una categoría
    fun eliminarCategoria(
        categoria: Categoria,
        onResultado: (Boolean, String) -> Unit
    ) {

        viewModelScope.launch {

            repository.eliminarCategoria(categoria)

            onResultado(
                true,
                "Categoría eliminada correctamente"
            )
        }
    }
}