package com.example.insertcode.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "eventos": historial de alertas, accesos y eventos operativos del turno
@Entity(tableName = "eventos")
data class EventoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val ubicacion: String,
    val detalle: String,
    val categoria: String,   // ALERTA | EVENTO | ACCESO
    val severidad: String,   // CRITICA | MEDIA | NORMAL
    val estado: String,      // PENDIENTE | CONFIRMADA | RESUELTO | FALSA_ALARMA | AUTORIZADO | ESCALADA
    val camara: String,      // CAM-04
    val confianzaIa: Int = 0,
    val duracionSeg: Int = 0,
    val fecha: Long
)
