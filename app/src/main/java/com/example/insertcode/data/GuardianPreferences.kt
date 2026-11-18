package com.example.insertcode.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.insertcode.model.AjustesGuardian
import com.example.insertcode.model.Sesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Sesión y ajustes del guardián en DataStore
// (reutiliza la extensión Context.dataStore declarada en EstadoDataStore.kt)
class GuardianPreferences(private val context: Context) {

    private val NOMBRE = stringPreferencesKey("sesion_nombre")
    private val CORREO = stringPreferencesKey("sesion_correo")
    private val FOTO = stringPreferencesKey("sesion_foto")
    private val RECORDAR = booleanPreferencesKey("sesion_recordar")

    private val DOS_PASOS = booleanPreferencesKey("ajuste_dos_pasos")
    private val PUSH = booleanPreferencesKey("ajuste_push")
    private val ALERTAS_CRITICAS = booleanPreferencesKey("ajuste_alertas_criticas")
    private val MODO_OSCURO = booleanPreferencesKey("ajuste_modo_oscuro")

    val sesionFlow: Flow<Sesion> = context.dataStore.data.map { p ->
        Sesion(
            nombre = p[NOMBRE] ?: "Guardián",
            correo = p[CORREO] ?: "",
            fotoPath = p[FOTO]
        )
    }

    val ajustesFlow: Flow<AjustesGuardian> = context.dataStore.data.map { p ->
        AjustesGuardian(
            dosPasos = p[DOS_PASOS] ?: true,
            push = p[PUSH] ?: true,
            alertasCriticas = p[ALERTAS_CRITICAS] ?: true,
            modoOscuro = p[MODO_OSCURO]
        )
    }

    val modoOscuroFlow: Flow<Boolean?> = context.dataStore.data.map { p -> p[MODO_OSCURO] }

    // true si el usuario marcó "Recordarme" y hay una sesión guardada
    suspend fun sesionRecordada(): Boolean {
        val p = context.dataStore.data.first()
        return (p[RECORDAR] ?: false) && !p[CORREO].isNullOrBlank()
    }

    suspend fun guardarSesion(nombre: String, correo: String, recordar: Boolean) {
        context.dataStore.edit { p ->
            p[NOMBRE] = nombre
            p[CORREO] = correo
            p[RECORDAR] = recordar
        }
    }

    suspend fun limpiarSesion() {
        context.dataStore.edit { p ->
            p.remove(NOMBRE)
            p.remove(CORREO)
            p.remove(FOTO)
            p.remove(RECORDAR)
        }
    }

    suspend fun guardarFoto(path: String) {
        context.dataStore.edit { p -> p[FOTO] = path }
    }

    suspend fun setDosPasos(valor: Boolean) { context.dataStore.edit { p -> p[DOS_PASOS] = valor } }
    suspend fun setPush(valor: Boolean) { context.dataStore.edit { p -> p[PUSH] = valor } }
    suspend fun setAlertasCriticas(valor: Boolean) { context.dataStore.edit { p -> p[ALERTAS_CRITICAS] = valor } }
    suspend fun setModoOscuro(valor: Boolean) { context.dataStore.edit { p -> p[MODO_OSCURO] = valor } }
}
