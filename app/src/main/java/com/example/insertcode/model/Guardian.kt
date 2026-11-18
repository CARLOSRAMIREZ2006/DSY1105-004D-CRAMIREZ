package com.example.insertcode.model

import com.example.insertcode.data.EventoEntity

// ---------- Cámaras (datos de demostración, 12 asignadas) ----------
enum class EstadoCamara { EN_LINEA, SIN_CONEXION }

data class Camara(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val zona: String,
    val estado: EstadoCamara
)

object CamarasDemo {
    val lista: List<Camara> = listOf(
        Camara(1, "CAM-01", "Entrada Principal", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(4, "CAM-04", "Perímetro Norte", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(7, "CAM-07", "Bodega A", "Zona Sur", EstadoCamara.SIN_CONEXION),
        Camara(3, "CAM-03", "Estacionamiento", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(2, "CAM-02", "Portón Vehicular", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(5, "CAM-05", "Almacén Central", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(6, "CAM-06", "Perímetro Este", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(8, "CAM-08", "Perímetro Sur", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(9, "CAM-09", "Pasillo Técnico", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(10, "CAM-10", "Sala de Control", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(11, "CAM-11", "Acceso Peatonal", "Zona Sur", EstadoCamara.EN_LINEA),
        Camara(12, "CAM-12", "Andén de Carga", "Zona Sur", EstadoCamara.EN_LINEA)
    )
}

// ---------- Constantes de eventos (se guardan como String en ROOM) ----------
object CategoriaEvento {
    const val ALERTA = "ALERTA"
    const val EVENTO = "EVENTO"
    const val ACCESO = "ACCESO"
}

object SeveridadEvento {
    const val CRITICA = "CRITICA"
    const val MEDIA = "MEDIA"
    const val NORMAL = "NORMAL"
}

object EstadoEvento {
    const val PENDIENTE = "PENDIENTE"
    const val CONFIRMADA = "CONFIRMADA"
    const val RESUELTO = "RESUELTO"
    const val FALSA_ALARMA = "FALSA_ALARMA"
    const val AUTORIZADO = "AUTORIZADO"
    const val ESCALADA = "ESCALADA"
}

fun String.textoEstado(): String = when (this) {
    EstadoEvento.PENDIENTE -> "Pendiente"
    EstadoEvento.CONFIRMADA -> "Confirmada"
    EstadoEvento.RESUELTO -> "Resuelto"
    EstadoEvento.FALSA_ALARMA -> "Falsa alarma"
    EstadoEvento.AUTORIZADO -> "Autorizado"
    EstadoEvento.ESCALADA -> "Escalada"
    else -> this
}

// Tipo legible del evento (se deduce del título)
fun EventoEntity.tipoLegible(): String = when {
    titulo.contains("Movimiento", ignoreCase = true) -> "Intrusión perimetral"
    titulo.contains("Puerta", ignoreCase = true) -> "Acceso fuera de horario"
    titulo.contains("Cámara", ignoreCase = true) -> "Falla de cámara"
    titulo.contains("Batería", ignoreCase = true) -> "Falla de sensor"
    titulo.contains("Vehículo", ignoreCase = true) -> "Vehículo sospechoso"
    categoria == CategoriaEvento.ACCESO -> "Control de acceso"
    categoria == CategoriaEvento.ALERTA -> "Alerta de seguridad"
    else -> "Evento operativo"
}

// ---------- Estado de sesión / ajustes ----------
data class Sesion(
    val nombre: String = "Guardián",
    val correo: String = "",
    val fotoPath: String? = null
)

data class AjustesGuardian(
    val dosPasos: Boolean = true,
    val push: Boolean = true,
    val alertasCriticas: Boolean = true,
    val modoOscuro: Boolean? = null   // null = seguir el tema del sistema
)

// Errores mostrados en el login. 'general' aparece cuando el correo o la clave
// no coinciden con ninguna cuenta de prueba.
data class ErroresLogin(
    val correo: String? = null,
    val clave: String? = null,
    val general: String? = null
)

// ---------- Filtros del historial ----------
enum class DiaFiltro { HOY, AYER }

data class FiltroEventos(
    val tab: Int = 0,                 // 0 Todos, 1 Alertas, 2 Eventos, 3 Accesos
    val busqueda: String = "",
    val dia: DiaFiltro = DiaFiltro.HOY,
    val camara01: Boolean = false,
    val perimetro: Boolean = false,
    val criticas: Boolean = false
)

data class ResumenTurno(
    val eventosHoy: Int = 0,
    val criticasHoy: Int = 0,
    val pendientes: Int = 0,
    val alertasResueltasHoy: Int = 0
)
