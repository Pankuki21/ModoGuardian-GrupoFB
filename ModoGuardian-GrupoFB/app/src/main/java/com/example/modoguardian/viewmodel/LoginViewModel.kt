package com.example.modoguardian.viewmodel

import androidx.lifecycle.ViewModel
import com.example.modoguardian.model.Usuario
import com.example.modoguardian.repository.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginViewModel : ViewModel() {

    private val repository = UsuarioRepository()

    private val _usuario = MutableStateFlow("")
    val usuario: StateFlow<String> = _usuario.asStateFlow()

    private val _clave = MutableStateFlow("")
    val clave: StateFlow<String> = _clave.asStateFlow()

    private val _usuarioAutenticado = MutableStateFlow<Usuario?>(null)
    val usuarioAutenticado: StateFlow<Usuario?> = _usuarioAutenticado.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun cambiarUsuario(valor: String) {
        _usuario.value = valor
        _error.value = null
    }

    fun cambiarClave(valor: String) {
        _clave.value = valor
        _error.value = null
    }

    fun iniciarSesion() {
        val resultado = repository.autenticar(
            usuario = _usuario.value,
            clave = _clave.value
        )

        if (resultado != null) {
            _usuarioAutenticado.value = resultado
            _error.value = null
        } else {
            _usuarioAutenticado.value = null
            _error.value = "Usuario o clave incorrectos"
        }
    }
}