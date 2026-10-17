package com.example.acameet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.acameet.ui.viewmodel.UsuarioViewModel

// 1. COMPOSABLE STATEFUL (Contenedor con estado)
@Composable
fun RegisterRoute(
    viewModel: UsuarioViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    RegisterContent(
        nombre = nombre,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
        mensajeError = mensajeError,
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
            viewModel.registrarUsuario(
                nombre = nombre,
                correo = email,
                contrasena = password,
                confirmarContrasena = confirmPassword
            ) { exito, mensaje ->
                if (exito) {
                    onRegisterSuccess()
                } else {
                    mensajeError = mensaje
                }
            }
        },
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier
    )
}

// 2. COMPOSABLE STATELESS (Vista pura sin ViewModel)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContent(
    nombre: String,
    email: String,
    password: String,
    confirmPassword: String,
    mensajeError: String?,
    onNombreChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Registro de Usuario") }) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre completo") },
                singleLine = true,
                isError = mensajeError != null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Correo electrónico") },
                singleLine = true,
                isError = mensajeError != null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = mensajeError != null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = mensajeError != null,
                modifier = Modifier.fillMaxWidth()
            )

            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrarse")
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión aquí")
            }
        }
    }
}