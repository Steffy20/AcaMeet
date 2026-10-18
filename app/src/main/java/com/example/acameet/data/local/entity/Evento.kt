
package com.example.acameet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class Evento(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,
    val descripcion: String,
    val fecha: String,
    val hora: String,

    val categoriaId: Int,
    val lugarId: Int,

    // URL DE LA IMAGEN DEL EVENTO
    val imagenUrl: String = ""
)
