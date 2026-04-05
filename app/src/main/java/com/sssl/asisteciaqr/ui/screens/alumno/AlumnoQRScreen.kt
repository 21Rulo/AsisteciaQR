package com.sssl.asisteciaqr.ui.screens.alumno

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sssl.asisteciaqr.utils.QRGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlumnoQRScreen(
    onBack: () -> Unit
) {
    var matricula by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showQR by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Código QR") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!showQR) {
                // Formulario
                Text(
                    text = "Genera tu credencial digital",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = matricula,
                    onValueChange = { matricula = it },
                    label = { Text("Matrícula") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (nombre.isNotBlank() && matricula.isNotBlank()) {
                            val qrContent = QRGenerator.generateAlumnoQRJson(
                                matricula.trim(),
                                nombre.trim()
                            )
                            qrBitmap = QRGenerator.generateQRCode(qrContent, 512)
                            showQR = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nombre.isNotBlank() && matricula.isNotBlank()
                ) {
                    Text("Generar QR")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Este código será tu credencial digital. Muéstralo al profesor cuando pase lista.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                // Mostrar QR
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Matrícula: $matricula",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                qrBitmap?.let { bitmap ->
                    Card(
                        modifier = Modifier.size(300.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Muestra este código al profesor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        showQR = false
                        qrBitmap = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generar otro código")
                }
            }
        }
    }
}