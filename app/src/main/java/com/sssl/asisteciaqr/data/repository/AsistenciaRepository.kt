package com.sssl.asisteciaqr.data.repository

import com.sssl.asisteciaqr.data.dao.AlumnoDao
import com.sssl.asisteciaqr.data.dao.AsistenciaDao
import com.sssl.asisteciaqr.data.dao.GrupoDao
import com.sssl.asisteciaqr.data.entity.Alumno
import com.sssl.asisteciaqr.data.entity.Asistencia
import com.sssl.asisteciaqr.data.entity.Grupo
import kotlinx.coroutines.flow.Flow

class AsistenciaRepository(
    private val grupoDao: GrupoDao,
    private val alumnoDao: AlumnoDao,
    private val asistenciaDao: AsistenciaDao
) {

    // ==================== GRUPOS ====================

    fun getAllGrupos(): Flow<List<Grupo>> = grupoDao.getAllGrupos()

    suspend fun insertGrupo(grupo: Grupo): Long = grupoDao.insertGrupo(grupo)

    suspend fun updateGrupo(grupo: Grupo) = grupoDao.updateGrupo(grupo)

    suspend fun deleteGrupo(grupo: Grupo) = grupoDao.deleteGrupo(grupo)

    suspend fun getGrupoById(grupoId: Long): Grupo? = grupoDao.getGrupoById(grupoId)

    fun getGrupoByIdFlow(grupoId: Long): Flow<Grupo?> = grupoDao.getGrupoByIdFlow(grupoId)

    // ==================== ALUMNOS ====================

    fun getAlumnosByGrupo(grupoId: Long): Flow<List<Alumno>> =
        alumnoDao.getAlumnosByGrupo(grupoId)

    suspend fun insertAlumno(alumno: Alumno) = alumnoDao.insertAlumno(alumno)

    suspend fun insertAlumnos(alumnos: List<Alumno>) = alumnoDao.insertAlumnos(alumnos)

    suspend fun updateAlumno(alumno: Alumno) = alumnoDao.updateAlumno(alumno)

    suspend fun deleteAlumno(alumno: Alumno) = alumnoDao.deleteAlumno(alumno)

    suspend fun getAlumnoByMatricula(matricula: String): Alumno? =
        alumnoDao.getAlumnoByMatricula(matricula)

    fun getAlumnosCountByGrupo(grupoId: Long): Flow<Int> =
        alumnoDao.getAlumnosCountByGrupo(grupoId)

    // ==================== ASISTENCIAS ====================

    suspend fun insertAsistencia(asistencia: Asistencia): Long =
        asistenciaDao.insertAsistencia(asistencia)

    suspend fun deleteAsistencia(asistencia: Asistencia) =
        asistenciaDao.deleteAsistencia(asistencia)

    fun getAsistenciasByGrupoYFecha(grupoId: Long, fecha: String): Flow<List<Asistencia>> =
        asistenciaDao.getAsistenciasByGrupoYFecha(grupoId, fecha)

    fun getAsistenciasByAlumno(matricula: String): Flow<List<Asistencia>> =
        asistenciaDao.getAsistenciasByAlumno(matricula)

    suspend fun getAsistenciaHoy(matricula: String, fecha: String, grupoId: Long): Asistencia? =
        asistenciaDao.getAsistenciaHoy(matricula, fecha, grupoId)

    fun countAsistenciasByAlumnoEnGrupo(matricula: String, grupoId: Long): Flow<Int> =
        asistenciaDao.countAsistenciasByAlumnoEnGrupo(matricula, grupoId)

    fun getAsistenciasHoy(grupoId: Long, fecha: String): Flow<List<Asistencia>> =
        asistenciaDao.getAsistenciasHoy(grupoId, fecha)

    suspend fun countAsistenciasEnRango(
        matricula: String,
        grupoId: Long,
        fechaInicio: String,
        fechaFin: String
    ): Int = asistenciaDao.countAsistenciasEnRango(matricula, grupoId, fechaInicio, fechaFin)
}