package com.sssl.asisteciaqr.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grupos")
data class Grupo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombreGrupo: String,
    val materia: String,
    val fechaCreacion: Long = System.currentTimeMillis()
)

