package com.example.acameet.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.example.acameet.data.local.database.AcaMeetDatabase
import com.example.acameet.data.repository.UsuarioRepositoryImpl
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

// =====================================================
// RUTAS DE NAVEGACIÓN
// =====================================================

private object Rutas {

    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val INICIO = "inicio"
    const val CATEGORIAS = "categorias"
    const val EVENTOS = "eventos"
    const val LUGARES = "lugares"
    const val CONFIGURACION = "configuracion"
}

// =====================================================
// NAVEGACIÓN ENTRE PANTALLAS
// =====================================================

private fun NavHostController.irAPantalla(
    ruta: String
) {
    navigate(ruta) {

        launchSingleTop = true

        restoreState = true

        popUpTo(graph.startDestinationId) {
            saveState = true
        }
    }
}

// =====================================================
// NAVEGACIÓN PRINCIPAL DE ACAMEET
// =====================================================

@Composable
fun AppNavigation() {

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    val navController = rememberNavController()

    // =====================================================
    // SESIÓN DEL USUARIO
    // =====================================================

    val usuarioIdSesion = remember {
        MutableStateFlow<Int?>(null)
    }

    val usuarioIdActivo by usuarioIdSesion
        .collectAsStateWithLifecycle()

    val sesionActiva =
        usuarioIdActivo != null && usuarioIdActivo!! > 0

    // =====================================================
    // BASE DE DATOS ROOM
    // =====================================================

    val database = remember(context) {

        AcaMeetDatabase.getDatabase(
            context.applicationContext
        )
    }

    // =====================================================
    // REPOSITORIOS
    // =====================================================

    val usuarioRepository = remember(database) {

        UsuarioRepositoryImpl(
            database.usuarioDao()
        )
    }

    val categoriaRepository = remember(database) {

        CategoriaRepositoryImpl(
            database.categoriaDao()
        )
    }

    val lugarRepository = remember(database) {

        LugarRepositoryImpl(
            database.lugarDao()
        )
    }

    val eventoRepository = remember(database) {

        EventoRepositoryImpl(
            eventoDao = database.eventoDao(),
            categoriaDao = database.categoriaDao(),
            lugarDao = database.lugarDao()
        )
    }

    val preferencesRepository = remember(context) {

        UserPreferencesRepositoryImpl(
            context.applicationContext
        )
    }

    // =====================================================
    // VIEWMODELS
    // =====================================================

    val usuarioViewModel: UsuarioViewModel = viewModel(
        factory = UsuarioViewModelFactory(
            usuarioRepository
        )
    )

    val categoriaViewModel: CategoriaViewModel = viewModel(
        factory = CategoriaViewModelFactory(
            repository = categoriaRepository,
            usuarioIdActivo = usuarioIdSesion
        )
    )

    val lugarViewModel: LugarViewModel = viewModel(
        factory = LugarViewModelFactory(
            repository = lugarRepository,
            usuarioIdActivo = usuarioIdSesion
        )
    )

    val eventoViewModel: EventoViewModel = viewModel(
        factory = EventoViewModelFactory(
            repository = eventoRepository,
            usuarioIdActivo = usuarioIdSesion
        )
    )

    val configuracionViewModel: ConfiguracionViewModel =
        viewModel(
            factory = ConfiguracionViewModelFactory(
                preferencesRepository,
                usuarioRepository
            )
        )

    // =====================================================
    // PREFERENCIAS DEL USUARIO
    // =====================================================

    val preferencias by preferencesRepository
        .userPreferencesFlow
        .collectAsStateWithLifecycle(
            initialValue = null
        )

    // =====================================================
    // ESTADO DE NAVEGACIÓN DE EVENTOS
    // =====================================================

    var pantallaInicialEvento by remember {
        mutableStateOf("lista")
    }

    var eventoIdSeleccionado by remember {
        mutableStateOf<Int?>(null)
    }

    fun limpiarNavegacionEventos() {

        pantallaInicialEvento = "lista"

        eventoIdSeleccionado = null
    }

    fun limpiarEstadoSesion() {

        limpiarNavegacionEventos()

        eventoViewModel.limpiarFiltros()
    }

    fun abrirListaEventos() {

        limpiarEstadoSesion()

        navController.irAPantalla(
            Rutas.EVENTOS
        )
    }

    // =====================================================
    // PROTEGER RUTAS PRIVADAS
    // =====================================================

    fun comprobarSesion(): Boolean {

        if (!sesionActiva) {

            navController.navigate(Rutas.LOGIN) {

                popUpTo(navController.graph.id) {
                    inclusive = true
                }

                launchSingleTop = true
                restoreState = false
            }

            return false
        }

        return true
    }

    // =====================================================
    // RECUPERAR SESIÓN AL ABRIR LA APP
    // =====================================================

    LaunchedEffect(Unit) {

        delay(3000)

        var destino = Rutas.LOGIN

        try {

            val prefs = preferencesRepository
                .userPreferencesFlow
                .first()

            val idGuardado = prefs.usuarioIdGuardado

            if (
                prefs.recordarSesion &&
                idGuardado != null &&
                idGuardado > 0
            ) {

                val usuario = usuarioRepository
                    .obtenerUsuarioPorId(idGuardado)

                if (usuario != null) {

                    limpiarEstadoSesion()

                    usuarioIdSesion.value = usuario.id

                    destino = Rutas.INICIO

                } else {

                    preferencesRepository.cerrarSesion()
                }
            }

        } catch (e: Exception) {

            usuarioIdSesion.value = null

            destino = Rutas.LOGIN
        }

        navController.navigate(destino) {

            popUpTo(Rutas.SPLASH) {
                inclusive = true
            }

            launchSingleTop = true
        }
    }

    // =====================================================
    // TEMA CLARO Y OSCURO
    // =====================================================

    // Esperar a que DataStore entregue la preferencia guardada.
    // Así no se muestra temporalmente el modo oscuro.
    val preferenciasCargadas = preferencias

    if (preferenciasCargadas != null) {
        AcaMeetTheme(
            darkTheme = preferenciasCargadas.temaOscuro,
            dynamicColor = false
        ) {

            NavHost(
                navController = navController,
                startDestination = Rutas.SPLASH
            ) {

                // =================================================
                // SPLASH
                // =================================================

                composable(Rutas.SPLASH) {

                    SplashScreen()
                }

                // =================================================
                // LOGIN
                // =================================================

                composable(Rutas.LOGIN) {

                    LoginRoute(
                        viewModel = usuarioViewModel,

                        onLoginSuccess = { usuarioId ->

                            if (usuarioId > 0) {

                                limpiarEstadoSesion()

                                usuarioIdSesion.value = usuarioId

                                scope.launch {

                                    try {

                                        preferencesRepository
                                            .guardarUsuarioSesion(usuarioId)

                                    } catch (e: Exception) {

                                        // La sesión actual continúa activa.
                                    }
                                }

                                navController.navigate(Rutas.INICIO) {

                                    popUpTo(Rutas.LOGIN) {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                    restoreState = false
                                }
                            }
                        },

                        onNavigateToRegister = {

                            navController.navigate(
                                Rutas.REGISTER
                            )
                        }
                    )
                }

                // =================================================
                // REGISTRO
                // =================================================

                composable(Rutas.REGISTER) {

                    RegisterRoute(
                        viewModel = usuarioViewModel,

                        onRegisterSuccess = {

                            navController.popBackStack(
                                Rutas.LOGIN,
                                inclusive = false
                            )
                        },

                        onNavigateToLogin = {

                            navController.popBackStack(
                                Rutas.LOGIN,
                                inclusive = false
                            )
                        }
                    )
                }

                // =================================================
                // INICIO
                // =================================================

                composable(Rutas.INICIO) {

                    if (sesionActiva) {

                        HomeScreen(
                            eventoViewModel = eventoViewModel,
                            categoriaViewModel = categoriaViewModel,
                            lugarViewModel = lugarViewModel,

                            onCategoriasClick = {

                                navController.irAPantalla(
                                    Rutas.CATEGORIAS
                                )
                            },

                            onEventosClick = {

                                abrirListaEventos()
                            },

                            onConfiguracionClick = {

                                navController.irAPantalla(
                                    Rutas.CONFIGURACION
                                )
                            },

                            onCrearEventoClick = {

                                pantallaInicialEvento = "crear"

                                eventoIdSeleccionado = null

                                navController.irAPantalla(
                                    Rutas.EVENTOS
                                )
                            },

                            onDetalleEventoClick = { id ->

                                pantallaInicialEvento = "detalle"

                                eventoIdSeleccionado = id

                                navController.irAPantalla(
                                    Rutas.EVENTOS
                                )
                            }
                        )

                    } else {

                        LaunchedEffect(Unit) {
                            comprobarSesion()
                        }
                    }
                }

                // =================================================
                // CATEGORÍAS Y LUGARES
                // =================================================

                composable(Rutas.CATEGORIAS) {

                    if (sesionActiva) {

                        CategoriaScreen(
                            viewModel = categoriaViewModel,
                            lugarViewModel = lugarViewModel,

                            // USUARIO QUE INICIÓ SESIÓN
                            usuarioIdActivo = usuarioIdActivo,

                            onInicioClick = {

                                navController.irAPantalla(
                                    Rutas.INICIO
                                )
                            },

                            onEventosClick = {

                                abrirListaEventos()
                            },

                            onConfiguracionClick = {

                                navController.irAPantalla(
                                    Rutas.CONFIGURACION
                                )
                            }
                        )

                    } else {

                        LaunchedEffect(Unit) {
                            comprobarSesion()
                        }
                    }
                }

                // =================================================
                // EVENTOS
                // =================================================

                composable(Rutas.EVENTOS) {

                    if (sesionActiva) {

                        EventoScreen(
                            viewModel = eventoViewModel,
                            categoriaViewModel = categoriaViewModel,
                            lugarViewModel = lugarViewModel,

                            // USUARIO QUE INICIÓ SESIÓN
                            usuarioIdActivo = usuarioIdActivo,

                            pantallaInicial = pantallaInicialEvento,

                            eventoIdInicial = eventoIdSeleccionado,

                            onInicioClick = {

                                limpiarEstadoSesion()

                                navController.irAPantalla(
                                    Rutas.INICIO
                                )
                            },

                            onCategoriasClick = {

                                limpiarNavegacionEventos()

                                navController.irAPantalla(
                                    Rutas.CATEGORIAS
                                )
                            },

                            onConfiguracionClick = {

                                limpiarNavegacionEventos()

                                navController.irAPantalla(
                                    Rutas.CONFIGURACION
                                )
                            }
                        )

                    } else {

                        LaunchedEffect(Unit) {
                            comprobarSesion()
                        }
                    }
                }

                // =================================================
                // LUGARES
                // =================================================

                composable(Rutas.LUGARES) {

                    if (sesionActiva) {

                        LugarScreen(
                            viewModel = lugarViewModel,

                            // USUARIO QUE INICIÓ SESIÓN
                            usuarioIdActivo = usuarioIdActivo,

                            onInicioClick = {

                                navController.irAPantalla(
                                    Rutas.INICIO
                                )
                            }
                        )

                    } else {

                        LaunchedEffect(Unit) {
                            comprobarSesion()
                        }
                    }
                }

                // =================================================
                // CONFIGURACIÓN
                // =================================================

                composable(Rutas.CONFIGURACION) {

                    if (sesionActiva) {

                        ConfiguracionRoute(
                            viewModel = configuracionViewModel,

                            usuarioId = usuarioIdActivo,

                            onLogout = {

                                scope.launch {

                                    try {

                                        preferencesRepository
                                            .cerrarSesion()

                                        // DESACTIVAR USUARIO
                                        usuarioIdSesion.value = null

                                        // LIMPIAR FILTROS Y EVENTOS
                                        limpiarEstadoSesion()

                                        // ELIMINAR HISTORIAL ANTERIOR
                                        navController.navigate(
                                            Rutas.LOGIN
                                        ) {

                                            popUpTo(
                                                navController.graph.id
                                            ) {
                                                inclusive = true
                                            }

                                            launchSingleTop = true
                                            restoreState = false
                                        }

                                    } catch (e: Exception) {

                                        // Se conserva la sesión si
                                        // no se pudo actualizar
                                        // las preferencias.
                                    }
                                }
                            },

                            onInicioClick = {

                                navController.irAPantalla(
                                    Rutas.INICIO
                                )
                            },

                            onCategoriasClick = {

                                navController.irAPantalla(
                                    Rutas.CATEGORIAS
                                )
                            },

                            onEventosClick = {

                                abrirListaEventos()
                            }
                        )

                    } else {

                        LaunchedEffect(Unit) {
                            comprobarSesion()
                        }
                    }
                }
            }
        }
    }
}
