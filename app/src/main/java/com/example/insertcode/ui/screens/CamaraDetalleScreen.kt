package com.example.insertcode.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.model.EstadoCamara
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.components.BarraInferior
import com.example.insertcode.ui.components.ChipEtiqueta
import com.example.insertcode.ui.theme.ColorAdvertencia
import com.example.insertcode.ui.theme.ColorCritico
import com.example.insertcode.ui.theme.ColorExito
import com.example.insertcode.ui.utils.rememberAhora
import com.example.insertcode.util.formatoDuracion
import com.example.insertcode.util.formatoFechaHoraSeg
import com.example.insertcode.viewmodels.GuardianViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val VerdeCamara = Color(0xFF69F0AE)

// Vista en vivo de una cámara (la imagen es una simulación dibujada con Canvas)
@Composable
fun CamaraDetalleScreen(
    navController: NavController,
    viewModel: GuardianViewModel,
    camaraId: Int
) {
    val camaras by viewModel.camaras.collectAsState()
    val reconectando by viewModel.reconectandoId.collectAsState()
    val pendientes by viewModel.alertasPendientes.collectAsState()
    val camara = camaras.firstOrNull { it.id == camaraId }

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var grabando by rememberSaveable { mutableStateOf(true) }
    var segundosRec by rememberSaveable { mutableIntStateOf(0) }
    var microfono by rememberSaveable { mutableStateOf(false) }
    var luz by rememberSaveable { mutableStateOf(false) }
    var pantallaCompleta by rememberSaveable { mutableStateOf(false) }

    val enLinea = camara?.estado == EstadoCamara.EN_LINEA

    // Contador de grabación
    LaunchedEffect(grabando, enLinea) {
        while (grabando && enLinea) {
            delay(1000)
            segundosRec++
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { BarraInferior(Rutas.CAMARAS, navController, pendientes) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------- Cabecera ----------
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(camara?.nombre ?: "Cámara no encontrada", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (enLinea) "● EN VIVO · HD" else "● SIN CONEXIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = if (enLinea) ColorCritico else ColorAdvertencia
                    )
                }
                if (enLinea && grabando) ChipEtiqueta("● REC", ColorCritico)
            }

            if (camara == null) {
                Text("Esta cámara ya no está asignada a tu turno.")
                return@Column
            }

            // ---------- Imagen ----------
            val ahora by rememberAhora(1000L)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (pantallaCompleta) Modifier.height(420.dp) else Modifier.aspectRatio(16f / 10f))
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (enLinea && luz) Color(0xFF14452A) else Color(0xFF07140D))
            ) {
                if (enLinea) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Líneas de escaneo
                        var y = 0f
                        while (y < size.height) {
                            drawLine(Color.Black.copy(alpha = 0.22f), Offset(0f, y), Offset(size.width, y), strokeWidth = 2f)
                            y += 7f
                        }
                        // Silueta de la puerta
                        drawRect(
                            color = VerdeCamara.copy(alpha = 0.10f),
                            topLeft = Offset(size.width * 0.36f, size.height * 0.28f),
                            size = Size(size.width * 0.28f, size.height * 0.62f)
                        )
                        // Esquinas del encuadre
                        val l = 28f
                        val m = 20f
                        val c = VerdeCamara.copy(alpha = 0.6f)
                        drawLine(c, Offset(m, m), Offset(m + l, m), 3f)
                        drawLine(c, Offset(m, m), Offset(m, m + l), 3f)
                        drawLine(c, Offset(size.width - m, m), Offset(size.width - m - l, m), 3f)
                        drawLine(c, Offset(size.width - m, m), Offset(size.width - m, m + l), 3f)
                        drawLine(c, Offset(m, size.height - m), Offset(m + l, size.height - m), 3f)
                        drawLine(c, Offset(m, size.height - m), Offset(m, size.height - m - l), 3f)
                        drawLine(c, Offset(size.width - m, size.height - m), Offset(size.width - m - l, size.height - m), 3f)
                        drawLine(c, Offset(size.width - m, size.height - m), Offset(size.width - m, size.height - m - l), 3f)
                    }
                    Column(modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
                        Text(ahora.formatoFechaHoraSeg(), color = VerdeCamara, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                        Text("INSERCODE ${camara.codigo}", color = VerdeCamara, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = if (luz) "LUZ · ON" else "IR · AUTO",
                        color = VerdeCamara,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(16.dp).align(Alignment.TopEnd)
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (grabando) "● REC  ${formatoDuracion(segundosRec)}" else "■ DETENIDO",
                            color = if (grabando) ColorCritico else Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace
                        )
                        Text("H.265 · 25FPS", color = Color.LightGray, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    }
                } else {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VideocamOff, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Text("Sin conexión con ${camara.codigo}", color = Color.White)
                        Button(
                            onClick = { viewModel.reconectarCamara(camara.id) },
                            enabled = reconectando == null
                        ) {
                            if (reconectando == camara.id) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Reintentar conexión")
                            }
                        }
                    }
                }
            }

            // ---------- Controles ----------
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                ControlCamara(Icons.Default.CameraAlt, "FOTO", habilitado = enLinea, onClick = {
                    viewModel.registrarCaptura(camara)
                    scope.launch { snackbar.showSnackbar("Captura registrada en el historial") }
                })
                ControlCamara(
                    icono = if (grabando) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                    etiqueta = if (grabando) "PARAR" else "GRABAR",
                    peligro = grabando,
                    habilitado = enLinea,
                    onClick = {
                        grabando = !grabando
                        if (grabando) segundosRec = 0
                    }
                )
                ControlCamara(
                    icono = if (microfono) Icons.Default.Mic else Icons.Default.MicOff,
                    etiqueta = "HABLAR",
                    activo = microfono,
                    habilitado = enLinea,
                    onClick = { microfono = !microfono }
                )
                ControlCamara(
                    icono = if (luz) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                    etiqueta = "LUZ",
                    activo = luz,
                    habilitado = enLinea,
                    onClick = { luz = !luz }
                )
                ControlCamara(
                    icono = if (pantallaCompleta) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    etiqueta = "FULL",
                    activo = pantallaCompleta,
                    onClick = { pantallaCompleta = !pantallaCompleta }
                )
            }

            // ---------- Estado del dispositivo ----------
            if (!pantallaCompleta) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("ESTADO DEL DISPOSITIVO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DatoDispositivo(
                                icono = Icons.Default.Videocam,
                                titulo = "ESTADO",
                                valor = if (enLinea) "En línea" else "Sin conexión",
                                color = if (enLinea) ColorExito else ColorAdvertencia,
                                modifier = Modifier.weight(1f)
                            )
                            DatoDispositivo(
                                icono = Icons.Default.Wifi,
                                titulo = "SEÑAL",
                                valor = if (enLinea) "Excelente" else "—",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            DatoDispositivo(
                                icono = Icons.Default.Storage,
                                titulo = "GRABACIÓN",
                                valor = if (!enLinea) "—" else if (grabando) "Nube + Local" else "Detenida",
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlCamara(
    icono: ImageVector,
    etiqueta: String,
    onClick: () -> Unit,
    activo: Boolean = false,
    peligro: Boolean = false,
    habilitado: Boolean = true
) {
    val esquema = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FilledIconButton(
            onClick = onClick,
            enabled = habilitado,
            modifier = Modifier.size(if (peligro) 60.dp else 50.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = when {
                    peligro -> esquema.error
                    activo -> esquema.primary
                    else -> esquema.surfaceVariant
                },
                contentColor = when {
                    peligro -> esquema.onError
                    activo -> esquema.onPrimary
                    else -> esquema.onSurfaceVariant
                }
            )
        ) {
            Icon(icono, contentDescription = etiqueta)
        }
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun DatoDispositivo(
    icono: ImageVector,
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        }
        Text(titulo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}
