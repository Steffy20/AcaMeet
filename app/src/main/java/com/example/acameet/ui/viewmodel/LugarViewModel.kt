
package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.data.repository.LugarRepository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LugarUiState {

    data object Loading : LugarUiState

    data class Success(
        val lugares: List<Lugar>,
        val procesando: Boolean = false
    ) : LugarUiState

    data class Error(
        val mensaje: String
    ) : LugarUiState
}

class LugarViewModel(
    private val repository: LugarRepository,
    private val usuarioIdActivo: StateFlow<Int?>
) : ViewModel() {

    // =====================================================
    // ESTADO REACTIVO DE LOS LUGARES
    // =====================================================

    private val _procesando = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<LugarUiState>(
        LugarUiState.Loading
    )

    val uiState: StateFlow<LugarUiState> =
        _uiState.asStateFlow()

    // Se conserva esta propiedad para mantener
    // compatibilidad con HomeScreen, CategoriaScreen
    // y las demás pantallas existentes.
    val lugares: StateFlow<List<Lugar>> =
        usuarioIdActivo
            .flatMapLatest { usuarioId ->

                if (usuarioId == null || usuarioId <= 0) {
                    flowOf(emptyList())
                } else {
                    repository.obtenerLugares(usuarioId)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {

            usuarioIdActivo
                .flatMapLatest { usuarioId ->

                    if (usuarioId == null || usuarioId <= 0) {

                        flowOf<LugarUiState>(
                            LugarUiState.Success(
                                lugares = emptyList()
                            )
                        )

                    } else {

                        repository.obtenerLugares(usuarioId)
                            .map<List<Lugar>, LugarUiState> {
                                    lista ->

                                LugarUiState.Success(
                                    lugares = lista,
                                    procesando = _procesando.value
                                )
                            }
                            .onStart {
                                emit(LugarUiState.Loading)
                            }
                            .catch { error ->

                                if (error is CancellationException) {
                                    throw error
                                }

                                emit(
                                    LugarUiState.Error(
                                        error.message
                                            ?: "Error al cargar lugares"
                                    )
                                )
                            }
                    }
                }
                .collect { estado ->

                    _uiState.value = estado
                }
        }
    }

    // =====================================================
    // FUNCIONES DE APOYO
    // =====================================================

    private fun limpiarNombre(
        nombre: String
    ): String {
        return nombre.trim()
            .replace(Regex("\\s+"), " ")
    }

    private fun comprobarSesion(
        usuarioId: Int
    ) {
        check(usuarioIdActivo.value == usuarioId) {
            "La sesión ha cambiado. Intenta nuevamente."
        }
    }

    private fun actualizarProcesando(
        procesando: Boolean
    ) {
        _procesando.value = procesando

        val estado = _uiState.value

        if (estado is LugarUiState.Success) {

            _uiState.value = estado.copy(
                procesando = procesando
            )
        }
    }

    private fun mostrarError(
        mensaje: String
    ) {
        _uiState.value = LugarUiState.Error(
            mensaje = mensaje
        )
    }

    // =====================================================
    // REGISTRAR LUGAR
    // =====================================================

    fun registrarLugar(
        lugar: Lugar,
        onResultado: (Boolean, String) -> Unit
    ) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {

            onResultado(
                false,
                "Debes iniciar sesión"
            )
            return
        }

        viewModelScope.launch {

            actualizarProcesando(true)

            try {

                val nombreLimpio = limpiarNombre(
                    lugar.nombre
                )

                require(nombreLimpio.isNotBlank()) {
                    "El nombre del lugar es obligatorio"
                }

                val existente =
                    repository.obtenerLugarPorNombre(
                        nombre = nombreLimpio,
                        usuarioId = usuarioId
                    )

                require(existente == null) {
                    "Ya existe un lugar con ese nombre"
                }

                comprobarSesion(usuarioId)

                repository.insertarLugar(
                    lugar.copy(
                        id = 0,
                        nombre = nombreLimpio,
                        usuarioId = usuarioId
                    )
                )

                if (usuarioIdActivo.value == usuarioId) {
                    onResultado(
                        true,
                        "Lugar registrado correctamente"
                    )
                }

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                val mensaje = error.message
                    ?: "Error al registrar lugar"

                if (usuarioIdActivo.value == usuarioId) {
                    mostrarError(mensaje)

                    onResultado(
                        false,
                        mensaje
                    )
                }

            } finally {

                actualizarProcesando(false)
            }
        }
    }

    // =====================================================
    // OBTENER LUGAR POR ID
    // =====================================================

    fun obtenerLugarPorId(
        id: Int,
        onResultado: (Lugar?) -> Unit
    ) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {

            onResultado(null)
            return
        }

        viewModelScope.launch {

            try {

                val lugar = repository.obtenerLugarPorId(
                    id = id,
                    usuarioId = usuarioId
                )

                onResultado(
                    if (usuarioIdActivo.value == usuarioId) {
                        lugar
                    } else {
                        null
                    }
                )

            } catch (error: CancellationException) {

                throw error

            } catch (_: Exception) {

                onResultado(null)
            }
        }
    }

    // =====================================================
    // OBTENER LUGAR POR NOMBRE
    // =====================================================

    fun obtenerLugarPorNombre(
        nombre: String,
        onResultado: (Lugar?) -> Unit
    ) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {

            onResultado(null)
            return
        }

        viewModelScope.launch {

            try {

                val lugar =
                    repository.obtenerLugarPorNombre(
                        nombre = limpiarNombre(nombre),
                        usuarioId = usuarioId
                    )

                onResultado(
                    if (usuarioIdActivo.value == usuarioId) {
                        lugar
                    } else {
                        null
                    }
                )

            } catch (error: CancellationException) {

                throw error

            } catch (_: Exception) {

                onResultado(null)
            }
        }
    }

    // =====================================================
    // ACTUALIZAR LUGAR
    // =====================================================

    fun actualizarLugar(
        lugar: Lugar,
        onResultado: (Boolean, String) -> Unit
    ) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {

            onResultado(
                false,
                "Debes iniciar sesión"
            )
            return
        }

        viewModelScope.launch {

            actualizarProcesando(true)

            try {

                val nombreLimpio = limpiarNombre(
                    lugar.nombre
                )

                require(nombreLimpio.isNotBlank()) {
                    "El nombre del lugar es obligatorio"
                }

                val existente =
                    repository.obtenerLugarPorNombre(
                        nombre = nombreLimpio,
                        usuarioId = usuarioId
                    )

                require(
                    existente == null ||
                            existente.id == lugar.id
                ) {
                    "Ya existe un lugar con ese nombre"
                }

                comprobarSesion(usuarioId)

                val filas =
                    repository.actualizarLugar(
                        lugar = lugar.copy(
                            nombre = nombreLimpio
                        ),
                        usuarioId = usuarioId
                    )

                if (usuarioIdActivo.value == usuarioId) {

                    if (filas > 0) {

                        onResultado(
                            true,
                            "Lugar actualizado correctamente"
                        )

                    } else {

                        onResultado(
                            false,
                            "El lugar no existe o no pertenece a tu cuenta"
                        )
                    }
                }

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                val mensaje = error.message
                    ?: "Error al actualizar lugar"

                if (usuarioIdActivo.value == usuarioId) {
                    mostrarError(mensaje)

                    onResultado(
                        false,
                        mensaje
                    )
                }

            } finally {

                actualizarProcesando(false)
            }
        }
    }

    // =====================================================
    // ELIMINAR LUGAR
    // =====================================================

    fun eliminarLugar(
        lugar: Lugar,
        onResultado: (Boolean, String) -> Unit
    ) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {

            onResultado(
                false,
                "Debes iniciar sesión"
            )
            return
        }

        viewModelScope.launch {

            actualizarProcesando(true)

            try {

                comprobarSesion(usuarioId)

                val filas =
                    repository.eliminarLugar(
                        lugar = lugar,
                        usuarioId = usuarioId
                    )

                if (usuarioIdActivo.value == usuarioId) {

                    if (filas > 0) {

                        onResultado(
                            true,
                            "Lugar eliminado correctamente"
                        )

                    } else {

                        onResultado(
                            false,
                            "El lugar no existe o no pertenece a tu cuenta"
                        )
                    }
                }

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                val mensaje = error.message
                    ?: "Error al eliminar lugar"

                if (usuarioIdActivo.value == usuarioId) {
                    mostrarError(mensaje)

                    onResultado(
                        false,
                        mensaje
                    )
                }

            } finally {

                actualizarProcesando(false)
            }
        }
    }
}
