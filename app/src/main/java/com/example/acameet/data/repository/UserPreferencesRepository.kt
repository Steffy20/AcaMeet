package com.example.acameet.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

data class UserPreferences(
    val recordarSesion: Boolean = false,
    val temaOscuro: Boolean = true,
    val notificacionesHabilitadas: Boolean = true,
    val idioma: String = "Español"
)

// Interfaz exigida por la rúbrica de evaluación
interface UserPreferencesRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun saveRecordarSesion(value: Boolean)
    suspend fun saveTemaOscuro(value: Boolean)
    suspend fun saveNotificaciones(value: Boolean)
    suspend fun saveIdioma(value: String)
}

// Implementación concreta con DataStore
class UserPreferencesRepositoryImpl(
    private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {
        val RECORDAR_SESION = booleanPreferencesKey("recordar_sesion")
        val TEMA_OSCURO = booleanPreferencesKey("tema_oscuro")
        val NOTIFICACIONES_HABILITADAS = booleanPreferencesKey("notificaciones_habilitadas")
        val IDIOMA = stringPreferencesKey("idioma_app")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            recordarSesion = preferences[PreferencesKeys.RECORDAR_SESION] ?: false,
            temaOscuro = preferences[PreferencesKeys.TEMA_OSCURO] ?: true,
            notificacionesHabilitadas = preferences[PreferencesKeys.NOTIFICACIONES_HABILITADAS] ?: true,
            idioma = preferences[PreferencesKeys.IDIOMA] ?: "Español"
        )
    }

    override suspend fun saveRecordarSesion(value: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.RECORDAR_SESION] = value }
    }

    override suspend fun saveTemaOscuro(value: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.TEMA_OSCURO] = value }
    }

    override suspend fun saveNotificaciones(value: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICACIONES_HABILITADAS] = value }
    }

    override suspend fun saveIdioma(value: String) {
        context.dataStore.edit { it[PreferencesKeys.IDIOMA] = value }
    }
}