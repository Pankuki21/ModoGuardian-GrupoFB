package com.example.modoguardian

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.modoguardian.model.Rol
import com.example.modoguardian.ui.screens.LoginScreen
import com.example.modoguardian.ui.screens.PanelPrincipalScreen
import com.example.modoguardian.ui.screens.RegistroScreen
import com.example.modoguardian.ui.theme.ModoGuardianTheme
import com.example.modoguardian.viewmodel.LoginViewModel
import com.example.modoguardian.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val windowSizeClass = calculateWindowSizeClass(this)
            val navController = rememberNavController()

            val loginViewModel: LoginViewModel = viewModel()
            val registroViewModel: RegistroViewModel = viewModel()
            var modoOscuro by rememberSaveable { mutableStateOf(true) }

            // Observamos el usuario actual sin acceder directamente a StateFlow.value.
            val usuarioActual by loginViewModel.usuarioActual.collectAsState()

            ModoGuardianTheme(modoOscuro = modoOscuro) {

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

                                    navController.navigate(ruta)
                                }
                            },
                            onCrearCuenta = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        RegistroScreen(
                            registroViewModel = registroViewModel,
                            onRegistroExitoso = {
                                registroViewModel.uiState.value.registrado?.let { usuario ->
                                    loginViewModel.iniciarSesionConUsuario(usuario)
                                    val ruta = when (usuario.rol) {
                                        Rol.ADMINISTRADOR -> "admin"
                                        Rol.SUPERVISOR -> "supervisor"
                                        Rol.OPERADOR -> "operador"
                                    }
                                    navController.navigate(ruta) { popUpTo("login") { inclusive = true } }
                                }
                            },
                            onIniciarSesion = { navController.popBackStack() }
                        )
                    }

                    // Pantalla del Administrador.
                    composable("admin") {

                        usuarioActual?.let { usuario ->

                            PanelPrincipalScreen(
                                usuario = usuario,
                                modoOscuro = modoOscuro,
                                onCambiarModoOscuro = { modoOscuro = it },
                                usuariosPrueba = loginViewModel.obtenerUsuariosDePrueba(),
                                onCambiarRolUsuario = loginViewModel::actualizarRolDePrueba,
                                onCambiarAccesoUsuario = loginViewModel::actualizarAccesoDePrueba,
                                onCerrarSesion = {
                                loginViewModel.cerrarSesion()
                                navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                    }

                    // Pantalla del Supervisor.
                    composable("supervisor") {

                        usuarioActual?.let { usuario ->

                            PanelPrincipalScreen(
                                usuario = usuario,
                                modoOscuro = modoOscuro,
                                onCambiarModoOscuro = { modoOscuro = it },
                                usuariosPrueba = loginViewModel.obtenerUsuariosDePrueba(),
                                onCambiarRolUsuario = loginViewModel::actualizarRolDePrueba,
                                onCambiarAccesoUsuario = loginViewModel::actualizarAccesoDePrueba,
                                onCerrarSesion = {
                                loginViewModel.cerrarSesion()
                                navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                    }

                    // Pantalla del Operador.
                    composable("operador") {

                        usuarioActual?.let { usuario ->

                            PanelPrincipalScreen(
                                usuario = usuario,
                                modoOscuro = modoOscuro,
                                onCambiarModoOscuro = { modoOscuro = it },
                                usuariosPrueba = loginViewModel.obtenerUsuariosDePrueba(),
                                onCambiarRolUsuario = loginViewModel::actualizarRolDePrueba,
                                onCambiarAccesoUsuario = loginViewModel::actualizarAccesoDePrueba,
                                onCerrarSesion = {
                                loginViewModel.cerrarSesion()
                                navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
