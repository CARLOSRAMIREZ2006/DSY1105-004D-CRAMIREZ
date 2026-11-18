package com.example.insertcode.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.model.DiaFiltro
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.components.BarraInferior
import com.example.insertcode.ui.components.BloqueEstadistica
import com.example.insertcode.ui.components.TarjetaEvento
import com.example.insertcode.ui.theme.ColorAdvertencia
import com.example.insertcode.ui.theme.ColorCritico
import com.example.insertcode.util.ReporteTurno
import com.example.insertcode.util.calcularProgresoTurno
import com.example.insertcode.util.rangoHora
import com.example.insertcode.viewmodels.GuardianViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Historial del turno: resumen, pestañas, búsqueda, filtros y exportación a PDF
@Composable
fun EventosScreen(
    navController: NavController,
    viewModel: GuardianViewModel
) {
    val eventos by viewModel.eventosFiltrados.collectAsState()
    val filtro by viewModel.filtro.collectAsState()
    val resumen by viewModel.resumenTurno.collectAsState()
    val pendientes by viewModel.alertasPendientes.collectAsState()
    val sesion by viewModel.sesion.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var exportando by remember { mutableStateOf(false) }

    val progreso = calcularProgresoTurno()
    val grupos = eventos.groupBy { it.fecha.rangoHora() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Column {
                Button(
                    onClick = {
                        if (exportando) return@Button
                        exportando = true
                        scope.launch {
                            try {
                                val archivo = withContext(Dispatchers.IO) {
                                    ReporteTurno.generar(context, sesion.nombre, eventos)
                                }
                                ReporteTurno.compartir(context, archivo)
                            } catch (e: Exception) {
                                snackbar.showSnackbar("No se pudo generar el reporte")
                            } finally {
                                exportando = false
                            }
                        }
                    },
                    enabled = eventos.isNotEmpty() && !exportando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (exportando) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Text("  Exportar reporte de turno (PDF)")
                    }
                }
                BarraInferior(Rutas.EVENTOS, navController, pendientes)
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("Historial", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Turno actual · 14:00 - 22:00",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ---------- Resumen ----------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BloqueEstadistica("${resumen.eventosHoy}", "eventos hoy", Modifier.weight(1f))
                            BloqueEstadistica("${resumen.criticasHoy}", "críticas", Modifier.weight(1f), ColorCritico)
                            BloqueEstadistica("${resumen.pendientes}", "pendientes", Modifier.weight(1f), ColorAdvertencia)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("PROGRESO DE TURNO", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${progreso.porcentaje}% • ${progreso.restante}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progreso.porcentaje / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            // ---------- Pestañas ----------
            item {
                TabRow(selectedTabIndex = filtro.tab) {
                    listOf("Todos", "Alertas", "Eventos", "Accesos").forEachIndexed { i, titulo ->
                        Tab(
                            selected = filtro.tab == i,
                            onClick = { viewModel.onTabChange(i) },
                            text = { Text(titulo) }
                        )
                    }
                }
            }

            // ---------- Búsqueda ----------
            item {
                OutlinedTextField(
                    value = filtro.busqueda,
                    onValueChange = viewModel::onBusquedaEventosChange,
                    placeholder = { Text("Buscar por cámara, zona o tipo...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (filtro.busqueda.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onBusquedaEventosChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ---------- Filtros rápidos ----------
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { ChipFiltro("Hoy", filtro.dia == DiaFiltro.HOY) { viewModel.onDiaChange(DiaFiltro.HOY) } }
                    item { ChipFiltro("Ayer", filtro.dia == DiaFiltro.AYER) { viewModel.onDiaChange(DiaFiltro.AYER) } }
                    item { ChipFiltro("Cámara 01", filtro.camara01) { viewModel.toggleCamara01() } }
                    item { ChipFiltro("Perímetro", filtro.perimetro) { viewModel.togglePerimetro() } }
                    item { ChipFiltro("Críticas", filtro.criticas) { viewModel.toggleCriticas() } }
                }
            }

            // ---------- Eventos agrupados por franja horaria ----------
            if (eventos.isEmpty()) {
                item {
                    Text(
                        "No hay eventos para los filtros seleccionados.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            }
            grupos.forEach { (rango, lista) ->
                item(key = "hora_$rango") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(rango, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text(
                            "${lista.size} eventos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(items = lista, key = { it.id }) { e ->
                    TarjetaEvento(evento = e, onClick = { navController.navigate(Rutas.alertaDetalle(e.id)) })
                }
            }
        }
    }
}

@Composable
private fun ChipFiltro(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = seleccionado,
        onClick = onClick,
        label = { Text(texto) },
        leadingIcon = if (seleccionado) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else null
    )
}
