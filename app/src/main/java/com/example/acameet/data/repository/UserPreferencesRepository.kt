
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

// =====================================================
// PREFERENCIAS DEL USUARIO
// =====================================================

data class UserPreferences(
    val recordarSesion: Boolean = false,
    val temaOscuro: Boolean = true,
    val notificacionesHabilitadas: Boolean = true,
    val idioma: String = "Español",
    val usuarioIdGuardado: Int? = null,
    val usuarioIdActivo: Int? = null
)

// =====================================================
// INTERFAZ DEL REPOSITORIO
// =====================================================

interface UserPreferencesRepository {

    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun saveRecordarSesion(value: Boolean)

    suspend fun saveTemaOscuro(value: Boolean)

    suspend fun saveNotificaciones(value: Boolean)

    suspend fun saveIdioma(value: String)

    suspend fun guardarUsuarioSesion(usuarioId: Int)

    suspend fun cerrarSesion()
}

// =====================================================
// IMPLEMENTACIÓN DEL REPOSITORIO
// =====================================================

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

        // ID utilizado para recuperar la sesión.
        val USUARIO_ID =
            intPreferencesKey("usuario_id_sesion")

        // ID de la cuenta que inició sesión.
        val USUARIO_ID_ACTIVO =
            intPreferencesKey("usuario_id_activo")
    }

    // =====================================================
    // COPIA DEL TEMA PARA EL INICIO NATIVO
    // =====================================================

    private val temaNativo = context.getSharedPreferences(
        "acameet_tema_nativo",
        Context.MODE_PRIVATE
    )

    // =====================================================
    // LEER PREFERENCIAS
    // =====================================================

    override val userPreferencesFlow: Flow<UserPreferences> =
        context.dataStore.data.map { preferences ->

            val temaOscuro =
                preferences[PreferencesKeys.TEMA_OSCURO] ?: true

            if (
                !temaNativo.contains("tema_oscuro") ||
                temaNativo.getBoolean(
                    "tema_oscuro",
                    true
                ) != temaOscuro
            ) {

                temaNativo.edit()
                    .putBoolean(
                        "tema_oscuro",
                        temaOscuro
                    )
                    .apply()
            }

            UserPreferences(

                recordarSesion =
                    preferences[PreferencesKeys.RECORDAR_SESION]
                        ?: false,

                temaOscuro = temaOscuro,

                notificacionesHabilitadas =
                    preferences[
                        PreferencesKeys.NOTIFICACIONES_HABILITADAS
                    ] ?: true,

                idioma =
                    preferences[PreferencesKeys.IDIOMA]
                        ?: "Español",

                usuarioIdGuardado =
                    preferences[PreferencesKeys.USUARIO_ID],

                usuarioIdActivo =
                    preferences[PreferencesKeys.USUARIO_ID_ACTIVO]
            )
        }

    // =====================================================
    // RECORDAR SESIÓN
    // =====================================================

    override suspend fun saveRecordarSesion(
        value: Boolean
    ) {

        context.dataStore.edit { preferences ->

            preferences[PreferencesKeys.RECORDAR_SESION] =
                value

            if (value) {

                val usuarioActivo =
                    preferences[PreferencesKeys.USUARIO_ID_ACTIVO]

                if (
                    usuarioActivo != null &&
                    usuarioActivo > 0
                ) {

                    preferences[PreferencesKeys.USUARIO_ID] =
                        usuarioActivo
                }

            } else {

                // No se elimina la sesión activa.
                // Solo se desactiva el inicio automático.
                preferences.remove(
                    PreferencesKeys.USUARIO_ID
                )
            }
        }
    }

    // =====================================================
    // TEMA CLARO Y OSCURO
    // =====================================================

    override suspend fun saveTemaOscuro(
        value: Boolean
    ) {

        context.dataStore.edit { preferences ->

            preferences[PreferencesKeys.TEMA_OSCURO] =
                value
        }

        temaNativo.edit()
            .putBoolean(
                "tema_oscuro",
                value
            )
            .apply()
    }

    // =====================================================
    // ACTIVAR O DESACTIVAR NOTIFICACIONES
    // =====================================================

    override suspend fun saveNotificaciones(
        value: Boolean
    ) {

        context.dataStore.edit { preferences ->

            preferences[
                PreferencesKeys.NOTIFICACIONES_HABILITADAS
            ] = value
        }
    }

    // =====================================================
    // IDIOMA
    // =====================================================

    override suspend fun saveIdioma(
        value: String
    ) {

        context.dataStore.edit { preferences ->

            preferences[PreferencesKeys.IDIOMA] =
                value
        }
    }

    // =====================================================
    // GUARDAR USUARIO QUE INICIÓ SESIÓN
    // =====================================================

    override suspend fun guardarUsuarioSesion(
        usuarioId: Int
    ) {

        require(usuarioId > 0) {
            "El ID del usuario debe ser válido"
        }

        context.dataStore.edit { preferences ->

            // Guardar siempre el usuario activo.
            preferences[PreferencesKeys.USUARIO_ID_ACTIVO] =
                usuarioId

            val recordar =
                preferences[PreferencesKeys.RECORDAR_SESION]
                    ?: false

            if (recordar) {

                preferences[PreferencesKeys.USUARIO_ID] =
                    usuarioId

            } else {

                preferences.remove(
                    PreferencesKeys.USUARIO_ID
                )
            }
        }
    }

    // =====================================================
    // CERRAR SESIÓN
    // =====================================================

    override suspend fun cerrarSesion() {

        context.dataStore.edit { preferences ->

            // Eliminar usuario activo.
            preferences.remove(
                PreferencesKeys.USUARIO_ID_ACTIVO
            )

            // Eliminar inicio automático.
            preferences.remove(
                PreferencesKeys.USUARIO_ID
            )
        }
    }
}
