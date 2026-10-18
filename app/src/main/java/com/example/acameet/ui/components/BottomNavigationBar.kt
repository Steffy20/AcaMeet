
package com.example.acameet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavigationBar(
    pantallaActual: String,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {}
) {

    val colores = MaterialTheme.colorScheme

    // Detectar el tema activo desde Theme.kt
    val esOscuro = colores.background == Color(0xFF020B12)

    // Azul original de AcaMeet
    val azul = Color(0xFF0798F2)

    // Colores según el tema
    val fondo = if (esOscuro) {
        Color(0xFF06141E)
    } else {
        Color.White
    }

    val gris = if (esOscuro) {
        Color(0xFF9BA8B4)
    } else {
        Color(0xFF657786)
    }

    val indicador = if (esOscuro) {
        Color(0xFF102E42)
    } else {
        Color(0xFFE2F0F9)
    }

    // Colores compartidos por todos los botones
    val coloresBotones = NavigationBarItemDefaults.colors(
        selectedIconColor = azul,
        selectedTextColor = azul,
        unselectedIconColor = gris,
        unselectedTextColor = gris,
        indicatorColor = indicador
    )

    NavigationBar(
        containerColor = fondo,
        contentColor = colores.onSurface
    ) {

        // INICIO
        NavigationBarItem(
            selected = pantallaActual == "inicio",
            onClick = onInicioClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio"
                )
            },
            label = {
                Text("Inicio")
            },
            colors = coloresBotones
        )

        // CATEGORÍAS
        NavigationBarItem(
            selected = pantallaActual == "categorias",
            onClick = onCategoriasClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = "Categorías"
                )
            },
            label = {
                Text("Categorías")
            },
            colors = coloresBotones
        )

        // EVENTOS
        NavigationBarItem(
            selected = pantallaActual == "eventos",
            onClick = onEventosClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Eventos"
                )
            },
            label = {
                Text("Eventos")
            },
            colors = coloresBotones
        )

        // CONFIGURACIÓN
        NavigationBarItem(
            selected = pantallaActual == "configuracion",
            onClick = onConfiguracionClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración"
                )
            },
            label = {
                Text("Configuración")
            },
            colors = coloresBotones
        )
    }
}
