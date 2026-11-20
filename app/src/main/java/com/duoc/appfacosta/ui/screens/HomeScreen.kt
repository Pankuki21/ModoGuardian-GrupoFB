package com.duoc.appfacosta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.appfacosta.data.model.Camera
import com.duoc.appfacosta.data.model.SecurityEvent
import com.duoc.appfacosta.data.model.UserSession
import com.duoc.appfacosta.ui.components.DarkCard
import com.duoc.appfacosta.ui.components.EventRow
import com.duoc.appfacosta.ui.components.Pill
import com.duoc.appfacosta.ui.components.SectionTitle
import com.duoc.appfacosta.ui.theme.*

@Composable
fun HomeScreen(
    session: UserSession,
    cameras: List<Camera>,
    events: List<SecurityEvent>,
    onSeeAllCameras: () -> Unit,
    onReport: () -> Unit
) {
    val online = cameras.count { it.online }
    val offline = cameras.size - online

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Accent),
                contentAlignment = Alignment.Center
            ) { Text(session.initials, color = TextMain, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Hola, ${session.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("● Turno activo · Zona Sur", color = Success, fontSize = 12.sp)
            }
            Pill("EN LÍNEA", Success)
        }

        // Estado general
        DarkCard {
            Text("Estado general", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("MODOGUARDIAN · GUARDIÁN OS V2.4", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatBlock("Total", cameras.size.toString(), "Cámaras", TextMain)
                StatBlock("Activas", online.toString(), "En línea", Success)
                StatBlock("Atención", offline.toString(), "Mantenimiento", Warning)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("CAPACIDAD OPERATIVA", color = TextMuted, fontSize = 11.sp)
                Text("91%", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { 0.91f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = Accent,
                trackColor = BgElevated
            )
        }

        // Cámaras asignadas
        SectionTitle("Cámaras asignadas", "Ver todas", onSeeAllCameras)
        cameras.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { CameraTile(it, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        // Eventos en vivo
        SectionTitle("Eventos en vivo")
        events.forEach { EventRow(it) }

        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onReport,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Danger)
        ) {
            Text("⚠  Reportar Incidencia", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StatBlock(label: String, value: String, sub: String, color: androidx.compose.ui.graphics.Color) {
    Column {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = color, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
fun CameraTile(camera: Camera, modifier: Modifier = Modifier) {
    DarkCard(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(BgElevated),
            contentAlignment = Alignment.Center
        ) {
            if (camera.online && camera.recording) {
                Box(Modifier.align(Alignment.TopStart).padding(6.dp)) { Pill("REC", Danger) }
            }
            Text(
                if (camera.online) "📹" else "OFFLINE",
                color = if (camera.online) TextMain else TextMuted,
                fontSize = 13.sp
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(camera.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(
            "${camera.zone} · ${if (camera.online) "En línea" else "Sin conexión"}",
            color = if (camera.online) Success else TextMuted,
            fontSize = 11.sp
        )
    }
}
