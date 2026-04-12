package com.sssl.asisteciaqr.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.sssl.asisteciaqr.data.entity.Asistencia
import java.io.File
import java.io.FileOutputStream

object ReportePDFGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val MARGIN = 40

    /**
     * Genera un PDF con el reporte completo del grupo
     */
    fun generarReporteGrupo(
        context: Context,
        nombreGrupo: String,
        materia: String,
        estadisticas: List<EstadisticasUtils.EstadisticasAlumno>,
        periodo: String,
        totalClases: Int
    ): File? {
        if (estadisticas.isEmpty()) return null

        try {
            val document = PdfDocument()
            val paint = Paint().apply {
                textSize = 12f
                isAntiAlias = true
            }

            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var yPosition = MARGIN.toFloat() + 20

            // Título
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("REPORTE DE ASISTENCIAS", MARGIN.toFloat(), yPosition, paint)
            yPosition += 30

            // Info del grupo
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Grupo: $nombreGrupo", MARGIN.toFloat(), yPosition, paint)
            yPosition += 18
            canvas.drawText("Materia: $materia", MARGIN.toFloat(), yPosition, paint)
            yPosition += 18
            canvas.drawText("Periodo: $periodo", MARGIN.toFloat(), yPosition, paint)
            yPosition += 18
            canvas.drawText("Total de clases: $totalClases", MARGIN.toFloat(), yPosition, paint)
            yPosition += 30

            // Estadísticas generales
            val estadisticasGrupo = EstadisticasUtils.calcularEstadisticasGrupo(estadisticas)
            paint.isFakeBoldText = true
            canvas.drawText("ESTADÍSTICAS GENERALES", MARGIN.toFloat(), yPosition, paint)
            yPosition += 20

            paint.isFakeBoldText = false
            canvas.drawText(
                "Promedio del grupo: ${EstadisticasUtils.formatearPorcentaje(estadisticasGrupo.promedioAsistencia)}",
                MARGIN.toFloat() + 10,
                yPosition,
                paint
            )
            yPosition += 18
            canvas.drawText(
                "Alumnos: ${estadisticasGrupo.totalAlumnos} | " +
                        "Excelentes: ${estadisticasGrupo.alumnosExcelentes} | " +
                        "Buenos: ${estadisticasGrupo.alumnosBuenos} | " +
                        "Regulares: ${estadisticasGrupo.alumnosRegulares} | " +
                        "En riesgo: ${estadisticasGrupo.alumnosRiesgo}",
                MARGIN.toFloat() + 10,
                yPosition,
                paint
            )
            yPosition += 30

            // Línea separadora
            canvas.drawLine(MARGIN.toFloat(), yPosition, (PAGE_WIDTH - MARGIN).toFloat(), yPosition, paint)
            yPosition += 20

            // Encabezados de tabla
            paint.isFakeBoldText = true
            canvas.drawText("Alumno", MARGIN.toFloat(), yPosition, paint)
            canvas.drawText("Matrícula", 220f, yPosition, paint)
            canvas.drawText("Asist.", 340f, yPosition, paint)
            canvas.drawText("Faltas", 400f, yPosition, paint)
            canvas.drawText("%", 470f, yPosition, paint)
            yPosition += 5

            // Línea bajo encabezados
            canvas.drawLine(MARGIN.toFloat(), yPosition, (PAGE_WIDTH - MARGIN).toFloat(), yPosition, paint)
            yPosition += 15

            paint.isFakeBoldText = false

            // Datos de alumnos (ordenados por porcentaje descendente)
            val alumnosOrdenados = estadisticas.sortedByDescending { it.porcentaje }

            for (estadistica in alumnosOrdenados) {
                // Verificar si hay espacio en la página
                if (yPosition > PAGE_HEIGHT - 100) {
                    // Terminar página actual
                    document.finishPage(page)

                    // Crear nueva página
                    val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
                    val newPage = document.startPage(newPageInfo)
                    val newCanvas = newPage.canvas
                    yPosition = MARGIN.toFloat() + 20

                    // Redibujar encabezados
                    paint.isFakeBoldText = true
                    newCanvas.drawText("Alumno", MARGIN.toFloat(), yPosition, paint)
                    newCanvas.drawText("Matrícula", 220f, yPosition, paint)
                    newCanvas.drawText("Asist.", 340f, yPosition, paint)
                    newCanvas.drawText("Faltas", 400f, yPosition, paint)
                    newCanvas.drawText("%", 470f, yPosition, paint)
                    yPosition += 5
                    newCanvas.drawLine(MARGIN.toFloat(), yPosition, (PAGE_WIDTH - MARGIN).toFloat(), yPosition, paint)
                    yPosition += 15
                    paint.isFakeBoldText = false
                }

                val nombreCorto = if (estadistica.alumno.nombre.length > 25) {
                    estadistica.alumno.nombre.substring(0, 22) + "..."
                } else {
                    estadistica.alumno.nombre
                }

                canvas.drawText(nombreCorto, MARGIN.toFloat(), yPosition, paint)
                canvas.drawText(estadistica.alumno.matricula, 220f, yPosition, paint)
                canvas.drawText("${estadistica.totalAsistencias}/${totalClases}", 340f, yPosition, paint)
                canvas.drawText("${estadistica.totalFaltas}", 410f, yPosition, paint)

                // Colorear el porcentaje según la categoría
                val porcentajeText = EstadisticasUtils.formatearPorcentaje(estadistica.porcentaje)
                canvas.drawText(porcentajeText, 470f, yPosition, paint)

                yPosition += 18
            }

            // Pie de página
            yPosition = PAGE_HEIGHT - 30f
            paint.textSize = 10f
            canvas.drawText(
                "Generado el ${DateUtils.getCurrentDateTime()}",
                MARGIN.toFloat(),
                yPosition,
                paint
            )

            document.finishPage(page)

            // Guardar PDF
            val fileName = "Reporte_${nombreGrupo.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val directory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "AsistenciaQR"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }

            document.close()
            return file

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Genera un PDF con el reporte individual de un alumno
     */
    fun generarReporteIndividual(
        context: Context,
        nombreAlumno: String,
        matricula: String,
        nombreGrupo: String,
        estadistica: EstadisticasUtils.EstadisticasAlumno,
        asistencias: List<Asistencia>,
        periodo: String
    ): File? {
        try {
            val document = PdfDocument()
            val paint = Paint().apply {
                textSize = 12f
                isAntiAlias = true
            }

            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var yPosition = MARGIN.toFloat() + 20

            // Título
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("REPORTE INDIVIDUAL DE ASISTENCIA", MARGIN.toFloat(), yPosition, paint)
            yPosition += 30

            // Línea decorativa
            canvas.drawLine(MARGIN.toFloat(), yPosition, (PAGE_WIDTH - MARGIN).toFloat(), yPosition, paint)
            yPosition += 20

            // Info del alumno
            paint.textSize = 14f
            canvas.drawText("Alumno: $nombreAlumno", MARGIN.toFloat(), yPosition, paint)
            yPosition += 22

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("Matrícula: $matricula", MARGIN.toFloat(), yPosition, paint)
            yPosition += 18
            canvas.drawText("Grupo: $nombreGrupo", MARGIN.toFloat(), yPosition, paint)
            yPosition += 18
            canvas.drawText("Periodo: $periodo", MARGIN.toFloat(), yPosition, paint)
            yPosition += 30

            // Resumen
            paint.isFakeBoldText = true
            paint.textSize = 14f
            canvas.drawText("RESUMEN", MARGIN.toFloat(), yPosition, paint)
            yPosition += 20

            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText(
                "• Asistencias: ${estadistica.totalAsistencias}/${estadistica.totalClases}",
                MARGIN.toFloat() + 10,
                yPosition,
                paint
            )
            yPosition += 18
            canvas.drawText(
                "• Faltas: ${estadistica.totalFaltas}",
                MARGIN.toFloat() + 10,
                yPosition,
                paint
            )
            yPosition += 18

            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText(
                "• Porcentaje de asistencia: ${EstadisticasUtils.formatearPorcentaje(estadistica.porcentaje)}",
                MARGIN.toFloat() + 10,
                yPosition,
                paint
            )
            yPosition += 30

            // Línea separadora
            paint.isFakeBoldText = false
            canvas.drawLine(MARGIN.toFloat(), yPosition, (PAGE_WIDTH - MARGIN).toFloat(), yPosition, paint)
            yPosition += 20

            // Detalle por fecha
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText("DETALLE POR FECHA", MARGIN.toFloat(), yPosition, paint)
            yPosition += 20

            paint.textSize = 11f
            paint.isFakeBoldText = false

            if (asistencias.isEmpty()) {
                canvas.drawText("No hay asistencias registradas en este periodo.", MARGIN.toFloat() + 10, yPosition, paint)
            } else {
                // Agrupar por fecha
                val asistenciasPorFecha = asistencias.sortedByDescending { it.fecha }

                for (asistencia in asistenciasPorFecha) {
                    // Verificar espacio
                    if (yPosition > PAGE_HEIGHT - 80) {
                        document.finishPage(page)
                        val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
                        val newPage = document.startPage(newPageInfo)
                        yPosition = MARGIN.toFloat() + 20
                    }

                    val fechaFormato = DateUtils.formatDateForDisplay(asistencia.fecha)
                    val horaFormato = DateUtils.formatTimeForDisplay(asistencia.hora)

                    canvas.drawText(
                        "✓  $fechaFormato  -  $horaFormato",
                        MARGIN.toFloat() + 10,
                        yPosition,
                        paint
                    )
                    yPosition += 16
                }
            }

            // Pie de página
            yPosition = PAGE_HEIGHT - 30f
            paint.textSize = 10f
            canvas.drawText(
                "Generado el ${DateUtils.getCurrentDateTime()}",
                MARGIN.toFloat(),
                yPosition,
                paint
            )

            document.finishPage(page)

            // Guardar PDF
            val fileName = "Reporte_${nombreAlumno.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val directory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "AsistenciaQR"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }

            document.close()
            return file

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}