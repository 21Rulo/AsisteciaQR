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
import com.sssl.asisteciaqr.utils.EstadisticasUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialAsistenciaScreen(
    viewModel: AsistenciaViewModel,
    grupoId: Long,
    onAlumnoClick: (String) -> Unit, // matricula
    onGenerarPDFGrupo: () -> Unit,
    onBack: () -> Unit
) {
    val grupos by viewModel.grupos.collectAsState()
    val periodoSeleccionado by viewModel.periodoSeleccionado.collectAsState()
    val estadisticasAlumnos by viewModel.estadisticasAlumnos.collectAsState()
    val estadisticasGrupo by viewModel.estadisticasGrupo.collectAsState()

    val grupo = grupos.find { it.id == grupoId }

    // Cargar estadísticas al entrar
    LaunchedEffect(grupoId, periodoSeleccionado) {
        viewModel.cargarEstadisticas(grupoId, periodoSeleccionado)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial - ${grupo?.nombreGrupo ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onGenerarPDFGrupo) {
                        Icon(Icons.Default.PictureAsPdf, "Generar PDF del Grupo")
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
            // Filtros de periodo
            FiltrosPeriodo(
                periodoSeleccionado = periodoSeleccionado,
                onPeriodoChange = { periodo ->
                    viewModel.setPeriodoFiltro(periodo, grupoId)
                }
            )

            // Estadísticas generales
            estadisticasGrupo?.let { stats ->
                EstadisticasGeneralesCard(stats)
            }

            // Lista de alumnos con estadísticas
            if (estadisticasAlumnos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay asistencias registradas",
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
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(estadisticasAlumnos.sortedByDescending { it.porcentaje }) { estadistica ->
                        AlumnoEstadisticaCard(
                            estadistica = estadistica,
                            onClick = { onAlumnoClick(estadistica.alumno.matricula) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FiltrosPeriodo(
    periodoSeleccionado: PeriodoFiltro,
    onPeriodoChange: (PeriodoFiltro) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            PeriodoFiltro.values().forEach { periodo ->
                FilterChip(
                    selected = periodoSeleccionado == periodo,
                    onClick = { onPeriodoChange(periodo) },
                    label = { Text(periodo.nombre) },
                    leadingIcon = if (periodoSeleccionado == periodo) {
                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
fun EstadisticasGeneralesCard(stats: EstadisticasUtils.EstadisticasGrupo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estadísticas Generales",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Clases",
                    value = "${stats.totalClases}",
                    icon = Icons.Default.CalendarToday
                )
                StatItem(
                    label = "Promedio",
                    value = EstadisticasUtils.formatearPorcentaje(stats.promedioAsistencia),
                    icon = Icons.Default.ShowChart
                )
                StatItem(
                    label = "Alumnos",
                    value = "${stats.totalAlumnos}",
                    icon = Icons.Default.Group
                )
            }

            if (stats.totalClases > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MiniStatBadge("Excelentes", stats.alumnosExcelentes, Color(0xFF4CAF50))
                    MiniStatBadge("Buenos", stats.alumnosBuenos, Color(0xFF2196F3))
                    MiniStatBadge("Regulares", stats.alumnosRegulares, Color(0xFFFF9800))
                    MiniStatBadge("Riesgo", stats.alumnosRiesgo, Color(0xFFF44336))
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MiniStatBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = color.copy(alpha = 0.2f)
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AlumnoEstadisticaCard(
    estadistica: EstadisticasUtils.EstadisticasAlumno,
    onClick: () -> Unit
) {
    val backgroundColor = when (estadistica.colorCategoria) {
        ColorAsistencia.EXCELENTE -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        ColorAsistencia.BUENO -> Color(0xFF2196F3).copy(alpha = 0.15f)
        ColorAsistencia.REGULAR -> Color(0xFFFF9800).copy(alpha = 0.15f)
        ColorAsistencia.MALO -> Color(0xFFF44336).copy(alpha = 0.15f)
    }

    val iconColor = when (estadistica.colorCategoria) {
        ColorAsistencia.EXCELENTE -> Color(0xFF4CAF50)
        ColorAsistencia.BUENO -> Color(0xFF2196F3)
        ColorAsistencia.REGULAR -> Color(0xFFFF9800)
        ColorAsistencia.MALO -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (estadistica.colorCategoria) {
                    ColorAsistencia.EXCELENTE -> Icons.Default.CheckCircle
                    ColorAsistencia.BUENO -> Icons.Default.ThumbUp
                    ColorAsistencia.REGULAR -> Icons.Default.Warning
                    ColorAsistencia.MALO -> Icons.Default.Error
                },
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = estadistica.alumno.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = estadistica.alumno.matricula,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${estadistica.totalAsistencias}/${estadistica.totalClases} clases • ${estadistica.totalFaltas} faltas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = EstadisticasUtils.formatearPorcentaje(estadistica.porcentaje),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = iconColor
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver detalle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}