
package com.duoc.appfacosta

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.duoc.appfacosta.auth.FirebaseAuthRepository
import com.duoc.appfacosta.auth.GoogleAuth
import com.duoc.appfacosta.data.MockData
import com.duoc.appfacosta.data.model.SecurityEvent
import com.duoc.appfacosta.data.model.UserSession
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.duoc.appfacosta.ui.screens.*
import com.duoc.appfacosta.ui.theme.AppFACOSTATheme
import com.duoc.appfacosta.R
import com.duoc.appfacosta.ui.theme.BgCard
import com.duoc.appfacosta.ui.theme.TextMuted
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AppFACOSTATheme {
                AppRoot()
            }
        }
    }
}

enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Inicio", Icons.Default.Home),
    CAMS("Cámaras", Icons.Default.Videocam),
    EVENTS("Eventos", Icons.Default.Event),
    ALERTS("Alertas", Icons.Default.Notifications),
    PROFILE("Perfil", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppRoot() {
    val activity = LocalContext.current as? Activity
    val webClientId = stringResource(R.string.default_web_client_id)
    val scope = rememberCoroutineScope()
    val authRepo = remember { FirebaseAuthRepository() }

    var session by remember { mutableStateOf(authRepo.obtenerUsuarioActual()?.toUserSession()) }
    var loading by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var tab by remember { mutableStateOf(Tab.HOME) }

    val alerts: SnapshotStateList<SecurityEvent> = remember {
        mutableStateListOf<SecurityEvent>().apply { addAll(MockData.alerts) }
    }
    val pending = alerts.count { it.status == "Pendiente" }

    val current = session
    if (current == null) {
        LoginScreen(
            loading = loading,
            error = loginError,
            onEmailLogin = { email, password ->
                loading = true
                loginError = null
                scope.launch {
                    try {
                        val user = authRepo.iniciarSesion(email, password).await().user
                            ?: error("Firebase no devolvió usuario")
                        session = user.toUserSession()
                    } catch (error: Exception) {
                        loginError = mensajeAuth(error, "No se pudo iniciar sesión")
                    } finally {
                        loading = false
                    }
                }
            },
            onRegister = { name, email, password ->
                loading = true
                loginError = null
                scope.launch {
                    try {
                        val user = authRepo.registrar(email, password).await().user
                            ?: error("Firebase no devolvió usuario")
                        runCatching { authRepo.actualizarNombre(name)?.await() }
                        session = UserSession(name, user.email ?: email)
                    } catch (error: Exception) {
                        loginError = mensajeAuth(error, "No se pudo crear la cuenta")
                    } finally {
                        loading = false
                    }
                }
            },
            onGoogleSignIn = {
                val act = activity ?: return@LoginScreen
                loading = true
                loginError = null
                scope.launch {
                    GoogleAuth.signIn(act, webClientId, authRepo)
                        .onSuccess { session = it }
                        .onFailure { error ->
                            loginError = when (error) {
                                is GetCredentialCancellationException -> null
                                is NoCredentialException -> "Agrega una cuenta de Google a este dispositivo"
                                else -> mensajeAuth(
                                    error,
                                    "No se pudo iniciar sesión con Google. Revisa la configuración de Firebase"
                                )
                            }
                        }
                    loading = false
                }
            }
        )
        return
    }

    Scaffold(
        containerColor = com.duoc.appfacosta.ui.theme.BgDark,
        bottomBar = {
            NavigationBar(containerColor = BgCard) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t,
                        onClick = { tab = t },
                        icon = {
                            if (t == Tab.ALERTS && pending > 0) {
                                BadgedBox(badge = { Badge { Text("$pending") } }) {
                                    Icon(t.icon, contentDescription = t.label)
                                }
                            } else {
                                Icon(t.icon, contentDescription = t.label)
                            }
                        },
                        label = { Text(t.label, color = if (tab == t) Color.White else TextMuted) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tab) {
                Tab.HOME -> HomeScreen(
                    session = current,
                    cameras = MockData.cameras,
                    events = MockData.events,
                    onSeeAllCameras = { tab = Tab.CAMS },
                    onReport = { tab = Tab.ALERTS }
                )
                Tab.CAMS -> CamerasScreen(MockData.cameras)
                Tab.EVENTS -> EventsScreen(MockData.events)
                Tab.ALERTS -> AlertsScreen(alerts)
                Tab.PROFILE -> ProfileScreen(
                    session = current,
                    onLogout = {
                        authRepo.cerrarSesion()
                        session = null
                        tab = Tab.HOME
                    }
                )
            }
        }
    }
}

private fun FirebaseUser.toUserSession() = UserSession(
    name = displayName?.takeIf { it.isNotBlank() } ?: email?.substringBefore("@") ?: "Usuario",
    email = email.orEmpty()
)

private fun mensajeAuth(error: Throwable, fallback: String): String = when {
    error is FirebaseNetworkException -> "No hay conexión a Internet"
    error is FirebaseAuthException -> when (error.errorCode) {
        "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
        "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con ese correo"
        "ERROR_WEAK_PASSWORD" -> "La contraseña debe tener al menos 6 caracteres"
        "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" ->
            "Correo o contraseña incorrectos"
        "ERROR_OPERATION_NOT_ALLOWED" -> "Habilita este método de acceso en Firebase"
        "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
            "Ese correo ya tiene otro método de acceso. Inicia sesión con él primero"
        else -> fallback
    }
    else -> fallback
}
