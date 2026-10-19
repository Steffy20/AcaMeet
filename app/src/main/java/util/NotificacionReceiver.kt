
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

import kotlinx.coroutines.CancellationException
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

    // =====================================================
    // RECIBIR EL RECORDATORIO PROGRAMADO
    // =====================================================

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

            } catch (e: CancellationException) {

                throw e

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

    // =====================================================
    // PROCESAR NOTIFICACIÓN
    // =====================================================

    private suspend fun procesarNotificacion(
        context: Context,
        intent: Intent
    ) {

        // =================================================
        // 1. OBTENER IDENTIFICADORES
        // =================================================

        val eventoId = intent.getIntExtra(
            "EVENTO_ID",
            0
        )

        val usuarioIdEvento = intent.getIntExtra(
            "USUARIO_ID",
            0
        )

        if (eventoId <= 0 || usuarioIdEvento <= 0) {

            Log.w(
                TAG,
                "Recordatorio con identificadores inválidos"
            )

            return
        }

        // =================================================
        // 2. CONSULTAR PREFERENCIAS
        // =================================================

        val preferenciasRepository =
            UserPreferencesRepositoryImpl(context)

        val preferencias = withTimeoutOrNull(5000L) {

            preferenciasRepository.userPreferencesFlow.first()

        } ?: run {

            Log.w(
                TAG,
                "No se pudieron consultar las preferencias"
            )

            return
        }

        // =================================================
        // 3. VERIFICAR NOTIFICACIONES HABILITADAS
        // =================================================

        if (!preferencias.notificacionesHabilitadas) {

            Log.d(
                TAG,
                "El usuario desactivó las notificaciones"
            )

            return
        }

        // =================================================
        // 4. VERIFICAR SESIÓN ACTIVA
        // =================================================

        val usuarioIdActivo = preferencias.usuarioIdActivo

        if (
            usuarioIdActivo == null ||
            usuarioIdActivo <= 0 ||
            usuarioIdActivo != usuarioIdEvento
        ) {

            Log.w(
                TAG,
                "Recordatorio omitido por cambio o cierre de sesión"
            )

            return
        }

        // =================================================
        // 5. COMPROBAR QUE EL EVENTO EXISTE
        // =================================================

        val database = AcaMeetDatabase.getDatabase(context)

        val evento = database.eventoDao()
            .obtenerEventoPorId(
                id = eventoId,
                usuarioId = usuarioIdActivo
            )

        if (evento == null) {

            Log.w(
                TAG,
                "El evento $eventoId ya no existe o no pertenece al usuario"
            )

            return
        }

        // =================================================
        // 6. OBTENER DATOS ACTUALES DEL EVENTO
        // =================================================

        val titulo = evento.nombre

        val descripcion = evento.descripcion
            .ifBlank {
                "Tienes un evento agendado en AcaMeet."
            }

        // =================================================
        // 7. OBTENER ADMINISTRADOR DE NOTIFICACIONES
        // =================================================

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        // =================================================
        // 8. VERIFICAR PERMISOS
        // =================================================

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

        if (!notificationManager.areNotificationsEnabled()) {

            Log.w(
                TAG,
                "Las notificaciones están deshabilitadas en Android"
            )

            return
        }

        // =================================================
        // 9. CREAR CANAL DE NOTIFICACIONES
        // =================================================

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
                    "Recordatorios de eventos académicos de AcaMeet"

                enableVibration(true)
            }

            notificationManager.createNotificationChannel(
                channel
            )

            val canalActual =
                notificationManager.getNotificationChannel(
                    CHANNEL_ID
                )

            if (
                canalActual?.importance ==
                NotificationManager.IMPORTANCE_NONE
            ) {

                Log.w(
                    TAG,
                    "El canal de recordatorios está deshabilitado"
                )

                return
            }
        }

        // =================================================
        // 10. PREPARAR APERTURA DE LA APLICACIÓN
        // =================================================

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

        // =================================================
        // 11. CONSTRUIR NOTIFICACIÓN DE ANDROID
        // =================================================

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
            .setContentIntent(pendingIntent)
            .setVisibility(
                NotificationCompat.VISIBILITY_PRIVATE
            )
            .build()

        // =================================================
        // 12. GUARDAR EN EL HISTORIAL DE ROOM
        // =================================================

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
                .insertarNotificacion(notificacion)

            Log.d(
                TAG,
                "Recordatorio guardado en Room: $eventoId"
            )

        } catch (e: CancellationException) {

            throw e

        } catch (e: Exception) {

            Log.e(
                TAG,
                "No se pudo guardar el recordatorio en Room",
                e
            )
        }

        // =================================================
        // 13. MOSTRAR NOTIFICACIÓN EN ANDROID
        // =================================================

        notificationManager.notify(
            eventoId,
            notification
        )

        Log.d(
            TAG,
            "Recordatorio mostrado para evento $eventoId"
        )
    }
}
