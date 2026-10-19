
package com.example.acameet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.example.acameet.data.local.entity.Notificacion
import com.example.acameet.data.repository.NotificacionRepository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

// =====================================================
// ESTADOS DE LA PANTALLA DE NOTIFICACIONES
// =====================================================

sealed interface NotificacionUiState {

    data object Loading : NotificacionUiState

    data class Success(
        val notificaciones: List<Notificacion>,
        val pendientes: Int
    ) : NotificacionUiState

    data class Error(
        val mensaje: String
    ) : NotificacionUiState
}

// =====================================================
// VIEWMODEL DE NOTIFICACIONES
// =====================================================

@OptIn(ExperimentalCoroutinesApi::class)
class NotificacionViewModel(
    private val repository: NotificacionRepository
) : ViewModel() {

    // USUARIO ACTUAL
    private val usuarioId = MutableStateFlow<Int?>(null)

    // ESTADO DE LA INTERFAZ
    private val _uiState = MutableStateFlow<NotificacionUiState>(
        NotificacionUiState.Loading
    )

    val uiState: StateFlow<NotificacionUiState> =
        _uiState.asStateFlow()

    // =================================================
    // OBSERVAR NOTIFICACIONES DEL USUARIO
    // =================================================

    init {

        viewModelScope.launch {

            usuarioId
                .flatMapLatest { id ->

                    if (id == null || id <= 0) {

                        flowOf(
                            NotificacionUiState.Success(
                                notificaciones = emptyList(),
                                pendientes = 0
                            )
                        )

                    } else {

                        combine(
                            repository.obtenerNotificaciones(id),
                            repository.contarNoLeidas(id)
                        ) { lista, total ->

                            NotificacionUiState.Success(
                                notificaciones = lista,
                                pendientes = total
                            ) as NotificacionUiState

                        }.catch { error ->

                            if (error is CancellationException) {
                                throw error
                            }

                            emit(
                                NotificacionUiState.Error(
                                    mensaje = error.message
                                        ?: "No se pudieron cargar las notificaciones"
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

    // =================================================
    // SELECCIONAR USUARIO ACTIVO
    // =================================================

    fun seleccionarUsuario(id: Int?) {

        val nuevoId = id?.takeIf { it > 0 }

        if (usuarioId.value != nuevoId) {

            // LIMPIAR DATOS DE LA SESIÓN ANTERIOR
            _uiState.value = if (nuevoId == null) {

                NotificacionUiState.Success(
                    notificaciones = emptyList(),
                    pendientes = 0
                )

            } else {

                NotificacionUiState.Loading
            }

            usuarioId.value = nuevoId
        }
    }

    // =================================================
    // MARCAR UNA NOTIFICACIÓN COMO LEÍDA
    // =================================================

    fun marcarComoLeida(id: Int) {

        if (id <= 0) return

        ejecutar { user ->

            repository.marcarComoLeida(
                notificacionId = id,
                usuarioId = user
            )
        }
    }

    // =================================================
    // MARCAR TODAS LAS NOTIFICACIONES COMO LEÍDAS
    // =================================================

    fun marcarTodasComoLeidas() {

        ejecutar { user ->

            repository.marcarTodasComoLeidas(
                usuarioId = user
            )
        }
    }

    // =================================================
    // ELIMINAR UNA NOTIFICACIÓN
    // =================================================

    fun eliminarNotificacion(id: Int) {

        if (id <= 0) return

        ejecutar { user ->

            repository.eliminarNotificacion(
                notificacionId = id,
                usuarioId = user
            )
        }
    }

    // =================================================
    // ELIMINAR TODO EL HISTORIAL
    // =================================================

    fun eliminarTodas() {

        ejecutar { user ->

            repository.eliminarTodas(
                usuarioId = user
            )
        }
    }

    // =================================================
    // EJECUTAR OPERACIONES DEL REPOSITORIO
    // =================================================

    private fun ejecutar(
        accion: suspend (Int) -> Unit
    ) {

        val id = usuarioId.value ?: return

        if (id <= 0) return

        viewModelScope.launch {

            try {

                // EVITAR OPERACIONES DE UNA SESIÓN ANTIGUA
                if (usuarioId.value != id) {
                    return@launch
                }

                accion(id)

            } catch (e: CancellationException) {

                throw e

            } catch (e: Exception) {

                // NO MOSTRAR ERRORES DE OTRA SESIÓN
                if (usuarioId.value == id) {

                    _uiState.value = NotificacionUiState.Error(
                        mensaje = e.message
                            ?: "Ocurrió un error al administrar las notificaciones"
                    )
                }
            }
        }
    }
}

// =====================================================
// FACTORY DEL VIEWMODEL
// =====================================================

class NotificacionViewModelFactory(
    private val repository: NotificacionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                NotificacionViewModel::class.java
            )
        ) {

            return NotificacionViewModel(
                repository = repository
            ) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}
