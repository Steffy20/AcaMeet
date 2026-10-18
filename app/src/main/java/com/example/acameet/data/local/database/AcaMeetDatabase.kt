
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

    abstract fun usuarioDao(): UsuarioDao

    abstract fun categoriaDao(): CategoriaDao

    abstract fun lugarDao(): LugarDao

    abstract fun eventoDao(): EventoDao

    companion object {

        @Volatile
        private var INSTANCE: AcaMeetDatabase? = null

        fun getDatabase(context: Context): AcaMeetDatabase {

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
