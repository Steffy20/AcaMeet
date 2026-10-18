
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

private val AzulLogin = Color(0xFF0798F2)
private val GrisLogin = Color(0xFF9BA8B4)
private val VerdeLogin = Color(0xFF4CAF50)
private val RojoLogin = Color(0xFFFF6B6B)

// STATEFUL
@Composable
fun LoginRoute(
    viewModel: UsuarioViewModel,
    onLoginSuccess: (Int) -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    val correoValido = email.isNotBlank() &&
            Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    val formularioValido = correoValido && password.isNotEmpty()

    LoginContent(
        email = email,
        password = password,
        mensajeError = mensajeError,
        correoValido = correoValido,
        formularioValido = formularioValido,

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
                viewModel.iniciarSesionConUsuario(
                    correo = email,
                    contrasena = password
                ) { usuario, mensaje ->

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

// STATELESS
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
    modifier: Modifier = Modifier
) {
    var mostrarContrasena by remember {
        mutableStateOf(false)
    }

    val background = Brush.verticalGradient(
        listOf(
            Color(0xFF020B12),
            Color(0xFF03141F),
            Color(0xFF001D32)
        )
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

            Image(
                painter = painterResource(R.drawable.acameet_logo),
                contentDescription = "Logo de AcaMeet",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height((-35).dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Aca",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Meet",
                    color = AzulLogin,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "Gestión de eventos académicos",
                color = GrisLogin,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(50.dp))

            Text(
                "Iniciar sesión",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Ingresa tus datos para continuar",
                modifier = Modifier.fillMaxWidth(),
                color = GrisLogin,
                fontSize = 15.sp
            )

            Spacer(Modifier.height(30.dp))

            // CORREO
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Correo electrónico")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Email,
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
                        Icons.Default.Lock,
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
                            contentDescription = "Mostrar contraseña"
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

            if (mensajeError != null) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = mensajeError ?: "",
                    color = RojoLogin,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = onLoginClick,
                enabled = formularioValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulLogin,
                    disabledContainerColor = Color(0xFF345064),
                    disabledContentColor = GrisLogin
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "Iniciar sesión",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(26.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿No tienes una cuenta?",
                    color = GrisLogin,
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = onNavigateToRegister
                ) {
                    Text(
                        "Crear cuenta",
                        color = AzulLogin,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun MensajeLogin(
    texto: String,
    valido: Boolean
) {
    Text(
        text = (if (valido) "✓ " else "✗ ") + texto,
        color = if (valido) VerdeLogin else RojoLogin,
        fontSize = 12.sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun coloresLoginTiempoReal() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = AzulLogin,
        unfocusedBorderColor = Color(0xFF345064),
        focusedLabelColor = AzulLogin,
        unfocusedLabelColor = GrisLogin,
        focusedLeadingIconColor = AzulLogin,
        unfocusedLeadingIconColor = GrisLogin,
        focusedTrailingIconColor = AzulLogin,
        unfocusedTrailingIconColor = GrisLogin,
        cursorColor = AzulLogin,
        errorTextColor = Color.White,
        errorBorderColor = RojoLogin,
        errorLabelColor = RojoLogin,
        errorLeadingIconColor = RojoLogin
    )
