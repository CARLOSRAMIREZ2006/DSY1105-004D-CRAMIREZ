package com.example.insertcode.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.data.EventoEntity
import com.example.insertcode.model.EstadoEvento
import com.example.insertcode.model.SeveridadEvento
import com.example.insertcode.model.textoEstado
import com.example.insertcode.model.tipoLegible
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.components.ChipEtiqueta
import com.example.insertcode.ui.components.colorEstado
import com.example.insertcode.ui.components.colorSeveridad
import com.example.insertcode.ui.theme.ColorAdvertencia
import com.example.insertcode.ui.theme.ColorCritico
import com.example.insertcode.ui.theme.ColorExito
import com.example.insertcode.ui.utils.rememberAhora
import com.example.insertcode.util.formatoFechaHoraSeg
import com.example.insertcode.util.formatoMinSeg
import com.example.insertcode.util.hace
import com.example.insertcode.viewmodels.GuardianViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Número de la Central de monitoreo (reemplázalo por el real)
private const val TELEFONO_CENTRAL = "+56220000000"
private const val TIEMPO_RESPUESTA_SEG = 120

// Detalle de una alerta/evento con las acciones del protocolo
@Composable
fun AlertaDetalleScreen(
    navController: NavController,
    viewModel: GuardianViewModel,
    eventoId: Long
) {
    val evento by remember(eventoId) { viewModel.evento(eventoId) }.collectAsState(initial = null)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var restante by rememberSaveable(eventoId) { mutableIntStateOf(TIEMPO_RESPUESTA_SEG) }
    val e = evento
    val pendiente = e?.estado == EstadoEvento.PENDIENTE

    // Cuenta regresiva: al llegar a 0 se escala automáticamente a Central
    LaunchedEffect(pendiente) {
        if (pendiente) {
            while (restante > 0) {
                delay(1000)
                restante--
            }
            viewModel.escalarAlerta(eventoId)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { innerPadding ->
        if (e == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Cargando evento…")
            }
            return@Scaffold
        }

        val color = colorSeveridad(e.severidad)
        val ahora by rememberAhora(1000L)

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
                Text(
                    text = if (e.severidad == SeveridadEvento.CRITICA) "Alerta Crítica" else "Detalle del evento",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                ChipEtiqueta(
                    texto = if (e.severidad == SeveridadEvento.CRITICA) "CRÍTICO" else e.estado.textoEstado().uppercase(),
                    color = color
                )
            }
            Text(
                text = hace(e.fecha, ahora),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )

            // ---------- Resumen del evento ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = color)
                        }
                        Text(e.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    FilaInfo(Icons.Default.Place, "${e.ubicacion} - ${e.camara}")
                    FilaInfo(Icons.Default.AccessTime, e.fecha.formatoFechaHoraSeg(), monoespaciado = true)
                    if (e.confianzaIa > 0) {
                        FilaInfo(Icons.Default.RemoveRedEye, "Confianza IA: ${e.confianzaIa}% detección")
                        LinearProgressIndicator(
                            progress = { e.confianzaIa / 100f },
                            color = ColorExito,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // ---------- Video + mapa ----------
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                ReproductorEvidencia(e.camara, Modifier.weight(1f))
                MiniMapaPerimetro(e.camara, e.ubicacion, color, Modifier.weight(1f))
            }

            // ---------- Detalles ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("DETALLES DEL EVENTO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FilaDetalle("Tipo", e.tipoLegible())
                    HorizontalDivider()
                    FilaDetalle("Zona", "${e.ubicacion} - ${e.detalle}")
                    HorizontalDivider()
                    FilaDetalle("Cámara", e.camara)
                    HorizontalDivider()
                    FilaDetalle(
                        "Duración",
                        if (e.duracionSeg > 0) "${e.duracionSeg} segundos" else "Instantáneo"
                    )
                    HorizontalDivider()
                    FilaDetalle("Estado", e.estado.textoEstado())
                }
            }

            // ---------- Acciones ----------
            if (pendiente) {
                Text("ACCIONES REQUERIDAS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = ColorCritico)
                Button(
                    onClick = {
                        viewModel.confirmarAlerta(e.id)
                        scope.launch { snackbar.showSnackbar("Alerta confirmada y enviada a Central") }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Text("  Confirmar Alerta - Enviar a Central")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.marcarFalsaAlarma(e.id)
                        scope.launch { snackbar.showSnackbar("Marcada como falsa alarma") }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = ColorAdvertencia)
                    Text("  Falsa Alarma", color = ColorAdvertencia)
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = colorEstado(e.estado).copy(alpha = 0.15f))
                ) {
                    Text(
                        text = when (e.estado) {
                            EstadoEvento.ESCALADA -> "Sin respuesta en 2 minutos: se escaló automáticamente a Central y al Jefe de Seguridad."
                            EstadoEvento.FALSA_ALARMA -> "Este evento fue marcado como falsa alarma."
                            EstadoEvento.CONFIRMADA -> "Alerta confirmada y enviada a Central. Evidencia guardada."
                            else -> "Estado actual: ${e.estado.textoEstado()}."
                        },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            OutlinedButton(
                onClick = { navController.navigate(Rutas.incidencia(e.id)) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Text("  Reportar Incidencia con foto")
            }
            OutlinedButton(
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$TELEFONO_CENTRAL")))
                    } catch (ex: ActivityNotFoundException) {
                        scope.launch { snackbar.showSnackbar("Este dispositivo no puede realizar llamadas") }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Text("  Llamar a Central")
            }

            // ---------- Protocolo y tiempo de respuesta ----------
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorCritico.copy(alpha = 0.10f))
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ColorCritico)
                    Text(
                        "Protocolo Insercode: Confirmar en menos de 2 minutos o se escala automáticamente a Central y Jefe de Seguridad.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (pendiente) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = ColorAdvertencia, modifier = Modifier.size(18.dp))
                        Text("TIEMPO DE RESPUESTA", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        "${formatoMinSeg(restante)} restante",
                        style = MaterialTheme.typography.titleSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { (TIEMPO_RESPUESTA_SEG - restante) / TIEMPO_RESPUESTA_SEG.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("00:00", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    Text("02:00 AUTO-ESCALADO", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun FilaInfo(icono: ImageVector, texto: String, monoespaciado: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = texto,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (monoespaciado) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@Composable
private fun FilaDetalle(titulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(titulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 16.dp))
    }
}

// Reproductor simulado de 10 s: la evidencia real vendría del servidor de video
@Composable
private fun ReproductorEvidencia(camara: String, modifier: Modifier = Modifier) {
    var reproduciendo by rememberSaveable { mutableStateOf(false) }
    var segundos by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(reproduciendo) {
        while (reproduciendo) {
            delay(1000)
            segundos++
            if (segundos >= 10) {
                reproduciendo = false
                segundos = 0
            }
        }
    }

    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0B1F14))
            .clickable { reproduciendo = !reproduciendo }
    ) {
        Text(
            "$camara • REC",
            color = ColorCritico,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(10.dp).align(Alignment.TopStart)
        )
        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (reproduciendo) Icons.Default.Videocam else Icons.Default.PlayArrow,
                contentDescription = if (reproduciendo) "Pausar video" else "Reproducir video",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = if (reproduciendo) "Reproduciendo…" else "Ver video de 10s",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Text(
            "00:${segundos.toString().padStart(2, '0')} / 00:10",
            color = Color.LightGray,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(10.dp).align(Alignment.BottomStart)
        )
    }
}

// Mapa esquemático del perímetro con la cámara pulsando
@Composable
private fun MiniMapaPerimetro(camara: String, zona: String, color: Color, modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "pulso")
    val pulso by transicion.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulsoRadio"
    )
    val lineas = MaterialTheme.colorScheme.outline
    Box(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Cuadrícula
            var x = 0f
            while (x < size.width) {
                drawLine(lineas.copy(alpha = 0.2f), Offset(x, 0f), Offset(x, size.height), 1f)
                x += 24f
            }
            var y = 0f
            while (y < size.height) {
                drawLine(lineas.copy(alpha = 0.2f), Offset(0f, y), Offset(size.width, y), 1f)
                y += 24f
            }
            // Perímetro
            val perimetro = Path().apply {
                moveTo(size.width * 0.15f, size.height * 0.25f)
                lineTo(size.width * 0.85f, size.height * 0.25f)
                lineTo(size.width * 0.80f, size.height * 0.75f)
                lineTo(size.width * 0.20f, size.height * 0.75f)
                close()
            }
            drawPath(
                path = perimetro,
                color = lineas,
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
            )
            // Cámara (punto + halo pulsante)
            val centro = Offset(size.width * 0.5f, size.height * 0.4f)
            drawCircle(color.copy(alpha = 0.25f), radius = 22f * pulso, center = centro)
            drawCircle(color, radius = 7f, center = centro)
        }
        Text(
            camara,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.align(Alignment.Center).padding(top = 44.dp)
        )
        Text(
            zona.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(8.dp).align(Alignment.BottomStart)
        )
    }
}
