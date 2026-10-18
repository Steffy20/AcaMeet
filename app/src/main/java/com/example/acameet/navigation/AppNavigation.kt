
package com.example.acameet.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.example.acameet.data.local.database.AcaMeetDatabase

import com.example.acameet.data.repository.UsuarioRepository
import com.example.acameet.data.repository.CategoriaRepositoryImpl
import com.example.acameet.data.repository.LugarRepositoryImpl
import com.example.acameet.data.repository.EventoRepositoryImpl
import com.example.acameet.data.repository.UserPreferencesRepositoryImpl

import com.example.acameet.ui.viewmodel.UsuarioViewModel
import com.example.acameet.ui.viewmodel.UsuarioViewModelFactory

import com.example.acameet.ui.viewmodel.CategoriaViewModel
import com.example.acameet.ui.viewmodel.CategoriaViewModelFactory

import com.example.acameet.ui.viewmodel.LugarViewModel
import com.example.acameet.ui.viewmodel.LugarViewModelFactory

import com.example.acameet.ui.viewmodel.ConfiguracionViewModel
import com.example.acameet.ui.viewmodel.ConfiguracionViewModelFactory

import com.example.acameet.ui.EventoViewModel
import com.example.acameet.ui.EventoViewModelFactory

import com.example.acameet.ui.theme.AcaMeetTheme

import com.example.acameet.ui.screens.*

