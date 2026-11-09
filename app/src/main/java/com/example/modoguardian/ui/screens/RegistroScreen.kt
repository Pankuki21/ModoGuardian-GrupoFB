package com.example.modoguardian.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.modoguardian.R
import com.example.modoguardian.viewmodel.RegistroViewModel

@Composable
fun RegistroScreen(
    registroViewModel: RegistroViewModel,
    onRegistroExitoso: () -> Unit,
    onIniciarSesion: () -> Unit
) {
    val estado by registroViewModel.uiState.collectAsState()
    Scaffold(containerColor = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(padding).padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(Modifier.fillMaxWidth().widthIn(max = 480.dp)) {
                Image(
                    painter = painterResource(R.drawable.logo_guardian),
                    contentDescription = "Logo de Modo Guardián",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.align(Alignment.CenterHorizontally).width(190.dp).height(104.dp)
                )
                Spacer(Modifier.height(28.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp)) {
                        Text("Crear cuenta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text("Regístrate en Modo Guardián", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Las cuentas nuevas usan el perfil Operador.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(18.dp))

                        OutlinedTextField(
                            value = estado.nombre,
                            onValueChange = registroViewModel::onNombreChange,
                            label = { Text("Nombre") }, singleLine = true,
                            isError = estado.errorNombre != null,
                            supportingText = { estado.errorNombre?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = estado.correo,
                            onValueChange = registroViewModel::onCorreoChange,
                            label = { Text("Correo electrónico") }, singleLine = true,
                            isError = estado.errorCorreo != null,
                            supportingText = { estado.errorCorreo?.let { Text(it) } },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = estado.clave,
                            onValueChange = registroViewModel::onClaveChange,
                            label = { Text("Contraseña") }, singleLine = true,
                            isError = estado.errorClave != null,
                            supportingText = { estado.errorClave?.let { Text(it) } },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = estado.confirmarClave,
                            onValueChange = registroViewModel::onConfirmarClaveChange,
                            label = { Text("Confirmar contraseña") }, singleLine = true,
                            isError = estado.errorConfirmacion != null,
                            supportingText = { estado.errorConfirmacion?.let { Text(it) } },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                        estado.errorGeneral?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(Modifier.height(18.dp))
                        Button(onClick = { if (registroViewModel.registrar()) onRegistroExitoso() }, modifier = Modifier.fillMaxWidth().height(54.dp)) {
                            Text("Crear cuenta", fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = onIniciarSesion, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text("¿Ya tienes cuenta? Inicia sesión")
                        }
                    }
                }
            }
        }
    }
}
