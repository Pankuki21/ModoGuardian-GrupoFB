package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import com.example.modoguardian.model.Usuario
import com.example.modoguardian.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LoginViewModel : ViewModel() {
    private val repository = UsuarioRepository()

    private val _usuarioLogueado = MutableStateFlow<Usuario?>(null)
    val usuarioLogueado: StateFlow<Usuario?> = _usuarioLogueado

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun iniciarSesion(correo: String, clave: String) {
        val usuario = repository.autenticar(correo, clave)
        if (usuario != null) {
            _usuarioLogueado.value = usuario
            _error.value = null
        } else {
            _error.value = "Correo o clave incorrectos"
        }
    }

    fun cerrarSesion() {
        _usuarioLogueado.value = null
        _error.value = null
    }
}