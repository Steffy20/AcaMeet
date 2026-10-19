
package com.example.acameet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.ui.viewmodel.LugarUiState
import com.example.acameet.ui.viewmodel.LugarViewModel

@Composable
fun LugarScreen(
    viewModel: LugarViewModel,
    onInicioClick: () -> Unit,
    usuarioIdActivo: Int? = null
) {
    LugarRoute(
        viewModel = viewModel,
        onInicioClick = onInicioClick,
        usuarioIdActivo = usuarioIdActivo
    )
}

// =====================================================
// ROUTE: ESTADO, VALIDACIONES Y OPERACIONES
// =====================================================

@Composable
fun LugarRoute(
    viewModel: LugarViewModel,
    onInicioClick: () -> Unit,
    usuarioIdActivo: Int? = null
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    val lugares = when (val actual = estado) {
        is LugarUiState.Success -> {
            if (usuarioIdActivo != null && usuarioIdActivo > 0) {
                actual.lugares.filter {
                    it.usuarioId == usuarioIdActivo
                }
            } else {
                emptyList()
            }
        }
        else -> emptyList()
    }

    var mostrarFormulario by remember(usuarioIdActivo) {
        mutableStateOf(false)
    }

    var lugarEditar by remember(usuarioIdActivo) {
        mutableStateOf<Lugar?>(null)
    }

    var lugarEliminar by remember(usuarioIdActivo) {
        mutableStateOf<Lugar?>(null)
    }

    var nombre by remember(usuarioIdActivo) {
        mutableStateOf("")
    }

    var mensaje by remember(usuarioIdActivo) {
        mutableStateOf("")
    }

    var errorNombre by remember(usuarioIdActivo) {
        mutableStateOf<String?>(null)
    }

    var guardando by remember(usuarioIdActivo) {
        mutableStateOf(false)
    }

    var eliminando by remember(usuarioIdActivo) {
        mutableStateOf(false)
    }

    var verificandoNombre by remember(usuarioIdActivo) {
        mutableStateOf(false)
    }

    val sesionActiva =
        usuarioIdActivo != null && usuarioIdActivo > 0

    val procesando =
        guardando || eliminando || verificandoNombre

    LugarContent(
        estado = estado,
        lugares = lugares,
        sesionActiva = sesionActiva,
        procesando = procesando,
        onInicioClick = onInicioClick,
        onRegistrarClick = {
            nombre = ""
            errorNombre = null
            lugarEditar = null
            mostrarFormulario = true
        },
        onEditarClick = { lugar ->
            lugarEditar = lugar
            nombre = lugar.nombre
            errorNombre = null
            mostrarFormulario = true
        },
        onEliminarClick = { lugar ->
            lugarEliminar = lugar
        }
    )

    // =====================================================
    // FORMULARIO PARA REGISTRAR O EDITAR
    // =====================================================

    if (mostrarFormulario && sesionActiva) {
        LugarFormularioDialog(
            esEdicion = lugarEditar != null,
            nombre = nombre,
            errorNombre = errorNombre,
            guardando = guardando,
            verificandoNombre = verificandoNombre,
            onNombreChange = {
                nombre = it
                errorNombre = null
            },
            onCancelar = {
                if (!guardando && !verificandoNombre) {
                    mostrarFormulario = false
                    lugarEditar = null
                    errorNombre = null
                }
            },
            onGuardar = {
                val nombreLimpio = nombre
                    .trim()
                    .replace(Regex("\\s+"), " ")

                val lugarActual = lugarEditar
                val usuarioOperacion = usuarioIdActivo

                when {
                    nombreLimpio.isBlank() -> {
                        errorNombre = "Ingresa el nombre del lugar"
                    }

                    !sesionActiva -> {
                        errorNombre = "Debes iniciar sesión"
                    }

                    lugarActual != null &&
                            lugarActual.usuarioId != usuarioOperacion -> {
                        errorNombre =
                            "Este lugar no pertenece a tu cuenta"
                    }

                    else -> {
                        verificandoNombre = true

                        viewModel.obtenerLugarPorNombre(
                            nombreLimpio
                        ) { existente ->

                            if (usuarioOperacion == usuarioIdActivo) {
                                verificandoNombre = false

                                if (
                                    existente != null &&
                                    existente.id != lugarActual?.id
                                ) {
                                    errorNombre = "El lugar ya existe"
                                } else {
                                    guardando = true

                                    val lugar = if (lugarActual == null) {
                                        Lugar(
                                            nombre = nombreLimpio
                                        )
                                    } else {
                                        lugarActual.copy(
                                            nombre = nombreLimpio
                                        )
                                    }

                                    val onResultado:
                                                (Boolean, String) -> Unit =
                                        { exitoso, texto ->

                                            if (
                                                usuarioOperacion ==
                                                usuarioIdActivo
                                            ) {
                                                guardando = false

                                                if (exitoso) {
                                                    mostrarFormulario = false
                                                    lugarEditar = null
                                                    nombre = ""
                                                    errorNombre = null
                                                    mensaje = texto
                                                } else {
                                                    errorNombre = texto
                                                }
                                            }
                                        }

                                    if (lugarActual == null) {
                                        viewModel.registrarLugar(
                                            lugar,
                                            onResultado
                                        )
                                    } else {
                                        viewModel.actualizarLugar(
                                            lugar,
                                            onResultado
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    // =====================================================
    // CONFIRMACIÓN PARA ELIMINAR
    // =====================================================

    lugarEliminar?.let { lugar ->
        if (
            sesionActiva &&
            lugar.usuarioId == usuarioIdActivo
        ) {
            LugarEliminarDialog(
                nombre = lugar.nombre,
                eliminando = eliminando,
                onCancelar = {
                    if (!eliminando) {
                        lugarEliminar = null
                    }
                },
                onConfirmar = {
                    if (!eliminando) {
                        eliminando = true

                        val usuarioOperacion = usuarioIdActivo

                        viewModel.eliminarLugar(lugar) {
                                _, texto ->

                            if (usuarioOperacion == usuarioIdActivo) {
                                eliminando = false
                                lugarEliminar = null
                                mensaje = texto
                            }
                        }
                    }
                }
            )
        }
    }

    // =====================================================
    // MENSAJES
    // =====================================================

    if (mensaje.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                mensaje = ""
            },
            title = {
                Text("Información")
            },
            text = {
                Text(mensaje)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mensaje = ""
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

// =====================================================
// CONTENT: SOLO INTERFAZ Y CALLBACKS
// =====================================================

@Composable
private fun LugarContent(
    estado: LugarUiState,
    lugares: List<Lugar>,
    sesionActiva: Boolean,
    procesando: Boolean,
    onInicioClick: () -> Unit,
    onRegistrarClick: () -> Unit,
    onEditarClick: (Lugar) -> Unit,
    onEliminarClick: (Lugar) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gestión de Lugares",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onInicioClick
        ) {
            Text("Volver al inicio")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRegistrarClick,
            enabled = sesionActiva && !procesando
        ) {
            Text("+ Registrar lugar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            !sesionActiva -> {
                Text(
                    text = "Debes iniciar sesión para administrar lugares",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            estado is LugarUiState.Loading -> {
                CircularProgressIndicator()
            }

            estado is LugarUiState.Error -> {
                Text(
                    text = estado.mensaje,
                    color = MaterialTheme.colorScheme.error
                )
            }

            lugares.isEmpty() -> {
                Text(
                    text = "No hay lugares registrados",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                LazyColumn {
                    items(
                        items = lugares,
                        key = { it.id }
                    ) { lugar ->
                        LugarItem(
                            lugar = lugar,
                            procesando = procesando,
                            onEditarClick = {
                                onEditarClick(lugar)
                            },
                            onEliminarClick = {
                                onEliminarClick(lugar)
                            }
                        )
                    }
                }
            }
        }
    }
}

// =====================================================
// TARJETA DE LUGAR
// =====================================================

@Composable
private fun LugarItem(
    lugar: Lugar,
    procesando: Boolean,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = lugar.nombre,
                style = MaterialTheme.typography.titleMedium
            )

            Row {
                TextButton(
                    onClick = onEditarClick,
                    enabled = !procesando
                ) {
                    Text("Editar")
                }

                TextButton(
                    onClick = onEliminarClick,
                    enabled = !procesando
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}

// =====================================================
// DIÁLOGO PARA REGISTRAR O EDITAR
// =====================================================

@Composable
private fun LugarFormularioDialog(
    esEdicion: Boolean,
    nombre: String,
    errorNombre: String?,
    guardando: Boolean,
    verificandoNombre: Boolean,
    onNombreChange: (String) -> Unit,
    onCancelar: () -> Unit,
    onGuardar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!guardando && !verificandoNombre) {
                onCancelar()
            }
        },
        title = {
            Text(
                if (esEdicion) {
                    "Editar lugar"
                } else {
                    "Registrar lugar"
                }
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = onNombreChange,
                    label = {
                        Text("Nombre del lugar")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !guardando && !verificandoNombre
                )

                errorNombre?.let { error ->
                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )

                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !guardando && !verificandoNombre,
                onClick = onGuardar
            ) {
                Text(
                    when {
                        verificandoNombre -> "Verificando..."
                        guardando -> "Guardando..."
                        else -> "Guardar"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !guardando && !verificandoNombre,
                onClick = onCancelar
            ) {
                Text("Cancelar")
            }
        }
    )
}

// =====================================================
// DIÁLOGO PARA ELIMINAR
// =====================================================

@Composable
private fun LugarEliminarDialog(
    nombre: String,
    eliminando: Boolean,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!eliminando) {
                onCancelar()
            }
        },
        title = {
            Text("Eliminar lugar")
        },
        text = {
            Text(
                "¿Deseas eliminar $nombre? " +
                        "Esta acción no se puede deshacer."
            )
        },
        confirmButton = {
            TextButton(
                enabled = !eliminando,
                onClick = onConfirmar
            ) {
                Text(
                    if (eliminando) {
                        "Eliminando..."
                    } else {
                        "Eliminar"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !eliminando,
                onClick = onCancelar
            ) {
                Text("Cancelar")
            }
        }
    )
}
