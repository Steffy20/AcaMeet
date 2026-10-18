
package com.example.acameet.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.acameet.ui.viewmodel.ConfiguracionUiState
import com.example.acameet.ui.viewmodel.ConfiguracionViewModel


import com.example.acameet.ui.components.BottomNavigationBar


private val Azul = Color(0xFF0798F2)
private val FondoOscuro = Color(0xFF020B12)
private val Tarjeta = Color(0xFF0A1C28)
private val Gris = Color(0xFF9BA8B4)
private val Blanco = Color.White
private val Rojo = Color(0xFFF87171)
private val Borde = Color(0xFF19384B)

private val FondoDegradado = Brush.verticalGradient(
    listOf(
        Color(0xFF020B12),
        Color(0xFF03141F),
        Color(0xFF001D32)
    )
)

@Composable
fun ConfiguracionRoute(
    viewModel: ConfiguracionViewModel,
    usuarioId: Int?,
    onLogout: () -> Unit,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var mostrarPerfil by remember { mutableStateOf(false) }
    var mostrarContrasena by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }

    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }

    var mensaje by remember { mutableStateOf<String?>(null) }
    var procesando by remember { mutableStateOf(false) }

    ConfiguracionContent(
        uiState = uiState,

        onRecordarSesionChanged = { enabled ->
            viewModel.onRecordarSesionChanged(
                enabled = enabled,
                usuarioId = usuarioId
            )
        },

        onTemaOscuroChanged = viewModel::onTemaOscuroChanged,
        onNotificacionesChanged = viewModel::onNotificacionesChanged,

        onEditarPerfil = {
            val id = usuarioId

            if (id != null) {
                mensaje = null
                procesando = true

                viewModel.cargarUsuario(id) { usuario ->
                    procesando = false

                    if (usuario != null) {
                        nombre = usuario.nombre
                        correo = usuario.correo
                        mostrarPerfil = true
                    } else {
                        mensaje = "No se pudo cargar el perfil"
                    }
                }
            }
        },

        onCambiarContrasena = {
            actual = ""
            nueva = ""
            confirmar = ""
            mensaje = null
            mostrarContrasena = true
        },

        onLogout = onLogout,
        onInicioClick = onInicioClick,
        onCategoriasClick = onCategoriasClick,
        onEventosClick = onEventosClick,
        modifier = modifier
    )

    if (procesando && !mostrarPerfil && !mostrarContrasena) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Tarjeta,
            title = {
                Text("Cargando", color = Blanco)
            },
            text = {
                CircularProgressIndicator(color = Azul)
            },
            confirmButton = {}
        )
    }

    if (mostrarPerfil) {
        AlertDialog(
            onDismissRequest = {
                if (!procesando) mostrarPerfil = false
            },
            containerColor = Tarjeta,
            titleContentColor = Blanco,
            textContentColor = Blanco,
            title = {
                Text("Editar perfil")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CampoConfiguracion(
                        valor = nombre,
                        alCambiar = {
                            nombre = it
                            mensaje = null
                        },
                        etiqueta = "Nombres y apellidos"
                    )

                    CampoConfiguracion(
                        valor = correo,
                        alCambiar = {
                            correo = it
                            mensaje = null
                        },
                        etiqueta = "Correo electrónico"
                    )

                    if (mensaje != null) {
                        Text(
                            mensaje ?: "",
                            color = Rojo,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !procesando,
                    onClick = {
                        val id = usuarioId ?: return@TextButton

                        procesando = true
                        mensaje = null

                        viewModel.actualizarPerfil(
                            usuarioId = id,
                            nombre = nombre,
                            correo = correo
                        ) { exito, respuesta ->
                            procesando = false

                            if (exito) {
                                mostrarPerfil = false
                                mensaje = null
                            } else {
                                mensaje = respuesta
                            }
                        }
                    }
                ) {
                    Text("Guardar", color = Azul)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !procesando,
                    onClick = {
                        mostrarPerfil = false
                        mensaje = null
                    }
                ) {
                    Text("Cancelar", color = Gris)
                }
            }
        )
    }

    if (mostrarContrasena) {
        AlertDialog(
            onDismissRequest = {
                if (!procesando) mostrarContrasena = false
            },
            containerColor = Tarjeta,
            titleContentColor = Blanco,
            textContentColor = Blanco,
            title = {
                Text("Cambiar contraseña")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CampoConfiguracion(
                        valor = actual,
                        alCambiar = {
                            actual = it
                            mensaje = null
                        },
                        etiqueta = "Contraseña actual",
                        esContrasena = true
                    )

                    CampoConfiguracion(
                        valor = nueva,
                        alCambiar = {
                            nueva = it
                            mensaje = null
                        },
                        etiqueta = "Nueva contraseña",
                        esContrasena = true
                    )

                    CampoConfiguracion(
                        valor = confirmar,
                        alCambiar = {
                            confirmar = it
                            mensaje = null
                        },
                        etiqueta = "Confirmar contraseña",
                        esContrasena = true
                    )

                    if (mensaje != null) {
                        Text(
                            mensaje ?: "",
                            color = Rojo,
                            fontSize = 13.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !procesando,
                    onClick = {
                        val id = usuarioId ?: return@TextButton

                        procesando = true
                        mensaje = null

                        viewModel.cambiarContrasena(
                            usuarioId = id,
                            actual = actual,
                            nueva = nueva,
                            confirmar = confirmar
                        ) { exito, respuesta ->
                            procesando = false

                            if (exito) {
                                mostrarContrasena = false
                                actual = ""
                                nueva = ""
                                confirmar = ""
                                mensaje = null
                            } else {
                                mensaje = respuesta
                            }
                        }
                    }
                ) {
                    Text("Guardar", color = Azul)
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !procesando,
                    onClick = {
                        mostrarContrasena = false
                        mensaje = null
                    }
                ) {
                    Text("Cancelar", color = Gris)
                }
            }
        )
    }
}

@Composable
fun ConfiguracionContent(
    uiState: ConfiguracionUiState,
    onRecordarSesionChanged: (Boolean) -> Unit,
    onTemaOscuroChanged: (Boolean) -> Unit,
    onNotificacionesChanged: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onInicioClick: () -> Unit = {},
    onCategoriasClick: () -> Unit = {},
    onEventosClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onEditarPerfil: () -> Unit = {},
    onCambiarContrasena: () -> Unit = {}
) {
    Scaffold(
        containerColor = FondoOscuro,
        bottomBar = {
            BottomNavigationBar(
                pantallaActual = "configuracion",
                onInicioClick = onInicioClick,
                onCategoriasClick = onCategoriasClick,
                onEventosClick = onEventosClick
            )
        }
    ) { innerPadding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(FondoDegradado)
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Azul
                )
            } else {
                val prefs = uiState.preferences

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Configuración",
                        color = Blanco,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Personaliza tu experiencia en AcaMeet",
                        color = Gris,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    TituloSeccion(
                        titulo = "Cuenta",
                        icono = Icons.Default.Person
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Tarjeta
                        ),
                        border = BorderStroke(1.dp, Borde)
                    ) {
                        Column {
                            OpcionConfiguracion(
                                titulo = "Editar perfil",
                                descripcion = "Actualiza tu información personal",
                                icono = Icons.Default.Person,
                                onClick = onEditarPerfil
                            )

                            HorizontalDivider(color = Borde)

                            OpcionConfiguracion(
                                titulo = "Cambiar contraseña",
                                descripcion = "Gestiona tu contraseña",
                                icono = Icons.Default.Lock,
                                onClick = onCambiarContrasena
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    TituloSeccion(
                        titulo = "Preferencias",
                        icono = Icons.Default.Settings
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Tarjeta
                        ),
                        border = BorderStroke(1.dp, Borde)
                    ) {
                        Column {
                            OpcionConSwitch(
                                titulo = "Recordar sesión",
                                descripcion = "Mantener la sesión iniciada",
                                icono = Icons.Default.Key,
                                activado = prefs.recordarSesion,
                                onCheckedChange = onRecordarSesionChanged
                            )

                            HorizontalDivider(color = Borde)

                            OpcionConSwitch(
                                titulo = "Tema oscuro",
                                descripcion = "Activa o desactiva el tema oscuro",
                                icono = Icons.Default.DarkMode,
                                activado = prefs.temaOscuro,
                                onCheckedChange = onTemaOscuroChanged
                            )

                            HorizontalDivider(color = Borde)

                            OpcionConSwitch(
                                titulo = "Notificaciones",
                                descripcion = "Recibir alertas de próximos eventos",
                                icono = Icons.Default.Notifications,
                                activado = prefs.notificacionesHabilitadas,
                                onCheckedChange = onNotificacionesChanged
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Rojo),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF26171C),
                            contentColor = Rojo
                        )
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            "Cerrar sesión",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun TituloSeccion(
    titulo: String,
    icono: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Azul,
            modifier = Modifier.size(22.dp)
        )

        Text(
            titulo,
            color = Blanco,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun OpcionConfiguracion(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Azul,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                titulo,
                color = Blanco,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                descripcion,
                color = Gris,
                fontSize = 12.sp
            )
        }

        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Gris
        )
    }
}

@Composable
private fun OpcionConSwitch(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    activado: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Azul,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                titulo,
                color = Blanco,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                descripcion,
                color = Gris,
                fontSize = 12.sp
            )
        }

        Switch(
            checked = activado,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Blanco,
                checkedTrackColor = Azul,
                uncheckedThumbColor = Gris,
                uncheckedTrackColor = Borde
            )
        )
    }
}

@Composable
private fun CampoConfiguracion(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    esContrasena: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (esContrasena) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Blanco,
            unfocusedTextColor = Blanco,
            focusedLabelColor = Azul,
            unfocusedLabelColor = Gris,
            focusedBorderColor = Azul,
            unfocusedBorderColor = Borde,
            cursorColor = Azul
        ),
        shape = RoundedCornerShape(12.dp)
    )
}
