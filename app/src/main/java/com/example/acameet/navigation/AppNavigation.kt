
package com.example.acameet.navigation

import android.app.NotificationManager
import android.content.Context
import android.util.Log

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import kotlinx.coroutines.CancellationException
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
import com.example.acameet.data.repository.NotificacionRepositoryImpl

import com.example.acameet.ui.viewmodel.UsuarioViewModel
import com.example.acameet.ui.viewmodel.UsuarioViewModelFactory
import com.example.acameet.ui.viewmodel.CategoriaViewModel
import com.example.acameet.ui.viewmodel.CategoriaViewModelFactory
import com.example.acameet.ui.viewmodel.LugarViewModel
import com.example.acameet.ui.viewmodel.LugarViewModelFactory
import com.example.acameet.ui.viewmodel.ConfiguracionViewModel
import com.example.acameet.ui.viewmodel.ConfiguracionViewModelFactory
import com.example.acameet.ui.viewmodel.NotificacionViewModel
import com.example.acameet.ui.viewmodel.NotificacionViewModelFactory

import com.example.acameet.ui.EventoViewModel
import com.example.acameet.ui.EventoViewModelFactory

import com.example.acameet.ui.theme.AcaMeetTheme
import com.example.acameet.ui.screens.*
import com.example.acameet.util.RecordatorioEventoScheduler

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
// FUNCIÓN PARA NAVEGAR ENTRE PANTALLAS
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
// NAVEGACIÓN PRINCIPAL
// =====================================================

