package com.sssl.asisteciaqr.utils

import android.content.Context
import android.net.Uri
import com.sssl.asisteciaqr.data.entity.Alumno
import java.io.BufferedReader
import java.io.InputStreamReader

object CSVParser {

    data class CSVResult(
        val alumnos: List<Alumno>,
        val errores: List<String>
    )

    /**
     * Lee un archivo CSV y lo convierte en una lista de Alumnos
     * Formato esperado: nombre,matricula
     *
     * @param context Contexto de la aplicación
     * @param uri Uri del archivo CSV seleccionado
     * @param grupoId ID del grupo al que pertenecerán los alumnos
     * @return CSVResult con la lista de alumnos y posibles errores
     */
    fun parseCSV(context: Context, uri: Uri, grupoId: Long): CSVResult {
        val alumnos = mutableListOf<Alumno>()
        val errores = mutableListOf<String>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var lineNumber = 0
                    var headerSkipped = false

                    reader.forEachLine { line ->
                        lineNumber++

                        // Saltar la primera línea si parece ser un encabezado
                        if (!headerSkipped && (line.contains("nombre", ignoreCase = true) ||
                                    line.contains("matrícula", ignoreCase = true))) {
                            headerSkipped = true
                            return@forEachLine
                        }

                        // Ignorar líneas vacías
                        if (line.trim().isEmpty()) return@forEachLine

                        try {
                            val parts = line.split(",").map { it.trim() }

                            if (parts.size < 2) {
                                errores.add("Línea $lineNumber: formato inválido (debe tener nombre,matricula)")
                                return@forEachLine
                            }

                            val nombre = parts[0]
                            val matricula = parts[1]

                            if (nombre.isBlank() || matricula.isBlank()) {
                                errores.add("Línea $lineNumber: nombre o matrícula vacíos")
                                return@forEachLine
                            }

                            // Crear alumno con UUID automático
                            val alumno = Alumno(
                                matricula = matricula,
                                nombre = nombre,
                                grupoId = grupoId
                            )

                            alumnos.add(alumno)

                        } catch (e: Exception) {
                            errores.add("Línea $lineNumber: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            errores.add("Error al leer el archivo: ${e.message}")
        }

        return CSVResult(alumnos, errores)
    }

    /**
     * Valida si un archivo es un CSV válido
     */
    fun isValidCSV(context: Context, uri: Uri): Boolean {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            mimeType == "text/csv" ||
                    mimeType == "text/comma-separated-values" ||
                    uri.path?.endsWith(".csv") == true
        } catch (e: Exception) {
            false
        }
    }
}