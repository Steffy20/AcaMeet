
package com.example.acameet.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.acameet.R

@Composable
fun SplashScreen() {

    val colores = MaterialTheme.colorScheme
    val azul = Color(0xFF0798F2)

    val esOscuro =
        colores.background == Color(0xFF020B12)

    val fondo = Brush.verticalGradient(
        colors = if (esOscuro) {
            listOf(
                Color(0xFF020B12),
                Color(0xFF03141F),
                Color(0xFF001D32)
            )
        } else {
            listOf(
                Color(0xFFF3F7FA),
                Color(0xFFEDF5FA),
                Color(0xFFE2F0F9)
            )
        }
    )

    val progreso = remember {
        Animatable(0f)
    }

    LaunchedEffect(Unit) {
        progreso.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 3000,
                easing = LinearEasing
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(
                    id = R.drawable.acameet_logo
                ),
                contentDescription = "Logo de AcaMeet",
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            color = colores.onBackground
                        )
                    ) {
                        append("Aca")
                    }

                    withStyle(
                        SpanStyle(
                            color = azul
                        )
                    ) {
                        append("Meet")
                    }
                },
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Gestión de eventos académicos",
                color = colores.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(40.dp))

            LinearProgressIndicator(
                progress = { progreso.value },
                modifier = Modifier
                    .width(155.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = azul,
                trackColor = if (esOscuro) {
                    Color(0xFF19384B)
                } else {
                    Color(0xFFDCE5EB)
                }
            )
        }
    }
}
