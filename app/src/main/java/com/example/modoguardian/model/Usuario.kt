package com.example.modoguardian.model

enum class Rol {
    ADMINISTRADOR,
    SUPERVISOR,
    OPERADOR
}

data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val clave: String,
    val rol: Rol
)