
package com.example.acameet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.acameet.ui.EventoUiState
import com.example.acameet.ui.EventoViewModel
import com.example.acameet.ui.ImagenEventoUtils
import com.example.acameet.ui.components.BottomNavigationBar
import com.example.acameet.ui.viewmodel.CategoriaUiState
import com.example.acameet.ui.viewmodel.CategoriaViewModel
import com.example.acameet.ui.viewmodel.LugarViewModel

// COLORES ORIGINALES DE ACAMEET
private val AzulInicio = Color(0xFF0798F2)
private val FondoImagenInicio = Color(0xFF08283B)

// ============================================================
// HOMESCREEN - COMPATIBILIDAD CON APPNAVIGATION
// ============================================================

@Composable
fun HomeScreen(
    eventoViewModel: EventoViewModel,
    categoriaViewModel: CategoriaViewModel,
    lugarViewModel: LugarViewModel,
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
        onCategoriasClick = onCategoriasClick,
        onEventosClick = onEventosClick,
        onConfiguracionClick = onConfiguracionClick,
        onCrearEventoClick = onCrearEventoClick,
        onDetalleEventoClick = onDetalleEventoClick
    )
}

// ============================================================
// HOMEROUTE - MANEJO DE ESTADOS
// ============================================================

@Composable
fun HomeRoute(
    eventoViewModel: EventoViewModel,
    categoriaViewModel: CategoriaViewModel,
    lugarViewModel: LugarViewModel,
    onCategoriasClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {},
    onCrearEventoClick: () -> Unit = {},
    onDetalleEventoClick: (Int) -> Unit = {}
) {

    // ESTADO DE EVENTOS
    val eventoState by eventoViewModel.uiState
        .collectAsStateWithLifecycle()

    // ESTADO DE CATEGORÍAS
    val categoriaState by categoriaViewModel.uiState
        .collectAsStateWithLifecycle()

    // LISTA DE LUGARES
    val lugares by lugarViewModel.lugares
        .collectAsStateWithLifecycle()

    // OBTENER CATEGORÍAS
    val categorias = when (val estado = categoriaState) {
        is CategoriaUiState.Success -> estado.categorias
        else -> emptyList()
    }

    // TEXTO DE BÚSQUEDA
    var busqueda by rememberSaveable {
        mutableStateOf("")
    }

    // CATEGORÍA SELECCIONADA
    var categoriaSeleccionada by rememberSaveable {
        mutableStateOf<Int?>(null)
    }

    // OBTENER EVENTOS
    val eventos = when (val estado = eventoState) {
        is EventoUiState.Success -> estado.eventos
        else -> emptyList()
    }

    // FILTRAR EVENTOS POR BÚSQUEDA Y CATEGORÍA
    val eventosFiltrados = eventos.filter { evento ->

        val coincideBusqueda =
            evento.nombre.contains(
                busqueda,
                ignoreCase = true
            ) ||
                    evento.descripcion.contains(
                        busqueda,
                        ignoreCase = true
                    )

        val coincideCategoria =
            categoriaSeleccionada == null ||
                    evento.categoriaId == categoriaSeleccionada

        coincideBusqueda && coincideCategoria
    }

    // LLAMADA A LA INTERFAZ
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
        onCategoriasClick = onCategoriasClick,
        onEventosClick = onEventosClick,
        onConfiguracionClick = onConfiguracionClick,
        onCrearEventoClick = onCrearEventoClick,
        onDetalleEventoClick = onDetalleEventoClick
    )
}