@Composable
fun AppNavigation() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // BASE DE DATOS ROOM
    val database = remember(context) {
        AcaMeetDatabase.getDatabase(context)
    }

    // REPOSITORIOS
    val usuarioRepository = remember(database) {
        UsuarioRepository(database.usuarioDao())
    }

    val categoriaRepository = remember(database) {
        CategoriaRepositoryImpl(database.categoriaDao())
    }

    val lugarRepository = remember(database) {
        LugarRepositoryImpl(database.lugarDao())
    }

    val eventoRepository = remember(database) {
        EventoRepositoryImpl(database.eventoDao())
    }

    val preferencesRepository = remember(context) {
        UserPreferencesRepositoryImpl(
            context.applicationContext
        )
    }

    // VIEWMODELS
    val usuarioViewModel: UsuarioViewModel = viewModel(
        factory = UsuarioViewModelFactory(
            usuarioRepository
        )
    )

    val categoriaViewModel: CategoriaViewModel = viewModel(
        factory = CategoriaViewModelFactory(
            categoriaRepository
        )
    )

    val lugarViewModel: LugarViewModel = viewModel(
        factory = LugarViewModelFactory(
            lugarRepository
        )
    )

    val eventoViewModel: EventoViewModel = viewModel(
        factory = EventoViewModelFactory(
            eventoRepository
        )
    )

    val configuracionViewModel: ConfiguracionViewModel = viewModel(
        factory = ConfiguracionViewModelFactory(
            preferencesRepository,
            usuarioRepository
        )
    )

    // PREFERENCIAS GUARDADAS EN DATASTORE
    val preferencias by preferencesRepository
        .userPreferencesFlow
        .collectAsState(initial = null)

    // ESTADOS DE NAVEGACIÓN
    var pantallaActual by remember {
        mutableStateOf("splash")
    }

    var usuarioIdActivo by remember {
        mutableStateOf<Int?>(null)
    }

    var pantallaInicialEvento by remember {
        mutableStateOf("lista")
    }

    var eventoIdSeleccionado by remember {
        mutableStateOf<Int?>(null)
    }

    // RECUPERAR SESIÓN AL ABRIR LA APLICACIÓN
    LaunchedEffect(Unit) {

        delay(3000)

        try {
            val prefs = preferencesRepository
                .userPreferencesFlow
                .first()

            val idGuardado = prefs.usuarioIdGuardado

            if (
                prefs.recordarSesion &&
                idGuardado != null
            ) {
                val usuario = usuarioRepository
                    .obtenerUsuarioPorId(idGuardado)

                if (usuario != null) {
                    usuarioIdActivo = usuario.id
                    pantallaActual = "inicio"
                } else {
                    preferencesRepository.cerrarSesion()
                    pantallaActual = "login"
                }

            } else {
                pantallaActual = "login"
            }

        } catch (e: Exception) {
            pantallaActual = "login"
        }
    }

    // APLICAR TEMA OSCURO O CLARO
    AcaMeetTheme(
        darkTheme = preferencias?.temaOscuro ?: true,
        dynamicColor = false
    ) {

        when (pantallaActual) {

            // SPLASH
            "splash" -> {
                SplashScreen()
            }

            // INICIAR SESIÓN
            "login" -> {
                LoginRoute(
                    viewModel = usuarioViewModel,

                    onLoginSuccess = { usuarioId ->

                        usuarioIdActivo = usuarioId

                        scope.launch {
                            try {
                                preferencesRepository
                                    .guardarUsuarioSesion(usuarioId)
                            } catch (e: Exception) {
                                // La sesión actual continúa aunque
                                // falle el guardado de la preferencia.
                            }
                        }

                        pantallaActual = "inicio"
                    },

                    onNavigateToRegister = {
                        pantallaActual = "register"
                    }
                )
            }

            // REGISTRAR USUARIO
            "register" -> {
                RegisterRoute(
                    viewModel = usuarioViewModel,

                    onRegisterSuccess = {
                        pantallaActual = "login"
                    },

                    onNavigateToLogin = {
                        pantallaActual = "login"
                    }
                )
            }

            // INICIO
            "inicio" -> {
                HomeScreen(
                    eventoViewModel = eventoViewModel,
                    categoriaViewModel = categoriaViewModel,
                    lugarViewModel = lugarViewModel,

                    onCategoriasClick = {
                        pantallaActual = "categorias"
                    },

                    onEventosClick = {
                        pantallaInicialEvento = "lista"
                        eventoIdSeleccionado = null

                        eventoViewModel.limpiarFiltros()

                        pantallaActual = "eventos"
                    },

                    onConfiguracionClick = {
                        pantallaActual = "configuracion"
                    },

                    onCrearEventoClick = {
                        pantallaInicialEvento = "crear"
                        eventoIdSeleccionado = null

                        pantallaActual = "eventos"
                    },

                    onDetalleEventoClick = { id ->
                        pantallaInicialEvento = "detalle"
                        eventoIdSeleccionado = id

                        pantallaActual = "eventos"
                    }
                )
            }

            // CATEGORÍAS
            "categorias" -> {
                CategoriaScreen(
                    viewModel = categoriaViewModel,

                    onInicioClick = {
                        pantallaActual = "inicio"
                    },

                    onEventosClick = {
                        pantallaInicialEvento = "lista"
                        eventoIdSeleccionado = null

                        eventoViewModel.limpiarFiltros()

                        pantallaActual = "eventos"
                    },

                    onConfiguracionClick = {
                        pantallaActual = "configuracion"
                    }
                )
            }

            // EVENTOS
            "eventos" -> {
                EventoScreen(
                    viewModel = eventoViewModel,
                    categoriaViewModel = categoriaViewModel,
                    lugarViewModel = lugarViewModel,

                    pantallaInicial = pantallaInicialEvento,
                    eventoIdInicial = eventoIdSeleccionado,

                    onInicioClick = {
                        pantallaInicialEvento = "lista"
                        eventoIdSeleccionado = null

                        eventoViewModel.limpiarFiltros()

                        pantallaActual = "inicio"
                    },

                    onCategoriasClick = {
                        pantallaInicialEvento = "lista"
                        eventoIdSeleccionado = null

                        pantallaActual = "categorias"
                    },

                    onConfiguracionClick = {
                        pantallaInicialEvento = "lista"
                        eventoIdSeleccionado = null

                        pantallaActual = "configuracion"
                    }
                )
            }

            // LUGARES
            "lugares" -> {
                LugarScreen(
                    viewModel = lugarViewModel,

                    onInicioClick = {
                        pantallaActual = "inicio"
                    }
                )
            }

            // CONFIGURACIÓN
            "configuracion" -> {
                ConfiguracionRoute(
                    viewModel = configuracionViewModel,
                    usuarioId = usuarioIdActivo,

                    onLogout = {

                        scope.launch {
                            try {
                                preferencesRepository.cerrarSesion()

                                usuarioIdActivo = null
                                pantallaInicialEvento = "lista"
                                eventoIdSeleccionado = null

                                eventoViewModel.limpiarFiltros()

                                pantallaActual = "login"

                            } catch (e: Exception) {
                                // Mantener la sesión si falla
                                // la eliminación del ID guardado.
                            }
                        }
                    },

                    onInicioClick = {
                        pantallaActual = "inicio"
                    },

                    onCategoriasClick = {
                        pantallaActual = "categorias"
                    },

                    onEventosClick = {
                        pantallaInicialEvento = "lista"
                        eventoIdSeleccionado = null

                        eventoViewModel.limpiarFiltros()

                        pantallaActual = "eventos"
                    }
                )
            }
        }
    }
}
