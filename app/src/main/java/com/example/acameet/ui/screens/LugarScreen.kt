
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
import com.example.acameet.ui.viewmodel.LugarViewModel

@Composable
fun LugarScreen(
    viewModel: LugarViewModel,
    onInicioClick: () -> Unit,
    usuarioIdActivo: Int? = null
) {
    // =====================================================
    // DATOS DEL USUARIO ACTIVO
    // =====================================================

    val lugaresRoom by viewModel.lugares.collectAsStateWithLifecycle()

    val lugares = remember(lugaresRoom, usuarioIdActivo) {
        if (usuarioIdActivo != null && usuarioIdActivo > 0) {
            lugaresRoom.filter {
                it.usuarioId == usuarioIdActivo
            }
        } else {
            emptyList()
        }
    }

    // =====================================================
    // ESTADOS DE LA PANTALLA
    // =====================================================

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

    // =====================================================
    // CONTENIDO PRINCIPAL
    // =====================================================

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
            onClick = {
                nombre = ""
                errorNombre = null
                lugarEditar = null
                mostrarFormulario = true
            },
            enabled = sesionActiva && !guardando
        ) {
            Text("+ Registrar lugar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!sesionActiva) {

            Text(
                text = "Debes iniciar sesión para administrar lugares",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        } else if (lugares.isEmpty()) {

            Text(
                text = "No hay lugares registrados",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        } else {

            LazyColumn {

                items(
                    items = lugares,
                    key = { it.id }
                ) { lugar ->

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
                                    onClick = {
                                        lugarEditar = lugar
                                        nombre = lugar.nombre
                                        errorNombre = null
                                        mostrarFormulario = true
                                    },
                                    enabled = !guardando && !eliminando
                                ) {
                                    Text("Editar")
                                }

                                TextButton(
                                    onClick = {
                                        lugarEliminar = lugar
                                    },
                                    enabled = !guardando && !eliminando
                                ) {
                                    Text("Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // =====================================================
    // FORMULARIO REGISTRAR / EDITAR
    // =====================================================

    if (mostrarFormulario && sesionActiva) {

        AlertDialog(
            onDismissRequest = {
                if (!guardando && !verificandoNombre) {
                    mostrarFormulario = false
                    lugarEditar = null
                    errorNombre = null
                }
            },

            title = {
                Text(
                    if (lugarEditar == null) {
                        "Registrar lugar"
                    } else {
                        "Editar lugar"
                    }
                )
            },

            text = {

                Column {

                    OutlinedTextField(
                        value = nombre,

                        onValueChange = {
                            nombre = it
                            errorNombre = null
                        },

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

                    onClick = {

                        val nombreLimpio = nombre
                            .trim()
                            .replace(Regex("\\s+"), " ")

                        val lugarActual = lugarEditar
                        val usuarioOperacion = usuarioIdActivo

                        when {

                            nombreLimpio.isBlank() -> {

                                errorNombre =
                                    "Ingresa el nombre del lugar"
                            }

                            !sesionActiva -> {

                                errorNombre =
                                    "Debes iniciar sesión"
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

                                    if (
                                        usuarioOperacion ==
                                        usuarioIdActivo
                                    ) {

                                        verificandoNombre = false

                                        if (
                                            existente != null &&
                                            existente.id != lugarActual?.id
                                        ) {

                                            errorNombre =
                                                "El lugar ya existe"

                                        } else {

                                            guardando = true

                                            val lugar = if (
                                                lugarActual == null
                                            ) {

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

                                                            mostrarFormulario =
                                                                false

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

                    onClick = {
                        mostrarFormulario = false
                        lugarEditar = null
                        errorNombre = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // =====================================================
    // CONFIRMAR ELIMINACIÓN
    // =====================================================

    lugarEliminar?.let { lugar ->

        if (
            sesionActiva &&
            lugar.usuarioId == usuarioIdActivo
        ) {

            AlertDialog(
                onDismissRequest = {
                    if (!eliminando) {
                        lugarEliminar = null
                    }
                },

                title = {
                    Text("Eliminar lugar")
                },

                text = {
                    Text(
                        "¿Deseas eliminar ${lugar.nombre}? " +
                                "Esta acción no se puede deshacer."
                    )
                },

                confirmButton = {

                    TextButton(
                        enabled = !eliminando,

                        onClick = {

                            if (!eliminando) {

                                eliminando = true

                                val usuarioOperacion = usuarioIdActivo

                                viewModel.eliminarLugar(lugar) {
                                        exitoso, texto ->

                                    if (
                                        usuarioOperacion ==
                                        usuarioIdActivo
                                    ) {

                                        eliminando = false

                                        if (exitoso) {

                                            lugarEliminar = null
                                            mensaje = texto

                                        } else {

                                            lugarEliminar = null
                                            mensaje = texto
                                        }
                                    }
                                }
                            }
                        }
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

                        onClick = {
                            lugarEliminar = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    // =====================================================
    // MENSAJES DE RESULTADO
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
