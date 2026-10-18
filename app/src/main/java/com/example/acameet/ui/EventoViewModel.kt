
package com.example.acameet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.acameet.data.local.entity.Evento
import com.example.acameet.data.repository.EventoRepository

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estados de la pantalla de eventos
sealed interface EventoUiState {

    data object Loading : EventoUiState

    data class Success(
        val eventos: List<Evento>
    ) : EventoUiState

    data class Error(
        val mensaje: String
    ) : EventoUiState
}

// Mensajes de las operaciones
data class EventoMensaje(
    val exitoso: Boolean,
    val texto: String
)

// ViewModel de eventos
class EventoViewModel(
    private val repository: EventoRepository
) : ViewModel() {

    // Texto de búsqueda
    private val _busqueda = MutableStateFlow("")

    val busqueda: StateFlow<String> = _busqueda

    // Filtro por categoría
    private val _categoriaFiltro = MutableStateFlow<Int?>(null)

    val categoriaFiltro: StateFlow<Int?> = _categoriaFiltro

    // Canal para comunicar resultados a la interfaz
    private val mensajeChannel =
        Channel<EventoMensaje>(Channel.BUFFERED)

    val mensajes = mensajeChannel.receiveAsFlow()

    // Lista de eventos obtenidos desde Room
    private val eventosRoom = repository.obtenerEventos()

    // Estado de la pantalla
    val uiState: StateFlow<EventoUiState> =
        combine(
            eventosRoom,
            _busqueda,
            _categoriaFiltro
        ) { eventos, texto, categoriaId ->

            val textoLimpio = texto.trim()

            val eventosFiltrados = eventos.filter { evento ->

                val coincideBusqueda =
                    textoLimpio.isBlank() ||
                            evento.nombre.contains(
                                textoLimpio,
                                ignoreCase = true
                            ) ||
                            evento.descripcion.contains(
                                textoLimpio,
                                ignoreCase = true
                            )

                val coincideCategoria =
                    categoriaId == null ||
                            evento.categoriaId == categoriaId

                coincideBusqueda && coincideCategoria
            }

            EventoUiState.Success(
                eventos = eventosFiltrados
            ) as EventoUiState

        }.catch { error ->

            emit(
                EventoUiState.Error(
                    mensaje = error.message
                        ?: "Error al cargar los eventos"
                )
            )

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EventoUiState.Loading
        )

    // Actualizar búsqueda
    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    // Filtrar eventos por categoría
    fun filtrarPorCategoria(categoriaId: Int?) {
        _categoriaFiltro.value = categoriaId
    }

    // Limpiar búsqueda y filtros
    fun limpiarFiltros() {
        _busqueda.value = ""
        _categoriaFiltro.value = null
    }

    // Obtener un evento por ID para abrir su detalle
    suspend fun obtenerEventoParaPantalla(id: Int): Evento? {
        return repository.obtenerEventoPorId(id)
    }

    // Registrar un nuevo evento
    fun registrarEvento(
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        categoriaId: Int,
        lugarId: Int
    ) {

        val nombreLimpio = nombre.trim()
        val descripcionLimpia = descripcion.trim()

        if (
            nombreLimpio.isBlank() ||
            descripcionLimpia.isBlank() ||
            fecha.isBlank() ||
            hora.isBlank()
        ) {
            enviarMensaje(
                false,
                "Completa todos los campos del evento"
            )
            return
        }

        if (categoriaId <= 0) {
            enviarMensaje(
                false,
                "Selecciona una categoría válida"
            )
            return
        }

        if (lugarId <= 0) {
            enviarMensaje(
                false,
                "Selecciona un lugar válido"
            )
            return
        }

        viewModelScope.launch {
            try {

                val nuevoEvento = Evento(
                    nombre = nombreLimpio,
                    descripcion = descripcionLimpia,
                    fecha = fecha,
                    hora = hora,
                    categoriaId = categoriaId,
                    lugarId = lugarId
                )

                repository.insertarEvento(nuevoEvento)

                mensajeChannel.send(
                    EventoMensaje(
                        exitoso = true,
                        texto = "Evento registrado correctamente"
                    )
                )

            } catch (e: Exception) {

                mensajeChannel.send(
                    EventoMensaje(
                        exitoso = false,
                        texto = "No se pudo registrar el evento"
                    )
                )
            }
        }
    }

    // Actualizar un evento existente
    fun actualizarEvento(
        evento: Evento,
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        categoriaId: Int,
        lugarId: Int
    ) {

        val nombreLimpio = nombre.trim()
        val descripcionLimpia = descripcion.trim()

        if (
            nombreLimpio.isBlank() ||
            descripcionLimpia.isBlank() ||
            fecha.isBlank() ||
            hora.isBlank()
        ) {
            enviarMensaje(
                false,
                "Completa todos los campos del evento"
            )
            return
        }

        if (categoriaId <= 0) {
            enviarMensaje(
                false,
                "Selecciona una categoría válida"
            )
            return
        }

        if (lugarId <= 0) {
            enviarMensaje(
                false,
                "Selecciona un lugar válido"
            )
            return
        }

        viewModelScope.launch {
            try {

                val eventoActualizado = evento.copy(
                    nombre = nombreLimpio,
                    descripcion = descripcionLimpia,
                    fecha = fecha,
                    hora = hora,
                    categoriaId = categoriaId,
                    lugarId = lugarId
                )

                repository.actualizarEvento(eventoActualizado)

                mensajeChannel.send(
                    EventoMensaje(
                        exitoso = true,
                        texto = "Evento actualizado correctamente"
                    )
                )

            } catch (e: Exception) {

                mensajeChannel.send(
                    EventoMensaje(
                        exitoso = false,
                        texto = "No se pudo actualizar el evento"
                    )
                )
            }
        }
    }

    // Eliminar un evento
    fun eliminarEvento(evento: Evento) {

        viewModelScope.launch {
            try {

                repository.eliminarEvento(evento)

                mensajeChannel.send(
                    EventoMensaje(
                        exitoso = true,
                        texto = "Evento eliminado correctamente"
                    )
                )

            } catch (e: Exception) {

                mensajeChannel.send(
                    EventoMensaje(
                        exitoso = false,
                        texto = "No se pudo eliminar el evento"
                    )
                )
            }
        }
    }

    // Enviar mensajes a la interfaz
    private fun enviarMensaje(
        exitoso: Boolean,
        texto: String
    ) {
        viewModelScope.launch {
            mensajeChannel.send(
                EventoMensaje(
                    exitoso = exitoso,
                    texto = texto
                )
            )
        }
    }
}
