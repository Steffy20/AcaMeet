
package com.example.acameet.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.local.entity.Evento
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.ui.EventoUiState
import com.example.acameet.ui.EventoViewModel
import com.example.acameet.ui.ImagenEventoUtils
import com.example.acameet.ui.components.BottomNavigationBar
import com.example.acameet.ui.viewmodel.CategoriaMensaje
import com.example.acameet.ui.viewmodel.CategoriaUiState
import com.example.acameet.ui.viewmodel.CategoriaViewModel
import com.example.acameet.ui.viewmodel.LugarViewModel
import java.util.Calendar
import java.util.Locale

private val AzulEv = Color(0xFF0798F2)
private val RojoEv = Color(0xFFE57373)

// =====================================================
// PANTALLA PRINCIPAL
// =====================================================

@Composable
fun EventoScreen(
    viewModel: EventoViewModel,
    categoriaViewModel: CategoriaViewModel,
    lugarViewModel: LugarViewModel,
    pantallaInicial: String = "lista",
    eventoIdInicial: Int? = null,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {},
    usuarioIdActivo: Int? = null
) {
    EventoRoute(
        viewModel = viewModel,
        categoriaViewModel = categoriaViewModel,
        lugarViewModel = lugarViewModel,
        pantallaInicial = pantallaInicial,
        eventoIdInicial = eventoIdInicial,
        onInicioClick = onInicioClick,
        onCategoriasClick = onCategoriasClick,
        onConfiguracionClick = onConfiguracionClick,
        usuarioIdActivo = usuarioIdActivo
    )
}

// =====================================================
// CONEXIÓN CON LOS VIEWMODELS
// =====================================================

