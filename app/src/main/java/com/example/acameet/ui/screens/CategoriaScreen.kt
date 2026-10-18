
package com.example.acameet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
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

private val AzulAcaMeet = Color(0xFF0798F2)

@Composable
fun CategoriaRoute(
    viewModel: CategoriaViewModel,
    onInicioClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var pantalla by remember {
        mutableStateOf("lista")
    }

    var categoriaSeleccionada by remember {
        mutableStateOf<Categoria?>(null)
    }

    var categoriaEliminar by remember {
        mutableStateOf<Categoria?>(null)
    }

    var mensaje by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(viewModel) {
        viewModel.mensajes.collect { resultado: CategoriaMensaje ->
            mensaje = resultado.texto

            if (resultado.exitoso) {
                pantalla = "lista"
                categoriaSeleccionada = null
                categoriaEliminar = null
            }
        }
    }

    CategoriaContent(
        uiState = uiState,
        pantalla = pantalla,
        categoriaSeleccionada = categoriaSeleccionada,
        categoriaEliminar = categoriaEliminar,
        mensaje = mensaje,
        onPantallaChange = { pantalla = it },
        onCategoriaSeleccionadaChange = {
            categoriaSeleccionada = it
        },
        onCategoriaEliminarChange = {
            categoriaEliminar = it
        },
        onMensajeChange = { mensaje = it },
        onRegistrarCategoria = viewModel::registrarCategoria,
        onActualizarCategoria = { categoria, nombre ->
            viewModel.actualizarCategoria(categoria, nombre)
        },
        onEliminarCategoria = viewModel::eliminarCategoria,
        onInicioClick = onInicioClick,
        onEventosClick = onEventosClick,
        onConfiguracionClick = onConfiguracionClick
    )
}

