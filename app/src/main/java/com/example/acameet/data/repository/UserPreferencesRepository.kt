
package com.example.acameet.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "user_settings"
)

data class UserPreferences(
    val recordarSesion: Boolean = false,
    val temaOscuro: Boolean = true,
    val notificacionesHabilitadas: Boolean = true,
    val idioma: String = "Español",
    val usuarioIdGuardado: Int? = null
)

interface UserPreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun saveRecordarSesion(value: Boolean)

    suspend fun saveTemaOscuro(value: Boolean)

    suspend fun saveNotificaciones(value: Boolean)

    suspend fun saveIdioma(value: String)

    suspend fun guardarUsuarioSesion(usuarioId: Int)

    suspend fun cerrarSesion()
}

class UserPreferencesRepositoryImpl(
    private val context: Context
) : UserPreferencesRepository {

    private object PreferencesKeys {

        val RECORDAR_SESION =
            booleanPreferencesKey("recordar_sesion")

        val TEMA_OSCURO =
            booleanPreferencesKey("tema_oscuro")

        val NOTIFICACIONES_HABILITADAS =
            booleanPreferencesKey("notificaciones_habilitadas")

        val IDIOMA =
            stringPreferencesKey("idioma_app")

        val USUARIO_ID =
            intPreferencesKey("usuario_id_sesion")
    }

    private val temaNativo = context.getSharedPreferences(
        "acameet_tema_nativo",
        Context.MODE_PRIVATE
    )

    override val userPreferencesFlow: Flow<UserPreferences> =
        context.dataStore.data.map { preferences ->

            val temaOscuro =
                preferences[PreferencesKeys.TEMA_OSCURO] ?: true

            // Mantener sincronizada la copia del tema.
            if (
                !temaNativo.contains("tema_oscuro") ||
                temaNativo.getBoolean(
                    "tema_oscuro",
                    true
                ) != temaOscuro
            ) {
                temaNativo.edit()
                    .putBoolean("tema_oscuro", temaOscuro)
                    .apply()
            }

            UserPreferences(
                recordarSesion =
                    preferences[PreferencesKeys.RECORDAR_SESION]
                        ?: false,

                temaOscuro = temaOscuro,

                notificacionesHabilitadas =
                    preferences[PreferencesKeys.NOTIFICACIONES_HABILITADAS]
                        ?: true,

                idioma =
                    preferences[PreferencesKeys.IDIOMA]
                        ?: "Español",

                usuarioIdGuardado =
                    preferences[PreferencesKeys.USUARIO_ID]
            )
        }

    override suspend fun saveRecordarSesion(value: Boolean) {

        context.dataStore.edit { preferences ->

            preferences[PreferencesKeys.RECORDAR_SESION] = value

            if (!value) {
                preferences.remove(PreferencesKeys.USUARIO_ID)
            }
        }
    }

    override suspend fun saveTemaOscuro(value: Boolean) {

        // Guardar en DataStore.
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TEMA_OSCURO] = value
        }

        // Guardar copia para el inicio nativo.
        temaNativo.edit()
            .putBoolean("tema_oscuro", value)
            .apply()
    }

    override suspend fun saveNotificaciones(value: Boolean) {

        context.dataStore.edit { preferences ->

            preferences[
                PreferencesKeys.NOTIFICACIONES_HABILITADAS
            ] = value
        }
    }

    override suspend fun saveIdioma(value: String) {

        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IDIOMA] = value
        }
    }

    override suspend fun guardarUsuarioSesion(usuarioId: Int) {

        context.dataStore.edit { preferences ->

            val recordar =
                preferences[PreferencesKeys.RECORDAR_SESION] ?: false

            if (recordar) {
                preferences[PreferencesKeys.USUARIO_ID] = usuarioId
            } else {
                preferences.remove(PreferencesKeys.USUARIO_ID)
            }
        }
    }

    override suspend fun cerrarSesion() {

        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USUARIO_ID)
        }
    }
}
