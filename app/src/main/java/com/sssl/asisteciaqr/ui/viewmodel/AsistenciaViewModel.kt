package com.sssl.asisteciaqr.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sssl.asisteciaqr.data.entity.Alumno
import com.sssl.asisteciaqr.data.entity.Asistencia
import com.sssl.asisteciaqr.data.entity.Grupo
import com.sssl.asisteciaqr.data.repository.AsistenciaRepository
import com.sssl.asisteciaqr.utils.CSVParser
import com.sssl.asisteciaqr.utils.DateUtils
import com.sssl.asisteciaqr.utils.PDFGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class AsistenciaViewModel(
    private val repository: AsistenciaRepository
) : ViewModel() {

    // Estado para grupos
    val grupos: StateFlow<List<Grupo>> = repository.getAllGrupos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGrupo = MutableStateFlow<Grupo?>(null)
    val selectedGrupo: StateFlow<Grupo?> = _selectedGrupo.asStateFlow()

    // Estado para alumnos del grupo seleccionado
    val alumnosDelGrupo: StateFlow<List<Alumno>> = _selectedGrupo
        .filterNotNull()
        .flatMapLatest { grupo ->
            repository.getAlumnosByGrupo(grupo.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Estado para asistencias de hoy
    val asistenciasHoy: StateFlow<List<Asistencia>> = _selectedGrupo
        .filterNotNull()
        .flatMapLatest { grupo ->
            repository.getAsistenciasHoy(grupo.id, DateUtils.getCurrentDate())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _scanMessage = MutableStateFlow<ScanMessage?>(null)
    val scanMessage: StateFlow<ScanMessage?> = _scanMessage.asStateFlow()

    private val _csvImportResult = MutableStateFlow<CSVImportResult?>(null)
    val csvImportResult: StateFlow<CSVImportResult?> = _csvImportResult.asStateFlow()

    // ==================== GRUPOS ====================

    fun createGrupo(nombreGrupo: String, materia: String, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val grupo = Grupo(
                nombreGrupo = nombreGrupo.trim(),
                materia = materia.trim()
            )
            val grupoId = repository.insertGrupo(grupo)
            onSuccess(grupoId)
        }
    }

    fun selectGrupo(grupo: Grupo) {
        _selectedGrupo.value = grupo
    }

    fun clearSelectedGrupo() {
        _selectedGrupo.value = null
    }

    fun deleteGrupo(grupo: Grupo, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteGrupo(grupo)
            if (_selectedGrupo.value?.id == grupo.id) {
                _selectedGrupo.value = null
            }
            onSuccess()
        }
    }

    // ==================== ALUMNOS ====================

    /**
     * Importa alumnos desde un archivo CSV
     */
    fun importarCSV(context: Context, uri: Uri, grupoId: Long) {
        viewModelScope.launch {
            try {
                val result = CSVParser.parseCSV(context, uri, grupoId)

                if (result.alumnos.isNotEmpty()) {
                    // Insertar todos los alumnos
                    repository.insertAlumnos(result.alumnos)

                    _csvImportResult.value = CSVImportResult.Success(
                        importados = result.alumnos.size,
                        errores = result.errores
                    )
                } else {
                    _csvImportResult.value = CSVImportResult.Error(
                        "No se encontraron alumnos válidos en el archivo",
                        result.errores
                    )
                }
            } catch (e: Exception) {
                _csvImportResult.value = CSVImportResult.Error(
                    "Error al importar: ${e.message}",
                    emptyList()
                )
            }
        }
    }

    /**
     * Agrega un alumno manualmente
     */
    fun agregarAlumnoManual(nombre: String, matricula: String, grupoId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                // Verificar si ya existe
                val existente = repository.getAlumnoByMatricula(matricula)
                if (existente != null) {
                    _scanMessage.value = ScanMessage.AlreadyExists(nombre)
                } else {
                    val alumno = Alumno(
                        matricula = matricula.trim(),
                        nombre = nombre.trim(),
                        grupoId = grupoId
                        // qrToken se genera automáticamente con UUID
                    )
                    repository.insertAlumno(alumno)
                    onSuccess()
                }
            } catch (e: Exception) {
                _scanMessage.value = ScanMessage.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun deleteAlumno(alumno: Alumno, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAlumno(alumno)
            onSuccess()
        }
    }

    /**
     * Genera un PDF con los códigos QR de todos los alumnos del grupo
     */
    fun generarPDFconQRs(context: Context, grupoId: Long, onSuccess: (File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val grupo = repository.getGrupoById(grupoId)
                val alumnos = alumnosDelGrupo.value

                if (alumnos.isEmpty()) {
                    onError("No hay alumnos en este grupo")
                    return@launch
                }

                val file = PDFGenerator.generarPDFconQRs(
                    context = context,
                    alumnos = alumnos,
                    nombreGrupo = grupo?.nombreGrupo ?: "Grupo"
                )

                if (file != null) {
                    onSuccess(file)
                } else {
                    onError("Error al generar el PDF")
                }
            } catch (e: Exception) {
                onError("Error: ${e.message}")
            }
        }
    }

    // ==================== ASISTENCIAS ====================

    /**
     * Registra asistencia usando el UUID del QR
     */
    fun registrarAsistencia(qrContent: String, grupoId: Long) {
        viewModelScope.launch {
            try {
                // El QR ahora solo contiene el UUID
                val qrToken = qrContent.trim()

                if (qrToken.isBlank()) {
                    _scanMessage.value = ScanMessage.InvalidQR
                    return@launch
                }

                // Buscar alumno por UUID
                val alumno = repository.getAlumnoByQrToken(qrToken)

                when {
                    alumno == null -> {
                        _scanMessage.value = ScanMessage.NotInGroup
                    }
                    alumno.grupoId != grupoId -> {
                        _scanMessage.value = ScanMessage.WrongGroup(alumno.nombre)
                    }
                    else -> {
                        // Verificar si ya registró asistencia hoy
                        val fecha = DateUtils.getCurrentDate()
                        val yaRegistro = repository.getAsistenciaHoy(alumno.matricula, fecha, grupoId)

                        if (yaRegistro != null) {
                            _scanMessage.value = ScanMessage.AlreadyRegistered(alumno.nombre)
                        } else {
                            // Registrar asistencia
                            val asistencia = Asistencia(
                                matriculaAlumno = alumno.matricula,
                                grupoId = grupoId,
                                fecha = fecha,
                                hora = DateUtils.getCurrentTime()
                            )
                            repository.insertAsistencia(asistencia)
                            _scanMessage.value = ScanMessage.Success(alumno.nombre)
                        }
                    }
                }
            } catch (e: Exception) {
                _scanMessage.value = ScanMessage.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun clearScanMessage() {
        _scanMessage.value = null
    }

    fun clearCSVImportResult() {
        _csvImportResult.value = null
    }
}

// Clase sellada para mensajes de escaneo
sealed class ScanMessage {
    data class Success(val nombre: String) : ScanMessage()
    data class AlreadyRegistered(val nombre: String) : ScanMessage()
    data class AlumnoAdded(val nombre: String) : ScanMessage()
    data class AlreadyExists(val nombre: String) : ScanMessage()
    data class WrongGroup(val nombre: String) : ScanMessage()
    data class Error(val message: String) : ScanMessage()
    object NotInGroup : ScanMessage()
    object InvalidQR : ScanMessage()
}

// Resultado de importación CSV
sealed class CSVImportResult {
    data class Success(val importados: Int, val errores: List<String>) : CSVImportResult()
    data class Error(val message: String, val errores: List<String>) : CSVImportResult()
}