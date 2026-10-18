
package com.example.acameet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ENTIDAD DE USUARIOS EN ROOM
@Entity(tableName = "usuarios")
data class Usuario(

    // IDENTIFICADOR ÚNICO AUTOGENERADO
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // NOMBRE COMPLETO DEL USUARIO
    val nombre: String,

    // CORREO ELECTRÓNICO
    val correo: String,

    // CONTRASEÑA DEL USUARIO
    val contrasena: String
)
