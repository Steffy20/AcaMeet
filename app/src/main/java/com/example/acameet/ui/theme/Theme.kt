
package com.example.acameet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// COLORES DEL TEMA OSCURO ORIGINAL DE ACAMEET
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0798F2),
    onPrimary = Color.White,

    secondary = Color(0xFF9BA8B4),
    onSecondary = Color(0xFF020B12),

    background = Color(0xFF020B12),
    onBackground = Color.White,

    surface = Color(0xFF0A1C28),
    onSurface = Color.White,

    surfaceVariant = Color(0xFF102B3B),
    onSurfaceVariant = Color(0xFF9BA8B4),

    outline = Color(0xFF19384B),

    error = Color(0xFFF87171),
    onError = Color.White
)

// COLORES DEL TEMA CLARO DE ACAMEET
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0798F2),
    onPrimary = Color.White,

    secondary = Color(0xFF657786),
    onSecondary = Color.White,

    background = Color(0xFFF3F7FA),
    onBackground = Color(0xFF152B3A),

    surface = Color.White,
    onSurface = Color(0xFF152B3A),

    surfaceVariant = Color(0xFFEAF1F5),
    onSurfaceVariant = Color(0xFF657786),

    outline = Color(0xFFDCE5EB),

    error = Color(0xFFB42318),
    onError = Color.White
)

@Composable
fun AcaMeetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        },
        typography = Typography,
        content = content
    )
}
