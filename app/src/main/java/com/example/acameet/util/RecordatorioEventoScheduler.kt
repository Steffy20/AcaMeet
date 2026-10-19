
package com.example.acameet.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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

    fun programar(evento: Evento): Boolean {
        if (evento.id <= 0 || evento.usuarioId <= 0) {
            return false
        }

        val formato = SimpleDateFormat(
            "yyyy-MM-dd HH:mm",
            Locale.US
        ).apply {
            isLenient = false
        }

        val momento = try {
            formato.parse(
                "${evento.fecha} ${evento.hora}"
            )?.time ?: return false
        } catch (_: Exception) {
            return false
        }

        if (momento <= System.currentTimeMillis()) {
            cancelar(evento.id)
            return false
        }

        val pendingIntent = crearPendingIntent(evento)

        return try {
            alarmManager.cancel(pendingIntent)

            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                !alarmManager.canScheduleExactAlarms()
            ) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    momento,
                    pendingIntent
                )
                false
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    momento,
                    pendingIntent
                )
                true
            }
        } catch (_: SecurityException) {
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    momento,
                    pendingIntent
                )
            } catch (_: Exception) {
                return false
            }
            false
        }
    }

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
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun crearPendingIntent(
        evento: Evento
    ): PendingIntent {
        val intent = Intent(
            appContext,
            NotificacionReceiver::class.java
        ).apply {
            putExtra("EVENTO_ID", evento.id)
            putExtra("USUARIO_ID", evento.usuarioId)
            putExtra("TITULO_EVENTO", evento.nombre)
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
