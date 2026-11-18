package com.example.insertcode.data

import android.content.Context
import android.net.Uri
import com.example.insertcode.model.CategoriaEvento
import com.example.insertcode.model.EstadoEvento
import com.example.insertcode.model.SeveridadEvento
import com.example.insertcode.util.inicioDelDia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

// Capa intermedia entre GuardianViewModel y ROOM
class GuardianRepository(
    private val eventoDao: EventoDao,
    private val incidenciaDao: IncidenciaDao,
    private val context: Context
) {
    val eventos: Flow<List<EventoEntity>> = eventoDao.getEventosFlow()
    val incidencias: Flow<List<IncidenciaEntity>> = incidenciaDao.getIncidenciasFlow()

    fun evento(id: Long): Flow<EventoEntity?> = eventoDao.getPorId(id)

    suspend fun insertarEvento(evento: EventoEntity): Long = eventoDao.insertar(evento)

    suspend fun actualizarEstado(id: Long, estado: String) = eventoDao.actualizarEstado(id, estado)

    // Registra la incidencia (copiando la foto al almacenamiento interno) y deja
    // constancia en el historial de eventos.
    suspend fun registrarIncidencia(tipo: String, descripcion: String, foto: Uri?, eventoId: Long?) {
        val fotoPath = foto?.let { guardarFotoLocal(it) ?: it.toString() }
        incidenciaDao.insertar(
            IncidenciaEntity(
                tipo = tipo,
                descripcion = descripcion,
                fotoPath = fotoPath,
                eventoId = eventoId,
                fecha = System.currentTimeMillis()
            )
        )
        eventoDao.insertar(
            EventoEntity(
                titulo = "Incidencia reportada",
                ubicacion = tipo,
                detalle = descripcion.take(60),
                categoria = CategoriaEvento.EVENTO,
                severidad = SeveridadEvento.NORMAL,
                estado = EstadoEvento.CONFIRMADA,
                camara = "—",
                fecha = System.currentTimeMillis()
            )
        )
    }

    // Copia la imagen (cámara o galería) a /files/fotos para tenerla offline
    suspend fun guardarFotoLocal(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "fotos").apply { mkdirs() }
            val destino = File(dir, "foto_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: return@runCatching null
            destino.absolutePath
        }.getOrNull()
    }

    // Primera ejecución: carga eventos de ejemplo para que el historial no parta vacío
    suspend fun sembrarSiVacio() {
        if (eventoDao.contar() > 0) return
        val ahora = System.currentTimeMillis()
        fun haceMin(m: Int) = ahora - m * 60_000L
        val medianoche = inicioDelDia(0)

        eventoDao.insertarTodos(
            listOf(
                EventoEntity(titulo = "Movimiento no autorizado", ubicacion = "Perímetro Norte",
                    detalle = "Zona C · Sensor PIR-04", categoria = CategoriaEvento.ALERTA,
                    severidad = SeveridadEvento.CRITICA, estado = EstadoEvento.PENDIENTE,
                    camara = "CAM-04", confianzaIa = 92, duracionSeg = 12, fecha = haceMin(2)),
                EventoEntity(titulo = "Acceso autorizado", ubicacion = "Entrada Principal",
                    detalle = "Credencial #4582 · A. Rivera", categoria = CategoriaEvento.ACCESO,
                    severidad = SeveridadEvento.NORMAL, estado = EstadoEvento.AUTORIZADO,
                    camara = "CAM-01", fecha = haceMin(22)),
                EventoEntity(titulo = "Cámara desconectada", ubicacion = "Bodega A",
                    detalle = "Offline 3m 12s · Auto-reconexión", categoria = CategoriaEvento.ALERTA,
                    severidad = SeveridadEvento.MEDIA, estado = EstadoEvento.RESUELTO,
                    camara = "CAM-07", fecha = haceMin(37)),
                EventoEntity(titulo = "Puerta abierta fuera de horario", ubicacion = "Almacén Central",
                    detalle = "Puerta D-02 · 4m abierta", categoria = CategoriaEvento.ALERTA,
                    severidad = SeveridadEvento.CRITICA, estado = EstadoEvento.PENDIENTE,
                    camara = "CAM-05", confianzaIa = 88, duracionSeg = 240, fecha = haceMin(55)),
                EventoEntity(titulo = "Ronda completada", ubicacion = "Perímetro Sur",
                    detalle = "Guardián · Ruta 2 · 18 checkpoints", categoria = CategoriaEvento.EVENTO,
                    severidad = SeveridadEvento.NORMAL, estado = EstadoEvento.CONFIRMADA,
                    camara = "CAM-08", fecha = haceMin(75)),
                EventoEntity(titulo = "Vehículo no identificado", ubicacion = "Estacionamiento",
                    detalle = "Placa no registrada · 3m detenido", categoria = CategoriaEvento.ALERTA,
                    severidad = SeveridadEvento.CRITICA, estado = EstadoEvento.PENDIENTE,
                    camara = "CAM-03", confianzaIa = 81, duracionSeg = 180, fecha = haceMin(95)),
                EventoEntity(titulo = "Salida autorizada", ubicacion = "Portón Vehicular",
                    detalle = "Placa JKL-892 · Logística", categoria = CategoriaEvento.ACCESO,
                    severidad = SeveridadEvento.NORMAL, estado = EstadoEvento.AUTORIZADO,
                    camara = "CAM-02", fecha = haceMin(115)),
                EventoEntity(titulo = "Batería baja - Sensor", ubicacion = "Perímetro Este",
                    detalle = "SEN-11 · 12% restante", categoria = CategoriaEvento.ALERTA,
                    severidad = SeveridadEvento.MEDIA, estado = EstadoEvento.FALSA_ALARMA,
                    camara = "CAM-06", fecha = haceMin(130)),
                // Turno de ayer
                EventoEntity(titulo = "Ronda completada", ubicacion = "Perímetro Norte",
                    detalle = "Guardián · Ruta 1 · 18 checkpoints", categoria = CategoriaEvento.EVENTO,
                    severidad = SeveridadEvento.NORMAL, estado = EstadoEvento.CONFIRMADA,
                    camara = "CAM-04", fecha = medianoche - 2 * 3_600_000L),
                EventoEntity(titulo = "Movimiento no autorizado", ubicacion = "Perímetro Norte",
                    detalle = "Zona A · Sensor PIR-02", categoria = CategoriaEvento.ALERTA,
                    severidad = SeveridadEvento.CRITICA, estado = EstadoEvento.CONFIRMADA,
                    camara = "CAM-04", confianzaIa = 90, duracionSeg = 9, fecha = medianoche - 3 * 3_600_000L),
                EventoEntity(titulo = "Acceso autorizado", ubicacion = "Entrada Principal",
                    detalle = "Credencial #4120 · M. Soto", categoria = CategoriaEvento.ACCESO,
                    severidad = SeveridadEvento.NORMAL, estado = EstadoEvento.AUTORIZADO,
                    camara = "CAM-01", fecha = medianoche - 4 * 3_600_000L)
            )
        )
    }
}
