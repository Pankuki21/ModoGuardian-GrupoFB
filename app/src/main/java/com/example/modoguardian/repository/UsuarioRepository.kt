package com.example.modoguardian.repository

import android.content.Context
import android.util.Base64
import com.example.modoguardian.model.Rol
import com.example.modoguardian.model.Usuario
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UsuarioRepository(context: Context) {

    private val preferencias = context.applicationContext
        .getSharedPreferences("usuarios_modo_guardian", Context.MODE_PRIVATE)
    private val aleatorio = SecureRandom()

    private val usuariosDePrueba = listOf(
        Usuario(
            id = 1,
            nombre = "Administrador",
            correo = "admin@guardian.test",
            rol = Rol.ADMINISTRADOR
        ),
        Usuario(
            id = 2,
            nombre = "Supervisor",
            correo = "supervisor@guardian.test",
            rol = Rol.SUPERVISOR
        ),
        Usuario(
            id = 3,
            nombre = "Operador",
            correo = "operador@guardian.test",
            rol = Rol.OPERADOR
        )
    )

    fun autenticar(correo: String, clave: String): Usuario? {
        val correoLimpio = correo.trim().lowercase()
        val usuarioPrueba = usuariosDePrueba.find {
            it.correo.equals(correoLimpio, ignoreCase = true)
        }
        if (usuarioPrueba != null) return usuarioPrueba.takeIf { clave == CLAVE_PRUEBA }

        val cuenta = cuentasLocales().firstOrNull {
            it.usuario.correo.equals(correoLimpio, ignoreCase = true)
        } ?: return null
        return if (verificarClave(clave, cuenta.salt, cuenta.hash)) cuenta.usuario else null
    }

    fun registrar(nombre: String, correo: String, clave: String): ResultadoRegistro {
        val nombreLimpio = nombre.trim()
        val correoLimpio = correo.trim().lowercase()
        if (nombreLimpio.isBlank()) return ResultadoRegistro.NombreInvalido
        if (!CORREO_VALIDO.matches(correoLimpio)) return ResultadoRegistro.CorreoInvalido
        if (clave.length < 6) return ResultadoRegistro.ClaveCorta
        if (usuariosDePrueba.any { it.correo.equals(correoLimpio, true) } ||
            cuentasLocales().any { it.usuario.correo.equals(correoLimpio, true) }
        ) return ResultadoRegistro.CorreoExistente

        val salt = ByteArray(16).also(aleatorio::nextBytes)
        val usuario = Usuario(
            id = (cuentasLocales().maxOfOrNull { it.usuario.id } ?: 3) + 1,
            nombre = nombreLimpio,
            correo = correoLimpio,
            rol = Rol.OPERADOR
        )
        val lista = JSONArray(preferencias.getString(CLAVE_CUENTAS, "[]"))
        lista.put(
            JSONObject()
                .put("id", usuario.id)
                .put("nombre", usuario.nombre)
                .put("correo", usuario.correo)
                .put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                .put("hash", Base64.encodeToString(hashClave(clave, salt), Base64.NO_WRAP))
        )
        preferencias.edit().putString(CLAVE_CUENTAS, lista.toString()).apply()
        return ResultadoRegistro.Exito(usuario)
    }

    private fun cuentasLocales(): List<CuentaLocal> {
        val lista = JSONArray(preferencias.getString(CLAVE_CUENTAS, "[]"))
        return (0 until lista.length()).mapNotNull { indice ->
            runCatching {
                val fila = lista.getJSONObject(indice)
                CuentaLocal(
                    usuario = Usuario(
                        id = fila.getInt("id"),
                        nombre = fila.getString("nombre"),
                        correo = fila.getString("correo"),
                        rol = Rol.OPERADOR
                    ),
                    salt = Base64.decode(fila.getString("salt"), Base64.NO_WRAP),
                    hash = Base64.decode(fila.getString("hash"), Base64.NO_WRAP)
                )
            }.getOrNull()
        }
    }

    private fun verificarClave(clave: String, salt: ByteArray, esperado: ByteArray): Boolean {
        return hashClave(clave, salt).contentEquals(esperado)
    }

    private fun hashClave(clave: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(clave.toCharArray(), salt, ITERACIONES, LONGITUD_HASH)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private data class CuentaLocal(val usuario: Usuario, val salt: ByteArray, val hash: ByteArray)

    sealed interface ResultadoRegistro {
        data class Exito(val usuario: Usuario) : ResultadoRegistro
        data object NombreInvalido : ResultadoRegistro
        data object CorreoInvalido : ResultadoRegistro
        data object ClaveCorta : ResultadoRegistro
        data object CorreoExistente : ResultadoRegistro
    }

    companion object {
        private const val CLAVE_PRUEBA = "123456"
        private const val CLAVE_CUENTAS = "cuentas"
        private const val ITERACIONES = 120_000
        private const val LONGITUD_HASH = 256
        private val CORREO_VALIDO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
