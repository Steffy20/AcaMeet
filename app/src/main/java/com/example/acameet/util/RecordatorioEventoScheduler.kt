
package com.example.acameet.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import com.example.acameet.data.local.entity.Evento

import java.text.SimpleDateFormat
import java.util.Locale

class RecordatorioEventoScheduler(
    context: Context
) {

    private val appContext = context.applicationContext

    private val alarmManager =
        appContext.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

    companion object {
        private const val TAG = "AcaMeetRecordatorios"
    }

    // =====================================================
    // PROGRAMAR RECORDATORIO DE EVENTO
    // =====================================================

    fun programar(evento: Evento): Boolean {

        // VALIDAR EVENTO Y USUARIO
        if (
            evento.id <= 0 ||
            evento.usuarioId <= 0
        ) {
            return false
        }

        // =================================================
        // CONVERTIR FECHA Y HORA
        // =================================================

        val formato = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.US
        ).apply {
            isLenient = false
        }

        val momento = try {

            formato.parse(
                "${evento.fecha} ${evento.hora}"
            )?.time

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Fecha u hora inválida para el evento ${evento.id}",
                e
            )

            null
        }

        // =================================================
        // EVITAR RECORDATORIOS INVÁLIDOS
        // =================================================

        if (
            momento == null ||
            momento <= System.currentTimeMillis()
        ) {

            cancelar(evento.id)

            Log.d(
                TAG,
                "No se programa el evento ${evento.id}: fecha inválida o pasada"
            )

            return false
        }

        // =================================================
        // CREAR PENDING INTENT
        // =================================================

        val pendingIntent = crearPendingIntent(evento)

        return try {

            // CANCELAR PROGRAMACIÓN ANTERIOR
            alarmManager.cancel(pendingIntent)

            // =============================================
            // ANDROID 12 O SUPERIOR
            // =============================================

            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {

                // PROGRAMACIÓN APROXIMADA
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    momento,
                    pendingIntent
                )

                Log.d(
                    TAG,
                    "Recordatorio aproximado programado: evento ${evento.id}"
                )

                true

            } else {

                // PROGRAMACIÓN EXACTA
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    momento,
                    pendingIntent
                )

                Log.d(
                    TAG,
                    "Recordatorio exacto programado: evento ${evento.id}"
                )

                true
            }

        } catch (e: SecurityException) {

            // =============================================
            // ALTERNATIVA SI NO HAY PERMISO EXACTO
            // =============================================

            Log.w(
                TAG,
                "No se permite alarma exacta. Se intenta una aproximada.",
                e
            )

            try {

                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    momento,
                    pendingIntent
                )

                Log.d(
                    TAG,
                    "Recordatorio aproximado programado: evento ${evento.id}"
                )

                true

            } catch (error: Exception) {

                Log.e(
                    TAG,
                    "No se pudo programar el evento ${evento.id}",
                    error
                )

                false
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "Error al programar el evento ${evento.id}",
                e
            )

            false
        }
    }

    // =====================================================
    // CANCELAR RECORDATORIO
    // =====================================================

    fun cancelar(eventoId: Int) {

        if (eventoId <= 0) return

        val intent = Intent(
            appContext,
            NotificacionReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            appContext,
            eventoId,
            intent,
            PendingIntent.FLAG_NO_CREATE or
                    PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {

            alarmManager.cancel(pendingIntent)

            pendingIntent.cancel()

            Log.d(
                TAG,
                "Recordatorio cancelado: evento $eventoId"
            )
        }
    }

    // =====================================================
    // CREAR PENDING INTENT DEL EVENTO
    // =====================================================

    private fun crearPendingIntent(
        evento: Evento
    ): PendingIntent {

        val intent = Intent(
            appContext,
            NotificacionReceiver::class.java
        ).apply {

            putExtra(
                "EVENTO_ID",
                evento.id
            )

            putExtra(
                "USUARIO_ID",
                evento.usuarioId
            )

            putExtra(
                "TITULO_EVENTO",
                evento.nombre
            )

            putExtra(
                "DESCRIPCION_EVENTO",
                evento.descripcion
            )
        }

        return PendingIntent.getBroadcast(
            appContext,
            evento.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )
    }
}
