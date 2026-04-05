package com.sssl.asisteciaqr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sssl.asisteciaqr.data.entity.Alumno
import com.sssl.asisteciaqr.data.entity.Asistencia
import com.sssl.asisteciaqr.data.entity.Grupo
import com.sssl.asisteciaqr.data.repository.AsistenciaRepository
import com.sssl.asisteciaqr.utils.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun addAlumnoFromQR(qrContent: String, grupoId: Long) {
        viewModelScope.launch {
            try {
                // Parsear JSON del QR
                val matriculaRegex = """"matricula"\s*:\s*"([^"]+)"""".toRegex()
                val nombreRegex = """"nombre"\s*:\s*"([^"]+)"""".toRegex()

                val matricula = matriculaRegex.find(qrContent)?.groupValues?.get(1)
                val nombre = nombreRegex.find(qrContent)?.groupValues?.get(1)

                if (matricula != null && nombre != null) {
                    // Verificar si ya existe
                    val existente = repository.getAlumnoByMatricula(matricula)
                    if (existente != null) {
                        _scanMessage.value = ScanMessage.AlreadyExists(nombre)
                    } else {
                        val alumno = Alumno(
                            matricula = matricula,
                            nombre = nombre,
                            grupoId = grupoId
                        )
                        repository.insertAlumno(alumno)
                        _scanMessage.value = ScanMessage.AlumnoAdded(nombre)
                    }
                } else {
                    _scanMessage.value = ScanMessage.InvalidQR
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

    // ==================== ASISTENCIAS ====================

    fun registrarAsistencia(qrContent: String, grupoId: Long) {
        viewModelScope.launch {
            try {
                // Parsear JSON del QR
                val matriculaRegex = """"matricula"\s*:\s*"([^"]+)"""".toRegex()
                val nombreRegex = """"nombre"\s*:\s*"([^"]+)"""".toRegex()

                val matricula = matriculaRegex.find(qrContent)?.groupValues?.get(1)
                val nombre = nombreRegex.find(qrContent)?.groupValues?.get(1)

                if (matricula.isNullOrBlank()) {
                    _scanMessage.value = ScanMessage.InvalidQR
                    return@launch
                }

                // Verificar que el alumno existe y pertenece al grupo
                val alumno = repository.getAlumnoByMatricula(matricula)

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
                        val yaRegistro = repository.getAsistenciaHoy(matricula, fecha, grupoId)

                        if (yaRegistro != null) {
                            _scanMessage.value = ScanMessage.AlreadyRegistered(alumno.nombre)
                        } else {
                            // Registrar asistencia
                            val asistencia = Asistencia(
                                matriculaAlumno = matricula,
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