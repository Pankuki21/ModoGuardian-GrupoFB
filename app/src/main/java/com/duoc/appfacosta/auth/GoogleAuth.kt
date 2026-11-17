
package com.duoc.appfacosta.auth

import android.app.Activity
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.duoc.appfacosta.data.model.UserSession
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

object GoogleAuth {

    private const val TAG = "GoogleAuth"

    /**
     * 1) Pide la cuenta de Google con Credential Manager.
     * 2) Con el idToken inicia sesión en Firebase usando tu FirebaseAuthRepository.
     * 3) Devuelve el UserSession que ya usa el resto de la app.
     */
    suspend fun signIn(
        activity: Activity,
        webClientId: String,
        repo: FirebaseAuthRepository = FirebaseAuthRepository()
    ): Result<UserSession> = try {
        if (webClientId.startsWith("TU_WEB_CLIENT_ID")) {
            error("Falta reemplazar google_web_client_id en strings.xml")
        }

        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val response = CredentialManager.create(activity).getCredential(activity, request)
        val credential = response.credential

        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCred = GoogleAuthProvider.getCredential(google.idToken, null)

            val user = repo.iniciarSesionConGoogle(firebaseCred).await().user
                ?: error("Firebase no devolvió usuario")

            val email = user.email ?: google.id
            Result.success(
                UserSession(
                    name = user.displayName ?: google.displayName ?: email,
                    email = email
                )
            )
        } else {
            Result.failure(IllegalStateException("Credencial no soportada"))
        }
    } catch (e: Exception) {
        Log.e(TAG, "${e.javaClass.simpleName}: ${e.message}", e)
        Result.failure(e)
    }
}
