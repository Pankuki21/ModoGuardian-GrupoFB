
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
import com.duoc.appfacosta.auth.FirebaseAuthRepository
import com.duoc.appfacosta.auth.GoogleAuth
import com.duoc.appfacosta.data.MockData
import com.duoc.appfacosta.data.model.SecurityEvent
import com.duoc.appfacosta.data.model.UserSession
import com.duoc.appfacosta.ui.screens.*
import com.duoc.appfacosta.ui.theme.AppFACOSTATheme
import com.duoc.appfacosta.R
import com.duoc.appfacosta.ui.theme.BgCard
import com.duoc.appfacosta.ui.theme.TextMuted
import kotlinx.coroutines.launch

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
    val webClientId = stringResource(R.string.google_web_client_id)
    val scope = rememberCoroutineScope()
    val authRepo = remember { FirebaseAuthRepository() }

    var session by remember { mutableStateOf<UserSession?>(null) }
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
            onEmailLogin = { email ->
                session = UserSession.fromEmail(email)
            },
            onGoogleSignIn = {
                val act = activity ?: return@LoginScreen
                loading = true
                loginError = null
                scope.launch {
                    GoogleAuth.signIn(act, webClientId, authRepo)
                        .onSuccess { session = it }
                        .onFailure { loginError = "No se pudo iniciar sesión con Google. Intenta nuevamente." }
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
