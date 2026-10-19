package com.example.acameet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import coil3.compose.AsyncImage

import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.local.entity.Evento
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.data.local.entity.Notificacion

import com.example.acameet.ui.EventoUiState
import com.example.acameet.ui.EventoViewModel
import com.example.acameet.ui.ImagenEventoUtils

import com.example.acameet.ui.components.BottomNavigationBar
import com.example.acameet.ui.components.CampanaNotificaciones

import com.example.acameet.ui.viewmodel.CategoriaUiState
import com.example.acameet.ui.viewmodel.CategoriaViewModel
import com.example.acameet.ui.viewmodel.LugarViewModel
import com.example.acameet.ui.viewmodel.NotificacionUiState
import com.example.acameet.ui.viewmodel.NotificacionViewModel

private val AzulInicio = Color(0xFF0798F2)
private val FondoImagenInicio = Color(0xFF08283B)

// =====================================================
// PANTALLA DE INICIO
// =====================================================

@Composable
fun HomeScreen(
    eventoViewModel: EventoViewModel,
    categoriaViewModel: CategoriaViewModel,
    lugarViewModel: LugarViewModel,
    notificacionViewModel: NotificacionViewModel,
    usuarioIdActivo: Int? = null,
    onCategoriasClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {},
    onCrearEventoClick: () -> Unit = {},
    onDetalleEventoClick: (Int) -> Unit = {}
) {
    HomeRoute(
        eventoViewModel = eventoViewModel,
        categoriaViewModel = categoriaViewModel,
        lugarViewModel = lugarViewModel,
        notificacionViewModel = notificacionViewModel,
        usuarioIdActivo = usuarioIdActivo,
        onCategoriasClick = onCategoriasClick,
        onEventosClick = onEventosClick,
        onConfiguracionClick = onConfiguracionClick,
        onCrearEventoClick = onCrearEventoClick,
        onDetalleEventoClick = onDetalleEventoClick
    )
}

// =====================================================
// CONEXIÓN CON LOS VIEWMODELS
// =====================================================

@Composable
fun HomeRoute(
    eventoViewModel: EventoViewModel,
    categoriaViewModel: CategoriaViewModel,
    lugarViewModel: LugarViewModel,
    notificacionViewModel: NotificacionViewModel,
    usuarioIdActivo: Int? = null,
    onCategoriasClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {},
    onCrearEventoClick: () -> Unit = {},
    onDetalleEventoClick: (Int) -> Unit = {}
) {
    // ACTUALIZAR EL USUARIO ACTIVO
    LaunchedEffect(
        notificacionViewModel,
        usuarioIdActivo
    ) {
        notificacionViewModel.seleccionarUsuario(
            usuarioIdActivo
        )
    }

    // ESTADOS REACTIVOS
    val eventoState by eventoViewModel.uiState
        .collectAsStateWithLifecycle()

    val categoriaState by categoriaViewModel.uiState
        .collectAsStateWithLifecycle()

    val lugares by lugarViewModel.lugares
        .collectAsStateWithLifecycle()

    val notificacionState by notificacionViewModel.uiState
        .collectAsStateWithLifecycle()

    val categorias = when (val estado = categoriaState) {
        is CategoriaUiState.Success -> estado.categorias
        else -> emptyList()
    }

    val eventos = when (val estado = eventoState) {
        is EventoUiState.Success -> estado.eventos
        else -> emptyList()
    }

    val notificaciones = when (val estado = notificacionState) {
        is NotificacionUiState.Success -> estado.notificaciones
        else -> emptyList()
    }

    val pendientes = when (val estado = notificacionState) {
        is NotificacionUiState.Success -> estado.pendientes
        else -> 0
    }

    // BUSCADOR
    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    // CATEGORÍA SELECCIONADA
    var categoriaSeleccionada by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    // =====================================================
    // FILTRAR EVENTOS
    // =====================================================

    val eventosFiltrados = eventos.filter { evento ->

        val nombreLugar = lugares.find {
            it.id == evento.lugarId
        }?.nombre.orEmpty()

        val nombreCategoria = categorias.find {
            it.id == evento.categoriaId
        }?.nombre.orEmpty()

        val coincideBusqueda =
            busqueda.isBlank() ||
                    evento.nombre.contains(
                        busqueda,
                        ignoreCase = true
                    ) ||
                    evento.descripcion.contains(
                        busqueda,
                        ignoreCase = true
                    ) ||
                    nombreLugar.contains(
                        busqueda,
                        ignoreCase = true
                    ) ||
                    nombreCategoria.contains(
                        busqueda,
                        ignoreCase = true
                    )

        val coincideCategoria =
            categoriaSeleccionada == null ||
                    evento.categoriaId == categoriaSeleccionada

        coincideBusqueda && coincideCategoria
    }

    // =====================================================
    // INTERFAZ SIN VIEWMODELS NI DAOS
    // =====================================================

    HomeContent(
        eventos = eventosFiltrados,
        cargando = eventoState is EventoUiState.Loading,
        error = (eventoState as? EventoUiState.Error)?.mensaje,
        categorias = categorias,
        lugares = lugares,
        busqueda = busqueda,
        onBusquedaChange = {
            busqueda = it
        },
        categoriaSeleccionada = categoriaSeleccionada,
        onCategoriaChange = {
            categoriaSeleccionada = it
        },
        usuarioIdActivo = usuarioIdActivo,
        notificaciones = notificaciones,
        pendientes = pendientes,
        onMarcarTodas = {
            notificacionViewModel.marcarTodasComoLeidas()
        },
        onMarcarLeida = { id ->
            notificacionViewModel.marcarComoLeida(id)
        },
        onEliminarNotificacion = { id ->
            notificacionViewModel.eliminarNotificacion(id)
        },
        onEliminarTodas = {
            notificacionViewModel.eliminarTodas()
        },
        onCategoriasClick = onCategoriasClick,
        onEventosClick = onEventosClick,
        onConfiguracionClick = onConfiguracionClick,
        onCrearEventoClick = onCrearEventoClick,
        onDetalleEventoClick = onDetalleEventoClick
    )
}

