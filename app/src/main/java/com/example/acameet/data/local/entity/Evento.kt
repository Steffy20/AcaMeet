
package com.example.acameet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ENTIDAD DE EVENTOS EN ROOM
@Entity(tableName = "eventos")
data class Evento(

    // IDENTIFICADOR ÚNICO DEL EVENTO
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // INFORMACIÓN DEL EVENTO
    val nombre: String,
    val descripcion: String,
    val fecha: String,
    val hora: String,

    // RELACIÓN CON CATEGORÍA Y LUGAR
    val categoriaId: Int,
    val lugarId: Int
)
