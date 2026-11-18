package com.example.insertcode.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Tabla "incidencias": reportes manuales del guardián (con foto opcional)
@Entity(tableName = "incidencias")
data class IncidenciaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val descripcion: String,
    val fotoPath: String?,
    val eventoId: Long?,     // alerta asociada (si se reportó desde una alerta)
    val fecha: Long
)
