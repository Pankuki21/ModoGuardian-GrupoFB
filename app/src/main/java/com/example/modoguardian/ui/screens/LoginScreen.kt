package com.example.modoguardian.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.modoguardian.R
import com.example.modoguardian.ui.theme.AzulInsertCode
import com.example.modoguardian.viewmodel.LoginViewModel
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    widthSizeClass: WindowWidthSizeClass,
    onLoginSuccess: () -> Unit,
    onCrearCuenta: () -> Unit
) {
    val activity = LocalContext.current as? Activity
    val uiState by loginViewModel.uiState.collectAsState()
    val errores by loginViewModel.errores.collectAsState()
    val usuarioActual by loginViewModel.usuarioActual.collectAsState()
    LaunchedEffect(usuarioActual) {
        if (usuarioActual != null) {
            onLoginSuccess()
        }
    }
    val anchoMaximo = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> 520.dp
        WindowWidthSizeClass.Medium -> 600.dp
        else -> 640.dp
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { insets ->
        Box(
            modifier = Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = anchoMaximo).padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_guardian),
                    contentDescription = "Logo de Modo Guardián",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.width(190.dp).height(104.dp)
                )
                Text("INSERCODE · SEGURIDAD INTEGRAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.5.sp)
                Text("ACCESO CON FIREBASE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(Modifier.height(28.dp))
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(28.dp)) {
                    Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconoUi(IconosUi.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text("Acceso seguro", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.weight(1f))
                        Text("●", color = MaterialTheme.colorScheme.secondary)
                    }
                }

                Spacer(Modifier.height(34.dp))
                Text("Bienvenido\nde vuelta", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
                Spacer(Modifier.height(10.dp))
                Text("Ingresa a tu turno de monitoreo", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(30.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = loginViewModel::onEmailChange,
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    isError = errores.email != null,
                    supportingText = { errores.email?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                var mostrarClave by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = loginViewModel::onPasswordChange,
                    label = { Text("Contraseña") },
                    singleLine = true,
                    isError = errores.password != null,
                    supportingText = { errores.password?.let { Text(it) } },
                    visualTransformation = if (mostrarClave) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { mostrarClave = !mostrarClave }) {
                            IconoUi(if (mostrarClave) IconosUi.VisibilityOff else IconosUi.Visibility, contentDescription = if (mostrarClave) "Ocultar contraseña" else "Mostrar contraseña")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                errores.general?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { loginViewModel.iniciarSesion() },
                    enabled = uiState.email.isNotBlank() && uiState.password.isNotBlank() && !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulInsertCode),
                    modifier = Modifier.fillMaxWidth().height(58.dp)
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else Text("Ingresar al sistema", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
                Text("Tus permisos se cargarán según tu perfil", modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                    Text("  O CONTINUAR CON  ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    androidx.compose.material3.HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outline)
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { activity?.let(loginViewModel::iniciarSesionConGoogle) },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.weight(1f)
                    ) { Text("G   Google") }
                    OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) { Text("●   Apple") }
                }
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    Text("¿No tienes cuenta? ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = onCrearCuenta) { Text("Crear cuenta", fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        IconoUi(IconosUi.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Text("Autenticación y permisos administrados por Firebase", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
