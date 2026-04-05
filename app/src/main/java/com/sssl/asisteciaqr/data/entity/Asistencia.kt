package com.sssl.asisteciaqr.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asistencias",
    foreignKeys = [
        ForeignKey(
            entity = Alumno::class,
            parentColumns = ["matricula"],
            childColumns = ["matriculaAlumno"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("matriculaAlumno"), Index("fecha")]
)
data class Asistencia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val matriculaAlumno: String,
    val grupoId: Long,
    val fecha: String, // Formato: "yyyy-MM-dd"
    val hora: String,  // Formato: "HH:mm:ss"
    val timestamp: Long = System.currentTimeMillis()
)