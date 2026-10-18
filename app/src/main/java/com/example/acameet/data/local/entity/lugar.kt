
package com.example.acameet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lugares")
data class Lugar(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nombre: String,

    @ColumnInfo(defaultValue = "0")
    val usuarioId: Int = 0
)
