package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import com.example.modoguardian.model.LoginErrores
import com.example.modoguardian.model.LoginUiState
import com.example.modoguardian.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val repository = UsuarioRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _errores = MutableStateFlow(LoginErrores())
    val errores: StateFlow<LoginErrores> = _errores.asStateFlow()

    private val _usuarioActual = MutableStateFlow(
        null as com.example.modoguardian.model.Usuario?
    )
    val usuarioActual: StateFlow<com.example.modoguardian.model.Usuario?> =
        _usuarioActual.asStateFlow()

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

    fun validarFormulario(): Boolean {

        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        var errorEmail: String? = null
        var errorPassword: String? = null

        if (email.isBlank()) {
            errorEmail = "Ingresa tu correo electrónico"
        } else if (!email.contains("@")) {
            errorEmail = "Ingresa un correo válido"
        }

        if (password.isBlank()) {
            errorPassword = "Ingresa tu contraseña"
        }

        _errores.value = LoginErrores(
            email = errorEmail,
            password = errorPassword
        )

        return errorEmail == null && errorPassword == null
    }

    fun iniciarSesion(): Boolean {

        if (!validarFormulario()) {
            return false
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true
        )

        val usuario = repository.autenticar(
            _uiState.value.email.trim(),
            _uiState.value.password
        )

        _uiState.value = _uiState.value.copy(
            isLoading = false
        )

        return if (usuario != null) {

            _usuarioActual.value = usuario

            _errores.value = LoginErrores()

            true

        } else {

            _errores.value = _errores.value.copy(
                general = "Correo o contraseña incorrectos"
            )

            false
        }
    }
    fun cerrarSesion() {
        _usuarioActual.value = null
    }
}