
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

@Composable
fun AppNavigation() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    // BASE DE DATOS ROOM
    val database = remember(context) {
        AcaMeetDatabase.getDatabase(
            context.applicationContext
        )
    }

    // REPOSITORIOS
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
            database.eventoDao()
        )
    }

    val preferencesRepository = remember(context) {
        UserPreferencesRepositoryImpl(
            context.applicationContext
        )
    }

    // VIEWMODELS CON FÁBRICAS MANUALES
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

    val configuracionViewModel: ConfiguracionViewModel =
        viewModel(
            factory = ConfiguracionViewModelFactory(
                preferencesRepository,
                usuarioRepository
            )
        )

    // PREFERENCIAS
    val preferencias by preferencesRepository
        .userPreferencesFlow
        .collectAsStateWithLifecycle(
            initialValue = null
        )

    // SESIÓN ACTUAL
    var usuarioIdActivo by remember {
        mutableStateOf<Int?>(null)
    }

    // ESTADO INTERNO DE EVENTOS
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

    fun abrirListaEventos() {
        limpiarNavegacionEventos()
        eventoViewModel.limpiarFiltros()
        navController.irAPantalla(Rutas.EVENTOS)
    }

    // RECUPERAR SESIÓN DESPUÉS DEL SPLASH
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
                idGuardado != null
            ) {
                val usuario = usuarioRepository
                    .obtenerUsuarioPorId(idGuardado)

                if (usuario != null) {
                    usuarioIdActivo = usuario.id
                    destino = Rutas.INICIO
                } else {
                    preferencesRepository.cerrarSesion()
                }
            }
        } catch (e: Exception) {
            destino = Rutas.LOGIN
        }

        navController.navigate(destino) {
            popUpTo(Rutas.SPLASH) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    // TEMA OSCURO / CLARO
    AcaMeetTheme(
        darkTheme = preferencias?.temaOscuro ?: true,
        dynamicColor = false
    ) {

        NavHost(
            navController = navController,
            startDestination = Rutas.SPLASH
        ) {

            // SPLASH
            composable(Rutas.SPLASH) {
                SplashScreen()
            }

            // INICIAR SESIÓN
            composable(Rutas.LOGIN) {

                LoginRoute(
                    viewModel = usuarioViewModel,

                    onLoginSuccess = { usuarioId ->

                        usuarioIdActivo = usuarioId

                        scope.launch {
                            try {
                                preferencesRepository
                                    .guardarUsuarioSesion(usuarioId)
                            } catch (e: Exception) {
                                // La sesión activa continúa.
                            }
                        }

                        navController.navigate(Rutas.INICIO) {
                            popUpTo(Rutas.LOGIN) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },

                    onNavigateToRegister = {
                        navController.navigate(Rutas.REGISTER)
                    }
                )
            }

            // REGISTRO
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

            // INICIO
            composable(Rutas.INICIO) {

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
            }

            // CATEGORÍAS
            composable(Rutas.CATEGORIAS) {

                CategoriaScreen(
                    viewModel = categoriaViewModel,

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
            }

            // EVENTOS
            composable(Rutas.EVENTOS) {

                EventoScreen(
                    viewModel = eventoViewModel,
                    categoriaViewModel = categoriaViewModel,
                    lugarViewModel = lugarViewModel,

                    pantallaInicial = pantallaInicialEvento,
                    eventoIdInicial = eventoIdSeleccionado,

                    onInicioClick = {
                        limpiarNavegacionEventos()
                        eventoViewModel.limpiarFiltros()

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
            }

            // LUGARES
            composable(Rutas.LUGARES) {

                LugarScreen(
                    viewModel = lugarViewModel,

                    onInicioClick = {
                        navController.irAPantalla(
                            Rutas.INICIO
                        )
                    }
                )
            }

            // CONFIGURACIÓN
            composable(Rutas.CONFIGURACION) {

                ConfiguracionRoute(
                    viewModel = configuracionViewModel,
                    usuarioId = usuarioIdActivo,

                    onLogout = {
                        scope.launch {
                            try {
                                preferencesRepository
                                    .cerrarSesion()

                                usuarioIdActivo = null
                                limpiarNavegacionEventos()
                                eventoViewModel.limpiarFiltros()

                                navController.navigate(
                                    Rutas.LOGIN
                                ) {
                                    popUpTo(
                                        navController.graph.id
                                    ) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }

                            } catch (e: Exception) {
                                // Evita cerrar sesión si
                                // falla el guardado.
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
            }
        }
    }
}
