package com.sssl.asisteciaqr.ui.screens.profesor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sssl.asisteciaqr.ui.viewmodel.AsistenciaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGrupoScreen(
    viewModel: AsistenciaViewModel,
    onGrupoCreated: () -> Unit,
    onBack: () -> Unit
) {
    var nombreGrupo by remember { mutableStateOf("") }
    var materia by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Grupo") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nuevo Grupo",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = nombreGrupo,
                onValueChange = { nombreGrupo = it },
                label = { Text("Nombre del grupo") },
                placeholder = { Text("Ej: 3A, Grupo 1, etc.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = materia,
                onValueChange = { materia = it },
                label = { Text("Materia") },
                placeholder = { Text("Ej: Matemáticas, Biología, etc.") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isCreating = true
                    viewModel.createGrupo(nombreGrupo, materia) {
                        isCreating = false
                        onGrupoCreated()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating && nombreGrupo.isNotBlank() && materia.isNotBlank()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Grupo")
                }
            }
        }
    }
}