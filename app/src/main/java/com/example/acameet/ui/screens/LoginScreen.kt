
package com.example.acameet.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.acameet.R
import com.example.acameet.ui.viewmodel.UsuarioViewModel

// COLORES PRINCIPALES DE ACAMEET
private val AzulLogin = Color(0xFF0798F2)
private val VerdeLogin = Color(0xFF4CAF50)
private val RojoLogin = Color(0xFFFF6B6B)

// ============================================================
// LOGIN ROUTE - LÓGICA DE INICIO DE SESIÓN
// ============================================================

@Composable
fun LoginRoute(
    viewModel: UsuarioViewModel,
    onLoginSuccess: (Int) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    var iniciandoSesion by remember {
        mutableStateOf(false)
    }

    val correoValido =
        email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    val formularioValido =
        correoValido &&
                password.isNotEmpty() &&
                !iniciandoSesion

    LoginContent(
        email = email,
        password = password,
        mensajeError = mensajeError,
        correoValido = correoValido,
        formularioValido = formularioValido,
        iniciandoSesion = iniciandoSesion,
        onEmailChange = {
            email = it
            mensajeError = null
        },
        onPasswordChange = {
            password = it
            mensajeError = null
        },
        onLoginClick = {
            if (formularioValido) {
                iniciandoSesion = true

                viewModel.iniciarSesionConUsuario(
                    correo = email.trim(),
                    contrasena = password
                ) { usuario, mensaje ->

                    iniciandoSesion = false

                    if (usuario != null) {
                        onLoginSuccess(usuario.id)
                    } else {
                        mensajeError = mensaje
                    }
                }
            }
        },
        onNavigateToRegister = onNavigateToRegister,
        modifier = modifier
    )
}

// ============================================================
// LOGIN CONTENT - INTERFAZ CON TEMA CLARO Y OSCURO
// ============================================================

@Composable
fun LoginContent(
    email: String,
    password: String,
    mensajeError: String?,
    correoValido: Boolean,
    formularioValido: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    iniciandoSesion: Boolean = false
) {
    var mostrarContrasena by remember {
        mutableStateOf(false)
    }

    // DETECTAR EL TEMA ACTIVO
    val modoOscuro = !MaterialTheme.colorScheme.isLightLogin()

    val colorTextoPrincipal =
        MaterialTheme.colorScheme.onBackground

    val colorTextoSecundario =
        MaterialTheme.colorScheme.onSurfaceVariant

    // FONDO DINÁMICO SEGÚN EL TEMA
    val background = Brush.verticalGradient(
        colors = if (modoOscuro) {
            listOf(
                Color(0xFF020B12),
                Color(0xFF03141F),
                Color(0xFF001D32)
            )
        } else {
            listOf(
                Color(0xFFF8FBFD),
                Color(0xFFEDF5FA),
                Color(0xFFDCEEF8)
            )
        }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 80.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // LOGO DE ACAMEET
            Image(
                painter = painterResource(R.drawable.acameet_logo),
                contentDescription = "Logo de AcaMeet",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height((-35).dp))

            // NOMBRE DE LA APLICACIÓN
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aca",
                    color = colorTextoPrincipal,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Meet",
                    color = AzulLogin,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Gestión de eventos académicos",
                color = colorTextoSecundario,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(50.dp))

            // TÍTULO DE INICIO DE SESIÓN
            Text(
                text = "Iniciar sesión",
                modifier = Modifier.fillMaxWidth(),
                color = colorTextoPrincipal,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Ingresa tus datos para continuar",
                modifier = Modifier.fillMaxWidth(),
                color = colorTextoSecundario,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(30.dp))

            // CORREO ELECTRÓNICO
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Correo electrónico")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Correo"
                    )
                },
                singleLine = true,
                isError = email.isNotEmpty() && !correoValido,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                colors = coloresLoginTiempoReal(),
                shape = RoundedCornerShape(14.dp)
            )

            // VALIDACIÓN DEL CORREO
            if (email.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))

                MensajeLogin(
                    texto = if (correoValido) {
                        "Correo electrónico válido"
                    } else {
                        "Ingresa un correo electrónico válido"
                    },
                    valido = correoValido
                )
            }

            Spacer(Modifier.height(18.dp))

            // CONTRASEÑA
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Contraseña")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Contraseña"
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            mostrarContrasena = !mostrarContrasena
                        }
                    ) {
                        Icon(
                            imageVector = if (mostrarContrasena) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (mostrarContrasena) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            }
                        )
                    }
                },
                visualTransformation = if (mostrarContrasena) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                colors = coloresLoginTiempoReal(),
                shape = RoundedCornerShape(14.dp)
            )

            // MENSAJE DE ERROR
            if (mensajeError != null) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(30.dp))

            // BOTÓN INICIAR SESIÓN
            Button(
                onClick = onLoginClick,
                enabled = formularioValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulLogin,
                    contentColor = Color.White,
                    disabledContainerColor =
                        MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor =
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (iniciandoSesion) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Iniciar sesión",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            // CREAR CUENTA
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    color = colorTextoSecundario,
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = onNavigateToRegister
                ) {
                    Text(
                        text = "Crear cuenta",
                        color = AzulLogin,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================
// VALIDACIÓN DEL CORREO
// ============================================================

@Composable
private fun MensajeLogin(
    texto: String,
    valido: Boolean
) {
    Text(
        text = (if (valido) "✓ " else "✗ ") + texto,
        color = if (valido) {
            VerdeLogin
        } else {
            MaterialTheme.colorScheme.error
        },
        fontSize = 12.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

// ============================================================
// COLORES DE CAMPOS ADAPTADOS AL TEMA
// ============================================================

@Composable
private fun coloresLoginTiempoReal() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor =
            MaterialTheme.colorScheme.onBackground,

        unfocusedTextColor =
            MaterialTheme.colorScheme.onBackground,

        focusedBorderColor = AzulLogin,

        unfocusedBorderColor =
            MaterialTheme.colorScheme.outline,

        focusedLabelColor = AzulLogin,

        unfocusedLabelColor =
            MaterialTheme.colorScheme.onSurfaceVariant,

        focusedLeadingIconColor = AzulLogin,

        unfocusedLeadingIconColor =
            MaterialTheme.colorScheme.onSurfaceVariant,

        focusedTrailingIconColor = AzulLogin,

        unfocusedTrailingIconColor =
            MaterialTheme.colorScheme.onSurfaceVariant,

        cursorColor = AzulLogin,

        errorTextColor =
            MaterialTheme.colorScheme.onBackground,

        errorBorderColor =
            MaterialTheme.colorScheme.error,

        errorLabelColor =
            MaterialTheme.colorScheme.error,

        errorLeadingIconColor =
            MaterialTheme.colorScheme.error
    )

// ============================================================
// IDENTIFICAR SI EL ESQUEMA ACTUAL ES CLARO
// ============================================================

private fun ColorScheme.isLightLogin(): Boolean {
    val fondo = background

    // CALCULAR LA LUMINOSIDAD DEL FONDO DEL TEMA
    val luminosidad =
        (0.2126f * fondo.red) +
                (0.7152f * fondo.green) +
                (0.0722f * fondo.blue)

    return luminosidad > 0.5f
}