// ============================================================
// HOMECONTENT - INTERFAZ PRINCIPAL
// ============================================================

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
    onCategoriasClick: () -> Unit,
    onEventosClick: () -> Unit,
    onConfiguracionClick: () -> Unit,
    onCrearEventoClick: () -> Unit,
    onDetalleEventoClick: (Int) -> Unit
) {

    val colores = MaterialTheme.colorScheme

    // FONDO ORIGINAL AZUL OSCURO
    val fondoInicio = if (
        colores.background == Color(0xFF020B12)
    ) {
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

        // BARRA DE NAVEGACIÓN INFERIOR
        bottomBar = {
            BottomNavigationBar(
                pantallaActual = "inicio",
                onCategoriasClick = onCategoriasClick,
                onEventosClick = onEventosClick,
                onConfiguracionClick = onConfiguracionClick
            )
        },

        // BOTÓN PARA CREAR EVENTOS
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
                .background(fondoInicio)
                .padding(padding)
                .padding(horizontal = 22.dp)
        ) {

            Spacer(Modifier.height(36.dp))

            // TÍTULO DE LA APLICACIÓN
            Text(
                text = "AcaMeet",
                color = colores.onBackground,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Organiza y descubre eventos académicos",
                color = colores.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(26.dp))

            // BUSCADOR DE EVENTOS
            OutlinedTextField(
                value = busqueda,
                onValueChange = onBusquedaChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar eventos académicos...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulInicio,
                    focusedLeadingIconColor = AzulInicio,
                    focusedTextColor = colores.onBackground,
                    unfocusedTextColor = colores.onBackground,
                    focusedPlaceholderColor = colores.onSurfaceVariant,
                    unfocusedPlaceholderColor = colores.onSurfaceVariant,
                    unfocusedBorderColor = colores.outline,
                    unfocusedLeadingIconColor = colores.onSurfaceVariant,
                    cursorColor = AzulInicio
                )
            )

            Spacer(Modifier.height(12.dp))

            // FILTRO POR CATEGORÍAS
            FiltroCategoriaInicio(
                categorias = categorias,
                seleccion = categoriaSeleccionada,
                onSeleccionar = onCategoriaChange
            )

            Spacer(Modifier.height(26.dp))

            // ENCABEZADO DE MIS EVENTOS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Mis eventos",
                    color = colores.onBackground,
                    fontSize = 22.sp,
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

            Spacer(Modifier.height(12.dp))

            // ESTADOS DE LA LISTA DE EVENTOS
            when {

                // CARGANDO EVENTOS
                cargando -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AzulInicio
                        )
                    }
                }

                // ERROR AL CARGAR EVENTOS
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = error,
                            color = colores.error
                        )
                    }
                }

                // NO EXISTEN EVENTOS
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
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = AzulInicio,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Próximos eventos",
                                color = colores.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "No hay eventos para mostrar",
                                color = colores.onSurfaceVariant,
                                fontSize = 14.sp
                            )

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = onCrearEventoClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AzulInicio
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Crear evento")
                            }
                        }
                    }
                }

                // LISTADO DE EVENTOS
                else -> {

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(
                            bottom = 90.dp
                        )
                    ) {

                        items(
                            items = eventos,
                            key = { it.id }
                        ) { evento ->

                            // BUSCAR CATEGORÍA DEL EVENTO
                            val categoria = categorias.find {
                                it.id == evento.categoriaId
                            }?.nombre ?: "Sin categoría"

                            // BUSCAR LUGAR DEL EVENTO
                            val lugar = lugares.find {
                                it.id == evento.lugarId
                            }?.nombre ?: "Lugar no disponible"

                            TarjetaEventoInicio(
                                evento = evento,
                                categoria = categoria,
                                lugar = lugar,
                                onClick = {
                                    onDetalleEventoClick(evento.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// FILTRO DE CATEGORÍAS
// ============================================================

@Composable
private fun FiltroCategoriaInicio(
    categorias: List<Categoria>,
    seleccion: Int?,
    onSeleccionar: (Int?) -> Unit
) {

    var expandido by remember {
        mutableStateOf(false)
    }

    Box {

        OutlinedButton(
            onClick = {
                expandido = true
            },
            shape = RoundedCornerShape(20.dp)
        ) {

            Text(
                text = categorias.find {
                    it.id == seleccion
                }?.nombre ?: "Todas las categorías",
                color = AzulInicio
            )

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = AzulInicio
            )
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = {
                expandido = false
            }
        ) {

            DropdownMenuItem(
                text = {
                    Text("Todas las categorías")
                },
                onClick = {
                    onSeleccionar(null)
                    expandido = false
                }
            )

            categorias.forEach { categoria ->

                DropdownMenuItem(
                    text = {
                        Text(categoria.nombre)
                    },
                    onClick = {
                        onSeleccionar(categoria.id)
                        expandido = false
                    }
                )
            }
        }
    }
}

// ============================================================
// TARJETA DE EVENTO CON IMAGEN AUTOMÁTICA
// ============================================================

@Composable
private fun TarjetaEventoInicio(
    evento: Evento,
    categoria: String,
    lugar: String,
    onClick: () -> Unit
) {

    val colores = MaterialTheme.colorScheme

    // OBTENER IMAGEN AUTOMÁTICA SEGÚN EL TEMA
    val imagenUrl = remember(evento.nombre, categoria) {
        ImagenEventoUtils.obtenerImagen(
            titulo = evento.nombre,
            categoria = categoria
        )
    }

    // CONTROLAR ERRORES AL CARGAR IMAGEN
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
            width = 1.dp,
            color = AzulInicio.copy(alpha = 0.18f)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // IMAGEN DEL EVENTO
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

                    // IMAGEN DE RESPALDO
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Imagen no disponible",
                        tint = AzulInicio,
                        modifier = Modifier.size(38.dp)
                    )

                } else {

                    AsyncImage(
                        model = imagenUrl,
                        contentDescription = "Imagen de ${evento.nombre}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = {
                            errorImagen = true
                        }
                    )
                }
            }

            // INFORMACIÓN DEL EVENTO
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                // NOMBRE DEL EVENTO
                Text(
                    text = evento.nombre,
                    color = colores.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // DESCRIPCIÓN
                Text(
                    text = evento.descripcion,
                    color = colores.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // FECHA
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AzulInicio,
                        modifier = Modifier.size(13.dp)
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        text = evento.fecha,
                        color = colores.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // HORA
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = AzulInicio,
                        modifier = Modifier.size(13.dp)
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        text = evento.hora,
                        color = colores.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // LUGAR
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AzulInicio,
                        modifier = Modifier.size(13.dp)
                    )

                    Spacer(Modifier.width(5.dp))

                    Text(
                        text = lugar,
                        color = colores.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // CATEGORÍA
                Text(
                    text = categoria,
                    color = AzulInicio,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // BOTÓN PARA VER DETALLE
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = AzulInicio,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
