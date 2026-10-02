package com.example.acameet.navigation


import androidx.compose.runtime.*
import com.example.acameet.ui.screens.LoginScreen
import com.example.acameet.SplashScreen
import kotlinx.coroutines.delay
import com.example.acameet.ui.screens.RegisterScreen

@Composable
fun AppNavigation() {

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
                    // Más adelante llevará a Inicio
                },
                onRegisterClick = {
                    pantallaActual = "register"
                }
            )
        }

        "register" -> {
            RegisterScreen(
                onRegisterClick = {
                    // Más adelante guardará el usuario con Room
                },
                onLoginClick = {
                    pantallaActual = "login"
                }
            )
        }
    }
}