@Composable
fun CategoriaContent(
    uiState: CategoriaUiState,
    pantalla: String,
    categoriaSeleccionada: Categoria?,
    categoriaEliminar: Categoria?,
    mensaje: String?,
    onPantallaChange: (String) -> Unit,
    onCategoriaSeleccionadaChange: (Categoria?) -> Unit,
    onCategoriaEliminarChange: (Categoria?) -> Unit,
    onMensajeChange: (String?) -> Unit,
    onRegistrarCategoria: (String) -> Unit,
    onActualizarCategoria: (Categoria, String) -> Unit,
    onEliminarCategoria: (Categoria) -> Unit,
    onInicioClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme

    val esOscuro =
        colores.background == Color(0xFF020B12)

    val fondo = if (esOscuro) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF020B12),
                Color(0xFF03141F),
                Color(0xFF001D32)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFF3F7FA),
                Color(0xFFEDF5FA),
                Color(0xFFE2F0F9)
            )
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavigationBar(
                pantallaActual = "categorias",
                onInicioClick = onInicioClick,
                onEventosClick = onEventosClick,
                onConfiguracionClick = onConfiguracionClick
            )
        },
        floatingActionButton = {
            if (pantalla == "lista") {
                FloatingActionButton(
                    onClick = {
                        onCategoriaSeleccionadaChange(null)
                        onPantallaChange("crear")
                    },
                    containerColor = AzulAcaMeet,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear categoría"
                    )
                }
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(paddingValues)
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            when (pantalla) {

                "lista" -> {
                    Text(
                        text = "Categorías",
                        color = colores.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Administra las categorías de tus eventos",
                        color = colores.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    when (val estado = uiState) {

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
                                    text = estado.mensaje,
                                    color = colores.error
                                )
                            }
                        }

                        is CategoriaUiState.Success -> {
                            if (estado.categorias.isEmpty()) {
                                CategoriaVacia(
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement =
                                        Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(
                                        bottom = 90.dp
                                    )
                                ) {
                                    items(
                                        items = estado.categorias,
                                        key = { it.id }
                                    ) { categoria ->
                                        TarjetaCategoria(
                                            categoria = categoria,
                                            onDetalleClick = {
                                                onCategoriaSeleccionadaChange(
                                                    categoria
                                                )
                                                onPantallaChange("detalle")
                                            },
                                            onEditarClick = {
                                                onCategoriaSeleccionadaChange(
                                                    categoria
                                                )
                                                onPantallaChange("editar")
                                            },
                                            onEliminarClick = {
                                                onCategoriaEliminarChange(
                                                    categoria
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "crear" -> {
                    EncabezadoCategoria(
                        titulo = "Crear categoría",
                        onVolver = {
                            onPantallaChange("lista")
                        }
                    )

                    FormularioCategoria(
                        categoriaInicial = null,
                        procesando = (
                                (uiState as? CategoriaUiState.Success)
                                    ?.procesando == true
                                ),
                        onGuardar = { nombre ->
                            onRegistrarCategoria(nombre)
                        },
                        onCancelar = {
                            onPantallaChange("lista")
                        }
                    )
                }

                "editar" -> {
                    val categoria = categoriaSeleccionada

                    if (categoria != null) {
                        EncabezadoCategoria(
                            titulo = "Editar categoría",
                            onVolver = {
                                onPantallaChange("detalle")
                            }
                        )

                        FormularioCategoria(
                            categoriaInicial = categoria,
                            procesando = (
                                    (uiState as? CategoriaUiState.Success)
                                        ?.procesando == true
                                    ),
                            onGuardar = { nuevoNombre ->
                                onActualizarCategoria(
                                    categoria,
                                    nuevoNombre
                                )
                            },
                            onCancelar = {
                                onPantallaChange("detalle")
                            }
                        )
                    }
                }

                "detalle" -> {
                    val categoria = categoriaSeleccionada

                    if (categoria != null) {
                        EncabezadoCategoria(
                            titulo = "Detalle de categoría",
                            onVolver = {
                                onPantallaChange("lista")
                            }
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(
                                    rememberScrollState()
                                ),
                            verticalArrangement =
                                Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colores.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement =
                                        Arrangement.spacedBy(14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = AzulAcaMeet,
                                        modifier = Modifier.size(48.dp)
                                    )

                                    Text(
                                        text = categoria.nombre,
                                        color = colores.onSurface,
                                        fontSize = 23.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    HorizontalDivider(
                                        color = colores.outline
                                    )

                                    Text(
                                        text = "Categoría de evento académico",
                                        color = colores.onSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    onPantallaChange("editar")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AzulAcaMeet
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null
                                )

                                Spacer(Modifier.width(8.dp))

                                Text("Editar categoría")
                            }

                            OutlinedButton(
                                onClick = {
                                    onCategoriaEliminarChange(categoria)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = colores.error
                                )

                                Spacer(Modifier.width(8.dp))

                                Text(
                                    "Eliminar categoría",
                                    color = colores.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // CONFIRMACIÓN PARA ELIMINAR
    categoriaEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = {
                onCategoriaEliminarChange(null)
            },
            containerColor = colores.surface,
            titleContentColor = colores.onSurface,
            textContentColor = colores.onSurfaceVariant,
            title = {
                Text(
                    "Eliminar categoría",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "¿Deseas eliminar la categoría " +
                            "\"${categoria.nombre}\"?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEliminarCategoria(categoria)
                    }
                ) {
                    Text(
                        "Eliminar",
                        color = colores.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onCategoriaEliminarChange(null)
                    }
                ) {
                    Text(
                        "Cancelar",
                        color = colores.onSurfaceVariant
                    )
                }
            }
        )
    }

    // MENSAJES DEL VIEWMODEL
    mensaje?.let { texto ->
        AlertDialog(
            onDismissRequest = {
                onMensajeChange(null)
            },
            containerColor = colores.surface,
            titleContentColor = colores.onSurface,
            textContentColor = colores.onSurfaceVariant,
            title = {
                Text(
                    "AcaMeet",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(texto)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onMensajeChange(null)
                    }
                ) {
                    Text(
                        "Aceptar",
                        color = AzulAcaMeet
                    )
                }
            }
        )
    }
}

// COMPATIBILIDAD CON APPNAVIGATION
@Composable
fun CategoriaScreen(
    viewModel: CategoriaViewModel,
    onInicioClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {}
) {
    CategoriaRoute(
        viewModel = viewModel,
        onInicioClick = onInicioClick,
        onEventosClick = onEventosClick,
        onConfiguracionClick = onConfiguracionClick
    )
}

@Composable
private fun EncabezadoCategoria(
    titulo: String,
    onVolver: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = AzulAcaMeet
            )
        }

        Text(
            text = titulo,
            color = colores.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }

    Spacer(modifier = Modifier.height(18.dp))
}

@Composable
private fun FormularioCategoria(
    categoriaInicial: Categoria?,
    procesando: Boolean,
    onGuardar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    var nombre by remember(categoriaInicial) {
        mutableStateOf(categoriaInicial?.nombre ?: "")
    }

    var error by remember(categoriaInicial) {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Información de la categoría",
            color = AzulAcaMeet,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = colores.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement =
                    Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Completa los datos para " +
                            if (categoriaInicial == null) {
                                "registrar una nueva categoría."
                            } else {
                                "actualizar esta categoría."
                            },
                    color = colores.onSurfaceVariant,
                    fontSize = 14.sp
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        error = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text("Nombre de la categoría *")
                    },
                    placeholder = {
                        Text("Ej. Conferencias")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = AzulAcaMeet
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulAcaMeet,
                        focusedLabelColor = AzulAcaMeet,
                        focusedLeadingIconColor = AzulAcaMeet,
                        focusedTextColor = colores.onSurface,
                        unfocusedTextColor = colores.onSurface,
                        focusedPlaceholderColor =
                            colores.onSurfaceVariant,
                        unfocusedPlaceholderColor =
                            colores.onSurfaceVariant,
                        unfocusedBorderColor = colores.outline,
                        unfocusedLabelColor =
                            colores.onSurfaceVariant,
                        cursorColor = AzulAcaMeet
                    )
                )

                error?.let {
                    Text(
                        text = it,
                        color = colores.error,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Button(
            onClick = {
                val nombreLimpio = nombre
                    .trim()
                    .replace(Regex("\\s+"), " ")

                if (nombreLimpio.isBlank()) {
                    error = "Ingresa el nombre de la categoría"
                } else {
                    onGuardar(nombreLimpio)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !procesando,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulAcaMeet
            )
        ) {
            if (procesando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (categoriaInicial == null) {
                        Icons.Default.Add
                    } else {
                        Icons.Default.Edit
                    },
                    contentDescription = null
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    if (categoriaInicial == null) {
                        "Guardar categoría"
                    } else {
                        "Guardar cambios"
                    }
                )
            }
        }

        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth(),
            enabled = !procesando,
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                "Cancelar",
                color = colores.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CategoriaVacia(
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme

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
                color = colores.surface
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

            Spacer(Modifier.height(18.dp))

            Text(
                text = "No hay categorías registradas",
                color = colores.onBackground,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = "Presiona + para crear tu primera categoría",
                color = colores.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TarjetaCategoria(
    categoria: Categoria,
    onDetalleClick: () -> Unit,
    onEditarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    val esOscuro =
        colores.background == Color(0xFF020B12)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetalleClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = colores.surface
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
                color = if (esOscuro) {
                    Color(0xFF102E42)
                } else {
                    Color(0xFFE2F0F9)
                }
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

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = categoria.nombre,
                    color = colores.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Categoría de evento",
                    color = colores.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }

            IconButton(onClick = onEditarClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar categoría",
                    tint = AzulAcaMeet
                )
            }

            IconButton(onClick = onEliminarClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar categoría",
                    tint = colores.error
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = AzulAcaMeet
            )
        }
    }
}