@Composable
fun EventoRoute(
    viewModel: EventoViewModel,
    categoriaViewModel: CategoriaViewModel,
    lugarViewModel: LugarViewModel,
    pantallaInicial: String = "lista",
    eventoIdInicial: Int? = null,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {},
    usuarioIdActivo: Int? = null
) {
    val uiStateOriginal by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val categoriaState by categoriaViewModel.uiState.collectAsStateWithLifecycle()
    val lugaresOriginales by lugarViewModel.lugares.collectAsStateWithLifecycle()

    val categorias = if (usuarioIdActivo != null) {
        (categoriaState as? CategoriaUiState.Success)
            ?.categorias
            ?.filter { it.usuarioId == usuarioIdActivo }
            ?: emptyList()
    } else {
        emptyList()
    }

    val lugares = if (usuarioIdActivo != null) {
        lugaresOriginales.filter { it.usuarioId == usuarioIdActivo }
    } else {
        emptyList()
    }

    val uiState: EventoUiState =
        when (val estado = uiStateOriginal) {
            is EventoUiState.Success -> {
                EventoUiState.Success(
                    eventos = estado.eventos.filter {
                        usuarioIdActivo != null &&
                                it.usuarioId == usuarioIdActivo
                    }
                )
            }
            else -> estado
        }

    var pantalla by remember(
        pantallaInicial,
        eventoIdInicial,
        usuarioIdActivo
    ) {
        mutableStateOf(pantallaInicial)
    }

    var seleccionado by remember(usuarioIdActivo) {
        mutableStateOf<Evento?>(null)
    }

    var eliminar by remember(usuarioIdActivo) {
        mutableStateOf<Evento?>(null)
    }

    var mensaje by remember(usuarioIdActivo) {
        mutableStateOf<String?>(null)
    }

    var filtroCategoria by remember(usuarioIdActivo) {
        mutableStateOf<Int?>(null)
    }

    var resultadoRegistroCategoria by remember(usuarioIdActivo) {
        mutableStateOf<CategoriaMensaje?>(null)
    }

    var guardandoEvento by remember(usuarioIdActivo) {
        mutableStateOf(false)
    }

    var eliminandoEvento by remember(usuarioIdActivo) {
        mutableStateOf(false)
    }

    // Limpiar los filtros al cambiar de cuenta.
    LaunchedEffect(usuarioIdActivo) {
        viewModel.limpiarFiltros()
    }

    // Abrir el detalle de un evento desde Inicio.
    LaunchedEffect(
        eventoIdInicial,
        usuarioIdActivo
    ) {
        if (
            eventoIdInicial != null &&
            usuarioIdActivo != null
        ) {
            try {
                val evento = viewModel.obtenerEventoParaPantalla(
                    eventoIdInicial
                )

                seleccionado = evento?.takeIf {
                    it.usuarioId == usuarioIdActivo
                }

                pantalla = if (seleccionado != null) {
                    "detalle"
                } else {
                    "lista"
                }

            } catch (e: Exception) {
                seleccionado = null
                pantalla = "lista"
                mensaje = "No se pudo cargar el evento"
            }
        }
    }

    // Escuchar resultados de las operaciones.
    LaunchedEffect(viewModel, usuarioIdActivo) {
        if (usuarioIdActivo != null) {
            viewModel.mensajes.collect { resultado ->
                guardandoEvento = false
                eliminandoEvento = false
                mensaje = resultado.texto

                if (resultado.exitoso) {
                    pantalla = "lista"
                    seleccionado = null
                    eliminar = null
                }
            }
        }
    }

    LaunchedEffect(categoriaViewModel, usuarioIdActivo) {
        if (usuarioIdActivo != null) {
            categoriaViewModel.mensajes.collect { resultado ->
                resultadoRegistroCategoria = resultado
            }
        }
    }

    EventoContent(
        uiState = uiState,
        busqueda = busqueda,
        categorias = categorias,
        lugares = lugares,
        pantalla = pantalla,
        seleccionado = seleccionado,
        eliminar = eliminar,
        mensaje = mensaje,
        categoriaFiltro = filtroCategoria,
        guardandoEvento = guardandoEvento,
        eliminandoEvento = eliminandoEvento,
        usuarioIdActivo = usuarioIdActivo,

        onPantallaChange = {
            pantalla = it
        },

        onSeleccionadoChange = {
            seleccionado = it
        },

        onEliminarChange = {
            eliminar = it
        },

        onMensajeChange = {
            mensaje = it
        },

        onCategoriaFiltroChange = {
            filtroCategoria = it
        },

        onBusquedaChange = viewModel::actualizarBusqueda,

        resultadoRegistroCategoria = resultadoRegistroCategoria,
        onLimpiarResultadoCategoria = {
            resultadoRegistroCategoria = null
        },
        onRegistrarCategoria = { nombre ->
            if (usuarioIdActivo != null) {
                resultadoRegistroCategoria = null
                categoriaViewModel.registrarCategoria(nombre)
            } else {
                resultadoRegistroCategoria = CategoriaMensaje(
                    false, "Debes iniciar sesión"
                )
            }
        },

        onRegistrarLugar = { nombre, resultado ->
            if (usuarioIdActivo != null) {
                lugarViewModel.registrarLugar(
                    Lugar(nombre = nombre),
                    resultado
                )
            } else {
                resultado(false, "Debes iniciar sesión")
            }
        },

        onGuardarEvento = {
                inicial, nombre, descripcion, fecha,
                hora, categoriaId, lugarId ->

            if (
                usuarioIdActivo != null &&
                !guardandoEvento
            ) {
                val categoriaValida = categorias.any {
                    it.id == categoriaId
                }

                val lugarValido = lugares.any {
                    it.id == lugarId
                }

                if (!categoriaValida || !lugarValido) {
                    mensaje = "Selecciona una categoría y un lugar válidos"
                } else {
                    guardandoEvento = true

                    if (inicial == null) {
                        viewModel.registrarEvento(
                            nombre = nombre,
                            descripcion = descripcion,
                            fecha = fecha,
                            hora = hora,
                            categoriaId = categoriaId,
                            lugarId = lugarId,
                            imagenUrl = ""
                        )
                    } else if (
                        inicial.usuarioId == usuarioIdActivo
                    ) {
                        viewModel.actualizarEvento(
                            evento = inicial,
                            nombre = nombre,
                            descripcion = descripcion,
                            fecha = fecha,
                            hora = hora,
                            categoriaId = categoriaId,
                            lugarId = lugarId,
                            imagenUrl = inicial.imagenUrl
                        )
                    } else {
                        guardandoEvento = false
                        mensaje = "No puedes editar este evento"
                    }
                }
            }
        },

        onEliminarEvento = { evento ->
            if (
                usuarioIdActivo != null &&
                evento.usuarioId == usuarioIdActivo &&
                !eliminandoEvento
            ) {
                eliminandoEvento = true
                viewModel.eliminarEvento(evento)
            }
        },

        onInicioClick = onInicioClick,
        onCategoriasClick = onCategoriasClick,
        onConfiguracionClick = onConfiguracionClick
    )
}

