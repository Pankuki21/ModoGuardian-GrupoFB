package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian.model.Rol
import com.example.modoguardian.navigation.MainViewModel
import com.example.modoguardian.navigation.NavigationEvent
import com.example.modoguardian.ui.screens.AdminScreen
import com.example.modoguardian.ui.screens.LoginScreen
import com.example.modoguardian.ui.screens.OperadorScreen
import com.example.modoguardian.ui.screens.SupervisorScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val windowSizeClass = calculateWindowSizeClass(this)
            val navController = rememberNavController()

            val loginViewModel: LoginViewModel = viewModel()
            val mainViewModel: MainViewModel = viewModel()

            // Observamos el usuario actual sin acceder directamente a StateFlow.value.
            val usuarioActual by loginViewModel.usuarioActual.collectAsState()

            /*
             * Escucha los eventos de navegación enviados
             * por MainViewModel.
             */
            LaunchedEffect(Unit) {

                mainViewModel.navigationEvent.collect { event ->

                    when (event) {

                        is NavigationEvent.NavigateTo -> {
                            navController.navigate(event.route)
                        }

                        NavigationEvent.NavigateBack -> {
                            navController.popBackStack()
                        }
                    }
                }
            }

            ModoGuardianTheme {

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {

                    // Pantalla de inicio de sesión.
                    composable("login") {

                        LoginScreen(
                            loginViewModel = loginViewModel,
                            widthSizeClass = windowSizeClass.widthSizeClass,
                            onLoginSuccess = {

                                usuarioActual?.let { usuario ->

                                    val ruta = when (usuario.rol) {
                                        Rol.ADMINISTRADOR -> "admin"
                                        Rol.SUPERVISOR -> "supervisor"
                                        Rol.OPERADOR -> "operador"
                                    }

                                    mainViewModel.navigateTo(ruta)
                                }
                            }
                        )
                    }

                    // Pantalla del Administrador.
                    composable("admin") {

                        usuarioActual?.let { usuario ->

                            AdminScreen(
                                usuarioActual = usuario,
                                onCerrarSesion = {
                                    loginViewModel.cerrarSesion()

                                    navController.navigate("login") {
                                        popUpTo(0) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Pantalla del Supervisor.
                    composable("supervisor") {

                        usuarioActual?.let { usuario ->

                            SupervisorScreen(
                                usuarioActual = usuario,
                                onCerrarSesion = {
                                    loginViewModel.cerrarSesion()

                                    navController.navigate("login") {
                                        popUpTo(0) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Pantalla del Operador.
                    composable("operador") {

                        usuarioActual?.let { usuario ->

                            OperadorScreen(
                                usuarioActual = usuario,
                                onCerrarSesion = {
                                    loginViewModel.cerrarSesion()

                                    navController.navigate("login") {
                                        popUpTo(0) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}