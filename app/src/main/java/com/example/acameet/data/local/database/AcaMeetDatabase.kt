
package com.example.acameet.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
    version = 5,
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

        // MIGRACIÓN DE LA VERSIÓN 4 A LA 5
        // AGREGA LA COLUMNA PARA LAS IMÁGENES
        // SIN ELIMINAR LOS DATOS EXISTENTES
        private val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(
                db: SupportSQLiteDatabase
            ) {
                db.execSQL(
                    "ALTER TABLE eventos " +
                            "ADD COLUMN imagenUrl TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        // OBTENER INSTANCIA DE LA BASE DE DATOS
        fun getDatabase(
            context: Context
        ): AcaMeetDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcaMeetDatabase::class.java,
                    "acameet_database"
                )
                    .addMigrations(MIGRATION_4_5)
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
