package com.example.insertcode.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.insertcode.model.CategoriaEvento
import com.example.insertcode.model.EstadoEvento
import com.example.insertcode.navigation.Rutas
import com.example.insertcode.ui.components.BarraInferior
import com.example.insertcode.ui.components.TarjetaEvento
import com.example.insertcode.viewmodels.GuardianViewModel

// Alertas del turno: pendientes (requieren acción) e historial de las ya atendidas
@Composable
fun AlertasScreen(
    navController: NavController,
    viewModel: GuardianViewModel
) {
    val eventos by viewModel.eventos.collectAsState()
    val pendientes by viewModel.alertasPendientes.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val alertas = eventos.filter { it.categoria == CategoriaEvento.ALERTA }
    val lista = if (tab == 0) {
        alertas.filter { it.estado == EstadoEvento.PENDIENTE }
    } else {
        alertas.filter { it.estado != EstadoEvento.PENDIENTE }
    }

    Scaffold(
        bottomBar = { BarraInferior(Rutas.ALERTAS, navController, pendientes) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Alertas", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = if (pendientes == 0) "Sin alertas pendientes" else "$pendientes requieren tu atención",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Pendientes") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Atendidas") })
            }

            if (lista.isEmpty()) {
                Text(
                    text = if (tab == 0) "¡Todo en orden! No tienes alertas por atender." else "Aún no hay alertas atendidas.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = lista, key = { it.id }) { e ->
                    TarjetaEvento(evento = e, onClick = { navController.navigate(Rutas.alertaDetalle(e.id)) })
                }
            }
        }
    }
}
