
package com.example.acameet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.acameet.data.local.entity.Notificacion

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val AzulNotificaciones = Color(0xFF0798F2)

// =====================================================
// CAMPANA DE NOTIFICACIONES
// =====================================================

@Composable
fun CampanaNotificaciones(
    notificaciones: List<Notificacion>,
    pendientes: Int,
    onMarcarTodas: () -> Unit,
    onMarcarLeida: (Int) -> Unit,
    onEliminar: (Int) -> Unit,
    onDetalleEventoClick: (Int) -> Unit,
    onEliminarTodas: () -> Unit = {}
) {

    var mostrarHistorial by remember {
        mutableStateOf(false)
    }

    var confirmarEliminarTodas by remember {
        mutableStateOf(false)
    }

    // =====================================================
    // BOTÓN DE CAMPANA CON CONTADOR
    // =====================================================

    BadgedBox(
        badge = {

            if (pendientes > 0) {

                Badge(
                    containerColor = Color(0xFFE53935),
                    contentColor = Color.White
                ) {

                    Text(
                        text = if (pendientes > 99) {
                            "99+"
                        } else {
                            pendientes.toString()
                        }
                    )
                }
            }
        }
    ) {

        IconButton(
            onClick = {
                mostrarHistorial = true
            }
        ) {

            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Abrir notificaciones",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(26.dp)
            )
        }
    }

    // =====================================================
    // HISTORIAL DE NOTIFICACIONES
    // =====================================================

    if (mostrarHistorial) {

        AlertDialog(
            onDismissRequest = {
                mostrarHistorial = false
            },

            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(20.dp),

            title = {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = AzulNotificaciones
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "Notificaciones",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = {
                            mostrarHistorial = false
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
            },

            text = {

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    // =========================================
                    // CONTADOR Y ACCIONES GENERALES
                    // =========================================

                    Text(
                        text = if (pendientes == 1) {
                            "1 notificación sin leer"
                        } else {
                            "$pendientes notificaciones sin leer"
                        },
                        color = AzulNotificaciones,
                        fontSize = 12.sp
                    )

                    if (notificaciones.isNotEmpty()) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            if (pendientes > 0) {

                                TextButton(
                                    onClick = onMarcarTodas
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )

                                    Spacer(Modifier.width(4.dp))

                                    Text(
                                        text = "Leer todas",
                                        fontSize = 11.sp
                                    )
                                }

                            } else {

                                Spacer(Modifier.width(1.dp))
                            }

                            TextButton(
                                onClick = {
                                    confirmarEliminarTodas = true
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(Modifier.width(4.dp))

                                Text(
                                    text = "Eliminar todas",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    Spacer(Modifier.height(12.dp))

                    // =========================================
                    // HISTORIAL VACÍO
                    // =========================================

                    if (notificaciones.isEmpty()) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 35.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                tint = AzulNotificaciones,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Sin notificaciones",
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "Los recordatorios de tus eventos aparecerán aquí.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    } else {

                        // =====================================
                        // LISTA DE NOTIFICACIONES
                        // =====================================

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            items(
                                items = notificaciones,
                                key = { it.id }
                            ) { notificacion ->

                                TarjetaNotificacion(
                                    notificacion = notificacion,

                                    onAbrirEvento = {

                                        if (!notificacion.leida) {
                                            onMarcarLeida(notificacion.id)
                                        }

                                        mostrarHistorial = false

                                        onDetalleEventoClick(
                                            notificacion.eventoId
                                        )
                                    },

                                    onMarcarLeida = {
                                        onMarcarLeida(notificacion.id)
                                    },

                                    onEliminar = {
                                        onEliminar(notificacion.id)
                                    }
                                )
                            }
                        }
                    }
                }
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        mostrarHistorial = false
                    }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }

    // =====================================================
    // CONFIRMACIÓN PARA ELIMINAR TODO EL HISTORIAL
    // =====================================================

    if (confirmarEliminarTodas) {

        AlertDialog(
            onDismissRequest = {
                confirmarEliminarTodas = false
            },

            title = {
                Text("Eliminar notificaciones")
            },

            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar todas tus notificaciones? Esta acción no se puede deshacer."
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {
                        onEliminarTodas()
                        confirmarEliminarTodas = false
                    }
                ) {
                    Text(
                        text = "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        confirmarEliminarTodas = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// =====================================================
// TARJETA INDIVIDUAL DE NOTIFICACIÓN
// =====================================================

@Composable
private fun TarjetaNotificacion(
    notificacion: Notificacion,
    onAbrirEvento: () -> Unit,
    onMarcarLeida: () -> Unit,
    onEliminar: () -> Unit
) {

    val colores = MaterialTheme.colorScheme

    // =====================================================
    // FORMATEAR FECHA Y HORA
    // =====================================================

    val fechaFormateada = remember(
        notificacion.fechaCreacion
    ) {

        SimpleDateFormat(
            "dd/MM/yyyy - HH:mm",
            Locale.getDefault()
        ).format(
            Date(notificacion.fechaCreacion)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(14.dp),

        colors = CardDefaults.cardColors(
            containerColor = if (notificacion.leida) {
                colores.surface
            } else {
                AzulNotificaciones.copy(alpha = 0.09f)
            }
        ),

        border = BorderStroke(
            width = 1.dp,
            color = AzulNotificaciones.copy(
                alpha = if (notificacion.leida) {
                    0.12f
                } else {
                    0.4f
                }
            )
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.Top
            ) {

                Surface(
                    shape = CircleShape,
                    color = AzulNotificaciones.copy(alpha = 0.13f)
                ) {

                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = AzulNotificaciones,
                        modifier = Modifier.padding(9.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = notificacion.titulo,

                        fontWeight = if (notificacion.leida) {
                            FontWeight.Medium
                        } else {
                            FontWeight.Bold
                        },

                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = notificacion.descripcion,
                        fontSize = 12.sp,
                        color = colores.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = fechaFormateada,
                        fontSize = 10.sp,
                        color = colores.onSurfaceVariant
                    )
                }

                // INDICADOR DE NOTIFICACIÓN NO LEÍDA
                if (!notificacion.leida) {

                    Surface(
                        color = AzulNotificaciones,
                        shape = CircleShape,
                        modifier = Modifier.size(8.dp)
                    ) {}
                }
            }

            Spacer(Modifier.height(8.dp))

            HorizontalDivider(
                color = colores.outlineVariant
            )

            // =========================================
            // ACCIONES DE CADA NOTIFICACIÓN
            // =========================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {

                if (!notificacion.leida) {

                    IconButton(
                        onClick = onMarcarLeida,
                        modifier = Modifier.size(36.dp)
                    ) {

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Marcar como leída",
                            tint = AzulNotificaciones,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.size(36.dp)
                ) {

                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar notificación",
                        tint = colores.error,
                        modifier = Modifier.size(19.dp)
                    )
                }

                TextButton(
                    onClick = onAbrirEvento
                ) {

                    Text(
                        text = "Ver evento",
                        fontSize = 12.sp,
                        color = AzulNotificaciones
                    )
                }
            }
        }
    }
}
