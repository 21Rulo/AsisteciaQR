package com.sssl.asisteciaqr.ui.screens.profesor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sssl.asisteciaqr.model.PeriodoFiltro
import com.sssl.asisteciaqr.ui.viewmodel.AsistenciaViewModel
import com.sssl.asisteciaqr.utils.ColorAsistencia
import com.sssl.asisteciaqr.utils.DateUtils
import com.sssl.asisteciaqr.utils.EstadisticasUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleAlumnoScreen(
    viewModel: AsistenciaViewModel,
    grupoId: Long,
    matricula: String,
    onGenerarPDFIndividual: () -> Unit,
    onBack: () -> Unit
) {
    val alumnos by viewModel.alumnosDelGrupo.collectAsState()
    val asistenciasDetalle by viewModel.asistenciasAlumnoDetalle.collectAsState()
    val periodoSeleccionado by viewModel.periodoSeleccionado.collectAsState()
    val estadisticasAlumnos by viewModel.estadisticasAlumnos.collectAsState()

    val alumno = alumnos.find { it.matricula == matricula }
    val estadistica = estadisticasAlumnos.find { it.alumno.matricula == matricula }

    // Cargar detalle al entrar
    LaunchedEffect(matricula, grupoId, periodoSeleccionado) {
        viewModel.cargarDetalleAlumno(matricula, grupoId, periodoSeleccionado)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = alumno?.nombre ?: "Alumno",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Mat: $matricula",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onGenerarPDFIndividual) {
                        Icon(Icons.Default.PictureAsPdf, "Generar PDF")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Resumen de estadísticas
            estadistica?.let { stats ->
                ResumenAlumnoCard(stats)
            }

            // Historial de asistencias
            if (asistenciasDetalle.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sin asistencias",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "en el periodo seleccionado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Agrupar por fecha y obtener todas las fechas de clases
                LaunchedEffect(grupoId) {
                    viewModel.cargarEstadisticas(grupoId, periodoSeleccionado)
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Historial de ${periodoSeleccionado.nombre}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(asistenciasDetalle) { asistencia ->
                        AsistenciaItemCard(asistencia)
                    }
                }
            }
        }
    }
}

@Composable
fun ResumenAlumnoCard(estadistica: EstadisticasUtils.EstadisticasAlumno) {
    val backgroundColor = when (estadistica.colorCategoria) {
        ColorAsistencia.EXCELENTE -> Color(0xFF4CAF50)
        ColorAsistencia.BUENO -> Color(0xFF2196F3)
        ColorAsistencia.REGULAR -> Color(0xFFFF9800)
        ColorAsistencia.MALO -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Resumen Global",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = estadistica.alumno.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = backgroundColor
                ) {
                    Text(
                        text = EstadisticasUtils.formatearPorcentaje(estadistica.porcentaje),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ResumenStat(
                    label = "Asistencias",
                    value = "${estadistica.totalAsistencias}",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50)
                )

                ResumenStat(
                    label = "Faltas",
                    value = "${estadistica.totalFaltas}",
                    icon = Icons.Default.Cancel,
                    color = Color(0xFFF44336)
                )

                ResumenStat(
                    label = "Total Clases",
                    value = "${estadistica.totalClases}",
                    icon = Icons.Default.CalendarToday,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ResumenStat(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AsistenciaItemCard(asistencia: com.sssl.asisteciaqr.data.entity.Asistencia) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = Color(0xFF4CAF50)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = DateUtils.formatDateForDisplay(asistencia.fecha),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = DateUtils.formatTimeForDisplay(asistencia.hora),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Presente",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}