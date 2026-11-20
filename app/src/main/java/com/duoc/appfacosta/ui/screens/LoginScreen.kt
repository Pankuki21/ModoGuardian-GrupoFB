package com.duoc.appfacosta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.appfacosta.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    loading: Boolean,
    error: String?,
    onEmailLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var registering by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    val validEmail = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val passwordsMatch = password == confirmPassword
    val canSubmit = !loading && validEmail && password.length >= 6 &&
        if (registering) name.isNotBlank() && passwordsMatch else true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
        }
        Spacer(Modifier.height(12.dp))
        Text("ModoGuardian", fontWeight = FontWeight.Black, fontSize = 22.sp, color = TextMain)
        Text("InsertCode", color = TextMuted, fontSize = 11.sp)

        Spacer(Modifier.height(32.dp))

        Text(
            if (registering) "Crear una cuenta" else "Bienvenido\nde vuelta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextMain,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text(
            if (registering) "Completa tus datos para comenzar" else "Ingresa a tu turno de monitoreo",
            color = TextMuted,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        if (registering) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("NOMBRE COMPLETO") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(14.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("CORREO ELECTRÓNICO") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("CONTRASEÑA") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            visualTransformation = if (showPassword) androidx.compose.ui.text.input.VisualTransformation.None
            else PasswordVisualTransformation(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth()
        )

        if (registering) {
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("CONFIRMAR CONTRASEÑA") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showConfirmPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) androidx.compose.ui.text.input.VisualTransformation.None
                else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmPassword.isNotEmpty() && !passwordsMatch,
                supportingText = {
                    if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                        Text("Las contraseñas no coinciden")
                    }
                },
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (registering) onRegister(name.trim(), email.trim(), password)
                else onEmailLogin(email.trim(), password)
            },
            enabled = canSubmit,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(if (registering) "Crear cuenta" else "Ingresar al sistema", fontWeight = FontWeight.SemiBold)
            }
        }

        TextButton(onClick = {
            registering = !registering
            password = ""
            confirmPassword = ""
        }, enabled = !loading) {
            Text(if (registering) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate")
        }

        if (!registering) {
            Text(
                "Tus permisos se cargarán automáticamente según tu perfil",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Stroke)
            Text(if (registering) "  O REGISTRAR CON  " else "  O CONTINUAR CON  ", color = TextMuted, fontSize = 11.sp)
            HorizontalDivider(modifier = Modifier.weight(1f), color = Stroke)
        }
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = onGoogleSignIn,
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Stroke),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMain)
        ) {
            Text(if (registering) "Registrarse con Google" else "Continuar con Google", fontWeight = FontWeight.SemiBold)
        }

        if (error != null) {
            Spacer(Modifier.height(16.dp))
            Text(error, color = Danger, fontSize = 13.sp)
        }

        Spacer(Modifier.height(28.dp))
        Text(
            "🔒 Protegido con encriptación de extremo a extremo",
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Accent,
    unfocusedBorderColor = Stroke,
    focusedContainerColor = BgCard,
    unfocusedContainerColor = BgCard,
    focusedTextColor = TextMain,
    unfocusedTextColor = TextMain,
    focusedLabelColor = TextMuted,
    unfocusedLabelColor = TextMuted,
    cursorColor = Accent
)
