package com.sssl.asisteciaqr.utils

import com.sssl.asisteciaqr.data.entity.Alumno
import com.sssl.asisteciaqr.data.entity.Asistencia

object EstadisticasUtils {

    /**
     * Calcula el porcentaje de asistencia
     */
    fun calcularPorcentaje(asistencias: Int, totalClases: Int): Float {
        if (totalClases == 0) return 0f
        return (asistencias.toFloat() / totalClases.toFloat()) * 100f
    }

    /**
     * Formatea el porcentaje a string
     */
    fun formatearPorcentaje(porcentaje: Float): String {
        return String.format("%.1f%%", porcentaje)
    }

    /**
     * Determina el color según el porcentaje
     */
    fun getColorPorcentaje(porcentaje: Float): ColorAsistencia {
        return when {
            porcentaje >= 90f -> ColorAsistencia.EXCELENTE
            porcentaje >= 70f -> ColorAsistencia.BUENO
            porcentaje >= 50f -> ColorAsistencia.REGULAR
            else -> ColorAsistencia.MALO
        }
    }

    /**
     * Agrupa asistencias por fecha
     */
    fun agruparPorFecha(asistencias: List<Asistencia>): Map<String, List<Asistencia>> {
        return asistencias.groupBy { it.fecha }
    }

    /**
     * Obtiene las fechas únicas de las asistencias
     */
    fun getFechasUnicas(asistencias: List<Asistencia>): List<String> {
        return asistencias.map { it.fecha }.distinct().sorted()
    }

    /**
     * Calcula estadísticas de un alumno
     */
    data class EstadisticasAlumno(
        val alumno: Alumno,
        val totalAsistencias: Int,
        val totalClases: Int,
        val totalFaltas: Int,
        val porcentaje: Float,
        val colorCategoria: ColorAsistencia
    )

    fun calcularEstadisticasAlumno(
        alumno: Alumno,
        asistenciasAlumno: List<Asistencia>,
        totalClases: Int
    ): EstadisticasAlumno {
        val totalAsistencias = asistenciasAlumno.size
        val totalFaltas = totalClases - totalAsistencias
        val porcentaje = calcularPorcentaje(totalAsistencias, totalClases)
        val color = getColorPorcentaje(porcentaje)

        return EstadisticasAlumno(
            alumno = alumno,
            totalAsistencias = totalAsistencias,
            totalClases = totalClases,
            totalFaltas = totalFaltas,
            porcentaje = porcentaje,
            colorCategoria = color
        )
    }

    /**
     * Calcula estadísticas generales del grupo
     */
    data class EstadisticasGrupo(
        val totalAlumnos: Int,
        val totalClases: Int,
        val promedioAsistencia: Float,
        val alumnosExcelentes: Int,  // >= 90%
        val alumnosBuenos: Int,       // 70-89%
        val alumnosRegulares: Int,    // 50-69%
        val alumnosRiesgo: Int        // < 50%
    )

    fun calcularEstadisticasGrupo(
        estadisticasAlumnos: List<EstadisticasAlumno>
    ): EstadisticasGrupo {
        if (estadisticasAlumnos.isEmpty()) {
            return EstadisticasGrupo(0, 0, 0f, 0, 0, 0, 0)
        }

        val promedioAsistencia = estadisticasAlumnos
            .map { it.porcentaje }
            .average()
            .toFloat()

        val porCategoria = estadisticasAlumnos.groupBy { it.colorCategoria }

        return EstadisticasGrupo(
            totalAlumnos = estadisticasAlumnos.size,
            totalClases = estadisticasAlumnos.firstOrNull()?.totalClases ?: 0,
            promedioAsistencia = promedioAsistencia,
            alumnosExcelentes = porCategoria[ColorAsistencia.EXCELENTE]?.size ?: 0,
            alumnosBuenos = porCategoria[ColorAsistencia.BUENO]?.size ?: 0,
            alumnosRegulares = porCategoria[ColorAsistencia.REGULAR]?.size ?: 0,
            alumnosRiesgo = porCategoria[ColorAsistencia.MALO]?.size ?: 0
        )
    }

    /**
     * Obtiene el rango de fechas como string legible
     */
    fun getRangoFechasTexto(fechaInicio: String, fechaFin: String): String {
        val inicio = DateUtils.formatDateForDisplay(fechaInicio)
        val fin = DateUtils.formatDateForDisplay(fechaFin)
        return "$inicio - $fin"
    }
}

enum class ColorAsistencia {
    EXCELENTE,  // >= 90% (Verde)
    BUENO,      // 70-89% (Azul)
    REGULAR,    // 50-69% (Amarillo)
    MALO        // < 50% (Rojo)
}