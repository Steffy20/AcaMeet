
package com.example.acameet.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.example.acameet.data.local.entity.Usuario
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.data.local.entity.Evento

import com.example.acameet.data.local.dao.UsuarioDao
import com.example.acameet.data.local.dao.CategoriaDao
import com.example.acameet.data.local.dao.LugarDao
import com.example.acameet.data.local.dao.EventoDao

// BASE DE DATOS PRINCIPAL DE ACAMEET
@Database(
    entities = [
        Usuario::class,
        Categoria::class,
        Lugar::class,
        Evento::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AcaMeetDatabase : RoomDatabase() {

    // DAO DE USUARIOS
    abstract fun usuarioDao(): UsuarioDao

    // DAO DE CATEGORÍAS
    abstract fun categoriaDao(): CategoriaDao

    // DAO DE LUGARES
    abstract fun lugarDao(): LugarDao

    // DAO DE EVENTOS
    abstract fun eventoDao(): EventoDao

    companion object {

        @Volatile
        private var INSTANCE: AcaMeetDatabase? = null

        // OBTENER INSTANCIA ÚNICA DE LA BASE DE DATOS
        fun getDatabase(
            context: Context
        ): AcaMeetDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcaMeetDatabase::class.java,
                    "acameet_database"
                )
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
