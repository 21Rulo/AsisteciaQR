package com.sssl.asisteciaqr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream

object QRGenerator {

    fun generateQRCode(content: String, size: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateAlumnoQRJson(matricula: String, nombre: String): String {
        return """{"matricula":"$matricula","nombre":"$nombre"}"""
    }

    /**
     * Genera y guarda el QR individual de un alumno como PNG
     *
     * @param context Contexto de la aplicación
     * @param qrToken UUID del alumno
     * @param nombreAlumno Nombre del alumno para el archivo
     * @param matricula Matrícula del alumno
     * @return File del PNG generado o null si falla
     */
    fun generarQRIndividual(
        context: Context,
        qrToken: String,
        nombreAlumno: String,
        matricula: String
    ): File? {
        return try {
            // Generar QR de 1024x1024 (alta calidad)
            val qrBitmap = generateQRCode(qrToken, 1024) ?: return null

            // Crear directorio
            val directory = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "AsistenciaQR/QRs_Individuales"
            )

            if (!directory.exists()) {
                directory.mkdirs()
            }

            // Crear nombre de archivo limpio
            val nombreLimpio = nombreAlumno
                .replace(Regex("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]"), "")
                .replace(" ", "_")

            val fileName = "QR_${nombreLimpio}_${matricula}.png"
            val file = File(directory, fileName)

            // Guardar como PNG
            FileOutputStream(file).use { out ->
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            return file

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}