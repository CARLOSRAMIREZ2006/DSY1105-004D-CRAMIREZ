package com.example.insertcode.util

import android.net.Uri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------- Formatos de fecha / hora ----------
fun Long.formatoHora(): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))

fun Long.formatoHoraSeg(): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(this))

fun Long.formatoFechaHoraSeg(): String =
    SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date(this))

fun Long.formatoFechaHora(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(this))

// "14:00 - 15:00": franja horaria en la que cae el timestamp
fun Long.rangoHora(): String {
    val c = Calendar.getInstance().apply { timeInMillis = this@rangoHora }
    val h = c.get(Calendar.HOUR_OF_DAY)
    return String.format(Locale.getDefault(), "%02d:00 - %02d:00", h, (h + 1) % 24)
}

// Timestamp (ms) de las 00:00 de hoy (offsetDias = -1 -> ayer)
fun inicioDelDia(offsetDias: Int = 0): Long {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    c.add(Calendar.DAY_OF_YEAR, offsetDias)
    return c.timeInMillis
}

fun hace(ms: Long, ahora: Long = System.currentTimeMillis()): String {
    val s = maxOf(0L, (ahora - ms) / 1000)
    return when {
        s < 60 -> "Hace $s s"
        s < 3600 -> "Hace ${s / 60} min"
        s < 86400 -> "Hace ${s / 3600} h"
        else -> "Hace ${s / 86400} d"
    }
}

fun formatoDuracion(segundos: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d:%02d", segundos / 3600, (segundos % 3600) / 60, segundos % 60)

fun formatoMinSeg(segundos: Int): String =
    String.format(Locale.getDefault(), "%02d:%02d", segundos / 60, segundos % 60)

// ---------- Progreso del turno (14:00 - 22:00) ----------
data class ProgresoTurno(val porcentaje: Int, val restante: String)

fun calcularProgresoTurno(
    horaInicio: Int = 14,
    horaFin: Int = 22,
    ahora: Long = System.currentTimeMillis()
): ProgresoTurno {
    val c = Calendar.getInstance().apply { timeInMillis = ahora }
    val minActual = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
    val ini = horaInicio * 60
    val total = horaFin * 60 - ini
    val transcurrido = (minActual - ini).coerceIn(0, total)
    val restante = total - transcurrido
    return ProgresoTurno(
        porcentaje = (transcurrido * 100) / total,
        restante = "${restante / 60}h ${restante % 60}m restantes"
    )
}

// ---------- Rutas de archivo -> Uri ----------
// fotoPath puede ser una ruta de archivo (/data/...) o una Uri (content://...)
fun String?.aUri(): Uri? = this?.let {
    if (it.startsWith("/")) Uri.fromFile(File(it)) else Uri.parse(it)
}
