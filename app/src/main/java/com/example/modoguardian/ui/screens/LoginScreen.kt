package com.example.modoguardian.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.example.modoguardian.R
import com.example.modoguardian.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    widthSizeClass: WindowWidthSizeClass,
    onLoginSuccess: () -> Unit
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val errores by loginViewModel.errores.collectAsState()

    val isLoginEnabled =
        uiState.email.isNotBlank() &&
                uiState.password.isNotBlank() &&
                !uiState.isLoading

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        when (widthSizeClass) {

            // Diseño para celulares.
            WindowWidthSizeClass.Compact -> {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoginForm(
                        uiState = uiState,
                        errores = errores,
                        isLoginEnabled = isLoginEnabled,
                        loginViewModel = loginViewModel,
                        onLoginSuccess = onLoginSuccess,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Diseño para pantallas medianas.
            WindowWidthSizeClass.Medium -> {

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.70f)
                    ) {
                        LoginForm(
                            uiState = uiState,
                            errores = errores,
                            isLoginEnabled = isLoginEnabled,
                            loginViewModel = loginViewModel,
                            onLoginSuccess = onLoginSuccess,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }

            // Diseño para pantallas grandes.
            WindowWidthSizeClass.Expanded -> {

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        LoginForm(
                            uiState = uiState,
                            errores = errores,
                            isLoginEnabled = isLoginEnabled,
                            loginViewModel = loginViewModel,
                            onLoginSuccess = onLoginSuccess,
                            modifier = Modifier.padding(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginForm(
    uiState: com.example.modoguardian.model.LoginUiState,
    errores: com.example.modoguardian.model.LoginErrores,
    isLoginEnabled: Boolean,
    loginViewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Logo de la aplicación.
        Image(
            painter = painterResource(id = R.drawable.logo_guardian),
            contentDescription = "Logo de Modo Guardián",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Modo Guardián",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo de correo.
        OutlinedTextField(
            value = uiState.email,
            onValueChange = loginViewModel::onEmailChange,
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = errores.email != null,
            supportingText = {
                errores.email?.let {
                    Text(it)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de contraseña.
        OutlinedTextField(
            value = uiState.password,
            onValueChange = loginViewModel::onPasswordChange,
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = errores.password != null,
            supportingText = {
                errores.password?.let {
                    Text(it)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        errores.general?.let {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de inicio de sesión.
        Button(
            onClick = {
                if (loginViewModel.iniciarSesion()) {
                    onLoginSuccess()
                }
            },
            enabled = isLoginEnabled
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Ingresar")
            }
        }
    }
}