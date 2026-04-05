package com.sssl.asisteciaqr.ui.screens.profesor

import android.Manifest
import android.media.ToneGenerator
import android.media.AudioManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
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
    var isScanning by remember { mutableStateOf(false) }

    val toneGenerator = remember {
        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        isScanning = false
        if (result.contents != null) {
            viewModel.registrarAsistencia(result.contents, grupoId)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Efecto de sonido según el mensaje
    LaunchedEffect(scanMessage) {
        when (scanMessage) {
            is ScanMessage.Success -> {
                // Sonido de éxito
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
            }
            is ScanMessage.AlreadyRegistered, is ScanMessage.WrongGroup -> {
                // Sonido de advertencia
                toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 200)
            }
            is ScanMessage.Error, is ScanMessage.InvalidQR, is ScanMessage.NotInGroup -> {
                // Sonido de error
                toneGenerator.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 300)
            }
            else -> {}
        }

        if (scanMessage != null) {
            kotlinx.coroutines.delay(2000)
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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (hasPermission) {
                        isScanning = true
                        val options = ScanOptions().apply {
                            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            setPrompt("Escanea el QR del alumno")
                            setBeepEnabled(false) // Usaremos nuestros propios sonidos
                            setOrientationLocked(false)
                            setCameraId(0)
                        }
                        scanLauncher.launch(options)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.QrCodeScanner, "Escanear")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Estadísticas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = DateUtils.formatDateForDisplay(DateUtils.getCurrentDate()),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Presentes",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${asistenciasHoy.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                text = "Faltas",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${alumnos.size - asistenciasHoy.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Column {
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${alumnos.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Mensaje de feedback
            scanMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (message) {
                            is ScanMessage.Success -> MaterialTheme.colorScheme.primaryContainer
                            is ScanMessage.AlreadyRegistered -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (message) {
                                is ScanMessage.Success -> Icons.Default.CheckCircle
                                is ScanMessage.AlreadyRegistered -> Icons.Default.Info
                                else -> Icons.Default.Error
                            },
                            contentDescription = null,
                            tint = when (message) {
                                is ScanMessage.Success -> MaterialTheme.colorScheme.primary
                                is ScanMessage.AlreadyRegistered -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (message) {
                                is ScanMessage.Success -> "✓ ${message.nombre} - Asistencia registrada"
                                is ScanMessage.AlreadyRegistered -> "⚠ ${message.nombre} ya registró asistencia"
                                is ScanMessage.WrongGroup -> "✗ ${message.nombre} no pertenece a este grupo"
                                is ScanMessage.NotInGroup -> "✗ Alumno no registrado en el sistema"
                                is ScanMessage.InvalidQR -> "✗ Código QR inválido"
                                is ScanMessage.Error -> "✗ Error: ${message.message}"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Lista de asistencias
            if (asistenciasHoy.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay asistencias registradas",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Presiona el botón para escanear",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(asistenciasHoy.sortedByDescending { it.timestamp }) { asistencia ->
                        val alumno = alumnos.find { it.matricula == asistencia.matriculaAlumno }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = alumno?.nombre ?: "Desconocido",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = asistencia.matriculaAlumno,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
    }
}