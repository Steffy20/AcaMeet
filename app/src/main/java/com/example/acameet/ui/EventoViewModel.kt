
package com.example.acameet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.acameet.data.local.entity.Evento
import com.example.acameet.data.repository.EventoRepository
import com.example.acameet.util.RecordatorioEventoScheduler

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// =====================================================
// ESTADOS DE LA PANTALLA DE EVENTOS
// =====================================================

sealed class EventoUiState {

    data object Loading : EventoUiState()

    data class Success(
        val eventos: List<Evento>
    ) : EventoUiState()

    data class Error(
        val mensaje: String
    ) : EventoUiState()
}

// =====================================================
// MENSAJES DE OPERACIONES
// =====================================================

data class EventoMensaje(
    val exitoso: Boolean,
    val texto: String
)

// =====================================================
// VIEWMODEL DE EVENTOS
// =====================================================

class EventoViewModel(
    private val repository: EventoRepository,
    private val usuarioIdActivo: StateFlow<Int?>,
    private val recordatorioScheduler:
    RecordatorioEventoScheduler? = null
) : ViewModel() {

    // =================================================
    // BÚSQUEDA
    // =================================================

    private val _busqueda = MutableStateFlow("")

    val busqueda: StateFlow<String> =
        _busqueda.asStateFlow()

    // =================================================
    // FILTRO POR CATEGORÍA
    // =================================================

    private val _categoriaFiltro =
        MutableStateFlow<Int?>(null)

    val categoriaFiltro: StateFlow<Int?> =
        _categoriaFiltro.asStateFlow()

    // =================================================
    // MENSAJES DE UNA SOLA EJECUCIÓN
    // =================================================

    private val _mensajes =
        Channel<EventoMensaje>(Channel.BUFFERED)

    val mensajes = _mensajes.receiveAsFlow()

    // =================================================
    // ESTADO DE LA INTERFAZ
    // =================================================

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<EventoUiState> =
        usuarioIdActivo
            .flatMapLatest { usuarioId ->

                if (usuarioId == null || usuarioId <= 0) {

                    flowOf<EventoUiState>(
                        EventoUiState.Success(emptyList())
                    )

                } else {

                    combine(
                        repository.obtenerEventos(usuarioId),
                        _busqueda,
                        _categoriaFiltro
                    ) { eventos, textoBusqueda, categoriaId ->

                        val filtrados = eventos.filter { evento ->

                            val perteneceAlUsuario =
                                evento.usuarioId == usuarioId

                            val coincideBusqueda =
                                textoBusqueda.isBlank() ||
                                        evento.nombre.contains(
                                            textoBusqueda,
                                            ignoreCase = true
                                        ) ||
                                        evento.descripcion.contains(
                                            textoBusqueda,
                                            ignoreCase = true
                                        )

                            val coincideCategoria =
                                categoriaId == null ||
                                        evento.categoriaId == categoriaId

                            perteneceAlUsuario &&
                                    coincideBusqueda &&
                                    coincideCategoria
                        }

                        EventoUiState.Success(
                            eventos = filtrados
                        ) as EventoUiState
                    }
                }
            }
            .catch { error ->

                if (error is CancellationException) {
                    throw error
                }

                emit(
                    EventoUiState.Error(
                        error.message
                            ?: "Error al cargar los eventos"
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = EventoUiState.Loading
            )

    // =================================================
    // ACTUALIZAR BÚSQUEDA
    // =================================================

    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    // =================================================
    // FILTRAR POR CATEGORÍA
    // =================================================

    fun filtrarPorCategoria(categoriaId: Int?) {
        _categoriaFiltro.value = categoriaId
    }

    // =================================================
    // LIMPIAR FILTROS
    // =================================================

    fun limpiarFiltros() {
        _busqueda.value = ""
        _categoriaFiltro.value = null
    }

    // =================================================
    // OBTENER EVENTO POR ID
    // =================================================

    suspend fun obtenerEventoParaPantalla(
        id: Int
    ): Evento? {

        val usuarioId =
            usuarioIdActivo.value ?: return null

        if (usuarioId <= 0) {
            return null
        }

        return try {

            val evento = repository.obtenerEventoPorId(
                id = id,
                usuarioId = usuarioId
            )

            if (
                usuarioIdActivo.value == usuarioId &&
                evento?.usuarioId == usuarioId
            ) {
                evento
            } else {
                null
            }

        } catch (error: CancellationException) {

            throw error

        } catch (_: Exception) {

            null
        }
    }

    // =================================================
    // REGISTRAR EVENTO
    // =================================================

    fun registrarEvento(
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        categoriaId: Int,
        lugarId: Int,
        imagenUrl: String = ""
    ) {

        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {

            enviarMensaje(
                exitoso = false,
                texto = "Debes iniciar sesión"
            )

            return
        }

        viewModelScope.launch {

            try {

                require(nombre.isNotBlank()) {
                    "El nombre del evento es obligatorio"
                }

                require(descripcion.isNotBlank()) {
                    "La descripción del evento es obligatoria"
                }

                require(fecha.isNotBlank()) {
                    "La fecha del evento es obligatoria"
                }

                require(hora.isNotBlank()) {
                    "La hora del evento es obligatoria"
                }

                require(categoriaId > 0) {
                    "Selecciona una categoría válida"
                }

                require(lugarId > 0) {
                    "Selecciona un lugar válido"
                }

                comprobarSesion(usuarioId)

                val evento = Evento(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    fecha = fecha,
                    hora = hora,
                    categoriaId = categoriaId,
                    lugarId = lugarId,
                    imagenUrl = imagenUrl,
                    usuarioId = usuarioId
                )

                val nuevoId =
                    repository.insertarEvento(evento)

                comprobarSesion(usuarioId)

                try {

                    recordatorioScheduler?.programar(
                        evento.copy(
                            id = nuevoId.toInt()
                        )
                    )

                } catch (_: Exception) {
                    // El registro en Room ya se realizó.
                }

                enviarMensajeSiSesionActiva(
                    usuarioId = usuarioId,
                    exitoso = true,
                    texto = "Evento registrado correctamente"
                )

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                enviarMensajeSiSesionActiva(
                    usuarioId = usuarioId,
                    exitoso = false,
                    texto = error.message
                        ?: "Error al registrar el evento"
                )
            }
        }
    }

    // =================================================
    // ACTUALIZAR EVENTO
    // =================================================

    fun actualizarEvento(
        evento: Evento,
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        categoriaId: Int,
        lugarId: Int,
        imagenUrl: String = evento.imagenUrl
    ) {

        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {

            enviarMensaje(
                exitoso = false,
                texto = "Debes iniciar sesión"
            )

            return
        }

        viewModelScope.launch {

            try {

                require(evento.usuarioId == usuarioId) {
                    "Este evento no pertenece a tu cuenta"
                }

                require(nombre.isNotBlank()) {
                    "El nombre del evento es obligatorio"
                }

                require(descripcion.isNotBlank()) {
                    "La descripción del evento es obligatoria"
                }

                require(fecha.isNotBlank()) {
                    "La fecha del evento es obligatoria"
                }

                require(hora.isNotBlank()) {
                    "La hora del evento es obligatoria"
                }

                require(categoriaId > 0) {
                    "Selecciona una categoría válida"
                }

                require(lugarId > 0) {
                    "Selecciona un lugar válido"
                }

                comprobarSesion(usuarioId)

                val actualizado = evento.copy(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    fecha = fecha,
                    hora = hora,
                    categoriaId = categoriaId,
                    lugarId = lugarId,
                    imagenUrl = imagenUrl
                )

                val filas = repository.actualizarEvento(
                    evento = actualizado,
                    usuarioId = usuarioId
                )

                comprobarSesion(usuarioId)

                if (filas > 0) {

                    try {

                        recordatorioScheduler?.programar(
                            actualizado
                        )

                    } catch (_: Exception) {
                        // La actualización ya se guardó.
                    }

                    enviarMensajeSiSesionActiva(
                        usuarioId = usuarioId,
                        exitoso = true,
                        texto = "Evento actualizado correctamente"
                    )

                } else {

                    enviarMensajeSiSesionActiva(
                        usuarioId = usuarioId,
                        exitoso = false,
                        texto = "El evento no existe o no pertenece a tu cuenta"
                    )
                }

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                enviarMensajeSiSesionActiva(
                    usuarioId = usuarioId,
                    exitoso = false,
                    texto = error.message
                        ?: "Error al actualizar el evento"
                )
            }
        }
    }

    // =================================================
    // ELIMINAR EVENTO
    // =================================================

    fun eliminarEvento(evento: Evento) {

        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {

            enviarMensaje(
                exitoso = false,
                texto = "Debes iniciar sesión"
            )

            return
        }

        viewModelScope.launch {

            try {

                require(evento.usuarioId == usuarioId) {
                    "Este evento no pertenece a tu cuenta"
                }

                comprobarSesion(usuarioId)

                val filas = repository.eliminarEvento(
                    evento = evento,
                    usuarioId = usuarioId
                )

                comprobarSesion(usuarioId)

                if (filas > 0) {

                    try {

                        recordatorioScheduler?.cancelar(
                            evento.id
                        )

                    } catch (_: Exception) {
                        // El evento ya se eliminó de Room.
                    }

                    enviarMensajeSiSesionActiva(
                        usuarioId = usuarioId,
                        exitoso = true,
                        texto = "Evento eliminado correctamente"
                    )

                } else {

                    enviarMensajeSiSesionActiva(
                        usuarioId = usuarioId,
                        exitoso = false,
                        texto = "El evento no existe o no pertenece a tu cuenta"
                    )
                }

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                enviarMensajeSiSesionActiva(
                    usuarioId = usuarioId,
                    exitoso = false,
                    texto = error.message
                        ?: "Error al eliminar el evento"
                )
            }
        }
    }

    // =================================================
    // COMPROBAR QUE LA SESIÓN NO HAYA CAMBIADO
    // =================================================

    private fun comprobarSesion(usuarioId: Int) {

        check(usuarioIdActivo.value == usuarioId) {
            "La sesión ha cambiado. Intenta nuevamente."
        }
    }

    // =================================================
    // ENVIAR MENSAJES SOLO A LA SESIÓN CORRECTA
    // =================================================

    private fun enviarMensajeSiSesionActiva(
        usuarioId: Int,
        exitoso: Boolean,
        texto: String
    ) {

        if (usuarioIdActivo.value != usuarioId) {
            return
        }

        viewModelScope.launch {

            if (usuarioIdActivo.value == usuarioId) {

                _mensajes.send(
                    EventoMensaje(
                        exitoso = exitoso,
                        texto = texto
                    )
                )
            }
        }
    }

    // =================================================
    // ENVIAR MENSAJE GENERAL
    // =================================================

    private fun enviarMensaje(
        exitoso: Boolean,
        texto: String
    ) {

        viewModelScope.launch {

            _mensajes.send(
                EventoMensaje(
                    exitoso = exitoso,
                    texto = texto
                )
            )
        }
    }
}
