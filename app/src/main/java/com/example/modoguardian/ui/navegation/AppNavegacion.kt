package com.example.modoguardian.ui.navegation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian.model.Rol
import com.example.modoguardian.ui.screens.*
import com.example.modoguardian.viewmodel.LoginViewModel

@Composable
fun AppNavegacion(
    loginViewModel: LoginViewModel = viewModel()
) {
    val navController = rememberNavController()
    val usuarioActual by loginViewModel.usuarioLogueado.collectAsState()
    val mensajeError by loginViewModel.error.collectAsState()

    LaunchedEffect(usuarioActual) {
        usuarioActual?.let { usuario ->
            when (usuario.rol) {
                Rol.ADMINISTRADOR -> navController.navigate("admin") {
                    popUpTo("login") { inclusive = true }
                }
                Rol.SUPERVISOR -> navController.navigate("supervisor") {
                    popUpTo("login") { inclusive = true }
                }
                Rol.OPERADOR -> navController.navigate("operador") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginClick = { correo, clave ->
                    loginViewModel.iniciarSesion(correo, clave)
                },
                errorMessage = mensajeError
            )
        }

        composable("admin") {
            usuarioActual?.let { usuario ->
                AdminScreen(
                    usuarioActual = usuario,
                    onCerrarSesion = {
                        loginViewModel.cerrarSesion()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("supervisor") {
            usuarioActual?.let { usuario ->
                SupervisorScreen(
                    usuarioActual = usuario,
                    onCerrarSesion = {
                        loginViewModel.cerrarSesion()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("operador") {
            usuarioActual?.let { usuario ->
                OperadorScreen(
                    usuarioActual = usuario,
                    onCerrarSesion = {
                        loginViewModel.cerrarSesion()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}