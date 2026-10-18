
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
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

// MENSAJES DE RESULTADO
data class EventoMensaje(
    val exitoso: Boolean,
    val texto: String
)

// VIEWMODEL DE EVENTOS
class EventoViewModel(
    private val repository: EventoRepository
) : ViewModel() {

    // BÚSQUEDA DE EVENTOS
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    // FILTRO POR CATEGORÍA
    private val _categoriaFiltro = MutableStateFlow<Int?>(null)
    val categoriaFiltro: StateFlow<Int?> = _categoriaFiltro

    // CANAL DE MENSAJES
    private val mensajeChannel =
        Channel<EventoMensaje>(Channel.BUFFERED)

    val mensajes = mensajeChannel.receiveAsFlow()

    // LISTADO DE EVENTOS
    val uiState: StateFlow<EventoUiState> = combine(
        repository.obtenerEventos(),
        _busqueda,
        _categoriaFiltro
    ) { eventos, texto, categoriaId ->

        val busquedaLimpia = texto.trim()

        EventoUiState.Success(
            eventos = eventos.filter { evento ->

                val coincideBusqueda =
                    busquedaLimpia.isBlank() ||
                            evento.nombre.contains(
                                busquedaLimpia,
                                ignoreCase = true
                            ) ||
                            evento.descripcion.contains(
                                busquedaLimpia,
                                ignoreCase = true
                            )

                val coincideCategoria =
                    categoriaId == null ||
                            evento.categoriaId == categoriaId

                coincideBusqueda && coincideCategoria
            }
        ) as EventoUiState

    }.catch { error ->

        emit(
            EventoUiState.Error(
                error.message ?: "Error al cargar los eventos"
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

    // LIMPIAR FILTROS
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

    // REGISTRAR EVENTO CON IMAGEN
    fun registrarEvento(
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        categoriaId: Int,
        lugarId: Int,
        imagenUrl: String = ""
    ) {

        if (
            !validar(
                nombre = nombre,
                descripcion = descripcion,
                fecha = fecha,
                hora = hora,
                categoriaId = categoriaId,
                lugarId = lugarId,
                imagenUrl = imagenUrl
            )
        ) {
            return
        }

        viewModelScope.launch {

            try {

                val nuevoEvento = Evento(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    fecha = fecha.trim(),
                    hora = hora.trim(),
                    categoriaId = categoriaId,
                    lugarId = lugarId,
                    imagenUrl = imagenUrl.trim()
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
                        texto = "No se pudo registrar el evento: " +
                                (e.message ?: "Error desconocido")
                    )
                )
            }
        }
    }

    // ACTUALIZAR EVENTO CON IMAGEN
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

        if (
            !validar(
                nombre = nombre,
                descripcion = descripcion,
                fecha = fecha,
                hora = hora,
                categoriaId = categoriaId,
                lugarId = lugarId,
                imagenUrl = imagenUrl
            )
        ) {
            return
        }

        viewModelScope.launch {

            try {

                val eventoActualizado = evento.copy(
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    fecha = fecha.trim(),
                    hora = hora.trim(),
                    categoriaId = categoriaId,
                    lugarId = lugarId,
                    imagenUrl = imagenUrl.trim()
                )

                repository.actualizarEvento(eventoActualizado)

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
                        texto = "No se pudo actualizar el evento: " +
                                (e.message ?: "Error desconocido")
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
                        texto = "No se pudo eliminar el evento: " +
                                (e.message ?: "Error desconocido")
                    )
                )
            }
        }
    }

    // VALIDAR DATOS DEL EVENTO
    private fun validar(
        nombre: String,
        descripcion: String,
        fecha: String,
        hora: String,
        categoriaId: Int,
        lugarId: Int,
        imagenUrl: String
    ): Boolean {

        val mensaje = when {

            nombre.isBlank() ->
                "Ingresa el nombre del evento"

            descripcion.isBlank() ->
                "Ingresa la descripción del evento"

            fecha.isBlank() ->
                "Selecciona la fecha del evento"

            hora.isBlank() ->
                "Selecciona la hora del evento"

            categoriaId <= 0 ->
                "Selecciona una categoría válida"

            lugarId <= 0 ->
                "Selecciona un lugar válido"

            imagenUrl.isNotBlank() &&
                    !imagenUrl.trim().startsWith(
                        "https://",
                        ignoreCase = true
                    ) ->
                "La URL de la imagen debe comenzar con https://"

            else -> null
        }

        if (mensaje != null) {

            enviarMensaje(
                exitoso = false,
                texto = mensaje
            )

            return false
        }

        return true
    }

    // ENVIAR MENSAJE A LA PANTALLA
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
