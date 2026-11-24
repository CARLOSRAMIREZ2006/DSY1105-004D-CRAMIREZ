package com.example.insertcode.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Reutiliza la extensión Context.dataStore declarada en EstadoDataStore.kt
// (un solo DataStore por nombre: crear dos con el mismo nombre provoca un crash)
class PreferencesManager(private val context: Context) {

    // Credenciales de usuarios registrados: se guarda solo el hash SHA-256 de la clave
    private val credencialesPrefs = context.getSharedPreferences("credenciales_locales", Context.MODE_PRIVATE)

    private fun hashClave(clave: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(clave.toByteArray())
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    fun guardarCredencial(correo: String, clave: String) {
        credencialesPrefs.edit().putString(correo.trim().lowercase(), hashClave(clave)).apply()
    }

    fun verificarCredencial(correo: String, clave: String): Boolean {
        val guardado = credencialesPrefs.getString(correo.trim().lowercase(), null) ?: return false
        return guardado == hashClave(clave)
    }

    private val MODO_ESPECIAL = booleanPreferencesKey("modo_especial")

    // Flow reactivo con el modo especial (false por defecto)
    val modoEspecialFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[MODO_ESPECIAL] ?: false
    }

    suspend fun guardarModoEspecial(valor: Boolean) {
        context.dataStore.edit { prefs -> prefs[MODO_ESPECIAL] = valor }
    }
}
