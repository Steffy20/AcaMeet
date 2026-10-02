package com.example.acameet.navigation


import androidx.compose.runtime.*
import com.example.acameet.ui.screens.LoginScreen
import com.example.acameet.SplashScreen
import kotlinx.coroutines.delay

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
                    // Luego conectaremos con Inicio
                },
                onRegisterClick = {
                    // Luego conectaremos con Crear cuenta
                }
            )
        }
    }
}