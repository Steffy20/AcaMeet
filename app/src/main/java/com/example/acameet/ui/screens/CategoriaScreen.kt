package com.example.acameet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.ui.viewmodel.CategoriaViewModel

@Composable
fun CategoriaScreen(
    viewModel: CategoriaViewModel
) {

    val categorias by viewModel.categorias.collectAsState()

    var mostrarDialogoAgregar by remember {
        mutableStateOf(false)
    }

    var categoriaEditar by remember {
        mutableStateOf<Categoria?>(null)
    }

    var categoriaEliminar by remember {
        mutableStateOf<Categoria?>(null)
    }

    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF020B12),
            Color(0xFF03141F),
            Color(0xFF001D32)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    mostrarDialogoAgregar = true
                },
                containerColor = Color(0xFF0798F2),
                contentColor = Color.White
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar categoría"
                )
            }
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(paddingValues)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
            ) {

                Spacer(
                    modifier = Modifier.height(35.dp)
                )

                Text(
                    text = "Categorías",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                Text(
                    text = "Administra las categorías de tus eventos",
                    color = Color(0xFF9BA8B4),
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(25.dp)
                )

                if (categorias.isEmpty()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "No hay categorías registradas",
                            color = Color(0xFF9BA8B4),
                            fontSize = 15.sp
                        )
                    }

                } else {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {

                        items(
                            items = categorias,
                            key = { categoria ->
                                categoria.id
                            }
                        ) { categoria ->

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor =
                                        Color(0xFF0A1C28)
                                )
                            ) {

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment =
                                        Alignment.CenterVertically
                                ) {

                                    Text(
                                        text = categoria.nombre,
                                        modifier =
                                            Modifier.weight(1f),
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight =
                                            FontWeight.Medium
                                    )

                                    IconButton(
                                        onClick = {
                                            categoriaEditar =
                                                categoria
                                        }
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Default.Edit,
                                            contentDescription =
                                                "Editar",
                                            tint =
                                                Color(0xFF0798F2)
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            categoriaEliminar =
                                                categoria
                                        }
                                    ) {

                                        Icon(
                                            imageVector =
                                                Icons.Default.Delete,
                                            contentDescription =
                                                "Eliminar",
                                            tint =
                                                Color(0xFFE57373)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para agregar categoría
    if (mostrarDialogoAgregar) {

        DialogoCategoria(
            titulo = "Nueva categoría",
            nombreInicial = "",
            textoBoton = "Guardar",
            onDismiss = {
                mostrarDialogoAgregar = false
            },
            onConfirmar = { nombre ->

                viewModel.registrarCategoria(nombre) {
                        exitoso, _ ->

                    if (exitoso) {
                        mostrarDialogoAgregar = false
                    }
                }
            }
        )
    }

    // Diálogo para editar categoría
    categoriaEditar?.let { categoria ->

        DialogoCategoria(
            titulo = "Editar categoría",
            nombreInicial = categoria.nombre,
            textoBoton = "Actualizar",
            onDismiss = {
                categoriaEditar = null
            },
            onConfirmar = { nuevoNombre ->

                viewModel.actualizarCategoria(
                    categoria = categoria,
                    nuevoNombre = nuevoNombre
                ) { exitoso, _ ->

                    if (exitoso) {
                        categoriaEditar = null
                    }
                }
            }
        )
    }

    // Confirmación para eliminar
    categoriaEliminar?.let { categoria ->

        AlertDialog(
            onDismissRequest = {
                categoriaEliminar = null
            },

            title = {
                Text(
                    text = "Eliminar categoría"
                )
            },

            text = {
                Text(
                    text =
                        "¿Deseas eliminar la categoría " +
                                "\"${categoria.nombre}\"?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        viewModel.eliminarCategoria(
                            categoria
                        ) { _, _ ->

                            categoriaEliminar = null
                        }
                    }
                ) {

                    Text(
                        text = "Eliminar",
                        color = Color(0xFFE53935)
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        categoriaEliminar = null
                    }
                ) {

                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun DialogoCategoria(
    titulo: String,
    nombreInicial: String,
    textoBoton: String,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {

    var nombre by remember(nombreInicial) {
        mutableStateOf(nombreInicial)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(titulo)
        },

        text = {

            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                },
                label = {
                    Text("Nombre de la categoría")
                },
                singleLine = true
            )
        },

        confirmButton = {

            TextButton(
                onClick = {
                    onConfirmar(nombre)
                }
            ) {

                Text(
                    text = textoBoton,
                    color = Color(0xFF0798F2)
                )
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancelar")
            }
        }
    )
}