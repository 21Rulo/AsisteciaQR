package com.sssl.asisteciaqr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.sssl.asisteciaqr.data.entity.Alumno
import java.io.File
import java.io.FileOutputStream

object PDFGenerator {

    private const val PAGE_WIDTH = 595 // A4 width in points (72 DPI)
    private const val PAGE_HEIGHT = 842 // A4 height in points
    private const val QR_SIZE = 180
    private const val MARGIN = 50

    /**
     * Genera un PDF con códigos QR de todos los alumnos
     * Layout: 4 QRs por página (2x2)
     *
     * @param context Contexto de la aplicación
     * @param alumnos Lista de alumnos del grupo
     * @param nombreGrupo Nombre del grupo para el archivo
     * @return File del PDF generado o null si falla
     */
    fun generarPDFconQRs(
        context: Context,
        alumnos: List<Alumno>,
        nombreGrupo: String
    ): File? {
        if (alumnos.isEmpty()) return null

        try {
            val document = PdfDocument()
            val paint = Paint().apply {
                textSize = 12f
                isAntiAlias = true
            }

            val qrsPerPage = 4
            val totalPages = (alumnos.size + qrsPerPage - 1) / qrsPerPage

            for (pageIndex in 0 until totalPages) {
                val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageIndex + 1).create()
                val page = document.startPage(pageInfo)
                val canvas = page.canvas

                // Calcular posiciones para 2x2 grid
                val startIndex = pageIndex * qrsPerPage
                val endIndex = minOf(startIndex + qrsPerPage, alumnos.size)
                val alumnosEnPagina = alumnos.subList(startIndex, endIndex)

                val positions = listOf(
                    Pair(MARGIN + 50, MARGIN + 100),                    // Top-Left
                    Pair(PAGE_WIDTH / 2 + 25, MARGIN + 100),            // Top-Right
                    Pair(MARGIN + 50, PAGE_HEIGHT / 2 + 50),            // Bottom-Left
                    Pair(PAGE_WIDTH / 2 + 25, PAGE_HEIGHT / 2 + 50)     // Bottom-Right
                )

                alumnosEnPagina.forEachIndexed { index, alumno ->
                    val (x, y) = positions[index]

                    // Generar QR con solo el token UUID
                    val qrBitmap = QRGenerator.generateQRCode(alumno.qrToken, QR_SIZE)

                    if (qrBitmap != null) {
                        // Dibujar QR
                        canvas.drawBitmap(qrBitmap, x.toFloat(), y.toFloat(), paint)

                        // Dibujar nombre del alumno debajo del QR
                        paint.textSize = 14f
                        paint.isFakeBoldText = true
                        val nombreLines = wrapText(alumno.nombre, 25)
                        nombreLines.forEachIndexed { lineIndex, line ->
                            canvas.drawText(
                                line,
                                x.toFloat() + 10,
                                y.toFloat() + QR_SIZE + 20 + (lineIndex * 18),
                                paint
                            )
                        }

                        // Dibujar matrícula
                        paint.textSize = 11f
                        paint.isFakeBoldText = false
                        canvas.drawText(
                            "Mat: ${alumno.matricula}",
                            x.toFloat() + 10,
                            y.toFloat() + QR_SIZE + 20 + (nombreLines.size * 18) + 15,
                            paint
                        )
                    }
                }

                // Pie de página
                paint.textSize = 10f
                paint.isFakeBoldText = false
                canvas.drawText(
                    "Grupo: $nombreGrupo - Página ${pageIndex + 1} de $totalPages",
                    MARGIN.toFloat(),
                    PAGE_HEIGHT - 30f,
                    paint
                )

                document.finishPage(page)
            }

            // Guardar el PDF
            val fileName = "QRs_${nombreGrupo.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
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
     * Divide un texto largo en múltiples líneas
     */
    private fun wrapText(text: String, maxLength: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            if ((currentLine + " " + word).length <= maxLength) {
                currentLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }
}