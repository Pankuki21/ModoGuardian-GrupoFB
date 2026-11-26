package com.duoc.appfacosta.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.appfacosta.data.model.UserSession
import com.duoc.appfacosta.ui.components.DarkCard
import com.duoc.appfacosta.ui.theme.*

@Composable
fun ProfileScreen(session: UserSession, onLogout: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) {
        context.getSharedPreferences("insertcode_profile_settings", Context.MODE_PRIVATE)
    }
    var twoStep by remember { mutableStateOf(preferences.getBoolean("two_step", true)) }
    var push by remember { mutableStateOf(preferences.getBoolean("push_notifications", true)) }
    var critical by remember { mutableStateOf(preferences.getBoolean("critical_alerts", true)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Mi perfil", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        DarkCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Accent),
                    contentAlignment = Alignment.Center
                ) { Text(session.initials, color = TextMain, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(session.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(session.email, color = TextMuted, fontSize = 12.sp)
                    Text("${session.role.label} · Zona Sur", color = Accent, fontSize = 12.sp)
                }
            }
        }

        Text("Seguridad", fontWeight = FontWeight.SemiBold)
        DarkCard {
            ToggleRow("Verificación en 2 pasos", if (twoStep) "Protección adicional activada" else "Protección adicional desactivada", twoStep) {
                twoStep = it
                preferences.edit().putBoolean("two_step", it).apply()
            }
            ToggleRow("Notificaciones push", if (push) "Avisos activados" else "Avisos desactivados", push) {
                push = it
                preferences.edit().putBoolean("push_notifications", it).apply()
            }
            ToggleRow("Alertas críticas", if (critical) "Siempre visibles y sonoras" else "Avisos críticos desactivados", critical) {
                critical = it
                preferences.edit().putBoolean("critical_alerts", it).apply()
            }
        }

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Danger.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(16.dp)
        ) { Text("Cerrar sesión", color = Danger, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
