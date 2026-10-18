
package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.data.repository.LugarRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LugarViewModel(
    private val repository: LugarRepository,
    private val usuarioIdActivo: StateFlow<Int?>
) : ViewModel() {

    // LUGARES DEL USUARIO ACTIVO
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

    // NORMALIZAR EL NOMBRE
    private fun limpiarNombre(nombre: String): String {
        return nombre.trim().replace(Regex("\\s+"), " ")
    }

    // COMPROBAR LA SESIÓN
    private fun comprobarSesion(usuarioId: Int) {
        check(usuarioIdActivo.value == usuarioId) {
            "La sesión ha cambiado. Intenta nuevamente."
        }
    }

    // REGISTRAR UN LUGAR
    fun registrarLugar(
        lugar: Lugar,
        onResultado: (Boolean, String) -> Unit
    ) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {
            onResultado(false, "Debes iniciar sesión")
            return
        }

        viewModelScope.launch {
            try {
                val nombreLimpio = limpiarNombre(lugar.nombre)

                require(nombreLimpio.isNotBlank()) {
                    "El nombre del lugar es obligatorio"
                }

                val existente = repository.obtenerLugarPorNombre(
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

                onResultado(
                    true,
                    "Lugar registrado correctamente"
                )

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                onResultado(
                    false,
                    error.message ?: "Error al registrar lugar"
                )
            }
        }
    }

    // BUSCAR LUGAR POR ID
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
                    if (usuarioIdActivo.value == usuarioId) lugar
                    else null
                )

            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                onResultado(null)
            }
        }
    }

    // BUSCAR LUGAR POR NOMBRE
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
                val lugar = repository.obtenerLugarPorNombre(
                    nombre = limpiarNombre(nombre),
                    usuarioId = usuarioId
                )

                onResultado(
                    if (usuarioIdActivo.value == usuarioId) lugar
                    else null
                )

            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                onResultado(null)
            }
        }
    }

    // ACTUALIZAR UN LUGAR
    fun actualizarLugar(
        lugar: Lugar,
        onResultado: (Boolean, String) -> Unit
    ) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {
            onResultado(false, "Debes iniciar sesión")
            return
        }

        viewModelScope.launch {
            try {
                val nombreLimpio = limpiarNombre(lugar.nombre)

                require(nombreLimpio.isNotBlank()) {
                    "El nombre del lugar es obligatorio"
                }

                val existente = repository.obtenerLugarPorNombre(
                    nombre = nombreLimpio,
                    usuarioId = usuarioId
                )

                require(
                    existente == null || existente.id == lugar.id
                ) {
                    "Ya existe un lugar con ese nombre"
                }

                comprobarSesion(usuarioId)

                val filas = repository.actualizarLugar(
                    lugar = lugar.copy(nombre = nombreLimpio),
                    usuarioId = usuarioId
                )

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

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                onResultado(
                    false,
                    error.message ?: "Error al actualizar lugar"
                )
            }
        }
    }

    // ELIMINAR UN LUGAR
    fun eliminarLugar(
        lugar: Lugar,
        onResultado: (Boolean, String) -> Unit
    ) {
        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {
            onResultado(false, "Debes iniciar sesión")
            return
        }

        viewModelScope.launch {
            try {
                comprobarSesion(usuarioId)

                val filas = repository.eliminarLugar(
                    lugar = lugar,
                    usuarioId = usuarioId
                )

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

            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                onResultado(
                    false,
                    error.message ?: "Error al eliminar lugar"
                )
            }
        }
    }
}
