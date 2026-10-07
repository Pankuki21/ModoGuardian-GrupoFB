package com.example.modoguardian.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modoguardian.model.Usuario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(usuarioActual: Usuario, onCerrarSesion: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administrador - Modo Guardián") },
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
            Text(
                text = "Bienvenido/a, ${usuarioActual.nombre}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Gestión del Sistema", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Usuarios registrados de prueba: admin, supervisor, operador")
                    Text("• Configuración global de parámetros de alertas: Activa")
                }
            }
        }
    }
}