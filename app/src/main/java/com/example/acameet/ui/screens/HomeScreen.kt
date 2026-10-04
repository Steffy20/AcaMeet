package com.example.acameet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.acameet.ui.components.BottomNavigationBar

@Composable
fun HomeScreen(
    onCategoriasClick: () -> Unit = {}
) {

    val background = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF020B12),
            Color(0xFF03141F),
            Color(0xFF001D32)
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            BottomNavigationBar(
                pantallaActual = "inicio",
                onCategoriasClick = onCategoriasClick
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFF0798F2),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear evento"
                )
            }
        }

    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(paddingValues)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
            ) {

                Spacer(modifier = Modifier.height(30.dp))

                // Encabezado
                Text(
                    text = "AcaMeet",
                    color = Color(0xFF0798F2),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Eventos académicos",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Consulta y administra tus eventos",
                    color = Color(0xFF9BA8B4),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Tarjeta inicial
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0A1C28)
                    )
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            modifier = Modifier.size(55.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF102E42)
                        ) {

                            Box(
                                contentAlignment = Alignment.Center
                            ) {

                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Eventos",
                                    tint = Color(0xFF0798F2),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {

                            Text(
                                text = "Mis eventos",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            Text(
                                text = "Aún no tienes eventos registrados",
                                color = Color(0xFF9BA8B4),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "Próximos eventos",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFF50616D),
                        modifier = Modifier.size(55.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "No hay eventos registrados",
                        color = Color(0xFF9BA8B4),
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Presiona + para crear tu primer evento",
                        color = Color(0xFF647580),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}