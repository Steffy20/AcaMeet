
package com.example.acameet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.acameet.data.local.entity.Evento
import com.example.acameet.data.repository.EventoRepository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ESTADOS DE LA PANTALLA DE EVENTOS
sealed interface EventoUiState {

    data object Loading : EventoUiState

    data class Success(
        val eventos: List<Evento>
    ) : EventoUiState

    data class Error(
        val mensaje: String
    ) : EventoUiState
}

// MENSAJES DE LAS OPERACIONES
data class EventoMensaje(
    val exitoso: Boolean,
    val texto: String
)

// VIEWMODEL DE EVENTOS
class EventoViewModel(
    private val repository: EventoRepository
) : ViewModel() {

    // TEXTO DE BÚSQUEDA
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // FILTRO POR CATEGORÍA
    private val _categoriaFiltro = MutableStateFlow<Int?>(null)
    val categoriaFiltro: StateFlow<Int?> =
        _categoriaFiltro.asStateFlow()

    // MENSAJES PARA LA INTERFAZ
    private val _mensajes = Channel<EventoMensaje>(
        Channel.BUFFERED
    )

    val mensajes = _mensajes.receiveAsFlow()

    // ESTADO DE LOS EVENTOS
    val uiState: StateFlow<EventoUiState> = combine(
        repository.obtenerEventos(),
        _busqueda,
        _categoriaFiltro
    ) { eventos, texto, categoriaId ->

        val eventosFiltrados = eventos.filter { evento ->

            val coincideBusqueda =
                evento.nombre.contains(
                    texto,
                    ignoreCase = true
                ) ||
                        evento.descripcion.contains(
                            texto,
                            ignoreCase = true
                        )

            val coincideCategoria =
                categoriaId == null ||
                        evento.categoriaId == categoriaId

            coincideBusqueda && coincideCategoria
        }

        EventoUiState.Success(eventosFiltrados) as EventoUiState

    }
        .catch { error ->
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

    // ACTUALIZAR BÚSQUEDA
    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    // FILTRAR POR CATEGORÍA
    fun filtrarPorCategoria(categoriaId: Int?) {
        _categoriaFiltro.value = categoriaId
    }

    // REGISTRAR EVENTO
    fun registrarEvento(
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        categoriaId: Int,
        lugarId: Int
    ) {
        viewModelScope.launch {

            if (
                nombre.isBlank() ||
                descripcion.isBlank() ||
                fecha.isBlank() ||
                hora.isBlank() ||
                categoriaId <= 0 ||
                lugarId <= 0
            ) {
                _mensajes.send(
                    EventoMensaje(
                        exitoso = false,
                        texto = "Completa todos los campos"
                    )
                )
                return@launch
            }

            try {

                val evento = Evento(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    fecha = fecha.trim(),
                    hora = hora.trim(),
                    categoriaId = categoriaId,
                    lugarId = lugarId
                )

                repository.insertarEvento(evento)

                _mensajes.send(
                    EventoMensaje(
                        exitoso = true,
                        texto = "Evento registrado correctamente"
                    )
                )

            } catch (e: Exception) {

                _mensajes.send(
                    EventoMensaje(
                        exitoso = false,
                        texto = e.message
                            ?: "Error al registrar evento"
                    )
                )
            }
        }
    }

    // ACTUALIZAR EVENTO
    fun actualizarEvento(
        evento: Evento,
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        categoriaId: Int,
        lugarId: Int
    ) {
        viewModelScope.launch {

            if (
                nombre.isBlank() ||
                descripcion.isBlank() ||
                fecha.isBlank() ||
                hora.isBlank() ||
                categoriaId <= 0 ||
                lugarId <= 0
            ) {
                _mensajes.send(
                    EventoMensaje(
                        exitoso = false,
                        texto = "Completa todos los campos"
                    )
                )
                return@launch
            }

            try {

                val eventoActualizado = evento.copy(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    fecha = fecha.trim(),
                    hora = hora.trim(),
                    categoriaId = categoriaId,
                    lugarId = lugarId
                )

                repository.actualizarEvento(eventoActualizado)

                _mensajes.send(
                    EventoMensaje(
                        exitoso = true,
                        texto = "Evento actualizado correctamente"
                    )
                )

            } catch (e: Exception) {

                _mensajes.send(
                    EventoMensaje(
                        exitoso = false,
                        texto = e.message
                            ?: "Error al actualizar evento"
                    )
                )
            }
        }
    }

    // ELIMINAR EVENTO
    fun eliminarEvento(evento: Evento) {
        viewModelScope.launch {

            try {

                repository.eliminarEvento(evento)

                _mensajes.send(
                    EventoMensaje(
                        exitoso = true,
                        texto = "Evento eliminado correctamente"
                    )
                )

            } catch (e: Exception) {

                _mensajes.send(
                    EventoMensaje(
                        exitoso = false,
                        texto = e.message
                            ?: "Error al eliminar evento"
                    )
                )
            }
        }
    }
}
