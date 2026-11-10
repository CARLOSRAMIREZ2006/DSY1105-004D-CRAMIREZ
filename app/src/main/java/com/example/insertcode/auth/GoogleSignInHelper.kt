package com.example.insertcode.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

// Datos de la cuenta de Google con la que se inició sesión
data class CuentaGoogle(
    val nombre: String,
    val correo: String,
    val idToken: String
)

sealed class ResultadoGoogle {
    data class Exito(val cuenta: CuentaGoogle) : ResultadoGoogle()
    data object Cancelado : ResultadoGoogle()
    data class Error(val mensaje: String) : ResultadoGoogle()
}

object GoogleSignInHelper {

    private const val TAG = "GoogleSignIn"

    // 1) Selector de cuentas de Google (Credential Manager)
    // 2) Firebase Auth valida el token y crea la sesión
    suspend fun elegirCuenta(context: Context, webClientId: String): ResultadoGoogle {
        val activity = context.findActivity()
            ?: return ResultadoGoogle.Error("No se encontró la Activity para mostrar el selector")

        // setFilterByAuthorizedAccounts(false): muestra TODAS las cuentas, incluida "Agregar otra cuenta"
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return try {
            val respuesta = CredentialManager.create(activity).getCredential(activity, request)
            val credencial = respuesta.credential

            if (credencial !is CustomCredential ||
                credencial.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return ResultadoGoogle.Error("Tipo de credencial no soportado")
            }

            val google = GoogleIdTokenCredential.createFrom(credencial.data)

            // Intercambia el token de Google por una sesión de Firebase
            val firebaseCredential = GoogleAuthProvider.getCredential(google.idToken, null)
            val resultado = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await()
            val usuario = resultado.user

            ResultadoGoogle.Exito(
                CuentaGoogle(
                    nombre = usuario?.displayName ?: google.displayName ?: "",
                    correo = usuario?.email ?: google.id,
                    idToken = google.idToken
                )
            )
        } catch (e: GetCredentialCancellationException) {
            ResultadoGoogle.Cancelado
        } catch (e: NoCredentialException) {
            Log.e(TAG, "NoCredentialException: ${e.message}", e)
            ResultadoGoogle.Error(
                "No hay credenciales de Google. Detalle: ${e.type} - ${e.message}"
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "GoogleIdTokenParsingException: ${e.message}", e)
            ResultadoGoogle.Error("No se pudo leer la información de la cuenta de Google")
        } catch (e: Exception) {
            Log.e(TAG, "Error ${e::class.java.simpleName}: ${e.message}", e)
            ResultadoGoogle.Error("${e::class.java.simpleName}: ${e.message}")
        }
    }

    // Desenvuelve el Context de Compose hasta encontrar la Activity
    private fun Context.findActivity(): Activity? {
        var contexto: Context = this
        while (contexto is ContextWrapper) {
            if (contexto is Activity) return contexto
            contexto = contexto.baseContext
        }
        return null
    }
}
