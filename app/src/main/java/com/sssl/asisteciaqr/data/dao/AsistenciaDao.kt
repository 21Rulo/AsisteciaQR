package com.sssl.asisteciaqr.data.dao

import androidx.room.*
import com.sssl.asisteciaqr.data.entity.Asistencia
import kotlinx.coroutines.flow.Flow

@Dao
interface AsistenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsistencia(asistencia: Asistencia): Long

    @Delete
    suspend fun deleteAsistencia(asistencia: Asistencia)

    // Obtener asistencias de un grupo en una fecha específica
    @Query("SELECT * FROM asistencias WHERE grupoId = :grupoId AND fecha = :fecha ORDER BY hora ASC")
    fun getAsistenciasByGrupoYFecha(grupoId: Long, fecha: String): Flow<List<Asistencia>>

    // Obtener todas las asistencias de un alumno
    @Query("SELECT * FROM asistencias WHERE matriculaAlumno = :matricula ORDER BY timestamp DESC")
    fun getAsistenciasByAlumno(matricula: String): Flow<List<Asistencia>>

    // Verificar si un alumno ya registró asistencia hoy
    @Query("SELECT * FROM asistencias WHERE matriculaAlumno = :matricula AND fecha = :fecha AND grupoId = :grupoId LIMIT 1")
    suspend fun getAsistenciaHoy(matricula: String, fecha: String, grupoId: Long): Asistencia?

    // Contar asistencias de un alumno en un grupo
    @Query("SELECT COUNT(*) FROM asistencias WHERE matriculaAlumno = :matricula AND grupoId = :grupoId")
    fun countAsistenciasByAlumnoEnGrupo(matricula: String, grupoId: Long): Flow<Int>

    // Obtener asistencias de hoy para un grupo
    @Query("SELECT * FROM asistencias WHERE grupoId = :grupoId AND fecha = :fecha")
    fun getAsistenciasHoy(grupoId: Long, fecha: String): Flow<List<Asistencia>>

    // ==================== NUEVAS QUERIES PARA HISTORIAL ====================

    // Obtener todas las asistencias de un grupo en un rango de fechas
    @Query("""
        SELECT * FROM asistencias 
        WHERE grupoId = :grupoId 
        AND fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY fecha DESC, hora DESC
    """)
    fun getAsistenciasEnRango(grupoId: Long, fechaInicio: String, fechaFin: String): Flow<List<Asistencia>>

    // Obtener todas las asistencias de un grupo (global)
    @Query("SELECT * FROM asistencias WHERE grupoId = :grupoId ORDER BY fecha DESC, hora DESC")
    fun getAsistenciasGlobal(grupoId: Long): Flow<List<Asistencia>>

    // Contar total de clases (fechas únicas) en un grupo
    @Query("SELECT COUNT(DISTINCT fecha) FROM asistencias WHERE grupoId = :grupoId")
    suspend fun countClasesTotales(grupoId: Long): Int

    // Contar clases en un rango de fechas
    @Query("""
        SELECT COUNT(DISTINCT fecha) FROM asistencias 
        WHERE grupoId = :grupoId 
        AND fecha BETWEEN :fechaInicio AND :fechaFin
    """)
    suspend fun countClasesEnRango(grupoId: Long, fechaInicio: String, fechaFin: String): Int

    // Obtener fechas únicas de clases en un grupo (para calcular total de clases)
    @Query("SELECT DISTINCT fecha FROM asistencias WHERE grupoId = :grupoId ORDER BY fecha DESC")
    suspend fun getFechasClases(grupoId: Long): List<String>

    // Obtener fechas de clases en un rango
    @Query("""
        SELECT DISTINCT fecha FROM asistencias 
        WHERE grupoId = :grupoId 
        AND fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY fecha DESC
    """)
    suspend fun getFechasClasesEnRango(grupoId: Long, fechaInicio: String, fechaFin: String): List<String>

    // Estadísticas: Total de asistencias por alumno en un rango de fechas
    @Query("""
        SELECT COUNT(*) FROM asistencias 
        WHERE matriculaAlumno = :matricula 
        AND grupoId = :grupoId 
        AND fecha BETWEEN :fechaInicio AND :fechaFin
    """)
    suspend fun countAsistenciasEnRango(
        matricula: String,
        grupoId: Long,
        fechaInicio: String,
        fechaFin: String
    ): Int

    // Obtener asistencias de un alumno en un rango
    @Query("""
        SELECT * FROM asistencias 
        WHERE matriculaAlumno = :matricula 
        AND grupoId = :grupoId 
        AND fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY fecha DESC, hora DESC
    """)
    fun getAsistenciasAlumnoEnRango(
        matricula: String,
        grupoId: Long,
        fechaInicio: String,
        fechaFin: String
    ): Flow<List<Asistencia>>

    // Obtener todas las asistencias de un alumno en un grupo (global)
    @Query("""
        SELECT * FROM asistencias 
        WHERE matriculaAlumno = :matricula 
        AND grupoId = :grupoId
        ORDER BY fecha DESC, hora DESC
    """)
    fun getAsistenciasAlumnoGlobal(matricula: String, grupoId: Long): Flow<List<Asistencia>>

    // Eliminar asistencias de un grupo
    @Query("DELETE FROM asistencias WHERE grupoId = :grupoId")
    suspend fun deleteAsistenciasByGrupo(grupoId: Long)
}