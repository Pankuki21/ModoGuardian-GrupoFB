package com.example.modoguardian.repository

import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.Usuario

class UsuarioRepository {

    private val usuariosRegistrados = listOf(
        Usuario(id = 1, nombre = "Administrador", correo = "admin@modoguardian.com", clave = "1234", rol = Rol.ADMINISTRADOR),
        Usuario(id = 2, nombre = "Supervisor Guardias", correo = "supervisor@modoguardian.com", clave = "1234", rol = Rol.SUPERVISOR),
        Usuario(id = 3, nombre = "Operador Cámaras", correo = "operador@modoguardian.com", clave = "1234", rol = Rol.OPERADOR)
    )

    fun autenticar(correo: String, clave: String): Usuario? {
        val correoLimpio = correo.trim()
        val claveLimpia = clave.trim()

        return usuariosRegistrados.find {
            it.correo.equals(correoLimpio, ignoreCase = true) && it.clave == claveLimpia
        }
    }
}