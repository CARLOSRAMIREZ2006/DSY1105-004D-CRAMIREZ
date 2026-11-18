package com.example.insertcode.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

// Método clásico de Google Sign-In: abre el selector de cuentas directamente
object GoogleSignInHelper {

    private const val TAG = "GoogleSignIn"
    private const val CODIGO_CANCELADO = 12501

    // Cliente que lanza el selector de cuentas
    fun clienteGoogle(context: Context, webClientId: String): GoogleSignInClient {
        val opciones = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(webClientId)
            .build()
        return GoogleSignIn.getClient(context, opciones)
    }

    // Lee la respuesta del selector y crea la sesión en Firebase
    suspend fun procesarResultado(data: Intent?): ResultadoGoogle {
        return try {
            val cuentaGoogle = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)
            val idToken = cuentaGoogle.idToken
                ?: return ResultadoGoogle.Error("Google no entregó el token. Revisa el Web Client ID.")

            val credencial = GoogleAuthProvider.getCredential(idToken, null)
            val resultado = FirebaseAuth.getInstance().signInWithCredential(credencial).await()
            val usuario = resultado.user

            ResultadoGoogle.Exito(
                CuentaGoogle(
                    nombre = usuario?.displayName ?: cuentaGoogle.displayName ?: "",
                    correo = usuario?.email ?: cuentaGoogle.email ?: "",
                    idToken = idToken
                )
            )
        } catch (e: ApiException) {
            Log.e(TAG, "ApiException código ${e.statusCode}: ${e.message}", e)
            if (e.statusCode == CODIGO_CANCELADO) {
                ResultadoGoogle.Cancelado
            } else {
                ResultadoGoogle.Error(
                    "Google devolvió el código ${e.statusCode}. Revisa el SHA-1 en Firebase y el Web Client ID."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ${e::class.java.simpleName}: ${e.message}", e)
            ResultadoGoogle.Error("${e::class.java.simpleName}: ${e.message}")
        }
    }
}
