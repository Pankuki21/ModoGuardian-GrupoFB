package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Usuario

data class EventoSeguridad(
    val id: Int,
    val camara: String,
    val descripcion: String,
    var atendido: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperadorScreen(
    usuarioActual: Usuario,
    onCerrarSesion: () -> Unit
) {
    var listaEventos by remember {
        mutableStateOf(
            listOf(
                EventoSeguridad(1, "Cámara Acceso Norte", "Detección de movimiento sospechoso", false),
                EventoSeguridad(2, "Cámara Estacionamiento", "Alerta de perímetro vulnerado", false),
                EventoSeguridad(3, "Cámara Recepción", "Ingreso fuera de horario", true)
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Operador / Monitoreo") },
                actions = {
                    TextButton(onClick = onCerrarSesion) {
                        Text("Cerrar Sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Listado de Eventos / Alertas", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listaEventos) { evento ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(evento.camara, style = MaterialTheme.typography.titleSmall)
                                Text(evento.descripcion, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    if (evento.atendido) "Estado: Atendido" else "Estado: PENDIENTE",
                                    color = if (evento.atendido) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            if (!evento.atendido) {
                                Button(
                                    onClick = {
                                        listaEventos = listaEventos.map {
                                            if (it.id == evento.id) it.copy(atendido = true) else it
                                        }
                                    }
                                ) {
                                    Text("Atender")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}