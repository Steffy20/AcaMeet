
package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.repository.CategoriaRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface CategoriaUiState {

    data object Loading : CategoriaUiState

    data class Success(
        val categorias: List<Categoria>,
        val procesando: Boolean = false
    ) : CategoriaUiState

    data class Error(
        val mensaje: String
    ) : CategoriaUiState
}

data class CategoriaMensaje(
    val exitoso: Boolean,
    val texto: String
)

class CategoriaViewModel(
    private val repository: CategoriaRepository
) : ViewModel() {

    private val _procesando = MutableStateFlow(false)

    private val mensajeChannel =
        Channel<CategoriaMensaje>(Channel.BUFFERED)

    val mensajes = mensajeChannel.receiveAsFlow()

    val uiState: StateFlow<CategoriaUiState> =
        combine(
            repository.obtenerCategorias(),
            _procesando
        ) { categorias, procesando ->

            CategoriaUiState.Success(
                categorias = categorias,
                procesando = procesando
            ) as CategoriaUiState

        }.catch { error ->

            emit(
                CategoriaUiState.Error(
                    error.message ?: "Error al cargar categorías"
                )
            )

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoriaUiState.Loading
        )

    // REGISTRAR CATEGORÍA
    fun registrarCategoria(nombre: String) {

        val nombreLimpio = nombre
            .trim()
            .replace(Regex("\\s+"), " ")

        if (nombreLimpio.isBlank()) {
            enviarMensaje(
                false,
                "Ingresa el nombre de la categoría"
            )
            return
        }

        if (_procesando.value) return

        viewModelScope.launch {

            _procesando.value = true

            try {
                val existente =
                    repository.obtenerCategoriaPorNombre(nombreLimpio)

                if (existente != null) {
                    mensajeChannel.send(
                        CategoriaMensaje(
                            false,
                            "La categoría ya existe"
                        )
                    )
                    return@launch
                }

                repository.insertarCategoria(
                    Categoria(nombre = nombreLimpio)
                )

                mensajeChannel.send(
                    CategoriaMensaje(
                        true,
                        "Categoría registrada correctamente"
                    )
                )

            } catch (e: Exception) {

                mensajeChannel.send(
                    CategoriaMensaje(
                        false,
                        "No se pudo registrar la categoría: " +
                                (e.message ?: "Error desconocido")
                    )
                )

            } finally {
                _procesando.value = false
            }
        }
    }

    // ACTUALIZAR CATEGORÍA
    fun actualizarCategoria(
        categoria: Categoria,
        nuevoNombre: String
    ) {

        val nombreLimpio = nuevoNombre
            .trim()
            .replace(Regex("\\s+"), " ")

        if (nombreLimpio.isBlank()) {
            enviarMensaje(
                false,
                "Ingresa el nombre de la categoría"
            )
            return
        }

        if (_procesando.value) return

        viewModelScope.launch {

            _procesando.value = true

            try {
                val existente =
                    repository.obtenerCategoriaPorNombre(nombreLimpio)

                if (
                    existente != null &&
                    existente.id != categoria.id
                ) {
                    mensajeChannel.send(
                        CategoriaMensaje(
                            false,
                            "La categoría ya existe"
                        )
                    )
                    return@launch
                }

                repository.actualizarCategoria(
                    categoria.copy(nombre = nombreLimpio)
                )

                mensajeChannel.send(
                    CategoriaMensaje(
                        true,
                        "Categoría actualizada correctamente"
                    )
                )

            } catch (e: Exception) {

                mensajeChannel.send(
                    CategoriaMensaje(
                        false,
                        "No se pudo actualizar la categoría: " +
                                (e.message ?: "Error desconocido")
                    )
                )

            } finally {
                _procesando.value = false
            }
        }
    }

    // ELIMINAR CATEGORÍA
    fun eliminarCategoria(categoria: Categoria) {

        if (_procesando.value) return

        viewModelScope.launch {

            _procesando.value = true

            try {
                repository.eliminarCategoria(categoria)

                mensajeChannel.send(
                    CategoriaMensaje(
                        true,
                        "Categoría eliminada correctamente"
                    )
                )

            } catch (e: Exception) {

                mensajeChannel.send(
                    CategoriaMensaje(
                        false,
                        "No se pudo eliminar la categoría: " +
                                (e.message ?: "Error desconocido")
                    )
                )

            } finally {
                _procesando.value = false
            }
        }
    }

    private fun enviarMensaje(
        exitoso: Boolean,
        texto: String
    ) {
        viewModelScope.launch {
            mensajeChannel.send(
                CategoriaMensaje(exitoso, texto)
            )
        }
    }
}
