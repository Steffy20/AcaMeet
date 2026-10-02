package com.example.acameet.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.acameet.SplashScreen
import com.example.acameet.data.local.database.AcaMeetDatabase
import com.example.acameet.data.repository.UsuarioRepository
import com.example.acameet.ui.screens.LoginScreen
import com.example.acameet.ui.screens.RegisterScreen
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

    // Repository
    val repository = remember {
        UsuarioRepository(database.usuarioDao())
    }

    // ViewModel
    val usuarioViewModel: UsuarioViewModel = viewModel(
        factory = UsuarioViewModelFactory(repository)
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
                onLoginClick = {
                    // Más adelante conectaremos el inicio de sesión
                },
                onRegisterClick = {
                    pantallaActual = "register"
                }
            )
        }

        "register" -> {
            RegisterScreen(
                onRegisterClick = { nombre,
                                    correo,
                                    contrasena,
                                    confirmarContrasena ->

                    usuarioViewModel.registrarUsuario(
                        nombre = nombre,
                        correo = correo,
                        contrasena = contrasena,
                        confirmarContrasena = confirmarContrasena
                    ) { exitoso, mensaje ->

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
    }
}