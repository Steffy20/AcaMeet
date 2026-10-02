package com.example.acameet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.acameet.navigation.AppNavigation
import com.example.acameet.ui.theme.AcaMeetTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AcaMeetTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun SplashScreen() {

        var progreso by remember {
            mutableFloatStateOf(0f)
        }

        val progresoAnimado by animateFloatAsState(
            targetValue = progreso,
            animationSpec = tween(durationMillis = 3000),
            label = "progresoSplash"
        )

        LaunchedEffect(Unit) {
            progreso = 1f
        }

        val background = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF020B12),
                Color(0xFF03141F),
                Color(0xFF001D32)
            )
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            // Logo AcaMeet
            Image(
                painter = painterResource(
                    id = R.drawable.acameet_logo
                ),
                contentDescription = "Logo de AcaMeet",
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(-45.dp))

            // Nombre AcaMeet
            Text(
                text = buildAnnotatedString {

                    withStyle(
                        style = SpanStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Aca")
                    }

                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF00A8FF),
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append("Meet")
                    }
                },
                fontSize = 42.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Descripción
            Text(
                text = "Gestión de eventos académicos",
                color = Color(0xFFB8C5CE),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(55.dp))

            // Barra de carga
            LinearProgressIndicator(
                progress = { progresoAnimado },
                modifier = Modifier
                    .width(140.dp)
                    .height(5.dp),
                color = Color(0xFF009DFF),
                trackColor = Color(0xFF263A49)
            )
        }
    }
}