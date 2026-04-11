package com.sssl.asisteciaqr.ui.screens.profesor

import android.content.Intent
import android.widget.Toast
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
import androidx.core.content.FileProvider
import com.sssl.asisteciaqr.ui.viewmodel.AsistenciaViewModel
import com.sssl.asisteciaqr.ui.viewmodel.CSVImportResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrupoDetailScreen(
    viewModel: AsistenciaViewModel,
    grupoId: Long,
    onScanAsistencia: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val grupos by viewModel.grupos.collectAsState()
    val alumnos by viewModel.alumnosDelGrupo.collectAsState()
    val asistenciasHoy by viewModel.asistenciasHoy.collectAsState()
    val csvImportResult by viewModel.csvImportResult.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var isGeneratingPDF by remember { mutableStateOf(false) }

    // Launcher para seleccionar archivo CSV
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importarCSV(context, it, grupoId)
        }
    }

    // Cargar grupo seleccionado
    LaunchedEffect(grupoId) {
        val grupo = grupos.find { it.id == grupoId }
        grupo?.let { viewModel.selectGrupo(it) }
    }

    val selectedGrupo = grupos.find { it.id == grupoId }

    // Mostrar resultado de importación CSV
    LaunchedEffect(csvImportResult) {
        csvImportResult?.let { result ->
            when (result) {
                is CSVImportResult.Success -> {
                    val mensaje = "✓ ${result.importados} alumnos importados" +
                            if (result.errores.isNotEmpty()) "\n⚠ ${result.errores.size} errores" else ""
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                }
                is CSVImportResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
            viewModel.clearCSVImportResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedGrupo?.nombreGrupo ?: "Grupo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    // Botón para generar PDF
                    IconButton(
                        onClick = {
                            if (alumnos.isEmpty()) {
                                Toast.makeText(context, "No hay alumnos para generar QRs", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }
                            isGeneratingPDF = true
                            viewModel.generarPDFconQRs(
                                context = context,
                                grupoId = grupoId,
                                onSuccess = { file ->
                                    isGeneratingPDF = false
                                    // Compartir PDF
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir PDF"))
                                },
                                onError = { error ->
                                    isGeneratingPDF = false
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        enabled = !isGeneratingPDF
                    ) {
                        if (isGeneratingPDF) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, "Generar PDF")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón pasar lista
                FloatingActionButton(
                    onClick = onScanAsistencia,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.QrCodeScanner, "Pasar lista")
                }

                // Botón agregar alumno manual
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.PersonAdd, "Agregar alumno")
                }

                // Botón importar CSV
                FloatingActionButton(
                    onClick = { csvLauncher.launch("text/*") },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Upload, "Importar CSV")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header con info del grupo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = selectedGrupo?.materia ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total alumnos",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${alumnos.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column {
                            Text(
                                text = "Presentes hoy",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${asistenciasHoy.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Lista de alumnos
            if (alumnos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay alumnos registrados",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Importa un CSV o agrega alumnos manualmente",
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
                    items(alumnos) { alumno ->
                        val presente = asistenciasHoy.any { it.matriculaAlumno == alumno.matricula }
                        AlumnoItem(
                            nombre = alumno.nombre,
                            matricula = alumno.matricula,
                            presente = presente
                        )
                    }
                }
            }
        }
    }

    // Dialog para agregar alumno manualmente
    if (showAddDialog) {
        AddAlumnoDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nombre, matricula ->
                viewModel.agregarAlumnoManual(nombre, matricula, grupoId) {
                    showAddDialog = false
                    Toast.makeText(context, "✓ Alumno agregado", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun AlumnoItem(
    nombre: String,
    matricula: String,
    presente: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (presente)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (presente) Icons.Default.CheckCircle else Icons.Default.Person,
                contentDescription = null,
                tint = if (presente)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = matricula,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (presente) {
                Text(
                    text = "Presente",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddAlumnoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var matricula by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Alumno") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = matricula,
                    onValueChange = { matricula = it },
                    label = { Text("Matrícula") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(nombre, matricula) },
                enabled = nombre.isNotBlank() && matricula.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}