package com.example.acameet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.acameet.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }

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
            .background(background)
            .padding(horizontal = 28.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo
            Image(
                painter = painterResource(
                    id = R.drawable.acameet_logo
                ),
                contentDescription = "Logo de AcaMeet",
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )


            Spacer(modifier = Modifier.height(-35.dp))

            // Nombre AcaMeet
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aca",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Meet",
                    color = Color(0xFF0798F2),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Gestión de eventos académicos",
                color = Color(0xFF9BA8B4),
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Iniciar sesión",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ingresa tus datos para continuar",
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF9BA8B4),
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Correo
            OutlinedTextField(
                value = correo,
                onValueChange = { correo = it },
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
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF0798F2),
                    unfocusedBorderColor = Color(0xFF345064),
                    focusedLabelColor = Color(0xFF0798F2),
                    unfocusedLabelColor = Color(0xFF9BA8B4),
                    focusedLeadingIconColor = Color(0xFF0798F2),
                    unfocusedLeadingIconColor = Color(0xFF9BA8B4),
                    cursorColor = Color(0xFF0798F2)
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Contraseña
            OutlinedTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
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
                            imageVector =
                                if (mostrarContrasena)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                            contentDescription = "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation =
                    if (mostrarContrasena)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF0798F2),
                    unfocusedBorderColor = Color(0xFF345064),
                    focusedLabelColor = Color(0xFF0798F2),
                    unfocusedLabelColor = Color(0xFF9BA8B4),
                    focusedLeadingIconColor = Color(0xFF0798F2),
                    unfocusedLeadingIconColor = Color(0xFF9BA8B4),
                    focusedTrailingIconColor = Color(0xFF0798F2),
                    unfocusedTrailingIconColor = Color(0xFF9BA8B4),
                    cursorColor = Color(0xFF0798F2)
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Botón iniciar sesión
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0798F2)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Iniciar sesión",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Crear cuenta
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    color = Color(0xFF9BA8B4),
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = onRegisterClick
                ) {
                    Text(
                        text = "Crear cuenta",
                        color = Color(0xFF0798F2),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}