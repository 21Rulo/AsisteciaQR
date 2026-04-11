package com.sssl.asisteciaqr.ui.screens.profesor

import android.Manifest
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sssl.asisteciaqr.ui.components.ContinuousQRScanner
import com.sssl.asisteciaqr.ui.viewmodel.AsistenciaViewModel
import com.sssl.asisteciaqr.ui.viewmodel.ScanMessage
import com.sssl.asisteciaqr.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanAsistenciaScreen(
    viewModel: AsistenciaViewModel,
    grupoId: Long,
    onBack: () -> Unit
) {
    val scanMessage by viewModel.scanMessage.collectAsState()
    val asistenciasHoy by viewModel.asistenciasHoy.collectAsState()
    val alumnos by viewModel.alumnosDelGrupo.collectAsState()
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }

    val toneGenerator = remember {
        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Efecto de sonido según el mensaje
    LaunchedEffect(scanMessage) {
        when (scanMessage) {
            is ScanMessage.Success -> {
                // Sonido de éxito (beep corto)
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            }
            is ScanMessage.AlreadyRegistered -> {
                // Sonido de advertencia (dos beeps)
                toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
            }
            is ScanMessage.Error, is ScanMessage.InvalidQR, is ScanMessage.NotInGroup, is ScanMessage.WrongGroup -> {
                // Sonido de error (beep largo)
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300)
            }
            else -> {}
        }

        if (scanMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearScanMessage()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pasar Lista") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showStats = !showStats }) {
                        Icon(
                            if (showStats) Icons.Default.CameraAlt else Icons.Default.BarChart,
                            "Alternar vista"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showStats) {
                // Vista de estadísticas
                EstadisticasView(
                    alumnos = alumnos,
                    asistenciasHoy = asistenciasHoy
                )
            } else {
                // Vista de cámara continua
                if (hasPermission) {
                    ContinuousQRScanner(
                        modifier = Modifier.fillMaxSize(),
                        onQRScanned = { qrContent ->
                            viewModel.registrarAsistencia(qrContent, grupoId)
                        }
                    )

                    // Overlay con información
                    ScanOverlay(
                        scanMessage = scanMessage,
                        totalAlumnos = alumnos.size,
                        presentes = asistenciasHoy.size
                    )
                } else {
                    // Sin permiso de cámara
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Permiso de cámara requerido",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                Text("Solicitar permiso")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScanOverlay(
    scanMessage: ScanMessage?,
    totalAlumnos: Int,
    presentes: Int
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Estadísticas en la parte superior
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$presentes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Presentes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${totalAlumnos - presentes}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Faltas",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalAlumnos",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Mensaje de feedback centrado
        scanMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    colors = CardDefaults.cardColors(
                        containerColor = when (message) {
                            is ScanMessage.Success -> Color(0xFF4CAF50)
                            is ScanMessage.AlreadyRegistered -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (message) {
                                is ScanMessage.Success -> Icons.Default.CheckCircle
                                is ScanMessage.AlreadyRegistered -> Icons.Default.Info
                                else -> Icons.Default.Error
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = when (message) {
                                is ScanMessage.Success -> "✓ ${message.nombre}\nAsistencia registrada"
                                is ScanMessage.AlreadyRegistered -> "⚠ ${message.nombre}\nYa registró asistencia"
                                is ScanMessage.WrongGroup -> "✗ ${message.nombre}\nNo pertenece a este grupo"
                                is ScanMessage.NotInGroup -> "✗ Alumno no registrado"
                                is ScanMessage.InvalidQR -> "✗ Código QR inválido"
                                is ScanMessage.Error -> "✗ Error: ${message.message}"
                                else -> ""
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        // Instrucciones
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Text(
                text = "Apunta la cámara al código QR del alumno",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun EstadisticasView(
    alumnos: List<com.sssl.asisteciaqr.data.entity.Alumno>,
    asistenciasHoy: List<com.sssl.asisteciaqr.data.entity.Asistencia>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(alumnos.sortedBy { it.nombre }) { alumno ->
            val presente = asistenciasHoy.any { it.matriculaAlumno == alumno.matricula }
            val asistencia = asistenciasHoy.find { it.matriculaAlumno == alumno.matricula }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (presente)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (presente) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (presente)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = alumno.nombre,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = alumno.matricula,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (presente && asistencia != null) {
                        Text(
                            text = DateUtils.formatTimeForDisplay(asistencia.hora),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}