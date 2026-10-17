
package com.example.acameet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.ui.components.BottomNavigationBar
import com.example.acameet.ui.viewmodel.CategoriaMensaje
import com.example.acameet.ui.viewmodel.CategoriaUiState
import com.example.acameet.ui.viewmodel.CategoriaViewModel
import kotlinx.coroutines.flow.collect

private val AzulAcaMeet = Color(0xFF0798F2)
private val FondoTarjeta = Color(0xFF0A1C28)
private val GrisTexto = Color(0xFF9BA8B4)

// CONTENEDOR CON ESTADO
@Composable
fun CategoriaScreen(
    viewModel: CategoriaViewModel,
    onInicioClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var mostrarDialogoAgregar by remember {
        mutableStateOf(false)
    }

    var categoriaEditar by remember {
        mutableStateOf<Categoria?>(null)
    }

    var categoriaEliminar by remember {
        mutableStateOf<Categoria?>(null)
    }

    var mensaje by remember {
        mutableStateOf<String?>(null)
    }

    // Recibe mensajes puntuales del ViewModel.
    LaunchedEffect(viewModel) {
        viewModel.mensajes.collect { resultado: CategoriaMensaje ->
            mensaje = resultado.texto

            if (resultado.exitoso) {
                mostrarDialogoAgregar = false
                categoriaEditar = null
                categoriaEliminar = null
            }
        }
    }

    CategoriaContent(
        uiState = uiState,
        onInicioClick = onInicioClick,
        onAgregarClick = {
            mostrarDialogoAgregar = true
        },
        onEditarClick = { categoria ->
            categoriaEditar = categoria
        },
        onEliminarClick = { categoria ->
            categoriaEliminar = categoria
        }
    )

    // MENSAJE DE RESULTADO
    mensaje?.let { texto ->
        AlertDialog(
            onDismissRequest = {
                mensaje = null
            },
            containerColor = FondoTarjeta,
            titleContentColor = Color.White,
            textContentColor = GrisTexto,
            title = {
                Text(
                    text = "AcaMeet",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(texto)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mensaje = null
                    }
                ) {
                    Text(
                        text = "Aceptar",
                        color = AzulAcaMeet
                    )
                }
            }
        )
    }

    // AGREGAR CATEGORÍA
    if (mostrarDialogoAgregar) {
        DialogoCategoria(
            titulo = "Nueva categoría",
            nombreInicial = "",
            textoBoton = "Guardar",
            onDismiss = {
                mostrarDialogoAgregar = false
            },
            onConfirmar = { nombre ->
                viewModel.registrarCategoria(nombre)
            }
        )
    }

    // EDITAR CATEGORÍA
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
                )
            }
        )
    }

    // ELIMINAR CATEGORÍA
    categoriaEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = {
                categoriaEliminar = null
            },
            containerColor = FondoTarjeta,
            titleContentColor = Color.White,
            textContentColor = GrisTexto,
            title = {
                Text(
                    text = "Eliminar categoría",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Deseas eliminar la categoría \"${categoria.nombre}\"?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarCategoria(categoria)
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
                    Text(
                        text = "Cancelar",
                        color = GrisTexto
                    )
                }
            }
        )
    }
}

// VISTA SIN VIEWMODEL
@Composable
private fun CategoriaContent(
    uiState: CategoriaUiState,
    onInicioClick: () -> Unit,
    onAgregarClick: () -> Unit,
    onEditarClick: (Categoria) -> Unit,
    onEliminarClick: (Categoria) -> Unit
) {
    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF020B12),
            Color(0xFF03141F),
            Color(0xFF001D32)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavigationBar(
                pantallaActual = "categorias",
                onInicioClick = onInicioClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarClick,
                containerColor = AzulAcaMeet,
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
                    color = GrisTexto,
                    fontSize = 14.sp
                )

                Spacer(
                    modifier = Modifier.height(25.dp)
                )

                when (uiState) {
                    is CategoriaUiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = AzulAcaMeet
                            )
                        }
                    }

                    is CategoriaUiState.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.mensaje,
                                color = Color(0xFFE57373)
                            )
                        }
                    }

                    is CategoriaUiState.Success -> {
                        if (uiState.categorias.isEmpty()) {
                            CategoriaVacia(
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = uiState.categorias,
                                    key = { categoria ->
                                        categoria.id
                                    }
                                ) { categoria ->
                                    TarjetaCategoria(
                                        categoria = categoria,
                                        onEditarClick = {
                                            onEditarClick(categoria)
                                        },
                                        onEliminarClick = {
                                            onEliminarClick(categoria)
                                        }
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

// ESTADO SIN CATEGORÍAS
@Composable
private fun CategoriaVacia(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(75.dp),
                shape = RoundedCornerShape(20.dp),
                color = FondoTarjeta
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = AzulAcaMeet,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Text(
                text = "No hay categorías registradas",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Text(
                text = "Presiona + para crear tu primera categoría",
                color = GrisTexto,
                fontSize = 13.sp
            )
        }
    }
}

// TARJETA DE CATEGORÍA
@Composable
private fun TarjetaCategoria(
    categoria: Categoria,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = FondoTarjeta
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF102E42)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = AzulAcaMeet,
                        modifier = Modifier.size(27.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = categoria.nombre,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Categoría de evento",
                    color = GrisTexto,
                    fontSize = 13.sp
                )
            }

            IconButton(
                onClick = onEditarClick
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar categoría",
                    tint = AzulAcaMeet
                )
            }

            IconButton(
                onClick = onEliminarClick
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar categoría",
                    tint = Color(0xFFE57373)
                )
            }
        }
    }
}

// DIÁLOGO PARA AGREGAR Y EDITAR
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
        containerColor = FondoTarjeta,
        titleContentColor = Color.White,
        textContentColor = GrisTexto,
        title = {
            Text(titulo)
        },
        text = {
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre de la categoría")
                },
                placeholder = {
                    Text("Ej. Conferencia")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulAcaMeet,
                    focusedLabelColor = AzulAcaMeet,
                    focusedLeadingIconColor = AzulAcaMeet
                )
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
                    color = AzulAcaMeet
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar",
                    color = GrisTexto
                )
            }
        }
    )
}