@Composable
fun AppNavigation(
    eventoDesdeNotificacion: Pair<Int, Int>? = null,
    onNotificacionProcesada: () -> Unit = {}
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // =================================================
    // SESIÓN DEL USUARIO
    // =================================================

    val usuarioIdSesion = remember {
        MutableStateFlow<Int?>(null)
    }

    val usuarioIdActivo by usuarioIdSesion
        .collectAsStateWithLifecycle()

    val sesionActiva =
        usuarioIdActivo != null &&
                usuarioIdActivo!! > 0

    var inicioCompletado by remember {
        mutableStateOf(false)
    }

    // =================================================
    // BASE DE DATOS ROOM
    // =================================================

    val database = remember(context) {
        AcaMeetDatabase.getDatabase(
            context.applicationContext
        )
    }

    // =================================================
    // REPOSITORIOS
    // =================================================

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

    // =================================================
    // REPOSITORIO DE NOTIFICACIONES
    // =================================================

    val notificacionRepository = remember(database) {
        NotificacionRepositoryImpl(
            database.notificacionDao()
        )
    }

    // =================================================
    // PROGRAMADOR DE RECORDATORIOS
    // =================================================

    val recordatorioScheduler = remember(context) {
        RecordatorioEventoScheduler(
            context.applicationContext
        )
    }

    // =================================================
    // NOTIFICACIONES DE ANDROID
    // =================================================

    val notificationManager = remember(context) {
        context.applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
    }

    // =================================================
    // VIEWMODELS CON FACTORY
    // SIN APPCONTAINER
    // =================================================

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
            usuarioIdActivo = usuarioIdSesion,
            recordatorioScheduler = recordatorioScheduler
        )
    )

    val configuracionViewModel: ConfiguracionViewModel = viewModel(
        factory = ConfiguracionViewModelFactory(
            preferencesRepository,
            usuarioRepository
        )
    )

    val notificacionViewModel: NotificacionViewModel = viewModel(
        factory = NotificacionViewModelFactory(
            notificacionRepository
        )
    )

    // =================================================
    // PREFERENCIAS DEL USUARIO
    // =================================================

    val preferencias by preferencesRepository
        .userPreferencesFlow
        .collectAsStateWithLifecycle(
            initialValue = null
        )

    // =================================================
    // ESTADO DE NAVEGACIÓN DE EVENTOS
    // =================================================

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

    // =================================================
    // PROTECCIÓN DE RUTAS PRIVADAS
    // =================================================

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

    // =================================================
    // RECUPERAR SESIÓN AL ABRIR LA APLICACIÓN
    // =================================================

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

                    preferencesRepository
                        .guardarUsuarioSesion(usuario.id)

                    limpiarEstadoSesion()

                    usuarioIdSesion.value = usuario.id

                    destino = Rutas.INICIO

                } else {

                    preferencesRepository.cerrarSesion()

                    usuarioIdSesion.value = null

                    notificationManager.cancelAll()
                }

            } else {

                preferencesRepository.cerrarSesion()

                usuarioIdSesion.value = null

                notificationManager.cancelAll()
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(
                "AcaMeetSesion",
                "Error al recuperar la sesión",
                e
            )

            usuarioIdSesion.value = null

            destino = Rutas.LOGIN
        }

        navController.navigate(destino) {

            popUpTo(Rutas.SPLASH) {
                inclusive = true
            }

            launchSingleTop = true
        }

        inicioCompletado = true
    }

    // =================================================
    // ABRIR EVENTO DESDE NOTIFICACIÓN DE ANDROID
    // =================================================

    LaunchedEffect(
        eventoDesdeNotificacion,
        usuarioIdActivo,
        inicioCompletado
    ) {

        val solicitud =
            eventoDesdeNotificacion ?: return@LaunchedEffect

        if (!inicioCompletado) {
            return@LaunchedEffect
        }

        val (eventoId, propietarioId) = solicitud

        val usuarioActual = usuarioIdActivo

        // ESPERAR SI NO EXISTE UNA SESIÓN ACTIVA
        if (
            usuarioActual == null ||
            usuarioActual <= 0
        ) {
            return@LaunchedEffect
        }

        // COMPROBAR QUE EL EVENTO SEA DEL USUARIO
        if (
            usuarioActual != propietarioId ||
            eventoId <= 0
        ) {

            Log.w(
                "AcaMeetNotificacion",
                "Evento omitido: usuario no autorizado"
            )

            onNotificacionProcesada()

            return@LaunchedEffect
        }

        try {

            // VERIFICAR QUE EL EVENTO EXISTA EN ROOM
            val evento = eventoRepository.obtenerEventoPorId(
                id = eventoId,
                usuarioId = usuarioActual
            )

            if (
                evento != null &&
                usuarioIdSesion.value == usuarioActual
            ) {

                pantallaInicialEvento = "detalle"

                eventoIdSeleccionado = eventoId

                navController.irAPantalla(
                    Rutas.EVENTOS
                )

            } else {

                Log.w(
                    "AcaMeetNotificacion",
                    "Evento inexistente o sesión modificada"
                )
            }

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(
                "AcaMeetNotificacion",
                "No se pudo abrir el evento",
                e
            )
        }

        onNotificacionProcesada()
    }

    // =================================================
    // TEMA CLARO Y OSCURO
    // =================================================

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

                // =====================================
                // PANTALLA SPLASH
                // =====================================

                composable(Rutas.SPLASH) {

                    SplashScreen()
                }

                // =====================================
                // INICIAR SESIÓN
                // =====================================

                composable(Rutas.LOGIN) {

                    LoginRoute(
                        viewModel = usuarioViewModel,

                        onLoginSuccess = { usuarioId ->

                            if (usuarioId > 0) {

                                scope.launch {

                                    try {

                                        preferencesRepository
                                            .guardarUsuarioSesion(
                                                usuarioId
                                            )

                                        notificationManager.cancelAll()

                                        limpiarEstadoSesion()

                                        usuarioIdSesion.value = usuarioId

                                        navController.navigate(
                                            Rutas.INICIO
                                        ) {

                                            popUpTo(Rutas.LOGIN) {
                                                inclusive = true
                                            }

                                            launchSingleTop = true
                                            restoreState = false
                                        }

                                    } catch (e: Exception) {

                                        Log.e(
                                            "AcaMeetSesion",
                                            "No se pudo guardar la sesión",
                                            e
                                        )
                                    }
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

                // =====================================
                // REGISTRO DE USUARIOS
                // =====================================

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

                // =====================================
                // PANTALLA DE INICIO
                // =====================================

                composable(Rutas.INICIO) {

                    if (sesionActiva) {

                        HomeScreen(
                            eventoViewModel = eventoViewModel,
                            categoriaViewModel = categoriaViewModel,
                            lugarViewModel = lugarViewModel,
                            notificacionViewModel = notificacionViewModel,

                            usuarioIdActivo = usuarioIdActivo,

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

                // =====================================
                // CATEGORÍAS
                // =====================================

                composable(Rutas.CATEGORIAS) {

                    if (sesionActiva) {

                        CategoriaScreen(
                            viewModel = categoriaViewModel,
                            lugarViewModel = lugarViewModel,

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

                // =====================================
                // EVENTOS
                // =====================================

                composable(Rutas.EVENTOS) {

                    if (sesionActiva) {

                        EventoScreen(
                            viewModel = eventoViewModel,
                            categoriaViewModel = categoriaViewModel,
                            lugarViewModel = lugarViewModel,

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

                // =====================================
                // LUGARES
                // =====================================

                composable(Rutas.LUGARES) {

                    if (sesionActiva) {

                        LugarScreen(
                            viewModel = lugarViewModel,

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

                // =====================================
                // CONFIGURACIÓN
                // =====================================

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

                                        notificationManager.cancelAll()

                                        usuarioIdSesion.value = null

                                        limpiarEstadoSesion()

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

                                        Log.e(
                                            "AcaMeetSesion",
                                            "Error al cerrar sesión",
                                            e
                                        )
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
