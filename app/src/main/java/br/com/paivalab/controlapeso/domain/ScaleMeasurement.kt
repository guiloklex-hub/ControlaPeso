package br.com.paivalab.controlapeso.domain

import java.time.LocalDateTime

data class ScaleMeasurement(
    val weightKg: Double?,
    val rawWeight: Int?,
    val impedanceOne: Int?,
    val impedanceTwo: Int?,
    val isStable: Boolean?,
    val timestamp: LocalDateTime?,
    val rawPayloadHex: String,
    val parserNotes: List<String>
)