// =====================================================
// CONTENIDO VISUAL DE INICIO
// =====================================================

@Composable
private fun HomeContent(
    eventos: List<Evento>,
    cargando: Boolean,
    error: String?,
    categorias: List<Categoria>,
    lugares: List<Lugar>,
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    categoriaSeleccionada: Int?,
    onCategoriaChange: (Int?) -> Unit,
    usuarioIdActivo: Int?,
    notificaciones: List<Notificacion>,
    pendientes: Int,
    onMarcarTodas: () -> Unit,
    onMarcarLeida: (Int) -> Unit,
    onEliminarNotificacion: (Int) -> Unit,
    onEliminarTodas: () -> Unit,
    onCategoriasClick: () -> Unit,
    onEventosClick: () -> Unit,
    onConfiguracionClick: () -> Unit,
    onCrearEventoClick: () -> Unit,
    onDetalleEventoClick: (Int) -> Unit
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
                pantallaActual = "inicio",
                onCategoriasClick = onCategoriasClick,
                onEventosClick = onEventosClick,
                onConfiguracionClick = onConfiguracionClick
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearEventoClick,
                containerColor = AzulInicio,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear evento"
                )
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
            Spacer(Modifier.height(24.dp))

            // =================================================
            // TÍTULO Y CAMPANA
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Eventos académicos",
                    color = colores.onBackground,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (
                    usuarioIdActivo != null &&
                    usuarioIdActivo > 0
                ) {
                    CampanaNotificaciones(
                        notificaciones = notificaciones,
                        pendientes = pendientes,
                        onMarcarTodas = onMarcarTodas,
                        onMarcarLeida = onMarcarLeida,
                        onEliminar = onEliminarNotificacion,
                        onEliminarTodas = onEliminarTodas,
                        onDetalleEventoClick = onDetalleEventoClick
                    )
                }
            }

            Spacer(Modifier.height(3.dp))

            Text(
                text = "Descubre, participa y amplía tu conocimiento",
                color = colores.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(16.dp))

            // =================================================
            // BUSCADOR
            // =================================================

            OutlinedTextField(
                value = busqueda,
                onValueChange = onBusquedaChange,
                modifier = Modifier.fillMaxWidth(),

                placeholder = {
                    Text(
                        text = "Buscar evento, tema o lugar...",
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                },

                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        modifier = Modifier.size(20.dp)
                    )
                },

                singleLine = true,
                shape = RoundedCornerShape(12.dp),

                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulInicio,
                    unfocusedBorderColor =
                        AzulInicio.copy(alpha = 0.28f),
                    focusedTextColor = colores.onBackground,
                    unfocusedTextColor = colores.onBackground,
                    focusedPlaceholderColor =
                        colores.onSurfaceVariant,
                    unfocusedPlaceholderColor =
                        colores.onSurfaceVariant,
                    cursorColor = AzulInicio
                )
            )

            Spacer(Modifier.height(14.dp))

            // =================================================
            // FILTROS DE CATEGORÍAS
            // =================================================

            FiltroCategoriaHorizontalInicio(
                categorias = categorias,
                seleccion = categoriaSeleccionada,
                onSeleccionar = onCategoriaChange
            )

            Spacer(Modifier.height(22.dp))

            // =================================================
            // SECCIÓN MIS EVENTOS
            // =================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mis eventos",
                    color = colores.onBackground,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onEventosClick
                ) {
                    Text(
                        text = "Ver todos",
                        color = AzulInicio
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // =================================================
            // LISTA DE EVENTOS
            // =================================================

            when {

                cargando -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AzulInicio
                        )
                    }
                }

                error != null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error,
                            color = colores.error
                        )
                    }
                }

                eventos.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = colores.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment =
                                Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = AzulInicio,
                                modifier = Modifier.size(44.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Próximos eventos",
                                color = colores.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "No hay eventos para mostrar",
                                color = colores.onSurfaceVariant,
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(14.dp))

                            Button(
                                onClick = onCrearEventoClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AzulInicio
                                )
                            ) {
                                Text("Crear evento")
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            bottom = 90.dp
                        )
                    ) {
                        items(
                            items = eventos,
                            key = { it.id }
                        ) { evento ->

                            val categoria = categorias.find {
                                it.id == evento.categoriaId
                            }?.nombre ?: "Sin categoría"

                            val lugar = lugares.find {
                                it.id == evento.lugarId
                            }?.nombre ?: "Lugar no disponible"

                            TarjetaEventoInicio(
                                evento = evento,
                                categoria = categoria,
                                lugar = lugar,
                                onClick = {
                                    onDetalleEventoClick(
                                        evento.id
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// =====================================================
// FILTROS HORIZONTALES
// =====================================================

@Composable
private fun FiltroCategoriaHorizontalInicio(
    categorias: List<Categoria>,
    seleccion: Int?,
    onSeleccionar: (Int?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
        horizontalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {
        ChipCategoriaInicio(
            nombre = "Todos",
            seleccionado = seleccion == null,
            onClick = {
                onSeleccionar(null)
            }
        )

        categorias.forEach { categoria ->
            ChipCategoriaInicio(
                nombre = categoria.nombre,
                seleccionado = seleccion == categoria.id,
                onClick = {
                    onSeleccionar(categoria.id)
                }
            )
        }
    }
}

// =====================================================
// CHIP DE CATEGORÍA
// =====================================================

@Composable
private fun ChipCategoriaInicio(
    nombre: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (seleccionado) {
            AzulInicio
        } else {
            colores.surface
        },
        border = BorderStroke(
            1.dp,
            if (seleccionado) {
                AzulInicio
            } else {
                AzulInicio.copy(alpha = 0.18f)
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
// TARJETA DE EVENTO
// =====================================================

@Composable
private fun TarjetaEventoInicio(
    evento: Evento,
    categoria: String,
    lugar: String,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

    val imagenUrl = remember(
        evento.nombre,
        categoria
    ) {
        ImagenEventoUtils.obtenerImagen(
            titulo = evento.nombre,
            categoria = categoria
        )
    }

    var errorImagen by remember(imagenUrl) {
        mutableStateOf(false)
    }

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
            AzulInicio.copy(alpha = 0.18f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(
                        width = 96.dp,
                        height = 105.dp
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .background(FondoImagenInicio),
                contentAlignment = Alignment.Center
            ) {
                if (errorImagen) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription =
                            "Imagen no disponible",
                        tint = AzulInicio,
                        modifier = Modifier.size(38.dp)
                    )
                } else {
                    AsyncImage(
                        model = imagenUrl,
                        contentDescription =
                            "Imagen de ${evento.nombre}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = {
                            errorImagen = true
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = evento.nombre,
                    color = colores.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = evento.descripcion,
                    color = colores.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                DatoEventoInicio(
                    icono = Icons.Default.CalendarToday,
                    texto = evento.fecha
                )

                DatoEventoInicio(
                    icono = Icons.Default.Schedule,
                    texto = evento.hora
                )

                DatoEventoInicio(
                    icono = Icons.Default.LocationOn,
                    texto = lugar
                )

                Text(
                    text = categoria,
                    color = AzulInicio,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver evento",
                tint = AzulInicio,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// =====================================================
// DATOS DEL EVENTO
// =====================================================

@Composable
private fun DatoEventoInicio(
    icono: ImageVector,
    texto: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = AzulInicio,
            modifier = Modifier.size(13.dp)
        )

        Spacer(Modifier.width(5.dp))

        Text(
            text = texto,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

