
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
import androidx.compose.material.icons.filled.Person
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

private val AzulRegistro = Color(0xFF0798F2)
private val GrisRegistro = Color(0xFF9BA8B4)
private val VerdeRegistro = Color(0xFF4CAF50)
private val RojoRegistro = Color(0xFFFF6B6B)

// STATEFUL ROUTE
@Composable
fun RegisterRoute(
    viewModel: UsuarioViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by rememberSaveable {
        mutableStateOf("")
    }

    var email by rememberSaveable {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var confirmPassword by remember {
        mutableStateOf("")
    }

    var mensajeError by remember {
        mutableStateOf<String?>(null)
    }

    var registrando by remember {
        mutableStateOf(false)
    }

    val nombreLimpio = nombre.trim().replace(
        Regex("\\s+"),
        " "
    )

    val nombreValido =
        nombreLimpio.split(" ").size >= 2 &&
                nombreLimpio.matches(
                    Regex("^[\\p{L}]+(?:[ '-][\\p{L}]+)*$")
                )

    val correoValido =
        email.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(
                    email.trim()
                ).matches()

    val largoValido = password.length >= 8
    val mayusculaValida = password.any { it.isUpperCase() }
    val minusculaValida = password.any { it.isLowerCase() }
    val numeroValido = password.any { it.isDigit() }

    val contrasenaValida =
        largoValido &&
                mayusculaValida &&
                minusculaValida &&
                numeroValido

    val confirmacionValida =
        confirmPassword.isNotEmpty() &&
                confirmPassword == password

    val formularioValido =
        nombreValido &&
                correoValido &&
                contrasenaValida &&
                confirmacionValida &&
                !registrando

    RegisterContent(
        nombre = nombre,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        mensajeError = mensajeError,
        nombreValido = nombreValido,
        correoValido = correoValido,
        largoValido = largoValido,
        mayusculaValida = mayusculaValida,
        minusculaValida = minusculaValida,
        numeroValido = numeroValido,
        confirmacionValida = confirmacionValida,
        formularioValido = formularioValido,

        onNombreChange = {
            nombre = it
            mensajeError = null
        },

        onEmailChange = {
            email = it
            mensajeError = null
        },

        onPasswordChange = {
            password = it
            mensajeError = null
        },

        onConfirmPasswordChange = {
            confirmPassword = it
            mensajeError = null
        },

        onRegisterClick = {
            if (formularioValido) {
                registrando = true

                viewModel.registrarUsuario(
                    nombre = nombreLimpio,
                    correo = email.trim(),
                    contrasena = password,
                    confirmarContrasena = confirmPassword
                ) { exito, mensaje ->
                    registrando = false

                    if (exito) {
                        onRegisterSuccess()
                    } else {
                        mensajeError = mensaje
                    }
                }
            }
        },

        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier
    )
}

// STATELESS CONTENT
@Composable
fun RegisterContent(
    nombre: String,
    email: String,
    password: String,
    confirmPassword: String,
    mensajeError: String?,
    nombreValido: Boolean,
    correoValido: Boolean,
    largoValido: Boolean,
    mayusculaValida: Boolean,
    minusculaValida: Boolean,
    numeroValido: Boolean,
    confirmacionValida: Boolean,
    formularioValido: Boolean,
    onNombreChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarContrasena by remember {
        mutableStateOf(false)
    }

    var mostrarConfirmacion by remember {
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
                .padding(top = 35.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.acameet_logo),
                contentDescription = "Logo de AcaMeet",
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height((-15).dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Aca",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Meet",
                    color = AzulRegistro,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(25.dp))

            Text(
                "Crear cuenta",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                "Regístrate para gestionar tus eventos académicos",
                modifier = Modifier.fillMaxWidth(),
                color = GrisRegistro,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(22.dp))

            // NOMBRE COMPLETO
            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Nombre completo")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Nombre"
                    )
                },
                singleLine = true,
                isError = nombre.isNotEmpty() && !nombreValido,
                colors = coloresRegistroTiempoReal(),
                shape = RoundedCornerShape(14.dp)
            )

            if (nombre.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))

                MensajeRegistro(
                    texto = if (nombreValido) {
                        "Nombre completo válido"
                    } else {
                        "Ingresa un nombre y un apellido, solo con letras"
                    },
                    valido = nombreValido
                )
            }

            Spacer(Modifier.height(12.dp))

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
                        Icons.Default.Email,
                        contentDescription = "Correo"
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                singleLine = true,
                isError = email.isNotEmpty() && !correoValido,
                colors = coloresRegistroTiempoReal(),
                shape = RoundedCornerShape(14.dp)
            )

            if (email.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))

                MensajeRegistro(
                    texto = if (correoValido) {
                        "Formato de correo válido"
                    } else {
                        "Ingresa un correo electrónico válido"
                    },
                    valido = correoValido
                )
            }

            Spacer(Modifier.height(12.dp))

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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                isError = password.isNotEmpty() &&
                        !(largoValido &&
                                mayusculaValida &&
                                minusculaValida &&
                                numeroValido),
                colors = coloresRegistroTiempoReal(),
                shape = RoundedCornerShape(14.dp)
            )

            if (password.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))

                MensajeRegistro(
                    texto = "Mínimo 8 caracteres",
                    valido = largoValido
                )

                MensajeRegistro(
                    texto = "Al menos una letra mayúscula",
                    valido = mayusculaValida
                )

                MensajeRegistro(
                    texto = "Al menos una letra minúscula",
                    valido = minusculaValida
                )

                MensajeRegistro(
                    texto = "Al menos un número",
                    valido = numeroValido
                )
            }

            Spacer(Modifier.height(12.dp))

            // CONFIRMAR CONTRASEÑA
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Confirmar contraseña")
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Confirmar contraseña"
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            mostrarConfirmacion = !mostrarConfirmacion
                        }
                    ) {
                        Icon(
                            imageVector = if (mostrarConfirmacion) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (mostrarConfirmacion) {
                                "Ocultar confirmación"
                            } else {
                                "Mostrar confirmación"
                            }
                        )
                    }
                },
                visualTransformation = if (mostrarConfirmacion) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                singleLine = true,
                isError = confirmPassword.isNotEmpty() &&
                        !confirmacionValida,
                colors = coloresRegistroTiempoReal(),
                shape = RoundedCornerShape(14.dp)
            )

            if (confirmPassword.isNotEmpty()) {
                Spacer(Modifier.height(5.dp))

                MensajeRegistro(
                    texto = if (confirmacionValida) {
                        "Las contraseñas coinciden"
                    } else {
                        "Las contraseñas no coinciden"
                    },
                    valido = confirmacionValida
                )
            }

            // MENSAJE DE ERROR
            if (mensajeError != null) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = mensajeError,
                    color = RojoRegistro,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(22.dp))

            // BOTÓN CREAR CUENTA
            Button(
                onClick = onRegisterClick,
                enabled = formularioValido,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AzulRegistro,
                    disabledContainerColor = Color(0xFF345064),
                    disabledContentColor = GrisRegistro
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "Crear cuenta",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // IR A INICIO DE SESIÓN
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "¿Ya tienes una cuenta?",
                    color = GrisRegistro,
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = onNavigateToLogin
                ) {
                    Text(
                        "Iniciar sesión",
                        color = AzulRegistro,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// MENSAJES DE VALIDACIÓN
@Composable
private fun MensajeRegistro(
    texto: String,
    valido: Boolean
) {
    Text(
        text = (if (valido) "✓ " else "✗ ") + texto,
        color = if (valido) {
            VerdeRegistro
        } else {
            RojoRegistro
        },
        fontSize = 12.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    )
}

// COLORES ORIGINALES DE LOS CAMPOS
@Composable
private fun coloresRegistroTiempoReal() =
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = AzulRegistro,
        unfocusedBorderColor = Color(0xFF345064),
        focusedLabelColor = AzulRegistro,
        unfocusedLabelColor = GrisRegistro,
        focusedLeadingIconColor = AzulRegistro,
        unfocusedLeadingIconColor = GrisRegistro,
        focusedTrailingIconColor = AzulRegistro,
        unfocusedTrailingIconColor = GrisRegistro,
        cursorColor = AzulRegistro,
        errorTextColor = Color.White,
        errorBorderColor = RojoRegistro,
        errorLabelColor = RojoRegistro,
        errorLeadingIconColor = RojoRegistro
    )
