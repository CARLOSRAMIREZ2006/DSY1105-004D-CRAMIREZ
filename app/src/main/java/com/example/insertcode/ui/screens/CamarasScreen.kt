package com.example.insertcode.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.model.EstadoCamara
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.components.BarraInferior
import com.example.insertcode.ui.components.ChipEtiqueta
import com.example.insertcode.ui.theme.ColorAdvertencia
import com.example.insertcode.ui.theme.ColorExito
import com.example.insertcode.viewmodels.GuardianViewModel

// Lista completa de las cámaras asignadas, con filtro por estado
@Composable
fun CamarasScreen(
    navController: NavController,
    viewModel: GuardianViewModel
) {
    val camaras by viewModel.camaras.collectAsState()
    val pendientes by viewModel.alertasPendientes.collectAsState()
    var filtro by rememberSaveable { mutableIntStateOf(0) } // 0 todas, 1 en línea, 2 sin conexión

    val visibles = when (filtro) {
        1 -> camaras.filter { it.estado == EstadoCamara.EN_LINEA }
        2 -> camaras.filter { it.estado == EstadoCamara.SIN_CONEXION }
        else -> camaras
    }.sortedBy { it.codigo }

    Scaffold(
        bottomBar = { BarraInferior(Rutas.CAMARAS, navController, pendientes) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Mis cámaras", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "${camaras.size} asignadas · ${camaras.count { it.estado == EstadoCamara.EN_LINEA }} en línea",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todas", "En línea", "Sin conexión").forEachIndexed { i, texto ->
                    FilterChip(
                        selected = filtro == i,
                        onClick = { filtro = i },
                        label = { Text(texto) },
                        leadingIcon = if (filtro == i) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = visibles, key = { it.id }) { cam ->
                    val enLinea = cam.estado == EstadoCamara.EN_LINEA
                    val color = if (enLinea) ColorExito else ColorAdvertencia
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { navController.navigate(Rutas.camaraDetalle(cam.id)) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (enLinea) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = null,
                                    tint = color
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(cam.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    "${cam.codigo} · ${cam.zona}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ChipEtiqueta(if (enLinea) "En línea" else "Sin conexión", color)
                        }
                    }
                }
            }
        }
    }
}
