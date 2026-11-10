package com.example.modoguardian.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

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
}
