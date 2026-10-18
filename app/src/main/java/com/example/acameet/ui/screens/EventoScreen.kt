
package com.example.acameet.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val AzulEv = Color(0xFF0798F2)
private val RojoEvOscuro = Color(0xFFE57373)

@Composable
private fun colorErrorEv(): Color {
    val colores = MaterialTheme.colorScheme
    return if (colores.background == Color(0xFF020B12))
        RojoEvOscuro else colores.error
}

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
    val colores = MaterialTheme.colorScheme
    val esOscuro = colores.background == Color(0xFF020B12)
    val rojo = colorErrorEv()

    val fondoEv = Brush.verticalGradient(
        if (esOscuro) {
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
    var seleccionado by remember(pantallaInicial, eventoIdInicial) {
        mutableStateOf<Evento?>(null)
    }
    var eliminar by remember { mutableStateOf<Evento?>(null) }
    var mensaje by remember { mutableStateOf<String?>(null) }
    var categoriaFiltro by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(eventoIdInicial) {
        if (eventoIdInicial != null) {
            try {
                val evento = viewModel.obtenerEventoParaPantalla(eventoIdInicial)
                if (evento != null) {
                    seleccionado = evento
                    pantalla = "detalle"
                } else {
                    pantalla = "lista"
                    mensaje = "El evento ya no está disponible"
                }
            } catch (_: Exception) {
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
                        seleccionado = null
                        pantalla = "crear"
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
                .background(fondoEv)
                .padding(padding)
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            when (pantalla) {
                "lista" -> {
                    Text(
                        "Eventos",
                        color = colores.onBackground,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Administra los eventos académicos",
                        color = colores.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(18.dp))

                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = viewModel::actualizarBusqueda,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar eventos...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulEv,
                            unfocusedBorderColor = colores.outline,
                            focusedTextColor = colores.onBackground,
                            unfocusedTextColor = colores.onBackground,
                            focusedPlaceholderColor = colores.onSurfaceVariant,
                            unfocusedPlaceholderColor = colores.onSurfaceVariant,
                            cursorColor = AzulEv
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    FiltroCategoriaEv(
                        categorias = categorias,
                        seleccion = categoriaFiltro,
                        onSeleccionar = { categoriaFiltro = it }
                    )

                    Spacer(Modifier.height(16.dp))

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
                            Text(estado.mensaje, color = rojo)
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
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    seleccionado = evento
                                                    pantalla = "detalle"
                                                },
                                            shape = RoundedCornerShape(18.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = colores.surface
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Event,
                                                    null,
                                                    tint = AzulEv,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Spacer(Modifier.width(12.dp))
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        evento.nombre,
                                                        color = colores.onSurface,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        evento.descripcion,
                                                        color = colores.onSurfaceVariant,
                                                        fontSize = 13.sp,
                                                        maxLines = 2
                                                    )
                                                    Spacer(Modifier.height(5.dp))
                                                    Text(
                                                        "${evento.fecha} • ${evento.hora}",
                                                        color = AzulEv,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Icon(
                                                    Icons.Default.ChevronRight,
                                                    "Ver detalle",
                                                    tint = AzulEv
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "detalle" -> {
                    seleccionado?.let { evento ->
                        EncabezadoEv("Detalle del evento") {
                            pantalla = "lista"
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = colores.surface
                                ),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        evento.nombre,
                                        color = colores.onSurface,
                                        fontSize = 23.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        evento.descripcion,
                                        color = colores.onSurfaceVariant
                                    )
                                    HorizontalDivider(color = colores.outline)

                                    DatoEv(
                                        "Categoría",
                                        categorias.find {
                                            it.id == evento.categoriaId
                                        }?.nombre ?: "Categoría no disponible"
                                    )
                                    DatoEv("Fecha", evento.fecha)
                                    DatoEv("Hora", evento.hora)
                                    DatoEv(
                                        "Lugar",
                                        lugares.find {
                                            it.id == evento.lugarId
                                        }?.nombre ?: "Lugar no disponible"
                                    )
                                }
                            }

                            Button(
                                onClick = { pantalla = "editar" },
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
                                onClick = { eliminar = evento },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    null,
                                    tint = rojo
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Eliminar evento", color = rojo)
                            }
                        }
                    }
                }

                "crear", "editar" -> {
                    val eventoInicial =
                        if (pantalla == "editar") seleccionado else null

                    EncabezadoEv(
                        if (eventoInicial == null) "Crear evento"
                        else "Editar evento"
                    ) {
                        pantalla =
                            if (eventoInicial == null) "lista" else "detalle"
                    }

                    FormularioEv(
                        eventoInicial = eventoInicial,
                        categorias = categorias,
                        lugares = lugares,
                        onRegistrarLugar = { nombre, onResultado ->
                            lugarViewModel.registrarLugar(
                                Lugar(nombre = nombre),
                                onResultado
                            )
                        },
                        onCancelar = {
                            pantalla =
                                if (eventoInicial == null) "lista" else "detalle"
                        },
                        onGuardar = { nombre, descripcion, fecha, hora,
                                      categoriaId, lugarId ->
                            if (eventoInicial == null) {
                                viewModel.registrarEvento(
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    fecha = fecha,
                                    hora = hora,
                                    categoriaId = categoriaId,
                                    lugarId = lugarId
                                )
                            } else {
                                viewModel.actualizarEvento(
                                    evento = eventoInicial,
                                    nombre = nombre,
                                    descripcion = descripcion,
                                    fecha = fecha,
                                    hora = hora,
                                    categoriaId = categoriaId,
                                    lugarId = lugarId
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    eliminar?.let { evento ->
        AlertDialog(
            onDismissRequest = { eliminar = null },
            containerColor = colores.surface,
            titleContentColor = colores.onSurface,
            textContentColor = colores.onSurfaceVariant,
            title = { Text("Eliminar evento") },
            text = {
                Text("¿Deseas eliminar el evento \"${evento.nombre}\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.eliminarEvento(evento) }
                ) {
                    Text("Eliminar", color = rojo)
                }
            },
            dismissButton = {
                TextButton(onClick = { eliminar = null }) {
                    Text("Cancelar", color = colores.onSurfaceVariant)
                }
            }
        )
    }

    mensaje?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensaje = null },
            containerColor = colores.surface,
            titleContentColor = colores.onSurface,
            textContentColor = colores.onSurfaceVariant,
            title = { Text("AcaMeet") },
            text = { Text(texto) },
            confirmButton = {
                TextButton(onClick = { mensaje = null }) {
                    Text("Aceptar", color = AzulEv)
                }
            }
        )
    }
}

@Composable
private fun EncabezadoEv(
    titulo: String,
    onVolver: () -> Unit
) {
    val colores = MaterialTheme.colorScheme

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
            color = colores.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun DatoEv(
    etiqueta: String,
    valor: String
) {
    val colores = MaterialTheme.colorScheme

    Column {
        Text(
            etiqueta,
            color = AzulEv,
            fontSize = 13.sp
        )
        Text(valor, color = colores.onSurface)
    }
}

@Composable
private fun FiltroCategoriaEv(
    categorias: List<Categoria>,
    seleccion: Int?,
    onSeleccionar: (Int?) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expandido = true }) {
            Text(
                categorias.find { it.id == seleccion }?.nombre
                    ?: "Todas las categorías",
                color = AzulEv
            )
            Spacer(Modifier.width(8.dp))
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
    val colores = MaterialTheme.colorScheme
    var mostrarDialogo by remember { mutableStateOf(false) }

    val nombreSeleccionado = opciones.find {
        obtenerId(it) == seleccionadoId
    }?.let(obtenerNombre) ?: "Seleccionar $etiqueta"

    Column {
        Text(
            etiqueta,
            color = AzulEv,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(6.dp))

        OutlinedButton(
            onClick = { mostrarDialogo = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                nombreSeleccionado,
                modifier = Modifier.weight(1f),
                color = colores.onBackground
            )
            Icon(
                Icons.Default.ArrowDropDown,
                "Seleccionar $etiqueta",
                tint = AzulEv
            )
        }
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            containerColor = colores.surface,
            titleContentColor = colores.onSurface,
            textContentColor = colores.onSurfaceVariant,
            title = { Text("Seleccionar $etiqueta") },
            text = {
                if (opciones.isEmpty()) {
                    Text("No hay registros disponibles")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        opciones.forEach { opcion ->
                            TextButton(
                                onClick = {
                                    onSeleccionar(obtenerId(opcion))
                                    mostrarDialogo = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    obtenerNombre(opcion),
                                    color = colores.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false }) {
                    Text("Cerrar", color = AzulEv)
                }
            }
        )
    }
}

@Composable
private fun FormularioEv(
    eventoInicial: Evento?,
    categorias: List<Categoria>,
    lugares: List<Lugar>,
    onRegistrarLugar: (String, (Boolean, String) -> Unit) -> Unit,
    onCancelar: () -> Unit,
    onGuardar: (String, String, String, String, Int, Int) -> Unit
) {
    val context = LocalContext.current
    val colores = MaterialTheme.colorScheme
    val rojo = colorErrorEv()

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

    var mostrarRegistrarLugar by remember {
        mutableStateOf(false)
    }
    var nuevoLugar by remember {
        mutableStateOf("")
    }
    var guardandoLugar by remember {
        mutableStateOf(false)
    }
    var mensajeLugar by remember {
        mutableStateOf<String?>(null)
    }

    val formato = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            isLenient = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "Información general",
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
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulEv,
                focusedTextColor = colores.onBackground,
                unfocusedTextColor = colores.onBackground
            )
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = {
                descripcion = it
                error = null
            },
            label = { Text("Descripción *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzulEv,
                focusedTextColor = colores.onBackground,
                unfocusedTextColor = colores.onBackground
            )
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

        Text(
            "Fecha y hora",
            color = AzulEv,
            fontWeight = FontWeight.Bold
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val calendario = Calendar.getInstance()

                    if (fecha.isNotBlank()) {
                        try {
                            calendario.time = formato.parse(fecha)!!
                        } catch (_: Exception) {
                            // Mantener fecha actual
                        }
                    }

                    val dialogo = DatePickerDialog(
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
                        calendario.get(Calendar.YEAR),
                        calendario.get(Calendar.MONTH),
                        calendario.get(Calendar.DAY_OF_MONTH)
                    )

                    if (eventoInicial == null) {
                        val hoy = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        dialogo.datePicker.minDate = hoy.timeInMillis
                    }

                    dialogo.show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Event,
                    null,
                    tint = AzulEv
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    fecha.ifBlank { "Fecha" },
                    fontSize = 12.sp,
                    color = colores.onBackground
                )
            }

            OutlinedButton(
                onClick = {
                    val calendario = Calendar.getInstance()
                    val partes = hora.split(":")

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
                        partes.getOrNull(0)?.toIntOrNull()
                            ?: calendario.get(Calendar.HOUR_OF_DAY),
                        partes.getOrNull(1)?.toIntOrNull()
                            ?: calendario.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Schedule,
                    null,
                    tint = AzulEv
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    hora.ifBlank { "Hora" },
                    fontSize = 12.sp,
                    color = colores.onBackground
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
                mensajeLugar = null
                mostrarRegistrarLugar = true
            }
        ) {
            Icon(
                Icons.Default.AddCircleOutline,
                contentDescription = null,
                tint = AzulEv
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "+ Registrar nuevo lugar",
                color = AzulEv
            )
        }

        if (lugares.isEmpty()) {
            Text(
                "Todavía no hay lugares registrados. " +
                        "Registra uno para continuar.",
                color = colores.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        error?.let {
            Text(
                it,
                color = rojo,
                fontSize = 13.sp
            )
        }

        Button(
            onClick = {
                when {
                    nombre.isBlank() || descripcion.isBlank() -> {
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

                    fecha.isBlank() || hora.isBlank() -> {
                        error = "Selecciona la fecha y la hora"
                    }

                    else -> {
                        val fechaParseada = try {
                            formato.parse(fecha)
                        } catch (_: Exception) {
                            null
                        }

                        val partes = hora.split(":")
                        val h = partes.getOrNull(0)?.toIntOrNull()
                        val m = partes.getOrNull(1)?.toIntOrNull()

                        if (
                            fechaParseada == null ||
                            h == null || h !in 0..23 ||
                            m == null || m !in 0..59
                        ) {
                            error = "Fecha u hora no válida"
                        } else {
                            val fechaHora = Calendar.getInstance().apply {
                                time = fechaParseada
                                set(Calendar.HOUR_OF_DAY, h)
                                set(Calendar.MINUTE, m)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            if (
                                eventoInicial == null &&
                                fechaHora.timeInMillis <=
                                System.currentTimeMillis()
                            ) {
                                error = "Selecciona una fecha y hora futuras"
                            } else {
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
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AzulEv
            )
        ) {
            Text(
                if (eventoInicial == null) "Crear evento"
                else "Guardar cambios"
            )
        }

        TextButton(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Cancelar",
                color = colores.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))
    }

    if (mostrarRegistrarLugar) {
        AlertDialog(
            onDismissRequest = {
                if (!guardandoLugar) {
                    mostrarRegistrarLugar = false
                }
            },
            containerColor = colores.surface,
            titleContentColor = colores.onSurface,
            textContentColor = colores.onSurfaceVariant,
            title = {
                Text("Registrar nuevo lugar")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Ingresa el nombre del lugar donde " +
                                "se realizará el evento.",
                        color = colores.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = nuevoLugar,
                        onValueChange = {
                            nuevoLugar = it
                            mensajeLugar = null
                        },
                        label = { Text("Nombre del lugar") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !guardandoLugar,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulEv,
                            focusedTextColor = colores.onSurface,
                            unfocusedTextColor = colores.onSurface
                        )
                    )

                    mensajeLugar?.let {
                        Text(
                            it,
                            color = rojo,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !guardandoLugar,
                    onClick = {
                        val nombreLimpio = nuevoLugar.trim()

                        when {
                            nombreLimpio.isBlank() -> {
                                mensajeLugar = "Ingresa el nombre del lugar"
                            }

                            lugares.any {
                                it.nombre.equals(
                                    nombreLimpio,
                                    ignoreCase = true
                                )
                            } -> {
                                mensajeLugar = "Este lugar ya está registrado"
                            }

                            else -> {
                                guardandoLugar = true

                                onRegistrarLugar(nombreLimpio) {
                                        exitoso, resultado ->

                                    guardandoLugar = false

                                    if (exitoso) {
                                        mostrarRegistrarLugar = false
                                        nuevoLugar = ""
                                        error = null
                                    } else {
                                        mensajeLugar = resultado
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
                    onClick = {
                        mostrarRegistrarLugar = false
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
}
