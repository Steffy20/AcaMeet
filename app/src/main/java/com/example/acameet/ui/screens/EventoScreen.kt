
package com.example.acameet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.acameet.data.local.entity.Evento
import com.example.acameet.ui.viewmodel.EventoUiState
import com.example.acameet.ui.viewmodel.EventoViewModel

private val Azul = Color(0xFF0798F2)
private val Tarjeta = Color(0xFF0A1C28)
private val Gris = Color(0xFF9BA8B4)

// PANTALLA CON ESTADO
@Composable
fun EventoScreen(
    viewModel: EventoViewModel,
    onVolver: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()

    var mostrarAgregar by remember { mutableStateOf(false) }
    var eventoEditar by remember { mutableStateOf<Evento?>(null) }
    var eventoEliminar by remember { mutableStateOf<Evento?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.mensajes.collect { resultado ->
            mensaje = resultado.texto
            if (resultado.exitoso) {
                mostrarAgregar = false
                eventoEditar = null
                eventoEliminar = null
            }
        }
    }

    EventoContent(
        uiState = uiState,
        busqueda = busqueda,
        onBusquedaChange = viewModel::actualizarBusqueda,
        onAgregar = { mostrarAgregar = true },
        onEditar = { eventoEditar = it },
        onEliminar = { eventoEliminar = it }
    )

    if (mostrarAgregar) {
        DialogoEvento(
            titulo = "Nuevo evento",
            eventoInicial = null,
            onCancelar = { mostrarAgregar = false },
            onGuardar = { nombre, descripcion, fecha, hora, categoriaId, lugarId ->
                viewModel.registrarEvento(
                    nombre, descripcion, fecha, hora, categoriaId, lugarId
                )
            }
        )
    }

    eventoEditar?.let { evento ->
        DialogoEvento(
            titulo = "Editar evento",
            eventoInicial = evento,
            onCancelar = { eventoEditar = null },
            onGuardar = { nombre, descripcion, fecha, hora, categoriaId, lugarId ->
                viewModel.actualizarEvento(
                    evento, nombre, descripcion, fecha, hora, categoriaId, lugarId
                )
            }
        )
    }

    eventoEliminar?.let { evento ->
        AlertDialog(
            onDismissRequest = { eventoEliminar = null },
            containerColor = Tarjeta,
            titleContentColor = Color.White,
            textContentColor = Gris,
            title = { Text("Eliminar evento") },
            text = {
                Text("¿Deseas eliminar el evento \"${evento.nombre}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.eliminarEvento(evento) }
                ) {
                    Text("Eliminar", color = Color(0xFFE57373))
                }
            },
            dismissButton = {
                TextButton(onClick = { eventoEliminar = null }) {
                    Text("Cancelar", color = Gris)
                }
            }
        )
    }

    mensaje?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensaje = null },
            containerColor = Tarjeta,
            titleContentColor = Color.White,
            textContentColor = Gris,
            title = { Text("AcaMeet") },
            text = { Text(texto) },
            confirmButton = {
                TextButton(onClick = { mensaje = null }) {
                    Text("Aceptar", color = Azul)
                }
            }
        )
    }
}

// PANTALLA SIN VIEWMODEL
@Composable
private fun EventoContent(
    uiState: EventoUiState,
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    onAgregar: () -> Unit,
    onEditar: (Evento) -> Unit,
    onEliminar: (Evento) -> Unit
) {
    val fondo = Brush.verticalGradient(
        listOf(
            Color(0xFF020B12),
            Color(0xFF03141F),
            Color(0xFF001D32)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregar,
                containerColor = Azul,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar evento")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(padding)
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(35.dp))

            Text(
                "Eventos",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Administra los eventos académicos",
                color = Gris,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(22.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = onBusquedaChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar eventos...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Azul,
                    focusedLeadingIconColor = Azul
                )
            )

            Spacer(Modifier.height(20.dp))

            when (uiState) {
                is EventoUiState.Loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Azul)
                    }
                }

                is EventoUiState.Error -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            uiState.mensaje,
                            color = Color(0xFFE57373)
                        )
                    }
                }

                is EventoUiState.Success -> {
                    if (uiState.eventos.isEmpty()) {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = null,
                                    tint = Azul,
                                    modifier = Modifier.size(55.dp)
                                )
                                Spacer(Modifier.height(14.dp))
                                Text(
                                    "No hay eventos para mostrar",
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            items(
                                items = uiState.eventos,
                                key = { it.id }
                            ) { evento ->
                                TarjetaEvento(
                                    evento = evento,
                                    onEditar = { onEditar(evento) },
                                    onEliminar = { onEliminar(evento) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// TARJETA DE EVENTO
@Composable
private fun TarjetaEvento(
    evento: Evento,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Tarjeta)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF102E42)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        tint = Azul
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    evento.nombre,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    evento.descripcion,
                    color = Gris,
                    fontSize = 13.sp,
                    maxLines = 2
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${evento.fecha}  •  ${evento.hora}",
                    color = Azul,
                    fontSize = 12.sp
                )
            }

            IconButton(onClick = onEditar) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar evento",
                    tint = Azul
                )
            }

            IconButton(onClick = onEliminar) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar evento",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}

// FORMULARIO PARA REGISTRAR Y EDITAR
@Composable
private fun DialogoEvento(
    titulo: String,
    eventoInicial: Evento?,
    onCancelar: () -> Unit,
    onGuardar: (String, String, String, String, Int, Int) -> Unit
) {
    var nombre by remember(eventoInicial) {
        mutableStateOf(eventoInicial?.nombre ?: "")
    }
    var descripcion by remember(eventoInicial) {
        mutableStateOf(eventoInicial?.descripcion ?: "")
    }
    var fecha by remember(eventoInicial) {
        mutableStateOf(eventoInicial?.fecha ?: "")
    }
    var hora by remember(eventoInicial) {
        mutableStateOf(eventoInicial?.hora ?: "")
    }
    var categoria by remember(eventoInicial) {
        mutableStateOf(eventoInicial?.categoriaId?.toString() ?: "")
    }
    var lugar by remember(eventoInicial) {
        mutableStateOf(eventoInicial?.lugarId?.toString() ?: "")
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = Tarjeta,
        titleContentColor = Color.White,
        textContentColor = Gris,
        title = { Text(titulo) },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del evento") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    label = { Text("Hora (HH:MM)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("ID de categoría") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = lugar,
                    onValueChange = { lugar = it },
                    label = { Text("ID de lugar") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onGuardar(
                        nombre,
                        descripcion,
                        fecha,
                        hora,
                        categoria.toIntOrNull() ?: 0,
                        lugar.toIntOrNull() ?: 0
                    )
                }
            ) {
                Text("Guardar", color = Azul)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar", color = Gris)
            }
        }
    )
}
