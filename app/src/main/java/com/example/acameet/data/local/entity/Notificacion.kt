
package com.example.acameet.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notificaciones",
    indices = [
        Index(value = ["usuarioId"]),
        Index(value = ["eventoId"])
    ]
)
data class Notificacion(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val usuarioId: Int,

    val eventoId: Int,

    val titulo: String,

    val descripcion: String,

    val fechaCreacion: Long = System.currentTimeMillis(),

    val leida: Boolean = false
)
