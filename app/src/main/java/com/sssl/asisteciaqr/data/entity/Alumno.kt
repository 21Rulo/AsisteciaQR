package com.sssl.asisteciaqr.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    indices = [Index("grupoId")]
)
data class Alumno(
    @PrimaryKey
    val matricula: String,
    val nombre: String,
    val grupoId: Long,
    val fechaRegistro: Long = System.currentTimeMillis()
)