package com.example.modoguardian.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.modoguardian.model.LoginErrores
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.Usuario
import com.example.modoguardian.repository.FirebaseAuthRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRepository = FirebaseAuthRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _errores = MutableStateFlow(LoginErrores())
    val errores: StateFlow<LoginErrores> = _errores.asStateFlow()

    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()

    // Recuperar la sesión de Firebase al iniciar la aplicación.
    init {
        firebaseRepository.obtenerUsuarioActual()?.let { usuario ->
            _usuarioActual.value = convertirUsuario(usuario)
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

    // Validar los campos antes de consultar Firebase.
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

    // Iniciar sesión mediante Firebase Authentication.
    fun iniciarSesion() {

        if (_uiState.value.isLoading || !validarFormulario()) {
            return
        }

        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        _uiState.value = _uiState.value.copy(isLoading = true)

        firebaseRepository.iniciarSesion(email, password)
            .addOnCompleteListener { tarea ->

                _uiState.value = _uiState.value.copy(isLoading = false)

                if (tarea.isSuccessful) {

                    val firebaseUser = tarea.result?.user

                    if (firebaseUser != null) {
                        _usuarioActual.value = convertirUsuario(firebaseUser)
                        _errores.value = LoginErrores()
                    } else {
                        _errores.value = _errores.value.copy(
                            general = "No se pudo recuperar la información del usuario."
                        )
                    }

                } else {

                    _usuarioActual.value = null

                    _errores.value = _errores.value.copy(
                        general = obtenerMensajeError(tarea.exception)
                    )
                }
            }
    }

    // Convertir el usuario de Firebase al modelo de Modo Guardián.
    private fun convertirUsuario(firebaseUser: FirebaseUser): Usuario {

        val correo = firebaseUser.email.orEmpty()

        val nombre = firebaseUser.displayName
            ?.takeIf { it.isNotBlank() }
            ?: correo.substringBefore("@").ifBlank { "Usuario" }

        return Usuario(
            id = firebaseUser.uid.hashCode(),
            nombre = nombre,
            correo = correo,
            rol = Rol.OPERADOR
        )
    }

    // Traducir los errores de Firebase a mensajes comprensibles.
    private fun obtenerMensajeError(error: Exception?): String {

        if (error is FirebaseNetworkException) {
            return "No hay conexión a Internet. Comprueba tu conexión."
        }

        val codigo = (error as? FirebaseAuthException)?.errorCode

        return when (codigo) {
            "ERROR_INVALID_EMAIL" ->
                "El correo electrónico no es válido."

            "ERROR_INVALID_CREDENTIAL",
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND" ->
                "Correo o contraseña incorrectos."

            "ERROR_USER_DISABLED" ->
                "Esta cuenta se encuentra deshabilitada."

            "ERROR_TOO_MANY_REQUESTS" ->
                "Demasiados intentos. Inténtalo más tarde."

            else ->
                "No se pudo iniciar sesión. Inténtalo nuevamente."
        }
    }

    fun cerrarSesion() {
        firebaseRepository.cerrarSesion()

        _usuarioActual.value = null
        _uiState.value = LoginUiState()
        _errores.value = LoginErrores()
    }

    // Se conserva temporalmente para la antigua pantalla
    // de registro local. La migraremos en la siguiente etapa.
    fun iniciarSesionConUsuario(usuario: Usuario) {
        _usuarioActual.value = usuario
        _uiState.value = LoginUiState()
        _errores.value = LoginErrores()
    }
}
