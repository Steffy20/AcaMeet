
package com.example.acameet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.acameet.data.local.entity.Evento
import com.example.acameet.data.repository.EventoRepository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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

    val busqueda: StateFlow<String> = _busqueda

    // FILTRO POR CATEGORÍA
    private val _categoriaFiltro =
        MutableStateFlow<Int?>(null)

    val categoriaFiltro: StateFlow<Int?> =
        _categoriaFiltro

    // CANAL PARA COMUNICAR RESULTADOS A LA INTERFAZ
    private val mensajeChannel =
        Channel<EventoMensaje>(Channel.BUFFERED)

    val mensajes = mensajeChannel.receiveAsFlow()

    // EVENTOS OBTENIDOS DESDE ROOM
    private val eventosRoom = repository.obtenerEventos()

    // ESTADO REACTIVO DE LA PANTALLA
    val uiState: StateFlow<EventoUiState> =
        combine(
            eventosRoom,
            _busqueda,
            _categoriaFiltro
        ) { eventos, texto, categoriaId ->

            val textoLimpio = texto.trim()

            val eventosFiltrados = eventos.filter { evento ->

                // VERIFICAR BÚSQUEDA
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

                // VERIFICAR CATEGORÍA
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

    // ACTUALIZAR TEXTO DE BÚSQUEDA
    fun actualizarBusqueda(texto: String) {
        _busqueda.value = texto
    }

    // FILTRAR EVENTOS POR CATEGORÍA
    fun filtrarPorCategoria(categoriaId: Int?) {
        _categoriaFiltro.value = categoriaId
    }

    // LIMPIAR BÚSQUEDA Y FILTROS
    fun limpiarFiltros() {
        _busqueda.value = ""
        _categoriaFiltro.value = null
    }

    // OBTENER EVENTO POR ID
    suspend fun obtenerEventoParaPantalla(
        id: Int
    ): Evento? {
        return repository.obtenerEventoPorId(id)
    }

    // REGISTRAR UN NUEVO EVENTO
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
        val fechaLimpia = fecha.trim()
        val horaLimpia = hora.trim()

        // VALIDAR CAMPOS OBLIGATORIOS
        if (
            nombreLimpio.isBlank() ||
            descripcionLimpia.isBlank() ||
            fechaLimpia.isBlank() ||
            horaLimpia.isBlank()
        ) {
            enviarMensaje(
                false,
                "Completa todos los campos del evento"
            )
            return
        }

        // VALIDAR CATEGORÍA
        if (categoriaId <= 0) {
            enviarMensaje(
                false,
                "Selecciona una categoría válida"
            )
            return
        }

        // VALIDAR LUGAR
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
                    fecha = fechaLimpia,
                    hora = horaLimpia,
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

            } catch (e: CancellationException) {
                throw e
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

    // ACTUALIZAR UN EVENTO EXISTENTE
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
        val fechaLimpia = fecha.trim()
        val horaLimpia = hora.trim()

        // VALIDAR CAMPOS OBLIGATORIOS
        if (
            nombreLimpio.isBlank() ||
            descripcionLimpia.isBlank() ||
            fechaLimpia.isBlank() ||
            horaLimpia.isBlank()
        ) {
            enviarMensaje(
                false,
                "Completa todos los campos del evento"
            )
            return
        }

        // VALIDAR CATEGORÍA
        if (categoriaId <= 0) {
            enviarMensaje(
                false,
                "Selecciona una categoría válida"
            )
            return
        }

        // VALIDAR LUGAR
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
                    fecha = fechaLimpia,
                    hora = horaLimpia,
                    categoriaId = categoriaId,
                    lugarId = lugarId
                )

                repository.actualizarEvento(
                    eventoActualizado
                )

                mensajeChannel.send(
                    EventoMensaje(
                        exitoso = true,
                        texto = "Evento actualizado correctamente"
                    )
                )

            } catch (e: CancellationException) {
                throw e
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

    // ELIMINAR UN EVENTO
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

            } catch (e: CancellationException) {
                throw e
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

    // ENVIAR MENSAJES A LA INTERFAZ
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
