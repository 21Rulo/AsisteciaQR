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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sssl.asisteciaqr.utils.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionSonidosScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    var selectedSuccess by remember {
        mutableStateOf(SoundManager.getSuccessSound(context))
    }
    var selectedError by remember {
        mutableStateOf(SoundManager.getErrorSound(context))
    }
    var selectedWarning by remember {
        mutableStateOf(SoundManager.getWarningSound(context))
    }

    // Listas de sonidos por categoría
    val successSounds = remember { SoundManager.getSoundsForCategory(context, SoundManager.Category.SUCCESS) }
    val errorSounds = remember { SoundManager.getSoundsForCategory(context, SoundManager.Category.ERROR) }
    val warningSounds = remember { SoundManager.getSoundsForCategory(context, SoundManager.Category.WARNING) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurar Sonidos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Banner informativo
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Presiona ▶ para escuchar cada sonido antes de elegir. Los cambios se guardan automáticamente.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ─────────── SECCIÓN ÉXITO ───────────
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(
                    title = "Sonido de Éxito ✅",
                    description = "Se reproduce cuando la asistencia se registra correctamente",
                    color = Color(0xFF4CAF50)
                )
            }

            items(successSounds) { sound ->
                SoundOptionCard(
                    sound = sound,
                    isSelected = selectedSuccess.id == sound.id,
                    highlightColor = Color(0xFF4CAF50),
                    onSelect = {
                        selectedSuccess = sound
                        SoundManager.setSuccessSound(context, sound)
                    },
                    onPreview = { SoundManager.playSound(context, sound) }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) }

            // ─────────── SECCIÓN ERROR ───────────
            item {
                SectionHeader(
                    title = "Sonido de Error ❌",
                    description = "Se reproduce cuando el alumno no pertenece a este grupo",
                    color = Color(0xFFF44336)
                )
            }

            items(errorSounds) { sound ->
                SoundOptionCard(
                    sound = sound,
                    isSelected = selectedError.id == sound.id,
                    highlightColor = Color(0xFFF44336),
                    onSelect = {
                        selectedError = sound
                        SoundManager.setErrorSound(context, sound)
                    },
                    onPreview = { SoundManager.playSound(context, sound) }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp)) }

            // ─────────── SECCIÓN ADVERTENCIA ───────────
            item {
                SectionHeader(
                    title = "Sonido de Advertencia ⚠️",
                    description = "Se reproduce cuando el alumno ya registró asistencia hoy",
                    color = Color(0xFFFF9800)
                )
            }

            items(warningSounds) { sound ->
                SoundOptionCard(
                    sound = sound,
                    isSelected = selectedWarning.id == sound.id,
                    highlightColor = Color(0xFFFF9800),
                    onSelect = {
                        selectedWarning = sound
                        SoundManager.setWarningSound(context, sound)
                    },
                    onPreview = { SoundManager.playSound(context, sound) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, description: String, color: Color) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SoundOptionCard(
    sound: SoundManager.SoundOption,
    isSelected: Boolean,
    highlightColor: Color,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    val isCustom = sound.source is SoundManager.SoundSource.Raw

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                highlightColor.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isSelected)
            CardDefaults.outlinedCardBorder().copy(brush = SolidColor(highlightColor))
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Text(
                text = sound.emoji,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.width(44.dp)
            )

            // Nombre y descripción
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sound.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) highlightColor else MaterialTheme.colorScheme.onSurface
                    )
                    // Badge si es personalizado
                    if (isCustom) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Custom",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    text = sound.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botón previsualizar
            IconButton(onClick = onPreview) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Escuchar",
                    tint = highlightColor
                )
            }

            // Radio button
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(selectedColor = highlightColor)
            )
        }
    }
}