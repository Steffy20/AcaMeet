
package com.example.acameet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.ui.components.BottomNavigationBar
import com.example.acameet.ui.viewmodel.CategoriaUiState
import com.example.acameet.ui.viewmodel.CategoriaViewModel
import com.example.acameet.ui.viewmodel.LugarViewModel

private val AzulCL = Color(0xFF0798F2)

private enum class SeccionCL {
    CATEGORIAS,
    LUGARES
}

@Composable
fun CategoriaScreen(
    viewModel: CategoriaViewModel,
    lugarViewModel: LugarViewModel,
    onInicioClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {},
    usuarioIdActivo: Int? = null
) {
    val categoriaState by viewModel.uiState.collectAsStateWithLifecycle()
    val lugares by lugarViewModel.lugares.collectAsStateWithLifecycle()

    val categorias = when (val estado = categoriaState) {
        is CategoriaUiState.Success -> estado.categorias
        else -> emptyList()
    }

    var seccion by remember {
        mutableStateOf(SeccionCL.CATEGORIAS)
    }
    var formulario by remember {
        mutableStateOf(false)
    }
    var categoriaEditar by remember {
        mutableStateOf<Categoria?>(null)
    }
    var categoriaEliminar by remember {
        mutableStateOf<Categoria?>(null)
    }
    var lugarEditar by remember {
        mutableStateOf<Lugar?>(null)
    }
    var lugarEliminar by remember {
        mutableStateOf<Lugar?>(null)
    }
    var mensaje by remember {
        mutableStateOf<String?>(null)
    }
    var guardandoLugar by remember {
        mutableStateOf(false)
    }
    var eliminandoLugar by remember {
        mutableStateOf(false)
    }

    val procesandoCategoria =
        (categoriaState as? CategoriaUiState.Success)?.procesando == true

    // Limpiar formularios al cambiar de cuenta.
    LaunchedEffect(usuarioIdActivo) {
        formulario = false
        categoriaEditar = null
        categoriaEliminar = null
        lugarEditar = null
        lugarEliminar = null
        mensaje = null
        guardandoLugar = false
        eliminandoLugar = false
        seccion = SeccionCL.CATEGORIAS
    }

    // Escuchar mensajes del ViewModel de categorías.
    LaunchedEffect(viewModel, usuarioIdActivo) {
        if (usuarioIdActivo != null && usuarioIdActivo > 0) {
            viewModel.mensajes.collect { resultado ->
                mensaje = resultado.texto
                if (resultado.exitoso) {
                    formulario = false
                    categoriaEditar = null
                    categoriaEliminar = null
                }
            }
        }
    }

    val colores = MaterialTheme.colorScheme
    val esOscuro = colores.background.red < 0.5f

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
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onInicioClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver a inicio",
                        tint = colores.onBackground
                    )
                }

                Text(
                    text = "Categorías y lugares",
                    color = colores.onBackground,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Administra las categorías y lugares de tus eventos",
                color = colores.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(22.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BotonSeccionCL(
                    texto = "Categorías",
                    icono = Icons.Default.Category,
                    seleccionado = seccion == SeccionCL.CATEGORIAS,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        seccion = SeccionCL.CATEGORIAS
                        formulario = false
                        lugarEditar = null
                    }
                )

                BotonSeccionCL(
                    texto = "Lugares",
                    icono = Icons.Default.LocationOn,
                    seleccionado = seccion == SeccionCL.LUGARES,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        seccion = SeccionCL.LUGARES
                        formulario = false
                        categoriaEditar = null
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            when (seccion) {
                SeccionCL.CATEGORIAS -> {
                    Text(
                        text = "Categorías registradas",
                        color = colores.onBackground,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    when (val estado = categoriaState) {
                        is CategoriaUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AzulCL)
                            }
                        }

                        is CategoriaUiState.Error -> {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = estado.mensaje,
                                    color = colores.error
                                )
                            }
                        }

                        is CategoriaUiState.Success -> {
                            if (categorias.isEmpty()) {
                                EstadoVacioCL(
                                    texto = "No hay categorías registradas",
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement =
                                        Arrangement.spacedBy(10.dp),
                                    contentPadding =
                                        PaddingValues(bottom = 12.dp)
                                ) {
                                    items(
                                        items = categorias,
                                        key = { it.id }
                                    ) { categoria ->
                                        TarjetaRegistroCL(
                                            nombre = categoria.nombre,
                                            icono = Icons.Default.Category,
                                            onEditar = {
                                                categoriaEditar = categoria
                                                formulario = true
                                            },
                                            onEliminar = {
                                                categoriaEliminar = categoria
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            categoriaEditar = null
                            formulario = true
                        },
                        enabled = !procesandoCategoria,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulCL
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar categoría")
                    }
                }

                SeccionCL.LUGARES -> {
                    Text(
                        text = "Lugares registrados",
                        color = colores.onBackground,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    if (lugares.isEmpty()) {
                        EstadoVacioCL(
                            texto = "No hay lugares registrados",
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(10.dp),
                            contentPadding =
                                PaddingValues(bottom = 12.dp)
                        ) {
                            items(
                                items = lugares,
                                key = { it.id }
                            ) { lugar ->
                                TarjetaRegistroCL(
                                    nombre = lugar.nombre,
                                    icono = Icons.Default.LocationOn,
                                    onEditar = {
                                        lugarEditar = lugar
                                        formulario = true
                                    },
                                    onEliminar = {
                                        lugarEliminar = lugar
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            lugarEditar = null
                            formulario = true
                        },
                        enabled = !guardandoLugar,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzulCL
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Agregar lugar")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    // Formulario para registrar o editar categoría.
    if (formulario && seccion == SeccionCL.CATEGORIAS) {
        DialogoNombreCL(
            titulo = if (categoriaEditar == null) {
                "Nueva categoría"
            } else {
                "Editar categoría"
            },
            etiqueta = "Nombre de la categoría",
            nombreInicial = categoriaEditar?.nombre ?: "",
            nombresExistentes = categorias
                .filter { it.id != categoriaEditar?.id }
                .map { it.nombre },
            procesando = procesandoCategoria,
            onCancelar = {
                if (!procesandoCategoria) {
                    formulario = false
                    categoriaEditar = null
                }
            },
            onGuardar = { nombre ->
                if (!procesandoCategoria) {
                    val actual = categoriaEditar
                    if (actual == null) {
                        viewModel.registrarCategoria(nombre)
                    } else {
                        viewModel.actualizarCategoria(
                            categoria = actual,
                            nuevoNombre = nombre
                        )
                    }
                }
            }
        )
    }

    // Formulario para registrar o editar lugar.
    if (formulario && seccion == SeccionCL.LUGARES) {
        DialogoNombreCL(
            titulo = if (lugarEditar == null) {
                "Nuevo lugar"
            } else {
                "Editar lugar"
            },
            etiqueta = "Nombre del lugar",
            nombreInicial = lugarEditar?.nombre ?: "",
            nombresExistentes = lugares
                .filter { it.id != lugarEditar?.id }
                .map { it.nombre },
            procesando = guardandoLugar,
            onCancelar = {
                if (!guardandoLugar) {
                    formulario = false
                    lugarEditar = null
                }
            },
            onGuardar = { nombre ->
                if (!guardandoLugar) {
                    guardandoLugar = true
                    val actual = lugarEditar
                    val usuarioOperacion = usuarioIdActivo

                    val callback: (Boolean, String) -> Unit =
                        { exitoso, texto ->
                            if (usuarioOperacion == usuarioIdActivo) {
                                guardandoLugar = false
                                mensaje = texto
                                if (exitoso) {
                                    formulario = false
                                    lugarEditar = null
                                }
                            }
                        }

                    if (actual == null) {
                        lugarViewModel.registrarLugar(
                            Lugar(nombre = nombre),
                            callback
                        )
                    } else {
                        lugarViewModel.actualizarLugar(
                            actual.copy(nombre = nombre),
                            callback
                        )
                    }
                }
            }
        )
    }

    // Confirmación de eliminación de categoría.
    categoriaEliminar?.let { categoria ->
        DialogoEliminarCL(
            tipo = "categoría",
            nombre = categoria.nombre,
            procesando = procesandoCategoria,
            onCancelar = {
                if (!procesandoCategoria) {
                    categoriaEliminar = null
                }
            },
            onConfirmar = {
                if (!procesandoCategoria) {
                    viewModel.eliminarCategoria(categoria)
                }
            }
        )
    }

    // Confirmación de eliminación de lugar.
    lugarEliminar?.let { lugar ->
        DialogoEliminarCL(
            tipo = "lugar",
            nombre = lugar.nombre,
            procesando = eliminandoLugar,
            onCancelar = {
                if (!eliminandoLugar) {
                    lugarEliminar = null
                }
            },
            onConfirmar = {
                if (!eliminandoLugar) {
                    eliminandoLugar = true
                    val usuarioOperacion = usuarioIdActivo

                    lugarViewModel.eliminarLugar(lugar) {
                            exitoso, texto ->
                        if (usuarioOperacion == usuarioIdActivo) {
                            eliminandoLugar = false
                            mensaje = texto
                            if (exitoso) {
                                lugarEliminar = null
                            }
                        }
                    }
                }
            }
        )
    }

    // Mensajes de confirmación y errores.
    mensaje?.let { texto ->
        AlertDialog(
            onDismissRequest = {
                mensaje = null
            },
            title = {
                Text("AcaMeet")
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
                        "Aceptar",
                        color = AzulCL
                    )
                }
            }
        )
    }
}

@Composable
private fun BotonSeccionCL(
    texto: String,
    icono: ImageVector,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) {
                AzulCL
            } else {
                colores.surfaceVariant
            },
            contentColor = if (seleccionado) {
                Color.White
            } else {
                colores.onSurfaceVariant
            }
        )
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = texto,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun TarjetaRegistroCL(
    nombre: String,
    icono: ImageVector,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = colores.surface
        ),
        border = BorderStroke(
            1.dp,
            AzulCL.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = AzulCL,
                modifier = Modifier.size(27.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = nombre,
                modifier = Modifier.weight(1f),
                color = colores.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = onEditar) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = AzulCL
                )
            }

            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = colores.error
                )
            }
        }
    }
}

