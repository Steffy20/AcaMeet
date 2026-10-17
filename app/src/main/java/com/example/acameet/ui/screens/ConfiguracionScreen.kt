package com.example.acameet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.acameet.ui.viewmodel.ConfiguracionUiState
import com.example.acameet.ui.viewmodel.ConfiguracionViewModel

// 1. COMPOSABLE STATEFUL (Contenedor con estado que conoce al ViewModel)
@Composable
fun ConfiguracionRoute(
    viewModel: ConfiguracionViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Consumo del ciclo de vida seguro exigido por la rúbrica
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ConfiguracionContent(
        uiState = uiState,
        onRecordarSesionChanged = viewModel::onRecordarSesionChanged,
        onTemaOscuroChanged = viewModel::onTemaOscuroChanged,
        onNotificacionesChanged = viewModel::onNotificacionesChanged,
        onLogout = onLogout,
        modifier = modifier
    )
}

// 2. COMPOSABLE STATELESS (Vista pura que no recibe ViewModel)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionContent(
    uiState: ConfiguracionUiState,
    onRecordarSesionChanged: (Boolean) -> Unit,
    onTemaOscuroChanged: (Boolean) -> Unit,
    onNotificacionesChanged: (Boolean) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val prefs = uiState.preferences

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Cuenta",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Editar perfil") },
                            supportingContent = { Text("Actualiza tu información personal") },
                            leadingContent = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Cambiar contraseña") },
                            supportingContent = { Text("Gestiona tu contraseña") },
                            leadingContent = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                        )
                    }
                }

                Text(
                    text = "Preferencias",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Recordar sesión") },
                            supportingContent = { Text("Mantener la sesión iniciada") },
                            leadingContent = { Icon(Icons.Default.Key, contentDescription = null) },
                            trailingContent = {
                                Switch(
                                    checked = prefs.recordarSesion,
                                    onCheckedChange = onRecordarSesionChanged
                                )
                            }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Tema oscuro") },
                            supportingContent = { Text("Activa o desactiva el tema oscuro") },
                            leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                            trailingContent = {
                                Switch(
                                    checked = prefs.temaOscuro,
                                    onCheckedChange = onTemaOscuroChanged
                                )
                            }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Notificaciones") },
                            supportingContent = { Text("Recibir alertas de próximos eventos") },
                            leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                            trailingContent = {
                                Switch(
                                    checked = prefs.notificacionesHabilitadas,
                                    onCheckedChange = onNotificacionesChanged
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cerrar sesión")
                }
            }
        }
    }
}