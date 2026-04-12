package com.sssl.asisteciaqr.model

import com.sssl.asisteciaqr.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

sealed class PeriodoFiltro(val nombre: String) {
    object Hoy : PeriodoFiltro("Hoy")
    object Semana : PeriodoFiltro("Semana")
    object Mes : PeriodoFiltro("Mes")
    object Global : PeriodoFiltro("Global")

    /**
     * Calcula el rango de fechas según el tipo de filtro
     */
    fun getRangoFechas(): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val fechaFin = dateFormat.format(calendar.time)

        val fechaInicio = when (this) {
            is Hoy -> {
                fechaFin // Mismo día
            }
            is Semana -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                dateFormat.format(calendar.time)
            }
            is Mes -> {
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                dateFormat.format(calendar.time)
            }
            is Global -> {
                "2000-01-01" // Fecha muy antigua para obtener todos los registros
            }
        }

        return Pair(fechaInicio, fechaFin)
    }

    companion object {
        fun values() = listOf(Hoy, Semana, Mes, Global)
    }
}