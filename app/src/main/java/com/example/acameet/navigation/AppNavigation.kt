package com.example.acameet.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.acameet.SplashScreen
import com.example.acameet.data.local.database.AcaMeetDatabase
import com.example.acameet.data.repository.CategoriaRepository
import com.example.acameet.data.repository.UsuarioRepository
import com.example.acameet.ui.screens.CategoriaScreen
import com.example.acameet.ui.screens.HomeScreen
import com.example.acameet.ui.screens.LoginScreen
import com.example.acameet.ui.screens.RegisterScreen
import com.example.acameet.ui.viewmodel.CategoriaViewModel
import com.example.acameet.ui.viewmodel.CategoriaViewModelFactory
import com.example.acameet.ui.viewmodel.UsuarioViewModel
import com.example.acameet.ui.viewmodel.UsuarioViewModelFactory
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {

    val context = LocalContext.current

    // Base de datos
    val database = remember {
        AcaMeetDatabase.getDatabase(context)
    }

    // Repository de usuarios
    val usuarioRepository = remember {
        UsuarioRepository(database.usuarioDao())
    }

    // ViewModel de usuarios
    val usuarioViewModel: UsuarioViewModel = viewModel(
        factory = UsuarioViewModelFactory(usuarioRepository)
    )

    // Repository de categorías
    val categoriaRepository = remember {
        CategoriaRepository(database.categoriaDao())
    }

    // ViewModel de categorías
    val categoriaViewModel: CategoriaViewModel = viewModel(
        factory = CategoriaViewModelFactory(categoriaRepository)
    )

    var pantallaActual by remember {
        mutableStateOf("splash")
    }

    LaunchedEffect(Unit) {
        delay(3000)
        pantallaActual = "login"
    }

    when (pantallaActual) {

        "splash" -> {
            SplashScreen()
        }

        "login" -> {
            LoginScreen(
                onLoginClick = { correo, contrasena ->

                    usuarioViewModel.iniciarSesion(
                        correo = correo,
                        contrasena = contrasena
                    ) { exitoso, _ ->

                        if (exitoso) {
                            pantallaActual = "inicio"
                        }
                    }
                },
                onRegisterClick = {
                    pantallaActual = "register"
                }
            )
        }

        "register" -> {
            RegisterScreen(
                onRegisterClick = {
                        nombre,
                        correo,
                        contrasena,
                        confirmarContrasena ->

                    usuarioViewModel.registrarUsuario(
                        nombre = nombre,
                        correo = correo,
                        contrasena = contrasena,
                        confirmarContrasena = confirmarContrasena
                    ) { exitoso, _ ->

                        if (exitoso) {
                            pantallaActual = "login"
                        }
                    }
                },
                onLoginClick = {
                    pantallaActual = "login"
                }
            )
        }

        "inicio" -> {
            HomeScreen(
                onCategoriasClick = {
                    pantallaActual = "categorias"
                }
            )
        }

        "categorias" -> {
            CategoriaScreen(
                viewModel = categoriaViewModel,
                onInicioClick = {
                    pantallaActual = "inicio"
                }
            )
        }
    }
}