package com.example.insertcode.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.data.EventoEntity
import com.example.insertcode.model.Camara
import com.example.insertcode.model.CategoriaEvento
import com.example.insertcode.model.EstadoCamara
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.components.BarraInferior
import com.example.insertcode.ui.components.BloqueEstadistica
import com.example.insertcode.ui.components.ChipEtiqueta
import com.example.insertcode.ui.components.ImagenInteligente
import com.example.insertcode.ui.components.TituloSeccion
import com.example.insertcode.ui.components.colorSeveridad
import com.example.insertcode.ui.components.iconoEvento
import com.example.insertcode.ui.theme.ColorAdvertencia
import com.example.insertcode.ui.theme.ColorCritico
import com.example.insertcode.ui.theme.ColorExito
import com.example.insertcode.ui.utils.rememberAhora
import com.example.insertcode.util.aUri
import com.example.insertcode.util.formatoHora
import com.example.insertcode.util.formatoHoraSeg
import com.example.insertcode.viewmodels.GuardianViewModel
import kotlinx.coroutines.delay

// Panel principal del guardián: estado general, cámaras asignadas y eventos en vivo
@Composable
fun InicioScreen(
    navController: NavController,
    viewModel: GuardianViewModel
) {
    val sesion by viewModel.sesion.collectAsState()
    val camaras by viewModel.camaras.collectAsState()
    val eventos by viewModel.eventos.collectAsState()
    val pendientes by viewModel.alertasPendientes.collectAsState()

    val total = camaras.size
    val activas = camaras.count { it.estado == EstadoCamara.EN_LINEA }
    val atencion = total - activas
    val capacidad = if (total == 0) 0 else (activas * 100) / total

    var ultimaSync by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var sincronizando by remember { mutableStateOf(false) }
    LaunchedEffect(sincronizando) {
        if (sincronizando) {
            delay(1200)
            ultimaSync = System.currentTimeMillis()
            sincronizando = false
        }
    }

    Scaffold(
        bottomBar = { BarraInferior(Rutas.INICIO, navController, pendientes) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Rutas.incidencia()) },
                icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                text = { Text("Reportar Incidencia") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---------- Saludo ----------
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ImagenInteligente(imagenUri = sesion.fotoPath.aUri(), tamano = 48.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hola, ${sesion.nombre}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Turno Activo • Zona Sur",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { navController.navigate(Rutas.ALERTAS) }) {
                        BadgedBox(badge = { if (pendientes > 0) Badge { Text("$pendientes") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alertas")
                        }
                    }
                    ChipEtiqueta("EN LÍNEA", ColorExito)
                }
            }

            // ---------- Estado general ----------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Estado General", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "INSERCODE · GUARDIÁN OS",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BloqueEstadistica("$total", "Cámaras", Modifier.weight(1f))
                            BloqueEstadistica("$activas", "Activas", Modifier.weight(1f), ColorExito)
                            BloqueEstadistica(
                                "$atencion", "Atención", Modifier.weight(1f),
                                if (atencion > 0) ColorAdvertencia else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CAPACIDAD OPERATIVA", style = MaterialTheme.typography.labelSmall)
                            Text("$capacidad%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = { capacidad / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextoSincronizacion(ultimaSync, sincronizando)
                            TextButton(onClick = { sincronizando = true }, enabled = !sincronizando) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Sincronizar")
                            }
                        }
                    }
                }
            }

            // ---------- Cámaras asignadas ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TituloSeccion("Cámaras Asignadas")
                    TextButton(onClick = { navController.navigate(Rutas.CAMARAS) }) { Text("Ver todas") }
                }
            }
            items(items = camaras.take(4).chunked(2)) { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    fila.forEach { cam ->
                        TarjetaCamaraMini(
                            camara = cam,
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate(Rutas.camaraDetalle(cam.id)) }
                        )
                    }
                    if (fila.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }

            // ---------- Eventos en vivo ----------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TituloSeccion("Eventos en vivo")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ColorCritico))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("LIVE FEED", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        eventos.take(4).forEachIndexed { index, e ->
                            FilaEventoVivo(e) { navController.navigate(Rutas.alertaDetalle(e.id)) }
                            if (index < 3) HorizontalDivider()
                        }
                        if (eventos.isEmpty()) {
                            Text("Sin eventos registrados", modifier = Modifier.padding(16.dp))
                        }
                        TextButton(
                            onClick = { navController.navigate(Rutas.EVENTOS) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Ver historial completo") }
                    }
                }
            }

            // ---------- Protocolo ----------
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Protocolo Activo • Nivel 2", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Encriptación E2E · Grabación continua habilitada",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(ColorExito))
                    }
                }
            }
        }
    }
}

@Composable
private fun TextoSincronizacion(ultimaSync: Long, sincronizando: Boolean) {
    val ahora by rememberAhora(15_000L)
    val minutos = maxOf(0L, (ahora - ultimaSync) / 60_000L)
    Text(
        text = if (sincronizando) "Sincronizando…" else "Última sincronización: hace $minutos min",
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun TarjetaCamaraMini(camara: Camara, modifier: Modifier, onClick: () -> Unit) {
    val enLinea = camara.estado == EstadoCamara.EN_LINEA
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (enLinea) ChipEtiqueta("● REC", ColorCritico) else ChipEtiqueta("OFFLINE", ColorAdvertencia)
                Text("HD", style = MaterialTheme.typography.labelSmall)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (enLinea) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (enLinea) {
                val ahora by rememberAhora(1000L)
                Text(ahora.formatoHoraSeg(), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            } else {
                Text("--:--:--", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
            Text(camara.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(camara.zona, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = if (enLinea) "En línea" else "Sin conexión",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (enLinea) ColorExito else ColorAdvertencia
                )
            }
        }
    }
}

@Composable
private fun FilaEventoVivo(evento: EventoEntity, onClick: () -> Unit) {
    val color = colorSeveridad(evento.severidad)
    val etiqueta = when {
        evento.severidad == com.example.insertcode.model.SeveridadEvento.CRITICA -> "CRÍTICA"
        evento.categoria == CategoriaEvento.ALERTA -> "ALERTA"
        else -> "EVENTO"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconoEvento(evento), contentDescription = null, tint = color)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipEtiqueta(etiqueta, color)
                Text(evento.fecha.formatoHora(), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
            Text(evento.titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "${evento.ubicacion} • ${evento.detalle}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
