package com.example.acameet.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.acameet.data.local.dao.CategoriaDao
import com.example.acameet.data.local.dao.UsuarioDao
import com.example.acameet.data.local.entity.Categoria
import com.example.acameet.data.local.entity.Usuario

@Database(
    entities = [
        Usuario::class,
        Categoria::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AcaMeetDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun categoriaDao(): CategoriaDao

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
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}