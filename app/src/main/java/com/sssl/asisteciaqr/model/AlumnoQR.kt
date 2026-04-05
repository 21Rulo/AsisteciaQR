package com.sssl.asisteciaqr.model

import kotlinx.serialization.Serializable

@Serializable
data class AlumnoQR(
    val matricula: String,
    val nombre: String
)