@Composable
private fun EstadoVacioCL(
    texto: String,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = texto,
            color = colores.onSurfaceVariant
        )
    }
}

@Composable
private fun DialogoNombreCL(
    titulo: String,
    etiqueta: String,
    nombreInicial: String,
    nombresExistentes: List<String>,
    procesando: Boolean = false,
    onCancelar: () -> Unit,
    onGuardar: (String) -> Unit
) {
    var nombre by remember(nombreInicial, titulo) {
        mutableStateOf(nombreInicial)
    }

    var error by remember(nombreInicial, titulo) {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = {
            if (!procesando) {
                onCancelar()
            }
        },
        title = {
            Text(titulo)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = {
                        nombre = it
                        error = null
                    },
                    label = {
                        Text(etiqueta)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !procesando
                )

                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !procesando,
                onClick = {
                    val limpio = nombre
                        .trim()
                        .replace(Regex("\\s+"), " ")

                    when {
                        limpio.isBlank() -> {
                            error = "El nombre es obligatorio"
                        }

                        nombresExistentes.any {
                            it.trim().equals(
                                limpio,
                                ignoreCase = true
                            )
                        } -> {
                            error = "Ya existe un registro con ese nombre"
                        }

                        else -> {
                            onGuardar(limpio)
                        }
                    }
                }
            ) {
                Text(
                    text = if (procesando) {
                        "Guardando..."
                    } else {
                        "Guardar"
                    },
                    color = AzulCL
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !procesando,
                onClick = onCancelar
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun DialogoEliminarCL(
    tipo: String,
    nombre: String,
    procesando: Boolean = false,
    onCancelar: () -> Unit,
    onConfirmar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    AlertDialog(
        onDismissRequest = {
            if (!procesando) {
                onCancelar()
            }
        },
        title = {
            Text("Eliminar $tipo")
        },
        text = {
            Text(
                "¿Deseas eliminar \"$nombre\"? " +
                        "Esta acción no se puede deshacer."
            )
        },
        confirmButton = {
            TextButton(
                enabled = !procesando,
                onClick = onConfirmar
            ) {
                Text(
                    text = if (procesando) {
                        "Eliminando..."
                    } else {
                        "Eliminar"
                    },
                    color = colores.error
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !procesando,
                onClick = onCancelar
            ) {
                Text("Cancelar")
            }
        }
    )
}
