package com.example.modoguardian.repository

import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.Usuario

class UsuarioRepository {

    private val usuarios = listOf(
        Usuario(
            usuario = "admin",
            nombre = "Administrador",
            rol = Rol.ADMINISTRADOR
        ),
        Usuario(
            usuario = "supervisor",
            nombre = "Supervisor",
            rol = Rol.SUPERVISOR
        ),
        Usuario(
            usuario = "operador",
            nombre = "Operador",
            rol = Rol.OPERADOR
        )
    )

    fun autenticar(usuario: String, clave: String): Usuario? {
        return when {
            usuario == "admin" && clave == "1234" ->
                usuarios.find { it.usuario == "admin" }

            usuario == "supervisor" && clave == "1234" ->
                usuarios.find { it.usuario == "supervisor" }

            usuario == "operador" && clave == "1234" ->
                usuarios.find { it.usuario == "operador" }

            else -> null
        }
    }
}