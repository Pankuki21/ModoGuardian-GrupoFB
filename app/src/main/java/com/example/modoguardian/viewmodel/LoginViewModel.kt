package com.example.modoguardian.viewmodel

import android.app.Application
import android.app.Activity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.modoguardian.model.LoginErrores
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.Usuario
import com.example.modoguardian.model.UsuarioPrueba
import com.example.modoguardian.repository.FirebaseAuthRepository
import com.example.modoguardian.repository.UsuarioRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialOption
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import android.os.Build
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository = FirebaseAuthRepository()
    private val usuarioRepository = UsuarioRepository(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _errores = MutableStateFlow(LoginErrores())
    val errores: StateFlow<LoginErrores> = _errores.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    // Recuperar la sesión activa
    init {
        firebaseRepository.obtenerUsuarioActual()?.let { usuario ->
            if (usuarioRepository.estaHabilitado(usuario.email.orEmpty())) {
                _usuarioActual.value = convertirUsuario(usuario)
            } else {
                firebaseRepository.cerrarSesion()
                _errores.value = LoginErrores(general = "El administrador deshabilitó esta cuenta de prueba")
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)

        _errores.value = _errores.value.copy(
            email = null,
            general = null
        )
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)

        _errores.value = _errores.value.copy(
            password = null,
            general = null
        )
    }

    // Validar el formulario
    fun validarFormulario(): Boolean {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        val errorEmail = when {
            email.isBlank() -> "Ingresa tu correo electrónico"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Ingresa un correo electrónico válido"
            else -> null
        }

        val errorPassword = if (password.isBlank()) {
            "Ingresa tu contraseña"
        } else {
            null
        }

        _errores.value = LoginErrores(
            email = errorEmail,
            password = errorPassword
        )

        return errorEmail == null && errorPassword == null
    }

    // Iniciar sesión con correo y contraseña
    fun iniciarSesion() {

        if (_uiState.value.isLoading || !validarFormulario()) {
            return
        }

        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        _uiState.value = _uiState.value.copy(isLoading = true)

        firebaseRepository.iniciarSesion(email, password)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    val firebaseUser = tarea.result?.user
                    if (firebaseUser != null) {
                        completarInicioSesion(firebaseUser)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _errores.value = _errores.value.copy(
                            general = "No se pudo recuperar la información del usuario"
                        )
                    }
                } else {
                    val usuarioDePrueba = usuarioRepository.autenticar(email, password)
                    if (usuarioDePrueba != null && usuarioRepository.esCorreoDeRolPreconfigurado(email)) {
                        if (!usuarioRepository.estaHabilitado(email)) {
                            _uiState.value = LoginUiState()
                            _usuarioActual.value = null
                            _errores.value = LoginErrores(
                                general = "El administrador deshabilitó esta cuenta de prueba"
                            )
                        } else {
                            _usuarioActual.value = usuarioDePrueba.copy(
                                rol = usuarioRepository.obtenerRolPorCorreo(email)
                            )
                            _uiState.value = LoginUiState()
                            _errores.value = LoginErrores()
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _usuarioActual.value = null
                        _errores.value = _errores.value.copy(
                            general = obtenerMensajeError(tarea.exception)
                        )
                    }
                }
            }
    }

    /** Inicia sesión con Google */
    fun iniciarSesionConGoogle(activity: Activity) {
        if (_uiState.value.isLoading) return

        _uiState.value = _uiState.value.copy(isLoading = true)
        _errores.value = LoginErrores()

        viewModelScope.launch {
            try {
                // Leer el cliente OAuth de google-services.json
                val context = activity.applicationContext
                val clientIdResource = context.resources.getIdentifier(
                    "default_web_client_id",
                    "string",
                    context.packageName
                )
                if (clientIdResource == 0) {
                    throw GoogleSignInConfigurationException()
                }

                val clientId = context.getString(clientIdResource)
                val googleOption: CredentialOption = if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
                ) {
                    GetSignInWithGoogleOption.Builder(clientId).build()
                } else {
                    GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .setServerClientId(clientId)
                        .build()
                }
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleOption)
                    .build()
                val response = CredentialManager.create(context).getCredential(activity, request)
                val credential = response.credential as? CustomCredential
                    ?: throw IllegalStateException("Google no devolvió una credencial válida")
                if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    throw IllegalStateException("Google no devolvió un token de acceso compatible")
                }

                val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val authCredential = GoogleAuthProvider.getCredential(googleToken, null)
                val firebaseUser = firebaseRepository.iniciarSesionConGoogle(authCredential)
                    .await().user
                    ?: throw IllegalStateException("No se pudo recuperar la información del usuario")
                completarInicioSesion(firebaseUser)
            } catch (_: GetCredentialCancellationException) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (error: CancellationException) {
                throw error
            } catch (error: NoCredentialException) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _errores.value = _errores.value.copy(
                    general = "No se encontró una cuenta de Google disponible en este dispositivo"
                )
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _errores.value = _errores.value.copy(
                    general = if (error is GoogleSignInConfigurationException) {
                        "Google aún no está configurado. Activa el proveedor en Firebase y actualiza google-services.json"
                    } else {
                        "No se pudo iniciar sesión con Google. Comprueba la configuración de Firebase e inténtalo nuevamente"
                    }
                )
            }
        }
    }

    private fun completarInicioSesion(firebaseUser: FirebaseUser) {
        if (!usuarioRepository.estaHabilitado(firebaseUser.email.orEmpty())) {
            firebaseRepository.cerrarSesion()
            _usuarioActual.value = null
            _uiState.value = LoginUiState()
            _errores.value = LoginErrores(general = "El administrador deshabilitó esta cuenta de prueba")
            return
        }
        _usuarioActual.value = convertirUsuario(firebaseUser)
        _uiState.value = LoginUiState()
        _errores.value = LoginErrores()
    }

    fun obtenerUsuariosDePrueba(): List<UsuarioPrueba> = usuarioRepository.obtenerUsuariosDePrueba()

    fun actualizarRolDePrueba(correo: String, rol: Rol) {
        usuarioRepository.actualizarRolDePrueba(correo, rol)
    }

    fun actualizarAccesoDePrueba(correo: String, habilitado: Boolean) {
        usuarioRepository.actualizarAccesoDePrueba(correo, habilitado)
    }

    // Convertir el usuario al modelo local
    private fun convertirUsuario(firebaseUser: FirebaseUser): Usuario {

        val correo = firebaseUser.email.orEmpty()

        val nombre = firebaseUser.displayName
            ?.takeIf { it.isNotBlank() }
            ?: correo.substringBefore("@").ifBlank { "Usuario" }

        return Usuario(
            id = firebaseUser.uid.hashCode(),
            nombre = nombre,
            correo = correo,
            rol = usuarioRepository.obtenerRolPorCorreo(correo)
        )
    }

    // Traducir errores de Firebase
    private fun obtenerMensajeError(error: Exception?): String {

        if (error is FirebaseNetworkException) {
            return "No hay conexión a Internet. Comprueba tu conexión"
        }

        val codigo = (error as? FirebaseAuthException)?.errorCode

        return when (codigo) {
            "ERROR_INVALID_EMAIL" ->
                "El correo electrónico no es válido"

            "ERROR_INVALID_CREDENTIAL",
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND" ->
                "Correo o contraseña incorrectos"

            "ERROR_USER_DISABLED" ->
                "Esta cuenta se encuentra deshabilitada"

            "ERROR_OPERATION_NOT_ALLOWED" ->
                "Este método de inicio de sesión aún no está habilitado en Firebase"

            "ERROR_TOO_MANY_REQUESTS" ->
                "Demasiados intentos. Inténtalo más tarde"

            else ->
                "No se pudo iniciar sesión. Inténtalo nuevamente"
        }
    }

    fun cerrarSesion() {
        firebaseRepository.cerrarSesion()

        _usuarioActual.value = null
        _uiState.value = LoginUiState()
        _errores.value = LoginErrores()
    }

    // Actualizar la sesión tras el registro
    fun iniciarSesionConUsuario(usuario: Usuario) {
        _usuarioActual.value = usuario
        _uiState.value = LoginUiState()
        _errores.value = LoginErrores()
    }

    private class GoogleSignInConfigurationException : Exception()
}
