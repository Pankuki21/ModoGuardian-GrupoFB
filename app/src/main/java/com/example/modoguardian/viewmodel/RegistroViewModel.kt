package com.example.modoguardian.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import com.example.modoguardian.model.RegistroUiState
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.Usuario
import com.example.modoguardian.repository.FirebaseAuthRepository
import com.example.modoguardian.repository.UsuarioRepository
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RegistroViewModel(application: Application) :
    AndroidViewModel(application) {

    private val firebaseRepository = FirebaseAuthRepository()
    private val usuarioRepository = UsuarioRepository(application)
    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    // Actualización de los campos del formulario
    fun onNombreChange(valor: String) {
        _uiState.value = _uiState.value.copy(
            nombre = valor,
            errorNombre = null,
            errorGeneral = null
        )
    }

    fun onCorreoChange(valor: String) {
        _uiState.value = _uiState.value.copy(
            correo = valor,
            errorCorreo = null,
            errorGeneral = null
        )
    }

    fun onClaveChange(valor: String) {
        _uiState.value = _uiState.value.copy(
            clave = valor,
            errorClave = null,
            errorGeneral = null
        )
    }

    fun onConfirmarClaveChange(valor: String) {
        _uiState.value = _uiState.value.copy(
            confirmarClave = valor,
            errorConfirmacion = null,
            errorGeneral = null
        )
    }

    // Registrar usuario mediante Firebase Authentication
    fun registrar() {
        val actual = _uiState.value
        if (actual.isLoading) return

        val nombre = actual.nombre.trim()
        val correo = actual.correo.trim()
        val clave = actual.clave

        // Validar los campos
        val errorNombre = when {
            nombre.isBlank() -> "Ingresa tu nombre"
            else -> null
        }

        val errorCorreo = when {
            correo.isBlank() -> "Ingresa tu correo electrónico"
            !Patterns.EMAIL_ADDRESS.matcher(correo).matches() ->
                "Ingresa un correo electrónico válido"
            else -> null
        }

        val errorClave = when {
            clave.isBlank() -> "Ingresa una contraseña"
            clave.length < 6 ->
                "La contraseña debe tener al menos 6 caracteres"
            else -> null
        }

        val errorConfirmacion = when {
            actual.confirmarClave.isBlank() ->
                "Confirma tu contraseña"
            clave != actual.confirmarClave ->
                "Las contraseñas no coinciden"
            else -> null
        }

        // Mostrar errores y detener el registro
        if (errorNombre != null ||
            errorCorreo != null ||
            errorClave != null ||
            errorConfirmacion != null
        ) {
            _uiState.value = actual.copy(
                nombre = nombre,
                correo = correo,
                errorNombre = errorNombre,
                errorCorreo = errorCorreo,
                errorClave = errorClave,
                errorConfirmacion = errorConfirmacion,
                errorGeneral = null
            )
            return
        }

        if (usuarioRepository.esCorreoDeRolPreconfigurado(correo)) {
            _uiState.value = actual.copy(
                nombre = nombre,
                correo = correo,
                errorGeneral = "Este correo está reservado para un perfil de acceso. Pide al administrador que habilite tu cuenta"
            )
            return
        }

        // Activar la carga
        _uiState.value = actual.copy(
            nombre = nombre,
            correo = correo,
            isLoading = true,
            errorNombre = null,
            errorCorreo = null,
            errorClave = null,
            errorConfirmacion = null,
            errorGeneral = null
        )

        // Crear la cuenta en Firebase
        firebaseRepository.registrar(correo, clave)
            .addOnCompleteListener { tarea ->
                if (tarea.isSuccessful) {
                    val firebaseUser =
                        tarea.result?.user
                            ?: firebaseRepository.obtenerUsuarioActual()
                    if (firebaseUser == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorGeneral =
                                "No se pudo recuperar la información de la cuenta"
                        )
                        return@addOnCompleteListener
                    }

                    // Crear el modelo local
                    val usuario = Usuario(
                        id = firebaseUser.uid.hashCode(),
                        nombre = nombre,
                        correo = firebaseUser.email ?: correo,
                        rol = Rol.OPERADOR
                    )

                    // Guardar el nombre en Firebase
                    val actualizacion =
                        firebaseRepository.actualizarNombre(nombre)
                    if (actualizacion == null) {
                        finalizarRegistro(usuario)
                    } else {
                        actualizacion.addOnCompleteListener {
                            finalizarRegistro(usuario)
                        }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorGeneral = obtenerMensajeError(tarea.exception)
                    )
                }
            }
    }

    // Finalizar el registro y notificar a la pantalla
    private fun finalizarRegistro(usuario: Usuario) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            registrado = usuario,
            errorGeneral = null
        )
    }

    // Traducir errores de Firebase
    private fun obtenerMensajeError(error: Exception?): String {
        if (error is FirebaseNetworkException) {
            return "No hay conexión a Internet. Comprueba tu conexión"
        }
        val codigo = (error as? FirebaseAuthException)?.errorCode
        return when (codigo) {
            "ERROR_EMAIL_ALREADY_IN_USE" ->
                "Este correo electrónico ya está registrado"
            "ERROR_INVALID_EMAIL" ->
                "El correo electrónico no es válido"
            "ERROR_WEAK_PASSWORD",
            "ERROR_PASSWORD_DOES_NOT_MEET_REQUIREMENTS" ->
                "La contraseña no cumple los requisitos de seguridad"
            "ERROR_OPERATION_NOT_ALLOWED" ->
                "El registro mediante correo electrónico no está habilitado"
            "ERROR_TOO_MANY_REQUESTS" ->
                "Demasiados intentos. Inténtalo más tarde"
            else ->
                "No se pudo crear la cuenta. Inténtalo nuevamente"
        }
    }

    fun limpiarRegistroExitoso() {
        _uiState.value = RegistroUiState()
    }
}
