package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorScreen(usuarioActual: Usuario, onCerrarSesion: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Supervisor") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Métricas globales de seguridad", style = MaterialTheme.typography.titleLarge)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardKpi("Total Eventos", "18", Modifier.weight(1f))
                CardKpi("Pendientes", "3", Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardKpi("Atendidos", "15", Modifier.weight(1f))
                CardKpi("T. Respuesta Prom.", "2.4 min", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CardKpi(titulo: String, valor: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = titulo, style = MaterialTheme.typography.labelMedium)
            Text(text = valor, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