// =====================================================
// CONTENIDO DE EVENTOS
// =====================================================

@Composable
fun EventoContent(
    uiState: EventoUiState,
    busqueda: String,
    categorias: List<Categoria>,
    lugares: List<Lugar>,
    pantalla: String,
    seleccionado: Evento?,
    eliminar: Evento?,
    mensaje: String?,
    categoriaFiltro: Int?,
    onPantallaChange: (String) -> Unit,
    onSeleccionadoChange: (Evento?) -> Unit,
    onEliminarChange: (Evento?) -> Unit,
    onMensajeChange: (String?) -> Unit,
    onCategoriaFiltroChange: (Int?) -> Unit,
    onBusquedaChange: (String) -> Unit,
    onRegistrarLugar: (
        String,
        (Boolean, String) -> Unit
    ) -> Unit,
    resultadoRegistroCategoria: CategoriaMensaje? = null,
    onLimpiarResultadoCategoria: () -> Unit = {},
    onRegistrarCategoria: (String) -> Unit = {},
    onGuardarEvento: (
        Evento?, String, String, String, String, Int, Int
    ) -> Unit,
    onEliminarEvento: (Evento) -> Unit,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {},
    guardandoEvento: Boolean = false,
    eliminandoEvento: Boolean = false,
    usuarioIdActivo: Int? = null
) {
    val colores = MaterialTheme.colorScheme
    val oscuro = colores.background.red < 0.5f

    val fondo = Brush.verticalGradient(
        if (oscuro) {
            listOf(
                Color(0xFF020B12),
                Color(0xFF03141F),
                Color(0xFF001D32)
            )
        } else {
            listOf(
                Color(0xFFF3F7FA),
                Color(0xFFEDF5FA),
                Color(0xFFE2F0F9)
            )
        }
    )

    Scaffold(
        containerColor = Color.Transparent,

        bottomBar = {
            BottomNavigationBar(
                pantallaActual = "eventos",
                onInicioClick = onInicioClick,
                onCategoriasClick = onCategoriasClick,
                onConfiguracionClick = onConfiguracionClick
            )
        },

        floatingActionButton = {
            if (
                pantalla == "lista" &&
                usuarioIdActivo != null
            ) {
                FloatingActionButton(
                    onClick = {
                        onSeleccionadoChange(null)
                        onPantallaChange("crear")
                    },
                    containerColor = AzulEv,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Crear evento"
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(padding)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            when (pantalla) {

                // LISTA DE EVENTOS
                "lista" -> {
                    Text(
                        text = "Eventos",
                        color = colores.onBackground,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = "Descubre y participa en eventos académicos",
                        color = colores.onSurfaceVariant,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = onBusquedaChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Buscar eventos...",
                                fontSize = 13.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Buscar",
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulEv,
                            unfocusedBorderColor =
                                AzulEv.copy(alpha = 0.28f),
                            focusedTextColor = colores.onBackground,
                            unfocusedTextColor = colores.onBackground,
                            focusedPlaceholderColor =
                                colores.onSurfaceVariant,
                            unfocusedPlaceholderColor =
                                colores.onSurfaceVariant,
                            cursorColor = AzulEv
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    SelectorCategoriaFiltroEv(
                        categorias = categorias,
                        seleccion = categoriaFiltro,
                        onSeleccionar = onCategoriaFiltroChange
                    )

                    Spacer(Modifier.height(18.dp))

                    when (val estado = uiState) {
                        is EventoUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = AzulEv
                                )
                            }
                        }

                        is EventoUiState.Error -> {
                            Text(
                                text = estado.mensaje,
                                color = RojoEv
                            )
                        }

                        is EventoUiState.Success -> {
                            val eventos = estado.eventos.filter {
                                categoriaFiltro == null ||
                                        it.categoriaId == categoriaFiltro
                            }

                            if (eventos.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No hay eventos para mostrar",
                                        color = colores.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement =
                                        Arrangement.spacedBy(12.dp),
                                    contentPadding =
                                        PaddingValues(bottom = 90.dp)
                                ) {
                                    items(
                                        items = eventos,
                                        key = { it.id }
                                    ) { evento ->
                                        val categoria =
                                            categorias.find {
                                                it.id == evento.categoriaId
                                            }?.nombre ?: "Evento"

                                        val lugar =
                                            lugares.find {
                                                it.id == evento.lugarId
                                            }?.nombre
                                                ?: "Lugar no disponible"

                                        TarjetaEventoEv(
                                            evento = evento,
                                            categoria = categoria,
                                            lugar = lugar,
                                            onClick = {
                                                onSeleccionadoChange(evento)
                                                onPantallaChange("detalle")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // DETALLE DEL EVENTO
                "detalle" -> {
                    val evento = seleccionado

                    if (
                        evento != null &&
                        evento.usuarioId == usuarioIdActivo
                    ) {
                        val categoria = categorias.find {
                            it.id == evento.categoriaId
                        }?.nombre ?: "Sin categoría"

                        val lugar = lugares.find {
                            it.id == evento.lugarId
                        }?.nombre ?: "Lugar no disponible"

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    onPantallaChange("lista")
                                }
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = colores.onBackground
                                )
                            }

                            Text(
                                text = "Detalle del evento",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = colores.onBackground
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            ImagenEventoEv(
                                nombre = evento.nombre,
                                categoria = categoria,
                                imagenUrl = evento.imagenUrl,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(185.dp)
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colores.surface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    AzulEv.copy(alpha = 0.22f)
                                )
                            ) {
                                Column {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment =
                                                Alignment.CenterVertically,
                                            horizontalArrangement =
                                                Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Description,
                                                contentDescription = null,
                                                tint = AzulEv,
                                                modifier = Modifier.size(25.dp)
                                            )

                                            Text(
                                                text = evento.nombre,
                                                color = colores.onSurface,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(Modifier.height(12.dp))

                                        Text(
                                            text = evento.descripcion,
                                            color = colores.onSurfaceVariant,
                                            fontSize = 13.sp,
                                            lineHeight = 19.sp
                                        )
                                    }

                                    HorizontalDivider(
                                        color = AzulEv.copy(alpha = 0.16f)
                                    )

                                    FilaDetalleEvento(
                                        icono = Icons.Default.GridView,
                                        etiqueta = "Categoría",
                                        valor = categoria
                                    )

                                    HorizontalDivider(
                                        color = AzulEv.copy(alpha = 0.16f)
                                    )

                                    FilaDetalleEvento(
                                        icono = Icons.Default.CalendarToday,
                                        etiqueta = "Fecha",
                                        valor = evento.fecha
                                    )

                                    HorizontalDivider(
                                        color = AzulEv.copy(alpha = 0.16f)
                                    )

                                    FilaDetalleEvento(
                                        icono = Icons.Default.Schedule,
                                        etiqueta = "Hora",
                                        valor = evento.hora
                                    )

                                    HorizontalDivider(
                                        color = AzulEv.copy(alpha = 0.16f)
                                    )

                                    FilaDetalleEvento(
                                        icono = Icons.Default.LocationOn,
                                        etiqueta = "Lugar",
                                        valor = lugar
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onPantallaChange("editar")
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF073C88)
                                    ),
                                    border = BorderStroke(1.dp, AzulEv),
                                    contentPadding = PaddingValues(
                                        horizontal = 8.dp,
                                        vertical = 12.dp
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(17.dp)
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text(
                                        "Editar evento",
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }

                                Button(
                                    onClick = {
                                        onEliminarChange(evento)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF421B22)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        Color(0xFFE53945)
                                    ),
                                    contentPadding = PaddingValues(
                                        horizontal = 8.dp,
                                        vertical = 12.dp
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFFF707A),
                                        modifier = Modifier.size(17.dp)
                                    )

                                    Spacer(Modifier.width(6.dp))

                                    Text(
                                        "Eliminar evento",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))
                        }
                    } else {
                        LaunchedEffect(pantalla, usuarioIdActivo) {
                            onPantallaChange("lista")
                        }
                    }
                }

                // CREAR Y EDITAR EVENTOS
                "crear", "editar" -> {
                    val inicial = if (pantalla == "editar") {
                        seleccionado?.takeIf {
                            it.usuarioId == usuarioIdActivo
                        }
                    } else {
                        null
                    }

                    EncabezadoEv(
                        titulo = if (pantalla == "crear") {
                            "Crear evento"
                        } else {
                            "Editar evento"
                        },
                        onVolver = {
                            onPantallaChange(
                                if (pantalla == "crear") {
                                    "lista"
                                } else {
                                    "detalle"
                                }
                            )
                        }
                    )

                    key(
                        usuarioIdActivo,
                        pantalla,
                        inicial?.id
                    ) {
                        FormularioEv(
                            eventoInicial = inicial,
                            categorias = categorias,
                            lugares = lugares,
                            procesando = guardandoEvento,
                            onRegistrarLugar = onRegistrarLugar,
                            resultadoRegistroCategoria = resultadoRegistroCategoria,
                            onLimpiarResultadoCategoria = onLimpiarResultadoCategoria,
                            onRegistrarCategoria = onRegistrarCategoria,
                            onCancelar = {
                                if (!guardandoEvento) {
                                    onPantallaChange(
                                        if (inicial == null) {
                                            "lista"
                                        } else {
                                            "detalle"
                                        }
                                    )
                                }
                            },
                            onGuardar = {
                                    nombre, descripcion, fecha,
                                    hora, categoriaId, lugarId ->
                                onGuardarEvento(
                                    inicial,
                                    nombre,
                                    descripcion,
                                    fecha,
                                    hora,
                                    categoriaId,
                                    lugarId
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // CONFIRMACIÓN DE ELIMINACIÓN
    eliminar?.let { evento ->
        if (evento.usuarioId == usuarioIdActivo) {
            AlertDialog(
                onDismissRequest = {
                    if (!eliminandoEvento) {
                        onEliminarChange(null)
                    }
                },
                title = {
                    Text("Eliminar evento")
                },
                text = {
                    Text(
                        "¿Deseas eliminar el evento \"${evento.nombre}\"?"
                    )
                },
                confirmButton = {
                    TextButton(
                        enabled = !eliminandoEvento,
                        onClick = {
                            onEliminarEvento(evento)
                        }
                    ) {
                        Text(
                            if (eliminandoEvento) {
                                "Eliminando..."
                            } else {
                                "Eliminar"
                            },
                            color = RojoEv
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !eliminandoEvento,
                        onClick = {
                            onEliminarChange(null)
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }

    // MENSAJES
    mensaje?.let { texto ->
        AlertDialog(
            onDismissRequest = {
                onMensajeChange(null)
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
                        onMensajeChange(null)
                    }
                ) {
                    Text(
                        "Aceptar",
                        color = AzulEv
                    )
                }
            }
        )
    }
}

// =====================================================
// FILAS DEL DETALLE
// =====================================================

@Composable
private fun FilaDetalleEvento(
    icono: ImageVector,
    etiqueta: String,
    valor: String
) {
    val colores = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = AzulEv,
            modifier = Modifier.size(24.dp)
        )

        Column(
            verticalArrangement =
                Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = etiqueta,
                color = colores.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = valor,
                color = colores.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

// =====================================================
// FILTROS HORIZONTALES
// =====================================================

@Composable
private fun SelectorCategoriaFiltroEv(
    categorias: List<Categoria>,
    seleccion: Int?,
    onSeleccionar: (Int?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {
        ChipCategoriaEv(
            nombre = "Todos",
            seleccionado = seleccion == null,
            onClick = {
                onSeleccionar(null)
            }
        )

        categorias.forEach { categoria ->
            ChipCategoriaEv(
                nombre = categoria.nombre,
                seleccionado = seleccion == categoria.id,
                onClick = {
                    onSeleccionar(categoria.id)
                }
            )
        }
    }
}

@Composable
private fun ChipCategoriaEv(
    nombre: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (seleccionado) {
            AzulEv
        } else {
            colores.surface
        },
        border = BorderStroke(
            1.dp,
            if (seleccionado) {
                AzulEv
            } else {
                AzulEv.copy(alpha = 0.18f)
            }
        )
    ) {
        Text(
            text = nombre,
            modifier = Modifier.padding(
                horizontal = 15.dp,
                vertical = 9.dp
            ),
            fontSize = 12.sp,
            color = if (seleccionado) {
                Color.White
            } else {
                colores.onSurface
            },
            maxLines = 1
        )
    }
}

// =====================================================
// IMAGEN AUTOMÁTICA
// =====================================================

@Composable
private fun ImagenEventoEv(
    nombre: String,
    categoria: String,
    modifier: Modifier = Modifier,
    imagenUrl: String = ""
) {
    val url = remember(nombre, categoria, imagenUrl) {
        imagenUrl.takeIf { it.isNotBlank() }
            ?: ImagenEventoUtils.obtenerImagen(
                nombre,
                categoria
            )
    }

    var error by remember(url) {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF08283B)),
        contentAlignment = Alignment.Center
    ) {
        if (error || url.isBlank()) {
            Icon(
                Icons.Default.Event,
                contentDescription = "Imagen no disponible",
                tint = AzulEv,
                modifier = Modifier.size(42.dp)
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = "Imagen de $nombre",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = {
                    error = true
                }
            )
        }
    }
}

// =====================================================
// TARJETAS DE EVENTOS
// =====================================================

@Composable
private fun TarjetaEventoEv(
    evento: Evento,
    categoria: String,
    lugar: String,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = colores.surface
        ),
        border = BorderStroke(
            1.dp,
            AzulEv.copy(alpha = 0.16f)
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            ImagenEventoEv(
                nombre = evento.nombre,
                categoria = categoria,
                imagenUrl = evento.imagenUrl,
                modifier = Modifier.size(96.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    categoria,
                    color = AzulEv,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    evento.nombre,
                    color = colores.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                TextoIconoEv(
                    Icons.Default.CalendarToday,
                    evento.fecha
                )

                TextoIconoEv(
                    Icons.Default.Schedule,
                    evento.hora
                )

                TextoIconoEv(
                    Icons.Default.LocationOn,
                    lugar
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = colores.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TextoIconoEv(
    icono: ImageVector,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp)
        )

        Spacer(Modifier.width(5.dp))

        Text(
            texto,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =====================================================
// ENCABEZADO CREAR Y EDITAR
// =====================================================

@Composable
private fun EncabezadoEv(
    titulo: String,
    onVolver: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVolver) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = AzulEv
            )
        }

        Text(
            titulo,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    Spacer(Modifier.height(14.dp))
}

// =====================================================
// SELECTORES DE CATEGORÍAS Y LUGARES
// =====================================================

@Composable
private fun <T> SelectorEv(
    etiqueta: String,
    opciones: List<T>,
    seleccionadoId: Int?,
    obtenerId: (T) -> Int,
    obtenerNombre: (T) -> String,
    onSeleccionar: (Int) -> Unit
) {
    var expandido by remember {
        mutableStateOf(false)
    }

    val nombre = opciones.find {
        obtenerId(it) == seleccionadoId
    }?.let(obtenerNombre)
        ?: "Seleccionar $etiqueta"

    Column {
        Text(
            etiqueta,
            color = AzulEv,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Box {
            OutlinedButton(
                onClick = {
                    expandido = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    nombre,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = AzulEv
                )
            }

            DropdownMenu(
                expanded = expandido,
                onDismissRequest = {
                    expandido = false
                }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = {
                            Text(obtenerNombre(opcion))
                        },
                        onClick = {
                            onSeleccionar(obtenerId(opcion))
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}

// =====================================================
// FORMULARIO DE EVENTOS
// =====================================================

@Composable
private fun FormularioEv(
    eventoInicial: Evento?,
    categorias: List<Categoria>,
    lugares: List<Lugar>,
    onRegistrarLugar: (
        String,
        (Boolean, String) -> Unit
    ) -> Unit,
    resultadoRegistroCategoria: CategoriaMensaje?,
    onLimpiarResultadoCategoria: () -> Unit,
    onRegistrarCategoria: (String) -> Unit,
    onCancelar: () -> Unit,
    onGuardar: (
        String, String, String, String, Int, Int
    ) -> Unit,
    procesando: Boolean = false
) {
    val context = LocalContext.current
    val colores = MaterialTheme.colorScheme

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

    var categoriaId by remember(eventoInicial) {
        mutableStateOf(eventoInicial?.categoriaId)
    }

    var lugarId by remember(eventoInicial) {
        mutableStateOf(eventoInicial?.lugarId)
    }

    var error by remember(eventoInicial) {
        mutableStateOf<String?>(null)
    }

    var mostrarLugar by remember(eventoInicial) {
        mutableStateOf(false)
    }

    var nuevoLugar by remember(eventoInicial) {
        mutableStateOf("")
    }

    var guardandoLugar by remember(eventoInicial) {
        mutableStateOf(false)
    }

    var errorLugar by remember(eventoInicial) {
        mutableStateOf<String?>(null)
    }

    var mostrarCategoria by remember(eventoInicial) {
        mutableStateOf(false)
    }

    var nuevaCategoria by remember(eventoInicial) {
        mutableStateOf("")
    }

    var guardandoCategoria by remember(eventoInicial) {
        mutableStateOf(false)
    }

    var errorCategoria by remember(eventoInicial) {
        mutableStateOf<String?>(null)
    }

    var categoriaPendiente by remember(eventoInicial) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(resultadoRegistroCategoria, categorias) {
        val resultado = resultadoRegistroCategoria ?: return@LaunchedEffect
        if (!guardandoCategoria) return@LaunchedEffect

        if (!resultado.exitoso) {
            guardandoCategoria = false
            errorCategoria = resultado.texto
            categoriaPendiente = null
            onLimpiarResultadoCategoria()
        } else {
            val creada = categorias.firstOrNull {
                it.nombre.equals(categoriaPendiente, ignoreCase = true)
            }
            if (creada != null) {
                categoriaId = creada.id
                guardandoCategoria = false
                mostrarCategoria = false
                nuevaCategoria = ""
                categoriaPendiente = null
                errorCategoria = null
                error = null
                onLimpiarResultadoCategoria()
            }
        }
    }

    val categoriaNombre = categorias.find {
        it.id == categoriaId
    }?.nombre ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement =
            Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Información del evento",
            color = AzulEv,
            fontWeight = FontWeight.Bold
        )

        // TÍTULO
        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                error = null
            },
            label = {
                Text("Título del evento *")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !procesando
        )

        // DESCRIPCIÓN
        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                error = null
            },
            label = {
                Text("Descripción *")
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            enabled = !procesando
        )

        // CATEGORÍA
        SelectorEv(
            etiqueta = "Categoría *",
            opciones = categorias,
            seleccionadoId = categoriaId,
            obtenerId = { it.id },
            obtenerNombre = { it.nombre },
            onSeleccionar = {
                if (!procesando) {
                    categoriaId = it
                    error = null
                }
            }
        )

        // REGISTRAR CATEGORÍA SIN SALIR DEL EVENTO
        TextButton(
            onClick = {
                nuevaCategoria = ""
                errorCategoria = null
                onLimpiarResultadoCategoria()
                mostrarCategoria = true
            },
            enabled = !procesando && !guardandoCategoria
        ) {
            Icon(Icons.Default.AddCircleOutline, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Registrar nueva categoría", color = AzulEv)
        }

        // IMAGEN AUTOMÁTICA
        if (
            nombre.isNotBlank() ||
            categoriaNombre.isNotBlank()
        ) {
            Text(
                "Imagen referencial del evento",
                color = AzulEv,
                fontWeight = FontWeight.Bold
            )

            ImagenEventoEv(
                nombre = nombre,
                categoria = categoriaNombre,
                imagenUrl = if (
                    eventoInicial?.nombre == nombre &&
                    eventoInicial.categoriaId == categoriaId
                ) {
                    eventoInicial.imagenUrl
                } else {
                    ""
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Text(
                "   ",
                color = colores.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Text(
            "Fecha y hora",
            color = AzulEv,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            // FECHA
            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()

                    DatePickerDialog(
                        context,
                        { _, anio, mes, dia ->
                            fecha = String.format(
                                Locale.US,
                                "%04d-%02d-%02d",
                                anio,
                                mes + 1,
                                dia
                            )
                            error = null
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                enabled = !procesando,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = null,
                    tint = AzulEv
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    fecha.ifBlank { "Fecha" },
                    fontSize = 12.sp,
                    color = colores.onSurface
                )
            }

            // HORA
            OutlinedButton(
                onClick = {
                    val cal = Calendar.getInstance()

                    TimePickerDialog(
                        context,
                        { _, h, m ->
                            hora = String.format(
                                Locale.US,
                                "%02d:%02d",
                                h,
                                m
                            )
                            error = null
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                enabled = !procesando,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = AzulEv
                )

                Spacer(Modifier.width(4.dp))

                Text(
                    hora.ifBlank { "Hora" },
                    fontSize = 12.sp,
                    color = colores.onSurface
                )
            }
        }

        // LUGAR
        SelectorEv(
            etiqueta = "Lugar *",
            opciones = lugares,
            seleccionadoId = lugarId,
            obtenerId = { it.id },
            obtenerNombre = { it.nombre },
            onSeleccionar = {
                if (!procesando) {
                    lugarId = it
                    error = null
                }
            }
        )

        // REGISTRAR LUGAR DESDE EL FORMULARIO
        TextButton(
            onClick = {
                nuevoLugar = ""
                errorLugar = null
                mostrarLugar = true
            },
            enabled = !procesando
        ) {
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = null
            )

            Spacer(Modifier.width(6.dp))

            Text(
                "Registrar nuevo lugar",
                color = AzulEv
            )
        }

        error?.let {
            Text(
                it,
                color = RojoEv
            )
        }

        // GUARDAR EVENTO
        Button(
            onClick = {
                val nombreLimpio = nombre.trim()
                val descripcionLimpia = descripcion.trim()

                when {
                    nombreLimpio.isBlank() ||
                            descripcionLimpia.isBlank() -> {
                        error = "Completa el título y la descripción"
                    }

                    categoriaId == null ||
                            categorias.none { it.id == categoriaId } -> {
                        error = "Selecciona una categoría válida"
                    }

                    lugarId == null ||
                            lugares.none { it.id == lugarId } -> {
                        error = "Selecciona un lugar válido"
                    }

                    fecha.isBlank() ||
                            hora.isBlank() -> {
                        error = "Selecciona la fecha y la hora"
                    }

                    else -> {
                        onGuardar(
                            nombreLimpio,
                            descripcionLimpia,
                            fecha,
                            hora,
                            categoriaId!!,
                            lugarId!!
                        )
                    }
                }
            },
            enabled = !procesando && !guardandoLugar && !guardandoCategoria,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulEv
            )
        ) {
            Text(
                when {
                    procesando -> "Guardando..."
                    eventoInicial == null -> "Crear evento"
                    else -> "Guardar cambios"
                }
            )
        }

        TextButton(
            onClick = onCancelar,
            enabled = !procesando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }

        Spacer(Modifier.height(20.dp))
    }

    // DIÁLOGO PARA REGISTRAR NUEVA CATEGORÍA
    if (mostrarCategoria) {
        AlertDialog(
            onDismissRequest = {
                if (!guardandoCategoria) mostrarCategoria = false
            },
            title = { Text("Registrar nueva categoría") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nuevaCategoria,
                        onValueChange = {
                            nuevaCategoria = it
                            errorCategoria = null
                        },
                        label = { Text("Nombre de la categoría") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !guardandoCategoria,
                        singleLine = true
                    )
                    errorCategoria?.let {
                        Text(it, color = RojoEv)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !guardandoCategoria,
                    onClick = {
                        val limpio = nuevaCategoria.trim()
                            .replace(Regex("\\s+"), " ")
                        when {
                            limpio.isBlank() -> {
                                errorCategoria = "Ingresa el nombre"
                            }
                            categorias.any {
                                it.nombre.equals(limpio, ignoreCase = true)
                            } -> {
                                errorCategoria = "La categoría ya existe"
                            }
                            else -> {
                                categoriaPendiente = limpio
                                guardandoCategoria = true
                                onRegistrarCategoria(limpio)
                            }
                        }
                    }
                ) {
                    Text(
                        if (guardandoCategoria) "Guardando..." else "Guardar",
                        color = AzulEv
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !guardandoCategoria,
                    onClick = { mostrarCategoria = false }
                ) { Text("Cancelar") }
            }
        )
    }

    // DIÁLOGO PARA REGISTRAR NUEVO LUGAR
    if (mostrarLugar) {
        AlertDialog(
            onDismissRequest = {
                if (!guardandoLugar) {
                    mostrarLugar = false
                }
            },
            title = {
                Text("Registrar nuevo lugar")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = nuevoLugar,
                        onValueChange = {
                            nuevoLugar = it
                            errorLugar = null
                        },
                        label = {
                            Text("Nombre del lugar")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !guardandoLugar,
                        singleLine = true
                    )

                    errorLugar?.let {
                        Text(
                            it,
                            color = RojoEv
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !guardandoLugar,
                    onClick = {
                        val limpio = nuevoLugar
                            .trim()
                            .replace(Regex("\\s+"), " ")

                        when {
                            limpio.isBlank() -> {
                                errorLugar = "Ingresa el nombre"
                            }

                            lugares.any {
                                it.nombre.equals(
                                    limpio,
                                    ignoreCase = true
                                )
                            } -> {
                                errorLugar = "El lugar ya existe"
                            }

                            else -> {
                                guardandoLugar = true

                                onRegistrarLugar(limpio) {
                                        exitoso, texto ->

                                    guardandoLugar = false

                                    if (exitoso) {
                                        mostrarLugar = false
                                        nuevoLugar = ""
                                    } else {
                                        errorLugar = texto
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        if (guardandoLugar) {
                            "Guardando..."
                        } else {
                            "Guardar"
                        },
                        color = AzulEv
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !guardandoLugar,
                    onClick = {
                        mostrarLugar = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

