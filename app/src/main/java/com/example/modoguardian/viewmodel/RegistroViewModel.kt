package com.example.modoguardian.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.modoguardian.model.RegistroUiState
import com.example.modoguardian.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UsuarioRepository(application)
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onNombreChange(valor: String) = actualizar { copy(nombre = valor, errorNombre = null, errorGeneral = null) }
    fun onCorreoChange(valor: String) = actualizar { copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    fun onClaveChange(valor: String) = actualizar { copy(clave = valor, errorClave = null, errorGeneral = null) }
    fun onConfirmarClaveChange(valor: String) = actualizar { copy(confirmarClave = valor, errorConfirmacion = null, errorGeneral = null) }

    fun registrar(): Boolean {
        val actual = _uiState.value
        val errorNombre = if (actual.nombre.isBlank()) "Ingresa tu nombre" else null
        val errorCorreo = if (actual.correo.isBlank()) "Ingresa tu correo electrónico" else null
        val errorClave = if (actual.clave.length < 6) "La contraseña debe tener al menos 6 caracteres" else null
        val errorConfirmacion = when {
            actual.confirmarClave.isBlank() -> "Confirma tu contraseña"
            actual.clave != actual.confirmarClave -> "Las contraseñas no coinciden"
            else -> null
        }
        if (errorNombre != null || errorCorreo != null || errorClave != null || errorConfirmacion != null) {
            _uiState.value = actual.copy(
                errorNombre = errorNombre,
                errorCorreo = errorCorreo,
                errorClave = errorClave,
                errorConfirmacion = errorConfirmacion,
                errorGeneral = null
            )
            return false
        }

        when (val resultado = repository.registrar(actual.nombre, actual.correo, actual.clave)) {
            is UsuarioRepository.ResultadoRegistro.Exito -> {
                _uiState.value = actual.copy(registrado = resultado.usuario, errorGeneral = null)
                return true
            }
            UsuarioRepository.ResultadoRegistro.NombreInvalido -> setError("Ingresa tu nombre")
            UsuarioRepository.ResultadoRegistro.CorreoInvalido -> setError("Ingresa un correo electrónico válido")
            UsuarioRepository.ResultadoRegistro.ClaveCorta -> setError("La contraseña debe tener al menos 6 caracteres")
            UsuarioRepository.ResultadoRegistro.CorreoExistente -> setError("Ya existe una cuenta con ese correo")
        }
        return false
    }

    private fun setError(mensaje: String) {
        _uiState.value = _uiState.value.copy(errorGeneral = mensaje)
    }

    private inline fun actualizar(cambio: RegistroUiState.() -> RegistroUiState) {
        _uiState.value = _uiState.value.cambio()
    }
}
