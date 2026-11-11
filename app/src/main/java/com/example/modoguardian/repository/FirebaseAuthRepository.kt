package com.example.modoguardian.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class FirebaseAuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // Registrar una nueva cuenta con correo y contraseña
    fun registrar(
        email: String,
        password: String
    ): Task<AuthResult> {
        return firebaseAuth.createUserWithEmailAndPassword(
            email.trim(),
            password
        )
    }

    // Iniciar sesion con una cuenta existente
    fun iniciarSesion(
        email: String,
        password: String
    ): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(
            email.trim(),
            password
        )
    }

    // Iniciar sesión en Firebase con Google
    fun iniciarSesionConGoogle(credencial: AuthCredential): Task<AuthResult> {
        return firebaseAuth.signInWithCredential(credencial)
    }

    // Enviar correo para restablecer la contraseña
    fun enviarRestablecimientoPassword(
        email: String
    ): Task<Void> {
        return firebaseAuth.sendPasswordResetEmail(
            email.trim()
        )
    }

    // Obtener el usuario actualmente autenticado
    fun obtenerUsuarioActual(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    // Cerrar sesion
    fun cerrarSesion() {
        firebaseAuth.signOut()
    }

    // Actualizar el nombre en Firebase
    fun actualizarNombre(nombre: String): Task<Void>? {
        val usuario = firebaseAuth.currentUser ?: return null
        val actualizacion = UserProfileChangeRequest.Builder()
            .setDisplayName(nombre.trim()).build()
        return usuario.updateProfile(actualizacion)
    }
}
