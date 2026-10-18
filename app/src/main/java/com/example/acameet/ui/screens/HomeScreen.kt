
package com.example.acameet.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.local.entity.Evento
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.ui.EventoUiState
import com.example.acameet.ui.EventoViewModel
import com.example.acameet.ui.components.BottomNavigationBar
import com.example.acameet.ui.viewmodel.CategoriaUiState
import com.example.acameet.ui.viewmodel.CategoriaViewModel
import com.example.acameet.ui.viewmodel.LugarViewModel

// Mantiene el azul original de AcaMeet.
private val AzulInicio = Color(0xFF0798F2)

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
    val eventoState by eventoViewModel.uiState.collectAsStateWithLifecycle()
    val categoriaState by categoriaViewModel.uiState.collectAsStateWithLifecycle()
    val lugares by lugarViewModel.lugares.collectAsStateWithLifecycle()

    val categorias = when (val estado = categoriaState) {
        is CategoriaUiState.Success -> estado.categorias
        else -> emptyList()
    }

    var busqueda by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember {
        mutableStateOf<Int?>(null)
    }

    val eventos = when (val estado = eventoState) {
        is EventoUiState.Success -> estado.eventos
        else -> emptyList()
    }

    val eventosFiltrados = eventos.filter { evento ->
        val coincideBusqueda =
            evento.nombre.contains(busqueda, ignoreCase = true) ||
                    evento.descripcion.contains(busqueda, ignoreCase = true)

        val coincideCategoria =
            categoriaSeleccionada == null ||
                    evento.categoriaId == categoriaSeleccionada

        coincideBusqueda && coincideCategoria
    }

    HomeContent(
        eventos = eventosFiltrados,
        cargando = eventoState is EventoUiState.Loading,
        error = (eventoState as? EventoUiState.Error)?.mensaje,
        categorias = categorias,
        lugares = lugares,
        busqueda = busqueda,
        onBusquedaChange = { busqueda = it },
        categoriaSeleccionada = categoriaSeleccionada,
        onCategoriaChange = { categoriaSeleccionada = it },
        onCategoriasClick = onCategoriasClick,
        onEventosClick = onEventosClick,
        onConfiguracionClick = onConfiguracionClick,
        onCrearEventoClick = onCrearEventoClick,
        onDetalleEventoClick = onDetalleEventoClick
    )
}

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

    val fondoInicio = if (colores.background == Color(0xFF020B12)) {
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
                contentColor = Color.White
            ) {
                Icon(
                    Icons.Default.Add,
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

            Text(
                "AcaMeet",
                color = colores.onBackground,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "Organiza y descubre eventos académicos",
                color = colores.onSurfaceVariant,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(26.dp))

            OutlinedTextField(
                value = busqueda,
                onValueChange = onBusquedaChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Buscar eventos académicos...")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
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

            FiltroCategoriaInicio(
                categorias = categorias,
                seleccion = categoriaSeleccionada,
                onSeleccionar = onCategoriaChange
            )

            Spacer(Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Mis eventos",
                    color = colores.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = onEventosClick) {
                    Text(
                        "Ver todos",
                        color = AzulInicio
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
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

                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            error,
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
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Event,
                                contentDescription = null,
                                tint = AzulInicio,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                "Próximos eventos",
                                color = colores.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                "No hay eventos para mostrar",
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

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(
                            items = eventos,
                            key = { it.id }
                        ) { evento ->
                            TarjetaEventoInicio(
                                evento = evento,
                                categoria = categorias.find {
                                    it.id == evento.categoriaId
                                }?.nombre ?: "Sin categoría",
                                lugar = lugares.find {
                                    it.id == evento.lugarId
                                }?.nombre ?: "Lugar no disponible",
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
            }
        ) {
            Text(
                categorias.find {
                    it.id == seleccion
                }?.nombre ?: "Todas las categorías",
                color = AzulInicio
            )

            Spacer(Modifier.width(8.dp))

            Icon(
                Icons.Default.ArrowDropDown,
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

@Composable
private fun TarjetaEventoInicio(
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
            Icon(
                Icons.Default.Event,
                contentDescription = null,
                tint = AzulInicio,
                modifier = Modifier.size(42.dp)
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    evento.nombre,
                    color = colores.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(5.dp))

                Text(
                    evento.descripcion,
                    color = colores.onSurfaceVariant,
                    fontSize = 13.sp,
                    maxLines = 2
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    "${evento.fecha} • ${evento.hora}",
                    color = AzulInicio,
                    fontSize = 12.sp
                )

                Text(
                    "$categoria • $lugar",
                    color = colores.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Ver detalle",
                tint = AzulInicio
            )
        }
    }
}
