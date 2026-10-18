package com.example.acameet.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.acameet.data.repository.UserPreferencesRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class NotificacionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Verificar si las notificaciones están activadas en DataStore
        val repository = UserPreferencesRepositoryImpl(context)
        val notificacionesHabilitadas = runBlocking {
            repository.userPreferencesFlow.first().notificacionesHabilitadas
        }

        if (!notificacionesHabilitadas) return

        // 2. Extraer datos del intent
        val titulo = intent.getStringExtra("TITULO_EVENTO") ?: "Próximo evento"
        val descripcion = intent.getStringExtra("DESCRIPCION_EVENTO") ?: "Tienes un evento agendado en AcaMeet"
        val eventoId = intent.getIntExtra("EVENTO_ID", 0)

        // 3. Obtener el servicio de notificaciones
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "acameet_eventos_channel"

        // 4. Crear el canal en Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Eventos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para eventos agendados en AcaMeet"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 5. Construir la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("⏰ $titulo")
            .setContentText(descripcion)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 6. Verificar permiso de notificación en Android 13+ antes de emitir
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val tienePermiso = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!tienePermiso) return
        }

        // 7. Lanzar la notificación
        notificationManager.notify(eventoId, notification)
    }
}