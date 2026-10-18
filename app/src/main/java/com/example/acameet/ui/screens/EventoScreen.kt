
package com.example.acameet.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.acameet.ui.viewmodel.CategoriaUiState
import com.example.acameet.ui.viewmodel.CategoriaViewModel
import com.example.acameet.ui.viewmodel.LugarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val AzulEv = Color(0xFF0798F2)
private val RojoEv = Color(0xFFE57373)

@Composable
fun EventoScreen(
    viewModel: EventoViewModel,
    categoriaViewModel: CategoriaViewModel,
    lugarViewModel: LugarViewModel,
    pantallaInicial: String = "lista",
    eventoIdInicial: Int? = null,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {}
) {
    EventoRoute(
        viewModel = viewModel,
        categoriaViewModel = categoriaViewModel,
        lugarViewModel = lugarViewModel,
        pantallaInicial = pantallaInicial,
        eventoIdInicial = eventoIdInicial,
        onInicioClick = onInicioClick,
        onCategoriasClick = onCategoriasClick,
        onConfiguracionClick = onConfiguracionClick
    )
}

@Composable
fun EventoRoute(
    viewModel: EventoViewModel,
    categoriaViewModel: CategoriaViewModel,
    lugarViewModel: LugarViewModel,
    pantallaInicial: String = "lista",
    eventoIdInicial: Int? = null,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val busqueda by viewModel.busqueda.collectAsStateWithLifecycle()
    val categoriaState by categoriaViewModel.uiState.collectAsStateWithLifecycle()
    val lugares by lugarViewModel.lugares.collectAsStateWithLifecycle()

    val categorias = when (val estado = categoriaState) {
        is CategoriaUiState.Success -> estado.categorias
        else -> emptyList()
    }

    var pantalla by remember(pantallaInicial, eventoIdInicial) {
        mutableStateOf(pantallaInicial)
    }
    var seleccionado by remember {
        mutableStateOf<Evento?>(null)
    }
    var eliminar by remember {
        mutableStateOf<Evento?>(null)
    }
    var mensaje by remember {
        mutableStateOf<String?>(null)
    }
    var filtroCategoria by remember {
        mutableStateOf<Int?>(null)
    }

    LaunchedEffect(eventoIdInicial) {
        if (eventoIdInicial != null) {
            try {
                seleccionado = viewModel.obtenerEventoParaPantalla(eventoIdInicial)
                pantalla = if (seleccionado != null) "detalle" else "lista"
            } catch (e: Exception) {
                pantalla = "lista"
                mensaje = "No se pudo cargar el evento"
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.mensajes.collect { resultado ->
            mensaje = resultado.texto
            if (resultado.exitoso) {
                pantalla = "lista"
                seleccionado = null
                eliminar = null
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
        onPantallaChange = { pantalla = it },
        onSeleccionadoChange = { seleccionado = it },
        onEliminarChange = { eliminar = it },
        onMensajeChange = { mensaje = it },
        onCategoriaFiltroChange = { filtroCategoria = it },
        onBusquedaChange = viewModel::actualizarBusqueda,
        onRegistrarLugar = { nombre, resultado ->
            lugarViewModel.registrarLugar(
                Lugar(nombre = nombre),
                resultado
            )
        },
        onGuardarEvento = {
                inicial, nombre, descripcion, fecha,
                hora, categoriaId, lugarId ->

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
            } else {
                viewModel.actualizarEvento(
                    evento = inicial,
                    nombre = nombre,
                    descripcion = descripcion,
                    fecha = fecha,
                    hora = hora,
                    categoriaId = categoriaId,
                    lugarId = lugarId,
                    imagenUrl = ""
                )
            }
        },
        onEliminarEvento = viewModel::eliminarEvento,
        onInicioClick = onInicioClick,
        onCategoriasClick = onCategoriasClick,
        onConfiguracionClick = onConfiguracionClick
    )
}

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
    onRegistrarLugar: (String, (Boolean, String) -> Unit) -> Unit,
    onGuardarEvento: (
        Evento?, String, String, String, String, Int, Int
    ) -> Unit,
    onEliminarEvento: (Evento) -> Unit,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    val oscuro = colores.background == Color(0xFF020B12)

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
            if (pantalla == "lista") {
                FloatingActionButton(
                    onClick = {
                        onSeleccionadoChange(null)
                        onPantallaChange("crear")
                    },
                    containerColor = AzulEv,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Crear evento")
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(fondo)
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            when (pantalla) {
                "lista" -> {
                    Text(
                        "Eventos",
                        color = colores.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Descubre y participa en eventos académicos",
                        color = colores.onSurfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = onBusquedaChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar eventos...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(10.dp))

                    SelectorCategoriaFiltroEv(
                        categorias = categorias,
                        seleccion = categoriaFiltro,
                        onSeleccionar = onCategoriaFiltroChange
                    )

                    Spacer(Modifier.height(14.dp))

                    when (val estado = uiState) {
                        is EventoUiState.Loading -> {
                            Box(
                                Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = AzulEv)
                            }
                        }

                        is EventoUiState.Error -> {
                            Text(
                                estado.mensaje,
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
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No hay eventos para mostrar",
                                        color = colores.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 90.dp)
                                ) {
                                    items(eventos, key = { it.id }) { evento ->
                                        val categoria = categorias.find {
                                            it.id == evento.categoriaId
                                        }?.nombre ?: "Evento"

                                        val lugar = lugares.find {
                                            it.id == evento.lugarId
                                        }?.nombre ?: "Lugar no disponible"

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

                "detalle" -> {
                    if (seleccionado != null) {
                        EncabezadoEv(
                            "Detalle del evento",
                            onVolver = { onPantallaChange("lista") }
                        )

                        val categoria = categorias.find {
                            it.id == seleccionado.categoriaId
                        }?.nombre ?: "Evento"

                        val lugar = lugares.find {
                            it.id == seleccionado.lugarId
                        }?.nombre ?: "Lugar no disponible"

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = colores.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    ImagenEventoEv(
                                        nombre = seleccionado.nombre,
                                        categoria = categoria,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(190.dp)
                                    )

                                    Text(
                                        seleccionado.nombre,
                                        fontSize = 23.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colores.onSurface
                                    )

                                    Text(
                                        seleccionado.descripcion,
                                        color = colores.onSurfaceVariant
                                    )

                                    HorizontalDivider()

                                    DatoEv("Categoría", categoria)
                                    DatoEv("Fecha", seleccionado.fecha)
                                    DatoEv("Hora", seleccionado.hora)
                                    DatoEv("Lugar", lugar)
                                }
                            }

                            Button(
                                onClick = { onPantallaChange("editar") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AzulEv
                                )
                            ) {
                                Icon(Icons.Default.Edit, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Editar evento")
                            }

                            OutlinedButton(
                                onClick = {
                                    onEliminarChange(seleccionado)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = RojoEv
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Eliminar evento", color = RojoEv)
                            }
                        }
                    }
                }

                "crear", "editar" -> {
                    val inicial =
                        if (pantalla == "editar") seleccionado else null

                    EncabezadoEv(
                        titulo = if (inicial == null) {
                            "Crear evento"
                        } else {
                            "Editar evento"
                        },
                        onVolver = {
                            onPantallaChange(
                                if (inicial == null) "lista" else "detalle"
                            )
                        }
                    )

                    FormularioEv(
                        eventoInicial = inicial,
                        categorias = categorias,
                        lugares = lugares,
                        onRegistrarLugar = onRegistrarLugar,
                        onCancelar = {
                            onPantallaChange(
                                if (inicial == null) "lista" else "detalle"
                            )
                        },
                        onGuardar = {
                                nombre, descripcion, fecha, hora,
                                categoriaId, lugarId ->

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

    eliminar?.let { evento ->
        AlertDialog(
            onDismissRequest = { onEliminarChange(null) },
            title = { Text("Eliminar evento") },
            text = {
                Text("¿Deseas eliminar el evento \"${evento.nombre}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = { onEliminarEvento(evento) }
                ) {
                    Text("Eliminar", color = RojoEv)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onEliminarChange(null) }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    mensaje?.let { texto ->
        AlertDialog(
            onDismissRequest = { onMensajeChange(null) },
            title = { Text("AcaMeet") },
            text = { Text(texto) },
            confirmButton = {
                TextButton(
                    onClick = { onMensajeChange(null) }
                ) {
                    Text("Aceptar", color = AzulEv)
                }
            }
        )
    }
}

// IMAGEN AUTOMÁTICA SEGÚN EL TEMA
@Composable
private fun ImagenEventoEv(
    nombre: String,
    categoria: String,
    modifier: Modifier = Modifier
) {
    val url = remember(nombre, categoria) {
        ImagenEventoUtils.obtenerImagen(nombre, categoria)
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
        if (error) {
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
                onError = { error = true }
            )
        }
    }
}

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
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ImagenEventoEv(
                nombre = evento.nombre,
                categoria = categoria,
                modifier = Modifier.size(96.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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

                TextoIconoEv(Icons.Default.CalendarToday, evento.fecha)
                TextoIconoEv(Icons.Default.Schedule, evento.hora)
                TextoIconoEv(Icons.Default.LocationOn, lugar)
            }

            Icon(
                Icons.Default.ChevronRight,
                "Ver detalle",
                tint = colores.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TextoIconoEv(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icono,
            null,
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

@Composable
private fun EncabezadoEv(
    titulo: String,
    onVolver: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onVolver) {
            Icon(
                Icons.Default.ArrowBack,
                "Volver",
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

@Composable
private fun DatoEv(etiqueta: String, valor: String) {
    Column {
        Text(etiqueta, color = AzulEv, fontSize = 13.sp)
        Text(
            valor,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SelectorCategoriaFiltroEv(
    categorias: List<Categoria>,
    seleccion: Int?,
    onSeleccionar: (Int?) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expandido = true }) {
            Text(
                categorias.find {
                    it.id == seleccion
                }?.nombre ?: "Todas las categorías",
                color = AzulEv
            )
            Icon(Icons.Default.ArrowDropDown, null, tint = AzulEv)
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas las categorías") },
                onClick = {
                    onSeleccionar(null)
                    expandido = false
                }
            )

            categorias.forEach { categoria ->
                DropdownMenuItem(
                    text = { Text(categoria.nombre) },
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
private fun <T> SelectorEv(
    etiqueta: String,
    opciones: List<T>,
    seleccionadoId: Int?,
    obtenerId: (T) -> Int,
    obtenerNombre: (T) -> String,
    onSeleccionar: (Int) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    val nombre = opciones.find {
        obtenerId(it) == seleccionadoId
    }?.let(obtenerNombre) ?: "Seleccionar $etiqueta"

    Column {
        Text(
            etiqueta,
            color = AzulEv,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(6.dp))

        Box {
            OutlinedButton(
                onClick = { expandido = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    nombre,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    null,
                    tint = AzulEv
                )
            }

            DropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(obtenerNombre(opcion)) },
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

@Composable
private fun FormularioEv(
    eventoInicial: Evento?,
    categorias: List<Categoria>,
    lugares: List<Lugar>,
    onRegistrarLugar: (
        String, (Boolean, String) -> Unit
    ) -> Unit,
    onCancelar: () -> Unit,
    onGuardar: (
        String, String, String, String, Int, Int
    ) -> Unit
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
    var error by remember { mutableStateOf<String?>(null) }

    var mostrarLugar by remember {
        mutableStateOf(false)
    }
    var nuevoLugar by remember {
        mutableStateOf("")
    }
    var guardandoLugar by remember {
        mutableStateOf(false)
    }
    var errorLugar by remember {
        mutableStateOf<String?>(null)
    }

    val categoriaNombre = categorias.find {
        it.id == categoriaId
    }?.nombre ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Información del evento",
            color = AzulEv,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                error = null
            },
            label = { Text("Título del evento *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                error = null
            },
            label = { Text("Descripción *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        SelectorEv(
            etiqueta = "Categoría *",
            opciones = categorias,
            seleccionadoId = categoriaId,
            obtenerId = { it.id },
            obtenerNombre = { it.nombre },
            onSeleccionar = {
                categoriaId = it
                error = null
            }
        )

        // VISTA PREVIA AUTOMÁTICA
        if (nombre.isNotBlank() || categoriaNombre.isNotBlank()) {
            Text(
                "Imagen automática del evento",
                color = AzulEv,
                fontWeight = FontWeight.Bold
            )

            ImagenEventoEv(
                nombre = nombre,
                categoria = categoriaNombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            Text(
                "La imagen se selecciona automáticamente " +
                        "según el título y la categoría.",
                color = colores.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Text(
            "Fecha y hora",
            color = AzulEv,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Event, null, tint = AzulEv)
                Spacer(Modifier.width(4.dp))
                Text(
                    fecha.ifBlank { "Fecha" },
                    fontSize = 12.sp,
                    color = colores.onSurface
                )
            }

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
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Schedule, null, tint = AzulEv)
                Spacer(Modifier.width(4.dp))
                Text(
                    hora.ifBlank { "Hora" },
                    fontSize = 12.sp,
                    color = colores.onSurface
                )
            }
        }

        SelectorEv(
            etiqueta = "Lugar *",
            opciones = lugares,
            seleccionadoId = lugarId,
            obtenerId = { it.id },
            obtenerNombre = { it.nombre },
            onSeleccionar = {
                lugarId = it
                error = null
            }
        )

        TextButton(
            onClick = {
                nuevoLugar = ""
                errorLugar = null
                mostrarLugar = true
            }
        ) {
            Icon(Icons.Default.AddCircleOutline, null)
            Spacer(Modifier.width(6.dp))
            Text("Registrar nuevo lugar", color = AzulEv)
        }

        error?.let {
            Text(it, color = RojoEv)
        }

        Button(
            onClick = {
                when {
                    nombre.isBlank() ||
                            descripcion.isBlank() ->
                        error = "Completa el título y la descripción"

                    categoriaId == null ->
                        error = "Selecciona una categoría"

                    lugarId == null ->
                        error = "Selecciona un lugar"

                    fecha.isBlank() ||
                            hora.isBlank() ->
                        error = "Selecciona la fecha y la hora"

                    else -> {
                        onGuardar(
                            nombre.trim(),
                            descripcion.trim(),
                            fecha,
                            hora,
                            categoriaId!!,
                            lugarId!!
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulEv
            )
        ) {
            Text(
                if (eventoInicial == null) {
                    "Crear evento"
                } else {
                    "Guardar cambios"
                }
            )
        }

        TextButton(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }

        Spacer(Modifier.height(20.dp))
    }

    if (mostrarLugar) {
        AlertDialog(
            onDismissRequest = {
                if (!guardandoLugar) mostrarLugar = false
            },
            title = { Text("Registrar nuevo lugar") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nuevoLugar,
                        onValueChange = {
                            nuevoLugar = it
                            errorLugar = null
                        },
                        label = { Text("Nombre del lugar") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !guardandoLugar
                    )

                    errorLugar?.let {
                        Text(it, color = RojoEv)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !guardandoLugar,
                    onClick = {
                        val limpio = nuevoLugar.trim()

                        when {
                            limpio.isBlank() ->
                                errorLugar = "Ingresa el nombre"

                            lugares.any {
                                it.nombre.equals(
                                    limpio,
                                    ignoreCase = true
                                )
                            } ->
                                errorLugar = "El lugar ya existe"

                            else -> {
                                guardandoLugar = true

                                onRegistrarLugar(limpio) {
                                        exitoso, mensaje ->
                                    guardandoLugar = false

                                    if (exitoso) {
                                        mostrarLugar = false
                                        nuevoLugar = ""
                                    } else {
                                        errorLugar = mensaje
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Guardar", color = AzulEv)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !guardandoLugar,
                    onClick = { mostrarLugar = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}
