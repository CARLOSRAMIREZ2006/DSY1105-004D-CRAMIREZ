package com.example.insertcode.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.insertcode.data.EventoEntity
import com.example.insertcode.data.GuardianPreferences
import com.example.insertcode.data.GuardianRepository
import com.example.insertcode.data.IncidenciaEntity
import com.example.insertcode.model.AjustesGuardian
import com.example.insertcode.model.Camara
import com.example.insertcode.model.CamarasDemo
import com.example.insertcode.model.CategoriaEvento
import com.example.insertcode.model.CuentasPrueba
import com.example.insertcode.model.DiaFiltro
import com.example.insertcode.model.ErroresLogin
import com.example.insertcode.model.EstadoCamara
import com.example.insertcode.model.EstadoEvento
import com.example.insertcode.model.FiltroEventos
import com.example.insertcode.model.ResumenTurno
import com.example.insertcode.model.RolUsuario
import com.example.insertcode.model.Sesion
import com.example.insertcode.model.SeveridadEvento
import com.example.insertcode.util.inicioDelDia
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ViewModel de toda la parte "Guardián": sesión, cámaras, eventos, alertas,
// incidencias y ajustes. Se comparte entre pantallas (ligado a la Activity).
class GuardianViewModel(
    private val repo: GuardianRepository,
    private val prefs: GuardianPreferences
) : ViewModel() {

    // ---------- Sesión ----------
    // null = todavía leyendo DataStore; true = hay sesión recordada
    val sesionInicial: StateFlow<Boolean?> = flow<Boolean?> { emit(prefs.sesionRecordada()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val sesion: StateFlow<Sesion> = prefs.sesionFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Sesion())

    // Rol de la sesión activa (null si no es una cuenta de prueba, por ejemplo con Google)
    val rol: StateFlow<RolUsuario?> = sesion
        .map { CuentasPrueba.rolDe(it.correo) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val ajustes: StateFlow<AjustesGuardian> = prefs.ajustesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AjustesGuardian())

    private val _erroresLogin = MutableStateFlow(ErroresLogin())
    val erroresLogin: StateFlow<ErroresLogin> = _erroresLogin.asStateFlow()

    private val _cargandoLogin = MutableStateFlow(false)
    val cargandoLogin: StateFlow<Boolean> = _cargandoLogin.asStateFlow()

    // ---------- Cámaras ----------
    private val _camaras = MutableStateFlow(CamarasDemo.lista)
    val camaras: StateFlow<List<Camara>> = _camaras.asStateFlow()

    // id de la cámara que se está reconectando (null = ninguna)
    private val _reconectandoId = MutableStateFlow<Int?>(null)
    val reconectandoId: StateFlow<Int?> = _reconectandoId.asStateFlow()

    // ---------- Eventos / alertas (ROOM) ----------
    val eventos: StateFlow<List<EventoEntity>> = repo.eventos
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val alertasPendientes: StateFlow<Int> = eventos
        .map { lista -> lista.count { it.estado == EstadoEvento.PENDIENTE } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val resumenTurno: StateFlow<ResumenTurno> = eventos
        .map { lista ->
            val hoy = inicioDelDia(0)
            val deHoy = lista.filter { it.fecha >= hoy }
            ResumenTurno(
                eventosHoy = deHoy.size,
                criticasHoy = deHoy.count { it.severidad == SeveridadEvento.CRITICA },
                pendientes = lista.count { it.estado == EstadoEvento.PENDIENTE },
                alertasResueltasHoy = deHoy.count {
                    it.categoria == CategoriaEvento.ALERTA && it.estado != EstadoEvento.PENDIENTE
                }
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ResumenTurno())

    // ---------- Filtros del historial ----------
    private val _filtro = MutableStateFlow(FiltroEventos())
    val filtro: StateFlow<FiltroEventos> = _filtro.asStateFlow()

    val eventosFiltrados: StateFlow<List<EventoEntity>> = combine(eventos, _filtro) { lista, f ->
        aplicarFiltro(lista, f)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---------- Incidencias ----------
    val incidencias: StateFlow<List<IncidenciaEntity>> = repo.incidencias
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch { repo.sembrarSiVacio() }
    }

    // =====================================================================
    //  LOGIN
    // =====================================================================
    private fun validarLogin(correo: String, clave: String): Boolean {
        val c = correo.trim()
        val errores = ErroresLogin(
            correo = if (!c.contains("@") || !c.contains(".")) "Ingresa un correo válido" else null,
            clave = if (clave.length < 6) "La contraseña debe tener al menos 6 caracteres" else null
        )
        _erroresLogin.value = errores
        return errores.correo == null && errores.clave == null
    }

    fun limpiarErroresLogin() {
        _erroresLogin.value = ErroresLogin()
    }

    // Login local/simulado (la app no tiene backend): valida el formato y guarda la sesión
    fun iniciarSesion(correo: String, clave: String, recordar: Boolean, onExito: () -> Unit) {
        if (!validarLogin(correo, clave)) return
        // Entran las cuentas de prueba del enunciado y los usuarios registrados; el resto se rechaza
        val esCuentaPrueba = CuentasPrueba.buscar(correo, clave) != null
        if (!esCuentaPrueba && !prefs.verificarCredencial(correo, clave)) {
            _erroresLogin.value = ErroresLogin(general = "Correo o contraseña incorrectos")
            return
        }
        viewModelScope.launch {
            _cargandoLogin.value = true
            delay(700)
            prefs.guardarSesion(nombreDesdeCorreo(correo), correo.trim(), recordar)
            _cargandoLogin.value = false
            onExito()
        }
    }

    fun iniciarSesionGoogle(nombre: String, correo: String, onExito: () -> Unit) {
        viewModelScope.launch {
            prefs.guardarSesion(nombre.ifBlank { nombreDesdeCorreo(correo) }, correo, true)
            onExito()
        }
    }

    fun cerrarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            prefs.limpiarSesion()
            onListo()
        }
    }

    private fun nombreDesdeCorreo(correo: String): String =
        correo.trim().substringBefore("@").replaceFirstChar { it.uppercase() }

    // =====================================================================
    //  CÁMARAS
    // =====================================================================
    // Simula la reconexión de una cámara sin conexión (2 s) y deja constancia en el historial
    fun reconectarCamara(id: Int) {
        if (_reconectandoId.value != null) return
        viewModelScope.launch {
            _reconectandoId.value = id
            delay(2000)
            _camaras.update { lista ->
                lista.map { if (it.id == id) it.copy(estado = EstadoCamara.EN_LINEA) else it }
            }
            _reconectandoId.value = null
            _camaras.value.firstOrNull { it.id == id }?.let { cam ->
                repo.insertarEvento(
                    EventoEntity(
                        titulo = "Cámara reconectada",
                        ubicacion = cam.nombre,
                        detalle = "Reconexión manual desde la app",
                        categoria = CategoriaEvento.EVENTO,
                        severidad = SeveridadEvento.NORMAL,
                        estado = EstadoEvento.RESUELTO,
                        camara = cam.codigo,
                        fecha = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun registrarCaptura(camara: Camara) {
        viewModelScope.launch {
            repo.insertarEvento(
                EventoEntity(
                    titulo = "Captura manual",
                    ubicacion = camara.nombre,
                    detalle = "Foto tomada por el guardián",
                    categoria = CategoriaEvento.EVENTO,
                    severidad = SeveridadEvento.NORMAL,
                    estado = EstadoEvento.CONFIRMADA,
                    camara = camara.codigo,
                    fecha = System.currentTimeMillis()
                )
            )
        }
    }

    // =====================================================================
    //  EVENTOS Y ALERTAS
    // =====================================================================
    fun evento(id: Long): Flow<EventoEntity?> = repo.evento(id)

    fun confirmarAlerta(id: Long) {
        viewModelScope.launch { repo.actualizarEstado(id, EstadoEvento.CONFIRMADA) }
    }

    fun marcarFalsaAlarma(id: Long) {
        viewModelScope.launch { repo.actualizarEstado(id, EstadoEvento.FALSA_ALARMA) }
    }

    // Se dispara cuando se acaba el tiempo de respuesta (2 min)
    fun escalarAlerta(id: Long) {
        viewModelScope.launch { repo.actualizarEstado(id, EstadoEvento.ESCALADA) }
    }

    fun onTabChange(tab: Int) = _filtro.update { it.copy(tab = tab) }
    fun onBusquedaEventosChange(q: String) = _filtro.update { it.copy(busqueda = q) }
    fun onDiaChange(dia: DiaFiltro) = _filtro.update { it.copy(dia = dia) }
    fun toggleCamara01() = _filtro.update { it.copy(camara01 = !it.camara01) }
    fun togglePerimetro() = _filtro.update { it.copy(perimetro = !it.perimetro) }
    fun toggleCriticas() = _filtro.update { it.copy(criticas = !it.criticas) }

    private fun aplicarFiltro(lista: List<EventoEntity>, f: FiltroEventos): List<EventoEntity> {
        val hoy0 = inicioDelDia(0)
        val ayer0 = inicioDelDia(-1)
        val q = f.busqueda.trim()
        return lista.filter { e ->
            val okDia = when (f.dia) {
                DiaFiltro.HOY -> e.fecha >= hoy0
                DiaFiltro.AYER -> e.fecha >= ayer0 && e.fecha < hoy0
            }
            val okTab = when (f.tab) {
                1 -> e.categoria == CategoriaEvento.ALERTA
                2 -> e.categoria == CategoriaEvento.EVENTO
                3 -> e.categoria == CategoriaEvento.ACCESO
                else -> true
            }
            val okBusqueda = q.isEmpty() || listOf(e.titulo, e.ubicacion, e.detalle, e.camara, e.categoria)
                .any { it.contains(q, ignoreCase = true) }
            val okCam = !f.camara01 || e.camara == "CAM-01"
            val okPerimetro = !f.perimetro || e.ubicacion.contains("Perímetro", ignoreCase = true)
            val okCriticas = !f.criticas || e.severidad == SeveridadEvento.CRITICA
            okDia && okTab && okBusqueda && okCam && okPerimetro && okCriticas
        }
    }

    // =====================================================================
    //  INCIDENCIAS
    // =====================================================================
    fun guardarIncidencia(
        tipo: String,
        descripcion: String,
        foto: Uri?,
        eventoId: Long?,
        onListo: () -> Unit
    ) {
        viewModelScope.launch {
            repo.registrarIncidencia(tipo, descripcion.trim(), foto, eventoId)
            onListo()
        }
    }

    // =====================================================================
    //  PERFIL / AJUSTES
    // =====================================================================
    fun actualizarFotoPerfil(uri: Uri) {
        viewModelScope.launch {
            val ruta = repo.guardarFotoLocal(uri) ?: uri.toString()
            prefs.guardarFoto(ruta)
        }
    }

    fun setDosPasos(valor: Boolean) { viewModelScope.launch { prefs.setDosPasos(valor) } }
    fun setPush(valor: Boolean) { viewModelScope.launch { prefs.setPush(valor) } }
    fun setAlertasCriticas(valor: Boolean) { viewModelScope.launch { prefs.setAlertasCriticas(valor) } }
    fun setModoOscuro(valor: Boolean) { viewModelScope.launch { prefs.setModoOscuro(valor) } }
}
