
package com.example.acameet.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

import com.example.acameet.data.local.dao.UsuarioDao
import com.example.acameet.data.local.dao.CategoriaDao
import com.example.acameet.data.local.dao.LugarDao
import com.example.acameet.data.local.dao.EventoDao

import com.example.acameet.data.local.entity.Usuario
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.data.local.entity.Evento

@Database(
    entities = [
        Usuario::class,
        Categoria::class,
        Lugar::class,
        Evento::class
    ],
    version = 6,
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

        // MIGRACIÓN DE VERSIÓN 4 A 5
        // AGREGA IMAGEN A LOS EVENTOS
        val MIGRATION_4_5 = object : Migration(4, 5) {

            override fun migrate(
                db: SupportSQLiteDatabase
            ) {
                db.execSQL(
                    """
                    ALTER TABLE eventos
                    ADD COLUMN imagenUrl TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )
            }
        }

        // MIGRACIÓN DE VERSIÓN 5 A 6
        // AGREGA EL PROPIETARIO DE CADA REGISTRO
        val MIGRATION_5_6 = object : Migration(5, 6) {

            override fun migrate(
                db: SupportSQLiteDatabase
            ) {
                db.execSQL(
                    """
                    ALTER TABLE eventos
                    ADD COLUMN usuarioId INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    ALTER TABLE categorias
                    ADD COLUMN usuarioId INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    ALTER TABLE lugares
                    ADD COLUMN usuarioId INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }
        }

        // OBTENER INSTANCIA DE LA BASE DE DATOS
        fun getDatabase(context: Context): AcaMeetDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcaMeetDatabase::class.java,
                    "acameet_database"
                )
                    .addMigrations(
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
