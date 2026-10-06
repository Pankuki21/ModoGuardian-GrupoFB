package com.example.modoguardian.model

data class Usuario(
    val usuario: String,
    val nombre: String,
    val rol: Rol
)

enum class Rol {
    ADMINISTRADOR,
    SUPERVISOR,
    OPERADOR
}