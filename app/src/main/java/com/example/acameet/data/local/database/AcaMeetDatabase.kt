
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
import com.example.acameet.data.local.dao.NotificacionDao

import com.example.acameet.data.local.entity.Usuario
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.local.entity.Lugar
import com.example.acameet.data.local.entity.Evento
import com.example.acameet.data.local.entity.Notificacion

@Database(
    entities = [
        Usuario::class,
        Categoria::class,
        Lugar::class,
        Evento::class,
        Notificacion::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AcaMeetDatabase : RoomDatabase() {

    // =====================================================
    // DAO DE USUARIOS
    // =====================================================

    abstract fun usuarioDao(): UsuarioDao

    // =====================================================
    // DAO DE CATEGORÍAS
    // =====================================================

    abstract fun categoriaDao(): CategoriaDao

    // =====================================================
    // DAO DE LUGARES
    // =====================================================

    abstract fun lugarDao(): LugarDao

    // =====================================================
    // DAO DE EVENTOS
    // =====================================================

    abstract fun eventoDao(): EventoDao

    // =====================================================
    // DAO DE NOTIFICACIONES
    // =====================================================

    abstract fun notificacionDao(): NotificacionDao

    companion object {

        @Volatile
        private var INSTANCE: AcaMeetDatabase? = null

        // =================================================
        // MIGRACIÓN DE VERSIÓN 4 A 5
        // AGREGA IMAGEN A LOS EVENTOS
        // =================================================

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

        // =================================================
        // MIGRACIÓN DE VERSIÓN 5 A 6
        // AGREGA EL PROPIETARIO DE CADA REGISTRO
        // =================================================

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

        // =================================================
        // MIGRACIÓN DE VERSIÓN 6 A 7
        // AGREGA HISTORIAL DE NOTIFICACIONES
        // =================================================

        val MIGRATION_6_7 = object : Migration(6, 7) {

            override fun migrate(
                db: SupportSQLiteDatabase
            ) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notificaciones (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        usuarioId INTEGER NOT NULL,
                        eventoId INTEGER NOT NULL,
                        titulo TEXT NOT NULL,
                        descripcion TEXT NOT NULL,
                        fechaCreacion INTEGER NOT NULL,
                        leida INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_notificaciones_usuarioId
                    ON notificaciones (usuarioId)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    index_notificaciones_eventoId
                    ON notificaciones (eventoId)
                    """.trimIndent()
                )
            }
        }

        // =================================================
        // OBTENER INSTANCIA DE LA BASE DE DATOS
        // =================================================

        fun getDatabase(
            context: Context
        ): AcaMeetDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcaMeetDatabase::class.java,
                    "acameet_database"
                )
                    .addMigrations(
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7
                    )
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}
