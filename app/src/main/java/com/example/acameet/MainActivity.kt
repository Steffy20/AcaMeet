
package com.example.acameet

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

import androidx.core.content.ContextCompat

import com.example.acameet.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    // =====================================================
    // EVENTO RECIBIDO DESDE UNA NOTIFICACIÓN
    // =====================================================

    private var eventoPendiente by mutableStateOf<Pair<Int, Int>?>(null)

    // =====================================================
    // SOLICITAR PERMISO DE NOTIFICACIONES
    // =====================================================

    private val solicitarPermisoNotificaciones =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            verificarPermisoAlarmasExactas()
        }

    // =====================================================
    // INICIAR APLICACIÓN
    // =====================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        // RECUPERAR TEMA NATIVO
        val preferencias = getSharedPreferences(
            "acameet_tema_nativo",
            MODE_PRIVATE
        )

        val modoOscuro = preferencias.getBoolean(
            "tema_oscuro",
            true
        )

        setTheme(
            if (modoOscuro) {
                R.style.Theme_AcaMeet_Dark
            } else {
                R.style.Theme_AcaMeet_Light
            }
        )

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // RECUPERAR EVENTO ENVIADO POR LA NOTIFICACIÓN
        procesarIntent(intent)

        // MOSTRAR APLICACIÓN
        setContent {

            AppNavigation(
                eventoDesdeNotificacion = eventoPendiente,
                onNotificacionProcesada = {
                    eventoPendiente = null
                }
            )
        }

        // VERIFICAR PERMISOS
        verificarPermisoNotificaciones()
    }

    // =====================================================
    // RECIBIR NOTIFICACIÓN CON LA APP ABIERTA
    // =====================================================

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)

        procesarIntent(intent)
    }

    // =====================================================
    // LEER IDENTIFICADORES DE LA NOTIFICACIÓN
    // =====================================================

    private fun procesarIntent(intent: Intent?) {

        if (intent == null) return

        val eventoId = intent.getIntExtra(
            "EVENTO_ID",
            0
        )

        val usuarioId = intent.getIntExtra(
            "USUARIO_ID",
            0
        )

        if (eventoId > 0 && usuarioId > 0) {

            eventoPendiente = Pair(
                eventoId,
                usuarioId
            )
        }

        // EVITAR PROCESAR EL MISMO EVENTO
        // SI LA ACTIVIDAD SE VUELVE A CREAR
        intent.removeExtra("EVENTO_ID")
        intent.removeExtra("USUARIO_ID")
    }

    // =====================================================
    // VERIFICAR PERMISO DE NOTIFICACIONES
    // =====================================================

    private fun verificarPermisoNotificaciones() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val permiso = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (
                permiso != PackageManager.PERMISSION_GRANTED
            ) {

                solicitarPermisoNotificaciones.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )

            } else {

                verificarPermisoAlarmasExactas()
            }

        } else {

            verificarPermisoAlarmasExactas()
        }
    }

    // =====================================================
    // VERIFICAR ACCESO A ALARMAS EXACTAS
    // =====================================================

    private fun verificarPermisoAlarmasExactas() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {

            val alarmManager = getSystemService(
                Context.ALARM_SERVICE
            ) as AlarmManager

            if (!alarmManager.canScheduleExactAlarms()) {

                try {

                    val intent = Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    ).apply {

                        data = Uri.parse(
                            "package:$packageName"
                        )
                    }

                    startActivity(intent)

                } catch (_: Exception) {

                    // ALGUNOS DISPOSITIVOS NO DISPONEN
                    // DE UNA PANTALLA PARA ESTE PERMISO
                }
            }
        }
    }
}

// =====================================================
// PANTALLA SPLASH DE ACAMEET
// =====================================================

@Composable
fun SplashScreen() {

    var progreso by remember {
        mutableFloatStateOf(0f)
    }

    val progresoAnimado by animateFloatAsState(
        targetValue = progreso,
        animationSpec = tween(
            durationMillis = 3000
        ),
        label = "progresoSplash"
    )

    LaunchedEffect(Unit) {
        progreso = 1f
    }

    val colores = MaterialTheme.colorScheme

    val modoOscuro = colores.background.red < 0.5f

    val background = Brush.verticalGradient(
        colors = if (modoOscuro) {
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

            // LOGO ACAMEET
            Image(
                painter = painterResource(
                    id = R.drawable.acameet_logo
                ),
                contentDescription = "Logo de AcaMeet",
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(
                modifier = Modifier.height((-45).dp)
            )

            // NOMBRE ACAMEET
            Text(
                text = buildAnnotatedString {

                    withStyle(
                        style = SpanStyle(
                            color = colores.onBackground,
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

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            // DESCRIPCIÓN
            Text(
                text = "Gestión de eventos académicos",
                color = colores.onSurfaceVariant,
                fontSize = 15.sp
            )

            Spacer(
                modifier = Modifier.height(55.dp)
            )

            // BARRA DE PROGRESO
            LinearProgressIndicator(
                progress = {
                    progresoAnimado
                },
                modifier = Modifier
                    .width(140.dp)
                    .height(5.dp),
                color = Color(0xFF009DFF),
                trackColor = if (modoOscuro) {
                    Color(0xFF263A49)
                } else {
                    Color(0xFFD0E2ED)
                }
            )
        }
    }
}
