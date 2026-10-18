
package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.repository.CategoriaRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ESTADOS DE LA PANTALLA DE CATEGORÍAS
sealed class CategoriaUiState {
    data object Loading : CategoriaUiState()

    data class Success(
        val categorias: List<Categoria>,
        val procesando: Boolean = false
    ) : CategoriaUiState()

    data class Error(
        val mensaje: String
    ) : CategoriaUiState()
}

// MENSAJES DE RESULTADO
data class CategoriaMensaje(
    val exitoso: Boolean,
    val texto: String
)

class CategoriaViewModel(
    private val repository: CategoriaRepository,
    private val usuarioIdActivo: StateFlow<Int?>
) : ViewModel() {

    private val _procesando = MutableStateFlow(false)

    private val _mensajes = Channel<CategoriaMensaje>(
        Channel.BUFFERED
    )

    val mensajes = _mensajes.receiveAsFlow()

    // CARGAR SOLO LAS CATEGORÍAS DEL USUARIO ACTIVO
    val uiState: StateFlow<CategoriaUiState> =
        usuarioIdActivo
            .flatMapLatest { usuarioId ->

                if (usuarioId == null || usuarioId <= 0) {
                    flowOf<CategoriaUiState>(
                        CategoriaUiState.Success(emptyList())
                    )
                } else {
                    combine(
                        repository.obtenerCategorias(usuarioId),
                        _procesando
                    ) { categorias, procesando ->

                        CategoriaUiState.Success(
                            categorias = categorias,
                            procesando = procesando
                        ) as CategoriaUiState
                    }
                }
            }
            .catch { error ->
                emit(
                    CategoriaUiState.Error(
                        error.message
                            ?: "No se pudieron cargar las categorías"
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = CategoriaUiState.Loading
            )

    // NORMALIZAR NOMBRES
    private fun limpiarNombre(nombre: String): String {
        return nombre.trim().replace(Regex("\\s+"), " ")
    }

    // REGISTRAR CATEGORÍA
    fun registrarCategoria(nombre: String) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {
            enviarMensaje(false, "Debes iniciar sesión")
            return
        }

        viewModelScope.launch {
            _procesando.value = true

            try {
                val nombreLimpio = limpiarNombre(nombre)

                require(nombreLimpio.isNotBlank()) {
                    "El nombre de la categoría es obligatorio"
                }

                val existente = repository.obtenerCategoriaPorNombre(
                    nombre = nombreLimpio,
                    usuarioId = usuarioId
                )

                require(existente == null) {
                    "Ya existe una categoría con ese nombre"
                }

                comprobarSesion(usuarioId)

                repository.insertarCategoria(
                    Categoria(
                        nombre = nombreLimpio,
                        usuarioId = usuarioId
                    )
                )

                _mensajes.send(
                    CategoriaMensaje(
                        exitoso = true,
                        texto = "Categoría registrada correctamente"
                    )
                )

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                enviarMensaje(
                    false,
                    error.message ?: "Error al registrar categoría"
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
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {
            enviarMensaje(false, "Debes iniciar sesión")
            return
        }

        viewModelScope.launch {
            _procesando.value = true

            try {
                val nombreLimpio = limpiarNombre(nuevoNombre)

                require(nombreLimpio.isNotBlank()) {
                    "El nombre de la categoría es obligatorio"
                }

                val existente = repository.obtenerCategoriaPorNombre(
                    nombre = nombreLimpio,
                    usuarioId = usuarioId
                )

                require(
                    existente == null ||
                            existente.id == categoria.id
                ) {
                    "Ya existe una categoría con ese nombre"
                }

                comprobarSesion(usuarioId)

                val filas = repository.actualizarCategoria(
                    categoria = categoria.copy(
                        nombre = nombreLimpio
                    ),
                    usuarioId = usuarioId
                )

                if (filas > 0) {
                    _mensajes.send(
                        CategoriaMensaje(
                            true,
                            "Categoría actualizada correctamente"
                        )
                    )
                } else {
                    enviarMensaje(
                        false,
                        "La categoría no existe o no pertenece a tu cuenta"
                    )
                }

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                enviarMensaje(
                    false,
                    error.message ?: "Error al actualizar categoría"
                )
            } finally {
                _procesando.value = false
            }
        }
    }

    // ELIMINAR CATEGORÍA
    fun eliminarCategoria(categoria: Categoria) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {
            enviarMensaje(false, "Debes iniciar sesión")
            return
        }

        viewModelScope.launch {
            _procesando.value = true

            try {
                comprobarSesion(usuarioId)

                val filas = repository.eliminarCategoria(
                    categoria = categoria,
                    usuarioId = usuarioId
                )

                if (filas > 0) {
                    _mensajes.send(
                        CategoriaMensaje(
                            true,
                            "Categoría eliminada correctamente"
                        )
                    )
                } else {
                    enviarMensaje(
                        false,
                        "La categoría no existe o no pertenece a tu cuenta"
                    )
                }

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                enviarMensaje(
                    false,
                    error.message ?: "Error al eliminar categoría"
                )
            } finally {
                _procesando.value = false
            }
        }
    }

    // COMPROBAR QUE LA SESIÓN NO HAYA CAMBIADO
    private fun comprobarSesion(usuarioId: Int) {
        check(usuarioIdActivo.value == usuarioId) {
            "La sesión ha cambiado. Intenta nuevamente."
        }
    }

    // ENVIAR MENSAJES A LA PANTALLA
    private fun enviarMensaje(
        exitoso: Boolean,
        texto: String
    ) {
        viewModelScope.launch {
            _mensajes.send(
                CategoriaMensaje(
                    exitoso = exitoso,
                    texto = texto
                )
            )
        }
    }
}
