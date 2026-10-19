
package com.example.acameet.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

import com.example.acameet.MainActivity
import com.example.acameet.data.local.database.AcaMeetDatabase
import com.example.acameet.data.local.entity.Notificacion
import com.example.acameet.data.repository.UserPreferencesRepositoryImpl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class NotificacionReceiver : BroadcastReceiver() {

    companion object {

        private const val CHANNEL_ID =
            "acameet_eventos_channel"

        private const val CHANNEL_NAME =
            "Recordatorios de Eventos"

        private const val TAG =
            "AcaMeetNotificacion"
    }

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val pendingResult = goAsync()

        val appContext = context.applicationContext

        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        ).launch {

            try {

                procesarNotificacion(
                    context = appContext,
                    intent = intent
                )

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "Error al procesar la notificación",
                    e
                )

            } finally {

                pendingResult.finish()
            }
        }
    }

    private suspend fun procesarNotificacion(
        context: Context,
        intent: Intent
    ) {

        // =====================================================
        // 1. OBTENER DATOS DEL EVENTO
        // =====================================================

        val eventoId = intent.getIntExtra(
            "EVENTO_ID",
            0
        )

        val usuarioIdEvento = intent.getIntExtra(
            "USUARIO_ID",
            0
        )

        val titulo = intent.getStringExtra(
            "TITULO_EVENTO"
        ) ?: "Recordatorio de evento"

        val descripcion = intent.getStringExtra(
            "DESCRIPCION_EVENTO"
        ) ?: "Tienes un evento agendado en AcaMeet."

        if (
            eventoId <= 0 ||
            usuarioIdEvento <= 0
        ) {

            Log.w(
                TAG,
                "Recordatorio sin evento o usuario válido"
            )

            return
        }

        // =====================================================
        // 2. OBTENER PREFERENCIAS DEL USUARIO
        // =====================================================

        val repository = UserPreferencesRepositoryImpl(
            context
        )

        val preferencias = withTimeoutOrNull(5000L) {

            repository.userPreferencesFlow.first()

        } ?: run {

            Log.w(
                TAG,
                "No se pudieron obtener las preferencias"
            )

            return
        }

        // =====================================================
        // 3. VERIFICAR NOTIFICACIONES HABILITADAS
        // =====================================================

        if (!preferencias.notificacionesHabilitadas) {

            Log.d(
                TAG,
                "Notificaciones desactivadas"
            )

            return
        }

        // =====================================================
        // 4. VERIFICAR USUARIO CON SESIÓN ACTIVA
        // =====================================================

        val usuarioIdActivo = preferencias.usuarioIdActivo

        if (
            usuarioIdActivo == null ||
            usuarioIdActivo <= 0
        ) {

            Log.d(
                TAG,
                "No existe una sesión activa. " +
                        "Se omite el recordatorio del evento $eventoId"
            )

            return
        }

        // =====================================================
        // 5. COMPROBAR QUE EL EVENTO PERTENECE
        //    AL USUARIO QUE INICIÓ SESIÓN
        // =====================================================

        if (usuarioIdActivo != usuarioIdEvento) {

            Log.w(
                TAG,
                "Recordatorio omitido: " +
                        "el evento $eventoId pertenece al usuario " +
                        "$usuarioIdEvento, pero la sesión activa " +
                        "corresponde al usuario $usuarioIdActivo"
            )

            return
        }

        Log.d(
            TAG,
            "Usuario validado correctamente: $usuarioIdActivo"
        )

        // =====================================================
        // 6. VERIFICAR PERMISO DE ANDROID
        // =====================================================

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val permiso = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (
                permiso !=
                PackageManager.PERMISSION_GRANTED
            ) {

                Log.w(
                    TAG,
                    "Permiso de notificaciones no concedido"
                )

                return
            }
        }

        // =====================================================
        // 7. GUARDAR NOTIFICACIÓN EN ROOM
        // =====================================================

        val database = AcaMeetDatabase.getDatabase(
            context
        )

        val notificacion = Notificacion(
            usuarioId = usuarioIdEvento,
            eventoId = eventoId,
            titulo = titulo,
            descripcion = descripcion,
            fechaCreacion = System.currentTimeMillis(),
            leida = false
        )

        try {

            database.notificacionDao()
                .insertarNotificacion(
                    notificacion
                )

            Log.d(
                TAG,
                "Notificación guardada en Room: evento $eventoId"
            )

        } catch (e: Exception) {

            // Si Room presenta un error, intentamos
            // mostrar igualmente la notificación.

            Log.e(
                TAG,
                "No se pudo guardar la notificación",
                e
            )
        }

        // =====================================================
        // 8. OBTENER ADMINISTRADOR DE NOTIFICACIONES
        // =====================================================

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        // =====================================================
        // 9. CREAR CANAL DE NOTIFICACIONES
        // =====================================================

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {

                description =
                    "Notificaciones para eventos agendados en AcaMeet"

                enableVibration(true)
            }

            notificationManager.createNotificationChannel(
                channel
            )
        }

        // =====================================================
        // 10. ABRIR ACAMEET AL TOCAR LA NOTIFICACIÓN
        // =====================================================

        val abrirAplicacion = Intent(
            context,
            MainActivity::class.java
        ).apply {

            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra(
                "EVENTO_ID",
                eventoId
            )

            putExtra(
                "USUARIO_ID",
                usuarioIdEvento
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            eventoId,
            abrirAplicacion,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // =====================================================
        // 11. CONSTRUIR NOTIFICACIÓN
        // =====================================================

        val notification = NotificationCompat.Builder(
            context,
            CHANNEL_ID
        )
            .setSmallIcon(
                android.R.drawable.ic_popup_reminder
            )
            .setContentTitle(
                "⏰ $titulo"
            )
            .setContentText(
                descripcion
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(descripcion)
            )
            .setPriority(
                NotificationCompat.PRIORITY_HIGH
            )
            .setCategory(
                NotificationCompat.CATEGORY_REMINDER
            )
            .setAutoCancel(true)
            .setContentIntent(
                pendingIntent
            )
            .setVisibility(
                NotificationCompat.VISIBILITY_PRIVATE
            )
            .build()

        // =====================================================
        // 12. MOSTRAR NOTIFICACIÓN EN EL TELÉFONO
        // =====================================================

        notificationManager.notify(
            eventoId,
            notification
        )

        Log.d(
            TAG,
            "Notificación mostrada: evento $eventoId " +
                    "para el usuario $usuarioIdActivo"
        )
    }
}
