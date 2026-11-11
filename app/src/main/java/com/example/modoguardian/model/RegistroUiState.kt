package com.example.modoguardian.model

data class RegistroUiState(
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val confirmarClave: String = "",

    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    val errorConfirmacion: String? = null,
    val errorGeneral: String? = null,

    val isLoading: Boolean = false,

    val registrado: Usuario? = null
)