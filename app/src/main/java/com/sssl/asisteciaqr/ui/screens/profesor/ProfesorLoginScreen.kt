package com.sssl.asisteciaqr.ui.screens.profesor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sssl.asisteciaqr.utils.PasswordManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfesorLoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showRecoveryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acceso Profesor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Ingresa la contraseña",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    showError = false
                },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                isError = showError
            )

            if (showError) {
                Text(
                    text = "Contraseña incorrecta",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (PasswordManager.verifyPassword(context, password)) {
                        onLoginSuccess()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = password.isNotBlank()
            ) {
                Text("Ingresar")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (PasswordManager.hasRecoveryPin(context)) {
                TextButton(onClick = { showRecoveryDialog = true }) {
                    Icon(Icons.Default.Help, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("¿Olvidaste tu contraseña?")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (PasswordManager.isUsingDefaultPassword(context)) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, null, Modifier.size(20.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Contraseña predeterminada:", style = MaterialTheme.typography.bodySmall)
                        Text("profesor123", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("Cámbiala desde Configuración", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (showRecoveryDialog) {
        RecoveryDialog(
            onDismiss = { showRecoveryDialog = false },
            onSuccess = { newPassword ->
                password = newPassword
                showRecoveryDialog = false
            }
        )
    }
}

@Composable
fun RecoveryDialog(onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showNewPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(if (step == 1) Icons.Default.Pin else Icons.Default.Lock, null) },
        title = { Text(if (step == 1) "Recuperar Contraseña" else "Nueva Contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (step == 1) {
                    Text("Ingresa tu PIN de recuperación de 4 dígitos")
                    OutlinedTextField(
                        value = pin,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pin = it
                                errorMessage = null
                            }
                        },
                        label = { Text("PIN de recuperación") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null
                    )
                } else {
                    Text("Ingresa tu nueva contraseña")
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; errorMessage = null },
                        label = { Text("Nueva contraseña") },
                        visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                Icon(if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = { Text("Confirmar contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null
                    )
                }
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step == 1) {
                        if (pin.length != 4) errorMessage = "El PIN debe tener 4 dígitos"
                        else if (!PasswordManager.verifyRecoveryPin(context, pin)) errorMessage = "PIN incorrecto"
                        else { step = 2; errorMessage = null }
                    } else {
                        when {
                            newPassword.length < 6 -> errorMessage = "Mínimo 6 caracteres"
                            newPassword != confirmPassword -> errorMessage = "Las contraseñas no coinciden"
                            else -> if (PasswordManager.setPassword(context, newPassword)) onSuccess(newPassword) else errorMessage = "Error al guardar"
                        }
                    }
                },
                enabled = if (step == 1) pin.length == 4 else newPassword.isNotBlank() && confirmPassword.isNotBlank()
            ) {
                Text(if (step == 1) "Verificar" else "Cambiar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}