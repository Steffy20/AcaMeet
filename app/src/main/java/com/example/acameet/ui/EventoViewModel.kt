
package com.example.acameet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.acameet.data.local.entity.Evento
import com.example.acameet.data.repository.EventoRepository
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
// MENSAJES DE RESULTADO
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
    private val usuarioIdActivo: StateFlow<Int?>
) : ViewModel() {

    // BÚSQUEDA DE EVENTOS
    private val _busqueda = MutableStateFlow("")

    // Exponer la búsqueda a EventoScreen
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // FILTRO DE CATEGORÍAS
    private val _categoriaFiltro = MutableStateFlow<Int?>(null)

    val categoriaFiltro: StateFlow<Int?> =
        _categoriaFiltro.asStateFlow()

    // MENSAJES
    private val _mensajes =
        Channel<EventoMensaje>(Channel.BUFFERED)

    val mensajes = _mensajes.receiveAsFlow()

    // =====================================================
    // OBTENER EVENTOS DEL USUARIO ACTIVO
    // =====================================================

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

                            coincideBusqueda && coincideCategoria
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
                        error.message ?: "Error al cargar eventos"
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = EventoUiState.Loading
            )

    // =====================================================
    // ACTUALIZAR BÚSQUEDA
    // =====================================================

    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    // =====================================================
    // FILTRAR POR CATEGORÍA
    // =====================================================

    fun filtrarPorCategoria(categoriaId: Int?) {
        _categoriaFiltro.value = categoriaId
    }

    // =====================================================
    // LIMPIAR FILTROS
    // =====================================================

    fun limpiarFiltros() {
        _busqueda.value = ""
        _categoriaFiltro.value = null
    }

    // =====================================================
    // OBTENER EVENTO POR ID
    // =====================================================

    suspend fun obtenerEventoParaPantalla(id: Int): Evento? {

        val usuarioId = usuarioIdActivo.value ?: return null

        if (usuarioId <= 0) {
            return null
        }

        return try {

            val evento = repository.obtenerEventoPorId(
                id = id,
                usuarioId = usuarioId
            )

            if (usuarioIdActivo.value == usuarioId) {
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

    // =====================================================
    // REGISTRAR EVENTO
    // =====================================================

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
                false,
                "Debes iniciar sesión"
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

                comprobarSesion(usuarioId)

                repository.insertarEvento(
                    Evento(
                        nombre = nombre.trim(),
                        descripcion = descripcion.trim(),
                        fecha = fecha,
                        hora = hora,
                        categoriaId = categoriaId,
                        lugarId = lugarId,
                        imagenUrl = imagenUrl,
                        usuarioId = usuarioId
                    )
                )

                comprobarSesion(usuarioId)

                enviarMensaje(
                    true,
                    "Evento registrado correctamente"
                )

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                if (usuarioIdActivo.value == usuarioId) {
                    enviarMensaje(
                        false,
                        error.message
                            ?: "Error al registrar evento"
                    )
                }
            }
        }
    }

    // =====================================================
    // ACTUALIZAR EVENTO
    // =====================================================

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
                false,
                "Debes iniciar sesión"
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

                    enviarMensaje(
                        true,
                        "Evento actualizado correctamente"
                    )

                } else {

                    enviarMensaje(
                        false,
                        "El evento no existe o no pertenece a tu cuenta"
                    )
                }

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                if (usuarioIdActivo.value == usuarioId) {
                    enviarMensaje(
                        false,
                        error.message
                            ?: "Error al actualizar evento"
                    )
                }
            }
        }
    }

    // =====================================================
    // ELIMINAR EVENTO
    // =====================================================

    fun eliminarEvento(evento: Evento) {

        val usuarioId = usuarioIdActivo.value

        if (usuarioId == null || usuarioId <= 0) {
            enviarMensaje(
                false,
                "Debes iniciar sesión"
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

                    enviarMensaje(
                        true,
                        "Evento eliminado correctamente"
                    )

                } else {

                    enviarMensaje(
                        false,
                        "El evento no existe o no pertenece a tu cuenta"
                    )
                }

            } catch (error: CancellationException) {

                throw error

            } catch (error: Exception) {

                if (usuarioIdActivo.value == usuarioId) {
                    enviarMensaje(
                        false,
                        error.message
                            ?: "Error al eliminar evento"
                    )
                }
            }
        }
    }

    // =====================================================
    // COMPROBAR SESIÓN
    // =====================================================

    private fun comprobarSesion(usuarioId: Int) {

        check(usuarioIdActivo.value == usuarioId) {
            "La sesión ha cambiado. Intenta nuevamente."
        }
    }

    // =====================================================
    // ENVIAR MENSAJES
    // =====================================================

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
