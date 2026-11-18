package com.example.insertcode.navigation

// Rutas de la app (las originales "registro", "resumen" y "listado" se mantienen)
object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val RESUMEN = "resumen"
    const val LISTADO = "listado"

    const val INICIO = "inicio"
    const val CAMARAS = "camaras"
    const val EVENTOS = "eventos"
    const val ALERTAS = "alertas"
    const val PERFIL = "perfil"
    const val PEDIDOS = "pedidos"
    const val CATALOGO = "catalogo"
    const val RUTA_GPS = "ruta_gps"
    const val ROOM_SYNC = "room_sync"

    const val CAMARA_DETALLE = "camara/{camaraId}"
    fun camaraDetalle(id: Int) = "camara/$id"

    const val ALERTA_DETALLE = "alerta/{eventoId}"
    fun alertaDetalle(id: Long) = "alerta/$id"

    const val INCIDENCIA = "incidencia?eventoId={eventoId}"
    fun incidencia(eventoId: Long? = null) =
        if (eventoId == null) "incidencia" else "incidencia?eventoId=$eventoId"
}
