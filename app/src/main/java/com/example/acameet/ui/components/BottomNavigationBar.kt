package com.example.acameet.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavigationBar(
    pantallaActual: String,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onLugaresClick: () -> Unit = {},
    onConfiguracionClick: () -> Unit = {}
) {

    val azul = Color(0xFF0798F2)
    val gris = Color(0xFF9BA8B4)
    val indicador = Color(0xFF102A3A)

    NavigationBar(
        containerColor = Color(0xFF06141E)
    ) {

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
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = azul,
                selectedTextColor = azul,
                indicatorColor = indicador,
                unselectedIconColor = gris,
                unselectedTextColor = gris
            )
        )

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
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = azul,
                selectedTextColor = azul,
                indicatorColor = indicador,
                unselectedIconColor = gris,
                unselectedTextColor = gris
            )
        )

        NavigationBarItem(
            selected = pantallaActual == "lugares",
            onClick = onLugaresClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Lugares"
                )
            },
            label = {
                Text("Lugares")
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = azul,
                selectedTextColor = azul,
                indicatorColor = indicador,
                unselectedIconColor = gris,
                unselectedTextColor = gris
            )
        )

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
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = azul,
                selectedTextColor = azul,
                indicatorColor = indicador,
                unselectedIconColor = gris,
                unselectedTextColor = gris
            )
        )
    }
}