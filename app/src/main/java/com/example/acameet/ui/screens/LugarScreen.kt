
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
    onInicioClick: () -> Unit
) {
    val lugares by viewModel.lugares.collectAsStateWithLifecycle()

    var mostrarFormulario by remember { mutableStateOf(false) }
    var lugarEditar by remember { mutableStateOf<Lugar?>(null) }
    var lugarEliminar by remember { mutableStateOf<Lugar?>(null) }
    var nombre by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }

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

        Button(onClick = onInicioClick) {
            Text("Volver al inicio")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                nombre = ""
                lugarEditar = null
                mostrarFormulario = true
            }
        ) {
            Text("+ Registrar lugar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(lugares, key = { it.id }) { lugar ->
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
                                    mostrarFormulario = true
                                }
                            ) {
                                Text("Editar")
                            }

                            TextButton(
                                onClick = {
                                    lugarEliminar = lugar
                                }
                            ) {
                                Text("Eliminar")
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        AlertDialog(
            onDismissRequest = {
                mostrarFormulario = false
            },
            title = {
                Text(
                    if (lugarEditar == null)
                        "Registrar lugar"
                    else
                        "Editar lugar"
                )
            },
            text = {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del lugar") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val nombreLimpio = nombre.trim()

                        if (nombreLimpio.isBlank()) {
                            mensaje = "Ingresa el nombre del lugar"
                        } else {
                            val lugarActual = lugarEditar

                            viewModel.obtenerLugarPorNombre(nombreLimpio) {
                                    existente ->

                                if (
                                    existente != null &&
                                    existente.id != lugarActual?.id
                                ) {
                                    mensaje = "El lugar ya existe"
                                } else {
                                    val lugar = lugarActual?.copy(
                                        nombre = nombreLimpio
                                    ) ?: Lugar(nombre = nombreLimpio)

                                    val onResultado:
                                                (Boolean, String) -> Unit =
                                        { _, texto ->
                                            mensaje = texto
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

                            mostrarFormulario = false
                        }
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarFormulario = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    lugarEliminar?.let { lugar ->
        AlertDialog(
            onDismissRequest = {
                lugarEliminar = null
            },
            title = {
                Text("Eliminar lugar")
            },
            text = {
                Text("¿Deseas eliminar ${lugar.nombre}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarLugar(lugar) { _, texto ->
                            mensaje = texto
                        }
                        lugarEliminar = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        lugarEliminar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (mensaje.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { mensaje = "" },
            title = { Text("Información") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = { mensaje = "" }) {
                    Text("Aceptar")
                }
            }
        )
    }
}
