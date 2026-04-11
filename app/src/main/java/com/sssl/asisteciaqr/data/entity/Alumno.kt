package com.sssl.asisteciaqr.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "alumnos",
    foreignKeys = [
        ForeignKey(
            entity = Grupo::class,
            parentColumns = ["id"],
            childColumns = ["grupoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("grupoId"), Index("qrToken")]
)
data class Alumno(
    @PrimaryKey
    val matricula: String,
    val nombre: String,
    val grupoId: Long,
    val qrToken: String = UUID.randomUUID().toString(), // UUID único para QR
    val fechaRegistro: Long = System.currentTimeMillis()
)